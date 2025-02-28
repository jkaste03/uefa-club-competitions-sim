package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * DoubleLeggedTieWrapper is a specialized implementation of ClubSlot that
 * encapsulates a double-legged tie.
 * <p>
 * This wrapper delegates the retrieval of club details of the tie (such as
 * name, ranking, and associated countries) to the underlying DoubleLeggedTie.
 * It provides a convenient abstraction to access a tie's properties while also
 * having a flag that indicates which ranking should be used for seeding.
 */
public class DoubleLeggedTieWrapper extends ClubSlot {
    private DoubleLeggedTie tie;
    private boolean worstRankForSeeding;

    /**
     * Constructs a DoubleLeggedTieWrapper with the specified double-legged tie and
     * seeding flag.
     *
     * @param tie                 the double-legged tie to be wrapped.
     * @param worstRankForSeeding a flag indicating whether the worst ranking should
     *                            be used for seeding.
     */
    public DoubleLeggedTieWrapper(DoubleLeggedTie tie, boolean worstRankForSeeding) {
        this.tie = tie;
        this.worstRankForSeeding = worstRankForSeeding;
    }

    public Tie getTie() {
        return tie;
    }

    public void setTie(DoubleLeggedTie tie) {
        this.tie = tie;
    }

    public boolean isWorstRankForSeeding() {
        return worstRankForSeeding;
    }

    public void setWorstRankForSeeding(boolean worstRankForSeeding) {
        this.worstRankForSeeding = worstRankForSeeding;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the name of the underlying double-legged tie appended with either
     * " (loser of)" or " (winner of)" depending on the "worstRankForSeeding" flag.
     */
    @Override
    public String getName() {
        return tie.getName() + (worstRankForSeeding ? " (loser of)" : " (winner of)");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves the list of countries associated with the
     * clubs in the underlying double-legged tie.
     *
     * @return a list of the countries associated with the clubs in the tie.
     */
    @Override
    public List<Country> getCountries() {
        return tie.getCountries();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the ranking of the club from the tie that determines the seeding.
     */
    @Override
    public float getRanking() {
        return tie.getRanking(worstRankForSeeding);
    }

    /**
     * Returns the appropriate ClubSlot based on the seeding criteria.
     * <p>
     * If {@code worstRankForSeeding} is {@code true}, this method returns the loser
     * of the tie; otherwise, it returns the winner.
     *
     * @return the ClubSlot representing the correct club for seeding.
     */
    public ClubSlot getCorrectClub() {
        return worstRankForSeeding ? tie.getLoser() : tie.getWinner();
    }

    @Override
    public String toString() {
        return "DoubleLeggedTieWrapper [dLTie=" + tie + ", worstRankForSeeding=" + worstRankForSeeding + "]";
    }
}