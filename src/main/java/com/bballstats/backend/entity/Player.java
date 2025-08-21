package com.bballstats.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Entity
@Table(name = "players",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_jersey", columnNames = {"team_id", "jersey_number"})
        })
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String lastName;

    @NotNull(message = "Position is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Position position;

    @NotNull(message = "Jersey number is required")
    @Min(value = 0, message = "Jersey must be >= 0")
    @Max(value = 99, message = "Jersey must be <= 99")
    @Column(name = "jersey_number", nullable = false)
    private Integer jerseyNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_player_team"))
    private Team team;

    // Opciono (možeš da izostaviš ako ne treba u MVP-u):
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Min(150) @Max(250)
    private Integer heightCm;

    @Min(50) @Max(160)
    private Integer weightKg;

    // Getteri/setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }
}
