package com.bballstats.backend.service;

import com.bballstats.backend.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface GameService {
    Game create(Game game);
    Game update(Long id, Game patch);
    void delete(Long id);
    Game findById(Long id);
    Page<Game> findAll(Long teamId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
