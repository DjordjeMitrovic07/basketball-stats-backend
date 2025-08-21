package com.bballstats.backend.dto;

import com.bballstats.backend.entity.Player;
import com.bballstats.backend.entity.Position;

public class PlayerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private Position position;
    private int jerseyNumber;
    private Integer heightCm;
    private Integer weightKg;
    private TeamDto team;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public int getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }
    public TeamDto getTeam() { return team; }
    public void setTeam(TeamDto team) { this.team = team; }

    public static PlayerDto from(Player p) {
        PlayerDto dto = new PlayerDto();
        dto.setId(p.getId());
        dto.setFirstName(p.getFirstName());
        dto.setLastName(p.getLastName());
        dto.setPosition(p.getPosition());
        dto.setJerseyNumber(p.getJerseyNumber());
        dto.setHeightCm(p.getHeightCm());
        dto.setWeightKg(p.getWeightKg());
        dto.setTeam(TeamDto.from(p.getTeam())); // sada će imati popunjena polja
        return dto;
    }
}
