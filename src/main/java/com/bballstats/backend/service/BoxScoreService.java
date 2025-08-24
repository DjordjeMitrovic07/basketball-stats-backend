package com.bballstats.backend.service;

import com.bballstats.backend.entity.BoxScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoxScoreService {
    BoxScore create(BoxScore box);
    BoxScore update(Long id, BoxScore patch);
    BoxScore findById(Long id);
    void delete(Long id);
    Page<BoxScore> byGame(Long gameId, Pageable pageable);
    Page<BoxScore> byPlayer(Long playerId, Pageable pageable);

    double calcEfg(BoxScore b); // (FGM + 0.5*3PM)/FGA
    double calcTs(BoxScore b);  // PTS / (2*(FGA + 0.44*FTA))
    // BoxScoreService.java
    Page<BoxScore> all(Pageable pageable);

}
