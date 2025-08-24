package com.bballstats.backend.controller;

import com.bballstats.backend.dto.BoxScoreCreateDto;
import com.bballstats.backend.dto.BoxScoreDto;
import com.bballstats.backend.dto.BoxScoreUpdateDto;
import com.bballstats.backend.entity.BoxScore;
import com.bballstats.backend.entity.Game;
import com.bballstats.backend.entity.Player;
import com.bballstats.backend.service.BoxScoreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class BoxScoreController {

    private final BoxScoreService service;

    public BoxScoreController(BoxScoreService service) {
        this.service = service;
    }

    // 1) SVE ZA JEDNU UTAKMICU
    @GetMapping("/games/{gameId}/boxscore")
    public Page<BoxScoreDto> listForGame(@PathVariable Long gameId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "pts,desc") String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service.byGame(gameId, PageRequest.of(page, size, Sort.by(dir, parts[0])))
                .map(b -> BoxScoreDto.from(b, service.calcEfg(b), service.calcTs(b)));
    }

    @PostMapping("/games/{gameId}/boxscore")
    public BoxScoreDto addForGame(@PathVariable Long gameId, @Valid @RequestBody BoxScoreCreateDto dto) {
        BoxScore b = new BoxScore();
        Game g = new Game(); g.setId(gameId); b.setGame(g);
        Player p = new Player(); p.setId(dto.getPlayerId()); b.setPlayer(p);

        b.setPts(dto.pts); b.setFgm(dto.fgm); b.setFga(dto.fga);
        b.setTp3m(dto.tp3m); b.setTp3a(dto.tp3a);
        b.setFtm(dto.ftm); b.setFta(dto.fta);
        b.setReb(dto.reb); b.setAst(dto.ast); b.setStl(dto.stl); b.setBlk(dto.blk);
        b.setTov(dto.tov); b.setMin(dto.min);

        BoxScore saved = service.create(b);
        return BoxScoreDto.from(saved, service.calcEfg(saved), service.calcTs(saved));
    }

    // 2) SVE ZA JEDNOG IGRAČA  ← sa normalizacijom sort polja
    @GetMapping("/players/{playerId}/boxscore")
    public Page<BoxScoreDto> listForPlayer(@PathVariable Long playerId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(defaultValue = "game.dateTime,desc") String sort) {
        String[] parts = sort.split(",");
        String field = normalizeSortField(parts[0]); // <-- PREVOD
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service.byPlayer(playerId, PageRequest.of(page, size, Sort.by(dir, field)))
                .map(b -> BoxScoreDto.from(b, service.calcEfg(b), service.calcTs(b)));
    }

    // Helper: dozvoli "dateTime" i prevedi ga u "game.dateTime"
    private String normalizeSortField(String raw) {
        if (raw == null || raw.isBlank()) return "game.dateTime";
        String f = raw.trim();
        if (f.equals("dateTime")) return "game.dateTime";
        // Dodaj po želji još prevoda:
        // if (f.equals("playerLastName")) return "player.lastName";
        return f;
    }

    // 3) SVI BOXSCORE-ovi (globalno)
    @GetMapping("/boxscore")
    public Page<BoxScoreDto> listAll(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(defaultValue = "pts,desc") String sort) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return service.all(PageRequest.of(page, size, Sort.by(dir, field)))
                .map(b -> BoxScoreDto.from(b, service.calcEfg(b), service.calcTs(b)));
    }

    // 4) IZMENA + BRISANJE
    @PutMapping("/boxscore/{id}")
    public BoxScoreDto update(@PathVariable Long id, @RequestBody BoxScoreUpdateDto dto) {
        BoxScore patch = new BoxScore();
        patch.setPts(dto.pts); patch.setFgm(dto.fgm); patch.setFga(dto.fga);
        patch.setTp3m(dto.tp3m); patch.setTp3a(dto.tp3a);
        patch.setFtm(dto.ftm); patch.setFta(dto.fta);
        patch.setReb(dto.reb); patch.setAst(dto.ast); patch.setStl(dto.stl); patch.setBlk(dto.blk);
        patch.setTov(dto.tov); patch.setMin(dto.min);

        BoxScore saved = service.update(id, patch);
        return BoxScoreDto.from(saved, service.calcEfg(saved), service.calcTs(saved));
    }

    @DeleteMapping("/boxscore/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
