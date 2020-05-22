package com.github.alexthesuperb.jopenboxscore;

import java.util.HashMap;

/**
 * <code>BaseballPlayer</code> is an abstract class that should
 * be extended to create more meaningful classes, such as pitchers
 * and hitters. The static <code>numObj</code> member tracks the number
 * of instances of this class that have been created.
 * 
 * @author Alexander C. Wimer
 * @version 1.0.0
 * @since 2020-4-17
 */
public abstract class BaseballPlayer implements SportsStatContainer,
        Comparable<BaseballPlayer> {

    /** <code>key</code> for games played. */
    public static final String KEY_G = "g";

    /** <code>key</code> for games started. */
    public static final String KEY_GS = "gs";

    /** <code>key</code> for at-bats. */
    public static final String KEY_AB = "ab";

    /** <code>key</code> for runs scored. */
    public static final String KEY_R = "r";  

    /** <code>key</code> for hits. */
    public static final String KEY_H = "h";
    
    /** <code>key</code> for runs batted in. */
    public static final String KEY_RBI = "rbi";
    
    /** <code>key</code> for singles. */
    public static final String KEY_1B = "1b"; 

    /** <code>key</code> for doubles. */
    public static final String KEY_2B = "2b"; 
    
    /** <code>key</code> for triples. */
    public static final String KEY_3B = "3b";
    
    /** <code>key</code> for home runs hit/allowed. */
    public static final String KEY_HR = "hr";
    
    /** <code>key</code> for hit by pitches. */
    public static final String KEY_HBP = "hbp"; 
    
    /** <code>key</code> for stolen bases. */
    public static final String KEY_SB = "sb"; 
    
    /** <code>key</code> for times caught stealing. */
    public static final String KEY_CS = "cs"; 
    
    public static final String KEY_PO = "po";
    
    /** <code>key</code> for strikeouts. */
    public static final String KEY_SO = "so";
    
    /** <code>key</code> for walks. */
    public static final String KEY_BB = "bb"; 

    /** <code>key</code> for sacrafice flies. */
    public static final String KEY_SF = "sf";

    /** <code>key</code> for sacrafice hits. */
    public static final String KEY_SH = "sh";

    /** <code>key</code> for errors. */
    public static final String KEY_E = "e";

    /** <code>key</code> for games started by a pitcher */
    public static final String KEY_PITCHER_GS = "gs";

    /** <code>key</code> for outs recorded by a pitcher */
    public static final String KEY_BATTERS_RETIRED = "br";

    /**<code>key</code> for hits allowed. */
    public static final String KEY_PITCHER_H = "hits_allowed";

    /** <code>key</code> for batters struck out by pitcher. */
    public static final String KEY_PITCHER_SO = "pitcher_strikeouts";

    /** <code>key</code> for runs allowed by pitcher. */
    public static final String KEY_PITCHER_R = "runs_allowed";
    
    /** <code>key</code> for earned runs allowed by pitcher. */
    public static final String KEY_PITCHER_ER = "earned_runs";

    /** <code>key</code> for walks allowed by a pitcher. */
    public static final String KEY_PITCHER_BB = "walks_allowed"; 

    /**<code>key</code> for wild pitches. */
    public static final String KEY_PITCHER_WP = "wp";

    /**<code>key</code> for balks. */
    public static final String KEY_PITCHER_BK = "bk";

    /**<code>key</code> for the <i>statistical category</i> pitcher wins. */
    public static final String KEY_PITCHER_WINS = "pitcher_wins";

    /**<code>key</code> for the <i>statistical category</i> pitcher losses. */
    public static final String KEY_PITCHER_LOSSES = "pitcher_losses";

    /**<code>key</code> for the <i>statistical category</i> pitcher saves. */
    public static final String KEY_PITCHER_SAVES = "pitcher_saves";

    /**<code>key</code> for passed balls. */
    public static final String KEY_PB = "pb";

    /**<code>key</code> for pickoff */
    public static final String KEY_PICKOFF = "po";

    /**<code>key</code> for pickoff/caught-stealing */
    public static final String KEY_POCS = "po";

    /**<code>key</code> for OA (unidentified, non-batter) */
    public static final String KEY_OA = "oa";
    
    /** Standard baseball code for pitcher position. */
    public static final int PITCHER = 1;

    /** Standard baseball code for catcher position. */
    public static final int CATCHER = 2;

    /**  Standard baseball code for first baseman position. */
    public static final int FIRST_BASEMAN = 3;

    /** Standard baseball code for second baseman position. */
    public static final int SECOND_BASEMAN = 4;

    /** Standard baseball code for third baseman position. */
    public static final int THIRD_BASEMAN = 5;

    /** Standard baseball code for shortstop position. */
    public static final int SHORT_STOP = 6;

    /** Standard baseball code for leftfielder position. */
    public static final int LEFT_FIELDER = 7;

    /** Standard baseball code for centerfielder position. */
    public static final int CENTER_FIELDER = 8;

    /** Standard baseball code for rightfielder position. */
    public static final int RIGHT_FIELDER = 9;

    /** Standard baseball code for DH position position. */
    public static final int DESIGNATED_HITTER = 10;

    /** Standard baseball code for pinch hitter position. */
    public static final int PINCH_HITTER = 11;

    /** Standard baseball code for pinch runner position. */
    public static final int PINCH_RUNNER = 12;

    /** Pitcher win */
    public static final char DECISION_WIN = 'W';

    /** Pitcher loss */
    public static final char DECISION_LOSS = 'L';

    /** Pitcher save */
    public static final char DECISION_SAVE = 'S';

    /** Pitcher no decision */
    public static final char DECISION_NONE = '\0';

    /** <code>HashMap</code> to store various statistics accumulated. */
    protected HashMap<String, Integer> stats;

    /** Player last name */
    protected String lastName;

    /** Player first name */
    protected String firstName;

    /** Unique player ID */
    protected String playerId;

    /** The number of instances of this class. */
    protected static int numObj;

    /**
     * Overloaded constructor initializes <code>stats</code> and sets player
     * identification information. Static <code>numObj</code> is incremented
     * so that the number of objects created can be tracked.
     * 
     * @param playerID The player's unique ID.
     * @param firstName The player's first name.
     * @param lastName The player's last name.
     */
    public BaseballPlayer(String playerID, String firstName, String lastName) {
        this.playerId = playerID;
        this.firstName = firstName;
        this.lastName = lastName;
        stats = new HashMap<>();
        numObj++;
    }

    /** Checks stats map for key. If <code>key</code> already exists,
     * increment value at key by <code>n</code>. If stats does not
     * contain key, add to map.
     * 
     * @param key The stat being incremented.
     * @param n How much to increment stat by.
     */
    public void incrementStats(String key, int n) {
        
        if (stats.containsKey(key)) {
            Integer update = stats.get(key) + n;
            stats.put(key, update);
        } else {
            stats.put(key,n);
        }
    }

    /**
     * Increment value at <code>key</code> by <code>1</code>.
     * 
     * @param key The key of the stat to be increment.
     */
    public void incrementStats(String key) {
        if (stats.containsKey(key)) {
            Integer update = stats.get(key) + 1;
            stats.put(key, update);
        } else {
            stats.put(key,1);
        }
    }

    /**
     * Return a single stat from <code>stats</code>.
     * @param key The key of the stat to be returned.
     * @return <code>stats.get(key)</code> if <code>stats</code> 
     * contains <code>key</code>; <code>0</code> if it does not.
     */
    public int getStat(String key) {
        return (stats.containsKey(key)) ? stats.get(key) : 0;
    }

    /**
     * Search <code>stats</code> for the the arguments and return
     * in an array.
     * 
     * @param keys An array of keys for 
     * <code>HashMap&#60String, Integer&#62</code>.
     * @return An array of the requested stats.
     */
    public int[] getStats(String[] keys) {
        
        int[] arr = new int[keys.length];

        for (int i = 0; i < keys.length; i++) {
            arr[i] = getStat(keys[i]);
        }

        return arr;
    }

    /** @return a deep copy of player's stats. */
    public HashMap<String, Integer> getStatsHashMap() {
       
        HashMap<String,Integer> copy = new HashMap<>();
        
        for (String k : stats.keySet()) {
            copy.put(k, stats.get(k));
        }
        return copy;
    }

    /** 
     * Set <code>firstName</code> and <code>lastName</code>. 
     * 
     * @param firstName The player's first name.
     * @param lastName The player's last name.
     */
    public void setName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Set <code>playerID</code>.
     *
     * @param playerID The player's ID. 
     */
    public void setPlayerID(String playerID) {
        this.playerId = playerID;
    }

    /** 
     * Returns <code>playerID</code>. 
     * 
     * @return <code>playerID</code> The player's ID. 
     */
    public String getPlayerId() {
        return playerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    /** 
     * Default player name signature, as would appear in a boxscore.
     * 
     * @return <code>lastName + " " + firstName.charAt(0)</code>
     */
    public String getName() {
        return lastName + " " + firstName.charAt(0);
    }

    /**
     * Convert The number of batters retired by a pitcher into
     * a boxscore-style innings pitched total. This number is 
     * expressed as the number of whole innings, a decimal point,
     * and the remaining thirds of an inning completed. For example,
     * <code>retired = 13</code> would be expressed as
     * <code>ip = 4.1</code>.
     * 
     * @param outsRecorded The number of batters retired.
     * @return Innings pitched if <code>outsRecorded >= 0</code>; otherwise,
     * <code>"NaN"</code>. 
     */
    public static String convertToIP(int outsRecorded) {

        if (outsRecorded < 0) {
            return "NaN";
        } else {
            String wholeIP;
            String fractionIP;
    
            wholeIP = String.valueOf(outsRecorded/3);
            fractionIP = String.valueOf(outsRecorded%3);
    
            return wholeIP + "." + fractionIP;
        }
    }

    public static float getOnBasePercentage(int atBats, int hits, int walks, 
            int hbp, int sacFlies) {
        return (float)(hits + walks + hbp)/(float)(atBats + walks + hbp + sacFlies);
    }

    public static float getSluggingPercentage(int atBats, int hits, int doubles,
            int triples, int homeruns) {
        return (float)(hits + doubles + 2*triples + 3*homeruns)/(float)atBats;
    }

    public static float getBattingAverage(int atBats, int hits) {
        return (float)hits/(float)atBats;
    }

    public static float getEarnedRunAverage(int earnedRuns, int battersRetired) {
        return (float)9 * (float)earnedRuns / ((float)battersRetired / (float)3);
    }

    @Override
    public int compareTo(BaseballPlayer o) {
        int compare = this.playerId.compareTo(o.playerId);
        if (compare == 0) {
            compare = this.lastName.compareTo(o.lastName);
            if (compare == 0) {
                return this.firstName.compareTo(o.firstName);
            } else {
                return compare;
            }
        } else {
            return compare;
        }
    }

}

// END OF FILE