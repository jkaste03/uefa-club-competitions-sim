package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;
import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * ClubIdWrapper is a specialized implementation of ClubSlot that encapsulates a
 * club's unique id.
 * <p>
 * This wrapper delegates the retrieval of club details (such as name, ranking,
 * and associated countries)
 * to the Clubs repository using the stored identifier. It provides a convenient
 * abstraction to access a
 * club's properties without holding a direct reference to the Club object.
 * <p>
 * Example usage:
 * 
 * <pre>
 * ClubIdWrapper wrapper = new ClubIdWrapper(5);
 * String clubName = wrapper.getName();
 * float clubRanking = wrapper.getRanking();
 * </pre>
 */
public class ClubIdWrapper extends ClubSlot {
    private int id;

    /**
     * Constructs a ClubIdWrapper with the specified club id.
     *
     * @param id the unique identifier of the club
     */
    public ClubIdWrapper(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return getClub(id).getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves the club corresponding to the stored id from
     * the {@link ClubRepository} repository and returns its ranking. The term
     * "applicable"
     * is not relevant in this subclass implementation.
     *
     * @return the ranking of the club.
     */
    @Override
    public float getRanking() {
        return getClub(id).getRanking();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a singleton list containing the country of
     * the club retrieved from the {@link ClubRepository} repository.
     *
     * @return a list with the club's country.
     */
    @Override
    public List<Country> getCountries() {
        return List.of(getClub(id).getCountry());
    }

    private Club getClub(int id) {
        return ClubRepository.getClub(id);
    }

    @Override
    public String toString() {
        return getClub(id).toString();
    }
}