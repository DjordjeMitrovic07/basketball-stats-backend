package com.bballstats.backend.repository.projections;

public interface PlayerTotals {
    Long getPlayerId();
    String getPlayerName();
    String getTeamAbbr();
    Long getPts();
    Long getFga();
    Long getFgm();
    Long getTp3a();
    Long getTp3m();
    Long getFta();
    Long getAst();
    Long getReb();
    Long getGames();
    Double getAvgMpg();
}

