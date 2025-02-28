package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

public class SingleLeggedTie extends Tie {

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
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public void play() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'play'");
    }
}
