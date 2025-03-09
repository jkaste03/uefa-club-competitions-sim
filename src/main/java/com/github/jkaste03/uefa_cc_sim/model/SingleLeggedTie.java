package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * SingleLeggedTie is a specialized implementation of the Tie class that
 * represents a single-legged tie between two clubs.
 * <p>
 * This class extends the abstract Tie class and implements the specific
 * behavior for a single-legged tie, including score calculation and determining
 * the winner.
 */
public class SingleLeggedTie extends Tie {

    /*
     * * Constructs a new single-legged tie with the specified club slots.
     */
    public SingleLeggedTie(ClubSlot club1, ClubSlot club2) {
        super(club1, club2);
        // TODO Auto-generated constructor stub
    }

    @Override
    public float getRanking() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRanking'");
    }

    @Override
    public List<Country> getCountries() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCountries'");
    }

    @Override
    public void play() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'play'");
    }
}
