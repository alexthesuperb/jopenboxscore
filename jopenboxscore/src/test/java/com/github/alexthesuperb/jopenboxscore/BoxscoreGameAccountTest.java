package com.github.alexthesuperb.jopenboxscore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class BoxscoreGameAccountTest {

    /** The expected values from a game account, derived from Retrosheet gamelogs */
    Object[] expecteds;

    /** The actual values, derived from <code>BoxscoreGameAccount</code>, the class we are testing */
    Object[] actuals;

    String gameId;
    
    public BoxscoreGameAccountTest(String gameId, Object[] expecteds, Object[] actuals) {
        this.gameId = gameId;
        this.expecteds = expecteds;
        this.actuals = actuals;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() throws Exception {

        /* Initialize list to store generated BoxscoreGameAccounts. */
        List<BoxscoreGameAccount> eveReaderGames = new LinkedList<BoxscoreGameAccount>();

        /* 1. Read in each official game outcome from the gamelog file. */
        List<TestGame> gamelogGames = readGamelogs("src/test/resources/gamelogs/GL2018.TXT");
        Collections.sort(gamelogGames);

        String eveDirStr = "src/test/resources/2018eve/";

        /* 2. Run RetrosheetEveReader for all "src/test/resources/2018eve" files */
        for (String fileName : new File(eveDirStr).list()) {
            if (fileName.contains(".EVN") || fileName.contains("EVA")) {
                RetrosheetEveReader retroReader = new RetrosheetEveReader(
                    eveDirStr + fileName,
                    "2018",
                    new File(eveDirStr)
                );
                retroReader.readAll();
                eveReaderGames.addAll(retroReader.getGameAccounts());
            }
        }

        /*
         * 3. Match each official (expected) game to the BoxscoreGameAccount (actual)
         * game by their IDs.
         */
        Object[][] data = new Object[gamelogGames.size()][3];
        
        for (int i = 0; i < gamelogGames.size(); i++) {
            String id = gamelogGames.get(i).getGameId();
            
            data[i][0] = id;
            data[i][1] = gamelogGames.get(i).toArray();
            // data[i][2] = boxscoreGameAccountToArray(null);

            for (BoxscoreGameAccount eveGame : eveReaderGames) {
                if (eveGame.getGameID().equals(id)) {
                    data[i][1] = gamelogGames.get(i).toArray();
                    data[i][2] = boxscoreGameAccountToArray(eveGame);
                    break;
                }
            }   
        }

        /* 4. Return the list of {expectedGame, actualGame} pairs. */
        return Arrays.asList(data);
    }

    public static Object[] boxscoreGameAccountToArray(BoxscoreGameAccount acct) {
        if (acct == null) {
            return new Object[] {
                "null", "null", "null", -1,
                -1, -1, -1, -1,
                -1, -1, -1, -1
            };
        } else {
            return new Object[] {
                acct.getGameID(), acct.getVisitingTeamId(), acct.getHomeTeamId(), acct.getTotalOuts(),
                acct.getVisitorScore(), acct.getVisitorStat(BaseballPlayer.KEY_H), acct.getVisitorStat(BaseballPlayer.KEY_E), 
                    acct.getVisitorStat(BaseballPlayer.KEY_AB),
                acct.getHomeScore(), acct.getHomeStat(BaseballPlayer.KEY_H), acct.getHomeStat(BaseballPlayer.KEY_E), 
                    acct.getHomeStat(BaseballPlayer.KEY_AB)
            };
        }
    }

    public static List<TestGame> readGamelogs(final String fileName) throws IOException {
        String line;
        final BufferedReader gamelogReader = new BufferedReader(new FileReader(fileName));
        final List<TestGame> gamelogs = new LinkedList<TestGame>();
        /*
         * To test the accuracy of BoxscoreGameAccount, we will compare their results
         * against the official game results compiled by Retrosheet and stored in
         * gamelog files.
         * 
         * Gamelog files are encoded in CSV, with each row representing a single game.
         * For our test, we care about the following information:
         * 
         * ENTRY #   INDEX #    DESCRIPTION 
         * -------   -------    ----------- 
         *   1         0        Date YYYYMMDD 
         *   2         1        Number of game 
         *   4         3        Visiting team id 
         *   7         6        Home team id 
         *   10        9        Visting team score (unquoted) 
         *   11        10       Home team score (unquoted) 
         *   12        11       Length of game in outs (unquoted) 
         *   22        21       Visiting team at-bats (unquoted) 
         *   23        22       Visiting team hits (unquoted) 
         *   46        45       Visiting team errors (unquoted) 
         *   50        49       Home team at-bats (unquoted) 
         *   51        50       Home team hits (unquoted) 
         *   74        73       Home team errors (unqoted)
         */
        while ((line = gamelogReader.readLine()) != null) {
            final String[] lineArr = line.split(",");

            final String date = lineArr[0].replaceAll("^\"|\"$", "");
            final String gmNumber = lineArr[1].replaceAll("^\"|\"$", "");
            final String visId = lineArr[3].replaceAll("^\"|\"$", "");
            final String homeId = lineArr[6].replaceAll("^\"|\"$", "");
            final int visScore = Integer.parseInt(lineArr[9]);
            final int homeScore = Integer.parseInt(lineArr[10]);
            final int totOuts = Integer.parseInt(lineArr[11]);
            final int visAb = Integer.parseInt(lineArr[21]);
            final int visHits = Integer.parseInt(lineArr[22]);
            final int visErrors = Integer.parseInt(lineArr[45]);
            final int homeAb = Integer.parseInt(lineArr[49]);
            final int homeHits = Integer.parseInt(lineArr[50]);
            final int homeErrors = Integer.parseInt(lineArr[73]);

            final String gameId = homeId + date + gmNumber;
            /*
             * Prepare test classes, where each test case is initialized with: 
             * 1.  gameId 
             * 2.  visId 
             * 3.  homeId 
             * 4.  totalOuts (54 for a full 9-inning game) 
             * 5.  visScore 
             * 6.  visHits 
             * 7.  visErrors 
             * 8.  visAtBats 
             * 9.  homeScore 
             * 10. homeHits 
             * 11. homeErrors
             * 12. homeAtBats
             */
            gamelogs.add(new TestGame(gameId, visId, homeId, totOuts, visScore, visHits, visErrors, visAb,
                    homeScore, homeHits, homeErrors, homeAb));

        }
        gamelogReader.close();
        return gamelogs;
    }

    @Test
    public void testGame() {
        assertArrayEquals(expecteds, actuals);
    }

    /**
     * A test class containing certain pertinent information about a game. This
     * class is used to test the accuracy of a collected game account.
     */
    static class TestGame implements GameAccount, Comparable<TestGame> {

        private final String gameId;
        private final int totalOuts;

        private final String visId;
        private final int visScore;
        private final int visHits;
        private final int visErrors;
        private final int visAtBats;

        private String homeId;
        private final int homeScore;
        private final int homeHits;
        private final int homeErrors;
        private final int homeAtBats;

        TestGame(final String gameId, final String visId, final String homeId, final int totalOuts, 
                final int visScore, final int visHits, final int visErrors, final int visAtBats, 
                final int homeScore, final int homeHits, final int homeErrors, final int homeAtBats) {
            this.gameId = gameId;           //Element 0
            this.visId = visId;             //1
            this.homeId = homeId;           //2
            this.totalOuts = totalOuts;     //3
            this.visScore = visScore;       //4
            this.visHits = visHits;         //5
            this.visErrors = visErrors;     //6
            this.visAtBats = visAtBats;     //7
            this.homeScore = homeScore;     //8
            this.homeHits = homeHits;       //9
            this.homeErrors = homeErrors;   //10
            this.homeAtBats = homeAtBats;   //12
        }

        public String getGameId() {
            return gameId;
        }

        public Object[] toArray() {
            return new Object[] {gameId, visId, homeId, totalOuts, 
                visScore, visHits, visErrors, visAtBats, 
                homeScore, homeHits, homeErrors, homeAtBats};
        }

        /**
         * Test the accuracy of a <code>BoxscoreGameAccount</code> object with a simple
         * pass/fail.
         * 
         * @param testObj An instance of <code>BoxscoreGameAccount</code> generated via
         *                <code>RetrosheetEventReader</code>.
         * @return <code>true</code> if all expected variables contained in this
         *         instance of <code>TestGame</code> match the collected variables
         *         contained in <code>BoxscoreGameAccount</code>.
         */
        boolean equals(final BoxscoreGameAccount testObj) {
            if (testObj == null) {
                return false;
            }
            if (!this.gameId.equals(testObj.getGameID())) {
                return false;
            }
            if (!this.visId.equals(testObj.getVisitingTeamId())) {
                return false;
            }
            if (!this.homeId.equals(testObj.getHomeTeamId())) {
                return false;
            }
            if (this.totalOuts != testObj.getTotalOuts()) {
                return false;
            }
            if (this.visScore != testObj.getVisitorScore()) {
                return false;
            }
            if (this.visHits != testObj.getVisitorStat(BaseballPlayer.KEY_H)) {
                return false;
            }
            if (this.visErrors != testObj.getVisitorStat(BaseballPlayer.KEY_E)) {
                return false;
            }
            if (this.visAtBats != testObj.getVisitorStat(BaseballPlayer.KEY_AB)) {
                return false;
            }
            if (homeScore != testObj.getHomeScore()) {
                return false;
            }
            if (this.homeHits != testObj.getHomeStat(BaseballPlayer.KEY_H)) {
                return false;
            }
            if (this.homeErrors != testObj.getHomeStat(BaseballPlayer.KEY_E)) {
                return false;
            }
            if (this.homeAtBats != testObj.getHomeStat(BaseballPlayer.KEY_AB)) {
                return false;
            }

            return true;
        }

        @Override
        public void addLine(final String pbpLine, final int lineNum)
                throws FileNotFoundException, IOException, IllegalArgumentException {
            /* does nothing */
        }

        @Override
        public int compareTo(TestGame obj) {
            return this.gameId.compareTo(obj.gameId);
        }
    }

}