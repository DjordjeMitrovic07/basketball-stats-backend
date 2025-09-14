package com.bballstats.backend.service.metrics;

import com.bballstats.backend.dto.metrics.PlayerSeasonMetricsDto;
import com.bballstats.backend.dto.metrics.LeaderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Agregacije i pretrage metrika nad sezonskim/statističkim podacima.
 *
 * Podržane "leaderboard" metrike (parametar {@code metric}):
 *   - pts  = points per game
 *   - apg  = assists per game
 *   - rpg  = rebounds per game
 *   - efg  = effective FG% = (FGM + 0.5 * 3PM) / FGA
 *   - ts   = true shooting % = PTS / (2 * (FGA + 0.44 * FTA))
 *   - tp3p = 3PT% = 3PM / 3PA  (preporuka: primeniti minimalni sample-size prag po meču)
 */
public interface MetricsAggService {

    /**
     * Paginirana lista igrača sa sezonskim metrikama i filterima.
     */
    Page<PlayerSeasonMetricsDto> playersMetrics(
            String season,
            Long teamId,
            String q,
            Integer minGames,
            String sort,
            Pageable pageable
    );

    /**
     * Vraća Top-N listu igrača po traženoj metrici.
     *
     * @param season   obavezno, npr. "2024/25"
     * @param metric   vidi listu podržanih metrika u JavaDoc-u
     * @param teamId   opcioni filter po timu (može biti null)
     * @param top      koliko redova vratiti (Top-N)
     * @param minGames opcioni prag minimalnog broja utakmica
     * @return lista LeaderDto (playerId, name, teamAbbr, value)
     */
    List<LeaderDto> leaderboard(
            String season,
            String metric,
            Long teamId,
            Integer top,
            Integer minGames
    );
}
