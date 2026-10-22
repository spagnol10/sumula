package br.com.sumula.campeonatos.internal;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import br.com.sumula.compartilhado.RegraDeNegocioException;
import jakarta.persistence.CascadeType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

	// Set (e não List) para o Hibernate poder buscar times e jogadores num único JOIN
	@OneToMany(mappedBy = "time", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("inscritoEm")
	private Set<Jogador> jogadores = new LinkedHashSet<>();

	protected Time() {
	}

	Time(Campeonato campeonato, String nome) {
		this.id = UUID.randomUUID();
		this.campeonato = campeonato;
		this.nome = nome;
		this.inscritoEm = Instant.now();
	}

	static final int MAXIMO_DE_JOGADORES = 30;

	Jogador inscreverJogador(String nomeDoJogador, Integer numero) {
		if (jogadores.size() >= MAXIMO_DE_JOGADORES) {
			throw new RegraDeNegocioException("O time atingiu o limite de " + MAXIMO_DE_JOGADORES + " jogadores");
		}
		if (numero != null && jogadores.stream().anyMatch(j -> Objects.equals(j.getNumero(), numero))) {
			throw new RegraDeNegocioException("O número " + numero + " já é usado por outro jogador do " + nome);
		}
		Jogador jogador = new Jogador(this, nomeDoJogador.strip(), numero);
		jogadores.add(jogador);
		return jogador;
	}

	public Set<Jogador> getJogadores() {
		return Collections.unmodifiableSet(jogadores);
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

}
