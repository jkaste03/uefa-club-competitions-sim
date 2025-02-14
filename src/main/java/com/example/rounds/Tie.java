package com.example.rounds;

import com.example.clubs.ClubSlot;

public abstract class Tie {
    protected ClubSlot club1;
    protected ClubSlot club2;

    public Tie(ClubSlot club1, ClubSlot club2) {
        this.club1 = club1;
        this.club2 = club2;
    }

    public abstract boolean play();

    protected int[] genScoreline() {
        int club1Goals = (int) (Math.random() * 4);
        int club2Goals = (int) (Math.random() * 4);
        return new int[] { club1Goals, club2Goals };
    }
}