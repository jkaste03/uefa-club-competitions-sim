package com.example.rounds;

import java.util.List;

import com.example.clubs.Club;
import com.example.clubs.ClubIdWrapper;
import com.example.clubs.ClubSlot;
import com.example.enums.CompetitionData;
import com.example.enums.Country;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Abstract class representing a round in the UEFA competitions.
 */
public abstract class Round {
    protected CompetitionData.Tournament tournament;
    protected CompetitionData.RoundType roundType;
    protected Round nextPrimaryRnd;
    protected Round nextSecondaryRnd;
    protected List<ClubSlot> clubSlots = new ArrayList<>();
    protected List<Tie> ties = new ArrayList<>();

    /**
     * Constructor that initializes the round with a tournament and round type.
     * 
     * @param tournament the tournament of the round.
     * @param roundType  the type of the round.
     */
    public Round(CompetitionData.Tournament tournament, CompetitionData.RoundType roundType) {
        this.tournament = tournament;
        this.roundType = roundType;
    }

    public String getName() {
        return tournament + " " + roundType;
    }

    public CompetitionData.Tournament getTournament() {
        return tournament;
    }

    public CompetitionData.RoundType getRoundType() {
        return roundType;
    }

    public Round getNextPrimaryRnd() {
        return nextPrimaryRnd;
    }

    public Round getNextSecondaryRnd() {
        return nextSecondaryRnd;
    }

    public void setNextRounds(Round nextPrimaryRnd, Round nextSecondaryRnd) {
        this.nextPrimaryRnd = nextPrimaryRnd;
        this.nextSecondaryRnd = nextSecondaryRnd;
    }

    public void setNextRound(Round nextPrimaryRnd) {
        this.nextPrimaryRnd = nextPrimaryRnd;
    }

    public List<ClubSlot> getClubSlots() {
        return clubSlots;
    }

    public void setClubSlots(List<ClubSlot> clubSlots) {
        this.clubSlots = clubSlots;
    }

    public List<Tie> getTies() {
        return ties;
    }

    public void setTies(List<Tie> ties) {
        this.ties = ties;
    }

    /**
     * Adds clubs to the round from a JSON file.
     */
    protected void addClubsFromJson() {
        // Read the JSON file
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(new File("src/main/java/com/example/data/data.json"));

            // Access the "rounds" object
            JsonNode roundsNode = rootNode.path("rounds");

            // Find the corresponding round in the JSON data
            JsonNode roundNode = roundsNode.path(getName());
            addClubsFromJsNode(roundNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds clubs to the round from a JSON node.
     * 
     * @param roundNode the JSON node containing the clubs.
     */
    private void addClubsFromJsNode(JsonNode roundNode) {
        // Iterate through the clubs in this round
        for (JsonNode clubNode : roundNode) {
            String clubName = clubNode.path("name").asText();
            Country country = Country.valueOf(clubNode.path("country").asText());
            float ranking = (float) clubNode.path("ranking").asDouble();
            Club club = new Club(clubName, country, ranking);
            addClubSlot(new ClubIdWrapper(club.getId()));
        }
    }

    /**
     * Adds a club slot to the round.
     * 
     * @param clubSlot the club slot to add.
     */
    public void addClubSlot(ClubSlot clubSlot) {
        clubSlots.add(clubSlot);
    }

    /**
     * Checks if a tie between two clubs is illegal based on political restrictions.
     * 
     * @param club1 the first club.
     * @param club2 the second club.
     * @return true if the tie is illegal, false otherwise.
     */
    protected boolean isIllegalTie(ClubSlot club1, ClubSlot club2) {
        if (!hasCommonCountry(club1, club2)) {
            return IllegalTies.isProhibited(club1, club2);
        }
        return false;
    }

    /**
     * Checks if two clubs share at least one common country.
     * 
     * @param club1 the first club.
     * @param club2 the second club.
     * @return true if they share at least one common country, false otherwise.
     */
    private boolean hasCommonCountry(ClubSlot club1, ClubSlot club2) {
        return club1.getCountries().stream().anyMatch(club2.getCountries()::contains);
    }

    protected void printClubSlotList(List<ClubSlot> clubSlotList) {
        clubSlotList.forEach(clubSlot -> System.out.println(clubSlot.getName()));
    }

    /**
     * Plays the round.
     */
    public abstract void play();

    @Override
    public String toString() {
        return "Round [nextPrimaryRnd=" + (nextPrimaryRnd != null ? nextPrimaryRnd.getName() : "null")
                + ", nextSecondaryRnd=" + (nextSecondaryRnd != null ? nextSecondaryRnd.getName() : "null")
                + ", clubSlots=" + clubSlots + ", ties=" + ties + "]";
    }
}