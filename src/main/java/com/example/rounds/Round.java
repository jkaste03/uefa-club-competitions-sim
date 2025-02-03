package com.example.rounds;

import java.util.List;

import com.example.clubs.Club;
import com.example.clubs.Country;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Round {
    protected String name;
    protected Round nextPrimaryRnd;
    protected Round nextSecondaryRnd;
    protected List<Tie> ties = new ArrayList<>();

    public Round(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNextRounds(Round nextPrimaryRnd, Round nextSecondaryRnd) {
        this.nextPrimaryRnd = nextPrimaryRnd;
        this.nextSecondaryRnd = nextSecondaryRnd;
    }

    public void setNextRound(Round nextPrimaryRnd) {
        this.nextPrimaryRnd = nextPrimaryRnd;
    }

    protected void addClubsFromJson() {
        // Read the JSON file
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(new File("src/main/java/com/example/data/data.json"));

            // Access the "rounds" object
            JsonNode roundsNode = rootNode.path("rounds");

            // Find the corresponding round in the JSON data
            JsonNode roundNode = roundsNode.path(name);
            addClubs(roundNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addClubs(JsonNode roundNode) {
        // Iterate through the clubs in this round
        for (JsonNode clubNode : roundNode) {
            String clubName = clubNode.path("club").asText();
            Country country = Country.valueOf(clubNode.path("country").asText());
            addClub(new Club(clubName, country));
        }
    }

    public abstract void addClub(Club club);

    public abstract void run();

    public abstract void play();
}