/*
 * C-style comment with author/copyright information...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.util.ArrayList;
import java.util.LinkedList;

public class Team {
    private ArrayList<LinkedList<BxScrPositionPlayer>> lineup;
    private LinkedList<BxScrPitcher> pitchers;
    private ArrayList<Integer> linescore;
    private String teamID, city, name;
    private int totLeftOnBase, totalDP, totalTP;
    private boolean homeTF;

    /**
     * Construct a new <code>Team</code>.
     * @param id Team's three-character ID (i.e., <code>"NYA"</code>).
     * @param city The name of this team's city (i.e. <code>"New York"</code>).
     * @param name This team's name (i.e. <code>"Yankees"</code>).
     * @param home <code>true</code> if this is the home team, <code>false</code>
     *        if this is the visitor.
     */
    public Team(String id, String city, String name, boolean home) {
        teamID = id;
        this.city = city;
        this.name = name;
        this.homeTF = home;
        lineup = new ArrayList<>(9);
        pitchers = new LinkedList<>();
        linescore = new ArrayList<>(30);
        
        for (int i = 0; i < 9; i++) {
            lineup.add(new LinkedList<BxScrPositionPlayer>());
        }
    }

    /**
     * Add either a double or triple play to this object's stat trackers.
     * 
     * @param isDP <code>true</code> if adding a double play, <code>false</code>
     * if adding a triple play.
     * 
     * @param n Number of plays to increment by.
     */
    public void add_double_triple_plays(boolean isDP, int n) {
        if (isDP)
            totalDP += n;
        else
            totalTP += n;
    }

    /**
     * 
     * @param isDP
     * @return
     */
    public int get_double_triple_plays(boolean isDP) {
        if (isDP)
            return totalDP;
        else
            return totalTP;
    }

    /**
     * Increment team's LOB total. This method should be called
     * at the end of each inning.
     * 
     * @param lob Runners left on base in a single inning.
     */
    public void addLOB(int lob) {
        totLeftOnBase += lob;
    }

    /**
     * Calculate runners left on base in team's half inning, then
     * add to total for game.
     * 
     * @param pa Plate appearances in half-inning.
     * @param runs Runs scored.
     * @param outs Outs made (this should usually be 3).
     */
    public void addLOB(int pa, int runs, int outs) {
        addLOB(pa-runs-outs);
    }

    /** @return Team stat runners left on base */
    public int get_lob() {
        return totLeftOnBase;
    }

    /**
     * Set decision <code>decKey</code> for player with ID <code>playerID</code>,
     * if <code>Team</code> contains that play.
     * @param decKey <code>BaseballPlayer.DECISION_WIN</code>,
     *        <code>BaseballPlayer.DECISION_LOSS</code>, or <code>
     *        BaseballPlayer.DECISION_SAVE</code>.
     * @param playerID The pitcher's ID.
     */
    public void setPitDecision(char decKey, String playerID) {
        for (BxScrPitcher p : pitchers) {
            if (p.getPlayerID().equals(playerID)) {
                p.setDecision(decKey);
            }
        }
    }

    /**
     * Set earned runs. If player is found and charged with those earned runs,
     * return true; if player is not found, return false.
     * 
     * @param pitcherID
     * @param er
     * @return
     */
    public boolean setEarnedRuns(String pitcherID, int er) {
        for (BxScrPitcher p : pitchers) {
            if (p.getPlayerID().equals(pitcherID)) {
                p.incrementStats(BaseballPlayer.KEY_ER);
                return true;
            }
        }
        return false;
    }

    /** @return team's ID. */
    public String getTeamID() {
        return teamID;
    }

    /** @return team's city. */
    public String getCity() {
        return city;
    }

    /** @return team's nickname. */
    public String getName() {
        return name;
    }

    /** @return <code>true</code> for home team, <code>false</code> for visitor. */
    public boolean getHomeTF() {
        return homeTF;
    }

    /** @return team's total at-bats, runs, hits, and RBI. */
    public int[] getLineupStats() {
        
        int[] stats = {0,0,0,0};
        
        String[] keys = {
            BaseballPlayer.KEY_AB, BaseballPlayer.KEY_R,
            BaseballPlayer.KEY_H, BaseballPlayer.KEY_RBI
        };
        
        for (LinkedList<BxScrPositionPlayer> ar : lineup) {
            for (BxScrPositionPlayer p : ar) {
                int[] pStats = p.getStats(keys);
                for (int i = 0; i < 4; i++)
                    stats[i] += pStats[i];
            }
        }
        return stats;
    }

    /** 
     * @return team's total batters retired, hits, runs, earned runs,
     * walks, and strikeouts.
     */
    public int[] getPitchingStats() {
        int[] stats = {0,0,0,0,0,0};
        String[] keys = {
            BaseballPlayer.KEY_BATTERS_RETIRED,
            BaseballPlayer.KEY_H,
            BaseballPlayer.KEY_R,
            BaseballPlayer.KEY_ER,
            BaseballPlayer.KEY_BB,
            BaseballPlayer.KEY_SO
        };
        
        for (BxScrPitcher p : pitchers) {
            int[] pStats = p.getStats(keys);
            for (int i = 0; i < 6; i++)
                stats[i] += pStats[i];
        }
        
        return stats;
    }

    /**
     * Award errors and passed balls.
     * 
     * @param statKey The statistical category
     * @param position The position of the player charged.
     */
    public void awardFieldingStats(String statKey, int position) {
        statKey = statKey.toLowerCase();

        if (statKey.equals(BaseballPlayer.KEY_E)) {
            /* 
             * To account for both dh and non-dh games, errors committed
             * by pitchers are stored in the Pitcher objects, not in their
             * BxScrPositionPlayer entities.  
             */
            if (position == BaseballPlayer.PITCHER) {
                getCurrentPitcher().incrementStats(BaseballPlayer.KEY_E,1);
            } else {
                for (LinkedList<BxScrPositionPlayer> ar : lineup) {
                    if (ar.getLast().getCurrentPosition() == position) {
                        ar.getLast().incrementStats(BaseballPlayer.KEY_E,1);
                        return;
                    }
                }
            }
        } else if (statKey.equals(BaseballPlayer.KEY_PB)) {
            for (LinkedList<BxScrPositionPlayer> ar : lineup) {
                if (ar.getLast().getCurrentPosition() == BaseballPlayer.CATCHER) {
                    ar.getLast().incrementStats(BaseballPlayer.KEY_PB,1);
                }
            }
        }
    }

    /**
     * Add a player to team's lineup. If that lineup spot is already filled, the
     * new player created will be a substitution.
     * 
     * @param start <code>true</code> 
     * @param id Player's unique ID.
     * @param first Player's first name.
     * @param last Player's last name.
     * @param spot Player's lineup spot (<code>1</code> through <code>9</code>, or
     *        <code>0</code> for pitchers in DH games).
     * @param pos Player's position.
     */
    public void addPlayer(boolean start, String id, String first, String last,
            String spot, String pos) throws IllegalArgumentException {
        int spotInt = Integer.parseInt(spot);
        int posInt = Integer.parseInt(pos);
        /* 
         * If player is already in lineup, then he is changing positions.
         * If he is not in lineup, he is entering the game and must be added.
         */
        if (containsBxScrPositionPlayer(id)) {
            lineup.get(spotInt-1).getLast().changePosition(posInt);
        } else {
            if (spotInt > 0) {
                /* 
                 * Account for the rare instance that a DH-rule pitcher moves to 
                 * a position after entering the game earlier as a pitcher. 
                 */
                if (containsPitcher(id)) {
                    BxScrPositionPlayer newB = new BxScrPositionPlayer(
                        id,first,last,spotInt,BxScrPositionPlayer.PITCHER);
                    newB.changePosition(posInt);
                    lineup.get(spotInt-1).add(newB);
                } else {
                    BxScrPositionPlayer newB = new BxScrPositionPlayer(
                        id,first,last,spotInt,posInt);
                    lineup.get(spotInt-1).add(newB);
                }
            }
        }
        if (posInt == BxScrPositionPlayer.PITCHER) {

            /* New pitcher entering the game -- add to game staff. */
            if (!containsPitcher(id)) {
                BxScrPitcher newP = new BxScrPitcher(id, first, last);
                pitchers.add(newP);
            }
        }
    }

    /**
     * 
     * @param id A unique player ID to search team for.
     * 
     * @return <code>true</code> if this object includes a 
     * <code>BxScrPitcher</code> whose <code>playerID</code> matches
     * parameter <code>id</code>.
     */
    public boolean containsPitcher(String id) {
        for (BxScrPitcher p : pitchers) {
            if (p.getPlayerID().equals(id))
                return true;
        }
        return false;
    }

    /**
     * Check if this team contains <code>BxScrPositionPlayer</code> matching
     * <code>playerId</code>.
     * 
     * @param playerID Player's ID.
     * @return <code>true</code> if this team contains player, <code>false</code>
     * otherwise.
     */
    private boolean containsBxScrPositionPlayer(String playerID) {
        for (LinkedList<BxScrPositionPlayer> list : lineup) {
            for (BxScrPositionPlayer p : list) {
                if (p.getPlayerID().equals(playerID))
                    return true;
            }
        }
        return false;
    }

    /** @return team's inning linescore. */
    public int[] getLinescore() {
        int[] lsArr = new int[linescore.size()];
        
        for(int i = 0; i < linescore.size(); i++) {
            lsArr[i] = linescore.get(i);
        }

        return lsArr;
    }

    /**
     * At the end of an inning, add runs scored in inning to team's linescore.
     * 
     * @param runs The runs scored by this team in the current inning.
     */
    public void linescoreAdd(int runs) {
        linescore.add(runs);
    }

    /** @return total runs scored by the team. */
    public int getTotalRuns() {
        int t = 0;
        for (int r : linescore) {
            t+=r;
        }
        return t;
    }

    /**
     * <p>Return a <code>String</code> representing the team's linescore, with a 
     * space ever <code>interval</code> innings or <code>spaces</code> spaces long.</p>
     * 
     * <p><b>Note:</b> The <code>String</code> produced by this method only reflects
     * the innings added to this particular team's linescore list, not the innings
     * played in the game. Therefore, calling this method for a home team leading
     * at the end of a game will result in a <code>String</code> one character shorter
     * than the visitor's.</p>
     * @param interval The interval at which spacing is added.
     * @param spaces The number of spaces added at the specified interval.
     * @return a <code>String</code> representation of the team's linescore.
     */
    public String linescoreToString(int interval, int spaces) 
            throws IllegalArgumentException {
        String s = "";
        
        if (interval < 0 || spaces < 0) {
            throw new IllegalArgumentException("interval and spaces must be " +
                "positive integers.");
        }

        for (int i = 0; i < linescore.size(); i++) {
            if (i > 0 && i%interval == 0)
                // s += " ";
                s += String.format("%" + spaces + "s", "");
            s += linescore.get(i);
        }
        return s.trim();
    }

    /** @return pitcher currently in the game for this team. */
    public BxScrPitcher getCurrentPitcher() {
        return pitchers.getLast();
    }

    /**
     * 
     * @param id
     * @return
     */
    public BxScrPitcher getPitcher(String id) {
        for (BxScrPitcher p : pitchers) {
            if (p.getPlayerID().equals(id))
                return p;
        }
        return null; 
    }

    /**
     * 
     * @param spot
     * @return
     */
    public BxScrPositionPlayer getLineupSpot(int spot) {
        return lineup.get(spot).getLast();
    }

    /**
     * 
     * @param id
     * @return
     */
    public BxScrPositionPlayer getBxScrPositionPlayer(String id) {
        for (LinkedList<BxScrPositionPlayer> list : lineup) {
            for (BxScrPositionPlayer p : list) {
                if (p.getPlayerID().equals(id))
                    return p;
            }
        }
        return null;
    }

    /** @return the team's lineup. */
    public ArrayList<LinkedList<BxScrPositionPlayer>> getLineup() {
        return lineup;
    }

    /** @return a list of all pitchers who played for team in game */
    public LinkedList<BxScrPitcher> getPitchingStaff() {
        return pitchers;
    }
}