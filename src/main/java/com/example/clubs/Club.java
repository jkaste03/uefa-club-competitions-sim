package com.example.clubs;

import java.util.List;

import com.example.api.ClubEloAPI;
import com.example.enums.Country;

/**
 * Class representing a club in the UEFA competitions.
 */
public class Club {
    private static int id_counter = 0;
    private int id;
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
        this.id = id_counter++;
        this.name = name;
        this.country = country;
        this.ranking = ranking;
        this.eloRating = ClubEloAPI.getEloRating(name);
        if (this.eloRating == 0.0) {
            System.out.println("Club not found: " + name);
        }
        Clubs.addClub(this);
    }

    public int getId() {
        return id;
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

    public float getRanking() {
        return ranking;
    }

    public List<Country> getCountries() {
        return List.of(country);
    }

    @Override
    public String toString() {
        return "Club [name=" + name + ", country=" + country + ", ranking=" + ranking + ", eloRating=" + eloRating
                + "]";
    }
}