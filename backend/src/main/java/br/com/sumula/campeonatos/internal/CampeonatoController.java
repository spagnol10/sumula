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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campeonatos")
class CampeonatoController {

	private final CampeonatoService service;

	CampeonatoController(CampeonatoService service) {
		this.service = service;
	}

	record NovoCampeonato(@NotBlank @Size(max = 120) String nome, boolean idaEVolta) {
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

	@PostMapping
	ResponseEntity<CampeonatoResponse> criar(@Valid @RequestBody NovoCampeonato req) {
		Campeonato campeonato = service.criar(req.nome(), req.idaEVolta());
		return ResponseEntity.created(URI.create("/api/v1/campeonatos/" + campeonato.getId()))
			.body(CampeonatoResponse.de(campeonato));
	}

	@GetMapping("/{id}")
	CampeonatoResponse buscar(@PathVariable UUID id) {
		return CampeonatoResponse.de(service.buscar(id));
	}

	@PostMapping("/{id}/times")
	ResponseEntity<TimeResponse> inscreverTime(@PathVariable UUID id, @Valid @RequestBody NovoTime req) {
		Time time = service.inscreverTime(id, req.nome());
		return ResponseEntity.created(URI.create("/api/v1/campeonatos/" + id)).body(TimeResponse.de(time));
	}

	@PostMapping("/{id}/iniciar")
	CampeonatoResponse iniciar(@PathVariable UUID id) {
		return CampeonatoResponse.de(service.iniciar(id));
	}

}
