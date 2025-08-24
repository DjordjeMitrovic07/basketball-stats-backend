package com.bballstats.backend.dto.metrics;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamMetricsDto {
    private Long teamId;
    private String teamName;
    private String season;
    private Integer games;

    // Totals (team)
    private int teamPoints;
    private int oppPoints;
    private double teamPossessions;
    private double oppPossessions;
    private int teamMinutes; // suma MP svih igrača

    // Derived (season-level)
    private double pace;  // poseda po 40/48 (vidi napomenu u servis impl)
    private double ortg;  // poeni na 100 poseda
    private double drtg;  // primljeni poeni na 100 poseda
    private double efg;   // timski
    private double ts;    // timski
}
