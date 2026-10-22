package br.com.sumula.partidas.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Gera a tabela de pontos corridos pelo método do círculo (tabelas de Berger).
 *
 * <p>Com {@code n} times, cada turno tem {@code n - 1} rodadas (ou {@code n} se for ímpar,
 * com um time de folga por rodada). A posição 0 fica fixa e as demais giram. O mando é
 * definido pela paridade do par no círculo (e, para a posição fixa, pela rodada), o que
 * garante no máximo um jogo de diferença entre casa e fora e o mínimo de "quebras"
 * (jogos seguidos com o mesmo mando): {@code n - 2} com n par e nenhuma com n ímpar.
 * No returno os confrontos se repetem com o mando invertido.
 */
final class GeradorDeTabela {

	record Confronto(int rodada, UUID mandante, UUID visitante) {
	}

	private GeradorDeTabela() {
	}

	static List<Confronto> gerar(List<UUID> times, boolean idaEVolta) {
		if (times.size() < 2) {
			throw new IllegalArgumentException("São necessários pelo menos 2 times");
		}

		List<UUID> circulo = new ArrayList<>(times);
		if (circulo.size() % 2 != 0) {
			circulo.addFirst(null); // folga fixa na posição 0
		}
		int n = circulo.size();
		int rodadasPorTurno = n - 1;

		List<Confronto> turno = new ArrayList<>();
		for (int r = 0; r < rodadasPorTurno; r++) {
			for (int i = 0; i < n / 2; i++) {
				UUID a = circulo.get(i);
				UUID b = circulo.get(n - 1 - i);
				if (a == null || b == null) {
					continue;
				}
				boolean aEmCasa = (i == 0) ? r % 2 == 0 : i % 2 == 1;
				turno.add(aEmCasa ? new Confronto(r + 1, a, b) : new Confronto(r + 1, b, a));
			}
			// mantém a posição 0 fixa e gira as demais no sentido horário
			Collections.rotate(circulo.subList(1, n), 1);
		}

		if (!idaEVolta) {
			return turno;
		}
		List<Confronto> tabela = new ArrayList<>(turno);
		for (Confronto c : turno) {
			tabela.add(new Confronto(c.rodada() + rodadasPorTurno, c.visitante(), c.mandante()));
		}
		return tabela;
	}

}
