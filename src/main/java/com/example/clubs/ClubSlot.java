package com.example.clubs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<Country> getCountries() {
        return this instanceof Club ? List.of(((Club) this).getCountry())
                : Stream.concat(
                        ((UndecidedClub) this).getClubSlot1().getCountries().stream(),
                        ((UndecidedClub) this).getClubSlot2().getCountries().stream())
                        .collect(Collectors.toList());
    }
}
