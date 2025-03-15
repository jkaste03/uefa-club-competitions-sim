package com.github.jkaste03.uefa_cc_sim.model;

import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.Tournament;

/**
 * Class representing the league phase in the UEFA Conference League.
 * This class handles the league phase rounds where clubs compete in a league
 * format specific to the Conference League.
 */
public class UeclLeaguePhaseRound extends LeaguePhaseRound {
    private final static int POT_COUNT = 6;

    /**
     * Constructs a ConferenceLeaguePhaseRound with the specified tournament.
     *
     * @param tournament the tournament for which this league phase round is
     *                   initialized.
     */
    public UeclLeaguePhaseRound() {
        super(Tournament.CONFERENCE_LEAGUE);
    }

    @Override
    protected void seed() {
        if (clubSlots == null || clubSlots.size() % POT_COUNT != 0) {
            throw new IllegalArgumentException("The number of clubSlots must be even to seed them properly.");
        }

        clubSlots.sort((c1, c2) -> Float.compare(c1.getRanking(), c2.getRanking()));

        for (int i = 0; i < POT_COUNT; i++) {
            pots.add(clubSlots.subList(i * clubSlots.size() / POT_COUNT, (i + 1) * clubSlots.size() / POT_COUNT));
            System.out.println("\n" + getName() + ", pot " + (i + 1) + ":");
            printClubSlotList(pots.get(i));
        }
    }
}