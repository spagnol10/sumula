package br.com.sumula.campeonatos.internal;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campeonatos")
class CampeonatoController {

	static final String CHAVE = "X-Chave-Organizador";

	private final CampeonatoService service;

	CampeonatoController(CampeonatoService service) {
		this.service = service;
	}

	/** {@code idaEVolta} é opcional; ausente significa turno único. */
	record NovoCampeonato(@NotBlank @Size(max = 120) String nome, Boolean idaEVolta) {
	}

	record NovoTime(@NotBlank @Size(max = 80) String nome) {
	}

	record TimeResponse(UUID id, String nome) {

		static TimeResponse de(Time time) {
			return new TimeResponse(time.getId(), time.getNome());
		}

	}

	record CampeonatoResponse(UUID id, String nome, String slug, boolean idaEVolta, StatusCampeonato status,
			Instant criadoEm, List<TimeResponse> times) {

		static CampeonatoResponse de(Campeonato c) {
			return new CampeonatoResponse(c.getId(), c.getNome(), c.getSlug(), c.isIdaEVolta(), c.getStatus(),
					c.getCriadoEm(), c.getTimes().stream().map(TimeResponse::de).toList());
		}

	}

	/** A chave só aparece nesta resposta; depois disso apenas o hash fica guardado. */
	record CampeonatoCriado(CampeonatoResponse campeonato, String chaveOrganizador) {
	}

	@PostMapping
	ResponseEntity<CampeonatoCriado> criar(@Valid @RequestBody NovoCampeonato req) {
		CampeonatoService.Criado criado = service.criar(req.nome(), Boolean.TRUE.equals(req.idaEVolta()));
		return ResponseEntity.created(URI.create("/api/v1/campeonatos/" + criado.campeonato().getId()))
			.body(new CampeonatoCriado(CampeonatoResponse.de(criado.campeonato()), criado.chaveOrganizador()));
	}

	@GetMapping("/{id}")
	CampeonatoResponse buscar(@PathVariable UUID id) {
		return CampeonatoResponse.de(service.buscar(id));
	}

	@GetMapping("/slug/{slug}")
	CampeonatoResponse buscarPorSlug(@PathVariable String slug) {
		return CampeonatoResponse.de(service.buscarPorSlug(slug));
	}

	/** Permite ao frontend validar uma chave antes de guardá-la (ex.: link de acesso). */
	@PostMapping("/{id}/chave/verificacao")
	ResponseEntity<Void> verificarChave(@PathVariable UUID id, @RequestHeader(name = CHAVE, required = false) String chave) {
		service.verificarChave(id, chave);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/times")
	ResponseEntity<TimeResponse> inscreverTime(@PathVariable UUID id,
			@RequestHeader(name = CHAVE, required = false) String chave, @Valid @RequestBody NovoTime req) {
		Time time = service.inscreverTime(id, chave, req.nome());
		return ResponseEntity.created(URI.create("/api/v1/campeonatos/" + id)).body(TimeResponse.de(time));
	}

	@PostMapping("/{id}/iniciar")
	CampeonatoResponse iniciar(@PathVariable UUID id, @RequestHeader(name = CHAVE, required = false) String chave) {
		return CampeonatoResponse.de(service.iniciar(id, chave));
	}

}
