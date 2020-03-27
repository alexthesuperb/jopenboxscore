/*
 * C-style comment with author/copyright information...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.util.ArrayList;
import java.util.LinkedList;

public class Team {

    private ArrayList<LinkedList<BxScrPositionPlayer>> lineup = new ArrayList<>(9);
    private LinkedList<BxScrPitcher> pitchers = new LinkedList<>();
    private ArrayList<Integer> linescore = new ArrayList<>(30);
    private String teamID, city, name;
    private int totLeftOnBase, totalDP, totalTP;
    private boolean homeTF;

    /**
     * 
     * @param id
     * @param city
     * @param name
     * @param home
     */
    public Team(String id, String city, String name, boolean home) {
        teamID = id;
        this.city = city;
        this.name = name;
        this.homeTF = home;
        for (int i = 0; i < 9; i++) {
            lineup.add(new LinkedList<BxScrPositionPlayer>());
        }
    }

    /**
     * 
     */
    public Team() {
        teamID = "";
        city = "";
        name = "";
        homeTF = false;
        for (int i = 0; i < 9; i++) {
            lineup.add(new LinkedList<BxScrPositionPlayer>());
        }
    }

    /**
     * 
     * @param id
     * @param city
     * @param name
     * @param homeTF
     */
    public void setTeamInfo(String id, String city, String name, boolean homeTF) {
        setTeamID(id);
        setCity(city);
        setName(name);
        setHomeTF(homeTF);
    }

    /**
     * 
     * @param id
     */
    public void setTeamID(String id) {
        teamID = id;
    }

    /**
     * 
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @param home
     */
    public void setHomeTF(boolean home) {
        homeTF = home;
    }

    /* Double and triple plays block */
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
    /* End double and triple play block */

    /* Left on base functions */
    /**
     * 
     * @param lob
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
    /* END left on base functions */

    /**
     * Check team's pitchers to award win/loss/save. If a pitcher's
     * ID matches one of the three arguments, award him with that 
     * statistic.
     * 
     * @param wpID winning pitcher's ID.
     * @param lpID losing pitcher's ID.
     * @param saveID save ID.
     */
    public void setPitDecisions(String wpID, String lpID, String saveID) {
        for (BxScrPitcher p : pitchers) {
            if (p.getPlayerID().equals(wpID)) {
                p.setDecision('W');
            }
            if (p.getPlayerID().equals(lpID)) {
                p.setDecision('L');
            }
            if (p.getPlayerID().equals(saveID)) {
                p.setDecision('S');
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

    /**
     * 
     * @return
     */
    public String getTeamID() {
        return teamID;
    }

    /**
     * 
     * @return
     */
    public String getCity() {
        return city;
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @return
     */
    public boolean getHomeTF() {
        return homeTF;
    }

    /**
     * 
     * @return
     */
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
     * 
     * @return
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
     * @param stat_code
     * @param position
     */
    public void award_fielding(String stat_code, int position) {

        stat_code = stat_code.toLowerCase();

        if (stat_code.equals("e")) {
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
        } else if (stat_code.equals("pb")) {
            for (LinkedList<BxScrPositionPlayer> ar : lineup) {
                if (ar.getLast().getCurrentPosition() == BaseballPlayer.CATCHER) {
                    ar.getLast().incrementStats(BaseballPlayer.KEY_PB,1);
                }
            }
        }
    }

    /**
     * 
     * @param start
     * @param id
     * @param first
     * @param last
     * @param spot
     * @param pos
     */
    public void addPlayer(boolean start, String id, String first, String last,
            String spot, String pos) {
        int int_spot = Integer.parseInt(spot);
        int int_pos = Integer.parseInt(pos);

        /* 
         * If player is already in lineup, then he is changing positions.
         * If he is not in lineup, he is entering the game and must be added.
         */
        if (containsBxScrPositionPlayer(id)) {
            lineup.get(int_spot-1).getLast().changePosition(int_pos);
        } else {
            if (int_spot > 0) {
                
                /* 
                 * Account for the rare instance that a DH-rule pitcher moves to 
                 * a position after entering the game earlier as a pitcher. 
                 */
                if (containsPitcher(id)) {
                    BxScrPositionPlayer newB = 
                        new BxScrPositionPlayer(id,first,last,int_spot,BxScrPositionPlayer.PITCHER);
                    newB.changePosition(int_pos);
                    lineup.get(int_spot-1).add(newB);
                } else {
                    BxScrPositionPlayer newB = new BxScrPositionPlayer(id,first,last,int_spot,int_pos);
                    lineup.get(int_spot-1).add(newB);
                }
            }
        }
        if (int_pos == BxScrPositionPlayer.PITCHER) {

            /* New pitcher entering the game -- add to game staff. */
            if (!containsPitcher(id)) {
                // if (!pitchers.isEmpty()) {
                //     this.getCurrentPitcher().setInningRemoved(Play.getInning());
                // }
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
     * 
     * @param id
     * @return
     */
    private boolean containsBxScrPositionPlayer(String id) {
        for (LinkedList<BxScrPositionPlayer> list : lineup) {
            for (BxScrPositionPlayer p : list) {
                if (p.getPlayerID().equals(id))
                    return true;
            }
        }
        return false;
    }

    /**
     * 
     * @return
     */
    public ArrayList<Integer> getLinescore() {
        return linescore;
    }

    /**
     * 
     * @param runs
     */
    public void linescoreAdd(int runs) {
        linescore.add(runs);
    }

    /**
     * 
     * @return
     */
    public int getTotalRuns() {
        int t = 0;
        for (int r : linescore) {
            t+=r;
        }
        return t;
    }

    /**
     * 
     * @return
     */
    public String linescoreToString() {
        String s = "";

        // for (int r : linescore)
        //     s = s + r + " ";
        for (int i = 0; i < linescore.size(); i++) {
            if (i > 0 && i%3 == 0)
                s += " ";
            s += linescore.get(i);
        }
        return s.trim();
    }

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @return
     */
    public ArrayList<LinkedList<BxScrPositionPlayer>> getLineup() {
        return lineup;
    }

    /**
     * 
     * @return
     */
    public LinkedList<BxScrPitcher> getPitchingStaff() {
        return pitchers;
    }
}