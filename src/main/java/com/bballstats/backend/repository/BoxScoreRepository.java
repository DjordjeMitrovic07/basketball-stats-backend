package com.bballstats.backend.repository;

import com.bballstats.backend.entity.BoxScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoxScoreRepository extends JpaRepository<BoxScore, Long> {

    @EntityGraph(attributePaths = {"player","player.team","game","game.homeTeam","game.awayTeam"})
    Page<BoxScore> findByGame_Id(Long gameId, Pageable pageable);

    @EntityGraph(attributePaths = {"player","player.team","game"})
    Page<BoxScore> findByPlayer_Id(Long playerId, Pageable pageable);

    boolean existsByGame_IdAndPlayer_Id(Long gameId, Long playerId);
    boolean existsByPlayer_Id(Long playerId);   // <— koristimo u servisu

    @EntityGraph(attributePaths = {"player","player.team","game","game.homeTeam","game.awayTeam"})
    Optional<BoxScore> findById(Long id);

    // Single-row detaljno učitavanje (player/team + game/home/away)
    @Query("""
           select b from BoxScore b
             join fetch b.player p
             left join fetch p.team
             join fetch b.game g
             left join fetch g.homeTeam
             left join fetch g.awayTeam
           where b.id = :id
           """)
    Optional<BoxScore> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"player","player.team","game","game.homeTeam","game.awayTeam"})
    Page<BoxScore> findAllBy(Pageable pageable);

    // Za metrike (opciono filter po sezoni)
    @Query("""
           select b from BoxScore b
             join b.game g
           where b.player.id = :playerId
             and (:season is null or g.season = :season)
           """)
    List<BoxScore> findByPlayerIdAndSeason(@Param("playerId") Long playerId,
                                           @Param("season") String season);

    // Box-score-ovi tima na konkretnoj utakmici
    @Query("""
           select b from BoxScore b
           where b.game.id = :gameId and b.player.team.id = :teamId
           """)
    List<BoxScore> findByGameIdAndTeamId(@Param("gameId") Long gameId,
                                         @Param("teamId") Long teamId);

    // Ako negde već koristiš “findByPlayerId”, ostavljamo i ovu varijantu
    @Query("select b from BoxScore b where b.player.id = :playerId")
    List<BoxScore> findByPlayerId(@Param("playerId") Long playerId);
}
