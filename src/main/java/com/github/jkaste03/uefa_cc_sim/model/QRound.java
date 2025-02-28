package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.Tournament;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class representing a qualifying round in the UEFA competitions.
 */
public class QRound extends Round {
    private final static int UCL_Q1_CP_TIES_NO_REBALANCE = 16;
    private final static String ROUND_CLUBS_SKIP_TO = Tournament.CONFERENCE_LEAGUE + " " + CompetitionData.RoundType.Q3
            + " " + CompetitionData.PathType.CHAMPIONS_PATH;

    private CompetitionData.PathType pathType;
    private List<ClubSlot> seededClubSlots = new ArrayList<>();
    private List<ClubSlot> unseededClubSlots = new ArrayList<>();

    /**
     * Constructs a qualifying round for the specified tournament and round type,
     * using the provided path type to determine the qualifying route. Also adds
     * clubs by reading from a JSON source.
     *
     * @param tournament the tournament for which this qualifying round is
     *                   initialized.
     * @param roundType  the type of the qualifying round.
     * @param pathType   the path type representing the qualifying route.
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

    /**
     * Seeds the clubSlots in the qualifying round.
     * Throws an IllegalArgumentException if the number of clubSlots is odd.
     */
    private void seed() {
        if (clubSlots == null || clubSlots.size() % 2 != 0) {
            throw new IllegalArgumentException("The number of clubSlots must be even to seed them properly.");
        }

        if (getName().equals(ROUND_CLUBS_SKIP_TO)) {
            updateClubSlotsIfHasOldWinner(); // Only to avoid incorrect printing of clubs that have skipped a round
        }

        clubSlots.sort((c1, c2) -> Float.compare(c1.getApplicableRanking(), c2.getApplicableRanking()));
        int halfSize = clubSlots.size() / 2;

        seededClubSlots = clubSlots.subList(0, halfSize);
        unseededClubSlots = clubSlots.subList(halfSize, clubSlots.size());
        System.out.println("\n" + getName() + ", seeded clubs:");
        printClubSlotList(seededClubSlots);
        System.out.println("\n" + getName() + ", unseeded clubs:");
        printClubSlotList(unseededClubSlots);
    }

    private void updateClubSlotsIfHasOldWinner() {
        clubSlots = clubSlots.stream()
                .map(clubSlot -> clubSlot instanceof DoubleLeggedTieWrapper ? Optional
                        .ofNullable(((DoubleLeggedTieWrapper) clubSlot).getDLTie().getWinner()).orElse(clubSlot)
                        : clubSlot)
                .collect(Collectors.toList());
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

    public void registerClubsForLeague() {
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
