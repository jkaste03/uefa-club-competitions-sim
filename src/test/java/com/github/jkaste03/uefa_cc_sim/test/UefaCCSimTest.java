package com.github.jkaste03.uefa_cc_sim.test;

import com.github.jkaste03.uefa_cc_sim.threads.SimulationThread;
import com.github.jkaste03.uefa_cc_sim.UefaCCSim;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.RoundType;
import com.github.jkaste03.uefa_cc_sim.enums.Country;
import com.github.jkaste03.uefa_cc_sim.model.ClubSlot;
import com.github.jkaste03.uefa_cc_sim.model.LeaguePhaseRound;
import com.github.jkaste03.uefa_cc_sim.model.QRound;
import com.github.jkaste03.uefa_cc_sim.model.Round;
import com.github.jkaste03.uefa_cc_sim.model.Rounds;
import com.github.jkaste03.uefa_cc_sim.model.Tie;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class contains unit tests for the UefaCCSim class.
 */
public class UefaCCSimTest {

    /**
     * Tests the draw method by running a simulation multiple times and verifying
     * the legality of the draw results.
     * 
     * <p>
     * This test performs the following steps:
     * </p>
     * <ul>
     * <li>Creates a new instance of {@code Rounds} and sets it in the
     * {@code SimulationThread}.</li>
     * <li>Runs the simulation 3000 times using a deep copy of the {@code Rounds}
     * object to ensure data consistency.</li>
     * <li>For each iteration, it tests all qualifying rounds ({@code QRound}) to
     * ensure no illegal ties or common country conflicts.</li>
     * <li>Tests all league phase rounds ({@code LeaguePhaseRound}) to ensure:
     * <ul>
     * <li>Clubs opponent home/away and pot constrains are met.</li>
     * <li>No illegal ties or common country conflicts.</li>
     * <li>No club meets clubs from the same country more than twice.</li>
     * </ul>
     * </li>
     * </ul>
     */
    @Test
    public void testDrawMethod() {

        // Create a new instance of Rounds
        Rounds rounds = new Rounds();
        SimulationThread.setRounds(rounds);

        Rounds roundsCopy = null;
        for (int i = 0; i < 3000; i++) {
            // Create a deep copy of the rounds object to reuse the same data without
            // interacting with json
            roundsCopy = UefaCCSim.deepCopy(rounds);
            // Run the simulation with the copied rounds object
            roundsCopy.run("threadName");

            // Test all QRounds
            roundsCopy.getRounds().stream()
                    .filter(r -> r instanceof QRound)
                    .forEach(r -> {
                        // Extract the ties from the round
                        List<Tie> ties = r.getTies();
                        // Check that the QRound draw is legal
                        checkNoIllegalTiesOrCommonCountry(r, ties);
                    });

            // Test all LeaguePhaseRounds
            for (Round r : roundsCopy.getRoundsOfType(RoundType.LEAGUE_PHASE)) {
                LeaguePhaseRound round = (LeaguePhaseRound) r;
                // Extract the pots, ties, and club slots from the league phase round
                List<List<ClubSlot>> pots = round.getPots();
                List<Tie> ties = round.getTies();
                List<ClubSlot> clubSlots = round.getClubSlots();

                // Check that the league phase is draw is legal
                checkOpponentPotHomeAway(pots, clubSlots, ties);
                checkNoIllegalTiesOrCommonCountry(round, ties);
                checkNoClubMeetsCountryMoreThanTwice(clubSlots, ties);
            }
        }
    }

    /**
     * Checks that each club meets exactly one club from each pot at home and one
     * away, with specific rules for 6 pots.
     *
     * @param clubSlots the list of club slots participating in the competition
     * @param ties      the list of ties between the clubs
     * @throws AssertionError if a club does not meet exactly one club from each pot
     *                        at home and one away
     */
    private void checkOpponentPotHomeAway(List<List<ClubSlot>> pots, List<ClubSlot> clubSlots,
            List<Tie> ties) {
        for (ClubSlot clubSlot : clubSlots) {
            // Count the number of home matches against each pot for the current club slot
            Map<Integer, Long> homePotCounts = ties.stream()
                    .filter(tie -> tie.getClubSlot1().equals(clubSlot))
                    .map(tie -> getPotForClubSlot(tie.getClubSlot2(), pots))
                    .collect(Collectors.groupingBy(pot -> pot, Collectors.counting()));

            // Count the number of away matches against each pot for the current club slot
            Map<Integer, Long> awayPotCounts = ties.stream()
                    .filter(tie -> tie.getClubSlot2().equals(clubSlot))
                    .map(tie -> getPotForClubSlot(tie.getClubSlot1(), pots))
                    .collect(Collectors.groupingBy(pot -> pot, Collectors.counting()));

            // If there are 6 pots, check the home and away matches for specific sub-pots
            if (pots.size() == 6) {
                checkPotSubpotHomeAway(homePotCounts, awayPotCounts, clubSlot, 1, 2);
                checkPotSubpotHomeAway(homePotCounts, awayPotCounts, clubSlot, 3, 4);
                checkPotSubpotHomeAway(homePotCounts, awayPotCounts, clubSlot, 5, 6);
            } else {
                // Otherwise, check that the club meets exactly one club from each pot at home
                // and away
                for (int pot = 1; pot <= pots.size(); pot++) {
                    assertTrue(homePotCounts.getOrDefault(pot, 0L) == 1,
                            "ClubSlot " + clubSlot + " does not meet exactly one club from pot " + pot + " at home.");
                    assertTrue(awayPotCounts.getOrDefault(pot, 0L) == 1,
                            "ClubSlot " + clubSlot + " does not meet exactly one club from pot " + pot + " away.");
                }
            }
        }
    }

    /**
     * Ensures a club meets exactly one club from each sub pot at home and one
     * away, and does not meet clubs from the same pot.
     *
     * @param homePotCounts a map containing the counts of home matches against each
     *                      pot
     * @param awayPotCounts a map containing the counts of away matches against each
     *                      pot
     * @param clubSlot      the club slot being checked
     * @param potX          the first pot to check
     * @param potY          the second pot to check
     */
    private void checkPotSubpotHomeAway(Map<Integer, Long> homePotCounts, Map<Integer, Long> awayPotCounts,
            ClubSlot clubSlot,
            int potX, int potY) {
        // Retrieve the home and away match counts against pots 1 and 2
        long homeCountPot1 = homePotCounts.getOrDefault(potX, 0L);
        long homeCountPot2 = homePotCounts.getOrDefault(potY, 0L);
        long awayCountPot1 = awayPotCounts.getOrDefault(potX, 0L);
        long awayCountPot2 = awayPotCounts.getOrDefault(potY, 0L);

        // Check that the club meets exactly one club from pots 1 or 2 at home and one
        // away
        assertTrue(homeCountPot1 + homeCountPot2 == 1,
                "ClubSlot " + clubSlot + " does not meet exactly one club from pots " + potX + " or " + potY
                        + " at home.");
        assertTrue(awayCountPot1 + awayCountPot2 == 1,
                "ClubSlot " + clubSlot + " does not meet exactly one club from pots " + potX + " or " + potY
                        + " away.");

        // Check that the club does not meet clubs from same pot home and away
        assertTrue(homeCountPot1 == 0 || awayCountPot1 == 0,
                "ClubSlot " + clubSlot + " meets clubs from pot " + potX + " both at home and away.");
        assertTrue(homeCountPot2 == 0 || awayCountPot2 == 0,
                "ClubSlot " + clubSlot + " meets clubs from pot " + potY + " both at home and away.");
    }

    /**
     * Helper method to get the pot number for a given club slot.
     *
     * @param clubSlot the club slot to find the pot for
     * @param pots     the list of lists with club slots
     * @return the pot number for the given club slot
     */
    private int getPotForClubSlot(ClubSlot clubSlot, List<List<ClubSlot>> pots) {
        for (int i = 0; i < pots.size(); i++) {
            if (pots.get(i).contains(clubSlot)) {
                return i + 1; // Assuming pot numbers are 1-based
            }
        }
        throw new IllegalArgumentException("ClubSlot " + clubSlot + " not found in any pot.");
    }

    /**
     * Checks that no illegal ties are present.
     *
     * @param clubSlots the list of club slots participating in the competition
     * @param ties      the list of ties between the clubs
     * @throws AssertionError if a club meets a club from its own country or if
     *                        there are illegal ties
     */
    private void checkNoIllegalTiesOrCommonCountry(Round round, List<Tie> ties) {
        ties.forEach(tie -> {
            ClubSlot clubSlot1 = tie.getClubSlot1();
            ClubSlot clubSlot2 = tie.getClubSlot2();
            assertTrue(!round.isIllegalTie(clubSlot1, clubSlot2),
                    "Illegal tie detected between " + clubSlot1 + " and " + clubSlot2);
        });
    }

    /**
     * Checks that no club meets more than two clubs from any specific country.
     *
     * @param clubSlots the list of club slots participating in the competition
     * @param ties      the list of ties between the clubs
     * @throws AssertionError if a club meets more than two clubs from the same
     *                        country
     */
    private void checkNoClubMeetsCountryMoreThanTwice(List<ClubSlot> clubSlots, List<Tie> ties) {
        for (ClubSlot clubSlot : clubSlots) {
            // Create a map to count the number of opponents from each country
            Map<Country, Long> opponentCountryCounts = ties.stream()
                    // Filter ties to include only those involving the current club slot
                    .filter(tie -> tie.getClubSlot1().equals(clubSlot) || tie.getClubSlot2().equals(clubSlot))
                    // Map each tie to the opponent club slot
                    .map(tie -> tie.getClubSlot1().equals(clubSlot) ? tie.getClubSlot2() : tie.getClubSlot1())
                    // Group the opponents by their country and count the occurrences
                    .collect(Collectors.groupingBy(opp -> opp.getCountries().get(0), Collectors.counting()));

            // Check that no club meets more than two clubs from any specific country
            for (Map.Entry<Country, Long> entry : opponentCountryCounts.entrySet()) {
                assertTrue(entry.getValue() <= 2,
                        "ClubSlot " + clubSlot + " meets more than 2 clubs from " + entry.getKey() + ". Ties: " + ties);
            }
        }
    }
}