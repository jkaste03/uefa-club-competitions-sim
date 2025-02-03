package com.example.clubs;

import com.example.api.ClubEloAPI;

public class Club {
    private String name;
    private Country country;
    private double eloRating;

    public Club(String name, Country country) {
        this.name = name;
        Double rating = ClubEloAPI.getEloRating(name);
        if (rating == null) {
            System.out.println("Club not found: " + name);
            this.eloRating = 0.0; // or any default value
        } else {
            this.eloRating = rating;
        }
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String navn) {
        this.name = navn;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country land) {
        this.country = land;
    }

    public double getEloRating() {
        return eloRating;
    }

    public void setEloRating(double eloRating) {
        this.eloRating = eloRating;
    }

    @Override
    public String toString() {
        return "Club [name=" + name + ", country=" + country + ", eloRating=" + eloRating + "]";
    }
}