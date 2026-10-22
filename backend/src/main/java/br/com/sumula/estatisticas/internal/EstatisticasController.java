package br.com.sumula.estatisticas.internal;

import java.util.List;
import java.util.UUID;

import br.com.sumula.campeonatos.Campeonatos;
import br.com.sumula.partidas.Partidas;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campeonatos/{campeonatoId}")
class EstatisticasController {

	private final Campeonatos campeonatos;

	private final Partidas partidas;

	EstatisticasController(Campeonatos campeonatos, Partidas partidas) {
		this.campeonatos = campeonatos;
		this.partidas = partidas;
	}

	@GetMapping("/classificacao")
	List<Classificacao.Linha> classificacao(@PathVariable UUID campeonatoId) {
		return Classificacao.calcular(campeonatos.times(campeonatoId), partidas.encerradas(campeonatoId));
	}

	@GetMapping("/artilharia")
	List<Artilharia.Linha> artilharia(@PathVariable UUID campeonatoId) {
		return Artilharia.calcular(campeonatos.times(campeonatoId), campeonatos.jogadores(campeonatoId),
				partidas.comSumula(campeonatoId));
	}

	@GetMapping("/suspensoes")
	List<Suspensoes.Suspensao> suspensoes(@PathVariable UUID campeonatoId) {
		return Suspensoes.calcular(campeonatos.times(campeonatoId), campeonatos.jogadores(campeonatoId),
				partidas.comSumula(campeonatoId));
	}

}
