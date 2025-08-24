package com.bballstats.backend.dto.metrics;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaderDto {
    private Long playerId;
    private String playerName;
    private Long teamId;
    private String teamName;
    private String metric;   // npr. "pts" | "ts" | "efg" | "usg"
    private double value;
    private int games;
    private double minPerGame;
}
