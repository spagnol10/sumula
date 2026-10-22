package br.com.sumula.estatisticas.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import br.com.sumula.campeonatos.JogadorResumo;
import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.estatisticas.internal.Artilharia.Linha;
import br.com.sumula.partidas.PartidaResumo;
import br.com.sumula.partidas.PartidaResumo.Evento;
import br.com.sumula.partidas.TipoDeEvento;
import org.junit.jupiter.api.Test;

class ArtilhariaTest {

	private final TimeResumo vila = new TimeResumo(UUID.randomUUID(), "Unidos da Vila");

	private final TimeResumo morro = new TimeResumo(UUID.randomUUID(), "Juventude do Morro");

	private final JogadorResumo ze = new JogadorResumo(UUID.randomUUID(), "Zé", 9, vila.id());

	private final JogadorResumo bruno = new JogadorResumo(UUID.randomUUID(), "Bruno", 10, morro.id());

	private final JogadorResumo ana = new JogadorResumo(UUID.randomUUID(), "Ana", 7, morro.id());

	@Test
	void ordenaPorGolsEEmpatadosDividemAPosicao() {
		PartidaResumo jogo = new PartidaResumo(UUID.randomUUID(), 1, vila.id(), morro.id(), false,
				List.of(gol(ze), gol(bruno), gol(ze), gol(ana), cartao(bruno), golSemAutor(vila)));

		List<Linha> artilharia = Artilharia.calcular(List.of(vila, morro), List.of(ze, bruno, ana), List.of(jogo));

		assertThat(artilharia).extracting(Linha::jogador).containsExactly("Zé", "Ana", "Bruno");
		assertThat(artilharia).extracting(Linha::posicao).containsExactly(1, 2, 2);
		assertThat(artilharia).extracting(Linha::gols).containsExactly(2, 1, 1);
		assertThat(artilharia.getFirst().time()).isEqualTo("Unidos da Vila");
	}

	@Test
	void semGolsComAutorFicaVazia() {
		PartidaResumo jogo = new PartidaResumo(UUID.randomUUID(), 1, vila.id(), morro.id(), true,
				List.of(golSemAutor(vila)));
		assertThat(Artilharia.calcular(List.of(vila, morro), List.of(ze), List.of(jogo))).isEmpty();
	}

	private static Evento gol(JogadorResumo j) {
		return new Evento(TipoDeEvento.GOL, j.timeId(), j.id(), null);
	}

	private static Evento golSemAutor(TimeResumo t) {
		return new Evento(TipoDeEvento.GOL, t.id(), null, null);
	}

	private static Evento cartao(JogadorResumo j) {
		return new Evento(TipoDeEvento.CARTAO_AMARELO, j.timeId(), j.id(), null);
	}

}
