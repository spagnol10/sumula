package br.com.sumula.campeonatos;

import java.util.List;
import java.util.UUID;

import br.com.sumula.campeonatos.internal.Campeonato;
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

	/** Lança {@link br.com.sumula.compartilhado.AcessoNegadoException} se a chave não for a do organizador. */
	public void exigirOrganizador(UUID campeonatoId, String chave) {
		buscar(campeonatoId).exigirOrganizador(chave);
	}

	public List<TimeResumo> times(UUID campeonatoId) {
		return buscar(campeonatoId).getTimes().stream().map(t -> new TimeResumo(t.getId(), t.getNome())).toList();
	}

	public List<JogadorResumo> jogadores(UUID campeonatoId) {
		return buscar(campeonatoId).getTimes()
			.stream()
			.flatMap(t -> t.getJogadores()
				.stream()
				.map(j -> new JogadorResumo(j.getId(), j.getNome(), j.getNumero(), t.getId())))
			.toList();
	}

	private Campeonato buscar(UUID campeonatoId) {
		return campeonatos.findById(campeonatoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Campeonato", campeonatoId));
	}

}
