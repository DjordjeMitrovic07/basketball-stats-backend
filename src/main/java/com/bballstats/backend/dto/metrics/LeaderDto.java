package com.bballstats.backend.dto.metrics;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderDto {
    private Long playerId;   // id igrača
    private String name;     // puno ime igrača
    private String teamAbbr; // npr. LAL, BOS, GSW
    private Double value;    // vrednost metrike (ppg, ts%, ...)
}
