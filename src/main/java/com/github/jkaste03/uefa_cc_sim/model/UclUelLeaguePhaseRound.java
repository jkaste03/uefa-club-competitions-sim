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

    // --- Hjelpeklasse for sjekk av utenlandsbegrensning ---
    class Helper {
        boolean canAddOpponent(ClubSlot club, ClubSlot opponent,
                Map<ClubSlot, Map<Country, Integer>> countryCounters) {
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

        void updateCountryCounters(ClubSlot club, ClubSlot opponent,
                Map<ClubSlot, Map<Country, Integer>> countryCounters) {
            for (Country oppCountry : opponent.getCountries()) {
                if (!club.getCountries().contains(oppCountry)) {
                    int count = countryCounters.get(club).getOrDefault(oppCountry, 0);
                    countryCounters.get(club).put(oppCountry, count + 1);
                }
            }
        }
    }

    @Override
    protected void draw() {
        // --- Forbered data: mapping for klubb til pot, liste over alle klubber ---
        Map<ClubSlot, Integer> clubToPot = new HashMap<>();
        List<ClubSlot> allClubs = new ArrayList<>();
        for (int i = 0; i < pots.size(); i++) {
            for (ClubSlot club : pots.get(i)) {
                clubToPot.put(club, i);
                allClubs.add(club);
            }
        }

        // --- Opprett krav for hver klubb ---
        // For hver klubb skal vi ha en matrise med krav per pot:
        // krav[p][0] = antall hjemmekamper mot klubber fra pot p (starter på 1)
        // krav[p][1] = antall bortekamper mot klubber fra pot p (starter på 1)
        Map<ClubSlot, int[][]> initialRequirements = new HashMap<>();
        for (ClubSlot club : allClubs) {
            int[][] arr = new int[POT_COUNT][2];
            for (int p = 0; p < POT_COUNT; p++) {
                arr[p][0] = 1;
                arr[p][1] = 1;
            }
            initialRequirements.put(club, arr);
        }

        // --- Initielle strukturer for tildelte oppgjør og utenlandskalere ---
        Map<ClubSlot, Set<ClubSlot>> initialAssigned = new HashMap<>();
        for (ClubSlot club : allClubs) {
            initialAssigned.put(club, new HashSet<>());
        }
        Map<ClubSlot, Map<Country, Integer>> initialCountryCounters = new HashMap<>();
        for (ClubSlot club : allClubs) {
            initialCountryCounters.put(club, new HashMap<>());
        }

        Helper helper = new Helper();

        // Vi skal samle opp alle trukkede oppgjør i en liste.
        List<Tie> finalTies = new ArrayList<>();

        // --- Definer den rekursive søkefunksjonen ---
        // Parametere:
        // currentReq: nåværende krav for hver klubb
        // currentAssigned: allerede tildelte oppgjør (for begge klubber)
        // countryCounters: teller for utenlandske oppgjør
        // tiesSoFar: oppbygde oppgjør til nå
        // nextPhase: fasen vi jobber med: 0 = inter–pot, 1 = intra–pot.
        //
        // Vi løser først alle inter–pot oppgjør (mellom alle par (i,j) med i<j).
        // Når disse er fullført, går vi over til intra–pot oppgjør.
        boolean solved = solveMatches(
                0, // start med inter–pot fase (fase 0)
                initialRequirements,
                initialAssigned,
                initialCountryCounters,
                finalTies,
                allClubs,
                helper,
                clubToPot);

        if (!solved) {
            throw new RuntimeException("Kunne ikke fullføre trekningen uten deadlock.");
        }

        // Dersom vi kom hit, er finalTies fullstendig.
        ties = finalTies;
    }

    /**
     * Rekursiv funksjon for å tilordne oppgjør.
     * 
     * @param phase           0 = inter–pot, 1 = intra–pot.
     * @param currentReq      gjeldende krav per klubb.
     * @param currentAssigned allerede tildelte motstandere.
     * @param countryCounters teller for utenlandske oppgjør.
     * @param tiesSoFar       liste over oppgjør så langt.
     * @param allClubs        liste over alle klubber.
     * @param helper          hjelpemetoder for utenlandssjekk.
     * @param clubToPot       mapping fra klubb til pot.
     * @return true om en fullstendig løsning ble funnet, false ellers.
     */
    private boolean solveMatches(
            int phase,
            Map<ClubSlot, int[][]> currentReq,
            Map<ClubSlot, Set<ClubSlot>> currentAssigned,
            Map<ClubSlot, Map<Country, Integer>> countryCounters,
            List<Tie> tiesSoFar,
            List<ClubSlot> allClubs,
            Helper helper,
            Map<ClubSlot, Integer> clubToPot) {
        // Velg hvilke par vi skal jobbe med i denne fasen:
        // I fase 0 (inter–pot): for hver kombinasjon av potter i og j (i < j),
        // må hver klubb i pot i møte en klubb i pot j (og vice versa).
        // I fase 1 (intra–pot): for hver pot må vi tilordne oppgjør mellom klubbene i
        // samme pot.

        // Vi finner den "mest begrensede" (minimum remaining values) oppgaven.
        // Oppgaven identifiseres ved en klubb A som fortsatt har et krav mot pot P.
        // For inter–pot: dersom klubb A tilhører pot i og pot P = j med i != j.
        // For intra–pot: pot P er lik klubbens egen pot.
        ClubSlot nextClub = null;
        int targetPot = -1;
        boolean isHomeRequirement = false; // om kravet skal fylles som hjemmekamp for nextClub
        int minCandidates = Integer.MAX_VALUE;
        // Søk gjennom alle klubber og deres krav for å finne den med færrest lovlige
        // kandidater.
        for (ClubSlot club : allClubs) {
            int clubPot = clubToPot.get(club);
            int[][] req = currentReq.get(club);
            // For hver pot vi må møte (0..POT_COUNT-1)
            for (int p = 0; p < req.length; p++) {
                // Bestem om vi er i inter–pot eller intra–pot for dette kravet
                if ((phase == 0 && clubPot != p) || (phase == 1 && clubPot == p)) {
                    // Sjekk hjemmekamp og bortekamp separat
                    for (int j = 0; j < 2; j++) {
                        if (req[p][j] > 0) {
                            // Finn antall mulige kandidater for denne kombinasjonen:
                            int candidates = countCandidates(club, p, j, currentReq, currentAssigned, countryCounters,
                                    helper, clubToPot, allClubs);
                            if (candidates < minCandidates) {
                                minCandidates = candidates;
                                nextClub = club;
                                targetPot = p;
                                isHomeRequirement = (j == 0);
                            }
                        }
                    }
                }
            }
        }

        // Dersom ingen krav gjenstår i denne fasen, gå videre til neste fase eller
        // avslutt.
        if (nextClub == null) {
            if (phase == 0) {
                // Start intra–pot fase
                return solveMatches(
                        1,
                        currentReq,
                        currentAssigned,
                        countryCounters,
                        tiesSoFar,
                        allClubs,
                        helper,
                        clubToPot);
            } else {
                // Ingen krav igjen i intra–pot – løsning funnet!
                return true;
            }
        }

        // Få liste over kandidater for oppgjør for nextClub mot targetPot
        List<ClubSlot> candidateOpponents = new ArrayList<>();
        for (ClubSlot opp : allClubs) {
            // Identifiser kandidatens pot basert på fasen:
            int oppPot = clubToPot.get(opp);
            if (phase == 0 && oppPot != targetPot)
                continue;
            if (phase == 1 && oppPot != clubToPot.get(nextClub))
                continue;
            // Skal oppgjøret dekke et krav for opp (fra nextClub sin pot)
            int[][] oppReq = currentReq.get(opp);
            if (oppReq[clubToPot.get(nextClub)][(isHomeRequirement ? 1 : 0)] <= 0)
                continue;
            if (currentAssigned.get(nextClub).contains(opp))
                continue;
            if (isIllegalTie(nextClub, opp))
                continue;
            if (!helper.canAddOpponent(nextClub, opp, countryCounters))
                continue;
            if (!helper.canAddOpponent(opp, nextClub, countryCounters))
                continue;
            candidateOpponents.add(opp);
        }

        // Dersom ingen kandidater finnes, backtrack.
        if (candidateOpponents.isEmpty())
            return false;

        // For hver kandidat, prøv å tilordne oppgjøret og kall rekursivt.
        // Vi oppdaterer både krav, assignedOpponents og landstellere.
        for (ClubSlot opp : candidateOpponents) {
            // Lag dype kopier av tilstandene slik at vi kan backtracke
            Map<ClubSlot, int[][]> reqCopy = deepCopyRequirements(currentReq);
            Map<ClubSlot, Set<ClubSlot>> assignedCopy = deepCopyAssigned(currentAssigned);
            Map<ClubSlot, Map<Country, Integer>> countersCopy = deepCopyCounters(countryCounters);
            List<Tie> tiesCopy = new ArrayList<>(tiesSoFar);

            // Bestem oppsett: dersom nextClub fyller sin krav (hjemmekamp eller bortekamp)
            // så må motparten fylle den komplementære (borte om nextClub er hjemme, ellers
            // hjemme).
            // Oppdater krav for nextClub og opp.
            reqCopy.get(nextClub)[targetPot][(isHomeRequirement ? 0 : 1)]--;
            reqCopy.get(opp)[clubToPot.get(nextClub)][(isHomeRequirement ? 1 : 0)]--;

            // Registrer at nextClub og opp har blitt møtt.
            assignedCopy.get(nextClub).add(opp);
            assignedCopy.get(opp).add(nextClub);

            // Oppdater landstellere for begge klubber.
            helper.updateCountryCounters(nextClub, opp, countersCopy);
            helper.updateCountryCounters(opp, nextClub, countersCopy);

            // Legg til oppgjøret – rekkefølgen bestemmes av hvem som spiller hjemme.
            Tie tie;
            if (isHomeRequirement) {
                tie = new SingleLeggedTie(nextClub, opp);
            } else {
                tie = new SingleLeggedTie(opp, nextClub);
            }
            tiesCopy.add(tie);

            // Rekursivt kall med oppdatert tilstand.
            if (solveMatches(phase, reqCopy, assignedCopy, countersCopy, tiesCopy, allClubs, helper, clubToPot)) {
                // Dersom vi finner en løsning, kopier resultatene tilbake.
                tiesSoFar.clear();
                tiesSoFar.addAll(tiesCopy);
                // Kopier over til currentReq, currentAssigned, countryCounters dersom de skal
                // brukes etterpå.
                currentReq.clear();
                currentReq.putAll(reqCopy);
                currentAssigned.clear();
                currentAssigned.putAll(assignedCopy);
                countryCounters.clear();
                countryCounters.putAll(countersCopy);
                return true;
            }
        }

        // Ingen kandidat førte til full løsning: returner false.
        return false;
    }

    /**
     * Teller antall lovlige kandidater for et gitt krav.
     */
    private int countCandidates(
            ClubSlot club,
            int targetPot,
            int homeFlag, // 0 for hjemmekrav, 1 for bortekrav
            Map<ClubSlot, int[][]> currentReq,
            Map<ClubSlot, Set<ClubSlot>> currentAssigned,
            Map<ClubSlot, Map<Country, Integer>> countryCounters,
            Helper helper,
            Map<ClubSlot, Integer> clubToPot,
            List<ClubSlot> allClubs) {
        int count = 0;
        for (ClubSlot opp : allClubs) {
            int oppPot = clubToPot.get(opp);
            if (clubToPot.get(club) == oppPot && targetPot != oppPot)
                continue; // i inter–pot-fasen: velg bare motstandere fra riktig pot.
            if (clubToPot.get(club) != oppPot && targetPot == oppPot && currentReq.get(club)[targetPot][homeFlag] <= 0)
                continue;
            // I intra–pot-fasen, velg bare motstandere fra samme pot.
            if (!currentAssigned.get(club).contains(opp)
                    && !isIllegalTie(club, opp)
                    && helper.canAddOpponent(club, opp, countryCounters)
                    && helper.canAddOpponent(opp, club, countryCounters)) {
                // Sjekk om opp har det komplementære kravet.
                int oppReq = currentReq.get(opp)[clubToPot.get(club)][(homeFlag == 0 ? 1 : 0)];
                if (oppReq > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Dype kopieringsmetoder for tilstandsstrukturer.
     */
    private Map<ClubSlot, int[][]> deepCopyRequirements(Map<ClubSlot, int[][]> original) {
        Map<ClubSlot, int[][]> copy = new HashMap<>();
        for (Map.Entry<ClubSlot, int[][]> entry : original.entrySet()) {
            int[][] arr = new int[entry.getValue().length][2];
            for (int i = 0; i < entry.getValue().length; i++) {
                arr[i][0] = entry.getValue()[i][0];
                arr[i][1] = entry.getValue()[i][1];
            }
            copy.put(entry.getKey(), arr);
        }
        return copy;
    }

    private Map<ClubSlot, Set<ClubSlot>> deepCopyAssigned(Map<ClubSlot, Set<ClubSlot>> original) {
        Map<ClubSlot, Set<ClubSlot>> copy = new HashMap<>();
        for (Map.Entry<ClubSlot, Set<ClubSlot>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    private Map<ClubSlot, Map<Country, Integer>> deepCopyCounters(Map<ClubSlot, Map<Country, Integer>> original) {
        Map<ClubSlot, Map<Country, Integer>> copy = new HashMap<>();
        for (Map.Entry<ClubSlot, Map<Country, Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        return copy;
    }
}