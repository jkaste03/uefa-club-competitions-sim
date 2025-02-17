package com.example.enums;

public final class CompetitionData {

    public enum Tournament {
        CHAMPIONS_LEAGUE,
        EUROPA_LEAGUE,
        CONFERENCE_LEAGUE;
    }

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

    public enum PathType {
        CHAMPIONS_PATH,
        LEAGUE_PATH,
        MAIN_PATH;
    }

    private CompetitionData() {
        // Prevent instantiation
    }
}