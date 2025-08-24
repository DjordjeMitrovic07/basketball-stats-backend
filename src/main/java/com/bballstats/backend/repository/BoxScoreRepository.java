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

    @EntityGraph(attributePaths = {"player", "player.team", "game", "game.homeTeam", "game.awayTeam"})
    Page<BoxScore> findByGame_Id(Long gameId, Pageable pageable);

    @EntityGraph(attributePaths = {"player", "player.team", "game"})
    Page<BoxScore> findByPlayer_Id(Long playerId, Pageable pageable);

    boolean existsByGame_IdAndPlayer_Id(Long gameId, Long playerId);

    // Ostavljamo i ovu varijantu zbog drugih poziva
    @EntityGraph(attributePaths = {"player", "player.team", "game", "game.homeTeam", "game.awayTeam"})
    Optional<BoxScore> findById(Long id);

    // 🔴 KLJUČNO: Single-row reload sa eksplicitnim JOIN FETCH da sigurno imamo player.firstName/lastName
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

    // ====== DODATO ZA METRIKE (bez lomljenja postojećeg) ======

    // Svi box-score-ovi igrača uz opcioni filter po sezoni (sezona je na Game entitetu)
    @Query("""
           select b from BoxScore b
             join b.game g
           where b.player.id = :playerId
             and (:season is null or g.season = :season)
           """)
    List<BoxScore> findByPlayerIdAndSeason(@Param("playerId") Long playerId,
                                           @Param("season") String season);

    // Box-score-ovi jednog tima na konkretnoj utakmici (tim ide preko player.team.id)
    @Query("""
           select b from BoxScore b
           where b.game.id = :gameId and b.player.team.id = :teamId
           """)
    List<BoxScore> findByGameIdAndTeamId(@Param("gameId") Long gameId,
                                         @Param("teamId") Long teamId);

    // (Opciono korisno) — svi box-score-ovi igrača bez filtera po sezoni
    List<BoxScore> findByPlayerId(Long playerId);
}
