package br.com.sumula.partidas.internal;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.sumula.campeonatos.Campeonatos;
import br.com.sumula.campeonatos.JogadorResumo;
import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.partidas.TipoDeEvento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class PartidaController {

	private static final String CHAVE = "X-Chave-Organizador";

	private final PartidaRepository partidas;

	private final PartidaService service;

	private final Campeonatos campeonatos;

	PartidaController(PartidaRepository partidas, PartidaService service, Campeonatos campeonatos) {
		this.partidas = partidas;
		this.service = service;
		this.campeonatos = campeonatos;
	}

	record Placar(@NotNull @Min(0) @Max(99) Integer golsMandante, @NotNull @Min(0) @Max(99) Integer golsVisitante) {
	}

	record NovoEvento(@NotNull TipoDeEvento tipo, @NotNull UUID timeId, UUID jogadorId, Integer minuto) {
	}

	record TimeNaPartida(UUID id, String nome) {
	}

	record EventoResponse(UUID id, TipoDeEvento tipo, UUID timeId, UUID jogadorId, String jogador, Integer minuto) {
	}

	record PartidaResponse(UUID id, UUID campeonatoId, int rodada, TimeNaPartida mandante, TimeNaPartida visitante,
			Integer golsMandante, Integer golsVisitante, StatusPartida status, boolean placarPelaSumula,
			List<EventoResponse> eventos) {
	}

	/** Nomes de times e jogadores vêm do módulo de campeonatos, numa consulta por requisição. */
	private final class Nomes {

		private final Map<UUID, String> times;

		private final Map<UUID, String> jogadores;

		Nomes(UUID campeonatoId) {
			times = campeonatos.times(campeonatoId).stream().collect(Collectors.toMap(TimeResumo::id, TimeResumo::nome));
			jogadores = campeonatos.jogadores(campeonatoId)
				.stream()
				.collect(Collectors.toMap(JogadorResumo::id, JogadorResumo::nome));
		}

		PartidaResponse de(Partida p) {
			return new PartidaResponse(p.getId(), p.getCampeonatoId(), p.getRodada(),
					new TimeNaPartida(p.getMandanteId(), times.get(p.getMandanteId())),
					new TimeNaPartida(p.getVisitanteId(), times.get(p.getVisitanteId())), p.getGolsMandante(),
					p.getGolsVisitante(), p.getStatus(), p.isPlacarPelaSumula(),
					p.getEventos()
						.stream()
						.map(e -> new EventoResponse(e.getId(), e.getTipo(), e.getTimeId(), e.getJogadorId(),
								e.getJogadorId() == null ? null : jogadores.get(e.getJogadorId()), e.getMinuto()))
						.toList());
		}

	}

	private PartidaResponse responder(Partida partida) {
		return new Nomes(partida.getCampeonatoId()).de(partida);
	}

	@GetMapping("/campeonatos/{campeonatoId}/partidas")
	List<PartidaResponse> listar(@PathVariable UUID campeonatoId) {
		Nomes nomes = new Nomes(campeonatoId);
		return partidas.findByCampeonatoIdOrderByRodadaAsc(campeonatoId).stream().map(nomes::de).toList();
	}

	@GetMapping("/partidas/{id}")
	PartidaResponse buscar(@PathVariable UUID id) {
		return responder(service.buscar(id));
	}

	@PutMapping("/partidas/{id}/resultado")
	PartidaResponse registrarResultado(@PathVariable UUID id, @RequestHeader(name = CHAVE, required = false) String chave,
			@Valid @RequestBody Placar placar) {
		return responder(service.registrarResultado(id, chave, placar.golsMandante(), placar.golsVisitante()));
	}

	@PostMapping("/partidas/{id}/inicio")
	PartidaResponse iniciar(@PathVariable UUID id, @RequestHeader(name = CHAVE, required = false) String chave) {
		return responder(service.iniciar(id, chave));
	}

	@PostMapping("/partidas/{id}/eventos")
	PartidaResponse registrarEvento(@PathVariable UUID id, @RequestHeader(name = CHAVE, required = false) String chave,
			@Valid @RequestBody NovoEvento evento) {
		return responder(service.registrarEvento(id, chave, evento.tipo(), evento.timeId(), evento.jogadorId(),
				evento.minuto()));
	}

	@DeleteMapping("/partidas/{id}/eventos/{eventoId}")
	PartidaResponse removerEvento(@PathVariable UUID id, @PathVariable UUID eventoId,
			@RequestHeader(name = CHAVE, required = false) String chave) {
		return responder(service.removerEvento(id, chave, eventoId));
	}

	@PostMapping("/partidas/{id}/encerramento")
	PartidaResponse encerrar(@PathVariable UUID id, @RequestHeader(name = CHAVE, required = false) String chave) {
		return responder(service.encerrar(id, chave));
	}

}
