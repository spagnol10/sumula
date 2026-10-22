package br.com.sumula.partidas.internal;

import java.time.Instant;
import java.util.UUID;

import br.com.sumula.partidas.TipoDeEvento;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Uma linha da súmula: gol ou cartão. */
@Entity
@Table(name = "eventos_partida")
public class EventoDaPartida {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "partida_id")
	private Partida partida;

	@Enumerated(EnumType.STRING)
	private TipoDeEvento tipo;

	private UUID timeId;

	private UUID jogadorId;

	private Integer minuto;

	private Instant registradoEm;

	protected EventoDaPartida() {
	}

	EventoDaPartida(Partida partida, TipoDeEvento tipo, UUID timeId, UUID jogadorId, Integer minuto) {
		this.id = UUID.randomUUID();
		this.partida = partida;
		this.tipo = tipo;
		this.timeId = timeId;
		this.jogadorId = jogadorId;
		this.minuto = minuto;
		this.registradoEm = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public TipoDeEvento getTipo() {
		return tipo;
	}

	public UUID getTimeId() {
		return timeId;
	}

	public UUID getJogadorId() {
		return jogadorId;
	}

	public Integer getMinuto() {
		return minuto;
	}

}
