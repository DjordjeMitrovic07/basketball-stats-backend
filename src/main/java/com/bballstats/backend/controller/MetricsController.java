package com.bballstats.backend.controller;

import com.bballstats.backend.dto.metrics.*;
import com.bballstats.backend.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/player/{id}")
    public PlayerMetricsDto playerMetrics(@PathVariable Long id,
                                          @RequestParam(required = false) String season){
        return metricsService.getPlayerMetrics(id, season);
    }

    @GetMapping("/team/{id}")
    public TeamMetricsDto teamMetrics(@PathVariable Long id,
                                      @RequestParam(required = false) String season){
        return metricsService.getTeamMetrics(id, season);
    }

    @GetMapping("/game/{id}")
    public GameMetricsDto gameMetrics(@PathVariable Long id){
        return metricsService.getGameMetrics(id);
    }

    @GetMapping("/leaders")
    public List<LeaderDto> leaders(@RequestParam String season,
                                   @RequestParam(defaultValue = "pts") String metric,
                                   @RequestParam(defaultValue = "10") int n,
                                   @RequestParam(required = false) Integer minGames,
                                   @RequestParam(required = false) Integer minMpg){
        return metricsService.getLeaders(season, metric, n, minGames, minMpg);
    }

    @GetMapping("/compare/players")
    public List<PlayerCompareDto> compare(@RequestParam String ids,
                                          @RequestParam(required = false) String season){
        List<Long> list = Arrays.stream(ids.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::valueOf).collect(Collectors.toList());
        return metricsService.comparePlayers(list, season);
    }
}
