package com.example.rounds;

import com.example.clubs.ClubSlot;

public abstract class Tie extends ClubSlot {
    protected ClubSlot clubSlot1;
    protected ClubSlot clubSlot2;
    protected ClubSlot winner;

    public Tie(ClubSlot club1, ClubSlot club2) {
        this.clubSlot1 = club1;
        this.clubSlot2 = club2;
    }

    public ClubSlot getWinner() {
        return winner;
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

    public abstract void play();

    protected int[] genScoreline() {
        int club1Goals = (int) (Math.random() * 4);
        int club2Goals = (int) (Math.random() * 4);
        return new int[] { club1Goals, club2Goals };
    }
}