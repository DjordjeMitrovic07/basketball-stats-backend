package com.bballstats.backend.config;

import com.bballstats.backend.entity.*;
import com.bballstats.backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final GameRepository gameRepo;
    private final BoxScoreRepository boxRepo;

    private static final boolean ENABLED = true;

    private final Map<String, Team>   teamByCode = new HashMap<>();
    private final Map<String, Team>   teamByExt  = new HashMap<>();
    private final Map<String, Player> playerByCode = new HashMap<>();
    private final Map<Integer, Long>  gameIndexMap = new HashMap<>();

    private static final DateTimeFormatter DT_T     = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DT_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @PostConstruct
    @Transactional
    public void run() {
        if (!ENABLED) return;

        if (teamRepo.count() > 0L || playerRepo.count() > 0L || gameRepo.count() > 0L || boxRepo.count() > 0L) {
            System.out.println("ℹ️  Seed preskočen (baza nije prazna).");
            return;
        }

        try {
            loadTeams();
            addNumericAliasesRobust();
            loadPlayers();
            loadGames();
            loadBoxScores();
            System.out.println("✅ Seed gotov.");
        } catch (Exception e) {
            System.err.println("❌ Seed nije uspeo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Seeding failed", e);
        }
    }

    /* ================= CSV helper ================= */

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') return s.substring(1);
        return s;
    }

    private static boolean looksLikeHeader(String raw) {
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isLetter(c)) return true;
        }
        return false;
    }

    // Preskače prazno i komentare (#...). Ako skipHeader==true, preskoči PRVI stvarni header:
    // header = prva nekomentarisana linija čiji prvi token NIJE numerički.
    // Ako prva linija počinje brojem (npr. "101,..."), to je podatak — NE preskači je.
    private List<String[]> readCsv(String path, boolean skipHeader) throws Exception {
        ClassPathResource res = new ClassPathResource(path);
        if (!res.exists()) throw new IllegalStateException("CSV ne postoji: " + path);

        List<String[]> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = !skipHeader;
            while ((line = br.readLine()) != null) {
                String raw = stripBom(line).trim();
                if (raw.isEmpty()) continue;

                int hash = raw.indexOf('#');
                if (hash == 0) continue;                            // cela linija komentar
                if (hash > 0) raw = raw.substring(0, hash).trim();  // inline komentar
                if (raw.isEmpty()) continue;

                if (!headerSkipped) {
                    String firstToken = raw.contains(",") ? raw.substring(0, raw.indexOf(",")).trim() : raw.trim();
                    boolean firstTokenIsNumeric = isNumeric(firstToken);
                    if (!firstTokenIsNumeric) { // header
                        headerSkipped = true;
                        continue;
                    }
                    headerSkipped = true; // prvi red je zapravo podatak
                }

                String[] parts = raw.split(",", -1);
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = stripBom(parts[i] == null ? "" : parts[i].trim());
                }
                out.add(parts);
            }
        }
        return out;
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isBlank()) return false;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    private static Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException nfe) {
            throw new IllegalStateException("Očekivan broj, dobio: \"" + s + "\"", nfe);
        }
    }

    private static LocalDateTime parseDateTimeFlexible(String s) {
        String txt = s == null ? "" : s.trim();
        try { return LocalDateTime.parse(txt, DT_T); }
        catch (Exception ignore) { }
        return LocalDateTime.parse(txt, DT_SPACE);
    }

    private String abbrFromName(String name) {
        if (name == null || name.isBlank()) return "";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(3, parts[0].length())).toUpperCase();
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append(Character.toUpperCase(p.charAt(0)));
        return sb.substring(0, Math.min(4, sb.length()));
    }

    /* ================= TEAMS =================
       teams.csv:
       B) extId,code,name,city,founded   (tako ti je sada)
       (podržavamo i A: code,name,city,founded)
     */
    private void loadTeams() throws Exception {
        List<String[]> rows = readCsv("seed/teams.csv", true);
        for (String[] r : rows) {
            String code;
            String name;
            String city;
            Integer founded;
            String extId = null;

            if (r.length >= 5 && !r[1].isBlank()) {
                // B) extId, code, name, city, founded
                extId  = r[0].trim();
                code   = r[1].trim();
                name   = r[2].trim();
                city   = r[3].trim();
                founded= parseInt(r[4]);
            } else {
                // A) code, name, city, founded
                code   = r[0].trim();
                name   = r[1].trim();
                city   = r[2].trim();
                founded= parseInt(r[3]);
            }

            Team t = new Team();
            t.setName(name);
            t.setCity(city);
            t.setFoundedYear(founded);
            t.setCode(code);
            t = teamRepo.save(t);

            if (code != null && !code.isBlank())
                teamByCode.put(code.toLowerCase(), t);

            String abbr = abbrFromName(name);
            if (!abbr.isBlank()) teamByCode.putIfAbsent(abbr.toLowerCase(), t);

            if (extId != null && !extId.isBlank())
                teamByExt.put(extId, t);

            if (code != null && isNumeric(code))
                teamByExt.put(code, t);
        }
        System.out.println("→ teams: " + teamByCode.size() + " (by code), " + teamByExt.size() + " (by extId)");
    }

    private void addNumericAliasesRobust() {
        Map<Long, Team> uniqueTeams = new LinkedHashMap<>();
        for (Team t : teamByCode.values()) if (t != null && t.getId() != null) uniqueTeams.putIfAbsent(t.getId(), t);
        List<Team> allTeams = new ArrayList<>(uniqueTeams.values());
        java.util.function.Function<String, Team> byCode = c -> teamByCode.getOrDefault(c.toLowerCase(), null);
        java.util.function.Function<String[], Team> byNameHas = needles -> {
            for (Team t : allTeams) {
                String n = (t.getName()==null?"":t.getName().toLowerCase());
                for (String s : needles) if (n.contains(s)) return t;
            }
            return null;
        };

        putAliasIfMissing("1",  firstNonNull(byCode.apply("lal"), byNameHas.apply(new String[]{"laker", "los angeles laker"})));
        putAliasIfMissing("2",  firstNonNull(byCode.apply("bc"),  byNameHas.apply(new String[]{"boston celtic", "celtic"})));
        putAliasIfMissing("3",  firstNonNull(byCode.apply("gsw"), byNameHas.apply(new String[]{"golden state", "warrior"})));
        putAliasIfMissing("4",  firstNonNull(byCode.apply("mb"),  byNameHas.apply(new String[]{"milwaukee buck"})));
        putAliasIfMissing("5",  firstNonNull(byCode.apply("dn"),  byNameHas.apply(new String[]{"denver nugget"})));
        putAliasIfMissing("6",  firstNonNull(byCode.apply("mh"),  byNameHas.apply(new String[]{"miami heat"})));
        putAliasIfMissing("7",  firstNonNull(byCode.apply("ps"),  byNameHas.apply(new String[]{"phoenix sun"})));
        putAliasIfMissing("8",  firstNonNull(byCode.apply("dm"),  byNameHas.apply(new String[]{"dallas maverick"})));
        putAliasIfMissing("9",  firstNonNull(byCode.apply("nyk"), byNameHas.apply(new String[]{"new york knick"})));
        putAliasIfMissing("10", firstNonNull(byCode.apply("cb"),  byNameHas.apply(new String[]{"chicago bull"})));
    }

    private static <T> T firstNonNull(T a, T b) { return a != null ? a : b; }
    private void putAliasIfMissing(String key, Team t) { if (t!=null) { teamByExt.putIfAbsent(key, t); teamByCode.putIfAbsent(key, t); } }

    /* ================= PLAYERS =================
       players.csv: (#) id,first,last,position,jersey,height,weight,teamRef
       teamRef = numerički extId (1..10) ili code ("lal", ...).
     */
    private void loadPlayers() throws Exception {
        List<String[]> rows = readCsv("seed/players.csv", true);
        for (String[] r : rows) {
            String pcode   = r[0]; // npr. "101" ili custom code
            String first   = r[1];
            String last    = r[2];
            Position pos   = Position.valueOf(r[3]);
            Integer jersey = parseInt(r[4]);
            Integer height = parseInt(r[5]);
            Integer weight = parseInt(r[6]);
            String teamRef = r[7];

            Team team = resolveTeam(teamRef);

            Player p = new Player();
            p.setFirstName(first);
            p.setLastName(last);
            p.setPosition(pos);
            p.setJerseyNumber(jersey);
            p.setHeightCm(height);
            p.setWeightKg(weight);
            p.setTeam(team);

            p = playerRepo.save(p);

            // ---- KLJUČEVI U MAPI ----
            String pcodeKey      = (pcode == null ? "" : pcode.trim().toLowerCase()); // "101"
            String nameUnderscore= (first + "_" + last).toLowerCase();                // "lebron_james"
            String nameSpace     = (first + " " + last).toLowerCase();                // "lebron james"

            if (!pcodeKey.isEmpty()) playerByCode.put(pcodeKey, p);
            playerByCode.putIfAbsent(nameUnderscore, p);
            playerByCode.putIfAbsent(nameSpace, p);
        }
        System.out.println("→ players: " + playerByCode.size());
    }

    /* ================= GAMES =================
       Podržani formati:
       A) id,dateTime,season,homeRef,awayRef,homeScore,awayScore
       B) season,dateTime,homeRef,awayRef,homeScore,awayScore
       C) id,dateTime,homeRef,awayRef,homeScore,awayScore,season
     */
    private void loadGames() throws Exception {
        List<String[]> rows = readCsv("seed/games.csv", true);
        int count = 0;

        for (String[] r : rows) {
            if (r.length < 6) throw new IllegalStateException("games.csv: premalo kolona: " + Arrays.toString(r));

            boolean hasIdFirst = (r.length >= 7 && isNumeric(r[0]));
            boolean looksLikeSeasonAt2 = (r.length >= 3 && r[2] != null && r[2].contains("/"));
            boolean looksLikeSeasonAt6 = (r.length >= 7 && r[6] != null && r[6].contains("/"));

            Integer csvIndex = null;
            String season;
            LocalDateTime dt;
            String homeRef;
            String awayRef;
            Integer homeScore;
            Integer awayScore;

            if (hasIdFirst && looksLikeSeasonAt2) {
                // A) id,dateTime,season,home,away,homeScore,awayScore
                csvIndex  = parseInt(r[0]);
                dt        = parseDateTimeFlexible(r[1]);
                season    = r[2];
                homeRef   = r[3];
                awayRef   = r[4];
                homeScore = parseInt(r[5]);
                awayScore = parseInt(r[6]);
            } else if (!hasIdFirst) {
                // B) season,dateTime,home,away,homeScore,awayScore
                season    = r[0];
                dt        = parseDateTimeFlexible(r[1]);
                homeRef   = r[2];
                awayRef   = r[3];
                homeScore = parseInt(r[4]);
                awayScore = parseInt(r[5]);
            } else if (hasIdFirst && looksLikeSeasonAt6) {
                // C) id,dateTime,home,away,homeScore,awayScore,season
                csvIndex  = parseInt(r[0]);
                dt        = parseDateTimeFlexible(r[1]);
                homeRef   = r[2];
                awayRef   = r[3];
                homeScore = parseInt(r[4]);
                awayScore = parseInt(r[5]);
                season    = r[6];
            } else {
                throw new IllegalStateException("games.csv: neprepoznat format reda: " + Arrays.toString(r));
            }

            if (homeScore == null || awayScore == null) {
                throw new IllegalStateException("games.csv: home/away score prazni: " + Arrays.toString(r));
            }

            Team home = resolveTeam(homeRef);
            Team away = resolveTeam(awayRef);

            Game g = new Game();
            g.setSeason(season);
            g.setDateTime(dt);
            g.setHomeTeam(home);
            g.setAwayTeam(away);
            g.setHomeScore(homeScore);
            g.setAwayScore(awayScore);

            g = gameRepo.save(g);
            count++;

            int indexKey = (csvIndex != null) ? csvIndex : (count - 1); // 0-based ako nema id
            gameIndexMap.put(indexKey, g.getId());
        }
        System.out.println("→ games: " + count);
    }

    /* ================= BOX SCORES =================
       box_scores.csv: (tvoj) id_utakmice, player_id/oznaka, PTS, REB, AST, ...
       Pošto u games.csv koristimo id=201.., ovde prvi stubac = 201.. mapira se preko gameIndexMap.
     */
    private void loadBoxScores() throws Exception {
        List<String[]> rows = readCsv("seed/box_scores.csv", true);
        int ok = 0;

        for (String[] r : rows) {
            if (r.length < 15)
                throw new IllegalStateException("box_scores.csv: premalo kolona: " + Arrays.toString(r));

            int gameIdx = parseInt(r[0]);
            String pref = r[1];

            Long gameId = mustGetGameIdByIndex(gameIdx);
            Game g = gameRepo.getReferenceById(gameId);
            Player p = resolvePlayer(pref);

            // Parsiraj sve kao int (dozvoli null u slučaju praznih polja)
            Integer c2  = parseInt(r[2]);   // PTS
            Integer c3  = parseInt(r[3]);
            Integer c4  = parseInt(r[4]);
            Integer c5  = parseInt(r[5]);
            Integer c6  = parseInt(r[6]);
            Integer c7  = parseInt(r[7]);
            Integer c8  = parseInt(r[8]);
            Integer c9  = parseInt(r[9]);
            Integer c10 = parseInt(r[10]);
            Integer c11 = parseInt(r[11]);
            Integer c12 = parseInt(r[12]);
            Integer c13 = parseInt(r[13]);
            Integer c14 = parseInt(r[14]);

            BoxScore b = new BoxScore();
            b.setGame(g);
            b.setPlayer(p);

            // Heuristika: ako je poslednja kolona minute (<=60), koristimo layout:
            //  gameId,playerRef,PTS,FGM,FGA,3PM,3PA,FTM,FTA,REB,AST,STL,BLK,TOV,MIN
            boolean minutesAtEnd = (c14 != null && c14 >= 0 && c14 <= 60);

            if (minutesAtEnd) {
                b.setPts(  nz(c2));
                b.setFgm(  nz(c3));
                b.setFga(  nz(c4));
                b.setTp3m( nz(c5));
                b.setTp3a( nz(c6));
                b.setFtm(  nz(c7));
                b.setFta(  nz(c8));
                b.setReb(  nz(c9));
                b.setAst(  nz(c10));
                b.setStl(  nz(c11));
                b.setBlk(  nz(c12));
                b.setTov(  nz(c13));
                b.setMin(  nz(c14));
            } else {
                // alternativni raspored (ako ti nekad zatreba)
                b.setPts(   nz(c2));
                b.setReb(   nz(c3));
                b.setAst(   nz(c4));
                b.setStl(   nz(c5));
                b.setBlk(   nz(c6));
                b.setTov(   nz(c7));
                b.setMin(   nz(c8));
                b.setFgm(   nz(c9));
                b.setFga(   nz(c10));
                b.setTp3m(  nz(c11));
                b.setTp3a(  nz(c12));
                b.setFtm(   nz(c13));
                b.setFta(   nz(c14));
            }

            boxRepo.save(b);
            ok++;
        }
        System.out.println("→ box_scores: " + ok);
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }

    /* ================= helpers ================= */

    private Team resolveTeam(String ref) {
        if (ref == null || ref.isBlank())
            throw new IllegalStateException("Team ref je prazan u CSV-u.");

        String key = ref.trim();

        Team t = teamByCode.get(key.toLowerCase());
        if (t != null) return t;

        if (isNumeric(key)) {
            t = teamByExt.get(key);
            if (t != null) return t;
        }

        throw new IllegalStateException(
                "Nepoznat team ref: " + ref +
                        " (poznati code: " + teamByCode.keySet() +
                        ", extId: " + teamByExt.keySet() + ")"
        );
    }

    private Player resolvePlayer(String ref) {
        if (ref == null || ref.isBlank())
            throw new IllegalStateException("Player ref je prazan u CSV-u.");

        String raw = ref.trim();
        String keyLower = raw.toLowerCase();

        // 1) mapa učitanih igrača (radi i za "101")
        Player byMap = playerByCode.get(keyLower);
        if (byMap != null) return byMap;

        // 2) "first last" -> "first_last"
        if (keyLower.contains(" ")) {
            Player byUnderscore = playerByCode.get(keyLower.replace(' ', '_'));
            if (byUnderscore != null) return byUnderscore;
        }

        // 3) dozvoli DB id samo sa prefiksom "db:"
        if (keyLower.startsWith("db:")) {
            String onlyNum = keyLower.substring(3).trim();
            if (isNumeric(onlyNum)) {
                long id = Long.parseLong(onlyNum);
                return playerRepo.findById(id)
                        .orElseThrow(() -> new IllegalStateException("Nepoznat player DB id: " + id));
            }
            throw new IllegalStateException("Nevažeći DB id format: " + ref);
        }

        throw new IllegalStateException(
                "Nepoznat player ref: " + ref + " (primeri ključeva: " + previewKeys(playerByCode.keySet(), 12) + ")"
        );
    }

    private static String previewKeys(Set<String> keys, int limit) {
        List<String> sample = new ArrayList<>(keys);
        if (sample.size() > limit) sample = sample.subList(0, limit);
        return sample.toString() + (keys.size() > limit ? " … (ukupno " + keys.size() + ")" : "");
    }

    private Long mustGetGameIdByIndex(int idx) {
        Long id = gameIndexMap.get(idx);
        if (id == null) throw new IllegalStateException("Nepoznat game_index: " + idx);
        return id;
    }

    /* ====== OPTIONAL: helper za generičan unos 5 igrača po timu (PG, SG, SF, PF, C) ====== */

    private Position pickPositionByIndex(int i) {
        // 1->PG, 2->SG, 3->SF, 4->PF, 5->C
        switch (i) {
            case 1: return Position.PG;
            case 2: return Position.SG;
            case 3: return Position.SF;
            case 4: return Position.PF;
            default: return Position.C;
        }
    }

    private void addPlayersToTeam(Team t, String tag) {
        // 5 “generičkih” igrača za demo/leaders
        for (int i = 1; i <= 5; i++) {
            Player p = new Player();
            p.setFirstName("Player");
            p.setLastName(tag + " " + i);
            p.setPosition(pickPositionByIndex(i));
            p.setJerseyNumber(10 + i);
            p.setHeightCm(185 + i * 2);
            p.setWeightKg(85 + i * 3);
            p.setTeam(t);

            p = playerRepo.save(p);

            // registruj ključeve za box-score reference
            String key1 = (p.getFirstName() + "_" + p.getLastName()).toLowerCase();
            String key2 = (p.getFirstName() + " " + p.getLastName()).toLowerCase();
            playerByCode.putIfAbsent(key1, p);
            playerByCode.putIfAbsent(key2, p);
        }
    }
}
