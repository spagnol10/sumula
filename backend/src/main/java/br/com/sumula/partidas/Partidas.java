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

}
