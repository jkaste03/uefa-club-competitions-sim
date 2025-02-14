import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.clubs.ClubSlot;
import com.example.clubs.Country;

package com.example.rounds;

public class QRoundTest {

    private QRound qRound;
    private List<ClubSlot> clubSlots;

    @BeforeEach
    public void setUp() {
        qRound = new QRound("Qualifying Round");
        clubSlots = new ArrayList<>();

        // Add mock ClubSlots with rankings and countries
        for (int i = 0; i < 4; i++) {
            ClubSlot clubSlot = new ClubSlot();
            clubSlot.setRanking(i + 1);
            clubSlot.addCountry(new Country("Country" + (i % 2)));
            clubSlots.add(clubSlot);
        }

        qRound.setClubSlots(clubSlots);
    }

    @Test
    public void testSeedClubsEvenNumber() {
        qRound.seedClubs();
        assertEquals(2, qRound.getSeededClubs().size());
        assertEquals(2, qRound.getUnseededClubs().size());
    }

    @Test
    public void testSeedClubsOddNumber() {
        clubSlots.add(new ClubSlot());
        qRound.setClubSlots(clubSlots);
        assertThrows(IllegalArgumentException.class, () -> qRound.seedClubs());
    }

    @Test
    public void testDrawTies() {
        qRound.seedClubs();
        qRound.drawTies();
        assertEquals(2, qRound.getTies().size());
    }

    @Test
    public void testHasCommonCountry() {
        ClubSlot club1 = new ClubSlot();
        club1.addCountry(new Country("Country1"));
        ClubSlot club2 = new ClubSlot();
        club2.addCountry(new Country("Country1"));
        ClubSlot club3 = new ClubSlot();
        club3.addCountry(new Country("Country2"));

        assertTrue(qRound.hasCommonCountry(club1, club2));
        assertFalse(qRound.hasCommonCountry(club1, club3));
    }

    @Test
    public void testRun() {
        qRound.run();
        assertEquals(2, qRound.getSeededClubs().size());
        assertEquals(2, qRound.getUnseededClubs().size());
        assertEquals(2, qRound.getTies().size());
    }

    @Test
    public void testPlay() {
        qRound.run();
        qRound.play();
        qRound.getTies().forEach(tie -> assertTrue(tie.isPlayed()));
    }
}