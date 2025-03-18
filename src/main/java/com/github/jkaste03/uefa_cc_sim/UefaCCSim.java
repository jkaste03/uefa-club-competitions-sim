package com.github.jkaste03.uefa_cc_sim;

import com.github.jkaste03.uefa_cc_sim.model.Rounds;
import java.io.*;

/**
 * The UefaCCSim class is the main entry point for running the UEFA competition
 * simulation. It creates an instance of the Rounds class and runs the
 * simulation multiple times (for now to measure performance).
 */
public class UefaCCSim {

    /**
     * The main method that runs the simulation.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create a new instance of Rounds
        Rounds rounds = new Rounds();
        // Run the simulation 10 times to measure performance
        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            // Create a deep copy of the rounds object to reuse the same data without
            // interacting with json
            Rounds roundsCopy = deepCopy(rounds);
            // Run the simulation with the copied rounds object
            roundsCopy.run();
            long endTime = System.currentTimeMillis();
            System.out.println("Running the simulation took " + (endTime - startTime) + " milliseconds.");
        }

    }

    /**
     * Creates a deep copy of the given object using serialization.
     *
     * @param <T>    The type of the object to be copied.
     * @param object The object to be copied.
     * @return A deep copy of the given object.
     * @throws RuntimeException If the deep copy process fails.
     */
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