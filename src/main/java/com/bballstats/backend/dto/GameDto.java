package com.bballstats.backend.dto;

import com.bballstats.backend.entity.Game;

import java.time.LocalDateTime;

public class GameDto {
    private Long id;
    private LocalDateTime dateTime;
    private TeamDto homeTeam;
    private TeamDto awayTeam;
    private Integer homeScore;
    private Integer awayScore;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public TeamDto getHomeTeam() { return homeTeam; }
    public void setHomeTeam(TeamDto homeTeam) { this.homeTeam = homeTeam; }
    public TeamDto getAwayTeam() { return awayTeam; }
    public void setAwayTeam(TeamDto awayTeam) { this.awayTeam = awayTeam; }
    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }

    public static GameDto from(Game g) {
        GameDto dto = new GameDto();
        dto.setId(g.getId());
        dto.setDateTime(g.getDateTime());
        dto.setHomeTeam(TeamDto.from(g.getHomeTeam()));
        dto.setAwayTeam(TeamDto.from(g.getAwayTeam()));
        dto.setHomeScore(g.getHomeScore());
        dto.setAwayScore(g.getAwayScore());
        return dto;
    }
}
