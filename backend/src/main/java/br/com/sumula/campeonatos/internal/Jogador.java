package br.com.sumula.campeonatos.internal;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jogadores")
public class Jogador {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "time_id")
	private Time time;

	private String nome;

	private Integer numero;

	private Instant inscritoEm;

	protected Jogador() {
	}

	Jogador(Time time, String nome, Integer numero) {
		this.id = UUID.randomUUID();
		this.time = time;
		this.nome = nome;
		this.numero = numero;
		this.inscritoEm = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public Integer getNumero() {
		return numero;
	}

}
