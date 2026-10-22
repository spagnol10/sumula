package br.com.sumula.campeonatos.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.sumula.compartilhado.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

class CampeonatoTest {

	private final Campeonato campeonato = new Campeonato("  Copa da Vila ", "copa-da-vila-abc123", false);

	@Test
	void comecaComInscricoesAbertas() {
		assertThat(campeonato.getNome()).isEqualTo("Copa da Vila");
		assertThat(campeonato.getStatus()).isEqualTo(StatusCampeonato.INSCRICOES_ABERTAS);
	}

	@Test
	void naoAceitaTimesComMesmoNomeIgnorandoCaixa() {
		campeonato.inscrever("Unidos da Vila");

		assertThatThrownBy(() -> campeonato.inscrever(" unidos DA vila "))
			.isInstanceOf(RegraDeNegocioException.class)
			.hasMessageContaining("Já existe");
	}

	@Test
	void precisaDeDoisTimesParaIniciar() {
		campeonato.inscrever("Unidos da Vila");

		assertThatThrownBy(campeonato::iniciar).isInstanceOf(RegraDeNegocioException.class)
			.hasMessageContaining("pelo menos 2 times");

		campeonato.inscrever("Real Várzea");
		campeonato.iniciar();
		assertThat(campeonato.getStatus()).isEqualTo(StatusCampeonato.EM_ANDAMENTO);
	}

	@Test
	void fechaInscricoesAoIniciar() {
		campeonato.inscrever("Unidos da Vila");
		campeonato.inscrever("Real Várzea");
		campeonato.iniciar();

		assertThatThrownBy(() -> campeonato.inscrever("Atrasados FC")).isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(campeonato::iniciar).isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void limitaQuantidadeDeTimes() {
		for (int i = 0; i < Campeonato.MAXIMO_DE_TIMES; i++) {
			campeonato.inscrever("Time " + i);
		}

		assertThatThrownBy(() -> campeonato.inscrever("Mais um")).isInstanceOf(RegraDeNegocioException.class)
			.hasMessageContaining("limite");
	}

}
