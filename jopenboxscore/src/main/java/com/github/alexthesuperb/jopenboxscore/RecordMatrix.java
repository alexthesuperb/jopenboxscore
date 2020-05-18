package com.github.alexthesuperb.jopenboxscore;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * The <code>RecordMatrix</code> class stores a single team's record, broken
 * down by opponent and home and away games.
 * </p>
 */
public class RecordMatrix {

    private class RecordRow {
        private int homeWins;
        private int homeLosses;
        private int homeTies;
        private int homeRunsScored;
        private int homeRunsAllowed;
        private int roadWins;
        private int roadLosses;
        private int roadTies;
        private int roadRunsScored;
        private int roadRunsAllowed;

        public RecordRow() {
            super();
        }

        public void updateRow(String outcome, boolean home, int rs, int ra) {
            if (home) {
                if (outcome.equals(KEY_OUTCOME_WIN)) {
                    homeWins++;
                } else if (outcome.equals(KEY_OUTCOME_LOSS)) {
                    homeLosses++;
                } else if (outcome.equals(KEY_OUTCOME_TIE)) {
                    homeTies++;
                }
                homeRunsScored += rs;
                homeRunsAllowed += ra;
            } else {
                if (outcome.equals(KEY_OUTCOME_WIN)) {
                    roadWins++;
                } else if (outcome.equals(KEY_OUTCOME_LOSS)) {
                    roadLosses++;
                } else if (outcome.equals(KEY_OUTCOME_TIE)) {
                    roadTies++;
                }
                roadRunsScored += rs;
                roadRunsAllowed += ra;
            }
        }

        public int getTotalGames() {
            return getHomeGames() + getRoadGames();
        }

        public int getTotalWins() {
            return homeWins + roadWins;
        }

        public int getTotalLosses() {
            return homeLosses + roadLosses;
        }

        public int getTotalTies() {
            return homeTies + roadTies;
        }

        public int getTotalRunsScored() {
            return homeRunsScored + roadRunsScored;
        }

        public int getTotalRunsAllowed() {
            return homeRunsAllowed + roadRunsAllowed;
        }

        public int getHomeGames() {
            return homeWins + homeLosses + homeTies;
        }

        public int getHomeWins() {
            return homeWins;
        }

        public int getHomeLosses() {
            return homeLosses;
        }

        public int getHomeTies() {
            return homeTies;
        }

        public int getHomeRunsScored() {
            return homeRunsScored;
        }

        public int getHomeRunsAllowed() {
            return homeRunsAllowed;
        }

        public int getRoadGames() {
            return roadWins + roadLosses + roadTies;
        }

        public int getRoadWins() {
            return roadWins;
        }

        public int getRoadLosses() {
            return roadLosses;
        }

        public int getRoadTies() {
            return roadTies;
        }

        public int getRoadRunsScored() {
            return roadRunsScored;
        }

        public int getRoadRunsAllowed() {
            return roadRunsAllowed;
        }
    }
    
    private static final String KEY_OUTCOME_WIN = "win";
    private static final String KEY_OUTCOME_LOSS = "loss";
    private static final String KEY_OUTCOME_TIE = "tie";

    public static final int INDEX_TOTAL_GAMES = 0;
    public static final int INDEX_TOTAL_WINS = 1;
    public static final int INDEX_TOTAL_LOSSES = 2;
    public static final int INDEX_TOTAL_TIES = 3;
    public static final int INDEX_TOTAL_RS = 4;
    public static final int INDEX_TOTAL_RA = 5;
    public static final int INDEX_HOME_GAMES = 6;
    public static final int INDEX_HOME_WINS = 7;
    public static final int INDEX_HOME_LOSSES = 8;
    public static final int INDEX_HOME_TIES = 9;
    public static final int INDEX_HOME_RS = 10;
    public static final int INDEX_HOME_RA = 11;
    public static final int INDEX_ROAD_GAMES = 12;
    public static final int INDEX_ROAD_WINS = 13;
    public static final int INDEX_ROAD_LOSSES = 14;
    public static final int INDEX_ROAD_TIES = 15;
    public static final int INDEX_ROAD_RS = 16;
    public static final int INDEX_ROAD_RA = 17;
    
    private String teamId;

    private HashMap<String, RecordRow> recordRows;

    public RecordMatrix(String teamId) {
        this.teamId = teamId;
        recordRows = new HashMap<>();
    }

    public String getTeamId() {
        return teamId;
    }

    public <T extends SingleGameTeam> void addTeamGame(T team) throws IllegalArgumentException {
        /* 
         * A team may only be added to records if it matches the teamId set 
         * in the constructor. 
         */
        if (!teamId.equals(team.getTeamId())) {
            throw new IllegalArgumentException("teamId found in argument team did not match" +
                " this instance of RecordMatrix's teamId");
        }

        String opponentId = team.getOpponentId();
        String outcome = team.getGameOutcome();
        int rs = team.getTotalRunsScored();
        int ra = team.getTotalRunsAllowed();
        boolean homeTf = team.getHomeTF();

        if (recordRows.containsKey(opponentId)) {
            recordRows.get(opponentId).updateRow(outcome, homeTf, rs, ra);
        } else {
            RecordRow newRow = new RecordRow();
            newRow.updateRow(outcome, homeTf, rs, ra);
            recordRows.put(opponentId, newRow);
        }
    }

    public List<String> getOpponentIds() {
        List<String> oppIds = new LinkedList<String>(recordRows.keySet());
        Collections.sort(oppIds);
        // String[] oppIds = new String[recordRows.size()];
        // int i = 0;
        // for (String id : recordRows.keySet()) {
        //     oppIds[i] = id;
        //     i++;
        // }
        return oppIds;
    }

    public int[] getRecord(String opponentId) {
        RecordRow rec;
        int[] returnArr = new int[18];
        /* 
         * If records does not contain a row of the specified key, return 
         * an empty zero-filled array. 
         */
        if (!recordRows.containsKey(opponentId)) {
            return returnArr;
        } else {
            rec = recordRows.get(opponentId);
        }

        returnArr[INDEX_TOTAL_GAMES] = rec.getTotalGames();
        returnArr[INDEX_TOTAL_WINS] = rec.getTotalWins();
        returnArr[INDEX_TOTAL_LOSSES] = rec.getTotalLosses();
        returnArr[INDEX_TOTAL_TIES] = rec.getTotalTies();
        returnArr[INDEX_TOTAL_RS] = rec.getTotalRunsScored();
        returnArr[INDEX_TOTAL_RA] = rec.getTotalRunsAllowed();

        returnArr[INDEX_HOME_GAMES] = rec.getHomeGames();
        returnArr[INDEX_HOME_WINS] = rec.getHomeWins();
        returnArr[INDEX_HOME_LOSSES] = rec.getHomeLosses();
        returnArr[INDEX_HOME_TIES] = rec.getHomeTies();
        returnArr[INDEX_HOME_RS] = rec.getHomeRunsScored();
        returnArr[INDEX_HOME_RA] = rec.getHomeRunsAllowed();

        returnArr[INDEX_ROAD_GAMES] = rec.getRoadGames();
        returnArr[INDEX_ROAD_WINS] = rec.getRoadWins();
        returnArr[INDEX_ROAD_LOSSES] = rec.getRoadLosses();
        returnArr[INDEX_ROAD_TIES] = rec.getRoadTies();
        returnArr[INDEX_ROAD_RS] = rec.getRoadRunsScored();
        returnArr[INDEX_ROAD_RA] = rec.getRoadRunsAllowed();

        return returnArr;
    }

    public int[] getTotals() {
        int returnArr[] = new int[18];

        for (String key : recordRows.keySet()) {
            int[] temp = getRecord(key);
            for (int i = 0; i < temp.length; i++) {
                returnArr[i] += temp[i];
            }
        }
        return returnArr;
    }
}