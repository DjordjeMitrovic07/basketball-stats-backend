package com.bballstats.backend.service.impl;

import com.bballstats.backend.entity.Game;
import com.bballstats.backend.entity.Team;
import com.bballstats.backend.repository.GameRepository;
import com.bballstats.backend.service.GameService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameServiceImpl implements GameService {

    private final GameRepository repo;

    public GameServiceImpl(GameRepository repo) {
        this.repo = repo;
    }

    private void validateTeamsDifferent(Game g) {
        Team h = g.getHomeTeam();
        Team a = g.getAwayTeam();
        if (h != null && a != null && h.getId() != null && h.getId().equals(a.getId())) {
            throw new IllegalArgumentException("homeTeam and awayTeam must be different");
        }
    }

    /** Pomocna: prisilno inicijalizuj timove dok je TX otvorena */
    private void touchTeams(Game g) {
        if (g == null) return;
        if (g.getHomeTeam() != null) {
            // pristup poljima forsira učitavanje i kad je LAZY
            g.getHomeTeam().getName();
            g.getHomeTeam().getCity();
            g.getHomeTeam().getFoundedYear();
        }
        if (g.getAwayTeam() != null) {
            g.getAwayTeam().getName();
            g.getAwayTeam().getCity();
            g.getAwayTeam().getFoundedYear();
        }
    }

    @Override
    public Game create(Game game) {
        validateTeamsDifferent(game);
        try {
            Game saved = repo.save(game);
            Game reloaded = repo.findById(saved.getId()).orElse(saved); // @EntityGraph učitava timove
            touchTeams(reloaded); // eksplicitno inicijalizuj
            return reloaded;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Invalid foreign key or constraint violation");
        }
    }

    @Override
    public Game update(Long id, Game patch) {
        Game existing = repo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Game not found: id=" + id));

        if (patch.getDateTime() != null) existing.setDateTime(patch.getDateTime());
        if (patch.getHomeTeam() != null) existing.setHomeTeam(patch.getHomeTeam());
        if (patch.getAwayTeam() != null) existing.setAwayTeam(patch.getAwayTeam());
        if (patch.getHomeScore() != null) existing.setHomeScore(patch.getHomeScore());
        if (patch.getAwayScore() != null) existing.setAwayScore(patch.getAwayScore());

        validateTeamsDifferent(existing);

        Game saved = repo.save(existing);
        Game reloaded = repo.findById(saved.getId()).orElse(saved);
        touchTeams(reloaded);
        return reloaded;
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("Game not found: id=" + id);
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Game findById(Long id) {
        Game g = repo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Game not found: id=" + id));
        touchTeams(g);
        return g;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Game> findAll(Long teamId, java.time.LocalDateTime from,
                              java.time.LocalDateTime to, Pageable pageable) {
        Page<Game> page = repo.search(teamId, from, to, pageable);
        // inicijalizuj timove za sve rezultate u listi
        page.forEach(this::touchTeams);
        return page;
    }
}
