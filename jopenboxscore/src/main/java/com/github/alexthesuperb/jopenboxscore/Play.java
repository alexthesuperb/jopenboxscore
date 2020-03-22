
/* 
* File: game/Play.java
* Description: read and record Retrosheet.org events (lines beginning
* with 'play,' flag). 
*/

package com.github.alexthesuperb.jopenboxscore;

public class Play{
    private static int inng=0, inngRuns=0, outs=0, inngPA, visSpot=0, homeSpot=0;
    private static boolean homeBatting;
    private static int[] baserunnerSpots = {-1,-1,-1};

    /* For cases of a team batting out of order */
    public static void adjustBattingSpot(int team, int newSpot){
        if(team==0){ //visitor
            visSpot = newSpot-1;
        } else { //home
            homeSpot = newSpot-1;
        }
    }
    public static int getInning(){
        return inng;
    }
    public static void reset(){
        inng = 0; outs = 0; visSpot = 0; homeSpot = 0;
        inngRuns = 0; inngPA = 0;
        homeBatting = false;
        for(int i = 0; i < 3; i++)
            baserunnerSpots[i] = 0;    
    }
    private static void newInning(Team visitor, Team home){
        //If third out was previously made, new inning has begun.
        if(outs == 3){
            if(homeBatting){
                home.linescoreAdd(inngRuns);
                home.add_lob(inngPA-inngRuns-outs);
            }
            else{
                visitor.linescoreAdd(inngRuns);
                visitor.add_lob(inngPA-inngRuns-outs);
            }
            homeBatting = !homeBatting;
            outs = 0;
            inngRuns = 0;
            inngPA = 0;
            if(!homeBatting)
                inng++;
            for(int i = 0; i < 3; i++)
                baserunnerSpots[i] = -1;
            visitor.getCurrentPitcher().new_inning();
            visitor.getCurrentPitcher().new_inning();
        }
    }
    public static void endGame(Team visitor, Team home){
        if(homeBatting){
            home.linescoreAdd(inngRuns);
            home.add_lob(inngPA,inngRuns,outs);
            visitor.getCurrentPitcher().set_inning_removed(inng);
        }
        else{
            visitor.linescoreAdd(inngRuns);
            visitor.add_lob(inngPA,inngRuns,outs);
            home.getCurrentPitcher().set_inning_removed(inng);
        }
    }
    public static void readPlay(Team visitor, Team home, String event){
        // System.out.print(String.format("%-20s", event + ": ")); //debug
        // System.out.println(); //debug
        newInning(visitor,home);
        try{
            if(homeBatting)
                readPlateEvent(home, visitor, event.split("\\.")[0], event.split("\\.")[1]);
            else
                readPlateEvent(visitor, home, event.split("\\.")[0], event.split("\\.")[1]);
        } catch (ArrayIndexOutOfBoundsException e){
            if(homeBatting)
                readPlateEvent(home, visitor, event, "");
            else
                readPlateEvent(visitor, home, event, "");
        }
        // printBases(); //debug
        // newInning(visitor,home);
    }
    private static void readPlateEvent(Team battingTeam, Team pitchingTeam, 
        String plateEvent, String bsrEvent)
    {
        int spot = (homeBatting) ? homeSpot : visSpot;
        boolean involvesBatter = true;
        int bAdvance = 0; //Bases taken by runner
        Pitcher pitcher = pitchingTeam.getCurrentPitcher();
        PositionPlayer batter = battingTeam.getLineupSpot(spot); 
        batter.setPitcherCharged(pitcher); //temp

        /* Award errors on both plate and baserunning events */
        for(int i = 0; i < plateEvent.length()-1; i++){
            if(plateEvent.charAt(i) == 'E'){
                int pos = Integer.parseInt(String.valueOf(plateEvent.charAt(i+1)));
                pitchingTeam.award_fielding("E", pos);
            }
        }
        for(int i = 0; i < bsrEvent.length()-1; i++){
            if(bsrEvent.charAt(i) == 'E'){
                int pos = Integer.parseInt(String.valueOf(bsrEvent.charAt(i+1)));
                pitchingTeam.award_fielding("E", pos);
            }
        }
        /* END ERROR AWARDING BLOCK */

        /* Award double and triple play credit */
        if(plateEvent.contains("TP")){
            pitchingTeam.add_double_triple_plays(false, 1);
        }
        if(plateEvent.contains("DP")){
            pitchingTeam.add_double_triple_plays(true, 1);
        }
        /* End DP/TP credit block */
        
        if(plateEvent.startsWith("NP"))
            return;
        
        for(String s : plateEvent.split(";")){
            //Begin by checking for baserunning events that do not involve batter
            if(s.startsWith("BK")){
                pitcher.add_stat("bk",1);
                involvesBatter = false;
            } else if(s.startsWith("DI")){
                involvesBatter = false;
            } else if(s.startsWith("WP")){
                pitcher.add_stat("wp",1);
                involvesBatter = false;
            } else if(s.startsWith("PB")){
                pitchingTeam.award_fielding("pb", 1);
                involvesBatter = false;
            } else if(s.startsWith("OA")){
                involvesBatter = false;
            } else if(s.startsWith("POCS")){ //pick off caught stealing
                if(!s.contains("E")){
                    if(s.charAt(4) == 'H'){
                        baserunnerSpots[2] = -1;
                    } else {
                        int strtBase = Integer.parseInt(String.valueOf(s.charAt(4)))-2;
                        baserunnerSpots[strtBase] = -1;
                    }
                    outs++;
                    pitcher.add_outs(1);
                }
                involvesBatter = false;
            } else if(s.startsWith("PO") || s.startsWith("CS")) {
                //Pick off / caught stealing
                if(!s.contains("E")){ //Error negates out.
                    if(s.charAt(2) == 'H'){
                        battingTeam.getLineupSpot(baserunnerSpots[2]).add_stat("cs",1);
                        baserunnerSpots[2] = -1;
                    } else {
                        int strtBase = Integer.parseInt(String.valueOf(s.charAt(2)))-1;
                        battingTeam.getLineupSpot(strtBase).add_stat("cs",1);
                        // System.out.println("strtBase = " + strtBase); //debug
                        baserunnerSpots[strtBase] = -1;
                    }
                    outs++;
                    pitcher.add_outs(1);
                }
                involvesBatter = false;
            } else if (s.startsWith("SB")){ //stolen base
                if(s.charAt(2) == 'H'){ //Stole home -- increment runs.
                    PositionPlayer r = battingTeam.getLineupSpot(baserunnerSpots[2]);
                    inngRuns++;
                    r.add_runs(1);
                    r.getPitcherCharged().add_runs(1);
                    r.add_stat("sb",1);
                    baserunnerSpots[2] = -1;
                } else{
                    if(bsrEvent.equals("")){ //Baserunning event may override stolen base (error)
                        int endBase = 0, startBase = 0;
                        if(s.charAt(2)=='2'){
                            endBase = 1;
                            startBase = 0;
                        } else if(s.charAt(2) == '3'){
                            endBase = 2;
                            startBase = 1;
                        }
                        battingTeam.getLineupSpot(startBase).add_stat("sb",1);
                        baserunnerSpots[endBase] = baserunnerSpots[startBase];
                        baserunnerSpots[startBase] = -1;
                    }
                }
                involvesBatter = false;
            }
            //Next, check for events that DO involve the batter (base hits, walks, etc)
            else if(s.contains("BOOT")){ //batting out of order
                bAdvance = 0;
                pitcher.add_outs(1);
                outs++;
            }
            else if(s.startsWith("HP")){ //hit by pitch
                pitcher.add_batters_hit(batter.getID());
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if(s.startsWith("S")){ //single
                batter.add_at_bats(1);
                batter.add_hits(1);
                pitcher.add_hits(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if(s.startsWith("D")){ //double
                batter.add_at_bats(1);
                batter.add_hits(1);
                batter.add_stat("d",1);
                pitcher.add_hits(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 2;
            } else if(s.startsWith("T")){ //triple
                batter.add_at_bats(1);
                batter.add_hits(1);
                batter.add_stat("t",1);
                pitcher.add_hits(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 3;
            } else if(s.startsWith("HR") || s.startsWith("H")){ //homerun
                batter.add_at_bats(1);
                batter.add_hits(1);
                batter.add_stat("hr",1);
                batter.add_rbi(1);
                pitcher.add_hits(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 4;
            } else if(s.startsWith("W") || s.startsWith("IW")){ //walk
                pitcher.add_walks(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
                if(s.contains("+")){
                    readPlateEvent(battingTeam,pitchingTeam,s.split("[+]")[1],bsrEvent);
                    bsrEvent = "";
                }
            } else if(s.startsWith("K")){ //strikeout
                batter.add_at_bats(1);
                pitcher.add_strikeouts(1);
                if(s.contains("+")){
                    // System.out.println("**" + s.split("[+]")[1]); //debug
                    readPlateEvent(battingTeam,pitchingTeam,s.split("[+]")[1],bsrEvent);
                    bsrEvent = "";
                    if(!bsrEvent.contains("B-")){
                        outs++;
                        pitcher.add_outs(1);
                    }
                } else {
                    outs++;
                    pitcher.add_outs(1);
                }
            } else if(s.startsWith("FC")){
                batter.add_at_bats(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if(s.contains("FO")){
                batter.add_at_bats(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
                for(int i = 0; i < s.length()-2; i++){
                    if(s.charAt(i) == '(' && s.charAt(i+2) == ')'){
                        if(s.charAt(i+1) != 'B'){
                            int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                            baserunnerSpots[oldBase] = -1;
                        }
                        outs++;
                        pitcher.add_outs(1);
                    }
                }
            } else if(s.contains("SH")){
                batter.add_stat("SH",1);
                if(!bsrEvent.contains("B-")){ //Temporary fix
                    outs++;
                    pitcher.add_outs(1);
                }
            } else if(s.contains("SF")){
                batter.add_stat("SF",1);
                outs++;
                pitcher.add_outs(1);
            } else if(Character.isDigit(s.charAt(0))){ //Fielded out
                batter.add_at_bats(1);
                if(s.contains("GDP") || s.contains("GTP") || s.contains("/G/DP/")){
                    for(int i = 0; i < s.length()-2; i++){
                        if(s.charAt(i) == '(' && s.charAt(i+2) == ')'){
                            if(s.charAt(i+1) != 'B'){
                                int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                                baserunnerSpots[oldBase] = -1;
                            }
                            outs++;
                            pitcher.add_outs(1);
                        }
                    }
                    if(!s.contains("(B)")){
                        outs++; //batter
                        pitcher.add_outs(1);
                    }
                } else if(s.contains("LDP") || s.contains("LTP")){
                    int outs_on_play = 0;
                    for(int i = 0; i < s.length()-2; i++){
                        if(s.charAt(i) == '(' && s.charAt(i+2) == ')'){
                            int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                            baserunnerSpots[oldBase] = -1;
                            // outs++;
                            outs_on_play++;
                            pitcher.add_outs(1);
                        }
                    }
                    outs += outs_on_play;
                    if(outs_on_play == 2)
                        pitchingTeam.add_double_triple_plays(true, 1); //temp
                    else
                        pitchingTeam.add_double_triple_plays(false, 1); //temp
                } else if(s.contains("E")){ //Error made by some subsequent fielder
                    batter.setPitcherCharged(pitcher);
                    bAdvance = 1;
                } else {
                    outs++;
                    pitcher.add_outs(1);
                }
            } else if(s.startsWith("C/E")){ //catcher's interference
                bAdvance = 1;
            } else if(s.startsWith("E")){
                batter.add_at_bats(1);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            }
        }
        
        readBaserunning(battingTeam, pitchingTeam, batter, pitcher, bsrEvent, bAdvance);
        if(involvesBatter){
            pitcher.increment_batters_faced();
            incrementSpot();
            inngPA++; //For end-of-inning LOB calculation
        }
    }
    private static void readBaserunning(Team battingTeam, Team pitchingTeam, 
        PositionPlayer batter, Pitcher pitcher, String bsrEvent, int bAdvance)
    {
        if(bsrEvent.equals("")){
            if(bAdvance == 4){
                batter.add_runs(1);
                batter.getPitcherCharged().add_runs(1);
                inngRuns++;
            } else if(bAdvance > 0 && bAdvance < 4){
                baserunnerSpots[bAdvance-1] = batter.getLineupSpot()-1;
            }
            return;
        }

        for(String s : bsrEvent.split(";")){
            if(s.startsWith("B")){
                if(s.startsWith("B-")){
                    if(s.charAt(2) == 'H')
                        bAdvance = 4;
                    else
                        bAdvance = Integer.parseInt(String.valueOf(s.charAt(2)));
                } else if(s.startsWith("BX")){
                    if(s.contains("E")){ //Error negates the out
                        if(s.charAt(2) == 'H')
                            bAdvance = 4;
                        else
                            bAdvance = Integer.parseInt(String.valueOf(s.charAt(2)));
                    } else {
                        outs++;
                        pitcher.add_outs(1);
                        bAdvance = 0;
                    }
                }
            } else { //All other runners (1-, 2-, 3-)
                int startBase = Integer.parseInt(String.valueOf(s.charAt(0)))-1;
                PositionPlayer r;
                try{ //TEMPORARY FIX.
                    r  = battingTeam.getLineupSpot(baserunnerSpots[startBase]);
                } catch (IndexOutOfBoundsException e){
                    r = battingTeam.getLineupSpot(0);
                }
                if(s.contains("-")){ //Runner movement
                    if(s.charAt(2) == 'H'){ //Runner scores
                        baserunnerSpots[startBase] = -1;
                        r.add_runs(1);
                        if(!s.contains("(NR)"))
                            batter.add_rbi(1);
                        try{
                            r.getPitcherCharged().add_runs(1);
                        } catch (NullPointerException e){
                            pitcher.add_runs(1);
                        }
                        // r.getPitcherCharged().add_runs(1);
                        inngRuns++;
                    } else {  //Runner moves to new base
                        int endBase = Integer.parseInt(String.valueOf(s.charAt(2)))-1;
                        if(startBase != endBase){
                            baserunnerSpots[endBase] = baserunnerSpots[startBase];
                            baserunnerSpots[startBase] = -1;
                        }
                    }
                } else if(s.contains("X")){ //Runner tagged out
                    if(s.contains("E")){ //Error negates out
                        if(s.charAt(2) == 'H'){ //Runner scores on error
                            baserunnerSpots[startBase] = -1;
                            r.add_runs(1);
                            r.getPitcherCharged().add_runs(1);
                            inngRuns++;
                        } else { //Runner moves to new base on error
                            int endBase = Integer.parseInt(String.valueOf(s.charAt(2)))-1;
                            baserunnerSpots[endBase] = baserunnerSpots[startBase];
                            baserunnerSpots[startBase] = -1;
                        }
                    } else { //Runner tagged out
                        baserunnerSpots[startBase] = -1;
                        outs++;
                        pitcher.add_outs(1);
                    }
                }
            }
        }

        if(bAdvance == 4){
            batter.add_runs(1);
            batter.getPitcherCharged().add_runs(1);
            inngRuns++;
        } else if(bAdvance > 0 && bAdvance < 4){
            baserunnerSpots[bAdvance-1] = batter.getLineupSpot()-1;
        }
        
    }
    private static void incrementSpot(){
        if(homeBatting)
            homeSpot = (homeSpot+1)%9;
        else 
            visSpot  = (visSpot+1)%9;
    }
    public static void printBases(){
        //debugging
        for(int i : baserunnerSpots){
            if(i > -1){
                System.out.print(i + " ");
            } else {
                System.out.print("_ ");
            }
        }
        System.out.println();
    }
    public static int getOuts(){
        return outs;
    }
    public static int getInng(){
        return inng; //index for play.Team.linescore 
    }
}