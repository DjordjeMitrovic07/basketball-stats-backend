package com.bballstats.backend.controller;

import com.bballstats.backend.dto.GameCreateDto;
import com.bballstats.backend.dto.GameDto;
import com.bballstats.backend.dto.GameUpdateDto;
import com.bballstats.backend.dto.TeamDto;
import com.bballstats.backend.entity.Game;
import com.bballstats.backend.entity.Team;
import com.bballstats.backend.repository.TeamRepository;
import com.bballstats.backend.service.GameService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "http://localhost:4200")
public class GameController {

    private final GameService service;
    private final TeamRepository teamRepo;

    public GameController(GameService service, TeamRepository teamRepo) {
        this.service = service;
        this.teamRepo = teamRepo;
    }

    // Helper – obogati DTO sigurnim učitavanjem team-ova (bez Lazy iznenađenja)
    private GameDto hydrateTeams(GameDto dto) {
        if (dto == null) return null;

        if (dto.getHomeTeam() == null || dto.getHomeTeam().getName() == null) {
            Long hid = dto.getHomeTeam() != null ? dto.getHomeTeam().getId() : null;
            if (hid != null) {
                Team ht = teamRepo.findById(hid).orElse(null);
                if (ht != null) dto.setHomeTeam(TeamDto.from(ht));
            }
        }
        if (dto.getAwayTeam() == null || dto.getAwayTeam().getName() == null) {
            Long aid = dto.getAwayTeam() != null ? dto.getAwayTeam().getId() : null;
            if (aid != null) {
                Team at = teamRepo.findById(aid).orElse(null);
                if (at != null) dto.setAwayTeam(TeamDto.from(at));
            }
        }
        return dto;
    }

    @GetMapping
    public Page<GameDto> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            // PODIGNUTO sa 10 → 1000
            @RequestParam(defaultValue = "1000") int size,
            // default sort po datumu DESC
            @RequestParam(defaultValue = "dateTime,desc") String sort
    ) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service.findAll(teamId, from, to, PageRequest.of(page, size, Sort.by(dir, field)))
                .map(g -> hydrateTeams(GameDto.from(g)));
    }

    // Novi endpoint: sve utakmice (bez paginacije) sa istim filterima
    @GetMapping("/all")
    public List<GameDto> listAll(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "dateTime,desc") String sort,
            @RequestParam(defaultValue = "1000") int size
    ) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service.findAll(teamId, from, to, PageRequest.of(0, size, Sort.by(dir, field)))
                .map(g -> hydrateTeams(GameDto.from(g)))
                .getContent();
    }

    @GetMapping("/{id}")
    public GameDto get(@PathVariable Long id) {
        return hydrateTeams(GameDto.from(service.findById(id)));
    }

    @PostMapping
    public GameDto create(@Valid @RequestBody GameCreateDto dto) {
        Game g = new Game();
        g.setDateTime(dto.getDateTime());
        if (dto.getHomeTeamId() != null) {
            Team h = new Team(); h.setId(dto.getHomeTeamId()); g.setHomeTeam(h);
        }
        if (dto.getAwayTeamId() != null) {
            Team a = new Team(); a.setId(dto.getAwayTeamId()); g.setAwayTeam(a);
        }
        g.setHomeScore(dto.getHomeScore());
        g.setAwayScore(dto.getAwayScore());

        Game created = service.create(g);
        return hydrateTeams(GameDto.from(created));
    }

    @PutMapping("/{id}")
    public GameDto update(@PathVariable Long id, @RequestBody GameUpdateDto dto) {
        Game patch = new Game();
        patch.setDateTime(dto.getDateTime());
        if (dto.getHomeTeamId() != null) { Team h = new Team(); h.setId(dto.getHomeTeamId()); patch.setHomeTeam(h); }
        if (dto.getAwayTeamId() != null) { Team a = new Team(); a.setId(dto.getAwayTeamId()); patch.setAwayTeam(a); }
        patch.setHomeScore(dto.getHomeScore());
        patch.setAwayScore(dto.getAwayScore());

        Game updated = service.update(id, patch);
        return hydrateTeams(GameDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
