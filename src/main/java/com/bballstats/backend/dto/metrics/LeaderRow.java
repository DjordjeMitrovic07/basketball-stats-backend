package com.bballstats.backend.dto.metrics;

public record LeaderRow(
        Long playerId,
        String name,
        String teamAbbr,
        double value
) {}
