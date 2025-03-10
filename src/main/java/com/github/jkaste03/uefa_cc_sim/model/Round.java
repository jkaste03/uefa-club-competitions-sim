package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData;
import com.github.jkaste03.uefa_cc_sim.service.ClubEloDataLoader;
import java.util.ArrayList;

/**
 * Abstract class representing a round in the UEFA competitions.
 */
public abstract class Round {
    protected CompetitionData.Tournament tournament;
    protected CompetitionData.RoundType roundType;
    protected Round nextPrimaryRnd;
    protected Round nextSecondaryRnd;
    protected List<ClubSlot> clubSlots = new ArrayList<>();
    protected List<Tie> ties = new ArrayList<>();

    /**
     * Constructor that initializes the round with a tournament and round type.
     * 
     * @param tournament the tournament of the round.
     * @param roundType  the type of the round.
     */
    public Round(CompetitionData.Tournament tournament, CompetitionData.RoundType roundType) {
        this.tournament = tournament;
        this.roundType = roundType;
    }

    public String getName() {
        return tournament + "";
    }

    public CompetitionData.Tournament getTournament() {
        return tournament;
    }

    public CompetitionData.RoundType getRoundType() {
        return roundType;
    }

    public Round getNextPrimaryRnd() {
        return nextPrimaryRnd;
    }

    public Round getNextSecondaryRnd() {
        return nextSecondaryRnd;
    }

    public void setNextRounds(Round nextPrimaryRnd, Round nextSecondaryRnd) {
        this.nextPrimaryRnd = nextPrimaryRnd;
        this.nextSecondaryRnd = nextSecondaryRnd;
    }

    public void setNextRound(Round nextPrimaryRnd) {
        this.nextPrimaryRnd = nextPrimaryRnd;
    }

    public List<ClubSlot> getClubSlots() {
        return clubSlots;
    }

    public void setClubSlots(List<ClubSlot> clubSlots) {
        this.clubSlots = clubSlots;
    }

    public List<Tie> getTies() {
        return ties;
    }

    public void setTies(List<Tie> ties) {
        this.ties = ties;
    }

    /**
     * Adds a club slot to the round.
     * 
     * @param clubSlot the club slot to add.
     */
    public void addClubSlot(ClubSlot clubSlot) {
        clubSlots.add(clubSlot);
    }

    /**
     * Checks if a tie between two club slots is illegal based on political
     * restrictions.
     * 
     * @param clubSlot1 the first club slot.
     * @param clubSlot2 the second club slot.
     * @return true if the tie is illegal, false otherwise.
     */
    protected static boolean isIllegalTie(ClubSlot clubSlot1, ClubSlot clubSlot2) {
        if (!hasCommonCountry(clubSlot1, clubSlot2)) {
            return IllegalTies.isProhibited(clubSlot1, clubSlot2);
        }
        return false;
    }

    /**
     * Checks if two club slots share at least one common country.
     * 
     * @param clubSlot1 the first club slot.
     * @param clubSlot2 the second club slot.
     * @return true if they share at least one common country, false otherwise.
     */
    private static boolean hasCommonCountry(ClubSlot clubSlot1, ClubSlot clubSlot2) {
        return clubSlot1.getCountries().stream().anyMatch(clubSlot2.getCountries()::contains);
    }

    /**
     * Prints the names of the clubs in the provided list of ClubSlot objects.
     *
     * @param clubSlotList the list of ClubSlot objects whose names are to be
     *                     printed
     */
    protected static void printClubSlotList(List<ClubSlot> clubSlotList) {
        clubSlotList.forEach(clubSlot -> System.out.println(clubSlot.getName()));
    }

    /**
     * Updates the club slots in all ties.
     * <p>
     * This method iterates through all the ties and updates the participating
     * club slots by calling the {@link Tie#updateClubSlotsIfTie()} method on each
     * tie. The {@code updateClubSlotsIfTie} method ensures that if a club slot
     * is a wrapper (such as an instance of {@code DoubleLeggedTieWrapper}), it
     * retrieves the appropriate underlying club slot (tie winner or loser) for
     * further use.
     */
    public void updateClubSlotsInTies() {
        for (Tie tie : ties) {
            tie.updateClubSlotsIfTie();
        }
    }

    /**
     * Seeds and draw the ties.
     */
    public void seedDraw() {
        seed();
        draw();
    }

    /**
     * Seeds the clubs for the round.
     * <p>
     * This method is responsible for seeding the clubs in the round. The specific
     * implementation may vary depending on the type of round (e.g., qualifying,
     * league phase).
     */
    protected abstract void seed();

    /**
     * Draws the ties for the round.
     * <p>
     * This method is responsible for drawing the ties for the round. The specific
     * implementation may vary depending on the type of round (e.g., qualifying,
     * league phase).
     */
    protected abstract void draw();

    /**
     * Updates clubSlots by replacing DoubleLeggedTieWrapper instances with their
     * winners, if available. This is only to avoid incorrect printing of clubs that
     * have skipped a round
     */
    protected void updateClubSlotsIfHasOldWinner() {
        clubSlots = clubSlots.stream()
                .map(clubSlot -> clubSlot instanceof DoubleLeggedTieWrapper ? Optional
                        .ofNullable(((DoubleLeggedTieWrapper) clubSlot).getTie().getWinner()).orElse(clubSlot)
                        : clubSlot)
                .collect(Collectors.toList());
    }

    /**
     * Plays the round.
     */
    public abstract void play(ClubEloDataLoader clubEloDataLoader);

    @Override
    public String toString() {
        return "Round [nextPrimaryRnd=" + (nextPrimaryRnd != null ? nextPrimaryRnd.getName() : "null")
                + ", nextSecondaryRnd=" + (nextSecondaryRnd != null ? nextSecondaryRnd.getName() : "null")
                + ", clubSlots=" + clubSlots + ", ties=" + ties + "]";
    }
}