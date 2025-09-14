package com.bballstats.backend.controller;

import com.bballstats.backend.dto.metrics.GameMetricsDto;
import com.bballstats.backend.dto.metrics.LeaderDto;
import com.bballstats.backend.dto.metrics.PlayerCompareDto;
import com.bballstats.backend.dto.metrics.PlayerMetricsDto;
import com.bballstats.backend.dto.metrics.TeamMetricsDto;
import com.bballstats.backend.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bballstats.backend.dto.metrics.TeamLeaderDto;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metrics") // koristi /api prefix kao i ostali kontroleri
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    private static final Set<String> ALLOWED_METRICS = Set.of(
            "pts", "apg", "rpg", "efg", "ts", "tp3p"
    );

    @GetMapping("/player/{id}")
    public PlayerMetricsDto playerMetrics(@PathVariable Long id,
                                          @RequestParam(required = false) String season) {
        return metricsService.getPlayerMetrics(id, season);
    }

    @GetMapping("/team/{id}")
    public TeamMetricsDto teamMetrics(@PathVariable Long id,
                                      @RequestParam(required = false) String season) {
        return metricsService.getTeamMetrics(id, season);
    }

    @GetMapping("/game/{id}")
    public GameMetricsDto gameMetrics(@PathVariable Long id) {
        return metricsService.getGameMetrics(id);
    }

    /**
     * Leaderboard endpoint za 6 metrika (PTS/APG/RPG/eFG%/TS%/3PT%).
     * Primer:
     *   GET /api/metrics/leaders?season=2024/25&metric=apg&n=5&minGames=3&minMpg=10
     */
    @GetMapping("/leaders")
    public ResponseEntity<List<LeaderDto>> leaders(@RequestParam String season,
                                                   @RequestParam(defaultValue = "pts") String metric,
                                                   @RequestParam(name = "n", defaultValue = "5") int n,
                                                   @RequestParam(required = false) Integer minGames,
                                                   @RequestParam(name = "minMpg", required = false) Integer minMinutesPerGame) {
        // Normalizacija i validacija metric parametra (hvata tipfelere odmah):
        String m = metric.toLowerCase(Locale.ROOT).trim();
        if (!ALLOWED_METRICS.contains(m)) {
            return ResponseEntity.badRequest().build();
        }

        // Zaštita od loših vrednosti n:
        int top = (n <= 0) ? 5 : n;

        // Delegacija na servis (postojeći potpis zadržan)
        List<LeaderDto> result = metricsService.getLeaders(season, m, top, minGames, minMinutesPerGame);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/compare/players")
    public List<PlayerCompareDto> compare(@RequestParam String ids,
                                          @RequestParam(required = false) String season) {
        List<Long> list = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return metricsService.comparePlayers(list, season);
    }

    @GetMapping("/team-leaders")
    public ResponseEntity<List<TeamLeaderDto>> teamLeaders(@RequestParam String season,
                                                           @RequestParam(defaultValue = "pts") String metric,
                                                           @RequestParam(name = "n", defaultValue = "5") int n,
                                                           @RequestParam(required = false) Integer minGames) {
        Set<String> allowed = Set.of("pts","apg","rpg","efg","ts","tp3p");
        String m = metric.toLowerCase(Locale.ROOT).trim();
        if (!allowed.contains(m)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(metricsService.getTeamLeaders(season, m, n <= 0 ? 5 : n, minGames));
    }

}
