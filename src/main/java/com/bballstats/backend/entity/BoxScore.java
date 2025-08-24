package com.bballstats.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "box_scores",
        uniqueConstraints = @UniqueConstraint(name="uk_boxscore_game_player", columnNames = {"game_id","player_id"}))
public class BoxScore {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_boxscore_game"))
    private Game game;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_boxscore_player"))
    private Player player;

    @Min(0) private Integer pts = 0;

    @Min(0) private Integer fgm = 0;
    @Min(0) private Integer fga = 0;

    @Column(name="tp3m") @Min(0) private Integer tp3m = 0;
    @Column(name="tp3a") @Min(0) private Integer tp3a = 0;

    @Min(0) private Integer ftm = 0;
    @Min(0) private Integer fta = 0;

    @Min(0) private Integer reb = 0;
    @Min(0) private Integer ast = 0;
    @Min(0) private Integer stl = 0;
    @Min(0) private Integer blk = 0;
    @Min(0) private Integer tov = 0;

    @Min(0) private Integer min = 0;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
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
}
