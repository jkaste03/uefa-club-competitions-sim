package com.github.jkaste03.uefa_cc_sim.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class serves as a repository for managing club instances in the UEFA
 * competition simulations. It provides methods to store and retrieve clubs
 * based on a unique identifier. All clubs are stored in a static map, ensuring
 * consistent access throughout the application.
 * <p>
 * The ClubRepository class functions as the storage location where all
 * simulation data produced through the simulations is stored for all clubs.
 */
public class ClubRepository {
    /*
     * The static map of clubs, where the key is the club's id and the value is the
     * Club instance.
     */
    private static Map<Integer, Club> clubs = new HashMap<>();

    /*
     * The name of the club that won the last UEFA Champions League. This is needed
     * decide the UCL seeding pots.
     */
    public static String lastUclWinnerName;

    /*
     * Retrieves the club with the specified id from the static map of clubs.
     */
    public static Club getClub(int id) {
        return clubs.get(id);
    }

    public static String getLastUclWinnerName() {
        return lastUclWinnerName;
    }

    public static void setLastUclWinnerName(String name) {
        lastUclWinnerName = name;
    }

    /*
     ** Retrieves a club's id by its name from the static map of clubs.
     */
    public static int getIdByName(String name) {
        return clubs.entrySet().stream()
                .filter(entry -> entry.getValue().getName().equals(name))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1); // Return -1 if the club is not found
    }

    /*
     * Adds a club to the static map of clubs. This method is used to populate the
     * repository with club data, which can be used for simulations and other
     */
    public static void addClub(Club club) {
        clubs.put(club.getId(), club);
    }
}