package com.bballstats.backend.repository;

import com.bballstats.backend.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByNameIgnoreCase(String name);
    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
