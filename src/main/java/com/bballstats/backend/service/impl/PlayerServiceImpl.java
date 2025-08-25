package com.bballstats.backend.service.impl;

import com.bballstats.backend.entity.Player;
import com.bballstats.backend.repository.BoxScoreRepository;
import com.bballstats.backend.repository.PlayerRepository;
import com.bballstats.backend.service.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository repo;
    private final BoxScoreRepository boxScoreRepo;

    public PlayerServiceImpl(PlayerRepository repo, BoxScoreRepository boxScoreRepo) {
        this.repo = repo;
        this.boxScoreRepo = boxScoreRepo;
    }

    @Override
    public Player create(Player player) {
        if (player.getTeam() != null
                && player.getTeam().getId() != null
                && player.getJerseyNumber() != null
                && repo.existsByTeam_IdAndJerseyNumber(player.getTeam().getId(), player.getJerseyNumber())) {
            throw new IllegalArgumentException("Jersey number already used in this team");
        }

        try {
            Player saved = repo.save(player);
            return repo.findById(saved.getId()).orElse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Invalid foreign key or duplicate constraint");
        }
    }

    @Override
    public Player update(Long id, Player patch) {
        Player existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: id=" + id));

        if (patch.getFirstName() != null) existing.setFirstName(patch.getFirstName());
        if (patch.getLastName()  != null) existing.setLastName(patch.getLastName());
        if (patch.getPosition()  != null) existing.setPosition(patch.getPosition());
        if (patch.getBirthDate() != null) existing.setBirthDate(patch.getBirthDate());
        if (patch.getHeightCm()  != null) existing.setHeightCm(patch.getHeightCm());
        if (patch.getWeightKg()  != null) existing.setWeightKg(patch.getWeightKg());

        if (patch.getTeam() != null) {
            existing.setTeam(patch.getTeam());
        }

        if (patch.getJerseyNumber() != null) {
            Integer newJersey = patch.getJerseyNumber();
            Long targetTeamId = (patch.getTeam() != null && patch.getTeam().getId() != null)
                    ? patch.getTeam().getId()
                    : (existing.getTeam() != null ? existing.getTeam().getId() : null);

            if (targetTeamId != null
                    && !newJersey.equals(existing.getJerseyNumber())
                    && repo.existsByTeam_IdAndJerseyNumber(targetTeamId, newJersey)) {
                throw new IllegalArgumentException("Jersey number already used in this team");
            }
            existing.setJerseyNumber(newJersey);
        }

        Player saved = repo.save(existing);
        return repo.findById(saved.getId()).orElse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Player not found: id=" + id);
        }
        // zaštita: ne dozvoli brisanje igrača koji ima box-score zapise
        if (boxScoreRepo.existsByPlayer_Id(id)) {
            throw new IllegalStateException("Cannot delete player with existing box scores");
        }
        repo.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public Player findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: id=" + id));
    }

    @Override @Transactional(readOnly = true)
    public Page<Player> findAll(Long teamId, String q, Pageable pageable) {
        if (teamId != null) return repo.findByTeam_Id(teamId, pageable);
        if (q != null && !q.isBlank()) return repo.findByLastNameContainingIgnoreCase(q.trim(), pageable);
        return repo.findAll(pageable);
    }
}
