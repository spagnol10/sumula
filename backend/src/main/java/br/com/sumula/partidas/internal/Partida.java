package br.com.sumula.partidas.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import br.com.sumula.compartilhado.RecursoNaoEncontradoException;
import br.com.sumula.compartilhado.RegraDeNegocioException;
import br.com.sumula.partidas.TipoDeEvento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Times e campeonato são referenciados só por id: pertencem a outro módulo (ADR 0002).
 *
 * <p>O placar tem duas origens mutuamente exclusivas: o lançamento direto
 * ({@link #registrarResultado}), para quem não usa súmula, ou a contagem dos gols da súmula,
 * a partir de {@link #iniciar}. Cartões são aceitos nos dois casos, porque alimentam as suspensões.
 */
@Entity
@Table(name = "partidas")
public class Partida {

	private static final int MINUTO_MAXIMO = 200;

	@Id
	private UUID id;

	private UUID campeonatoId;

	private int rodada;

	private UUID mandanteId;

	private UUID visitanteId;

	private Integer golsMandante;

	private Integer golsVisitante;

	@Enumerated(EnumType.STRING)
	private StatusPartida status;

	private boolean placarPelaSumula;

	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("registradoEm")
	private List<EventoDaPartida> eventos = new ArrayList<>();

	@Version
	private Long versao;

	protected Partida() {
	}

	Partida(UUID campeonatoId, int rodada, UUID mandanteId, UUID visitanteId) {
		this.id = UUID.randomUUID();
		this.campeonatoId = campeonatoId;
		this.rodada = rodada;
		this.mandanteId = mandanteId;
		this.visitanteId = visitanteId;
		this.status = StatusPartida.AGENDADA;
	}

	/** Registra o placar final sem súmula; pode ser chamado de novo para corrigir. */
	void registrarResultado(int golsMandante, int golsVisitante) {
		if (golsMandante < 0 || golsVisitante < 0) {
			throw new RegraDeNegocioException("O placar não pode ser negativo");
		}
		if (placarPelaSumula) {
			throw new RegraDeNegocioException("O placar desta partida vem da súmula: ajuste os gols por lá");
		}
		this.golsMandante = golsMandante;
		this.golsVisitante = golsVisitante;
		this.status = StatusPartida.ENCERRADA;
	}

	void iniciar() {
		if (status != StatusPartida.AGENDADA) {
			throw new RegraDeNegocioException("A partida já foi iniciada");
		}
		status = StatusPartida.EM_ANDAMENTO;
		placarPelaSumula = true;
		recalcularPlacar();
	}

	/** Aceita lançamentos durante o jogo e depois dele (súmula passada a limpo). */
	EventoDaPartida registrarEvento(TipoDeEvento tipo, UUID timeId, UUID jogadorId, Integer minuto) {
		if (status == StatusPartida.AGENDADA) {
			throw new RegraDeNegocioException("Inicie a partida antes de lançar gols e cartões");
		}
		if (tipo == TipoDeEvento.GOL && !placarPelaSumula) {
			throw new RegraDeNegocioException("O placar desta partida foi lançado direto e não pode receber gols pela súmula");
		}
		if (!timeId.equals(mandanteId) && !timeId.equals(visitanteId)) {
			throw new RegraDeNegocioException("O time informado não joga esta partida");
		}
		if (minuto != null && (minuto < 0 || minuto > MINUTO_MAXIMO)) {
			throw new RegraDeNegocioException("Minuto inválido: " + minuto);
		}
		if (tipo != TipoDeEvento.GOL && jogadorId == null) {
			throw new RegraDeNegocioException("Informe o jogador que recebeu o cartão");
		}
		EventoDaPartida evento = new EventoDaPartida(this, tipo, timeId, jogadorId, minuto);
		eventos.add(evento);
		recalcularPlacar();
		return evento;
	}

	void removerEvento(UUID eventoId) {
		if (!eventos.removeIf(e -> e.getId().equals(eventoId))) {
			throw new RecursoNaoEncontradoException("Evento", eventoId);
		}
		recalcularPlacar();
	}

	void encerrar() {
		if (status != StatusPartida.EM_ANDAMENTO) {
			throw new RegraDeNegocioException("Só é possível encerrar uma partida em andamento");
		}
		status = StatusPartida.ENCERRADA;
	}

	private void recalcularPlacar() {
		if (placarPelaSumula) {
			golsMandante = contarGols(mandanteId);
			golsVisitante = contarGols(visitanteId);
		}
	}

	private int contarGols(UUID timeId) {
		return (int) eventos.stream()
			.filter(e -> e.getTipo() == TipoDeEvento.GOL && e.getTimeId().equals(timeId))
			.count();
	}

	public UUID getId() {
		return id;
	}

	public UUID getCampeonatoId() {
		return campeonatoId;
	}

	public int getRodada() {
		return rodada;
	}

	public UUID getMandanteId() {
		return mandanteId;
	}

	public UUID getVisitanteId() {
		return visitanteId;
	}

	public Integer getGolsMandante() {
		return golsMandante;
	}

	public Integer getGolsVisitante() {
		return golsVisitante;
	}

	public StatusPartida getStatus() {
		return status;
	}

	public boolean isPlacarPelaSumula() {
		return placarPelaSumula;
	}

	public List<EventoDaPartida> getEventos() {
		return Collections.unmodifiableList(eventos);
	}

}
