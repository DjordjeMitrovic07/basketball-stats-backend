package com.bballstats.backend.service;

import com.bballstats.backend.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamService {
    Team create(Team team);
    Team update(Long id, Team team);
    void delete(Long id);
    Team findById(Long id);
    Page<Team> findAll(String q, Pageable pageable);
}
