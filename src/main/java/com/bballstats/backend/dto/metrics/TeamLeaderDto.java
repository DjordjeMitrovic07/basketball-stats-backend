package com.bballstats.backend.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamLeaderDto {
    private Long teamId;
    private String name;     // team name
    private String abbr;     // kratka oznaka (prikaz u listi)
    private double value;    // metrika
}
