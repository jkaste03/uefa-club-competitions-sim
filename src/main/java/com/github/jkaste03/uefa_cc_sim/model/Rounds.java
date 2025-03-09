package com.github.jkaste03.uefa_cc_sim.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.PathType;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.RoundType;
import com.github.jkaste03.uefa_cc_sim.enums.CompetitionData.Tournament;
import com.github.jkaste03.uefa_cc_sim.service.ClubEloDataLoader;
import com.github.jkaste03.uefa_cc_sim.service.JsonDataLoader;

/**
 * The Rounds class is responsible for initializing, linking, and executing all
 * rounds for UEFA competitions. It sets up the rounds, linking each round to
 * define the progression sequence. This detailed simulation ensures that
 * seeding, draws, tie registrations, and match play are executed in an
 * organized manner.
 */
public class Rounds {
    // Declare qualifying rounds and league rounds for all competitions.
    private QRound uclQ1CP, uclQ2CP, uclQ2LP, uclQ3CP, uclQ3LP, uclPoCP, uclPoLP;
    private QRound uelQ1MP, uelQ2MP, uelQ3MP, uelQ3CP, uelPo;
    private QRound ueclQ1MP, ueclQ2MP, ueclQ2CP, ueclQ3MP, ueclQ3CP, ueclPoMP, ueclPoCP;
    private LeaguePhaseRound uclLP, uelLP, ueclLP;
    private List<Round> rounds;

    // Map to hold club Elo ratings for each club.
    private ClubEloDataLoader clubEloDataLoader;

    /**
     * Constructs all rounds for UEFA competitions, initializes club Elo API,
     * and sets up the interlink between rounds. This constructor prepares the
     * simulation by creating each qualifying and league phase round instance.
     */
    public Rounds() {
        // Create instances for Champions League qualifier rounds.
        uclQ1CP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q1, PathType.CHAMPIONS_PATH);
        uclQ2CP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q2, PathType.CHAMPIONS_PATH);
        uclQ2LP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q2, PathType.LEAGUE_PATH);
        uclQ3CP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q3, PathType.CHAMPIONS_PATH);
        uclQ3LP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q3, PathType.LEAGUE_PATH);
        uclPoCP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.PLAYOFF, PathType.CHAMPIONS_PATH);
        uclPoLP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.PLAYOFF, PathType.LEAGUE_PATH);
        uclLP = new LeaguePhaseRound(Tournament.CHAMPIONS_LEAGUE);

        // Create instances for Europa League qualifier rounds.
        uelQ1MP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q1, PathType.MAIN_PATH);
        uelQ2MP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q2, PathType.MAIN_PATH);
        uelQ3MP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q3, PathType.MAIN_PATH);
        uelQ3CP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q3, PathType.CHAMPIONS_PATH);
        uelPo = new QRound(Tournament.EUROPA_LEAGUE, RoundType.PLAYOFF, PathType.MAIN_PATH);
        uelLP = new LeaguePhaseRound(Tournament.EUROPA_LEAGUE);

        // Create instances for Conference League qualifier rounds.
        ueclQ1MP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q1, PathType.MAIN_PATH);
        ueclQ2MP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q2, PathType.MAIN_PATH);
        ueclQ2CP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q2, PathType.CHAMPIONS_PATH);
        ueclQ3MP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q3, PathType.MAIN_PATH);
        ueclQ3CP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q3, PathType.CHAMPIONS_PATH);
        ueclPoMP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.PLAYOFF, PathType.MAIN_PATH);
        ueclPoCP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.PLAYOFF, PathType.CHAMPIONS_PATH);
        ueclLP = new LeaguePhaseRound(Tournament.CONFERENCE_LEAGUE);

        // Aggregate all rounds into a list for streamlined processing.
        rounds = new ArrayList<>(
                Arrays.asList(uclQ1CP, uelQ1MP, ueclQ1MP, uclQ2CP, uclQ2LP, uelQ2MP, ueclQ2MP, ueclQ2CP, uclQ3CP,
                        uclQ3LP, uelQ3MP, uelQ3CP, ueclQ3MP, ueclQ3CP, uclPoCP, uclPoLP, uelPo, ueclPoMP, ueclPoCP,
                        uclLP, uelLP, ueclLP));

        // Initialize data for each round.
        JsonDataLoader.loadDataForRounds(rounds);

        // Initialize external service to fetch club elo ratings
        clubEloDataLoader = new ClubEloDataLoader();
        clubEloDataLoader.init();

        // Link rounds to define the progression flow.
        linkRounds();
    }

    /**
     * Establishes connections between rounds by assigning the next primary and
     * secondary rounds. These links define the simulation flow from initial
     * qualifying rounds to the league phase.
     */
    private void linkRounds() {
        // Linking for Champions League
        uclQ1CP.setNextRounds(uclQ2CP, ueclQ2CP);
        uclQ2CP.setNextRounds(uclQ3CP, uelQ3CP);
        uclQ2LP.setNextRounds(uclQ3LP, uelQ3MP);
        uclQ3CP.setNextRounds(uclPoCP, uelPo);
        uclQ3LP.setNextRounds(uclPoLP, uelLP);
        uclPoCP.setNextRounds(uclLP, uelLP);
        uclPoLP.setNextRounds(uclLP, uelLP);

        // Linking for Europa League
        uelQ1MP.setNextRounds(uelQ2MP, ueclQ2MP);
        uelQ2MP.setNextRounds(uelQ3MP, ueclQ3MP);
        uelQ3MP.setNextRounds(uelPo, ueclPoMP);
        uelQ3CP.setNextRounds(uelPo, ueclPoCP);
        uelPo.setNextRounds(uelLP, ueclLP);

        // Linking for Conference League (single next round linkage in some cases)
        ueclQ1MP.setNextRound(ueclQ2MP);
        ueclQ2MP.setNextRound(ueclQ3MP);
        ueclQ2CP.setNextRound(ueclQ3CP);
        ueclQ3MP.setNextRound(ueclPoMP);
        ueclQ3CP.setNextRound(ueclPoCP);
        ueclPoMP.setNextRound(ueclLP);
        ueclPoCP.setNextRound(ueclLP);
    }

    /**
     * Initiates the simulation by executing all rounds in their respective order.
     * This method drives the simulation from qualifiers through league matches.
     */
    public void run() {
        // Start by processing the qualifying rounds.
        runQRounds();
        // Proceed to the league phase rounds.
        runLeagueRounds();
    }

    /**
     * Processes each qualifying round by iterating over all round types,
     * performing seeding, tie registration, and match play. The progression
     * for each round type is handled sequentially.
     */
    private void runQRounds() {
        // Retrieve all defined round types
        RoundType[] roundTypes = RoundType.values();
        List<Round> roundsOfType = null;
        // Execute seeding and draws for Q1 round type.
        seedDrawQRounds(getRoundsOfType(RoundType.Q1));
        for (int i = 0; roundTypes[i] != RoundType.LEAGUE_PHASE; i++) {
            // Filter rounds by the current round type.
            roundsOfType = getRoundsOfType(roundTypes[i]);
            // Update club slots in ties for the current round type.
            updateClubSlotsInTies(roundsOfType);
            // Register ties for the next round type.
            regTiesForNextQRounds(roundsOfType);
            // Execute seeding and draws for next round type.
            seedDrawQRounds(getRoundsOfType(roundTypes[i + 1]));
            // Play the matches of the round type.
            playRounds(roundsOfType);
        }
        // Register clubs for league phase after qualifiers complete.
        registerClubsForLeagues(roundsOfType);
    }

    /**
     * Filters and retrieves rounds that match the specified round type.
     *
     * @param roundType the type of round to filter by
     * @return a list of rounds matching the round type
     */
    private List<Round> getRoundsOfType(RoundType roundType) {
        return rounds.stream()
                .filter(round -> round.getRoundType() == roundType)
                .toList();
    }

    /**
     * Performs a seeding and draws for all QRounds in the list.
     *
     * @param roundsOfType list of rounds
     */
    private void seedDrawQRounds(List<Round> roundsOfType) {
        if (roundsOfType.get(0) instanceof QRound) {
            roundsOfType.forEach(round -> {
                round.seedDraw();
            });
        }
    }

    /** Updates club slots in ties for all rounds in the list. */
    private void updateClubSlotsInTies(List<Round> roundsOfType) {
        roundsOfType.forEach(round -> {
            round.updateClubSlotsInTies();
        });
    }

    /**
     * Registers ties for the next rounds if the current rounds are QRounds.
     * This ensures that winners are correctly aligned for their subsequent matches.
     *
     * @param roundsOfType list of rounds participating in the current qualifier
     *                     stage
     */
    private void regTiesForNextQRounds(List<Round> roundsOfType) {
        if (roundsOfType.get(0).getNextPrimaryRnd() instanceof QRound) {
            roundsOfType.forEach(round -> {
                ((QRound) round).regTiesForNextRounds();
            });
        }
    }

    /**
     * Executes the match play sequence for each round twice. The first execution
     * simulates the initial contest and the second ensures proper tie-break
     * registration.
     *
     * @param roundsOfType list of rounds to simulate matches on
     */
    private void playRounds(List<Round> roundsOfType) {
        // First legs of play
        roundsOfType.forEach(r -> r.play(clubEloDataLoader));
        // Second legs of play to determine tie outcomes.
        roundsOfType.forEach(r -> r.play(clubEloDataLoader));
    }

    /**
     * Registers clubs into league rounds after tie matches have concluded.
     * Each qualifying round casts to QRound to register clubs entering the league
     * phase.
     *
     * @param roundsOfType list of rounds from which clubs are registered
     */
    private void registerClubsForLeagues(List<Round> roundsOfType) {
        roundsOfType.forEach(round -> {
            ((QRound) round).registerClubsForLeague();
        });
    }

    private void runLeagueRounds() {
        // Execute seeding and draws for league phase rounds.
        // seedDrawLeagueRounds();
        // Play the league phase rounds.
        // playRounds(getRoundsOfType(RoundType.LEAGUE_PHASE));
    }

    /**
     * Returns a string representation of all rounds for logging and debugging
     * purposes.
     *
     * @return a string summarizing the rounds sequence in the simulation
     */
    @Override
    public String toString() {
        return "Rounds [" + Arrays.toString(rounds.toArray()) + "]";
    }
}