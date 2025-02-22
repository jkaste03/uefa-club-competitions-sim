package com.example.rounds;

import com.example.clubs.ClubIdWrapper;
import com.example.clubs.ClubSlot;
import com.example.enums.Country;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DoubleLeggedTie extends Tie {
    private int club1GoalsLeg1 = -1;
    private int club2GoalsLeg1 = -1;

    public DoubleLeggedTie(ClubSlot club1, ClubSlot club2) {
        super(club1, club2);
    }

    public void play() {
        if (club1GoalsLeg1 == -1) {
            updateClubSlotsIfTie();
            int[] results1 = genScoreline();
            club1GoalsLeg1 = results1[0];
            club2GoalsLeg1 = results1[1];

            System.out.println(getScorelineLeg1());
        } else {
            int[] results2 = genScoreline();
            club1Goals = club1GoalsLeg1 + results2[0];
            club2Goals = club2GoalsLeg1 + results2[1];

            genWinner(club1Goals, club2Goals);

            System.out.println(getScoreline());
        }
    }

    private String getScorelineLeg1() {
        return clubSlot1.getName() + " " + club1GoalsLeg1 + " - " + club2GoalsLeg1 + " " + clubSlot2.getName()
                + ". First leg played.";
    }

    private String getScoreline() {
        return clubSlot2.getName() + " " + club2Goals + " (" + (club2Goals - club2GoalsLeg1) + ") - ("
                + (club1Goals - club1GoalsLeg1) + ") " + club1Goals + " " + clubSlot1.getName() + ". Winner: "
                + winner.getName();
    }

    private void updateClubSlotsIfTie() {
        if (clubSlot1 instanceof DoubleLeggedTieWrapper) {
            DoubleLeggedTieWrapper clubSlot1DLTieWrapper = ((DoubleLeggedTieWrapper) clubSlot1);
            clubSlot1 = clubSlot1DLTieWrapper.getCorrectClubSlot();
        }
        if (clubSlot2 instanceof DoubleLeggedTieWrapper) {
            DoubleLeggedTieWrapper clubSlot2DLTieWrapper = ((DoubleLeggedTieWrapper) clubSlot2);
            clubSlot1 = clubSlot2DLTieWrapper.getCorrectClubSlot();
        }
    }

    /**
     * Retrieves the ranking of the UndecidedClub based on the rankings of its
     * associated clubs.
     * If the `worstRankForSeeding` flag is true, it returns the worst ranking among
     * the clubs.
     * Otherwise, it returns the best ranking among the clubs.
     * If there are no clubs, it returns 0.
     *
     * @return the ranking of the club as a float.
     */
    public float getRanking(boolean worstRankForSeeding) {
        return worstRankForSeeding ? Math.max(clubSlot1.getRanking(), clubSlot2.getRanking())
                : Math.min(clubSlot1.getRanking(), clubSlot2.getRanking());
    }

    public List<Country> getCountries() {
        return Stream.concat(
                clubSlot1.getCountries().stream(),
                clubSlot2.getCountries().stream())
                .collect(Collectors.toList());
    }

    private void genWinner(int totalClub1Goals, int totalClub2Goals) {
        boolean club1Wins = totalClub1Goals > totalClub2Goals ||
                (totalClub1Goals == totalClub2Goals && new Random().nextBoolean());

        this.winner = club1Wins ? (ClubIdWrapper) clubSlot1 : (ClubIdWrapper) clubSlot2;
    }

    @Override
    public float getRanking() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRanking'");
    }

    @Override
    public String getName() {
        return clubSlot1.getName() + " vs " + clubSlot2.getName();
    }

    @Override
    public String toString() {
        return "DoubleLeggedTie [toString()=" + super.toString() + "club1GoalsLeg1=" + club1GoalsLeg1
                + ", club2GoalsLeg1=" + club2GoalsLeg1 + "]";
    }
}
