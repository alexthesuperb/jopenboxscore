/*
 * C-style comment with author/copyright information...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;

/**
 * Single-game implementation of abstract class
 * <code>com.alexwimer.bsblib.BaseballPlayer</code>.
 * This class contains methods and members relevant to a 
 * pitcher's single-game performance. Specifically, it tracks
 * the number of outs recorded and batters faced in each inning.
 * 
 * @since 2020-03-17
 * @version 1.0.0
 * @author Alexander C. Wimer
 */
public class SingleGamePitcher extends BaseballPlayer {

    /** Pitching decision. */
    private char decision;

    /** List of the players hit by pitches. */
    private LinkedList<SingleGamePositionPlayer> battersHBP;

    /** Numbers of outs recorded in the current inning */
    private int inningOutsRecorded;

    /** Number of batters faced in the current inning. */
    private int inningBF;

    /** The inning in which the pitcher was removed. */
    private int inningRemoved;

    /**
     * Constructor calls superclass' constructor to initialize ID
     * information, then initialize local variables to reflect 
     * a player just entering the game.
     * 
     * @param playerID Player's unique ID.
     * @param firstName Player's first name.
     * @param lastName Player's last name.
     */
    public SingleGamePitcher(String playerID, String firstName, String lastName) {
        super(playerID, firstName, lastName);
        battersHBP = new LinkedList<>();
        stats.put(BaseballPlayer.KEY_G, 1);
        decision = DECISION_NONE;
        startNewInning();
    }
    
    /** @return The pitching decision awarded. */
    public char getDecision() {
        return decision;
    }

    /**
     * Set pitcher decision.
     * 
     * @param decision The pitching decision (win, loss, or save).
     */
    public void setDecision(char decision) {
        this.decision = decision;
    }

    /**
     * Set the inning the pitcher was removed from the game.
     * 
     * @param inning The inning the pitcher was removed.
     */
    public void setInningRemoved(int inning) {
        this.inningRemoved = inning; 
    }

    /** @return the inning the pitcher was removed. */
    public int getInningRemoved() {
        return inningRemoved;
    }

    /** @return Number of batters faced in pitcher's final inning. */
    public int getInningBF() {
        return inningBF;
    }

    /**
     * If a pitcher is pulled from an inning before recording an out,
     * that is noted in the boxscore. However, if <code>inningBF == 0</code>,
     * then the pitcher was pulled at the end of the previous inning, and
     * should not be counted. 
     * 
     * @return <code>true</code> if pitcher was removed from
     * an inning before recording an out; <code>false</code>
     * otherwise.
     */
    public boolean removedFromInningWithoutRecordingOut() {
        return (inningOutsRecorded == 0 && inningBF > 0) ? true : false;
    }

    /**
     * Call <code>this.incrementStats(key, 1)</code>.
     * 
     * @param key The stat's key.
     */
    @Override
    public void incrementStats(String key) {
        this.incrementStats(key, 1);
    }

    /**
     * 
     */
    @Override 
    public void incrementStats(String key, int n) {
        
        super.incrementStats(key,n);
        
        if (key.equals(KEY_BATTERS_RETIRED)) {
            inningOutsRecorded += n;
        }
    }

    /**
     * When the pitcher hits a batter, store that
     * <code>BxScrPositionPlayer</code> in a private list.
     * 
     * @param batter <code>BxScrPositionPlayer</code> hit by pitch.
     */
    public void addBattersHBP(SingleGamePositionPlayer batter) {
        battersHBP.add(batter);
    }

    /**
     * Return the batters hit by pitcher.
     * 
     * @return The batters hit.
     */
    public LinkedList<SingleGamePositionPlayer> getBattersHBP() {
        return battersHBP;
    }

    /** Reset inning-specific counters. */
    public void startNewInning() {
        inningOutsRecorded = 0;
        inningBF = 0;
    }

    /**
     * Return an array of boxscore stat line statistics. Because 
     * innings pitched is not an integer, <code>stats.get(KEY_BATTERS_RETIRED)</code>
     * is returned instead and must be converted to a human-readable value.
     * 
     * @return The array of stats.
     */
    public int[] getBxScrStats() {
        String[] kArr = {KEY_BATTERS_RETIRED, 
                         KEY_PITCHER_H,
                         KEY_PITCHER_R, 
                         KEY_PITCHER_ER,
                         KEY_PITCHER_BB,
                         KEY_PITCHER_SO};
        return getStats(kArr);
    }

    /**
     * Increment private member <code>inningBF</code>.
     */
	public void incrementInningBF() {
        inningBF++;
	}

}

// END OF FILE