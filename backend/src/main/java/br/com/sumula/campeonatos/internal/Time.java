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
@Table(name = "times")
public class Time {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "campeonato_id")
	private Campeonato campeonato;

	private String nome;

	private Instant inscritoEm;

	protected Time() {
	}

	Time(Campeonato campeonato, String nome) {
		this.id = UUID.randomUUID();
		this.campeonato = campeonato;
		this.nome = nome;
		this.inscritoEm = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

}
