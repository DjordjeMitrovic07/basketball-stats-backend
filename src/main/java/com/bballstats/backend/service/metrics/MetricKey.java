package com.bballstats.backend.service.metrics;

public enum MetricKey {
    PTS, APG, RPG, EFG, TS, TP3P;

    public static MetricKey from(String s) {
        return MetricKey.valueOf(s.toUpperCase());
    }

    public boolean isPercentage() {
        return this == EFG || this == TS || this == TP3P;
    }
}
