package com.github.jkaste03.uefa_cc_sim.rounds;

import com.github.jkaste03.uefa_cc_sim.clubs.ClubSlot;

public abstract class Tie extends ClubSlot {
    protected ClubSlot clubSlot1;
    protected ClubSlot clubSlot2;
    protected ClubSlot winner;
    protected int club1Goals = -1;
    protected int club2Goals = -1;

    public Tie(ClubSlot club1, ClubSlot club2) {
        this.clubSlot1 = club1;
        this.clubSlot2 = club2;
    }

    public ClubSlot getClubSlot1() {
        return clubSlot1;
    }

    public void setClubSlot1(ClubSlot club1) {
        this.clubSlot1 = club1;
    }

    public void setClubSlot2(ClubSlot club2) {
        this.clubSlot2 = club2;
    }

    public ClubSlot getClubSlot2() {
        return clubSlot2;
    }

    public ClubSlot getWinner() {
        return winner;
    }

    public ClubSlot getLoser() {
        return winner == clubSlot1 ? clubSlot2 : clubSlot1;
    }

    public abstract void play();

    protected int[] genScoreline() {
        int club1Goals = (int) (Math.random() * 4);
        int club2Goals = (int) (Math.random() * 4);
        return new int[] { club1Goals, club2Goals };
    }

    public void updateClubSlotsIfTie() {
        if (clubSlot1 instanceof DoubleLeggedTieWrapper) {
            clubSlot1 = ((DoubleLeggedTieWrapper) clubSlot1).getCorrectClub();
        }
        if (clubSlot2 instanceof DoubleLeggedTieWrapper) {
            clubSlot2 = ((DoubleLeggedTieWrapper) clubSlot2).getCorrectClub();
        }
    }

    @Override
    public String toString() {
        return "Tie [clubSlot1=" + clubSlot1 + ", clubSlot2=" + clubSlot2 + ", winner=" + winner.getName()
                + ", club1Goals=" + club1Goals + ", club2Goals=" + club2Goals + "]";
    }
}