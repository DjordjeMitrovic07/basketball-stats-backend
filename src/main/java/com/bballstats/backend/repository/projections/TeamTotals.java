package com.bballstats.backend.repository.projections;

public interface TeamTotals {
    Long getTeamId();
    String getTeamName();

    Long getPts();
    Long getFga();
    Long getFgm();
    Long getTp3a();
    Long getTp3m();
    Long getFta();
    Long getAst();
    Long getReb();
    Long getGames();
}
