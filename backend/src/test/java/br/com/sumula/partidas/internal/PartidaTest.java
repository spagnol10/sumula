package br.com.sumula.partidas.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import br.com.sumula.compartilhado.RegraDeNegocioException;
import br.com.sumula.partidas.TipoDeEvento;
import org.junit.jupiter.api.Test;

class PartidaTest {

	private final UUID mandante = UUID.randomUUID();

	private final UUID visitante = UUID.randomUUID();

	private final UUID artilheiro = UUID.randomUUID();

	private final Partida partida = new Partida(UUID.randomUUID(), 1, mandante, visitante);

	@Test
	void placarDaSumulaEhAContagemDosGols() {
		partida.iniciar();
		assertThat(partida.getGolsMandante()).isZero();

		partida.registrarEvento(TipoDeEvento.GOL, mandante, artilheiro, 10);
		EventoDaPartida golAnulado = partida.registrarEvento(TipoDeEvento.GOL, mandante, null, 30);
		partida.registrarEvento(TipoDeEvento.GOL, visitante, null, 44);
		partida.registrarEvento(TipoDeEvento.CARTAO_AMARELO, visitante, UUID.randomUUID(), 50);
		assertThat(placar()).isEqualTo("2x1");

		partida.removerEvento(golAnulado.getId());
		assertThat(placar()).isEqualTo("1x1");

		partida.encerrar();
		assertThat(partida.getStatus()).isEqualTo(StatusPartida.ENCERRADA);
	}

	@Test
	void naoLancaNadaAntesDeIniciar() {
		assertThatThrownBy(() -> partida.registrarEvento(TipoDeEvento.GOL, mandante, null, 1))
			.isInstanceOf(RegraDeNegocioException.class)
			.hasMessageContaining("Inicie a partida");
		assertThatThrownBy(partida::encerrar).isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void validaTimeMinutoECartaoSemJogador() {
		partida.iniciar();

		assertThatThrownBy(() -> partida.registrarEvento(TipoDeEvento.GOL, UUID.randomUUID(), null, 1))
			.hasMessageContaining("não joga esta partida");
		assertThatThrownBy(() -> partida.registrarEvento(TipoDeEvento.GOL, mandante, null, 201))
			.hasMessageContaining("Minuto inválido");
		assertThatThrownBy(() -> partida.registrarEvento(TipoDeEvento.CARTAO_VERMELHO, mandante, null, 5))
			.hasMessageContaining("Informe o jogador");
	}

	@Test
	void placarDiretoESumulaNaoSeMisturam() {
		partida.registrarResultado(2, 0);

		// cartões depois do jogo valem (suspensões), gols não
		partida.registrarEvento(TipoDeEvento.CARTAO_VERMELHO, visitante, UUID.randomUUID(), null);
		assertThat(placar()).isEqualTo("2x0");
		assertThatThrownBy(() -> partida.registrarEvento(TipoDeEvento.GOL, mandante, null, null))
			.hasMessageContaining("lançado direto");

		Partida comSumula = new Partida(UUID.randomUUID(), 1, mandante, visitante);
		comSumula.iniciar();
		assertThatThrownBy(() -> comSumula.registrarResultado(1, 0)).hasMessageContaining("vem da súmula");
	}

	@Test
	void naoIniciaDuasVezes() {
		partida.iniciar();
		assertThatThrownBy(partida::iniciar).isInstanceOf(RegraDeNegocioException.class);
	}

	private String placar() {
		return partida.getGolsMandante() + "x" + partida.getGolsVisitante();
	}

}
