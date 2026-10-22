package br.com.sumula.estatisticas.internal;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import br.com.sumula.campeonatos.JogadorResumo;
import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.partidas.PartidaResumo;
import br.com.sumula.partidas.TipoDeEvento;

/** Ranking de goleadores. Conta gols de partidas em andamento também (artilharia ao vivo). */
final class Artilharia {

	record Linha(int posicao, UUID jogadorId, String jogador, UUID timeId, String time, int gols) {
	}

	private static final Collator ALFABETICA = Collator.getInstance(Locale.of("pt", "BR"));

	private Artilharia() {
	}

	static List<Linha> calcular(List<TimeResumo> times, List<JogadorResumo> jogadores, List<PartidaResumo> partidas) {
		Map<UUID, String> nomesDosTimes = times.stream().collect(Collectors.toMap(TimeResumo::id, TimeResumo::nome));
		Map<UUID, JogadorResumo> porId = jogadores.stream().collect(Collectors.toMap(JogadorResumo::id, Function.identity()));

		Map<UUID, Integer> gols = new HashMap<>();
		partidas.stream()
			.flatMap(p -> p.eventos().stream())
			.filter(e -> e.tipo() == TipoDeEvento.GOL && e.jogadorId() != null && porId.containsKey(e.jogadorId()))
			.forEach(e -> gols.merge(e.jogadorId(), 1, Integer::sum));

		List<Map.Entry<UUID, Integer>> ordenado = gols.entrySet()
			.stream()
			.sorted(Map.Entry.<UUID, Integer>comparingByValue(Comparator.reverseOrder())
				.thenComparing(e -> porId.get(e.getKey()).nome(), ALFABETICA))
			.toList();

		// empatados dividem a posição (1, 2, 2, 4...)
		int[] posicoes = new int[ordenado.size()];
		for (int i = 0; i < ordenado.size(); i++) {
			boolean empatado = i > 0 && ordenado.get(i).getValue().equals(ordenado.get(i - 1).getValue());
			posicoes[i] = empatado ? posicoes[i - 1] : i + 1;
		}
		return IntStream.range(0, ordenado.size()).mapToObj(i -> {
			JogadorResumo j = porId.get(ordenado.get(i).getKey());
			return new Linha(posicoes[i], j.id(), j.nome(), j.timeId(), nomesDosTimes.get(j.timeId()),
					ordenado.get(i).getValue());
		}).toList();
	}

}
