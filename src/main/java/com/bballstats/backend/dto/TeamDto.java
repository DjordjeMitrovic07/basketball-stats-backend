package com.bballstats.backend.dto;

import com.bballstats.backend.entity.Team;

public class TeamDto {
    private Long id;
    private String name;
    private String city;
    private Integer foundedYear;
    private String code; // NEW

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public static TeamDto from(Team t) {
        if (t == null) return null;
        TeamDto dto = new TeamDto();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setCity(t.getCity());
        dto.setFoundedYear(t.getFoundedYear());
        dto.setCode(t.getCode()); // NEW
        return dto;
    }
}
