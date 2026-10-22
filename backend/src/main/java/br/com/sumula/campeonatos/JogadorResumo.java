package br.com.sumula.campeonatos;

import java.util.UUID;

public record JogadorResumo(UUID id, String nome, Integer numero, UUID timeId) {
}
