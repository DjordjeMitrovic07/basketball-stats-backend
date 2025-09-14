package com.bballstats.backend.dto.metrics;

public class LeaderboardRowDto {
    public Long playerId;
    public String playerName;
    public Long teamId;
    public String teamName;
    public double value;

    public LeaderboardRowDto() {}
    public LeaderboardRowDto(Long playerId, String playerName, Long teamId, String teamName, double value) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.value = value;
    }
}
