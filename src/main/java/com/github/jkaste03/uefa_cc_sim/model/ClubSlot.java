package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * Abstract representation of a club slot in the UEFA competition simulations.
 * This class abstracts the concept of a club's participation slot, providing
 * the essential details (ranking, associated countries, and name) required in
 * different competition contexts. Implementations will define how these
 * properties
 * are determined for various club slot types.
 */
public abstract class ClubSlot {
    /*
     * Retrieves the name associated with this club slot.
     * For individual clubs, this returns the club's name.
     * In the case where the slot represents a tie (involving multiple clubs),
     * a composite name (e.g. "club1 vs club2") is returned.
     */
    public abstract String getName();

    /*
     * Retrieves the list of countries associated with this club slot.
     * In cases where the club slot represents a tie (involving multiple clubs),
     * all relevant countries are returned.
     */
    public abstract List<Country> getCountries();

    /*
     * Retrieves the relevant ranking for the club slot.
     * In cases where the club slot represents a tie (involving multiple clubs),
     * only the ranking of the club that determines the seeding is considered.
     */
    public abstract float getRanking();
}