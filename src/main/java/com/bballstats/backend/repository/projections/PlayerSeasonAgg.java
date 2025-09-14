package com.bballstats.backend.repository.projections;

public interface PlayerSeasonAgg {
    Long getPlayerId();
    String getPlayerFirstName();
    String getPlayerLastName();
    Long getTeamId();
    String getTeamName();
    String getTeamAbbr();

    Long getGames();

    Long getPts();
    Long getReb();
    Long getAst();
    Long getStl();
    Long getBlk();
    Long getTov();
    Long getMin();

    Long getFgm();
    Long getFga();
    Long getTp3m();
    Long getTp3a();
    Long getFtm();
    Long getFta();
}
