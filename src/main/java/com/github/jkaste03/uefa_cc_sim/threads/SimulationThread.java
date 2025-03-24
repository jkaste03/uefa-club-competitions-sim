package com.github.jkaste03.uefa_cc_sim.threads;

import com.github.jkaste03.uefa_cc_sim.UefaCCSim;
import com.github.jkaste03.uefa_cc_sim.model.Rounds;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The SimulationThread class extends the Thread class to run a simulation
 * multiple times in a separate thread. It uses a deep copy of the rounds
 * object to ensure that the same data can be reused without interacting
 * with JSON.
 *
 * <p>
 * This class is designed to measure the performance of the simulation
 * by running it multiple times.
 */
public class SimulationThread extends Thread {
    private static Rounds rounds;
    private static AtomicInteger totalIterations = new AtomicInteger(0);
    private static final int MAX_ITERATIONS = 100;

    /**
     * Default constructor for the SimulationThread class.
     * It sets the thread name.
     */
    public SimulationThread(String name) {
        super(name);
    }

    /**
     * The run method is overridden to perform the simulation in a separate thread.
     * It creates a deep copy of the rounds object and runs the simulation with it.
     */
    @Override
    public void run() {
        // Get the name of the current thread
        String threadName = Thread.currentThread().getName();

        // Run the simulation until the total iterations reach MAX_ITERATIONS
        while (totalIterations.getAndIncrement() < MAX_ITERATIONS) {
            // Create a deep copy of the rounds object to reuse the same data without
            // interacting with json
            Rounds roundsCopy = UefaCCSim.deepCopy(rounds);
            // Run the simulation with the copied rounds object
            roundsCopy.run(threadName);
            // System.out.println("Thread " + threadName + " completed iteration " +
            // totalIterations.get());

            if (totalIterations.get() >= MAX_ITERATIONS) {
                break;
            }
        }
    }

    /**
     * Sets the rounds object to be used in the simulation thread.
     *
     * @param rounds The rounds object to be set.
     */
    public static void setRounds(Rounds rounds) {
        SimulationThread.rounds = rounds;
    }
}