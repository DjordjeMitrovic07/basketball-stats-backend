package com.bballstats.backend.service.impl;

import com.bballstats.backend.entity.BoxScore;
import com.bballstats.backend.repository.BoxScoreRepository;
import com.bballstats.backend.service.BoxScoreService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BoxScoreServiceImpl implements BoxScoreService {

    private final BoxScoreRepository repo;

    public BoxScoreServiceImpl(BoxScoreRepository repo) {
        this.repo = repo;
    }

    @Override
    public BoxScore create(BoxScore box) {
        Long gid = box.getGame().getId();
        Long pid = box.getPlayer().getId();
        if (repo.existsByGame_IdAndPlayer_Id(gid, pid)) {
            throw new IllegalArgumentException("Box score for this player and game already exists");
        }
        try {
            BoxScore saved = repo.save(box);
            // ✅ odmah vrati detaljno učitan objekat (sa player.firstName/lastName)
            return repo.findDetailedById(saved.getId()).orElse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Invalid foreign key");
        }
    }

    @Override
    public BoxScore update(Long id, BoxScore patch) {
        BoxScore existing = repo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("BoxScore not found: id=" + id));

        if (patch.getPts()  != null) existing.setPts(patch.getPts());
        if (patch.getFgm()  != null) existing.setFgm(patch.getFgm());
        if (patch.getFga()  != null) existing.setFga(patch.getFga());
        if (patch.getTp3m() != null) existing.setTp3m(patch.getTp3m());
        if (patch.getTp3a() != null) existing.setTp3a(patch.getTp3a());
        if (patch.getFtm()  != null) existing.setFtm(patch.getFtm());
        if (patch.getFta()  != null) existing.setFta(patch.getFta());
        if (patch.getReb()  != null) existing.setReb(patch.getReb());
        if (patch.getAst()  != null) existing.setAst(patch.getAst());
        if (patch.getStl()  != null) existing.setStl(patch.getStl());
        if (patch.getBlk()  != null) existing.setBlk(patch.getBlk());
        if (patch.getTov()  != null) existing.setTov(patch.getTov());
        if (patch.getMin()  != null) existing.setMin(patch.getMin());

        BoxScore saved = repo.save(existing);
        // ✅ nakon update-a isto vraćamo detaljno učitani objekat
        return repo.findDetailedById(saved.getId()).orElse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("BoxScore not found: id=" + id);
        repo.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public Page<BoxScore> byGame(Long gameId, Pageable pageable) {
        return repo.findByGame_Id(gameId, pageable);
    }

    @Override @Transactional(readOnly = true)
    public Page<BoxScore> byPlayer(Long playerId, Pageable pageable) {
        return repo.findByPlayer_Id(playerId, pageable);
    }

    @Override
    public double calcEfg(BoxScore b) {
        double fga = b.getFga() != null ? b.getFga() : 0;
        if (fga == 0) return Double.NaN;
        double fgm = b.getFgm() != null ? b.getFgm() : 0;
        double tp3m = b.getTp3m() != null ? b.getTp3m() : 0;
        return (fgm + 0.5 * tp3m) / fga;
    }

    @Override
    public double calcTs(BoxScore b) {
        double fga = b.getFga() != null ? b.getFga() : 0;
        double fta = b.getFta() != null ? b.getFta() : 0;
        double denom = 2 * (fga + 0.44 * fta);
        if (denom == 0) return Double.NaN;
        double pts = b.getPts() != null ? b.getPts() : 0;
        return pts / denom;
    }

    // BoxScoreServiceImpl.java
    @Override @Transactional(readOnly = true)
    public Page<BoxScore> all(Pageable pageable) {
        return repo.findAllBy(pageable);
    }

    @Override @Transactional(readOnly = true)
    public BoxScore findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("BoxScore not found: id=" + id));
    }


}
