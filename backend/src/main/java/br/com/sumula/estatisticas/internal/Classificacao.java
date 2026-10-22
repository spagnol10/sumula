package br.com.sumula.estatisticas.internal;

import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.partidas.ResultadoPartida;

/**
 * Classificação de pontos corridos: vitória 3, empate 1, derrota 0.
 *
 * <p>Desempate: pontos, vitórias, saldo de gols, gols marcados e, por fim, ordem alfabética
 * (para a tabela ser estável). Confronto direto e cartões entram quando a súmula tiver
 * esses dados.
 */
final class Classificacao {

	record Linha(int posicao, UUID timeId, String time, int pontos, int jogos, int vitorias, int empates,
			int derrotas, int golsPro, int golsContra, int saldo, int aproveitamento) {
	}

	private static final Collator ALFABETICA = Collator.getInstance(Locale.of("pt", "BR"));

	private static final Comparator<Acumulado> CRITERIOS = Comparator.comparingInt(Acumulado::pontos)
		.thenComparingInt(a -> a.vitorias)
		.thenComparingInt(Acumulado::saldo)
		.thenComparingInt(a -> a.golsPro)
		.reversed()
		.thenComparing(a -> a.nome, ALFABETICA);

	private Classificacao() {
	}

	static List<Linha> calcular(List<TimeResumo> times, List<ResultadoPartida> resultados) {
		Map<UUID, Acumulado> tabela = new LinkedHashMap<>();
		times.forEach(t -> tabela.put(t.id(), new Acumulado(t.id(), t.nome())));

		for (ResultadoPartida r : resultados) {
			Acumulado mandante = tabela.get(r.mandanteId());
			Acumulado visitante = tabela.get(r.visitanteId());
			if (mandante == null || visitante == null) {
				continue; // time removido do campeonato
			}
			mandante.registrar(r.golsMandante(), r.golsVisitante());
			visitante.registrar(r.golsVisitante(), r.golsMandante());
		}

		List<Acumulado> ordenados = tabela.values().stream().sorted(CRITERIOS).toList();
		return IntStream.range(0, ordenados.size()).mapToObj(i -> ordenados.get(i).linha(i + 1)).toList();
	}

	private static final class Acumulado {

		private final UUID id;

		private final String nome;

		private int vitorias;

		private int empates;

		private int derrotas;

		private int golsPro;

		private int golsContra;

		Acumulado(UUID id, String nome) {
			this.id = id;
			this.nome = nome;
		}

		void registrar(int marcados, int sofridos) {
			golsPro += marcados;
			golsContra += sofridos;
			if (marcados > sofridos) {
				vitorias++;
			}
			else if (marcados == sofridos) {
				empates++;
			}
			else {
				derrotas++;
			}
		}

		int pontos() {
			return vitorias * 3 + empates;
		}

		int saldo() {
			return golsPro - golsContra;
		}

		Linha linha(int posicao) {
			int jogos = vitorias + empates + derrotas;
			int aproveitamento = jogos == 0 ? 0 : Math.round(pontos() * 100f / (jogos * 3));
			return new Linha(posicao, id, nome, pontos(), jogos, vitorias, empates, derrotas, golsPro, golsContra,
					saldo(), aproveitamento);
		}

	}

}
