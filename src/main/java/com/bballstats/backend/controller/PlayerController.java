package com.bballstats.backend.controller;

import com.bballstats.backend.dto.PlayerDto;
import com.bballstats.backend.dto.PlayerUpsertDto;
import com.bballstats.backend.entity.Player;
import com.bballstats.backend.entity.Position;
import com.bballstats.backend.entity.Team;
import com.bballstats.backend.service.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "http://localhost:4200")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PlayerDto> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "0") int page,
            // PODIGNUTO sa 10 → 1000
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort
    ) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service
                .findAll(teamId, query, PageRequest.of(page, size, Sort.by(dir, field)))
                .map(PlayerDto::from);
    }

    // Novi endpoint: vrati sve igrače (bez paginacije)
    @GetMapping("/all")
    public List<PlayerDto> listAll(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "lastName,asc") String sort,
            @RequestParam(defaultValue = "1000") int size
    ) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service
                .findAll(teamId, query, PageRequest.of(0, size, Sort.by(dir, field)))
                .map(PlayerDto::from)
                .getContent();
    }

    @GetMapping("/{id}")
    public PlayerDto get(@PathVariable Long id) {
        return PlayerDto.from(service.findById(id));
    }

    @PostMapping
    public PlayerDto create(@RequestBody PlayerUpsertDto dto) {
        Player p = new Player();

        if (dto.getFirstName() != null) p.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)  p.setLastName(dto.getLastName());
        if (dto.getPosition() != null)  p.setPosition(Position.valueOf(dto.getPosition()));
        if (dto.getJerseyNumber() != null) p.setJerseyNumber(dto.getJerseyNumber());
        if (dto.getHeightCm() != null)  p.setHeightCm(dto.getHeightCm());
        if (dto.getWeightKg() != null)  p.setWeightKg(dto.getWeightKg());

        if (dto.getTeamId() != null) {
            Team t = new Team();
            t.setId(dto.getTeamId());
            p.setTeam(t);
        }

        return PlayerDto.from(service.create(p));
    }

    @PutMapping("/{id}")
    public PlayerDto update(@PathVariable Long id, @RequestBody PlayerUpsertDto dto) {
        Player patch = new Player();

        if (dto.getFirstName() != null) patch.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)  patch.setLastName(dto.getLastName());
        if (dto.getPosition() != null)  patch.setPosition(Position.valueOf(dto.getPosition()));
        if (dto.getJerseyNumber() != null) patch.setJerseyNumber(dto.getJerseyNumber());
        if (dto.getHeightCm() != null)  patch.setHeightCm(dto.getHeightCm());
        if (dto.getWeightKg() != null)  patch.setWeightKg(dto.getWeightKg());

        if (dto.getTeamId() != null) {
            Team t = new Team();
            t.setId(dto.getTeamId());
            patch.setTeam(t);
        }

        return PlayerDto.from(service.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
