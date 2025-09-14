package com.bballstats.backend.service.impl;

import com.bballstats.backend.dto.metrics.LeaderDto;
import com.bballstats.backend.dto.metrics.PlayerSeasonMetricsDto;
import com.bballstats.backend.repository.BoxScoreRepository;
import com.bballstats.backend.repository.projections.PlayerSeasonAgg;
import com.bballstats.backend.service.PlayersMetricsService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlayersMetricsServiceImpl implements PlayersMetricsService {

    private final BoxScoreRepository repo;

    public PlayersMetricsServiceImpl(BoxScoreRepository repo) {
        this.repo = repo;
    }

    @Override
    public Page<PlayerSeasonMetricsDto> playersMetrics(
            String season, Long teamId, String q, Integer minGames, String sort, Pageable pageable
    ) {
        // Dohvat bez sortiranja iz baze – sortiramo po izvedenim metrikama
        Page<PlayerSeasonAgg> raw = repo.aggregatePlayersBySeason(season, teamId, blankToNull(q),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));

        // Mapiranje u DTO + filter po minGames
        List<PlayerSeasonMetricsDto> all = raw.getContent().stream()
                .map(mapper())
                .filter(d -> minGames == null || d.getGames() >= minGames)
                .collect(Collectors.toList());

        // Sortiranje po metrici (ppg, tsPct, efgPct, tp3Pct, rpg, apg …)
        Comparator<PlayerSeasonMetricsDto> cmp = comparatorFor(sort);
        all.sort(cmp);

        // Paginacija na memoriji (jer smo promenili redosled)
        int from = Math.min(pageable.getPageNumber() * pageable.getPageSize(), all.size());
        int to   = Math.min(from + pageable.getPageSize(), all.size());
        List<PlayerSeasonMetricsDto> pageSlice = all.subList(from, to);

        return new PageImpl<>(pageSlice, pageable, all.size());
    }

    @Override
    public List<LeaderDto> leaderboard(String season, String metric, Long teamId, Integer minGames, int topN) {
        List<PlayerSeasonMetricsDto> all = playersMetrics(
                season, teamId, null, minGames, metric + ",desc", PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();

        return all.stream()
                .limit(topN)
                .map(d -> new LeaderDto(d.getPlayerId(), d.getPlayerName(), d.getTeamAbbr(), valueByMetric(d, metric)))
                .collect(Collectors.toList());
    }

    /* =====================  Helpers  ===================== */

    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }

    private Function<PlayerSeasonAgg, PlayerSeasonMetricsDto> mapper() {
        return a -> {
            PlayerSeasonMetricsDto d = new PlayerSeasonMetricsDto();

            String first = Optional.ofNullable(a.getPlayerFirstName()).orElse("").trim();
            String last  = Optional.ofNullable(a.getPlayerLastName()).orElse("").trim();
            String full  = (first + " " + last).trim();

            d.setPlayerId(a.getPlayerId());
            d.setPlayerName(full.isEmpty() ? "—" : full);

            d.setTeamId(a.getTeamId());
            d.setTeamName(a.getTeamName());
            d.setTeamAbbr(a.getTeamAbbr());

            int games = safeInt(a.getGames());
            d.setGames(games);

            double pts = safe(a.getPts()), reb = safe(a.getReb()), ast = safe(a.getAst());
            double stl = safe(a.getStl()), blk = safe(a.getBlk()), tov = safe(a.getTov());
            double min = safe(a.getMin());
            double fgm = safe(a.getFgm()), fga = safe(a.getFga());
            double tpm = safe(a.getTp3m()), tpa = safe(a.getTp3a());
            double ftm = safe(a.getFtm()), fta = safe(a.getFta());

            // per-game
            d.setPpg(games > 0 ? pts / games : 0);
            d.setRpg(games > 0 ? reb / games : 0);
            d.setApg(games > 0 ? ast / games : 0);
            d.setSpg(games > 0 ? stl / games : 0);
            d.setBpg(games > 0 ? blk / games : 0);
            d.setTovpg(games > 0 ? tov / games : 0);
            d.setMpg(games > 0 ? min / games : 0);

            // procenti (0–1)
            d.setFgPct(fga > 0 ? fgm / fga : 0);
            d.setTp3Pct(tpa > 0 ? tpm / tpa : 0);
            d.setFtPct(fta > 0 ? ftm / fta : 0);
            d.setEfgPct(fga > 0 ? (fgm + 0.5 * tpm) / fga : 0);
            double tsDen = 2 * (fga + 0.44 * fta);
            d.setTsPct(tsDen > 0 ? pts / tsDen : 0);

            return d;
        };
    }

    private static int safeInt(Long v) { return v == null ? 0 : v.intValue(); }
    private static double safe(Long v) { return v == null ? 0.0 : v.doubleValue(); }

    private static Comparator<PlayerSeasonMetricsDto> comparatorFor(String sort) {
        String key = (sort == null || sort.isBlank()) ? "ppg,desc" : sort.trim().toLowerCase();
        boolean desc = key.endsWith(",desc");
        String field = key.replace(",desc","").replace(",asc","").trim();

        Function<PlayerSeasonMetricsDto, Double> f = switch (field) {
            case "ppg"   -> PlayerSeasonMetricsDto::getPpg;
            case "rpg"   -> PlayerSeasonMetricsDto::getRpg;
            case "apg"   -> PlayerSeasonMetricsDto::getApg;
            case "spg"   -> PlayerSeasonMetricsDto::getSpg;
            case "bpg"   -> PlayerSeasonMetricsDto::getBpg;
            case "tovpg" -> PlayerSeasonMetricsDto::getTovpg;
            case "mpg"   -> PlayerSeasonMetricsDto::getMpg;
            case "fgpct" -> PlayerSeasonMetricsDto::getFgPct;
            case "tp3pct"-> PlayerSeasonMetricsDto::getTp3Pct;
            case "ftpct" -> PlayerSeasonMetricsDto::getFtPct;
            case "tspct" -> PlayerSeasonMetricsDto::getTsPct;
            case "efgpct"-> PlayerSeasonMetricsDto::getEfgPct;
            default      -> PlayerSeasonMetricsDto::getPpg;
        };

        Comparator<PlayerSeasonMetricsDto> cmp = Comparator.comparing(f, Comparator.nullsLast(Double::compare));
        return desc ? cmp.reversed() : cmp;
    }

    private static Double valueByMetric(PlayerSeasonMetricsDto d, String metric) {
        return switch (metric == null ? "" : metric.toLowerCase()) {
            case "rpg"    -> d.getRpg();
            case "apg"    -> d.getApg();
            case "spg"    -> d.getSpg();
            case "bpg"    -> d.getBpg();
            case "tovpg"  -> d.getTovpg();
            case "mpg"    -> d.getMpg();
            case "fgpct"  -> d.getFgPct();
            case "tp3pct" -> d.getTp3Pct();
            case "ftpct"  -> d.getFtPct();
            case "tspct"  -> d.getTsPct();
            case "efgpct" -> d.getEfgPct();
            default       -> d.getPpg();
        };
    }
}
