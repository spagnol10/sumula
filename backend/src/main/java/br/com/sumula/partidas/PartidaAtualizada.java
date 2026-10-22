package br.com.sumula.partidas;

import java.util.UUID;

/** Algo mudou na partida (placar, súmula ou status). Usado para notificar quem acompanha ao vivo. */
public record PartidaAtualizada(UUID campeonatoId, UUID partidaId) {
}
