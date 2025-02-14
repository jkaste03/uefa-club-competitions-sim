package com.example.rounds;

import java.util.List;

import com.example.clubs.ClubSlot;

import java.util.ArrayList;

/**
 * Class representing a qualifying round in the UEFA competitions.
 */
public class QRound extends Round {
    private List<ClubSlot> seededClubs = new ArrayList<>();
    private List<ClubSlot> unseededClubs = new ArrayList<>();

    /**
     * Constructor that initializes the qualifying round with a name and adds clubs
     * from JSON.
     * 
     * @param name the name of the qualifying round.
     */
    public QRound(String name) {
        super(name);
        addClubsFromJson();
    }

    /**
     * Runs the qualifying round by seeding clubs, drawing ties, and playing the
     * ties.
     */
    public void run() {
        seedClubs();
        drawTies();
        // if (nextPrimaryRnd instanceof QRound) {

        // }
        // play();
        System.out.println(seededClubs);
        System.out.println(unseededClubs);
    }

    /**
     * Seeds the clubSlots in the qualifying round.
     * Throws an IllegalArgumentException if the number of clubSlots is odd.
     */
    public void seedClubs() {
        if (clubSlots == null || clubSlots.size() % 2 != 0) {
            throw new IllegalArgumentException("The number of clubSlots must be even to seed them properly.");
        }

        clubSlots.sort((club1, club2) -> Float.compare(club1.getRanking(), club2.getRanking()));
        int halfSize = clubSlots.size() / 2;

        seededClubs = clubSlots.subList(0, halfSize);
        unseededClubs = clubSlots.subList(halfSize, clubSlots.size());
    }

    /**
     * Draws the ties for the qualifying round.
     * Ensures that clubs from the same country do not meet.
     * First, it pairs seeded clubs that have at least one club from the same
     * country among the unseeded.
     * Then, it pairs the remaining seeded clubs with the remaining unseeded clubs.
     */
    private void drawTies() {
        List<ClubSlot> remainingSeeded = new ArrayList<>(seededClubs);
        List<ClubSlot> remainingUnseeded = new ArrayList<>(unseededClubs);
        ties.clear();

        // First, draw opponents for seeded clubs that have at least one club from the
        // same country among the unseeded
        seededClubs.stream()
                .filter(seeded -> remainingUnseeded.stream().anyMatch(unseeded -> isIllegalTie(seeded, unseeded)))
                .forEach(seeded -> {
                    ClubSlot opponent;
                    do {
                        opponent = remainingUnseeded.get((int) (Math.random() * remainingUnseeded.size()));
                    } while (isIllegalTie(seeded, opponent));
                    remainingSeeded.remove(seeded);
                    remainingUnseeded.remove(opponent);
                    ties.add(Math.random() < 0.5 ? new DoubleLeggedTie(seeded, opponent)
                            : new DoubleLeggedTie(opponent, seeded));
                });

        // Then, draw opponents for the remaining seeded clubs
        remainingSeeded.forEach(seeded -> {
            ClubSlot opponent = remainingUnseeded.remove((int) (Math.random() * remainingUnseeded.size()));
            ties.add(Math.random() < 0.5 ? new DoubleLeggedTie(seeded, opponent)
                    : new DoubleLeggedTie(opponent, seeded));
        });
    }

    /**
     * Plays the ties in the qualifying round.
     */
    @Override
    public void play() {
        for (Tie tie : ties) {
            tie.play();
        }
    }

    @Override
    public String toString() {
        return "QRound [name=" + name + ", clubSlots=" + clubSlots + ", seededClubs=" + seededClubs + ", unseededClubs="
                + unseededClubs
                + ", nextPrimaryRnd=" + (nextPrimaryRnd != null ? nextPrimaryRnd.getName() : "null")
                + ", nextSecondaryRnd="
                + (nextSecondaryRnd != null ? nextSecondaryRnd.getName() : "null") + ", ties=" + ties + "]";
    }
}
