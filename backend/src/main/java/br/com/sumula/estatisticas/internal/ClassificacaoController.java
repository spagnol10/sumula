package br.com.sumula.estatisticas.internal;

import java.util.List;
import java.util.UUID;

import br.com.sumula.campeonatos.Campeonatos;
import br.com.sumula.partidas.Partidas;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ClassificacaoController {

	private final Campeonatos campeonatos;

	private final Partidas partidas;

	ClassificacaoController(Campeonatos campeonatos, Partidas partidas) {
		this.campeonatos = campeonatos;
		this.partidas = partidas;
	}

	@GetMapping("/api/v1/campeonatos/{campeonatoId}/classificacao")
	List<Classificacao.Linha> classificacao(@PathVariable UUID campeonatoId) {
		return Classificacao.calcular(campeonatos.times(campeonatoId), partidas.encerradas(campeonatoId));
	}

}
