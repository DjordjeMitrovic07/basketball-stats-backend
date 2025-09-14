package com.bballstats.backend.service;

import com.bballstats.backend.dto.metrics.LeaderDto;
import com.bballstats.backend.dto.metrics.PlayerSeasonMetricsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlayersMetricsService {
    Page<PlayerSeasonMetricsDto> playersMetrics(
            String season, Long teamId, String q, Integer minGames, String sort, Pageable pageable
    );

    List<LeaderDto> leaderboard(
            String season, String metric, Long teamId, Integer minGames, int topN
    );
}
