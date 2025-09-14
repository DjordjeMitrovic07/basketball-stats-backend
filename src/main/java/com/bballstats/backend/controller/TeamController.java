package com.bballstats.backend.controller;

import com.bballstats.backend.entity.Team;
import com.bballstats.backend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "http://localhost:4200") // prilagodi po potrebi
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Team> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            // PODIGNUTO sa 10 → 1000
            @RequestParam(name = "size", defaultValue = "1000") int size,
            @RequestParam(name = "sort", defaultValue = "name,asc") String sort
    ) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service.findAll(q, PageRequest.of(page, size, Sort.by(dir, field)));
    }

    // Novi endpoint: vrati sve (bez paginacije) — praktično page=0,size=1000
    @GetMapping("/all")
    public List<Team> listAll(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "sort", defaultValue = "name,asc") String sort,
            @RequestParam(name = "size", defaultValue = "1000") int size
    ) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service.findAll(q, PageRequest.of(0, size, Sort.by(dir, field))).getContent();
    }

    @GetMapping("/{id}")
    public Team get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Team create(@Valid @RequestBody Team team) {
        return service.create(team);
    }

    @PutMapping("/{id}")
    public Team update(@PathVariable Long id, @Valid @RequestBody Team team) {
        return service.update(id, team);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
