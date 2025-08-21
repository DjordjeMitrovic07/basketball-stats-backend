package com.bballstats.backend.service;

import com.bballstats.backend.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlayerService {
    Player create(Player player);
    Player update(Long id, Player player);
    void delete(Long id);
    Player findById(Long id);
    Page<Player> findAll(Long teamId, String q, Pageable pageable);
}
