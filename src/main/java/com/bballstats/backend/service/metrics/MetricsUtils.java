package com.bballstats.backend.service.metrics;

public final class MetricsUtils {
    private MetricsUtils(){}

    public static double safeDiv(double num, double den){
        if (den == 0.0) return 0.0;
        return num / den;
    }

    // eFG% = (FGM + 0.5 * 3PM) / FGA
    public static double efg(int fgm, int tpm, int fga){
        return safeDiv(fgm + 0.5 * tpm, fga);
    }

    // TS% = PTS / (2 * (FGA + 0.44 * FTA))
    public static double ts(int pts, int fga, int fta){
        return safeDiv(pts, 2.0 * (fga + 0.44 * fta));
    }

    // USG% (box-score varijanta)
    public static double usg(int fga, int fta, int tov,
                             int mp, int teamFga, int teamFta, int teamTov, int teamMinutes){
        double num = (fga + 0.44 * fta + tov) * (safeDiv(teamMinutes, 5.0));
        double den = mp * (teamFga + 0.44 * teamFta + teamTov);
        return 100.0 * safeDiv(num, den);
    }

    // Posedi (aproksimacija BEZ ORB/DRB korekcije jer ih nemamo u entitetu):
    // poss ≈ FGA + 0.4 * FTA - FGM + TOV
    public static double possessionsSimple(int fga, int fgm, int fta, int tov){
        return fga + 0.4 * fta - fgm + tov;
    }

    // Pace = PERIOD_LENGTH * (TeamPoss + OppPoss) / (2 * TeamMinutes)
    public static double pace(double teamPoss, double oppPoss, int teamMinutes, double periodLength){
        return safeDiv(periodLength * (teamPoss + oppPoss), 2.0 * teamMinutes);
    }

    public static double ortg(int points, double teamPoss){
        return 100.0 * safeDiv(points, teamPoss);
    }
    public static double drtg(int oppPoints, double teamPoss){
        return 100.0 * safeDiv(oppPoints, teamPoss);
    }

    public static double round1(double v){ return Math.round(v * 10.0) / 10.0; }
    public static double round2(double v){ return Math.round(v * 100.0) / 100.0; }
    public static double round3(double v){ return Math.round(v * 1000.0) / 1000.0; }
}
