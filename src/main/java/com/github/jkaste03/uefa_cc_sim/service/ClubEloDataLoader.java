package com.github.jkaste03.uefa_cc_sim.service;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/**
 * ClubEloService provides functionality to retrieve and manage Elo ratings for
 * football clubs.
 * <p>
 * This service acts as a centralized utility to obtain Elo ratings, which are
 * used to gauge
 * the strength of clubs in simulations and competitions. The ratings may be
 * fetched from external
 * data sources or local caches depending on the implementation.
 * <p>
 * Usage example:
 * 
 * <pre>
 * double eloRating = ClubEloService.getEloRating("FC Example");
 * </pre>
 * <p>
 * If a club's Elo rating is not found, the service returns 0.0 by default.
 */
public class ClubEloDataLoader {
    private static final String BASE_URL = "http://api.clubelo.com/";
    private static final String DATA_FOLDER = "src/main/java/com/github/jkaste03/uefa_cc_sim/data/";
    private static final Map<String, Double> eloMap = new HashMap<>();
    private static String filePath;

    /**
     * Constructor that initializes the ClubEloAPI by downloading the latest data if
     * not already present and loading Elo ratings.
     */
    public ClubEloDataLoader() {
        LocalDate today = LocalDate.now();
        filePath = DATA_FOLDER + today + ".csv";

        // Download file if it does not exist
        if (!Files.exists(Path.of(filePath))) {
            deleteExistingCSVFiles();
            downloadCSV(today);
        }

        loadEloRatings();
    }

    /**
     * Deletes all existing CSV files in the data package.
     */
    private void deleteExistingCSVFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(DATA_FOLDER), "*.csv")) {
            for (Path entry : stream) {
                Files.delete(entry);
            }
            System.out.println("Deleted old API data");
        } catch (IOException e) {
            System.err.println("Could not delete existing CSV files: " + e.getMessage());
        }
    }

    /**
     * Downloads the CSV file for the given date from the API.
     * 
     * @param date the date for which to download the CSV file.
     */
    private void downloadCSV(LocalDate date) {
        String urlString = BASE_URL + date;
        try (InputStream in = new URI(urlString).toURL().openStream()) {
            Files.createDirectories(Path.of(DATA_FOLDER));
            Files.copy(in, Path.of(filePath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Downloaded API data for " + date);
        } catch (Exception e) {
            System.err.println("Could not download API data from " + urlString + ": " + e.getMessage());
        }
    }

    /**
     * Loads Elo ratings from the CSV file into memory.
     */
    private void loadEloRatings() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 5)
                    continue;
                String club = values[1].trim();
                double elo = Double.parseDouble(values[4].trim());

                eloMap.put(club, elo);
            }
        } catch (IOException e) {
            System.err.println("Could not read API data: " + e.getMessage());
        }
    }

    /**
     * Retrieves the Elo rating for the specified club.
     *
     * @param clubName the name of the club whose Elo rating is requested
     * @return the Elo rating for the club if available, or 0.0 if not found
     */
    public static double getEloRating(String clubName) {
        return eloMap.getOrDefault(clubName, 0.0);
    }
}
