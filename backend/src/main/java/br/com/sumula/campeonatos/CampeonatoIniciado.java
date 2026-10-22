package br.com.sumula.campeonatos;

import java.util.List;
import java.util.UUID;

/** Publicado quando as inscrições fecham e a tabela pode ser gerada. */
public record CampeonatoIniciado(UUID campeonatoId, List<UUID> timeIds, boolean idaEVolta) {
}
