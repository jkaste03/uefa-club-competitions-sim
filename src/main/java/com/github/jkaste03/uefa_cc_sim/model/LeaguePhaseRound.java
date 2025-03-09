package com.github.jkaste03.uefa_cc_sim.model;

import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData;
import com.github.jkaste03.uefa_cc_sim.service.ClubEloDataLoader;

/**
 * Class representing a league phase in the UEFA competitions.
 * This class handles the league phase rounds where clubs compete in a league
 * format.
 */
public class LeaguePhaseRound extends Round {

    /**
     * Constructs a LeaguePhaseRound with the specified tournament.
     *
     * @param tournament the tournament for which this league phase round is
     *                   initialized.
     */
    public LeaguePhaseRound(CompetitionData.Tournament tournament) {
        super(tournament, CompetitionData.RoundType.LEAGUE_PHASE);
    }

    /**
     * Returns a string representation of the qualifying round, including the
     * tournament and round type.
     */
    @Override
    public String getName() {
        return super.getName() + " " + CompetitionData.RoundType.LEAGUE_PHASE;
    }

    /**
     * Plays the league phase round.
     * This method is currently not implemented and will throw an
     * UnsupportedOperationException.
     *
     * @param clubEloDataLoader the data loader for club Elo ratings.
     */
    @Override
    public void play(ClubEloDataLoader clubEloDataLoader) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'play'");
        // Todo: Update the clubEloDataLoader with the new Elo ratings after the matches
    }
}