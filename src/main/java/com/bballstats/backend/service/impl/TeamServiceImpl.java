package com.bballstats.backend.service.impl;

import com.bballstats.backend.entity.Team;
import com.bballstats.backend.repository.TeamRepository;
import com.bballstats.backend.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository repo;

    public TeamServiceImpl(TeamRepository repo) {
        this.repo = repo;
    }

    @Override
    public Team create(Team team) {
        if (repo.existsByNameIgnoreCase(team.getName())) {
            throw new IllegalArgumentException("Team with the same name already exists");
        }
        return repo.save(team);
    }

    @Override
    public Team update(Long id, Team patch) {
        Team existing = repo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Team not found: id=" + id));

        if (patch.getName() != null && !patch.getName().equalsIgnoreCase(existing.getName())) {
            if (repo.existsByNameIgnoreCase(patch.getName())) {
                throw new IllegalArgumentException("Team with the same name already exists");
            }
            existing.setName(patch.getName());
        }
        if (patch.getCity() != null) existing.setCity(patch.getCity());
        if (patch.getFoundedYear() != null) existing.setFoundedYear(patch.getFoundedYear());

        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Team not found: id=" + id);
        }
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Team findById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Team not found: id=" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Team> findAll(String q, Pageable pageable) {
        if (q != null && !q.isBlank()) {
            return repo.findByNameContainingIgnoreCase(q.trim(), pageable);
        }
        return repo.findAll(pageable);
    }
}
