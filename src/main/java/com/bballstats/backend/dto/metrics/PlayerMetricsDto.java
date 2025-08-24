package com.bballstats.backend.dto.metrics;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerMetricsDto {
    private Long playerId;
    private String playerName;
    private Long teamId;
    private String teamName;
    private Integer games;

    // Totals
    private int minutes;
    private int pts;
    private int fgm; private int fga;
    private int tpm; private int tpa;
    private int ftm; private int fta;
    private int tov;

    // Derived (season-level)
    private double efg;   // %
    private double ts;    // %
    private double usg;   // %

    // Per-game (helpful for UI)
    private double ptsPerGame;
    private double minPerGame;
}
