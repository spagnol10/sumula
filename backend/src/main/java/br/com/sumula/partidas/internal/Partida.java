package br.com.sumula.partidas.internal;

import java.util.UUID;

import br.com.sumula.compartilhado.RegraDeNegocioException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Times e campeonato são referenciados só por id: pertencem a outro módulo (ADR 0002).
 */
@Entity
@Table(name = "partidas")
public class Partida {

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

	/** Registra o placar final; pode ser chamado de novo para corrigir um placar lançado errado. */
	void registrarResultado(int golsMandante, int golsVisitante) {
		if (golsMandante < 0 || golsVisitante < 0) {
			throw new RegraDeNegocioException("O placar não pode ser negativo");
		}
		this.golsMandante = golsMandante;
		this.golsVisitante = golsVisitante;
		this.status = StatusPartida.ENCERRADA;
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

}
