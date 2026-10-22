package br.com.sumula.partidas.internal;

import java.util.UUID;

import br.com.sumula.campeonatos.Campeonatos;
import br.com.sumula.campeonatos.JogadorResumo;
import br.com.sumula.compartilhado.RecursoNaoEncontradoException;
import br.com.sumula.compartilhado.RegraDeNegocioException;
import br.com.sumula.partidas.PartidaAtualizada;
import br.com.sumula.partidas.TipoDeEvento;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso da súmula. Toda escrita exige a chave do organizador e avisa quem acompanha ao vivo. */
@Service
@Transactional
class PartidaService {

	private final PartidaRepository partidas;

	private final Campeonatos campeonatos;

	private final ApplicationEventPublisher eventos;

	PartidaService(PartidaRepository partidas, Campeonatos campeonatos, ApplicationEventPublisher eventos) {
		this.partidas = partidas;
		this.campeonatos = campeonatos;
		this.eventos = eventos;
	}

	@Transactional(readOnly = true)
	Partida buscar(UUID id) {
		return partidas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Partida", id));
	}

	Partida registrarResultado(UUID id, String chave, int golsMandante, int golsVisitante) {
		Partida partida = paraAlterar(id, chave);
		partida.registrarResultado(golsMandante, golsVisitante);
		return salvar(partida);
	}

	Partida iniciar(UUID id, String chave) {
		Partida partida = paraAlterar(id, chave);
		partida.iniciar();
		return salvar(partida);
	}

	Partida registrarEvento(UUID id, String chave, TipoDeEvento tipo, UUID timeId, UUID jogadorId, Integer minuto) {
		Partida partida = paraAlterar(id, chave);
		if (jogadorId != null) {
			JogadorResumo jogador = campeonatos.jogadores(partida.getCampeonatoId())
				.stream()
				.filter(j -> j.id().equals(jogadorId))
				.findFirst()
				.orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", jogadorId));
			if (!jogador.timeId().equals(timeId)) {
				throw new RegraDeNegocioException(jogador.nome() + " não é jogador do time informado");
			}
		}
		partida.registrarEvento(tipo, timeId, jogadorId, minuto);
		return salvar(partida);
	}

	Partida removerEvento(UUID id, String chave, UUID eventoId) {
		Partida partida = paraAlterar(id, chave);
		partida.removerEvento(eventoId);
		return salvar(partida);
	}

	Partida encerrar(UUID id, String chave) {
		Partida partida = paraAlterar(id, chave);
		partida.encerrar();
		return salvar(partida);
	}

	private Partida paraAlterar(UUID id, String chave) {
		Partida partida = buscar(id);
		campeonatos.exigirOrganizador(partida.getCampeonatoId(), chave);
		return partida;
	}

	private Partida salvar(Partida partida) {
		partidas.flush(); // força o optimistic lock antes de responder
		eventos.publishEvent(new PartidaAtualizada(partida.getCampeonatoId(), partida.getId()));
		return partida;
	}

}
