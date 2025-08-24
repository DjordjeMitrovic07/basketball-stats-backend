package com.bballstats.backend.dto.metrics;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GameMetricsDto {
    private Long gameId;
    private String date;
    private Long homeTeamId; private String homeTeamName;
    private Long awayTeamId; private String awayTeamName;
    private int homeScore; private int awayScore;

    // Team-level ratings for this game
    private double homePoss; private double awayPoss;
    private double pace;
    private double homeORtg; private double homeDRtg;
    private double awayORtg; private double awayDRtg;
}
