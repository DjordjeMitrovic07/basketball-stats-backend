package com.bballstats.backend.controller;

import com.bballstats.backend.dto.metrics.LeaderDto;
import com.bballstats.backend.dto.metrics.PlayerSeasonMetricsDto;
import com.bballstats.backend.service.PlayersMetricsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsQueryController {

    private final PlayersMetricsService service;

    public MetricsQueryController(PlayersMetricsService service) {
        this.service = service;
    }

    @GetMapping("/players")
    public Page<PlayerSeasonMetricsDto> players(
            @RequestParam String season,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minGames,
            @RequestParam(defaultValue = "ppg,desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.playersMetrics(season, teamId, q, minGames, sort, PageRequest.of(page, size));
    }

    @GetMapping("/leaderboard")
    public List<LeaderDto> leaderboard(
            @RequestParam String season,
            @RequestParam(defaultValue = "ppg") String metric,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Integer minGames,
            @RequestParam(defaultValue = "10") int top
    ) {
        return service.leaderboard(season, metric, teamId, minGames, top);
    }
}
