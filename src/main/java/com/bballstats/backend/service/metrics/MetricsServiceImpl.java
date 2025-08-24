package com.bballstats.backend.service.metrics;

import com.bballstats.backend.dto.metrics.*;
import com.bballstats.backend.entity.BoxScore;
import com.bballstats.backend.entity.Game;
import com.bballstats.backend.entity.Player;
import com.bballstats.backend.entity.Team;
import com.bballstats.backend.repository.BoxScoreRepository;
import com.bballstats.backend.repository.GameRepository;
import com.bballstats.backend.repository.PlayerRepository;
import com.bballstats.backend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.bballstats.backend.service.metrics.MetricsUtils.*;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final BoxScoreRepository boxScoreRepo;
    private final GameRepository gameRepo;
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;

    // 40 (FIBA) ili 48 (NBA)
    private final double PERIOD_LENGTH = 40.0;

    @Override
    public PlayerMetricsDto getPlayerMetrics(Long playerId, String season) {
        Player p = playerRepo.findById(playerId).orElseThrow(() -> new NoSuchElementException("Player not found"));
        List<BoxScore> bsList = boxScoreRepo.findByPlayerIdAndSeason(playerId, season);

        int games = bsList.size();
        int minutes = sum(bsList, BoxScore::getMin);
        int pts = sum(bsList, BoxScore::getPts);
        int fgm = sum(bsList, BoxScore::getFgm);
        int fga = sum(bsList, BoxScore::getFga);
        int tpm = sum(bsList, BoxScore::getTp3m);
        int tpa = sum(bsList, BoxScore::getTp3a);
        int ftm = sum(bsList, BoxScore::getFtm);
        int fta = sum(bsList, BoxScore::getFta);
        int tov = sum(bsList, BoxScore::getTov);

        // Team totals for USG denominator (po utakmici)
        Map<Long, List<BoxScore>> byGame = bsList.stream().collect(Collectors.groupingBy(b -> b.getGame().getId()));
        int teamMinutes = 0, teamFga = 0, teamFta = 0, teamTov = 0;
        for (List<BoxScore> gameBs : byGame.values()){
            teamMinutes += gameBs.stream().mapToInt(BoxScore::getMin).sum();
            teamFga     += gameBs.stream().mapToInt(BoxScore::getFga).sum();
            teamFta     += gameBs.stream().mapToInt(BoxScore::getFta).sum();
            teamTov     += gameBs.stream().mapToInt(BoxScore::getTov).sum();
        }

        double efgVal = efg(fgm, tpm, fga);
        double tsVal  = ts(pts, fga, fta);
        double usgVal = usg(fga, fta, tov, minutes, teamFga, teamFta, teamTov, teamMinutes);

        return PlayerMetricsDto.builder()
                .playerId(p.getId())
                .playerName(p.getFirstName() + " " + p.getLastName())
                .teamId(p.getTeam().getId())
                .teamName(p.getTeam().getName())
                .games(games)
                .minutes(minutes)
                .pts(pts)
                .fgm(fgm).fga(fga)
                .tpm(tpm).tpa(tpa)
                .ftm(ftm).fta(fta)
                .tov(tov)
                .efg(round3(efgVal))
                .ts(round3(tsVal))
                .usg(round3(usgVal))
                .ptsPerGame(round1(safeDiv(pts, (double)Math.max(games,1))))
                .minPerGame(round1(safeDiv(minutes, (double)Math.max(games,1))))
                .build();
    }

    @Override
    public TeamMetricsDto getTeamMetrics(Long teamId, String season) {
        Team t = teamRepo.findById(teamId).orElseThrow(() -> new NoSuchElementException("Team not found"));
        List<Game> games = gameRepo.findByTeamAndSeason(teamId, season);

        int totalTeamPoints = 0, totalOppPoints = 0, teamMinutes = 0;
        double sumTeamPoss = 0.0, sumOppPoss = 0.0;
        int teamFgm=0, teamFga=0, teamTpm=0, teamTpa=0, teamFtm=0, teamFta=0;

        for (Game g : games) {
            boolean isHome = g.getHomeTeam().getId().equals(teamId);
            int teamScore = isHome ? g.getHomeScore() : g.getAwayScore();
            int oppScore  = isHome ? g.getAwayScore() : g.getHomeScore();

            List<BoxScore> teamBs = boxScoreRepo.findByGameIdAndTeamId(g.getId(), teamId);
            Long oppTeamId = isHome ? g.getAwayTeam().getId() : g.getHomeTeam().getId();
            List<BoxScore> oppBs  = boxScoreRepo.findByGameIdAndTeamId(g.getId(), oppTeamId);

            int tFgm = sum(teamBs, BoxScore::getFgm);
            int tFga = sum(teamBs, BoxScore::getFga);
            int tFta = sum(teamBs, BoxScore::getFta);
            int tTov = sum(teamBs, BoxScore::getTov);
            int tMin = sum(teamBs, BoxScore::getMin);
            int tTpm = sum(teamBs, BoxScore::getTp3m);
            int tTpa = sum(teamBs, BoxScore::getTp3a);
            int tFtm = sum(teamBs, BoxScore::getFtm);

            int oFgm = sum(oppBs, BoxScore::getFgm);
            int oFga = sum(oppBs, BoxScore::getFga);
            int oFta = sum(oppBs, BoxScore::getFta);
            int oTov = sum(oppBs, BoxScore::getTov);
            int oMin = sum(oppBs, BoxScore::getMin);

            double teamPoss = possessionsSimple(tFga, tFgm, tFta, tTov);
            double oppPoss  = possessionsSimple(oFga, oFgm, oFta, oTov);

            totalTeamPoints += teamScore;
            totalOppPoints  += oppScore;
            teamMinutes     += tMin; // koristimo minute našeg tima za pace
            sumTeamPoss     += teamPoss;
            sumOppPoss      += oppPoss;

            teamFgm += tFgm; teamFga += tFga; teamTpm += tTpm; teamTpa += tTpa; teamFtm += tFtm; teamFta += tFta;
        }

        int gamesCount = games.size();
        double pace = pace(sumTeamPoss, sumOppPoss, Math.max(teamMinutes,1), PERIOD_LENGTH);
        double ortgVal = ortg(totalTeamPoints, sumTeamPoss);
        double drtgVal = drtg(totalOppPoints,  sumTeamPoss);
        double efgVal  = efg(teamFgm, teamTpm, teamFga);
        double tsVal   = ts(totalTeamPoints, teamFga, teamFta);

        return TeamMetricsDto.builder()
                .teamId(t.getId())
                .teamName(t.getName())
                .season(season)
                .games(gamesCount)
                .teamPoints(totalTeamPoints)
                .oppPoints(totalOppPoints)
                .teamPossessions(round3(sumTeamPoss))
                .oppPossessions(round3(sumOppPoss))
                .teamMinutes(teamMinutes)
                .pace(round2(pace))
                .ortg(round1(ortgVal))
                .drtg(round1(drtgVal))
                .efg(round3(efgVal))
                .ts(round3(tsVal))
                .build();
    }

    @Override
    public GameMetricsDto getGameMetrics(Long gameId) {
        Game g = gameRepo.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found"));

        List<BoxScore> home = boxScoreRepo.findByGameIdAndTeamId(g.getId(), g.getHomeTeam().getId());
        List<BoxScore> away = boxScoreRepo.findByGameIdAndTeamId(g.getId(), g.getAwayTeam().getId());

        int hFgm = sum(home, BoxScore::getFgm), hFga = sum(home, BoxScore::getFga), hFta = sum(home, BoxScore::getFta), hTov = sum(home, BoxScore::getTov), hMin = sum(home, BoxScore::getMin);
        int aFgm = sum(away, BoxScore::getFgm), aFga = sum(away, BoxScore::getFga), aFta = sum(away, BoxScore::getFta), aTov = sum(away, BoxScore::getTov), aMin = sum(away, BoxScore::getMin);

        double homePoss = possessionsSimple(hFga, hFgm, hFta, hTov);
        double awayPoss = possessionsSimple(aFga, aFgm, aFta, aTov);
        double paceVal  = pace(homePoss, awayPoss, Math.max(hMin,1), PERIOD_LENGTH);

        double homeOR   = ortg(g.getHomeScore(), homePoss);
        double homeDR   = drtg(g.getAwayScore(), homePoss);
        double awayOR   = ortg(g.getAwayScore(), awayPoss);
        double awayDR   = drtg(g.getHomeScore(), awayPoss);

        return GameMetricsDto.builder()
                .gameId(g.getId())
                .date(g.getDateTime() != null ? g.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
                .homeTeamId(g.getHomeTeam().getId()).homeTeamName(g.getHomeTeam().getName())
                .awayTeamId(g.getAwayTeam().getId()).awayTeamName(g.getAwayTeam().getName())
                .homeScore(g.getHomeScore()).awayScore(g.getAwayScore())
                .homePoss(round3(homePoss)).awayPoss(round3(awayPoss))
                .pace(round2(paceVal))
                .homeORtg(round1(homeOR)).homeDRtg(round1(homeDR))
                .awayORtg(round1(awayOR)).awayDRtg(round1(awayDR))
                .build();
    }

    @Override
    public List<LeaderDto> getLeaders(String season, String metric, int n, Integer minGames, Integer minMinutesPerGame) {
        List<Player> players = playerRepo.findAll();
        List<LeaderDto> rows = new ArrayList<>();

        for (Player p : players){
            List<BoxScore> bs = boxScoreRepo.findByPlayerIdAndSeason(p.getId(), season);
            int games = bs.size();
            if (minGames != null && games < minGames) continue;

            int minutes = sum(bs, BoxScore::getMin);
            double mpg = safeDiv(minutes, Math.max(games,1));
            if (minMinutesPerGame != null && mpg < minMinutesPerGame) continue;

            int pts = sum(bs, BoxScore::getPts);
            int fgm = sum(bs, BoxScore::getFgm), fga = sum(bs, BoxScore::getFga);
            int tpm = sum(bs, BoxScore::getTp3m), tpa = sum(bs, BoxScore::getTp3a);
            int fta = sum(bs, BoxScore::getFta);
            int tov = sum(bs, BoxScore::getTov);

            double value;
            switch (metric.toLowerCase()){
                case "ts":  value = ts(pts, fga, fta); break;
                case "efg": value = efg(fgm, tpm, fga); break;
                case "usg":
                    Map<Long, List<BoxScore>> byGame = bs.stream().collect(Collectors.groupingBy(b -> b.getGame().getId()));
                    int teamMinutes=0, teamFga=0, teamFta=0, teamTov=0;
                    for (List<BoxScore> gameBs : byGame.values()){
                        teamMinutes += gameBs.stream().mapToInt(BoxScore::getMin).sum();
                        teamFga     += gameBs.stream().mapToInt(BoxScore::getFga).sum();
                        teamFta     += gameBs.stream().mapToInt(BoxScore::getFta).sum();
                        teamTov     += gameBs.stream().mapToInt(BoxScore::getTov).sum();
                    }
                    value = usg(fga, fta, tov, minutes, teamFga, teamFta, teamTov, teamMinutes);
                    break;
                case "pts":
                default:    value = safeDiv(pts, Math.max(games,1)); // PPG
            }

            rows.add(LeaderDto.builder()
                    .playerId(p.getId())
                    .playerName(p.getFirstName() + " " + p.getLastName())
                    .teamId(p.getTeam().getId())
                    .teamName(p.getTeam().getName())
                    .metric(metric.toLowerCase())
                    .value(round3(value))
                    .games(games)
                    .minPerGame(round1(mpg))
                    .build());
        }

        return rows.stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerCompareDto> comparePlayers(List<Long> playerIds, String season) {
        return playerIds.stream().map(id -> {
            PlayerMetricsDto m = getPlayerMetrics(id, season);
            Player p = playerRepo.findById(id).orElseThrow();
            return PlayerCompareDto.builder()
                    .playerId(id)
                    .playerName(p.getFirstName() + " " + p.getLastName())
                    .teamId(p.getTeam().getId())
                    .teamName(p.getTeam().getName())
                    .ts(m.getTs())
                    .efg(m.getEfg())
                    .usg(m.getUsg())
                    .ptsPerGame(m.getPtsPerGame())
                    .minPerGame(m.getMinPerGame())
                    .build();
        }).collect(Collectors.toList());
    }

    // helpers
    private static int sum(List<BoxScore> list, ToInt<BoxScore> f){
        int s = 0;
        for (BoxScore b : list) s += f.get(b);
        return s;
    }
    @FunctionalInterface private interface ToInt<T>{ int get(T t); }
}
