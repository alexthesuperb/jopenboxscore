package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;

public class Pitcher extends Player{
    
    private char decision = '\0';
    private int outs_recorded, hits_allowed, runs_allowed, 
        earned_runs, walks_allowed, strikeouts;
    private int wild_pitches, balks, errors;
    private LinkedList<String> hits_by_pitch = new LinkedList<>();
    private int inning_outs = 0;
    private int inning_bf = 0;  //batters faced in current inning
    private int inning_removed; //Inning pitcher was removed from.
    
    public Pitcher(String id, String first, String last){
        super(id, first, last);
    }
    public int[] getPitchingStats(){
        int[] pitching_stats = {outs_recorded, hits_allowed, 
        runs_allowed, earned_runs, walks_allowed, strikeouts};
        return pitching_stats;
    }
    public char getDecision(){
        return decision;
    }
    public void award_decision(char dec){
        decision = dec;
    }

    /* Batters faced block */
    public int get_inning_removed(){
        return inning_removed;
    }
    public void set_inning_removed(int inng){
        //inng from game.Play -- increment by one for human-readable number
        inning_removed = inng + 1; 
    }
    public boolean removed_without_recorded_out_in_inning(){
        return (inning_outs == 0 && inning_bf > 0) ? true : false;
    }
    public int getInningBattersFaced(){
        return inning_bf;
    }
    public void new_inning(){
        inning_outs = 0;
        inning_bf = 0;
    }
    public void increment_batters_faced(){
        inning_bf++;
    }
    /* End batters faced block */

    /* HBP block */
    public void add_batters_hit(String id){
        hits_by_pitch.add(id);
    }
    public LinkedList<String> get_batters_hit(){
        return hits_by_pitch;
    }
    /* End HBP block */
    
    public void add_outs(int br){
        outs_recorded += br;
        inning_outs += br;
    }
    public void add_hits(int h){
        hits_allowed += h;
    }
    public void add_runs(int r){
        runs_allowed += r;
    }
    public void add_earned_runs(int er){
        earned_runs += er;
    }
    public void add_walks(int bb){
        walks_allowed += bb;
    }
    public void add_strikeouts(int k){
        strikeouts += k;
    }
    public void add_stat(String stat_code, int n){
        stat_code = stat_code.toLowerCase();
        if(stat_code.equals("wp")){
            wild_pitches += n;
        } else if(stat_code.equals("bk")){
            balks += n;
        } else if(stat_code.equals("e")){
            errors += n;
        }
    }
    public int get_stat(String stat_code){
        stat_code = stat_code.toLowerCase();
        if(stat_code.equals("wp")){
            return wild_pitches;
        } else if(stat_code.equals("bk")){
            return balks;
        } else if(stat_code.equals("e")){
            return errors;
        } else {
            return 0;
        }
    }
}