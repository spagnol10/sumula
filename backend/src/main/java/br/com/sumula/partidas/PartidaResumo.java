package br.com.sumula.partidas;

import java.util.List;
import java.util.UUID;

public record PartidaResumo(UUID id, int rodada, UUID mandanteId, UUID visitanteId, boolean encerrada,
		List<Evento> eventos) {

	public record Evento(TipoDeEvento tipo, UUID timeId, UUID jogadorId, Integer minuto) {
	}

}
