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
        // Lag en mapping fra klubb til hvilken pot den tilhører og en samlet liste over
        // klubber.
        Map<ClubSlot, Integer> clubToPot = new HashMap<>();
        List<ClubSlot> allClubs = new ArrayList<>();
        for (int i = 0; i < pots.size(); i++) {
            for (ClubSlot club : pots.get(i)) {
                clubToPot.put(club, i);
                allClubs.add(club);
            }
        }

        /*
         * Opprett krav for hver klubb: For hver pot (0 .. POT_COUNT-1)
         * må hver klubb spille nøyaktig 2 oppgjør – én der den er hjemmelag og én der
         * den er bortelag.
         * Vi lagrer kravene i et 2D-array for hver klubb:
         * krav[p][0] = antall hjemmekamper mot klubber fra pot p (starter på 1)
         * krav[p][1] = antall bortekamper mot klubber fra pot p (starter på 1)
         */
        Map<ClubSlot, int[][]> requirements = new HashMap<>();
        for (ClubSlot club : allClubs) {
            int[][] arr = new int[POT_COUNT][2];
            for (int p = 0; p < POT_COUNT; p++) {
                arr[p][0] = 1; // Hjemmekamp-krav mot pot p
                arr[p][1] = 1; // Bortekamp-krav mot pot p
            }
            requirements.put(club, arr);
        }

        // Holder oversikt over allerede trukkede oppgjør for å unngå duplikater.
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
            // Sjekker om klubb 'club' kan få et oppgjør mot 'opponent' uten å overskride
            // maksen for utenlandske oppgjør.
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

            // Oppdaterer telleren for 'club' med data fra 'opponent'.
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

        // Midlertidig liste for oppgjør (SingleLeggedTie) før vi bekrefter trekningen.
        List<Tie> tempTies = new ArrayList<>();
        Random random = new Random();
        final int MAX_ATTEMPTS = 1000000;
        boolean success = false;

        attemptLoop: for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Nullstill krav, tildelte oppgjør og utenlandstellere for hver ny trekning.
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

            // --- Inter–pot oppgjør ---
            // For alle par av potter (i, j) med i < j: hver klubb i pot i skal få et
            // oppgjør mot en klubb i pot j.
            for (int i = 0; i < POT_COUNT; i++) {
                for (int j = i + 1; j < POT_COUNT; j++) {
                    List<ClubSlot> clubsI = new ArrayList<>(pots.get(i));
                    List<ClubSlot> clubsJ = new ArrayList<>(pots.get(j));
                    Collections.shuffle(clubsI, random);
                    Collections.shuffle(clubsJ, random);

                    for (ClubSlot clubA : clubsI) {
                        int[][] reqA = currentReq.get(clubA);
                        // Fortsett til kravene mot pot j er dekket (enten som hjem eller borte).
                        while (reqA[j][0] + reqA[j][1] > 0) {
                            List<ClubSlot> candidates = new ArrayList<>();
                            for (ClubSlot clubB : clubsJ) {
                                int[][] reqB = currentReq.get(clubB);
                                if (reqB[i][0] + reqB[i][1] <= 0)
                                    continue;
                                if (currentAssigned.get(clubA).contains(clubB))
                                    continue;
                                if (IllegalTies.isProhibited(clubA.getCountries().get(0), clubB.getCountries().get(0)))
                                    continue;
                                if (!helper.canAddOpponent(clubA, clubB))
                                    continue;
                                if (!helper.canAddOpponent(clubB, clubA))
                                    continue;
                                // Sjekk om minst ett av de to mulige oppsett (A hjemme/B borte eller A borte/B
                                // hjemme) er mulig.
                                boolean option1 = reqA[j][0] > 0 && reqB[i][1] > 0;
                                boolean option2 = reqA[j][1] > 0 && reqB[i][0] > 0;
                                if (option1 || option2) {
                                    candidates.add(clubB);
                                }
                            }
                            if (candidates.isEmpty()) {
                                failed = true;
                                break;
                            }
                            ClubSlot clubB = candidates.get(random.nextInt(candidates.size()));
                            int[][] reqB = currentReq.get(clubB);
                            boolean option1 = reqA[j][0] > 0 && reqB[i][1] > 0; // clubA hjemme, clubB borte
                            boolean option2 = reqA[j][1] > 0 && reqB[i][0] > 0; // clubA borte, clubB hjemme
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
                                tempTies.add(new SingleLeggedTie(clubA, clubB));
                                reqA[j][0]--; // clubA oppfyller et hjemmekamp-krav mot pot j
                                reqB[i][1]--; // clubB oppfyller et bortekamp-krav mot pot i
                            } else {
                                tempTies.add(new SingleLeggedTie(clubB, clubA));
                                reqA[j][1]--; // clubA oppfyller et bortekamp-krav mot pot j
                                reqB[i][0]--; // clubB oppfyller et hjemmekamp-krav mot pot i
                            }
                            currentAssigned.get(clubA).add(clubB);
                            currentAssigned.get(clubB).add(clubA);
                            helper.updateCountryCounters(clubA, clubB);
                            helper.updateCountryCounters(clubB, clubA);
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

            // --- Intra–pot oppgjør ---
            // For hver pot trekkes oppgjør mellom klubber i samme pot.
            for (int i = 0; i < POT_COUNT; i++) {
                List<ClubSlot> clubs = new ArrayList<>(pots.get(i));
                Collections.shuffle(clubs, random);
                for (ClubSlot clubA : clubs) {
                    int[][] reqA = currentReq.get(clubA);
                    while (reqA[i][0] + reqA[i][1] > 0) {
                        List<ClubSlot> candidates = new ArrayList<>();
                        for (ClubSlot clubB : clubs) {
                            if (clubA.equals(clubB))
                                continue;
                            int[][] reqB = currentReq.get(clubB);
                            if (reqB[i][0] + reqB[i][1] <= 0)
                                continue;
                            if (currentAssigned.get(clubA).contains(clubB))
                                continue;
                            if (IllegalTies.isProhibited(clubA.getCountries().get(0), clubB.getCountries().get(0)))
                                continue;
                            if (!helper.canAddOpponent(clubA, clubB))
                                continue;
                            if (!helper.canAddOpponent(clubB, clubA))
                                continue;
                            boolean option1 = reqA[i][0] > 0 && reqB[i][1] > 0;
                            boolean option2 = reqA[i][1] > 0 && reqB[i][0] > 0;
                            if (option1 || option2) {
                                candidates.add(clubB);
                            }
                        }
                        if (candidates.isEmpty()) {
                            failed = true;
                            break;
                        }
                        ClubSlot clubB = candidates.get(random.nextInt(candidates.size()));
                        int[][] reqB = currentReq.get(clubB);
                        boolean option1 = reqA[i][0] > 0 && reqB[i][1] > 0;
                        boolean option2 = reqA[i][1] > 0 && reqB[i][0] > 0;
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
                            tempTies.add(new SingleLeggedTie(clubA, clubB));
                            reqA[i][0]--;
                            reqB[i][1]--;
                        } else {
                            tempTies.add(new SingleLeggedTie(clubB, clubA));
                            reqA[i][1]--;
                            reqB[i][0]--;
                        }
                        currentAssigned.get(clubA).add(clubB);
                        currentAssigned.get(clubB).add(clubA);
                        helper.updateCountryCounters(clubA, clubB);
                        helper.updateCountryCounters(clubB, clubA);
                    }
                    if (failed)
                        break;
                }
                if (failed)
                    break;
            }
            if (failed)
                continue attemptLoop;

            // Verifiser at alle krav er oppfylt
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
    }

}