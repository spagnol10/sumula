package br.com.sumula.partidas.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import br.com.sumula.partidas.internal.GeradorDeTabela.Confronto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GeradorDeTabelaTest {

	@ParameterizedTest(name = "{0} times")
	@ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8, 11, 12, 20 })
	void turnoUnicoTemTodosOsConfrontosUmaVez(int quantidade) {
		List<UUID> times = times(quantidade);

		List<Confronto> tabela = GeradorDeTabela.gerar(times, false);

		int rodadas = quantidade % 2 == 0 ? quantidade - 1 : quantidade;
		assertThat(tabela).hasSize(quantidade * (quantidade - 1) / 2);
		assertThat(tabela).extracting(Confronto::rodada).allMatch(r -> r >= 1 && r <= rodadas);
		assertThat(tabela.stream().map(c -> Set.of(c.mandante(), c.visitante())).distinct())
				.hasSize(tabela.size());
		assertCadaTimeJogaNoMaximoUmaVezPorRodada(tabela);
		assertMandoEquilibrado(tabela, times);
	}

	@ParameterizedTest(name = "{0} times")
	@ValueSource(ints = { 2, 3, 4, 7, 10 })
	void returnoRepeteConfrontosComMandoInvertido(int quantidade) {
		List<UUID> times = times(quantidade);

		List<Confronto> tabela = GeradorDeTabela.gerar(times, true);

		assertThat(tabela).hasSize(quantidade * (quantidade - 1));
		Set<List<UUID>> jogosComMando = new HashSet<>();
		tabela.forEach(c -> jogosComMando.add(List.of(c.mandante(), c.visitante())));
		assertThat(jogosComMando).as("cada mando aparece uma única vez").hasSize(tabela.size());
		assertCadaTimeJogaNoMaximoUmaVezPorRodada(tabela);
		assertMandoEquilibrado(tabela, times);
	}

	@ParameterizedTest(name = "{0} times")
	@ValueSource(ints = { 4, 5, 6, 9, 10, 16 })
	void minimizaJogosSeguidosComMesmoMando(int quantidade) {
		List<UUID> times = times(quantidade);
		List<Confronto> tabela = GeradorDeTabela.gerar(times, false);

		int quebras = 0;
		for (UUID time : times) {
			List<Boolean> emCasa = tabela.stream()
				.filter(c -> c.mandante().equals(time) || c.visitante().equals(time))
				.sorted(Comparator.comparingInt(Confronto::rodada))
				.map(c -> c.mandante().equals(time))
				.toList();
			for (int i = 1; i < emCasa.size(); i++) {
				if (emCasa.get(i).equals(emCasa.get(i - 1))) {
					quebras++;
				}
			}
		}
		// mínimo teórico de um turno: n - 2 com n par; com n ímpar dá para não ter nenhuma
		assertThat(quebras).isEqualTo(quantidade % 2 == 0 ? quantidade - 2 : 0);
	}

	@Test
	void exigePeloMenosDoisTimes() {
		assertThatIllegalArgumentException().isThrownBy(() -> GeradorDeTabela.gerar(times(1), false));
	}

	private static void assertCadaTimeJogaNoMaximoUmaVezPorRodada(List<Confronto> tabela) {
		Map<Integer, Set<UUID>> porRodada = new HashMap<>();
		for (Confronto c : tabela) {
			Set<UUID> jogaram = porRodada.computeIfAbsent(c.rodada(), r -> new HashSet<>());
			assertThat(jogaram.add(c.mandante())).isTrue();
			assertThat(jogaram.add(c.visitante())).isTrue();
		}
	}

	private static void assertMandoEquilibrado(List<Confronto> tabela, List<UUID> times) {
		for (UUID time : times) {
			long emCasa = tabela.stream().filter(c -> c.mandante().equals(time)).count();
			long fora = tabela.stream().filter(c -> c.visitante().equals(time)).count();
			assertThat(Math.abs(emCasa - fora)).as("diferença casa/fora").isLessThanOrEqualTo(1);
		}
	}

	private static List<UUID> times(int quantidade) {
		return IntStream.range(0, quantidade).mapToObj(i -> UUID.randomUUID()).toList();
	}

}
