package com.github.jkaste03.uefa_cc_sim;

import com.github.jkaste03.uefa_cc_sim.model.Rounds;

public class UefaCCSim {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Rounds rounds = new Rounds();
        rounds.run();
        long endTime = System.currentTimeMillis();
        System.out.println("Seeding and drawing league rounds took " + (endTime - startTime) + " milliseconds.");
    }
}