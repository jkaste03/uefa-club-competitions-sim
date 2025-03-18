package com.github.jkaste03.uefa_cc_sim;

import com.github.jkaste03.uefa_cc_sim.model.Rounds;
import java.io.*;

public class UefaCCSim {
    public static void main(String[] args) {
        Rounds rounds = new Rounds();
        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            // Tar en deep copy av rounds-objektet
            Rounds roundsCopy = deepCopy(rounds);
            roundsCopy.run();
            long endTime = System.currentTimeMillis();
            System.out.println("Running the simulation took " + (endTime - startTime) + " milliseconds.");
        }

    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopy(T object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }
}