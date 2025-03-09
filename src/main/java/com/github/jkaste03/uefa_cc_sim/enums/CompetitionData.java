package com.github.jkaste03.uefa_cc_sim.enums;

/**
 * Final class containing enums for various competition data used in UEFA
 * simulations.
 */
public final class CompetitionData {

    /**
     * Enum representing different tournaments in UEFA competitions.
     */
    public enum Tournament {
        CHAMPIONS_LEAGUE,
        EUROPA_LEAGUE,
        CONFERENCE_LEAGUE;
    }

    /**
     * Enum representing different round types in UEFA competitions.
     */
    public enum RoundType {
        Q1,
        Q2,
        Q3,
        PLAYOFF,
        LEAGUE_PHASE,
        KO_ROUND_PLAYOFF,
        ROUND_OF_16,
        QUARTER_FINAL,
        SEMI_FINAL,
        FINAL;
    }

    /**
     * Enum representing different path types in UEFA competitions.
     */
    public enum PathType {
        CHAMPIONS_PATH,
        LEAGUE_PATH,
        MAIN_PATH;
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private CompetitionData() {
    }
}