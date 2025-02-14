package com.example.clubs;

/**
 * Class representing an undecided club in the UEFA competitions.
 */
public class UndecidedClub extends ClubSlot {
    private ClubSlot clubSlot1;
    private ClubSlot clubSlot2;
    private Boolean worstRankForSeeding;

    /**
     * Constructor that initializes the undecided club with a list of clubs and a
     * flag for worst rank for seeding.
     * 
     * @param clubs               the list of clubs.
     * @param worstRankForSeeding the flag indicating whether to use the worst rank
     *                            for seeding.
     */
    public UndecidedClub(ClubSlot clubSlot1, ClubSlot clubSlot2, Boolean worstRankForSeeding) {
        this.clubSlot1 = clubSlot1;
        this.clubSlot2 = clubSlot2;
        this.worstRankForSeeding = worstRankForSeeding;
    }

    public ClubSlot getClubSlot1() {
        return clubSlot1;
    }

    public ClubSlot getClubSlot2() {
        return clubSlot2;
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
        return worstRankForSeeding ? Math.max(clubSlot1.getRanking(), clubSlot2.getRanking())
                : Math.min(clubSlot1.getRanking(), clubSlot2.getRanking());
    }
}
