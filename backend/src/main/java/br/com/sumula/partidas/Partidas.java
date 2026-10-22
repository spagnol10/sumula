package br.com.sumula.partidas;

import java.util.List;
import java.util.UUID;

import br.com.sumula.partidas.internal.PartidaRepository;
import br.com.sumula.partidas.internal.StatusPartida;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** API pública do módulo de partidas para os demais módulos. */
@Service
@Transactional(readOnly = true)
public class Partidas {

	private final PartidaRepository partidas;

	Partidas(PartidaRepository partidas) {
		this.partidas = partidas;
	}

	public List<ResultadoPartida> encerradas(UUID campeonatoId) {
		return partidas.findByCampeonatoIdAndStatus(campeonatoId, StatusPartida.ENCERRADA)
			.stream()
			.map(p -> new ResultadoPartida(p.getMandanteId(), p.getVisitanteId(), p.getGolsMandante(),
					p.getGolsVisitante()))
			.toList();
	}

	/** Todas as partidas com a súmula, em ordem de rodada. */
	public List<PartidaResumo> comSumula(UUID campeonatoId) {
		return partidas.findByCampeonatoIdOrderByRodadaAsc(campeonatoId)
			.stream()
			.map(p -> new PartidaResumo(p.getId(), p.getRodada(), p.getMandanteId(), p.getVisitanteId(),
					p.getStatus() == StatusPartida.ENCERRADA,
					p.getEventos()
						.stream()
						.map(e -> new PartidaResumo.Evento(e.getTipo(), e.getTimeId(), e.getJogadorId(), e.getMinuto()))
						.toList()))
			.toList();
	}

}
