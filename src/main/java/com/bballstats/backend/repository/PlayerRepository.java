package com.bballstats.backend.repository;

import com.bballstats.backend.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    // LISTE – uvek učitaj i team
    @EntityGraph(attributePaths = "team")
    Page<Player> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "team")
    Page<Player> findByTeam_Id(Long teamId, Pageable pageable);

    @EntityGraph(attributePaths = "team")
    Page<Player> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    // SINGLE – uvek učitaj i team
    @EntityGraph(attributePaths = "team")
    Optional<Player> findById(Long id);

    boolean existsByTeam_IdAndJerseyNumber(Long teamId, Integer jerseyNumber);
}
