package br.com.sumula.campeonatos;

import java.util.List;
import java.util.UUID;

import br.com.sumula.campeonatos.internal.CampeonatoRepository;
import br.com.sumula.compartilhado.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** API pública do módulo de campeonatos para os demais módulos. */
@Service
@Transactional(readOnly = true)
public class Campeonatos {

	private final CampeonatoRepository campeonatos;

	Campeonatos(CampeonatoRepository campeonatos) {
		this.campeonatos = campeonatos;
	}

	public List<TimeResumo> times(UUID campeonatoId) {
		return campeonatos.findById(campeonatoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Campeonato", campeonatoId))
			.getTimes()
			.stream()
			.map(t -> new TimeResumo(t.getId(), t.getNome()))
			.toList();
	}

}
