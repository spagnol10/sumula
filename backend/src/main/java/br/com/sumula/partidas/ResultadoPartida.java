package br.com.sumula.partidas;

import java.util.UUID;

public record ResultadoPartida(UUID mandanteId, UUID visitanteId, int golsMandante, int golsVisitante) {
}
