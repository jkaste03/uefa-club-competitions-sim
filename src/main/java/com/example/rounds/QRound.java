package com.example.rounds;

import java.util.List;

import com.example.clubs.ClubSlot;
import com.example.enums.CompetitionData;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class representing a qualifying round in the UEFA competitions.
 */
public class QRound extends Round {
    private final static int UCL_Q1_CP_TIES_NO_REBALANCE = 16;

    private CompetitionData.PathType pathType;
    private List<ClubSlot> seededClubSlots = new ArrayList<>();
    private List<ClubSlot> unseededClubSlots = new ArrayList<>();

    /**
     * Constructor that initializes the qualifying round with a name and adds clubs
     * from JSON.
     * 
     * @param name the name of the qualifying round.
     */
    public QRound(CompetitionData.Tournament tournament, CompetitionData.RoundType roundType,
            CompetitionData.PathType pathType) {
        super(tournament, roundType);
        this.pathType = pathType;
        addClubsFromJson();
    }

    @Override
    public String getName() {
        return super.getName() + " " + pathType;
    }

    /**
     * Runs the qualifying round by seeding clubs, drawing ties, and playing the
     * ties.
     */
    public void trySeedDraw() {
        if (ties.isEmpty()) {
            seed();
            draw();
        }
    }

    public void seedDrawNextIfQRound() {
        trySeedDraw();
    }

    /**
     * Seeds the clubSlots in the qualifying round.
     * Throws an IllegalArgumentException if the number of clubSlots is odd.
     */
    private void seed() {
        if (clubSlots == null || clubSlots.size() % 2 != 0) {
            throw new IllegalArgumentException("The number of clubSlots must be even to seed them properly.");
        }

        clubSlots.sort((club1, club2) -> Float.compare(club1.getRanking(), club2.getRanking()));
        int halfSize = clubSlots.size() / 2;

        seededClubSlots = clubSlots.subList(0, halfSize);
        unseededClubSlots = clubSlots.subList(halfSize, clubSlots.size());
        System.out.println("\n" + getName() + ", seeded clubs:");
        printClubSlotList(seededClubSlots);
        System.out.println("\n" + getName() + ", unseeded clubs:");
        printClubSlotList(unseededClubSlots);
    }

    /**
     * Draws the ties for the qualifying round.
     * Ensures that clubs from the same country do not meet.
     * First, it pairs seeded clubs that have at least one club from the same
     * country among the unseeded.
     * Then, it pairs the remaining seeded clubs with the remaining unseeded clubs.
     */
    private void draw() {
        List<ClubSlot> remainingSeeded = new ArrayList<>(seededClubSlots);
        List<ClubSlot> remainingUnseeded = new ArrayList<>(unseededClubSlots);
        ties.clear();

        // First, draw opponents for seeded clubs that have at least one club from the
        // same country among the unseeded
        seededClubSlots.stream()
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

        System.out.println("\n" + getName() + ", ties:");
        ties.forEach(tie -> System.out.println(tie.getName()));
    }

    public void regTiesForNextRounds() {
        shuffleTiesIfUCLQ1CP();
        for (int i = 0; i < ties.size(); i++) {
            updateClubsFromTieIfClubIsTie((DoubleLeggedTie) ties.get(i));
            this.nextPrimaryRnd.addClubSlot(new DoubleLeggedTieWrapper((DoubleLeggedTie) ties.get(i), false));
            if (this.nextSecondaryRnd != null) {
                if (tieCanSkipSecondaryRound(i)) {
                    this.nextSecondaryRnd.getNextPrimaryRnd()
                            .addClubSlot(new DoubleLeggedTieWrapper((DoubleLeggedTie) ties.get(i), true));
                } else {
                    this.nextSecondaryRnd.addClubSlot(new DoubleLeggedTieWrapper((DoubleLeggedTie) ties.get(i), true));
                }
            }
        }
    }

    private void shuffleTiesIfUCLQ1CP() {
        if (ifUCLQ1CP()) {
            Collections.shuffle(ties);
        }
    }

    private boolean ifUCLQ1CP() {
        return (tournament == CompetitionData.Tournament.CHAMPIONS_LEAGUE && roundType == CompetitionData.RoundType.Q1
                && pathType == CompetitionData.PathType.CHAMPIONS_PATH);
    }

    private boolean tieCanSkipSecondaryRound(int i) {
        return ifUCLQ1CP() && i < UCL_Q1_CP_TIES_NO_REBALANCE - ties.size();
    }

    private void updateClubsFromTieIfClubIsTie(DoubleLeggedTie tie) {
        if (tie.getClubSlot1() instanceof DoubleLeggedTieWrapper) {
            tie.setClubSlot1(((DoubleLeggedTieWrapper) (tie.getClubSlot1())).getCorrectClubSlot());
        }
        if (tie.getClubSlot2() instanceof DoubleLeggedTieWrapper) {
            tie.setClubSlot2(((DoubleLeggedTieWrapper) (tie.getClubSlot2())).getCorrectClubSlot());
        }
    }

    public void registerTieClubsForLeague() {
        for (Tie tie : ties) {
            this.nextPrimaryRnd.addClubSlot(tie.getWinner());
            if (this.nextSecondaryRnd != null) {
                this.nextSecondaryRnd.addClubSlot(tie.getLoser());
            }
        }
    }

    /**
     * Plays the ties in the qualifying round.
     */
    @Override
    public void play() {
        System.out.println("\n" + getName());
        for (Tie tie : ties) {
            tie.play();
        }
    }

    @Override
    public String toString() {
        return "QRound [name=" + getName() + ", toString()=" + super.toString() + ", seededClubs="
                + seededClubSlots + ", unseededClubs=" + unseededClubSlots + "]";
    }
}
