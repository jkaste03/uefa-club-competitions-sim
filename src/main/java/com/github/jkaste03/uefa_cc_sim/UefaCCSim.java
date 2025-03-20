package com.github.jkaste03.uefa_cc_sim;

import com.github.jkaste03.uefa_cc_sim.model.Rounds;
import com.github.jkaste03.uefa_cc_sim.threads.SimulationThread;
import java.io.*;

public class UefaCCSim {

    /**
     * The main method that runs the simulation.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create a new instance of Rounds
        Rounds rounds = new Rounds();
        SimulationThread.setRounds(rounds);

        // Determine the number of available processors
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: " + availableProcessors);

        // Set the number of threads to the number of available processors
        int numberOfThreads = availableProcessors;

        // Create an array to hold the threads
        SimulationThread[] threads = new SimulationThread[numberOfThreads];

        // Record the start time
        long startTime = System.currentTimeMillis();

        // Create and start multiple threads for simulation
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new SimulationThread("SimulationThread-" + (i + 1));
            threads[i].start();
            // // Create a deep copy of the rounds object to reuse the same data without
            // // interacting with json
            // Rounds roundsCopy = UefaCCSim.deepCopy(rounds);
            // // Run the simulation with the copied rounds object
            // roundsCopy.run("threadName");
        }

        // Wait for all threads to finish
        for (int i = 0; i < numberOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Record the end time
        long endTime = System.currentTimeMillis();

        // Calculate and print the total time taken
        System.out.println("Total time taken: " + (endTime - startTime) + " milliseconds");
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