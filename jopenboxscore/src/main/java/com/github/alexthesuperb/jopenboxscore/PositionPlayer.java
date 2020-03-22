package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;

public class PositionPlayer extends Player{
    
    //CONSTANTS -- STANDARD POSITION CODES
    public static final int PITCHER        = 1;
    public static final int CATCHER        = 2;
    public static final int FIRST_BASEMAN  = 3;
    public static final int SECOND_BASEMAN = 4;
    public static final int THIRD_BASEMAN  = 5;
    public static final int SHORT_STOP     = 6;
    public static final int LEFT_FIELDER   = 7;
    public static final int CENTER_FIELDER = 8;
    public static final int RIGHT_FIELDER  = 9;

    private int lineup_spot;
    private int at_bats, runs, hits, batted_in;
    private int doubles, triples, home_runs;
    private int errors, passed_balls;
    private int stolen_bases, caught_stealings;
    private int sac_flies, sac_hits;
    private LinkedList<Integer> positions = new LinkedList<>();
    Pitcher pitcherCharged = null;
    
    public PositionPlayer(String id, String first, String last, 
        String spot, String pos)
    {
        super(id, first, last);
        lineup_spot = Integer.parseInt(spot);
        positions.add(Integer.valueOf(pos));
    }
    public PositionPlayer(String id, String first, String last,
        int spot, int pos)
    {
        super(id, first, last);
        lineup_spot = spot;
        positions.add(pos);
    }

    /* Pitcher charged with player getting on base */
    public void setPitcherCharged(Pitcher p){
        pitcherCharged = p;
    }
    public void clearPitcherCharged(){
        setPitcherCharged(null);
    }
    public Pitcher getPitcherCharged(){
        return pitcherCharged;
    }
    /* END pitcher charged block */

    public void changePosition(String newPos){
        positions.add(Integer.valueOf(newPos));
    }
    public void changePosition(int newPos){
        positions.add(newPos);
    }
    public int getCurrentPosition(){
        return positions.getLast();
    }
    public void add_at_bats(int ab){
        at_bats += ab;
    }
    public void add_hits(int h){
        hits += h;
    }
    public void add_runs(int r){
        runs += r;
    }
    public void add_rbi(int rbi){
        batted_in += rbi;
    }
    public void add_stat(String stat_code, int n){
        stat_code = stat_code.toLowerCase();
        if (stat_code.equals("ab"))
            at_bats += n;
        else if(stat_code.equals("r"))
                runs += n;
        else if(stat_code.equals("h"))
            hits += n;
        else if(stat_code.equals("rbi"))
            batted_in += n;
        else if(stat_code.equals("2b") || stat_code.equals("d"))
            doubles += n;
        else if(stat_code.equals("3b") || stat_code.equals("t"))
            triples += n;
        else if(stat_code.equals("hr"))
            home_runs += n;
        else if(stat_code.equals("e"))
            errors += n;
        else if(stat_code.equals("pb"))
            passed_balls += n;
        else if(stat_code.equals("sh"))
            sac_hits += n;
        else if(stat_code.equals("sf"))
            sac_flies += n;
        else if(stat_code.equals("sb"))
            stolen_bases += n;
        else if(stat_code.equals("cs"))
            caught_stealings += n;
    }
    public int get_stat(String stat_code){
        stat_code = stat_code.toLowerCase();
        if (stat_code.equals("ab"))
            return at_bats;
        else if(stat_code.equals("r"))
            return runs;
        else if(stat_code.equals("h"))
            return hits;
        else if(stat_code.equals("rbi"))
            return batted_in;
        else if(stat_code.equals("2b") || stat_code.equals("d"))
            return doubles;
        else if(stat_code.equals("3b") || stat_code.equals("t"))
            return triples;
        else if(stat_code.equals("hr"))
            return home_runs;
        else if(stat_code.equals("e"))
            return errors;
        else if(stat_code.equals("pb"))
            return passed_balls;
        else if(stat_code.equals("sh"))
            return sac_hits;
        else if(stat_code.equals("sf"))
            return sac_flies;
        else if(stat_code.equals("sb"))
            return stolen_bases;
        else if(stat_code.equals("cs"))
            return caught_stealings; 
        else
            return 0;
    }
    public int[] getBattingStats(){
        int[] batting_stats = {at_bats, runs, hits, batted_in};
        return batting_stats;
    }
    public int getLineupSpot(){
        return lineup_spot;
    }
    public String getPositionString(){
        String str = "";
        for(Integer i : positions){
            switch(i){
                case 1:
                    str += "p-";
                    break;
                case 2:
                    str += "c-";
                    break;
                case 3:
                    str += "1b-";
                    break;
                case 4:
                    str += "2b-";
                    break;
                case 5:
                    str += "3b-";
                    break;
                case 6:
                    str +="ss-";
                    break;
                case 7:
                    str += "lf-";
                    break;
                case 8:
                    str += "cf-";
                    break;
                case 9:
                    str += "rf-";
                    break;
                case 10:
                    str += "dh-";
                    break;
                case 11:
                    str += "ph-";
                    break;
                case 12:
                    str += "pr-";
            }
        }
        return (str.length() > 0) ? str.substring(0,str.length()-1) : str;
    }
}