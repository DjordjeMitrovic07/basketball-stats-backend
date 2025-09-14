package com.bballstats.backend.service.metrics;

import com.bballstats.backend.dto.metrics.*;

import java.util.List;

public interface MetricsService {
    PlayerMetricsDto getPlayerMetrics(Long playerId, String season);
    TeamMetricsDto getTeamMetrics(Long teamId, String season);
    GameMetricsDto getGameMetrics(Long gameId);

    List<LeaderDto> getLeaders(String season, String metric, int n, Integer minGames, Integer minMinutesPerGame);
    List<PlayerCompareDto> comparePlayers(List<Long> playerIds, String season);
    List<TeamLeaderDto> getTeamLeaders(String season, String metric, int n, Integer minGames);
}
