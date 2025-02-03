package com.example.api;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class ClubEloAPI {
    private static final String BASE_URL = "http://api.clubelo.com/";
    private static final String DATA_FOLDER = "src/main/java/com/example/data/";
    private static final Map<String, Double> clubMap = new HashMap<>();
    private static String filePath;

    public ClubEloAPI() {
        LocalDate today = LocalDate.now();
        filePath = DATA_FOLDER + today + ".csv";

        // Last ned fil hvis den ikke finnes
        if (!Files.exists(Path.of(filePath))) {
            downloadCSV(today);
        }

        // Last inn data i minnet
        loadClubData();
    }

    private void downloadCSV(LocalDate date) {
        String urlString = BASE_URL + date;
        try (InputStream in = new URI(urlString).toURL().openStream()) {
            Files.createDirectories(Path.of(DATA_FOLDER));
            Files.copy(in, Path.of(filePath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Lastet ned API-data for " + date);
        } catch (Exception e) {
            System.err.println("Kunne ikke laste ned API-data fra " + urlString + ": " + e.getMessage());
        }
    }

    private void loadClubData() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Hopp over header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 5)
                    continue;
                String club = values[1].trim();
                Double elo = Double.parseDouble(values[4].trim());

                clubMap.put(club, elo);
            }
        } catch (IOException e) {
            System.err.println("Kunne ikke lese API-data: " + e.getMessage());
        }
    }

    public static Double getEloRating(String clubName) {
        return clubMap.getOrDefault(clubName, null);
    }

    // public static class ClubData {
    // private final Country country;
    // private final double eloRating;

    // public ClubData(Country country, double eloRating) {
    // this.country = country;
    // this.eloRating = eloRating;
    // }

    // public Country getCountry() {
    // return country;
    // }

    // public double getEloRating() {
    // return eloRating;
    // }
    // }
}
