package com.bballstats.backend.repository;

import com.bballstats.backend.entity.BoxScore;
import com.bballstats.backend.repository.projections.PlayerSeasonAgg;
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
    boolean existsByPlayer_Id(Long playerId);

    @EntityGraph(attributePaths = {"player","player.team","game","game.homeTeam","game.awayTeam"})
    Optional<BoxScore> findById(Long id);

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

    @Query("""
      select b from BoxScore b
        join b.game g
      where b.player.id = :playerId
        and (:season is null or g.season = :season)
      """)
    List<BoxScore> findByPlayerIdAndSeason(@Param("playerId") Long playerId,
                                           @Param("season") String season);

    @Query("""
      select b from BoxScore b
      where b.game.id = :gameId and b.player.team.id = :teamId
      """)
    List<BoxScore> findByGameIdAndTeamId(@Param("gameId") Long gameId,
                                         @Param("teamId") Long teamId);

    @Query("select b from BoxScore b where b.player.id = :playerId")
    List<BoxScore> findByPlayerId(@Param("playerId") Long playerId);


    /* =====================  METRICS (MVP)  ===================== */

    // import com.bballstats.backend.repository.projections.PlayerSeasonAgg;

    @Query("""
       select
         p.id        as playerId,
         p.firstName as playerFirstName,
         p.lastName  as playerLastName,
         t.id        as teamId,
         t.name      as teamName,
         t.name      as teamAbbr,

         count(distinct g.id) as games,

         sum(b.pts)  as pts,
         sum(b.reb)  as reb,
         sum(b.ast)  as ast,
         sum(b.stl)  as stl,
         sum(b.blk)  as blk,
         sum(b.tov)  as tov,
         sum(b.min)  as min,

         sum(b.fgm)  as fgm,
         sum(b.fga)  as fga,
         sum(b.tp3m) as tp3m,
         sum(b.tp3a) as tp3a,
         sum(b.ftm)  as ftm,
         sum(b.fta)  as fta
       from BoxScore b
         join b.player p
         left join p.team t
         join b.game g
       where g.season = :season
         and (:teamId is null or t.id = :teamId)
         and ( :q is null
               or lower(p.firstName) like lower(concat('%', :q, '%'))
               or lower(p.lastName)  like lower(concat('%', :q, '%')) )
       group by p.id, p.firstName, p.lastName, t.id, t.name
       """)
    Page<PlayerSeasonAgg> aggregatePlayersBySeason(
            @Param("season") String season,
            @Param("teamId") Long teamId,
            @Param("q") String q,
            Pageable pageable
    );

}
