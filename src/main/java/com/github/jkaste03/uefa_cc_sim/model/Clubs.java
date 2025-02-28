package com.github.jkaste03.uefa_cc_sim.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class serves as a repository for managing club instances in the UEFA
 * competition simulations. It provides methods to store and retrieve clubs
 * based on a unique identifier. All clubs are stored in a static map, ensuring
 * consistent access throughout the application.
 */
public class Clubs {
    private static Map<Integer, Club> clubs = new HashMap<>();

    public static Club getClub(int id) {
        return clubs.get(id);
    }

    public static void addClub(Club club) {
        clubs.put(club.getId(), club);
    }
}