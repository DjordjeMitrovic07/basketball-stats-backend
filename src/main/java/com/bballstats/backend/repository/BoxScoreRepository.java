package com.bballstats.backend.repository;

import com.bballstats.backend.entity.BoxScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoxScoreRepository extends JpaRepository<BoxScore, Long> {

    @EntityGraph(attributePaths = {"player", "player.team", "game", "game.homeTeam", "game.awayTeam"})
    Page<BoxScore> findByGame_Id(Long gameId, Pageable pageable);

    @EntityGraph(attributePaths = {"player", "player.team", "game"})
    Page<BoxScore> findByPlayer_Id(Long playerId, Pageable pageable);

    boolean existsByGame_IdAndPlayer_Id(Long gameId, Long playerId);

    @EntityGraph(attributePaths = {"player","game"})
    Optional<BoxScore> findById(Long id);
}
