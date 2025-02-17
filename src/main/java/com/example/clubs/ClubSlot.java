package com.example.clubs;

import java.util.List;

import com.example.enums.Country;

/**
 * Abstract class representing a slot for a club in the UEFA competitions.
 */
public abstract class ClubSlot {
    /**
     * Gets the ranking of the club slot.
     * 
     * @return the ranking of the club slot.
     */
    public abstract float getRanking();

    public abstract List<Country> getCountries();

    public abstract String getName();
}
