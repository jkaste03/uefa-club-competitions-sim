package com.github.jkaste03.uefa_cc_sim.clubs;

import java.util.HashMap;
import java.util.Map;

public class Clubs {
    private static Map<Integer, Club> clubs = new HashMap<>();

    public static Club getClub(int id) {
        return clubs.get(id);
    }

    public static void addClub(Club club) {
        clubs.put(club.getId(), club);
    }
}