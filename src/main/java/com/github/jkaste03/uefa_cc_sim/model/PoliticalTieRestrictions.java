package com.github.jkaste03.uefa_cc_sim.model;

import java.util.HashSet;
import java.util.Set;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * Class containing all the illegal matchups based on political restrictions
 * decided by the UEFA Executive Committee.
 */
public class PoliticalTieRestrictions {
    /**
     * A set of illegal pairs of countries.
     * <p>
     * Each pair is represented as a set of two countries. The order of the
     * countries in the set does not matter.
     */
    private static final Set<Set<Country>> illegalPairs = Set.of(
            Set.of(Country.ARM, Country.AZE),
            Set.of(Country.GIB, Country.ESP),
            Set.of(Country.KOS, Country.BHZ),
            Set.of(Country.KOS, Country.SRB),
            Set.of(Country.UKR, Country.BLR),
            Set.of(Country.UKR, Country.RUS));

    /**
     * Checks if a pair of countries is politically prohibited.
     * 
     * @param country1 the first country
     * @param country2 the second country
     * @return true if the pair is prohibited, false otherwise
     */
    public static boolean isProhibited(Country country1, Country country2) {
        Set<Country> pair = new HashSet<>();
        pair.add(country1);
        pair.add(country2);
        return illegalPairs.contains(pair);
    }

    /**
     * Checks if a match between two club slots is politically prohibited based on
     * the involved
     * countries.
     *
     * @param club1 the first club slot
     * @param club2 the second club slot
     * @return {@code true} if a match between the two club slots is prohibited,
     *         {@code false} otherwise
     */
    public static boolean isProhibited(ClubSlot club1, ClubSlot club2) {
        return club1.getCountries().stream()
                .anyMatch(c1 -> club2.getCountries().stream()
                        .anyMatch(c2 -> isProhibited(c1, c2)));
    }
}
