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

    private final static int MAX_RESTART_ATTEMPTS = 100000;

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
        // Lag mapping fra klubb til pot og samlet liste over klubber.
        Map<ClubSlot, Integer> clubToPot = new HashMap<>();
        List<ClubSlot> allClubs = new ArrayList<>();
        for (int i = 0; i < pots.size(); i++) {
            for (ClubSlot club : pots.get(i)) {
                clubToPot.put(club, i);
                allClubs.add(club);
            }
        }

        // Opprett krav for hver klubb: For hver pot må hver klubb spille nøyaktig 2
        // oppgjør –
        // én hjemmekamp og én bortekamp.
        Map<ClubSlot, int[][]> requirements = new HashMap<>();
        for (ClubSlot club : allClubs) {
            int[][] arr = new int[POT_COUNT][2];
            for (int p = 0; p < POT_COUNT; p++) {
                arr[p][0] = 1; // Hjemmekamp-krav mot pot p
                arr[p][1] = 1; // Bortekamp-krav mot pot p
            }
            requirements.put(club, arr);
        }

        // Holder oversikt over allerede tildelte oppgjør for å unngå duplikater.
        Map<ClubSlot, Set<ClubSlot>> assignedOpponents = new HashMap<>();
        for (ClubSlot club : allClubs) {
            assignedOpponents.put(club, new HashSet<>());
        }

        // Teller for antall oppgjør mot "utenlandske" land per klubb.
        Map<ClubSlot, Map<Country, Integer>> countryCounters = new HashMap<>();
        for (ClubSlot club : allClubs) {
            countryCounters.put(club, new HashMap<>());
        }

        // Hjelpeklasse for å sjekke og oppdatere utenlandstak.
        class Helper {
            boolean canAddOpponent(ClubSlot club, ClubSlot opponent) {
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

            void updateCountryCounters(ClubSlot club, ClubSlot opponent) {
                for (Country oppCountry : opponent.getCountries()) {
                    if (!club.getCountries().contains(oppCountry)) {
                        int count = countryCounters.get(club).getOrDefault(oppCountry, 0);
                        countryCounters.get(club).put(oppCountry, count + 1);
                    }
                }
            }
        }
        Helper helper = new Helper();

        List<Tie> tempTies = new ArrayList<>();
        Random random = new Random();
        final int MAX_ATTEMPTS = 1000000;
        boolean success = false;

        attemptLoop: for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Nullstill krav, tildelte oppgjør og utenlandsteller for hver ny trekning.
            Map<ClubSlot, int[][]> currentReq = new HashMap<>();
            for (ClubSlot club : allClubs) {
                int[][] arr = new int[POT_COUNT][2];
                for (int p = 0; p < POT_COUNT; p++) {
                    arr[p][0] = 1;
                    arr[p][1] = 1;
                }
                currentReq.put(club, arr);
            }
            Map<ClubSlot, Set<ClubSlot>> currentAssigned = new HashMap<>();
            for (ClubSlot club : allClubs) {
                currentAssigned.put(club, new HashSet<>());
            }
            // Nullstill countryCounters.
            for (ClubSlot club : allClubs) {
                countryCounters.get(club).clear();
            }
            tempTies.clear();
            boolean failed = false;

            // --- Pot–for–pot trekning ---
            // For hver pot (0..POT_COUNT-1) trekkes alle oppgjør for klubbene i den potten.
            for (int currentPot = 0; currentPot < POT_COUNT; currentPot++) {
                // Hent og bland klubbene i den nåværende potten.
                List<ClubSlot> clubsInPot = new ArrayList<>(pots.get(currentPot));
                Collections.shuffle(clubsInPot, random);

                for (ClubSlot club : clubsInPot) {
                    // Gå gjennom alle motstanderpottene (inkludert egen pot om intra–pot trekk skal
                    // gjelde)
                    for (int opponentPot = 0; opponentPot < POT_COUNT; opponentPot++) {
                        // Så lenge kravene for kamp (hjem og/eller borte) mot opponentPot ikke er
                        // oppfylt:
                        while (currentReq.get(club)[opponentPot][0] + currentReq.get(club)[opponentPot][1] > 0) {
                            List<ClubSlot> candidates = new ArrayList<>();
                            // Finn kandidater i pot opponentPot:
                            List<ClubSlot> clubsCandidatePool = new ArrayList<>(pots.get(opponentPot));
                            Collections.shuffle(clubsCandidatePool, random);
                            for (ClubSlot candidate : clubsCandidatePool) {
                                // Unngå kamp mot seg selv ved intra–pot trekk:
                                if (club.equals(candidate))
                                    continue;
                                // Hvis kamp allerede er trukket, hopp over.
                                if (currentAssigned.get(club).contains(candidate))
                                    continue;
                                // Sjekk at motpartens krav mot klubbens pot er ledig.
                                if (currentReq.get(candidate)[currentPot][0]
                                        + currentReq.get(candidate)[currentPot][1] <= 0)
                                    continue;
                                // Sjekk at oppgjøret er lovlig.
                                if (isIllegalTie(club, candidate))
                                    continue;
                                // Sjekk utenlandsk–begrensning.
                                if (!helper.canAddOpponent(club, candidate))
                                    continue;
                                if (!helper.canAddOpponent(candidate, club))
                                    continue;
                                // Sjekk om minst ett av de to mulige oppsett er mulig:
                                boolean option1 = currentReq.get(club)[opponentPot][0] > 0
                                        && currentReq.get(candidate)[currentPot][1] > 0;
                                boolean option2 = currentReq.get(club)[opponentPot][1] > 0
                                        && currentReq.get(candidate)[currentPot][0] > 0;
                                if (option1 || option2) {
                                    candidates.add(candidate);
                                }
                            }
                            if (candidates.isEmpty()) {
                                failed = true;
                                break;
                            }
                            ClubSlot selectedCandidate = candidates.get(random.nextInt(candidates.size()));
                            // Hent kandidatens krav.
                            int[][] candidateReq = currentReq.get(selectedCandidate);
                            boolean option1 = currentReq.get(club)[opponentPot][0] > 0
                                    && candidateReq[currentPot][1] > 0;
                            boolean option2 = currentReq.get(club)[opponentPot][1] > 0
                                    && candidateReq[currentPot][0] > 0;
                            boolean chooseOption1;
                            if (option1 && option2) {
                                chooseOption1 = random.nextBoolean();
                            } else if (option1) {
                                chooseOption1 = true;
                            } else if (option2) {
                                chooseOption1 = false;
                            } else {
                                failed = true;
                                break;
                            }
                            if (chooseOption1) {
                                tempTies.add(new SingleLeggedTie(club, selectedCandidate));
                                currentReq.get(club)[opponentPot][0]--; // club spiller hjemme mot opponentPot
                                candidateReq[currentPot][1]--; // selectedCandidate spiller borte mot currentPot
                            } else {
                                tempTies.add(new SingleLeggedTie(selectedCandidate, club));
                                currentReq.get(club)[opponentPot][1]--; // club spiller borte mot opponentPot
                                candidateReq[currentPot][0]--; // selectedCandidate spiller hjemme mot currentPot
                            }
                            currentAssigned.get(club).add(selectedCandidate);
                            currentAssigned.get(selectedCandidate).add(club);
                            helper.updateCountryCounters(club, selectedCandidate);
                            helper.updateCountryCounters(selectedCandidate, club);
                        }
                        if (failed)
                            break;
                    }
                    if (failed)
                        break;
                }
                if (failed)
                    break;
            }
            if (failed)
                continue attemptLoop;

            // Verifiser at alle krav er oppfylt for alle klubber.
            boolean allMet = true;
            for (ClubSlot club : allClubs) {
                int[][] r = currentReq.get(club);
                for (int p = 0; p < POT_COUNT; p++) {
                    if (r[p][0] != 0 || r[p][1] != 0) {
                        allMet = false;
                        break;
                    }
                }
                if (!allMet)
                    break;
            }
            if (!allMet)
                continue attemptLoop;

            success = true;
            break;
        } // end attemptLoop

        if (!success) {
            throw new RuntimeException("Kunne ikke fullføre trekningen uten deadlock etter maks antall forsøk.");
        }

        // Overfør de trukkede oppgjørene til ties-variabelen.
        ties = tempTies;

        // for (Tie tie : ties) {
        // System.out.println(tie.getName());
        // }
    }
}