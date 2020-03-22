package com.github.alexthesuperb.jopenboxscore;

import java.util.ArrayList;
import java.util.LinkedList;

public class Team {
    private ArrayList<LinkedList<PositionPlayer>> lineup = new ArrayList<>(9);
    private LinkedList<Pitcher> pitchers = new LinkedList<>();
    private ArrayList<Integer> linescore = new ArrayList<>(30);
    private String teamID, city, name;
    private int left_on_base, double_plays, triple_plays;
    private boolean homeTF;

    public Team(String id, String city, String name, boolean home){
        teamID = id;
        this.city = city;
        this.name = name;
        this.homeTF = home;
        for(int i = 0; i < 9; i++){
            lineup.add(new LinkedList<PositionPlayer>());
        }
    }

    public Team(){
        teamID = "";
        city = "";
        name = "";
        homeTF = false;
        for(int i = 0; i < 9; i++){
            lineup.add(new LinkedList<PositionPlayer>());
        }
    }
    public void setTeamInfo(String id, String city, String name, boolean homeTF){
        setTeamID(id);
        setCity(city);
        setName(name);
        setHomeTF(homeTF);
    }
    public void setTeamID(String id){
        teamID = id;
    }
    public void setCity(String city){
        this.city = city;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setHomeTF(boolean home){
        homeTF = home;
    }

    /* Double and triple plays block */
    public void add_double_triple_plays(boolean isDP, int n){
        if(isDP)
            double_plays += n;
        else
            triple_plays += n;
    }
    public int get_double_triple_plays(boolean isDP){
        if(isDP)
            return double_plays;
        else
            return triple_plays;
    }
    /* End double and triple play block */

    /* Left on base functions */
    public void add_lob(int lob){
        left_on_base += lob;
    }
    public void add_lob(int pa, int runs, int outs){
        add_lob(pa-runs-outs);
    }
    public int get_lob(){
        return left_on_base;
    }
    /* END left on base functions */

    public void setPitDecisions(String wpID, String lpID, String saveID){
        for(Pitcher p : pitchers){
            if(p.getID().equals(wpID))
                p.award_decision('W');
            if(p.getID().equals(lpID))
                p.award_decision('L');
            if(p.getID().equals(saveID))
                p.award_decision('S');
        }
    }
    //Set earned runs. If player is found and charged with those earned runs,
    //return true; if player is not found, return false.
    public boolean setEarnedRuns(String pitcherID, int er){
        for(Pitcher p : pitchers){
            if(p.getID().equals(pitcherID)){
                p.add_earned_runs(er);
                return true;
            }
        }
        return false;
    }
    public String getTeamID(){
        return teamID;
    }
    public String getCity(){
        return city;
    }
    public String getName(){
        return name;
    }
    public boolean getHomeTF(){
        return homeTF;
    }
    public int[] getLineupStats(){
        int[] stats = {0,0,0,0};
        for(LinkedList<PositionPlayer> ar : lineup){
            for(PositionPlayer p : ar){
                int[] pStats = p.getBattingStats();
                for(int i = 0; i < 4; i++)
                    stats[i] += pStats[i];
            }
        }
        return stats;
    }
    public int[] getPitchingStats(){
        int[] stats = {0,0,0,0,0,0};
        for(Pitcher p : pitchers){
            int[] pStats = p.getPitchingStats();
            for(int i = 0; i < 6; i++)
                stats[i] += pStats[i];
        }
        return stats;
    }

    //Award errors and passed balls.
    public void award_fielding(String stat_code, int position){
        stat_code = stat_code.toLowerCase();
        if(stat_code.equals("e")){
            //To account for both dh and non-dh games, errors committed
            //by pitchers are stored in the Pitcher objects, not in their
            //PositionPlayer entities 
            if(position == 1){ //pitcher
                getCurrentPitcher().add_stat("e",1);
            } else {
                for(LinkedList<PositionPlayer> ar : lineup){
                    if(ar.getLast().getCurrentPosition() == position){
                        ar.getLast().add_stat("e",1);
                        return;
                    }
                }
            }
        } else if(stat_code.equals("pb")){
            for(LinkedList<PositionPlayer> ar : lineup){
                if(ar.getLast().getCurrentPosition() == 2){
                    ar.getLast().add_stat("pb",1);
                }
            }
        }
    }
    public void addPlayer(boolean start, String id, String first, String last,
        String spot, String pos)
    {
        int int_spot = Integer.parseInt(spot);
        int int_pos = Integer.parseInt(pos);

        //Player already in lineup -- changing positions.
        if(containsPositionPlayer(id)){
            lineup.get(int_spot-1).getLast().changePosition(int_pos);
        } 
        //Not in lineup
        else {
            if(int_spot > 0){
                /* Account for the rare instance that a DH-rule pitcher moves to a position
                after entering the game earlier as a pitcher */
                if(containsPitcher(id)){
                    PositionPlayer newB = 
                        new PositionPlayer(id,first,last,int_spot,PositionPlayer.PITCHER);
                    newB.changePosition(int_pos);
                    lineup.get(int_spot-1).add(newB);
                } else {
                    PositionPlayer newB = new PositionPlayer(id,first,last,int_spot,int_pos);
                    lineup.get(int_spot-1).add(newB);
                }
            }
        }
        if(int_pos == PositionPlayer.PITCHER){
            //New pitcher entering the game -- add to game staff.
            if(!containsPitcher(id)){
                if(!pitchers.isEmpty())
                    this.getCurrentPitcher().set_inning_removed(Play.getInning());
                Pitcher newP = new Pitcher(id, first, last);
                pitchers.add(newP);
            }
        }
    }
    public boolean containsPitcher(String id){
        for(Pitcher p : pitchers){
            if(p.getID().equals(id))
                return true;
        }
        return false;
    }
    private boolean containsPositionPlayer(String id){
        for(LinkedList<PositionPlayer> list : lineup){
            for(PositionPlayer p : list){
                if(p.getID().equals(id))
                    return true;
            }
        }
        return false;
    }
    public ArrayList<Integer> getLinescore(){
        return linescore;
    }
    public void linescoreAdd(int runs){
        linescore.add(runs);
    }
    public int getTotalRuns(){
        int t = 0;
        for(int r : linescore){
            t+=r;
        }
        return t;
    }
    public String linescoreToString(){
        String s = "";
        // for(int r : linescore)
        //     s = s + r + " ";
        for(int i = 0; i < linescore.size(); i++){
            if(i > 0 && i%3 == 0)
                s += " ";
            s += linescore.get(i);
        }
        return s.trim();
    }
    public Pitcher getCurrentPitcher(){
        return pitchers.getLast();
    }
    public Pitcher getPitcher(String id){
        for(Pitcher p : pitchers){
            if(p.getID().equals(id))
                return p;
        }
        return null; 
    }
    public PositionPlayer getLineupSpot(int spot){
        return lineup.get(spot).getLast();
    }
    public PositionPlayer getPositionPlayer(String id){
        for(LinkedList<PositionPlayer> list : lineup){
            for(PositionPlayer p : list){
                if(p.getID().equals(id))
                    return p;
            }
        }
        return null;
    }
    public ArrayList<LinkedList<PositionPlayer>> getLineup(){
        return lineup;
    }
    public LinkedList<Pitcher> getPitchingStaff(){
        return pitchers;
    }
}