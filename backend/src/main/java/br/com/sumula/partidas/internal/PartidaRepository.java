package br.com.sumula.partidas.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaRepository extends JpaRepository<Partida, UUID> {

	@Override
	@EntityGraph(attributePaths = "eventos")
	Optional<Partida> findById(UUID id);

	@EntityGraph(attributePaths = "eventos")
	List<Partida> findByCampeonatoIdOrderByRodadaAsc(UUID campeonatoId);

	List<Partida> findByCampeonatoIdAndStatus(UUID campeonatoId, StatusPartida status);

	boolean existsByCampeonatoId(UUID campeonatoId);

}
