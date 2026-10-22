package br.com.sumula.estatisticas.internal;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.sumula.campeonatos.JogadorResumo;
import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.partidas.PartidaResumo;
import br.com.sumula.partidas.TipoDeEvento;

/**
 * Quem está suspenso para o próximo jogo do seu time.
 *
 * <p>Regra: um cartão vermelho ou {@value #AMARELOS_PARA_SUSPENSAO} amarelos acumulados geram
 * suspensão de uma partida (os amarelos zeram ao gerar suspensão). A suspensão é cumprida na
 * primeira partida encerrada do time numa rodada posterior à do cartão.
 */
final class Suspensoes {

	static final int AMARELOS_PARA_SUSPENSAO = 3;

	enum Motivo {

		CARTAO_VERMELHO, ACUMULO_DE_AMARELOS

	}

	record Suspensao(UUID jogadorId, String jogador, UUID timeId, String time, Motivo motivo, int rodadaDoCartao) {
	}

	private static final Collator ALFABETICA = Collator.getInstance(Locale.of("pt", "BR"));

	private Suspensoes() {
	}

	static List<Suspensao> calcular(List<TimeResumo> times, List<JogadorResumo> jogadores, List<PartidaResumo> partidas) {
		Map<UUID, String> nomesDosTimes = times.stream().collect(Collectors.toMap(TimeResumo::id, TimeResumo::nome));
		List<PartidaResumo> emOrdem = partidas.stream().sorted(Comparator.comparingInt(PartidaResumo::rodada)).toList();

		List<Suspensao> suspensos = new ArrayList<>();
		for (JogadorResumo jogador : jogadores) {
			int amarelos = 0;
			Motivo pendente = null;
			int rodadaDoCartao = 0;

			for (PartidaResumo partida : emOrdem) {
				boolean jogoDoTime = partida.mandanteId().equals(jogador.timeId())
						|| partida.visitanteId().equals(jogador.timeId());
				if (!jogoDoTime) {
					continue;
				}
				if (pendente != null && partida.encerrada() && partida.rodada() > rodadaDoCartao) {
					pendente = null; // cumpriu a suspensão
				}
				for (PartidaResumo.Evento evento : partida.eventos()) {
					if (!jogador.id().equals(evento.jogadorId())) {
						continue;
					}
					if (evento.tipo() == TipoDeEvento.CARTAO_VERMELHO) {
						pendente = Motivo.CARTAO_VERMELHO;
						rodadaDoCartao = partida.rodada();
					}
					else if (evento.tipo() == TipoDeEvento.CARTAO_AMARELO && ++amarelos == AMARELOS_PARA_SUSPENSAO) {
						amarelos = 0;
						pendente = Motivo.ACUMULO_DE_AMARELOS;
						rodadaDoCartao = partida.rodada();
					}
				}
			}
			if (pendente != null) {
				suspensos.add(new Suspensao(jogador.id(), jogador.nome(), jogador.timeId(),
						nomesDosTimes.get(jogador.timeId()), pendente, rodadaDoCartao));
			}
		}
		suspensos.sort(Comparator.comparing(Suspensao::time, ALFABETICA).thenComparing(Suspensao::jogador, ALFABETICA));
		return suspensos;
	}

}
