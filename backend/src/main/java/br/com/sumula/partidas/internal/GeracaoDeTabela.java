package br.com.sumula.partidas.internal;

import br.com.sumula.campeonatos.CampeonatoIniciado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Gera as partidas quando um campeonato é iniciado. Roda de forma assíncrona após o commit,
 * com o evento registrado no outbox (ADR 0005); por isso é idempotente.
 */
@Component
class GeracaoDeTabela {

	private static final Logger log = LoggerFactory.getLogger(GeracaoDeTabela.class);

	private final PartidaRepository partidas;

	GeracaoDeTabela(PartidaRepository partidas) {
		this.partidas = partidas;
	}

	@ApplicationModuleListener
	void quandoCampeonatoIniciar(CampeonatoIniciado evento) {
		if (partidas.existsByCampeonatoId(evento.campeonatoId())) {
			log.info("Tabela do campeonato {} já existe; evento ignorado", evento.campeonatoId());
			return;
		}
		var tabela = GeradorDeTabela.gerar(evento.timeIds(), evento.idaEVolta())
			.stream()
			.map(c -> new Partida(evento.campeonatoId(), c.rodada(), c.mandante(), c.visitante()))
			.toList();
		partidas.saveAll(tabela);
		log.info("Tabela do campeonato {} gerada com {} partidas", evento.campeonatoId(), tabela.size());
	}

}
