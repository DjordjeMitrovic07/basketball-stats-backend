package com.bballstats.backend.repository;

import com.bballstats.backend.entity.BoxScore;
import com.bballstats.backend.repository.projections.PlayerTotals;
import com.bballstats.backend.repository.projections.TeamTotals;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetricsRepository extends Repository<BoxScore, Long> {

    @Query("""
        select
            bs.player.id                                                 as playerId,
            concat(bs.player.firstName, concat(' ', bs.player.lastName)) as playerName,
            bs.player.team.name                                          as teamAbbr,
            sum(bs.pts)                                                  as pts,
            sum(bs.fga)                                                  as fga,
            sum(bs.fgm)                                                  as fgm,
            sum(bs.tp3a)                                                 as tp3a,
            sum(bs.tp3m)                                                 as tp3m,
            sum(bs.fta)                                                  as fta,
            sum(bs.ast)                                                  as ast,
            sum(bs.reb)                                                  as reb,
            count(bs.id)                                                 as games,
            avg(bs.min)                                                  as avgMpg
        from BoxScore bs
        where bs.game.season = :season
        group by
            bs.player.id, bs.player.firstName, bs.player.lastName, bs.player.team.name
        having (:minGames is null or count(bs.id) >= :minGames)
           and (:minMpg   is null or avg(bs.min)  >= :minMpg)
        """)
    List<PlayerTotals> fetchTotals(@Param("season") String season,
                                   @Param("minGames") Integer minGames,
                                   @Param("minMpg") Integer minMpg);

    @Query("""
        select
            bs.player.team.id    as teamId,
            bs.player.team.name  as teamName,
            sum(bs.pts)          as pts,
            sum(bs.fga)          as fga,
            sum(bs.fgm)          as fgm,
            sum(bs.tp3a)         as tp3a,
            sum(bs.tp3m)         as tp3m,
            sum(bs.fta)          as fta,
            sum(bs.ast)          as ast,
            sum(bs.reb)          as reb,
            count(distinct bs.game.id) as games
        from BoxScore bs
        where bs.game.season = :season
        group by bs.player.team.id, bs.player.team.name
        having (:minGames is null or count(distinct bs.game.id) >= :minGames)
        """)
    List<TeamTotals> fetchTeamTotals(@Param("season") String season,
                                     @Param("minGames") Integer minGames);
}
