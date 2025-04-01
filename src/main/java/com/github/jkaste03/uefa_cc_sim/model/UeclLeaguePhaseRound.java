package com.github.jkaste03.uefa_cc_sim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.drools.modelcompiler.consequence.FactHandleLookup.Single;

import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.Tournament;
import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * Class representing the league phase in the UEFA Conference League.
 * This class handles the league phase rounds where clubs compete in a league
 * format specific to the Conference League.
 */
public class UeclLeaguePhaseRound extends LeaguePhaseRound {
    private final static int POT_COUNT = 6;

    // Mapping fra klubb til pot (0-indexert) – fylles ved init.
    private Map<ClubSlot, Integer> clubToPot;

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

    private static class Helper {
        public boolean canAddOpponent(ClubSlot club, ClubSlot opponent,
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

        public void updateCountryCounters(ClubSlot club, ClubSlot opponent,
                Map<ClubSlot, Map<Country, Integer>> countryCounters) {
            for (Country oppCountry : opponent.getCountries()) {
                if (!club.getCountries().contains(oppCountry)) {
                    int count = countryCounters.get(club).getOrDefault(oppCountry, 0);
                    countryCounters.get(club).put(oppCountry, count + 1);
                }
            }
        }
    }

    /**
     * Returnerer underpot-indeksen for en gitt pot-indeks.
     * Underpot 0: pot 0 og 1, Underpot 1: pot 2 og 3, Underpot 2: pot 4 og 5.
     */
    private int getUnderpot(int potIndex) {
        return potIndex / 2;
    }

    /**
     * Trekker ligaoppsettet for Conference League. Algoritmen deles opp på
     * underpot-grupper.
     * For hver underpot (to pot-er) trekkes to legs: én der klubbene i den "første"
     * poten har hjemme,
     * og én der de bytter rolle. Backtracking brukes for å unngå deadlock.
     */
    @Override
    protected void draw() {
        // Oppsett for å telle antall "utenlandsoppgjør" for hver klubb.
        Map<ClubSlot, Map<Country, Integer>> countryCounters = new HashMap<>();
        for (ClubSlot club : clubSlots) {
            countryCounters.put(club, new HashMap<>());
        }
        Helper helper = new Helper();
        Random rnd = new Random();

        // For hver underpot: (pot 0/1, 2/3, 4/5)
        for (int up = 0; up < 3; up++) {
            int potA = up * 2;
            int potB = up * 2 + 1;
            // Leg 1: klubbene i potA er hjemme, potB er borte.
            List<ClubSlot> homeList = new ArrayList<>(pots.get(potA));
            List<ClubSlot> awayList = new ArrayList<>(pots.get(potB));
            Collections.shuffle(homeList, rnd);
            Collections.shuffle(awayList, rnd);
            if (!pairClubs(homeList, awayList, countryCounters, helper, rnd)) {
                throw new IllegalStateException("Deadlock i trekk for underpot " + up + " leg 1.");
            }
            // Leg 2: bytt roller – potB hjemme, potA borte.
            homeList = new ArrayList<>(pots.get(potB));
            awayList = new ArrayList<>(pots.get(potA));
            Collections.shuffle(homeList, rnd);
            Collections.shuffle(awayList, rnd);
            if (!pairClubs(homeList, awayList, countryCounters, helper, rnd)) {
                throw new IllegalStateException("Deadlock i trekk for underpot " + up + " leg 2.");
            }
        }
    }

    /**
     * Forsøker å parre alle klubbene i homeList med en gyldig motstander i
     * awayList.
     * Metoden bruker backtracking for å unngå deadlock.
     *
     * @param homeList        listen med hjemmeklubber (som skal trekkes)
     * @param awayList        listen med borteklubber
     * @param countryCounters teller for antall utenlandsoppgjør for hver klubb
     * @param helper          instans av hjelpeklasse for sjekk og oppdatering
     * @param rnd             Random-instans for shuffling
     * @return true om parringen lykkes, false ellers.
     */
    private boolean pairClubs(List<ClubSlot> homeList, List<ClubSlot> awayList,
            Map<ClubSlot, Map<Country, Integer>> countryCounters,
            Helper helper, Random rnd) {
        if (homeList.isEmpty()) {
            return true;
        }
        // Velg den første hjemmeklubben (rekkefølge er tilfeldig pga. shuffling)
        ClubSlot homeClub = homeList.remove(0);
        List<ClubSlot> awayCandidates = new ArrayList<>(awayList);
        Collections.shuffle(awayCandidates, rnd);

        for (ClubSlot awayClub : awayCandidates) {
            // Sjekk om tie-en er lovlig for begge retninger
            if (isIllegalTie(homeClub, awayClub) || isIllegalTie(awayClub, homeClub)) {
                continue;
            }
            if (!helper.canAddOpponent(homeClub, awayClub, countryCounters)
                    || !helper.canAddOpponent(awayClub, homeClub, countryCounters)) {
                continue;
            }
            // Forbered backup av countrystatistikken
            Map<Country, Integer> homeBackup = new HashMap<>(countryCounters.get(homeClub));
            Map<Country, Integer> awayBackup = new HashMap<>(countryCounters.get(awayClub));

            // Oppdater countrystatistikken for begge klubber
            helper.updateCountryCounters(homeClub, awayClub, countryCounters);
            helper.updateCountryCounters(awayClub, homeClub, countryCounters);

            // Legg til tie-en globalt
            Tie tie = new SingleLeggedTie(homeClub, awayClub);
            ties.add(tie);

            // Fjern valgt awayClub fra available-listen
            awayList.remove(awayClub);

            // Prøv å parre resten rekursivt
            if (pairClubs(homeList, awayList, countryCounters, helper, rnd)) {
                return true;
            }
            // Backtracking: fjern tie-en, gjenopprett countrystatistikk og legg tilbake
            // awayClub.
            ties.remove(tie);
            countryCounters.put(homeClub, homeBackup);
            countryCounters.put(awayClub, awayBackup);
            awayList.add(awayClub);
            // Gjenopprett rekkefølgen ved å shufflere bortelisten
            Collections.shuffle(awayList, rnd);
        }
        // Legg tilbake homeClub før vi returnerer false (backtracking)
        homeList.add(0, homeClub);
        return false;
    }
}