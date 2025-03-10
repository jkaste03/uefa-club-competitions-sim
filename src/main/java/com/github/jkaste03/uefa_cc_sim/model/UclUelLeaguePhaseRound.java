package com.github.jkaste03.uefa_cc_sim.model;

import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.Tournament;

/**
 * Class representing the league phase in the UEFA Conference League.
 * This class handles the league phase rounds where clubs compete in a league
 * format specific to the Conference League.
 */
public class UclUelLeaguePhaseRound extends LeaguePhaseRound {
    // Constant for clubs skipping a round (e.g., UCL Q3 LP to UEL LP)
    private final static String ROUND_CLUBS_SKIP_TO = Tournament.EUROPA_LEAGUE + " "
            + CompetitionData.RoundType.LEAGUE_PHASE;
    private final static int POT_COUNT = 6;

    /**
     * Constructs a ConferenceLeaguePhaseRound with the specified tournament.
     *
     * @param tournament the tournament for which this league phase round is
     *                   initialized.
     */
    public UclUelLeaguePhaseRound(Tournament tournament) {
        super(tournament);
    }

    @Override
    protected void seedCoreLogic() {
        if (clubSlots == null || clubSlots.size() % POT_COUNT != 0) {
            throw new IllegalArgumentException("The number of clubSlots must be even to seed them properly.");
        }

        if (getName().equals(ROUND_CLUBS_SKIP_TO)) {
            updateClubSlotsIfHasOldWinner(); // Only to avoid incorrect printing of clubs that have skipped a round
        }

        for (int i = 0; i < POT_COUNT; i++) {
            pots.add(clubSlots.subList(i * clubSlots.size() / POT_COUNT, (i + 1) * clubSlots.size() / POT_COUNT));
            System.out.println("\n" + getName() + ", pot " + (i + 1) + ":");
            printClubSlotList(pots.get(i));
        }
    }
}