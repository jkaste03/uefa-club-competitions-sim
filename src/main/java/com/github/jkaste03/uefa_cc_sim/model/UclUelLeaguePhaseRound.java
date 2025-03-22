package com.github.jkaste03.uefa_cc_sim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.Tournament;
import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * Class representing the league phase in the Champions League and Europa
 * League. This class handles the league phase rounds where clubs compete in a
 * league format specific to those competitions.
 */
public class UclUelLeaguePhaseRound extends LeaguePhaseRound {
    // Constant for clubs skipping a round (e.g., UCL Q3 LP to UEL LP)
    private final static String ROUND_CLUBS_SKIP_TO = Tournament.EUROPA_LEAGUE + " "
            + CompetitionData.RoundType.LEAGUE_PHASE;
    private final static int POT_COUNT = 4;

    /**
     * Constructs a ConferenceLeaguePhaseRound with the specified tournament.
     *
     * @param tournament the tournament for which this league phase round is
     *                   initialized.
     */
    public UclUelLeaguePhaseRound(Tournament tournament) {
        super(tournament);
    }

    /**
     * Seeds the club slots into pots for the league phase.
     * 
     * <p>
     * This method performs the following steps:
     * </p>
     * <ol>
     * <li>Ensures the number of club slots is divisible by {@code POT_COUNT}. If
     * not, throws an {@link IllegalStateException}.</li>
     * <li>If the current round is the one that clubs have skipped to, fixes the
     * club slot for those clubs to prevent them from being wrapped in
     * {@code DoubleLeggedTieWrapper}.</li>
     * <li>Sorts the club slots.</li>
     * <li>Divides the club slots into pots for the league phase and prints each
     * pot.</li>
     * </ol>
     * 
     * @throws IllegalStateException if the number of club slots is not divisible
     *                               by {@code POT_COUNT}.
     */
    @Override
    protected void seed() {
        // Ensure the number of clubSlots is divisible by POT_COUNT.
        if (clubSlots == null || clubSlots.size() % POT_COUNT != 0) {
            throw new IllegalStateException("ClubSlot count must be divisible by " + POT_COUNT + " to seed properly.");
        }

        // If round that clubs has skipped QRound to, fix club slot for those clubs.
        // This applies to Europa League as clubs skip from UCL Q3 LP to UEL LP.
        if (getName().equals(ROUND_CLUBS_SKIP_TO)) {
            updateClubSlotsIfClubHasSkipped(false); // Prevent skipped clubs from being DoubleLeggedTieWrapper
        }

        sortClubSlots();

        // Divide the club slots into pots for the league phase.
        for (int i = 0; i < POT_COUNT; i++) {
            pots.add(clubSlots.subList(i * clubSlots.size() / POT_COUNT, (i + 1) * clubSlots.size() / POT_COUNT));
            // System.out.println("\n" + getName() + ", pot " + (i + 1) + ":");
            printClubSlotList(pots.get(i));
        }
    }

    /**
     * Sorts the club slots for the league phase round.
     * <p>
     * If the tournament is the Champions League, this method checks if the last UCL
     * winner is present in the club slots.
     * If the UCL winner is found, it is moved to the top of the list.
     * <p>
     * After handling the UCL winner, the remaining club slots are sorted based on
     * their ranking.
     * The UCL winner, if present, remains at the top of the list.
     */
    private void sortClubSlots() {
        final boolean[] isUclWinnerHere = { false }; // Array to hold the state of UCL winner presence. This is an array
                                                     // to allow modification inside the lambda below.
        // Check if the UCL winner is present in the club slots and move them to the top
        if (tournament == Tournament.CHAMPIONS_LEAGUE) {
            clubSlots.stream()
                    .filter(c -> c.getName().equals(ClubRepository.getLastUclWinnerName()))
                    .findFirst()
                    .ifPresent(c -> {
                        Collections.swap(clubSlots, 0, clubSlots.indexOf(c));
                        isUclWinnerHere[0] = true;
                    });
        }

        // Sort the club slots based on their ranking. Leave the UCL winner at the top
        // if present.
        clubSlots.subList(isUclWinnerHere[0] ? 1 : 0, clubSlots.size())
                .sort((c1, c2) -> Float.compare(c1.getRanking(), c2.getRanking()));
    }

    @Override
    protected void draw() {
        final int MAX_ATTEMPTS = 1000000;
        boolean success = false;
        List<Tie> solution = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS && !success; attempt++) {
            // --- Forberedelser: Mapping fra klubb til pot, samlet liste, krav, osv. ---
            Map<ClubSlot, Integer> clubToPot = new HashMap<>();
            List<ClubSlot> allClubs = new ArrayList<>();
            for (int i = 0; i < pots.size(); i++) {
                for (ClubSlot club : pots.get(i)) {
                    clubToPot.put(club, i);
                    allClubs.add(club);
                }
            }

            // Opprett krav: For hver klubb og hver pot skal det være 1 hjemmekamp og 1
            // bortekamp.
            Map<ClubSlot, int[][]> requirements = new HashMap<>();
            for (ClubSlot club : allClubs) {
                int[][] req = new int[POT_COUNT][2];
                for (int p = 0; p < POT_COUNT; p++) {
                    req[p][0] = 1; // Hjemmekamp-krav mot pot p
                    req[p][1] = 1; // Bortekamp-krav mot pot p
                }
                requirements.put(club, req);
            }

            // Holder oversikt over tildelte oppgjør for å unngå duplikater.
            Map<ClubSlot, Set<ClubSlot>> assignedOpponents = new HashMap<>();
            for (ClubSlot club : allClubs) {
                assignedOpponents.put(club, new HashSet<>());
            }

            // Teller for antall oppgjør mot utenlandske land per klubb.
            Map<ClubSlot, Map<Country, Integer>> countryCounters = new HashMap<>();
            for (ClubSlot club : allClubs) {
                countryCounters.put(club, new HashMap<>());
            }

            // Instansier hjelperen for landssjekk.
            Helper helper = new Helper(countryCounters);

            // Liste for å lagre de endelige oppgjørene.
            List<Tie> finalTies = new ArrayList<>();

            // --- Bygg inter-pot "slots" ---
            List<InterSlot> interSlots = new ArrayList<>();
            for (int i = 0; i < POT_COUNT; i++) {
                for (int j = i + 1; j < POT_COUNT; j++) {
                    for (ClubSlot clubA : pots.get(i)) {
                        interSlots.add(new InterSlot(clubA, j));
                    }
                }
            }

            // Løs inter-pot oppgjør med backtracking.
            boolean interSolved = solveInter(0, interSlots, requirements, assignedOpponents, clubToPot, pots, helper,
                    finalTies);
            // Løs intra-pot oppgjør for hver pot.
            boolean intraSolved = interSolved
                    && solveIntra(0, requirements, assignedOpponents, helper, finalTies, pots);

            // Verifiser at alle krav er oppfylt.
            boolean allMet = true;
            if (intraSolved) {
                for (ClubSlot club : allClubs) {
                    int[][] req = requirements.get(club);
                    for (int p = 0; p < POT_COUNT; p++) {
                        if (req[p][0] != 0 || req[p][1] != 0) {
                            allMet = false;
                            break;
                        }
                    }
                    if (!allMet)
                        break;
                }
            }

            if (interSolved && intraSolved && allMet) {
                success = true;
                solution = new ArrayList<>(finalTies);
            }
        }

        if (!success) {
            throw new RuntimeException(
                    "Kunne ikke fullføre trekningen uten deadlock etter " + MAX_ATTEMPTS + " forsøk.");
        }

        ties = solution;
    }

    /* ---------- Private hjelpeklasser og metoder ---------- */

    /**
     * En indre klasse som representerer et inter-pot oppgjørslot.
     */
    private class InterSlot {
        ClubSlot clubA; // Klubben fra pot i
        int targetPot; // Målpotten (pot j)

        public InterSlot(ClubSlot clubA, int targetPot) {
            this.clubA = clubA;
            this.targetPot = targetPot;
        }
    }

    /**
     * Hjelpeklasse for å sjekke og oppdatere tellere for utenlandske oppgjør.
     */
    private class Helper {
        private Map<ClubSlot, Map<Country, Integer>> countryCounters;

        public Helper(Map<ClubSlot, Map<Country, Integer>> countryCounters) {
            this.countryCounters = countryCounters;
        }

        public boolean canAddOpponent(ClubSlot club, ClubSlot opponent) {
            for (Country oppCountry : opponent.getCountries()) {
                if (!club.getCountries().contains(oppCountry)) {
                    int count = countryCounters.get(club).getOrDefault(oppCountry, 0);
                    if (count >= 2) {
                        return false;
                    }
                }
            }
            return true;
        }

        public void updateCountryCounters(ClubSlot club, ClubSlot opponent) {
            for (Country oppCountry : opponent.getCountries()) {
                if (!club.getCountries().contains(oppCountry)) {
                    int count = countryCounters.get(club).getOrDefault(oppCountry, 0);
                    countryCounters.get(club).put(oppCountry, count + 1);
                }
            }
        }

        public void revertCountryCounters(ClubSlot club, ClubSlot opponent) {
            for (Country oppCountry : opponent.getCountries()) {
                if (!club.getCountries().contains(oppCountry)) {
                    int count = countryCounters.get(club).get(oppCountry);
                    if (count == 1) {
                        countryCounters.get(club).remove(oppCountry);
                    } else {
                        countryCounters.get(club).put(oppCountry, count - 1);
                    }
                }
            }
        }
    }

    /**
     * Løser inter-pot oppgjør med backtracking.
     */
    private boolean solveInter(int pos,
            List<InterSlot> interSlots,
            Map<ClubSlot, int[][]> requirements,
            Map<ClubSlot, Set<ClubSlot>> assignedOpponents,
            Map<ClubSlot, Integer> clubToPot,
            List<List<ClubSlot>> pots,
            Helper helper,
            List<Tie> finalTies) {
        if (pos >= interSlots.size()) {
            return true;
        }
        InterSlot slot = interSlots.get(pos);
        ClubSlot clubA = slot.clubA;
        int potJ = slot.targetPot;
        int[][] reqA = requirements.get(clubA);

        // Dersom kravene mot potJ er oppfylt for clubA, gå videre.
        if (reqA[potJ][0] + reqA[potJ][1] <= 0) {
            return solveInter(pos + 1, interSlots, requirements, assignedOpponents, clubToPot, pots, helper, finalTies);
        }

        int potA = clubToPot.get(clubA);
        for (ClubSlot clubB : pots.get(potJ)) {
            // Unngå duplikate oppgjør.
            if (assignedOpponents.get(clubA).contains(clubB))
                continue;

            int[][] reqB = requirements.get(clubB);
            if (reqB[potA][0] + reqB[potA][1] <= 0)
                continue;
            if (isIllegalTie(clubA, clubB))
                continue;
            if (!helper.canAddOpponent(clubA, clubB))
                continue;
            if (!helper.canAddOpponent(clubB, clubA))
                continue;

            boolean option1 = reqA[potJ][0] > 0 && reqB[potA][1] > 0;
            boolean option2 = reqA[potJ][1] > 0 && reqB[potA][0] > 0;
            if (!option1 && !option2)
                continue;

            // Prøv Option 1: clubA spilles hjemme, clubB borte.
            if (option1) {
                reqA[potJ][0]--;
                reqB[potA][1]--;
                assignedOpponents.get(clubA).add(clubB);
                assignedOpponents.get(clubB).add(clubA);
                helper.updateCountryCounters(clubA, clubB);
                helper.updateCountryCounters(clubB, clubA);
                finalTies.add(new SingleLeggedTie(clubA, clubB)); // clubA hjemme, clubB borte

                if (solveInter(pos + 1, interSlots, requirements, assignedOpponents, clubToPot, pots, helper,
                        finalTies)) {
                    return true;
                }
                // Backtrack
                finalTies.remove(finalTies.size() - 1);
                helper.revertCountryCounters(clubA, clubB);
                helper.revertCountryCounters(clubB, clubA);
                assignedOpponents.get(clubA).remove(clubB);
                assignedOpponents.get(clubB).remove(clubA);
                reqA[potJ][0]++;
                reqB[potA][1]++;
            }

            // Prøv Option 2: clubB spilles hjemme, clubA borte.
            if (option2) {
                reqA[potJ][1]--;
                reqB[potA][0]--;
                assignedOpponents.get(clubA).add(clubB);
                assignedOpponents.get(clubB).add(clubA);
                helper.updateCountryCounters(clubA, clubB);
                helper.updateCountryCounters(clubB, clubA);
                finalTies.add(new SingleLeggedTie(clubB, clubA)); // clubB hjemme, clubA borte

                if (solveInter(pos + 1, interSlots, requirements, assignedOpponents, clubToPot, pots, helper,
                        finalTies)) {
                    return true;
                }
                // Backtrack
                finalTies.remove(finalTies.size() - 1);
                helper.revertCountryCounters(clubA, clubB);
                helper.revertCountryCounters(clubB, clubA);
                assignedOpponents.get(clubA).remove(clubB);
                assignedOpponents.get(clubB).remove(clubA);
                reqA[potJ][1]++;
                reqB[potA][0]++;
            }
        }
        return false;
    }

    /**
     * Starter den rekursive løsningen for intra-pot oppgjør over alle potter.
     */
    private boolean solveIntra(int potIndex,
            Map<ClubSlot, int[][]> requirements,
            Map<ClubSlot, Set<ClubSlot>> assignedOpponents,
            Helper helper,
            List<Tie> finalTies,
            List<List<ClubSlot>> pots) {
        if (potIndex >= POT_COUNT)
            return true;
        List<ClubSlot> clubs = new ArrayList<>(pots.get(potIndex));
        if (!solveIntraForPot(potIndex, clubs, requirements, assignedOpponents, helper, finalTies))
            return false;
        return solveIntra(potIndex + 1, requirements, assignedOpponents, helper, finalTies, pots);
    }

    /**
     * Løser intra-pot oppgjør for én gitt pot med backtracking.
     */
    private boolean solveIntraForPot(int potIndex,
            List<ClubSlot> clubs,
            Map<ClubSlot, int[][]> requirements,
            Map<ClubSlot, Set<ClubSlot>> assignedOpponents,
            Helper helper,
            List<Tie> finalTies) {
        // Sjekk om alle klubber i denne potten har oppfylt kravene mot sin egen pot.
        boolean done = true;
        for (ClubSlot club : clubs) {
            int[][] req = requirements.get(club);
            if (req[potIndex][0] > 0 || req[potIndex][1] > 0) {
                done = false;
                break;
            }
        }
        if (done)
            return true;

        // Velg en klubb med uløste krav.
        ClubSlot clubA = null;
        for (ClubSlot club : clubs) {
            int[][] req = requirements.get(club);
            if (req[potIndex][0] > 0 || req[potIndex][1] > 0) {
                clubA = club;
                break;
            }
        }
        if (clubA == null)
            return true; // Burde ikke skje

        int[][] reqA = requirements.get(clubA);
        for (ClubSlot clubB : clubs) {
            if (clubA.equals(clubB))
                continue;
            if (assignedOpponents.get(clubA).contains(clubB))
                continue;
            int[][] reqB = requirements.get(clubB);
            if (reqB[potIndex][0] + reqB[potIndex][1] <= 0)
                continue;
            if (isIllegalTie(clubA, clubB))
                continue;
            if (!helper.canAddOpponent(clubA, clubB))
                continue;
            if (!helper.canAddOpponent(clubB, clubA))
                continue;

            boolean option1 = reqA[potIndex][0] > 0 && reqB[potIndex][1] > 0; // clubA hjemme, clubB borte
            boolean option2 = reqA[potIndex][1] > 0 && reqB[potIndex][0] > 0; // clubA borte, clubB hjemme
            if (!option1 && !option2)
                continue;

            // Prøv Option 1:
            if (option1) {
                reqA[potIndex][0]--;
                reqB[potIndex][1]--;
                assignedOpponents.get(clubA).add(clubB);
                assignedOpponents.get(clubB).add(clubA);
                helper.updateCountryCounters(clubA, clubB);
                helper.updateCountryCounters(clubB, clubA);
                finalTies.add(new SingleLeggedTie(clubA, clubB)); // clubA hjemme, clubB borte

                if (solveIntraForPot(potIndex, clubs, requirements, assignedOpponents, helper, finalTies))
                    return true;

                // Backtrack
                finalTies.remove(finalTies.size() - 1);
                helper.revertCountryCounters(clubA, clubB);
                helper.revertCountryCounters(clubB, clubA);
                assignedOpponents.get(clubA).remove(clubB);
                assignedOpponents.get(clubB).remove(clubA);
                reqA[potIndex][0]++;
                reqB[potIndex][1]++;
            }
            // Prøv Option 2:
            if (option2) {
                reqA[potIndex][1]--;
                reqB[potIndex][0]--;
                assignedOpponents.get(clubA).add(clubB);
                assignedOpponents.get(clubB).add(clubA);
                helper.updateCountryCounters(clubA, clubB);
                helper.updateCountryCounters(clubB, clubA);
                finalTies.add(new SingleLeggedTie(clubB, clubA)); // clubB hjemme, clubA borte

                if (solveIntraForPot(potIndex, clubs, requirements, assignedOpponents, helper, finalTies))
                    return true;

                // Backtrack
                finalTies.remove(finalTies.size() - 1);
                helper.revertCountryCounters(clubA, clubB);
                helper.revertCountryCounters(clubB, clubA);
                assignedOpponents.get(clubA).remove(clubB);
                assignedOpponents.get(clubB).remove(clubA);
                reqA[potIndex][1]++;
                reqB[potIndex][0]++;
            }
        }
        return false;
    }

}