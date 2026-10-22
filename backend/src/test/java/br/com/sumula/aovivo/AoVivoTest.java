package br.com.sumula.aovivo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import br.com.sumula.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/** SSE de ponta a ponta, com servidor HTTP de verdade. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AoVivoTest {

	@LocalServerPort
	int porta;

	@Autowired
	Transmissoes transmissoes;

	private final HttpClient http = HttpClient.newHttpClient();

	private String chave;

	@Test
	void avisaQuemAcompanhaQuandoAPartidaMuda() throws Exception {
		String criado = enviar("POST", "/api/v1/campeonatos", "{\"nome\": \"Copa Ao Vivo\"}");
		String id = JsonPath.read(criado, "$.campeonato.id");
		chave = JsonPath.read(criado, "$.chaveOrganizador");
		enviar("POST", "/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"A\"}");
		enviar("POST", "/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"B\"}");
		enviar("POST", "/api/v1/campeonatos/" + id + "/iniciar", "");
		await().atMost(Duration.ofSeconds(10))
			.until(() -> !JsonPath.<List<?>>read(enviar("GET", "/api/v1/campeonatos/" + id + "/partidas", null), "$").isEmpty());
		String partidaId = JsonPath.read(enviar("GET", "/api/v1/campeonatos/" + id + "/partidas", null), "$[0].id");

		List<String> linhas = new CopyOnWriteArrayList<>();
		var stream = http.sendAsync(
				HttpRequest.newBuilder(url("/api/v1/campeonatos/" + id + "/ao-vivo")).header("Accept", "text/event-stream").build(),
				HttpResponse.BodyHandlers.ofLines());
		stream.thenAccept(r -> r.body().forEach(linhas::add));
		await().atMost(Duration.ofSeconds(5)).until(() -> linhas.contains("event:conectado"));
		int conexoes = transmissoes.abertas();
		assertThat(conexoes).isPositive();

		enviar("POST", "/api/v1/partidas/" + partidaId + "/inicio", "");

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			int i = linhas.indexOf("event:partida");
			assertThat(i).isNotNegative();
			assertThat(linhas.get(i + 1)).isEqualTo("data:" + partidaId);
		});

		stream.cancel(true);
	}

	private URI url(String caminho) {
		return URI.create("http://localhost:" + porta + caminho);
	}

	private String enviar(String metodo, String caminho, String json) throws Exception {
		HttpRequest.Builder req = HttpRequest.newBuilder(url(caminho)).header("Content-Type", "application/json");
		if (chave != null) {
			req.header("X-Chave-Organizador", chave);
		}
		req.method(metodo, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
		HttpResponse<String> res = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
		assertThat(res.statusCode()).as(metodo + " " + caminho + ": " + res.body()).isLessThan(300);
		return res.body();
	}

}
