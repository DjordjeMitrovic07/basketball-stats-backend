package com.bballstats.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BoxScoreCreateDto {

    @NotNull
    private Long playerId;

    // opciono: tim koji frontend šalje radi trasiranja (BE ga trenutno ne koristi)
    private Long teamId;

    @Min(0) public Integer pts;
    @Min(0) public Integer fgm;  @Min(0) public Integer fga;
    @Min(0) public Integer tp3m; @Min(0) public Integer tp3a;
    @Min(0) public Integer ftm;  @Min(0) public Integer fta;
    @Min(0) public Integer reb;  @Min(0) public Integer ast;
    @Min(0) public Integer stl;  @Min(0) public Integer blk;
    @Min(0) public Integer tov;  @Min(0) public Integer min;

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}
