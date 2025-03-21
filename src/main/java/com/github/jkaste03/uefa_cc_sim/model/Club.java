package com.github.jkaste03.uefa_cc_sim.model;

import java.util.Objects;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * Class representing a club in the UEFA competitions.
 */
public class Club {
    private static int id_counter = 0;
    private int id;
    private String name;
    private Country country;
    private float ranking;

    // /**
    // * Constructs a Club with the specified name, country, and adjusted UEFA
    // * ranking. The UEFA ranking is adjusted to also include national associations
    // * ranked above the club.
    // * <p>
    // * The club's unique id is auto-assigned.
    // *
    // * @param name the club's name
    // * @param country the club's country
    // * @param ranking the club's adjusted UEFA ranking
    // */
    // public Club(String name, Country country, float ranking) {
    // this.id = id_counter++;
    // this.name = name;
    // this.country = country;
    // this.ranking = ranking;
    // }

    public int getId() {
        return id;
    }

    public void setId() {
        this.id = id_counter++;
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

    public float getRanking() {
        return ranking;
    }

    // Implement equals() based on the unique id of the club
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Club club = (Club) obj;
        return id == club.id; // Two clubs are equal if they have the same id
    }

    // Implement hashCode() based on the id
    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash code is based on the unique id
    }

    @Override
    public String toString() {
        return "Club [id=" + id + ", name=" + name + ", country=" + country + ", ranking=" + ranking + "]";
    }
}