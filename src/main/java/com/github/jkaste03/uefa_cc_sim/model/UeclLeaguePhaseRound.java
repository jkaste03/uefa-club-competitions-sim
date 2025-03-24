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

    /**
     * Seeds the club slots into pots for the league phase.
     * <p>
     * This method ensures that the number of club slots is divisible by the
     * constant POT_COUNT.
     * If the club slots are null or their size is not divisible by POT_COUNT, an
     * IllegalStateException is thrown.
     * The club slots are then sorted based on their ranking and divided into pots.
     * Each pot is printed to the console.
     * </p>
     *
     * @throws IllegalStateException if the number of club slots is null or not
     *                               divisible by POT_COUNT.
     */
    @Override
    protected void seed() {
        // Ensure the number of clubSlots is divisible by POT_COUNT.
        if (clubSlots == null || clubSlots.size() % POT_COUNT != 0) {
            throw new IllegalStateException("ClubSlot count must be divisible by " + POT_COUNT + " to seed properly.");
        }

        // Sort the club slots based on their ranking.
        clubSlots.sort((c1, c2) -> Float.compare(c1.getRanking(), c2.getRanking()));

        // Divide the club slots into pots for the league phase.
        for (int i = 0; i < POT_COUNT; i++) {
            pots.add(clubSlots.subList(i * clubSlots.size() / POT_COUNT, (i + 1) * clubSlots.size() / POT_COUNT));
            // System.out.println("\n" + getName() + ", pot " + (i + 1) + ":");
            printClubSlotList(pots.get(i));
        }
    }

    @Override
    protected void draw() {
        // // For Conference League har vi seks pot-er (indeksert 0..5).
        // // Trekningen skal gjøres i tre par: (pot 0 og 1), (pot 2 og 3) og (pot 4 og
        // 5).
        // final int NUM_PAIRINGS = 3;
        // final int MAX_ATTEMPTS = 1000000;
        // Random random = new Random();
        // List<Tie> tempTies = new ArrayList<>();

        // // Hjelpefunksjoner for utenlandstak (maks to oppgjør per fremmedland)
        // // Sjekker om et oppgjør kan legges til for 'club' med tanke på 'opponent'
        // // med hensyn til utenlandstak.
        // class Helper {
        // boolean canAddOpponent(ClubSlot club, ClubSlot opponent,
        // Map<ClubSlot, Map<Country, Integer>> counters) {
        // for (Country oppCountry : opponent.getCountries()) {
        // if (!club.getCountries().contains(oppCountry)) {
        // int count = counters.get(club).getOrDefault(oppCountry, 0);
        // if (count >= 2) {
        // return false;
        // }
        // }
        // }
        // return true;
        // }

        // void updateCountryCounters(ClubSlot club, ClubSlot opponent,
        // Map<ClubSlot, Map<Country, Integer>> counters) {
        // for (Country oppCountry : opponent.getCountries()) {
        // if (!club.getCountries().contains(oppCountry)) {
        // int count = counters.get(club).getOrDefault(oppCountry, 0);
        // counters.get(club).put(oppCountry, count + 1);
        // }
        // }
        // }
        // }
        // Helper helper = new Helper();

        // boolean success = false;
        // attemptLoop: for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
        // tempTies.clear();
        // boolean failed = false;
        // // Opprett og nullstill countryCounters for alle klubber i alle pot-er.
        // Map<ClubSlot, Map<Country, Integer>> countryCounters = new HashMap<>();
        // for (int i = 0; i < pots.size(); i++) {
        // for (ClubSlot club : pots.get(i)) {
        // countryCounters.put(club, new HashMap<>());
        // }
        // }

        // // For hvert pot-par (f.eks. pot 0 og 1, 2 og 3, 4 og 5)
        // for (int pairing = 0; pairing < NUM_PAIRINGS; pairing++) {
        // int potAIndex = pairing * 2; // 0, 2, 4
        // int potBIndex = potAIndex + 1; // 1, 3, 5

        // // Kopier og bland lagene i de to pottene
        // List<ClubSlot> potA = new ArrayList<>(pots.get(potAIndex));
        // List<ClubSlot> potB = new ArrayList<>(pots.get(potBIndex));
        // Collections.shuffle(potA, random);
        // Collections.shuffle(potB, random);

        // // Forutsetter at antall lag i de to pottene er like
        // if (potA.size() != potB.size()) {
        // failed = true;
        // break;
        // }

        // // Prøv å pare lagene ett–til–ett etter rekkefølge
        // for (int i = 0; i < potA.size(); i++) {
        // ClubSlot teamA = potA.get(i);
        // ClubSlot teamB = potB.get(i);

        // // Sjekk at oppgjøret ikke er forbudt (bruker gjerne den første landet i
        // listen)
        // if (isIllegalTie(teamA, teamB)) {
        // failed = true;
        // break;
        // }
        // // Sjekk utenlandstak for begge parter
        // if (!helper.canAddOpponent(teamA, teamB, countryCounters)) {
        // failed = true;
        // break;
        // }
        // if (!helper.canAddOpponent(teamB, teamA, countryCounters)) {
        // failed = true;
        // break;
        // }
        // // Dersom alt er OK: Opprett to ties, én med teamA hjemme og én med teamB
        // // hjemme.
        // tempTies.add(new SingleLeggedTie(teamA, teamB));
        // tempTies.add(new SingleLeggedTie(teamB, teamA));
        // // Oppdater tellere for utenlandske oppgjør
        // helper.updateCountryCounters(teamA, teamB, countryCounters);
        // helper.updateCountryCounters(teamB, teamA, countryCounters);
        // }
        // if (failed) {
        // break; // Gå ut av pot-par-loopen dersom et par feiler
        // }
        // }
        // if (failed) {
        // continue attemptLoop; // Prøv en ny trekning dersom noe gikk galt
        // }
        // success = true;
        // break;
        // } // end attemptLoop

        // if (!success) {
        // throw new RuntimeException(
        // "Kunne ikke fullføre Conference League trekning uten deadlock etter maks
        // antall forsøk.");
        // }
        // // Overfør de trukkede oppgjørene til ties-variabelen.
        // ties = tempTies;
    }
}