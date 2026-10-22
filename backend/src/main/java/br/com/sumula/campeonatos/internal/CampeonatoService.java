package br.com.sumula.campeonatos.internal;

import java.util.UUID;

import br.com.sumula.campeonatos.CampeonatoIniciado;
import br.com.sumula.compartilhado.RecursoNaoEncontradoException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class CampeonatoService {

	private final CampeonatoRepository campeonatos;

	private final ApplicationEventPublisher eventos;

	CampeonatoService(CampeonatoRepository campeonatos, ApplicationEventPublisher eventos) {
		this.campeonatos = campeonatos;
		this.eventos = eventos;
	}

	Campeonato criar(String nome, boolean idaEVolta) {
		return campeonatos.save(new Campeonato(nome, Slugs.gerar(nome), idaEVolta));
	}

	@Transactional(readOnly = true)
	Campeonato buscar(UUID id) {
		return campeonatos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Campeonato", id));
	}

	Time inscreverTime(UUID campeonatoId, String nome) {
		Time time = buscar(campeonatoId).inscrever(nome);
		campeonatos.flush(); // força o optimistic lock antes de responder
		return time;
	}

	Campeonato iniciar(UUID id) {
		Campeonato campeonato = buscar(id);
		campeonato.iniciar();
		eventos.publishEvent(new CampeonatoIniciado(campeonato.getId(),
				campeonato.getTimes().stream().map(Time::getId).toList(), campeonato.isIdaEVolta()));
		return campeonato;
	}

}
