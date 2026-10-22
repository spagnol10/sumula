package br.com.sumula;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Súmula ao vivo, artilharia e suspensões pela API. */
class SumulaTest extends TesteDeApi {

	@Test
	void sumulaCompletaAlimentaPlacarArtilhariaESuspensoes() throws Exception {
		String id = criarCampeonato("Copa da Súmula", "Vila", "Morro");
		List<String> times = ler(get("/api/v1/campeonatos/" + id), "$.times[*].id");
		String vila = times.get(0);
		String morro = times.get(1);
		String ze = ler(post("/api/v1/campeonatos/" + id + "/times/" + vila + "/jogadores", """
				{"nome": "Zé", "numero": 9}"""), "$.id");
		String bruno = ler(post("/api/v1/campeonatos/" + id + "/times/" + morro + "/jogadores", """
				{"nome": "Bruno", "numero": 10}"""), "$.id");
		assertThat(post("/api/v1/campeonatos/" + id + "/times/" + morro + "/jogadores", """
				{"nome": "Repetido", "numero": 10}""")).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);

		iniciarEAguardarTabela(id, 1);
		String partida = "/api/v1/partidas/" + ler(get("/api/v1/campeonatos/" + id + "/partidas"), "$[0].id");
		String mandante = ler(get(partida), "$.mandante.id");
		String golDoMandante = mandante.equals(vila) ? ze : bruno;
		String visitante = mandante.equals(vila) ? morro : vila;
		String jogadorVisitante = mandante.equals(vila) ? bruno : ze;

		assertThat(post(partida + "/eventos", evento("GOL", mandante, golDoMandante)))
			.hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
			.bodyJson().extractingPath("$.detail").asString().contains("Inicie a partida");

		assertThat(post(partida + "/inicio", "")).hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("EM_ANDAMENTO");
		post(partida + "/eventos", evento("GOL", mandante, golDoMandante));
		MvcTestResult golVisitante = post(partida + "/eventos", evento("GOL", visitante, null));
		MvcTestResult comCartao = post(partida + "/eventos", evento("CARTAO_VERMELHO", visitante, jogadorVisitante));
		assertThat(comCartao).hasStatusOk().bodyJson().extractingPath("$.golsMandante").isEqualTo(1);
		assertThat(comCartao).bodyJson().extractingPath("$.golsVisitante").isEqualTo(1);
		assertThat(comCartao).bodyJson().extractingPath("$.eventos[0].jogador").isEqualTo(mandante.equals(vila) ? "Zé" : "Bruno");

		// jogador de um time lançado como autor de gol do outro
		assertThat(post(partida + "/eventos", evento("GOL", mandante, jogadorVisitante)))
			.hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);

		String golAnulado = ler(golVisitante, "$.eventos[1].id");
		assertThat(delete(partida + "/eventos/" + golAnulado)).hasStatusOk()
			.bodyJson().extractingPath("$.golsVisitante").isEqualTo(0);
		assertThat(put(partida + "/resultado", """
				{"golsMandante": 5, "golsVisitante": 0}""")).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);

		assertThat(post(partida + "/encerramento", "")).hasStatusOk()
			.bodyJson().extractingPath("$.status").isEqualTo("ENCERRADA");

		assertThat(get("/api/v1/campeonatos/" + id + "/classificacao")).bodyJson()
			.extractingPath("$[0].timeId").isEqualTo(mandante);
		MvcTestResult artilharia = get("/api/v1/campeonatos/" + id + "/artilharia");
		assertThat(artilharia).bodyJson().extractingPath("$.length()").isEqualTo(1);
		assertThat(artilharia).bodyJson().extractingPath("$[0].jogadorId").isEqualTo(golDoMandante);
		MvcTestResult suspensoes = get("/api/v1/campeonatos/" + id + "/suspensoes");
		assertThat(suspensoes).bodyJson().extractingPath("$[0].jogadorId").isEqualTo(jogadorVisitante);
		assertThat(suspensoes).bodyJson().extractingPath("$[0].motivo").isEqualTo("CARTAO_VERMELHO");

		chave = null;
		assertThat(post(partida + "/eventos", evento("CARTAO_AMARELO", mandante, golDoMandante)))
			.hasStatus(HttpStatus.FORBIDDEN);
	}

	private static String evento(String tipo, String timeId, String jogadorId) {
		return "{\"tipo\": \"" + tipo + "\", \"timeId\": \"" + timeId + "\""
				+ (jogadorId == null ? "" : ", \"jogadorId\": \"" + jogadorId + "\"") + "}";
	}

}
