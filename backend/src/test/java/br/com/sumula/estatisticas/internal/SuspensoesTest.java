package br.com.sumula.estatisticas.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import br.com.sumula.campeonatos.JogadorResumo;
import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.estatisticas.internal.Suspensoes.Motivo;
import br.com.sumula.estatisticas.internal.Suspensoes.Suspensao;
import br.com.sumula.partidas.PartidaResumo;
import br.com.sumula.partidas.PartidaResumo.Evento;
import br.com.sumula.partidas.TipoDeEvento;
import org.junit.jupiter.api.Test;

class SuspensoesTest {

	private final TimeResumo vila = new TimeResumo(UUID.randomUUID(), "Vila");

	private final TimeResumo morro = new TimeResumo(UUID.randomUUID(), "Morro");

	private final JogadorResumo ze = new JogadorResumo(UUID.randomUUID(), "Zé", 4, vila.id());

	private final List<TimeResumo> times = List.of(vila, morro);

	@Test
	void vermelhoSuspendeParaOProximoJogo() {
		var rodada1 = jogo(1, true, vermelho(ze));

		assertThat(Suspensoes.calcular(times, List.of(ze), List.of(rodada1)))
			.singleElement()
			.satisfies(s -> {
				assertThat(s.motivo()).isEqualTo(Motivo.CARTAO_VERMELHO);
				assertThat(s.rodadaDoCartao()).isEqualTo(1);
				assertThat(s.time()).isEqualTo("Vila");
			});
	}

	@Test
	void suspensaoEhCumpridaNoJogoSeguinteEncerrado() {
		var rodada1 = jogo(1, true, vermelho(ze));
		var rodada2Agendada = jogo(2, false);
		assertThat(Suspensoes.calcular(times, List.of(ze), List.of(rodada1, rodada2Agendada))).hasSize(1);

		var rodada2Encerrada = jogo(2, true);
		assertThat(Suspensoes.calcular(times, List.of(ze), List.of(rodada2Encerrada, rodada1))).isEmpty();
	}

	@Test
	void tresAmarelosAcumuladosSuspendemEZeramAContagem() {
		var r1 = jogo(1, true, amarelo(ze));
		var r2 = jogo(2, true, amarelo(ze));
		assertThat(Suspensoes.calcular(times, List.of(ze), List.of(r1, r2))).isEmpty();

		var r3 = jogo(3, true, amarelo(ze));
		List<Suspensao> aposTerceiro = Suspensoes.calcular(times, List.of(ze), List.of(r1, r2, r3));
		assertThat(aposTerceiro).extracting(Suspensao::motivo).containsExactly(Motivo.ACUMULO_DE_AMARELOS);

		// cumpre na rodada 4; um amarelo na rodada 5 não suspende de novo
		var r4 = jogo(4, true);
		var r5 = jogo(5, true, amarelo(ze));
		assertThat(Suspensoes.calcular(times, List.of(ze), List.of(r1, r2, r3, r4, r5))).isEmpty();
	}

	@Test
	void jogosDeOutrosTimesNaoContamComoCumprimento() {
		JogadorResumo outro = new JogadorResumo(UUID.randomUUID(), "Outro", 1, UUID.randomUUID());
		var rodada1 = jogo(1, true, vermelho(ze));
		var outroJogo = new PartidaResumo(UUID.randomUUID(), 2, outro.timeId(), UUID.randomUUID(), true, List.of());

		assertThat(Suspensoes.calcular(times, List.of(ze, outro), List.of(rodada1, outroJogo)))
			.extracting(Suspensao::jogador)
			.containsExactly("Zé");
	}

	private PartidaResumo jogo(int rodada, boolean encerrada, Evento... eventos) {
		return new PartidaResumo(UUID.randomUUID(), rodada, vila.id(), morro.id(), encerrada, List.of(eventos));
	}

	private static Evento vermelho(JogadorResumo j) {
		return new Evento(TipoDeEvento.CARTAO_VERMELHO, j.timeId(), j.id(), null);
	}

	private static Evento amarelo(JogadorResumo j) {
		return new Evento(TipoDeEvento.CARTAO_AMARELO, j.timeId(), j.id(), null);
	}

}
