package com.bballstats.backend.controller;

import com.bballstats.backend.dto.PlayerDto;
import com.bballstats.backend.dto.PlayerUpdateDto;
import com.bballstats.backend.entity.Player;
import com.bballstats.backend.entity.Team;
import com.bballstats.backend.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "10") int size,
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

    @GetMapping("/{id}")
    public PlayerDto get(@PathVariable Long id) {
        return PlayerDto.from(service.findById(id));
    }

    @PostMapping
    public PlayerDto create(@Valid @RequestBody Player player) {
        // Za kreiranje ostavljamo @Valid – zahtevamo sva obavezna polja
        return PlayerDto.from(service.create(player));
    }

    @PutMapping("/{id}")
    public PlayerDto update(@PathVariable Long id, @RequestBody PlayerUpdateDto dto) {
        // Partial update: pripremimo "patch" entitet samo sa prosleđenim poljima
        Player patch = new Player();

        // Osnovna polja (samo ako su prosleđena)
        if (dto.getFirstName() != null) patch.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)  patch.setLastName(dto.getLastName());
        if (dto.getPosition() != null)  patch.setPosition(dto.getPosition());
        if (dto.getJerseyNumber() != null) patch.setJerseyNumber(dto.getJerseyNumber());
        if (dto.getBirthDate() != null) patch.setBirthDate(dto.getBirthDate());
        if (dto.getHeightCm() != null)  patch.setHeightCm(dto.getHeightCm());
        if (dto.getWeightKg() != null)  patch.setWeightKg(dto.getWeightKg());

        // Promena tima – koristimo samo ID referencu
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
