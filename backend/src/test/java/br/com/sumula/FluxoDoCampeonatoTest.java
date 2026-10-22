package br.com.sumula;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Fluxo completo da Fase 1 contra um Postgres real (Testcontainers). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class FluxoDoCampeonatoTest {

	@Autowired
	MockMvcTester mvc;

	/** Chave enviada nas requisições de escrita; cada teste define a sua. */
	private String chave;

	@Test
	void doCadastroAClassificacao() throws Exception {
		MvcTestResult criado = post("/api/v1/campeonatos", """
				{"nome": "Copa da Vila"}""");
		assertThat(criado).hasStatus(HttpStatus.CREATED).bodyJson().extractingPath("$.campeonato.status")
			.isEqualTo("INSCRICOES_ABERTAS");
		assertThat(criado).bodyJson().extractingPath("$.campeonato.slug").asString().startsWith("copa-da-vila-");
		String id = ler(criado, "$.campeonato.id");
		chave = ler(criado, "$.chaveOrganizador");
		String slug = ler(criado, "$.campeonato.slug");
		assertThat(get("/api/v1/campeonatos/slug/" + slug)).hasStatusOk().bodyJson().extractingPath("$.id").isEqualTo(id);

		for (String time : List.of("Unidos da Vila", "Real Várzea", "Juventude do Morro", "Estrela FC")) {
			assertThat(post("/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"" + time + "\"}"))
				.hasStatus(HttpStatus.CREATED);
		}
		assertThat(post("/api/v1/campeonatos/" + id + "/times", """
				{"nome": "estrela fc"}""")).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
			.bodyJson().extractingPath("$.detail").asString().contains("Já existe");

		assertThat(post("/api/v1/campeonatos/" + id + "/iniciar", "")).hasStatusOk()
			.bodyJson().extractingPath("$.status").isEqualTo("EM_ANDAMENTO");

		// a tabela é gerada de forma assíncrona pelo módulo de partidas
		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(get("/api/v1/campeonatos/" + id + "/partidas"))
			.bodyJson().extractingPath("$.length()").isEqualTo(6));

		MvcTestResult partidas = get("/api/v1/campeonatos/" + id + "/partidas");
		String partidaId = ler(partidas, "$[0].id");
		String mandante = ler(partidas, "$[0].mandante.nome");

		MvcTestResult resultado = put("/api/v1/partidas/" + partidaId + "/resultado", """
				{"golsMandante": 2, "golsVisitante": 1}""");
		assertThat(resultado).hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("ENCERRADA");

		MvcTestResult classificacao = get("/api/v1/campeonatos/" + id + "/classificacao");
		assertThat(classificacao).hasStatusOk().bodyJson().extractingPath("$.length()").isEqualTo(4);
		assertThat(classificacao).bodyJson().extractingPath("$[0].time").isEqualTo(mandante);
		assertThat(classificacao).bodyJson().extractingPath("$[0].pontos").isEqualTo(3);
	}

	@Test
	void naoIniciaDuasVezes() throws Exception {
		MvcTestResult criado = post("/api/v1/campeonatos", """
				{"nome": "Torneio Relâmpago", "idaEVolta": true}""");
		String id = ler(criado, "$.campeonato.id");
		chave = ler(criado, "$.chaveOrganizador");
		post("/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"A\"}");
		post("/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"B\"}");

		assertThat(post("/api/v1/campeonatos/" + id + "/iniciar", "")).hasStatusOk();
		assertThat(post("/api/v1/campeonatos/" + id + "/iniciar", "")).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(get("/api/v1/campeonatos/" + id + "/partidas"))
			.bodyJson().extractingPath("$.length()").isEqualTo(2));
	}

	@Test
	void alteracoesExigemAChaveDoOrganizador() throws Exception {
		MvcTestResult criado = post("/api/v1/campeonatos", """
				{"nome": "Copa Protegida"}""");
		String id = ler(criado, "$.campeonato.id");
		String chaveCerta = ler(criado, "$.chaveOrganizador");

		chave = null;
		assertThat(post("/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"Intruso\"}")).hasStatus(HttpStatus.FORBIDDEN);
		chave = "chave-errada";
		assertThat(post("/api/v1/campeonatos/" + id + "/iniciar", "")).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(post("/api/v1/campeonatos/" + id + "/chave/verificacao", "")).hasStatus(HttpStatus.FORBIDDEN);

		chave = chaveCerta;
		assertThat(post("/api/v1/campeonatos/" + id + "/chave/verificacao", "")).hasStatus(HttpStatus.NO_CONTENT);
		post("/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"A\"}");
		post("/api/v1/campeonatos/" + id + "/times", "{\"nome\": \"B\"}");
		post("/api/v1/campeonatos/" + id + "/iniciar", "");
		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(get("/api/v1/campeonatos/" + id + "/partidas"))
			.bodyJson().extractingPath("$.length()").isEqualTo(1));
		String partidaId = ler(get("/api/v1/campeonatos/" + id + "/partidas"), "$[0].id");

		chave = null;
		assertThat(put("/api/v1/partidas/" + partidaId + "/resultado", """
				{"golsMandante": 9, "golsVisitante": 0}""")).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void validaEntradaERecursoInexistente() {
		assertThat(post("/api/v1/campeonatos", """
				{"nome": "  "}""")).hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson().extractingPath("$.detail").isEqualTo("nome: não deve estar em branco");
		assertThat(post("/api/v1/campeonatos", "{\"nome\": ")).hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson().extractingPath("$.detail").asString().startsWith("Corpo da requisição inválido");
		assertThat(get("/api/v1/campeonatos/" + UUID.randomUUID())).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson().extractingPath("$.detail").asString().contains("Campeonato");
	}

	private MvcTestResult get(String uri) {
		return mvc.get().uri(uri).exchange();
	}

	private MvcTestResult post(String uri, String json) {
		var req = mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json);
		return (chave == null ? req : req.header("X-Chave-Organizador", chave)).exchange();
	}

	private MvcTestResult put(String uri, String json) {
		var req = mvc.put().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json);
		return (chave == null ? req : req.header("X-Chave-Organizador", chave)).exchange();
	}

	private static String ler(MvcTestResult resultado, String caminho) throws Exception {
		return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), caminho);
	}

}
