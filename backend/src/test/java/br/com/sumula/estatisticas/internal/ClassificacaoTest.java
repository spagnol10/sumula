package br.com.sumula.estatisticas.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.estatisticas.internal.Classificacao.Linha;
import br.com.sumula.partidas.ResultadoPartida;
import org.junit.jupiter.api.Test;

class ClassificacaoTest {

	private final TimeResumo alfa = time("Alfa");

	private final TimeResumo beta = time("Beta");

	private final TimeResumo gama = time("Gama");

	private final TimeResumo delta = time("Delta");

	@Test
	void somaPontosEEstatisticas() {
		List<Linha> tabela = Classificacao.calcular(List.of(alfa, beta, gama),
				List.of(jogo(alfa, 2, beta, 0), jogo(beta, 1, gama, 1), jogo(gama, 0, alfa, 3)));

		assertThat(tabela).first().satisfies(l -> {
			assertThat(l.posicao()).isEqualTo(1);
			assertThat(l.time()).isEqualTo("Alfa");
			assertThat(l.pontos()).isEqualTo(6);
			assertThat(l.jogos()).isEqualTo(2);
			assertThat(l.golsPro()).isEqualTo(5);
			assertThat(l.saldo()).isEqualTo(5);
			assertThat(l.aproveitamento()).isEqualTo(100);
		});
		// Beta e Gama com 1 ponto: Beta tem saldo -2, Gama -3
		assertThat(tabela).extracting(Linha::time).containsExactly("Alfa", "Beta", "Gama");
	}

	@Test
	void desempataPorVitoriasDepoisSaldoDepoisGolsPro() {
		// Alfa e Beta com 4 pontos e 1 vitória cada: o saldo decide (Beta +3, Alfa +1)
		List<Linha> porSaldo = Classificacao.calcular(List.of(alfa, beta, gama, delta),
				List.of(jogo(alfa, 1, gama, 0), jogo(alfa, 0, delta, 0), jogo(beta, 3, gama, 0),
						jogo(beta, 1, delta, 1)));
		assertThat(porSaldo).extracting(Linha::time).startsWith("Beta", "Alfa");

		// Alfa e Gama com 3 pontos, 1 vitória e saldo +1: Gama marcou mais gols
		List<Linha> porGolsPro = Classificacao.calcular(List.of(alfa, beta, gama, delta),
				List.of(jogo(alfa, 1, beta, 0), jogo(gama, 3, delta, 2)));
		assertThat(porGolsPro).extracting(Linha::time).startsWith("Gama", "Alfa");
	}

	@Test
	void vitoriasValemMaisQueSaldoComPontosIguais() {
		// Alfa: 1V 2D = 3 pts, saldo -9. Beta: 3E = 3 pts, saldo 0. A vitória vem antes do saldo.
		List<Linha> tabela = Classificacao.calcular(List.of(alfa, beta, gama, delta),
				List.of(jogo(alfa, 1, gama, 0), jogo(delta, 5, alfa, 0), jogo(gama, 5, alfa, 0),
						jogo(beta, 0, gama, 0), jogo(beta, 1, delta, 1), jogo(beta, 2, gama, 2)));
		assertThat(tabela).extracting(Linha::time).containsExactly("Gama", "Delta", "Alfa", "Beta");
		assertThat(tabela.get(2).saldo()).isLessThan(tabela.get(3).saldo());
	}

	@Test
	void semJogosOrdenaAlfabeticamenteComAcentos() {
		TimeResumo agua = time("Água Santa");
		List<Linha> tabela = Classificacao.calcular(List.of(beta, agua, alfa), List.of());

		assertThat(tabela).extracting(Linha::time).containsExactly("Água Santa", "Alfa", "Beta");
		assertThat(tabela).allSatisfy(l -> assertThat(l.aproveitamento()).isZero());
	}

	private static TimeResumo time(String nome) {
		return new TimeResumo(UUID.randomUUID(), nome);
	}

	private static ResultadoPartida jogo(TimeResumo mandante, int gm, TimeResumo visitante, int gv) {
		return new ResultadoPartida(mandante.id(), visitante.id(), gm, gv);
	}

}
