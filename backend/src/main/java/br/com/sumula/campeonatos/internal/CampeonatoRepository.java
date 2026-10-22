package br.com.sumula.campeonatos.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampeonatoRepository extends JpaRepository<Campeonato, UUID> {

	/** O agregado é pequeno (até 40 times), então sempre carrega os times junto. */
	@Override
	@EntityGraph(attributePaths = "times")
	Optional<Campeonato> findById(UUID id);

	@EntityGraph(attributePaths = "times")
	Optional<Campeonato> findBySlug(String slug);

}
