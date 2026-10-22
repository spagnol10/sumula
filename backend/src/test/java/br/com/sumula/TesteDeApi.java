package br.com.sumula;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Base dos testes de API contra um Postgres real (Testcontainers). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
abstract class TesteDeApi {

	@Autowired
	MockMvcTester mvc;

	/** Chave enviada nas requisições de escrita; cada teste define a sua. */
	String chave;

	/** Cria um campeonato, guarda a chave em {@link #chave} e devolve o id. */
	String criarCampeonato(String nome, String... times) throws Exception {
		MvcTestResult criado = post("/api/v1/campeonatos", "{\"nome\": \"" + nome + "\"}");
		chave = ler(criado, "$.chaveOrganizador");
		String id = ler(criado, "$.campeonato.id");
		for (String time : times) {
			post("/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"" + time + "\"}");
		}
		return id;
	}

	void iniciarEAguardarTabela(String campeonatoId, int partidasEsperadas) {
		post("/api/v1/campeonatos/" + campeonatoId + "/iniciar", "");
		await().atMost(Duration.ofSeconds(10))
			.untilAsserted(() -> assertThat(get("/api/v1/campeonatos/" + campeonatoId + "/partidas")).bodyJson()
				.extractingPath("$.length()")
				.isEqualTo(partidasEsperadas));
	}

	MvcTestResult get(String uri) {
		return mvc.get().uri(uri).exchange();
	}

	MvcTestResult post(String uri, String json) {
		var req = mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json);
		return (chave == null ? req : req.header("X-Chave-Organizador", chave)).exchange();
	}

	MvcTestResult put(String uri, String json) {
		var req = mvc.put().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json);
		return (chave == null ? req : req.header("X-Chave-Organizador", chave)).exchange();
	}

	MvcTestResult delete(String uri) {
		var req = mvc.delete().uri(uri);
		return (chave == null ? req : req.header("X-Chave-Organizador", chave)).exchange();
	}

	static <T> T ler(MvcTestResult resultado, String caminho) throws Exception {
		return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), caminho);
	}

}
