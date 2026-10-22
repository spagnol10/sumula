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

	record Criado(Campeonato campeonato, String chaveOrganizador) {
	}

	Criado criar(String nome, boolean idaEVolta) {
		ChaveDeOrganizador.Gerada chave = ChaveDeOrganizador.gerar();
		Campeonato campeonato = campeonatos.save(new Campeonato(nome, Slugs.gerar(nome), idaEVolta, chave.hash()));
		return new Criado(campeonato, chave.valor());
	}

	@Transactional(readOnly = true)
	Campeonato buscarPorSlug(String slug) {
		return campeonatos.findBySlug(slug).orElseThrow(() -> new RecursoNaoEncontradoException("Campeonato", slug));
	}

	@Transactional(readOnly = true)
	void verificarChave(UUID id, String chave) {
		buscar(id).exigirOrganizador(chave);
	}

	@Transactional(readOnly = true)
	Campeonato buscar(UUID id) {
		return campeonatos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Campeonato", id));
	}

	Time inscreverTime(UUID campeonatoId, String chave, String nome) {
		Campeonato campeonato = buscar(campeonatoId);
		campeonato.exigirOrganizador(chave);
		Time time = campeonato.inscrever(nome);
		campeonatos.flush(); // força o optimistic lock antes de responder
		return time;
	}

	Campeonato iniciar(UUID id, String chave) {
		Campeonato campeonato = buscar(id);
		campeonato.exigirOrganizador(chave);
		campeonato.iniciar();
		eventos.publishEvent(new CampeonatoIniciado(campeonato.getId(),
				campeonato.getTimes().stream().map(Time::getId).toList(), campeonato.isIdaEVolta()));
		return campeonato;
	}

}
