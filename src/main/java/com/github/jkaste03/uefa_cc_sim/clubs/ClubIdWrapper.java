package com.github.jkaste03.uefa_cc_sim.clubs;

import java.util.List;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

public class ClubIdWrapper extends ClubSlot {
    private int id;

    public ClubIdWrapper(int id) {
        this.id = id;
    }

    public Club getClub(int id) {
        return Clubs.getClub(id);
    }

    public String getName() {
        return getClub(id).getName();
    }

    @Override
    public float getRanking() {
        return getClub(id).getRanking();
    }

    @Override
    public List<Country> getCountries() {
        return getClub(id).getCountries();
    }

    @Override
    public String toString() {
        return getClub(id).toString();
    }
}
