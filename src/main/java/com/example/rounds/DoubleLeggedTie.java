package com.example.rounds;

import com.example.clubs.Club;
import com.example.clubs.ClubSlot;
import java.util.Random;

public class DoubleLeggedTie extends Tie {
    private int club1Goals1;
    private int club2Goals1;
    private int club1Goals2;
    private int club2Goals2;
    protected Club winner;

    public DoubleLeggedTie(ClubSlot club1, ClubSlot club2) {
        super(club1, club2);
    }

    public boolean play() {
        int[] results1 = genScoreline();
        int[] results2 = genScoreline();
        club1Goals1 = results1[0];
        club2Goals1 = results1[1];
        club1Goals2 = results2[0];
        club2Goals2 = results2[1];

        genWinner(club1Goals1 + club1Goals2, club2Goals1 + club2Goals2);

        System.out.println(club1 + " " + (club1Goals1 + club1Goals2) + " (" + results1[0] + ") - (" + results1[1] + ") "
                + (club2Goals1 + club2Goals2) + " " + club2 + ". Winner: " + winner);

        return winner == club1;
    }

    private void genWinner(int totalClub1Goals, int totalClub2Goals) {
        boolean club1Wins = totalClub1Goals > totalClub2Goals ||
                (totalClub1Goals == totalClub2Goals && new Random().nextBoolean());

        this.winner = club1Wins ? (Club) club1 : (Club) club2;
    }
}
