package com.github.jkaste03.uefa_cc_sim.model;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jkaste03.uefa_cc_sim.enums.Country;

/**
 * Represents a double-legged tie between two clubs.
 * <p>
 * This class extends the abstract {@link Tie} class and implements the specific
 * behavior for a double-legged tie, including score calculation and determining
 * the winner over two legs.
 */
public class DoubleLeggedTie extends Tie {
    private int club1GoalsLeg1 = -1;
    private int club2GoalsLeg1 = -1;

    /**
     * Constructs a new double-legged tie with the specified club slots.
     *
     * @param club1 the first club slot participating in the tie.
     * @param club2 the second club slot participating in the tie.
     */
    public DoubleLeggedTie(ClubSlot club1, ClubSlot club2) {
        super(club1, club2);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation simulates the double-legged tie by generating random
     * scores for each leg and determining the winner based on the aggregate score.
     */
    @Override
    public void play() {
        if (club1GoalsLeg1 == -1) {
            int[] results1 = genScoreline();
            club1GoalsLeg1 = results1[0];
            club2GoalsLeg1 = results1[1];

            System.out.println(getScorelineLeg1());
        } else {
            int[] results2 = genScoreline();
            club1Goals = club1GoalsLeg1 + results2[0];
            club2Goals = club2GoalsLeg1 + results2[1];

            genWinner();

            System.out.println(getScoreline());
        }
    }

    /**
     * Returns the name of the tie.
     * <p>
     * This implementation returns a string in the format "club1 vs club2",
     * where "club1" and "club2" are the names of the participating clubs.
     *
     * @return the name of the tie.
     */
    @Override
    public String getName() {
        return clubSlot1.getName() + " vs " + clubSlot2.getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves the list of countries associated with the
     * clubs in the underlying double-legged tie.
     *
     * @return a list of the countries associated with the clubs in the tie.
     */
    @Override
    public List<Country> getCountries() {
        return Stream.concat(
                clubSlot1.getCountries().stream(),
                clubSlot2.getCountries().stream())
                .collect(Collectors.toList());
    }

    @Override
    public float getRanking() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRanking'");
    }

    /**
     * Generates the scoreline for the first leg of the tie.
     *
     * @return a string representing the scoreline of the first leg.
     */
    private String getScorelineLeg1() {
        return clubSlot1.getName() + " " + club1GoalsLeg1 + " - " + club2GoalsLeg1 + " " + clubSlot2.getName()
                + ". First leg played.";
    }

    /**
     * Generates the scoreline for the entire tie.
     *
     * @return a string representing the scoreline of the tie.
     */
    private String getScoreline() {
        return clubSlot2.getName() + " " + club2Goals + " (" + (club2Goals - club2GoalsLeg1) + ") - ("
                + (club1Goals - club1GoalsLeg1) + ") " + club1Goals + " " + clubSlot1.getName() + ". Winner: "
                + winner.getName();
    }

    /**
     * Retrieves the ranking of the club based on the rankings of its associated
     * clubs.
     * If the `worstRankForSeeding` flag is true, it returns the worst ranking among
     * the clubs. Otherwise, it returns the best ranking among the clubs.
     * If there are no clubs, it returns 0.
     *
     * @param worstRankForSeeding a flag indicating whether to return the worst
     *                            ranking for seeding.
     * @return the ranking of the club as a float.
     */
    public float getRanking(boolean worstRankForSeeding) {
        return worstRankForSeeding ? Math.max(clubSlot1.getRanking(), clubSlot2.getRanking())
                : Math.min(clubSlot1.getRanking(), clubSlot2.getRanking());
    }

    /**
     * Determines the winner of the tie based on the total goals scored by each
     * club. If total goals are equal, a random winner is chosen (penalty shootout).
     */
    private void genWinner() {
        boolean club1Wins = club1Goals > club2Goals ||
                (club1Goals == club2Goals && new Random().nextBoolean());

        this.winner = club1Wins ? (ClubIdWrapper) clubSlot1 : (ClubIdWrapper) clubSlot2;
    }

    @Override
    public String toString() {
        return "DoubleLeggedTie [toString()=" + super.toString() + ", club1GoalsLeg1=" + club1GoalsLeg1
                + ", club2GoalsLeg1=" + club2GoalsLeg1 + "]";
    }
}
