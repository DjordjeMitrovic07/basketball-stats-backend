package com.bballstats.backend.dto.metrics;

import lombok.Data;

@Data
public class PlayerSeasonMetricsDto {
    private Long playerId;
    private String playerName;

    private Long teamId;
    private String teamName;
    private String teamAbbr;   // može biti null

    private int games;

    private double ppg, rpg, apg, spg, bpg, tovpg, mpg;
    private double fgPct, tp3Pct, ftPct, tsPct, efgPct; // 0–1 (frontend formatira u %)
}
