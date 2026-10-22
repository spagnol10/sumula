package br.com.sumula.campeonatos.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import br.com.sumula.compartilhado.AcessoNegadoException;
import br.com.sumula.compartilhado.RecursoNaoEncontradoException;
import br.com.sumula.compartilhado.RegraDeNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Raiz do agregado: controla inscrição de times e o início do campeonato. */
@Entity
@Table(name = "campeonatos")
public class Campeonato {

	static final int MINIMO_DE_TIMES = 2;

	static final int MAXIMO_DE_TIMES = 40;

	@Id
	private UUID id;

	private String nome;

	private String slug;

	@Column(name = "ida_e_volta") // a estratégia padrão geraria "idaevolta"
	private boolean idaEVolta;

	@Enumerated(EnumType.STRING)
	private StatusCampeonato status;

	private Instant criadoEm;

	private String chaveOrganizadorHash;

	@OneToMany(mappedBy = "campeonato", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("inscritoEm")
	private List<Time> times = new ArrayList<>();

	@Version
	private Long versao;

	protected Campeonato() {
	}

	Campeonato(String nome, String slug, boolean idaEVolta, String chaveOrganizadorHash) {
		this.id = UUID.randomUUID();
		this.nome = nome.strip();
		this.slug = slug;
		this.idaEVolta = idaEVolta;
		this.status = StatusCampeonato.INSCRICOES_ABERTAS;
		this.criadoEm = Instant.now();
		this.chaveOrganizadorHash = chaveOrganizadorHash;
	}

	public void exigirOrganizador(String chave) {
		if (!ChaveDeOrganizador.confere(chave, chaveOrganizadorHash)) {
			throw new AcessoNegadoException("Chave de organizador ausente ou inválida");
		}
	}

	Time inscrever(String nomeDoTime) {
		if (status != StatusCampeonato.INSCRICOES_ABERTAS) {
			throw new RegraDeNegocioException("As inscrições deste campeonato já foram encerradas");
		}
		if (times.size() >= MAXIMO_DE_TIMES) {
			throw new RegraDeNegocioException("O campeonato atingiu o limite de " + MAXIMO_DE_TIMES + " times");
		}
		String nome = nomeDoTime.strip();
		if (times.stream().anyMatch(t -> t.getNome().equalsIgnoreCase(nome))) {
			throw new RegraDeNegocioException("Já existe um time chamado \"" + nome + "\" neste campeonato");
		}
		Time time = new Time(this, nome);
		times.add(time);
		return time;
	}

	Jogador inscreverJogador(UUID timeId, String nome, Integer numero) {
		return times.stream()
			.filter(t -> t.getId().equals(timeId))
			.findFirst()
			.orElseThrow(() -> new RecursoNaoEncontradoException("Time", timeId))
			.inscreverJogador(nome, numero);
	}

	void iniciar() {
		if (status != StatusCampeonato.INSCRICOES_ABERTAS) {
			throw new RegraDeNegocioException("O campeonato já foi iniciado");
		}
		if (times.size() < MINIMO_DE_TIMES) {
			throw new RegraDeNegocioException("São necessários pelo menos " + MINIMO_DE_TIMES + " times para iniciar");
		}
		status = StatusCampeonato.EM_ANDAMENTO;
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getSlug() {
		return slug;
	}

	public boolean isIdaEVolta() {
		return idaEVolta;
	}

	public StatusCampeonato getStatus() {
		return status;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public List<Time> getTimes() {
		return Collections.unmodifiableList(times);
	}

}
