package com.bballstats.backend.dto;

import com.bballstats.backend.entity.BoxScore;

public class BoxScoreDto {
    private Long id;
    private Long gameId;

    private Long playerId;
    private String playerName;

    // >>> dodatak za frontend (skraćenice i prikaz tima)
    private Long teamId;
    private String teamName;

    private Integer pts, fgm, fga, tp3m, tp3a, ftm, fta, reb, ast, stl, blk, tov, min;

    // metrika
    private Double efg; // effective FG%
    private Double ts;  // True Shooting %

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public Integer getPts() { return pts; }
    public void setPts(Integer pts) { this.pts = pts; }

    public Integer getFgm() { return fgm; }
    public void setFgm(Integer fgm) { this.fgm = fgm; }

    public Integer getFga() { return fga; }
    public void setFga(Integer fga) { this.fga = fga; }

    public Integer getTp3m() { return tp3m; }
    public void setTp3m(Integer tp3m) { this.tp3m = tp3m; }

    public Integer getTp3a() { return tp3a; }
    public void setTp3a(Integer tp3a) { this.tp3a = tp3a; }

    public Integer getFtm() { return ftm; }
    public void setFtm(Integer ftm) { this.ftm = ftm; }

    public Integer getFta() { return fta; }
    public void setFta(Integer fta) { this.fta = fta; }

    public Integer getReb() { return reb; }
    public void setReb(Integer reb) { this.reb = reb; }

    public Integer getAst() { return ast; }
    public void setAst(Integer ast) { this.ast = ast; }

    public Integer getStl() { return stl; }
    public void setStl(Integer stl) { this.stl = stl; }

    public Integer getBlk() { return blk; }
    public void setBlk(Integer blk) { this.blk = blk; }

    public Integer getTov() { return tov; }
    public void setTov(Integer tov) { this.tov = tov; }

    public Integer getMin() { return min; }
    public void setMin(Integer min) { this.min = min; }

    public Double getEfg() { return efg; }
    public void setEfg(Double efg) { this.efg = efg; }

    public Double getTs() { return ts; }
    public void setTs(Double ts) { this.ts = ts; }

    public static BoxScoreDto from(BoxScore b, double efg, double ts) {
        BoxScoreDto d = new BoxScoreDto();
        d.setId(b.getId());
        d.setGameId(b.getGame().getId());

        // player
        if (b.getPlayer() != null) {
            d.setPlayerId(b.getPlayer().getId());
            String fn = b.getPlayer().getFirstName() != null ? b.getPlayer().getFirstName() : "";
            String ln = b.getPlayer().getLastName()  != null ? b.getPlayer().getLastName()  : "";
            String full = (fn + " " + ln).trim();
            d.setPlayerName(full.isEmpty() ? null : full);

            // >>> tim kroz player.team (ne menjamo šemu BoxScore)
            if (b.getPlayer().getTeam() != null) {
                d.setTeamId(b.getPlayer().getTeam().getId());
                d.setTeamName(b.getPlayer().getTeam().getName());
            }
        }

        d.setPts(b.getPts());
        d.setFgm(b.getFgm()); d.setFga(b.getFga());
        d.setTp3m(b.getTp3m()); d.setTp3a(b.getTp3a());
        d.setFtm(b.getFtm());   d.setFta(b.getFta());
        d.setReb(b.getReb());   d.setAst(b.getAst());
        d.setStl(b.getStl());   d.setBlk(b.getBlk());
        d.setTov(b.getTov());   d.setMin(b.getMin());

        d.setEfg(Double.isNaN(efg) ? null : efg);
        d.setTs(Double.isNaN(ts)  ? null : ts);
        return d;
    }
}
