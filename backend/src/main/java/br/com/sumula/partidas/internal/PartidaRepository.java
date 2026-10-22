package br.com.sumula.partidas.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaRepository extends JpaRepository<Partida, UUID> {

	List<Partida> findByCampeonatoIdOrderByRodadaAsc(UUID campeonatoId);

	List<Partida> findByCampeonatoIdAndStatus(UUID campeonatoId, StatusPartida status);

	boolean existsByCampeonatoId(UUID campeonatoId);

}
