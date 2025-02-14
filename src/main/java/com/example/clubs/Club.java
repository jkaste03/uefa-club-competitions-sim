package com.example.clubs;

import com.example.api.ClubEloAPI;

/**
 * Class representing a club in the UEFA competitions.
 */
public class Club extends ClubSlot {
    private String name;
    private Country country;
    private float ranking;
    private double eloRating;

    /**
     * Constructor that initializes the club with a name, country and uefa ranking,
     * and fetches
     * its Elo rating.
     * 
     * @param name    the name of the club.
     * @param country the country of the club.
     * @param ranking the uefa ranking of the club.
     */
    public Club(String name, Country country, float ranking) {
        this.name = name;
        this.country = country;
        this.ranking = ranking;
        this.eloRating = ClubEloAPI.getEloRating(name);
        if (this.eloRating == 0.0) {
            System.out.println("Club not found: " + name);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public double getEloRating() {
        return eloRating;
    }

    public void setEloRating(double eloRating) {
        this.eloRating = eloRating;
    }

    @Override
    public float getRanking() {
        return ranking;
    }

    @Override
    public String toString() {
        return "Club [name=" + name + ", country=" + country + ", ranking=" + ranking + ", eloRating=" + eloRating
                + "]";
    }
}