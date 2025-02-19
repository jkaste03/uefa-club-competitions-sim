package com.example.rounds;

import java.util.List;

import com.example.clubs.ClubSlot;
import com.example.enums.Country;

public class DoubleLeggedTieWrapper extends ClubSlot {
    private DoubleLeggedTie dLTie;
    private boolean worstRankForSeeding;

    public DoubleLeggedTieWrapper(DoubleLeggedTie dLTie, boolean worstRankForSeeding) {
        this.dLTie = dLTie;
        this.worstRankForSeeding = worstRankForSeeding;
    }

    public Tie getDLTie() {
        return dLTie;
    }

    public void setDLTie(DoubleLeggedTie dLTie) {
        this.dLTie = dLTie;
    }

    public boolean isWorstRankForSeeding() {
        return worstRankForSeeding;
    }

    public void setWorstRankForSeeding(boolean worstRankForSeeding) {
        this.worstRankForSeeding = worstRankForSeeding;
    }

    public ClubSlot getCorrectClubSlot() {
        return worstRankForSeeding ? dLTie.getLoser() : dLTie.getWinner();
    }

    @Override
    public String getName() {
        return dLTie.getName() + (worstRankForSeeding ? " (loser of)" : " (winner of)");
    }

    /**
     * Retrieves the ranking of the UndecidedClub based on the rankings of its
     * associated clubs.
     * If the `worstRankForSeeding` flag is true, it returns the worst ranking among
     * the clubs.
     * Otherwise, it returns the best ranking among the clubs.
     * If there are no clubs, it returns 0.
     *
     * @return the ranking of the club as a float.
     */
    @Override
    public float getRanking() {
        return dLTie.getRanking(worstRankForSeeding);
    }

    @Override
    public List<Country> getCountries() {
        return dLTie.getCountries();
    }

    @Override
    public String toString() {
        return dLTie.toString();
    }
}
