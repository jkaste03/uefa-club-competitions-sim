package com.example.rounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.api.ClubEloAPI;

public class Rounds {
    private static QRound uclQ1CP, uclQ2CP, uclQ2LP, uclQ3CP, uclQ3LP, uclPoCP, uclPoLP, uclLP;
    private static QRound uelQ1MP, uelQ2MP, uelQ3MP, uelQ3CP, uelPo, uelLP;
    private static QRound ueclQ1MP, ueclQ2MP, ueclQ2CP, ueclQ3MP, ueclQ3CP, ueclPoMP, ueclPoCP, ueclLP;

    private static List<Round> rounds;

    public Rounds() {
        new ClubEloAPI();
        uclQ1CP = new QRound("uclQ1CP");
        uclQ2CP = new QRound("uclQ2CP");
        uclQ2LP = new QRound("uclQ2LP");
        uclQ3CP = new QRound("uclQ3CP");
        uclQ3LP = new QRound("uclQ3LP");
        uclPoCP = new QRound("uclPoCP");
        uclPoLP = new QRound("uclPoLP");
        uclLP = new QRound("uclLP");

        uelQ1MP = new QRound("uelQ1MP");
        uelQ2MP = new QRound("uelQ2MP");
        uelQ3MP = new QRound("uelQ3MP");
        uelQ3CP = new QRound("uelQ3CP");
        uelPo = new QRound("uelPo");
        uelLP = new QRound("uelLP");

        ueclQ1MP = new QRound("ueclQ1MP");
        ueclQ2MP = new QRound("ueclQ2MP");
        ueclQ2CP = new QRound("ueclQ2CP");
        ueclQ3MP = new QRound("ueclQ3MP");
        ueclQ3CP = new QRound("ueclQ3CP");
        ueclPoMP = new QRound("ueclPoMP");
        ueclPoCP = new QRound("ueclPoCP");
        ueclLP = new QRound("ueclLP");

        rounds = new ArrayList<>(
                Arrays.asList(uclQ1CP, uelQ1MP, ueclQ1MP, uclQ2CP, uclQ2LP, uelQ2MP, ueclQ2MP, ueclQ2CP, uclQ3CP,
                        uclQ3LP, uelQ3MP, uelQ3CP, ueclQ3MP, ueclQ3CP, uclPoCP, uclPoLP, uelPo, ueclPoMP, ueclPoCP,
                        uclLP, uelLP, ueclLP));
        linkRounds();
    }

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

    @Override
    public String toString() {
        return "Rounds [" + Arrays.toString(rounds.toArray()) + "]";
    }

    public void run() {
        for (Round round : rounds) {
            round.run();
        }
    }
}