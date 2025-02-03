package com.example.rounds;

import java.util.List;

import com.example.clubs.Club;

import java.util.ArrayList;

public class QRound extends Round {
    private List<List<Club>> clubs = new ArrayList<>();
    private List<Club> seededClubs;
    private List<Club> unseededClubs;

    public QRound(String name) {
        super(name);
        clubs = new ArrayList<>();
        seededClubs = new ArrayList<>();
        unseededClubs = new ArrayList<>();
        addClubsFromJson();
    }

    @Override
    public void addClub(Club club) {
        clubs.add(new ArrayList<>(List.of(club)));
    }

    public void run() {
        seedClubs();
        drawTies();
        play();
    }

    private void seedClubs() {

    }

    private void drawTies() {

    }

    @Override
    public void play() {
        for (Tie tie : ties) {
            tie.play();
        }
    }

    @Override
    public String toString() {
        return "QRound [name=" + name + ", clubs=" + clubs + ", seededClubs=" + seededClubs + ", unseededClubs="
                + unseededClubs
                + ", nextPrimaryRnd=" + (nextPrimaryRnd != null ? nextPrimaryRnd.getName() : "null")
                + ", nextSecondaryRnd="
                + (nextSecondaryRnd != null ? nextSecondaryRnd.getName() : "null") + ", ties=" + ties + "]";
    }
}
