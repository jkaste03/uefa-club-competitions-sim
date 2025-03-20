package com.github.jkaste03.uefa_cc_sim.test;

import com.github.jkaste03.uefa_cc_sim.model.UclUelLeaguePhaseRound;
import com.github.jkaste03.uefa_cc_sim.threads.SimulationThread;
import com.github.jkaste03.uefa_cc_sim.UefaCCSim;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.RoundType;
import com.github.jkaste03.uefa_cc_sim.enums.Country;
import com.github.jkaste03.uefa_cc_sim.model.ClubSlot;
import com.github.jkaste03.uefa_cc_sim.model.LeaguePhaseRound;
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
     * Tests the draw method of the UefaCCSim class.
     * 
     * The test performs the following steps:
     * 1. Creates a new instance of Rounds and sets it in the SimulationThread.
     * 2. Runs the simulation 30,000 times using a deep copy of the rounds object.
     * 3. Finds the UclUelLeaguePhaseRound instance from the rounds.
     * 4. Ensures the UclUelLeaguePhaseRound instance is not null.
     * 5. Checks that each ClubSlot has exactly 4 home matches and 4 away matches.
     * 6. Verifies that no clubs meet a club from their own country.
     * 7. Ensures that no club meets more than two clubs from any specific country.
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

            // Find the UclUelLeaguePhaseRound instance
            UclUelLeaguePhaseRound leaguePhaseRound = null;
            for (Round round : roundsCopy.getRoundsOfType(RoundType.LEAGUE_PHASE)) {
                if (round instanceof UclUelLeaguePhaseRound) {

                    leaguePhaseRound = (UclUelLeaguePhaseRound) round;

                    // Check the ties variable
                    List<List<ClubSlot>> pots = leaguePhaseRound.getPots();
                    List<Tie> ties = leaguePhaseRound.getTies();
                    List<ClubSlot> clubSlots = leaguePhaseRound.getClubSlots();

                    // Check that the league phase is draw is legal
                    checkOneClubFromEachPotHomeAndAway(pots, clubSlots, ties);
                    checkNoIllegalTiesOrCommonCountry(leaguePhaseRound, clubSlots, ties);
                    checkNoClubMeetsCountryMoreThanTwice(clubSlots, ties);
                }
            }
        }
    }

    /**
     * Checks that each club meets exactly one club from each pot at home and one
     * away.
     *
     * @param clubSlots the list of club slots participating in the competition
     * @param ties      the list of ties between the clubs
     * @throws AssertionError if a club does not meet exactly one club from each pot
     *                        at home and one away
     */
    private void checkOneClubFromEachPotHomeAndAway(List<List<ClubSlot>> pots, List<ClubSlot> clubSlots,
            List<Tie> ties) {
        for (ClubSlot clubSlot : clubSlots) {
            Map<Integer, Long> homePotCounts = ties.stream()
                    .filter(tie -> tie.getClubSlot1().equals(clubSlot))
                    .map(tie -> getPotForClubSlot(tie.getClubSlot2(), pots))
                    .collect(Collectors.groupingBy(pot -> pot, Collectors.counting()));

            Map<Integer, Long> awayPotCounts = ties.stream()
                    .filter(tie -> tie.getClubSlot2().equals(clubSlot))
                    .map(tie -> getPotForClubSlot(tie.getClubSlot1(), pots))
                    .collect(Collectors.groupingBy(pot -> pot, Collectors.counting()));

            for (int pot = 1; pot <= 4; pot++) {
                assertTrue(homePotCounts.getOrDefault(pot, 0L) == 1,
                        "ClubSlot " + clubSlot + " does not meet exactly one club from pot " + pot + " at home. Ties: "
                                + ties);
                assertTrue(awayPotCounts.getOrDefault(pot, 0L) == 1,
                        "ClubSlot " + clubSlot + " does not meet exactly one club from pot " + pot + " away. Ties: "
                                + ties);
            }
        }
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
    private void checkNoIllegalTiesOrCommonCountry(LeaguePhaseRound round, List<ClubSlot> clubSlots,
            List<Tie> ties) {
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