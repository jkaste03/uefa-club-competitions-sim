package com.example.rounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.api.ClubEloAPI;
import com.example.enums.CompetitionData.Tournament;
import com.example.enums.CompetitionData.RoundType;
import com.example.enums.CompetitionData.PathType;

/**
 * The Rounds class initializes, links, and executes all the qualifying rounds
 * for UEFA competitions.
 * It provides a structured flow from seeding draws to playing rounds.
 */
public class Rounds {
    private static QRound uclQ1CP, uclQ2CP, uclQ2LP, uclQ3CP, uclQ3LP, uclPoCP, uclPoLP;
    private static QRound uelQ1MP, uelQ2MP, uelQ3MP, uelQ3CP, uelPo;
    private static QRound ueclQ1MP, ueclQ2MP, ueclQ2CP, ueclQ3MP, ueclQ3CP, ueclPoMP, ueclPoCP;
    private static LeaguePhaseRound uclLP, uelLP, ueclLP;

    private static List<Round> rounds;

    /**
     * Constructor that initializes all the qualifying rounds and links them.
     */
    public Rounds() {
        new ClubEloAPI();
        uclQ1CP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q1, PathType.CHAMPIONS_PATH);
        uclQ2CP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q2, PathType.CHAMPIONS_PATH);
        uclQ2LP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q2, PathType.LEAGUE_PATH);
        uclQ3CP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q3, PathType.CHAMPIONS_PATH);
        uclQ3LP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.Q3, PathType.LEAGUE_PATH);
        uclPoCP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.PLAYOFF, PathType.CHAMPIONS_PATH);
        uclPoLP = new QRound(Tournament.CHAMPIONS_LEAGUE, RoundType.PLAYOFF, PathType.LEAGUE_PATH);
        uclLP = new LeaguePhaseRound(Tournament.CHAMPIONS_LEAGUE, RoundType.LEAGUE_PHASE);

        uelQ1MP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q1, PathType.MAIN_PATH);
        uelQ2MP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q2, PathType.MAIN_PATH);
        uelQ3MP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q3, PathType.MAIN_PATH);
        uelQ3CP = new QRound(Tournament.EUROPA_LEAGUE, RoundType.Q3, PathType.CHAMPIONS_PATH);
        uelPo = new QRound(Tournament.EUROPA_LEAGUE, RoundType.PLAYOFF, PathType.MAIN_PATH);
        uelLP = new LeaguePhaseRound(Tournament.EUROPA_LEAGUE, RoundType.LEAGUE_PHASE);

        ueclQ1MP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q1, PathType.MAIN_PATH);
        ueclQ2MP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q2, PathType.MAIN_PATH);
        ueclQ2CP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q2, PathType.CHAMPIONS_PATH);
        ueclQ3MP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q3, PathType.MAIN_PATH);
        ueclQ3CP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.Q3, PathType.CHAMPIONS_PATH);
        ueclPoMP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.PLAYOFF, PathType.MAIN_PATH);
        ueclPoCP = new QRound(Tournament.CONFERENCE_LEAGUE, RoundType.PLAYOFF, PathType.CHAMPIONS_PATH);
        ueclLP = new LeaguePhaseRound(Tournament.CONFERENCE_LEAGUE, RoundType.LEAGUE_PHASE);

        rounds = new ArrayList<>(
                Arrays.asList(uclQ1CP, uelQ1MP, ueclQ1MP, uclQ2CP, uclQ2LP, uelQ2MP, ueclQ2MP, ueclQ2CP, uclQ3CP,
                        uclQ3LP, uelQ3MP, uelQ3CP, ueclQ3MP, ueclQ3CP, uclPoCP, uclPoLP, uelPo, ueclPoMP, ueclPoCP,
                        uclLP, uelLP, ueclLP));
        linkRounds();
    }

    /**
     * Links the rounds by setting the next primary and secondary rounds.
     * This method configures the progression sequence for each competition round.
     */
    private static void linkRounds() {
        uclQ1CP.setNextRounds(uclQ2CP, ueclQ2CP);
        uclQ2CP.setNextRounds(uclQ3CP, uelQ3CP);
        uclQ2LP.setNextRounds(uclQ3LP, uelQ3MP);
        uclQ3CP.setNextRounds(uclPoCP, uelPo);
        uclQ3LP.setNextRounds(uclPoLP, uelLP);
        uclPoCP.setNextRounds(uclLP, uelLP);
        uclPoLP.setNextRounds(uclLP, uelLP);

        uelQ1MP.setNextRounds(uelQ2MP, ueclQ2MP);
        uelQ2MP.setNextRounds(uelQ3MP, ueclQ3MP);
        uelQ3MP.setNextRounds(uelPo, ueclPoMP);
        uelQ3CP.setNextRounds(uelPo, ueclPoCP);
        uelPo.setNextRounds(uelLP, ueclLP);

        ueclQ1MP.setNextRound(ueclQ2MP);
        ueclQ2MP.setNextRound(ueclQ3MP);
        ueclQ2CP.setNextRound(ueclQ3CP);
        ueclQ3MP.setNextRound(ueclPoMP);
        ueclQ3CP.setNextRound(ueclPoCP);
        ueclPoMP.setNextRound(ueclLP);
        ueclPoCP.setNextRound(ueclLP);
    }

    /**
     * Runs all the rounds.
     * <p>
     * Iterates through each round type: filtering rounds by type, performing
     * seeding, draws,
     * trying to register ties for the next rounds, seeding the next rounds, and
     * playing
     * each round twice.
     * Execution stops after processing the PLAYOFF round.
     * </p>
     */
    public void run() {
        for (RoundType roundType : RoundType.values()) {
            List<Round> roundsOfType = getRoundsOfType(roundType);
            trySeedingDraws(roundsOfType);
            regTiesForNextRndsIfQRound(roundsOfType);
            seedDrawNextRndsIfQRounds(roundsOfType);
            playRounds(roundsOfType);
            if (roundType == RoundType.PLAYOFF) {
                registerTieClubsForLeagues(roundsOfType);
                break;
            }
        }
    }

    /**
     * Returns a list of rounds filtered by the specified round type.
     *
     * @param roundType the round type to filter rounds
     * @return a list of rounds of the specified type
     */
    private List<Round> getRoundsOfType(RoundType roundType) {
        return rounds.stream().filter(round -> round.getRoundType() == roundType).toList();
    }

    /**
     * Performs seeding draws for each round in the provided list.
     *
     * @param roundsOfType the list of rounds to perform seeding draws on
     */
    private void trySeedingDraws(List<Round> roundsOfType) {
        roundsOfType.forEach(round -> ((QRound) round).trySeedDraw());
    }

    /**
     * Registers ties for the subsequent rounds if applicable.
     *
     * @param roundsOfType the list of rounds to register ties for
     */
    private void regTiesForNextRndsIfQRound(List<Round> roundsOfType) {
        Round roundOfTypeNextRnd = roundsOfType.stream().findAny().orElse(null).getNextPrimaryRnd();
        if (roundOfTypeNextRnd instanceof QRound) {
            roundsOfType.forEach(round -> ((QRound) round).regTiesForNextRounds());
        }
    }

    /**
     * Initiates the seeding draw for the next rounds.
     *
     * @param roundsOfType the list of rounds to seed for the following round
     */
    private void seedDrawNextRndsIfQRounds(List<Round> roundsOfType) {
        roundsOfType.forEach(round -> ((QRound) round).seedDrawNextIfQRound());
    }

    /**
     * Plays each round in the list for a fixed number of iterations (2 times).
     *
     * @param roundsOfType the list of rounds to be played
     */
    private void playRounds(List<Round> roundsOfType) {
        for (int i = 0; i < 2; i++) {
            roundsOfType.forEach(Round::play);
        }
    }

    /**
     * Registers clubs for the next rounds by iterating through the provided list of
     * rounds.
     * Each round is cast to a QRound and the method registerTieClubsForLeague is
     * called on it.
     *
     * @param roundsOfType a list of rounds to register clubs for the next rounds
     */
    private void registerTieClubsForLeagues(List<Round> roundsOfType) {
        roundsOfType.forEach(round -> ((QRound) round).registerTieClubsForLeague());
    }

    /**
     * Returns a string representation of the rounds.
     *
     * @return a string containing information about all rounds
     */
    @Override
    public String toString() {
        return "Rounds [" + Arrays.toString(rounds.toArray()) + "]";
    }
}