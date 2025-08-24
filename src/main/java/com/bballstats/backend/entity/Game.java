package com.bballstats.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "dateTime is required")
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    // EAGER umesto LAZY -> da bi name/city bili dostupni odmah u DTO-u
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "home_team_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_game_home_team"))
    private Team homeTeam;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "away_team_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_game_away_team"))
    private Team awayTeam;

    @Min(0) @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "season", length = 16) // npr. "2024/25" ili "2024"
    private String season;

    @Min(0) @Column(name = "away_score")
    private Integer awayScore;

    /** Validacija: timovi moraju biti različiti */
    @AssertTrue(message = "homeTeam and awayTeam must be different")
    public boolean isTeamsDifferent() {
        if (homeTeam == null || awayTeam == null) return true; // @NotNull hvata realne null-e
        if (homeTeam.getId() == null || awayTeam.getId() == null) return true;
        return !homeTeam.getId().equals(awayTeam.getId());
    }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }

    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }

    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }
}
