package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;

/**
 * An implementation of abstract class <code>BaseballPlayer</code>.
 * This class should only be used to represent a position player within a 
 * single game.
 */
public class SingleGamePositionPlayer extends BaseballPlayer {

    /** Spot in the lineup. This is an index, ranging from 0 to 8. */
    private int lineupSpot;

    /** A list of all positions manned by the player, in sequential order. */
    private LinkedList<Integer> posList;

    /** The <code>Pitcher</code> held responsible for this player reaching base. */
    SingleGamePitcher pitcherCharged;

    /**
     * Parse <code>String lineupSpot</code> and <code>position</code> and 
     * call <code>BxScrPositionPlayer(String playerID, String firstName, 
     * String lastName, int lineupSpot, int position)</code>.
     * 
     * @param playerID The player's unique ID.
     * @param firstName Player's first name.
     * @param lastName Player's last name.
     * @param lineupSpot Player's lineup spot. 
     * @param position Player's position.
     */
    public SingleGamePositionPlayer(String playerID, String firstName, String lastName, 
            String lineupSpot, String position) {

        this(playerID, firstName, lastName, Integer.parseInt(lineupSpot), 
            Integer.parseInt(position));
    }

    /**
     * Initialize object with ID, name, and game-specific lineup spot and position
     * information.
     * 
     * @param playerID The player's unique ID.
     * @param firstName Player's first name.
     * @param lastName Player's last name.
     * @param lineupSpot Player's lineup spot. 
     * @param position Player's position.
     */
    public SingleGamePositionPlayer(String playerID, String firstName, String lastName,
            int lineupSpot, int position) {
        super(playerID, firstName, lastName);
        posList = new LinkedList<>();
        this.lineupSpot = lineupSpot;
        posList.add(position);
        stats.put(BaseballPlayer.KEY_G, 1);
    }

    /**
     * Set the <code>Pitcher</code> responsible with this player
     * reaching base.
     * @param pitcher The pitcher charged.
     */
    public void setPitcherCharged(SingleGamePitcher pitcher) {
        pitcherCharged = pitcher;
    }

    /** Set <code>pitcherCharged</code> to <code>null</code>. */
    public void clearPitcherCharged() {
        setPitcherCharged(null);
    }

    /**
     * Return the pitcher charged with this player reaching base.
     * 
     * @return The pitcher charged. This value may be <code>null</code>.
     */
    public SingleGamePitcher getPitcherCharged() {
        return pitcherCharged;
    }

    /**
     * Append <code>posList</code> with new position.
     * 
     * @param newPos The new position.
     */
    public void changePosition(String newPos) {
        changePosition(Integer.valueOf(newPos));
    }

    /**
     * Append <code>posList</code> with new position.
     * 
     * @param newPos The new position.
     */
    public void changePosition(int newPos) {
        posList.add(newPos);
    }

    /**
     * Return the player's current position.
     * 
     * @return <code>posList.getLast()</code>
     */
    public int getCurrentPosition() {
        return posList.getLast();
    }

    /**
     * Get the player's lineup spot.
     * 
     * @return The player's lineup spot.
     */
    public int getLineupSpot() {
        return lineupSpot;
    }

    /** 
     * @return an array of positions played by the player in
     * this game. This list is chronologically ordered: that is, the 
     * <code>int</code> at index <code>0</code> represents the first
     * position played by the player, and index <code>length - 1</code> 
     * represents the position he manned when either the game ended or he
     * was pulled from the game.
     */
    public int[] getPositionsList() {
        int[] positions = new int[posList.size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = posList.get(i);
        }
        return positions;
    }

    /**
     * Return a <code>String</code> of dash-delimited positions
     * manned by the player, as would appear in a classic boxscore. 
     * 
     * @return The positions manned by the player.
     */
    public String getPositionString() {
        String str = "";

        for (Integer i : posList) {
            switch(i) {
            case PITCHER:
                str += "p-";
                break;
            case CATCHER:
                str += "c-";
                break;
            case FIRST_BASEMAN:
                str += "1b-";
                break;
            case SECOND_BASEMAN:
                str += "2b-";
                break;
            case THIRD_BASEMAN:
                str += "3b-";
                break;
            case SHORT_STOP:
                str +="ss-";
                break;
            case LEFT_FIELDER:
                str += "lf-";
                break;
            case CENTER_FIELDER:
                str += "cf-";
                break;
            case RIGHT_FIELDER:
                str += "rf-";
                break;
            case DESIGNATED_HITTER:
                str += "dh-";
                break;
            case PINCH_HITTER:
                str += "ph-";
                break;
            case PINCH_RUNNER:
                str += "pr-";
            }
        }
        return (str.length() > 0) ? str.substring(0,str.length()-1) : str;
    }

    /**
     * Return an array of boxscore stat line statistics.
     * @return The array of stats.
     */
    public int[] getBxScrStats() {
        String[] kArr = {KEY_AB, KEY_R, KEY_H, KEY_RBI};
        return getStats(kArr);
    }

}

// END OF FILE