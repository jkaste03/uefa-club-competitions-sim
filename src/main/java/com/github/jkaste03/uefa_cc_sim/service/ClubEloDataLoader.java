package com.github.jkaste03.uefa_cc_sim.service;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

import com.github.jkaste03.uefa_cc_sim.model.ClubRepository;

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
 * Usage example for retrieving the Elo rating of the club with id 32:
 * 
 * <pre>
 * double eloRating = ClubEloService.getEloRating(32);
 * </pre>
 * <p>
 * If a club's Elo rating is not found, the service returns 0.0 by default.
 */
public class ClubEloDataLoader implements Serializable {
    private static final String BASE_URL = "http://api.clubelo.com/";
    private static final String DATA_FOLDER = "src/main/java/com/github/jkaste03/uefa_cc_sim/data/";
    private static String filePath = DATA_FOLDER + LocalDate.now() + ".csv";
    private final Map<Integer, Double> eloMap = new HashMap<>();

    /**
     * Initializes the Elo ratings by downloading the latest data if not already
     * present.
     */
    public void init() {
        // Download file if it does not exist
        if (!Files.exists(Path.of(filePath))) {
            deleteExistingCSVFiles();
            downloadCSV(LocalDate.now());
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
            // System.out.println("Deleted old API data");
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
            // System.out.println("Downloaded API data for " + date);
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
                String clubName = values[1].trim();
                int clubid = ClubRepository.getIdByName(clubName);
                double elo = Double.parseDouble(values[4].trim());

                eloMap.put(clubid, elo);
            }
        } catch (IOException e) {
            System.err.println("Could not read API data: " + e.getMessage());
        }
    }

    /**
     * Retrieves the Elo rating for the specified club id.
     *
     * @param clubId the id of the club whose Elo rating is requested
     * @return the Elo rating for the club if available, or 0.0 if not found
     */
    public double getEloRating(int clubId) {
        return eloMap.getOrDefault(clubId, 0.0);
    }

    /**
     * Sets the Elo rating for a specified club id.
     * 
     * @param clubId
     * @param elo
     */
    public void setEloRating(int clubId, double elo) {
        eloMap.put(clubId, elo);
    }
}
