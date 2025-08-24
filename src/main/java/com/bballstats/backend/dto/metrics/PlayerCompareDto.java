package com.bballstats.backend.dto.metrics;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerCompareDto {
    private Long playerId;
    private String playerName;
    private Long teamId;
    private String teamName;

    private double ts;
    private double efg;
    private double usg;
    private double ptsPerGame;
    private double minPerGame;
}
