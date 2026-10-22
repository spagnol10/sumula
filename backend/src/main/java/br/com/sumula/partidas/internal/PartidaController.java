package br.com.sumula.partidas.internal;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.sumula.campeonatos.Campeonatos;
import br.com.sumula.campeonatos.TimeResumo;
import br.com.sumula.compartilhado.RecursoNaoEncontradoException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class PartidaController {

	private final PartidaRepository partidas;

	private final Campeonatos campeonatos;

	PartidaController(PartidaRepository partidas, Campeonatos campeonatos) {
		this.partidas = partidas;
		this.campeonatos = campeonatos;
	}

	record Placar(@NotNull @Min(0) @Max(99) Integer golsMandante, @NotNull @Min(0) @Max(99) Integer golsVisitante) {
	}

	record TimeNaPartida(UUID id, String nome) {
	}

	record PartidaResponse(UUID id, int rodada, TimeNaPartida mandante, TimeNaPartida visitante,
			Integer golsMandante, Integer golsVisitante, StatusPartida status) {

		static PartidaResponse de(Partida p, Map<UUID, String> nomes) {
			return new PartidaResponse(p.getId(), p.getRodada(),
					new TimeNaPartida(p.getMandanteId(), nomes.get(p.getMandanteId())),
					new TimeNaPartida(p.getVisitanteId(), nomes.get(p.getVisitanteId())), p.getGolsMandante(),
					p.getGolsVisitante(), p.getStatus());
		}

	}

	@GetMapping("/campeonatos/{campeonatoId}/partidas")
	List<PartidaResponse> listar(@PathVariable UUID campeonatoId) {
		Map<UUID, String> nomes = nomesDosTimes(campeonatoId);
		return partidas.findByCampeonatoIdOrderByRodadaAsc(campeonatoId)
			.stream()
			.map(p -> PartidaResponse.de(p, nomes))
			.toList();
	}

	@PutMapping("/partidas/{id}/resultado")
	@Transactional
	PartidaResponse registrarResultado(@PathVariable UUID id,
			@RequestHeader(name = "X-Chave-Organizador", required = false) String chave,
			@Valid @RequestBody Placar placar) {
		Partida partida = partidas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Partida", id));
		campeonatos.exigirOrganizador(partida.getCampeonatoId(), chave);
		partida.registrarResultado(placar.golsMandante(), placar.golsVisitante());
		partidas.flush();
		return PartidaResponse.de(partida, nomesDosTimes(partida.getCampeonatoId()));
	}

	private Map<UUID, String> nomesDosTimes(UUID campeonatoId) {
		return campeonatos.times(campeonatoId)
			.stream()
			.collect(Collectors.toMap(TimeResumo::id, TimeResumo::nome));
	}

}
