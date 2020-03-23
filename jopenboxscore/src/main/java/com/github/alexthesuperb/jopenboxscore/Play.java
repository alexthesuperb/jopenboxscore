/*
 * C-style comment with author/copyright information...
 */

package com.github.alexthesuperb.jopenboxscore;

/**
 * 
 */
public class Play{

    private static int inng=0, inngRuns=0, outs=0, inngPA, visSpot=0, homeSpot=0;
    private static boolean homeBatting;
    private static int[] baserunnerSpots = {-1,-1,-1};

    /**
     * For cases of a team batting out of order.
     * @param team
     * @param newSpot
     */
    public static void adjustBattingSpot(int team, int newSpot){
        if(team==0){ //visitor
            visSpot = newSpot-1;
        } else { //home
            homeSpot = newSpot-1;
        }
    }

    /**  */
    public static int getInning(){
        return inng;
    }
    
    /**  */
    public static void reset(){
        inng = 0; outs = 0; visSpot = 0; homeSpot = 0;
        inngRuns = 0; inngPA = 0;
        homeBatting = false;
        for(int i = 0; i < 3; i++)
            baserunnerSpots[i] = 0;    
    }

    /**
     * 
     * @param visitor
     * @param home
     */
    private static void newInning(Team visitor, Team home){
        //If third out was previously made, new inning has begun.
        if(outs == 3){
            if(homeBatting){
                home.linescoreAdd(inngRuns);
                home.addLOB(inngPA-inngRuns-outs);
            }
            else{
                visitor.linescoreAdd(inngRuns);
                visitor.addLOB(inngPA-inngRuns-outs);
            }
            homeBatting = !homeBatting;
            outs = 0;
            inngRuns = 0;
            inngPA = 0;
            if(!homeBatting)
                inng++;
            for(int i = 0; i < 3; i++) {
                baserunnerSpots[i] = -1;
            }
            visitor.getCurrentPitcher().startNewInning();
        }
    }

    /**
     * 
     * @param visitor
     * @param home
     */
    public static void endGame(Team visitor, Team home){
        if(homeBatting){
            home.linescoreAdd(inngRuns);
            home.addLOB(inngPA,inngRuns,outs);
            visitor.getCurrentPitcher().setInningRemoved(inng);
        }
        else{
            visitor.linescoreAdd(inngRuns);
            visitor.addLOB(inngPA,inngRuns,outs);
            home.getCurrentPitcher().setInningRemoved(inng);
        }
    }

    /**
     * 
     * @param visitor
     * @param home
     * @param event
     */
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

    /**
     * 
     * @param battingTeam
     * @param pitchingTeam
     * @param plateEvent
     * @param bsrEvent
     */
    private static void readPlateEvent(Team battingTeam, Team pitchingTeam, 
        String plateEvent, String bsrEvent) {
        int spot = (homeBatting) ? homeSpot : visSpot;
        boolean involvesBatter = true;
        int bAdvance = 0; //Bases taken by runner
        BxScrPitcher pitcher = pitchingTeam.getCurrentPitcher();
        BxScrPositionPlayer batter = battingTeam.getLineupSpot(spot); 
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
                pitcher.incrementStats(BaseballPlayer.KEY_BK,1);
                involvesBatter = false;
            } else if(s.startsWith("DI")){
                involvesBatter = false;
            } else if(s.startsWith("WP")){
                pitcher.incrementStats(BaseballPlayer.KEY_WP,1);
                involvesBatter = false;
            } else if(s.startsWith("PB")){
                pitchingTeam.award_fielding(BaseballPlayer.KEY_PB, 1);
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
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
                involvesBatter = false;
            } else if(s.startsWith("PO") || s.startsWith("CS")) {
                //Pick off / caught stealing
                if(!s.contains("E")){ //Error negates out.
                    if(s.charAt(2) == 'H'){
                        battingTeam.getLineupSpot(baserunnerSpots[2]).incrementStats(BaseballPlayer.KEY_CS);
                        baserunnerSpots[2] = -1;
                    } else {
                        int strtBase = Integer.parseInt(String.valueOf(s.charAt(2)))-1;
                        battingTeam.getLineupSpot(strtBase).incrementStats(BaseballPlayer.KEY_CS);
                        baserunnerSpots[strtBase] = -1;
                    }
                    outs++;
                    //NOTE: THIS MAY RESULT IN SOME DOUBLE-COUNTING. LOOK AT SOURCE.
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
                involvesBatter = false;
            } else if (s.startsWith("SB")){ //stolen base
                if(s.charAt(2) == 'H'){ //Stole home -- increment runs.
                    BxScrPositionPlayer r = battingTeam.getLineupSpot(baserunnerSpots[2]);
                    inngRuns++;
                    r.incrementStats(BaseballPlayer.KEY_R);
                    r.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
                    r.incrementStats(BaseballPlayer.KEY_SB,1);
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
                        battingTeam.getLineupSpot(startBase).incrementStats(BaseballPlayer.KEY_SB);
                        baserunnerSpots[endBase] = baserunnerSpots[startBase];
                        baserunnerSpots[startBase] = -1;
                    }
                }
                involvesBatter = false;
            }
            //Next, check for events that DO involve the batter (base hits, walks, etc)
            else if(s.contains("BOOT")){ //batting out of order
                bAdvance = 0;
                // pitcher.add_outs(1);
                pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                outs++;
            }
            else if(s.startsWith("HP")){ //hit by pitch
                pitcher.addBattersHBP(batter);
                // pitcher.add_batters_hit(batter.getPlayerID());
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if(s.startsWith("S")){ //single
                // batter.add_at_bats(1);
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if(s.startsWith("D")){ //double
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                batter.incrementStats(BaseballPlayer.KEY_2B);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 2;
            } else if(s.startsWith("T")){ //triple
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                batter.incrementStats(BaseballPlayer.KEY_3B);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 3;
            } else if(s.startsWith("HR") || s.startsWith("H")){ //homerun
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                batter.incrementStats(BaseballPlayer.KEY_HR);
                batter.incrementStats(BaseballPlayer.KEY_RBI);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 4;
            } else if(s.startsWith("W") || s.startsWith("IW")){ //walk
                pitcher.incrementStats(BaseballPlayer.KEY_BB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
                if(s.contains("+")){
                    readPlateEvent(battingTeam,pitchingTeam,s.split("[+]")[1],bsrEvent);
                    bsrEvent = "";
                }
            } else if(s.startsWith("K")){ //strikeout
                batter.incrementStats(BaseballPlayer.KEY_AB);
                pitcher.incrementStats(BaseballPlayer.KEY_SO);
                if(s.contains("+")){
                    // System.out.println("**" + s.split("[+]")[1]); //debug
                    readPlateEvent(battingTeam,pitchingTeam,s.split("[+]")[1],bsrEvent);
                    bsrEvent = "";
                    if(!bsrEvent.contains("B-")){
                        outs++;
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                    }
                } else {
                    outs++;
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
            } else if(s.startsWith("FC")){
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if(s.contains("FO")){
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
                for(int i = 0; i < s.length()-2; i++){
                    if(s.charAt(i) == '(' && s.charAt(i+2) == ')'){
                        if(s.charAt(i+1) != 'B'){
                            int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                            baserunnerSpots[oldBase] = -1;
                        }
                        outs++;
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                    }
                }
            } else if(s.contains("SH")){
                batter.incrementStats(BaseballPlayer.KEY_SH);
                if(!bsrEvent.contains("B-")){ //Temporary fix
                    outs++;
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
            } else if(s.contains("SF")){
                batter.incrementStats(BaseballPlayer.KEY_SF);
                outs++;
                pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
            } else if(Character.isDigit(s.charAt(0))){ //Fielded out
                batter.incrementStats(BaseballPlayer.KEY_AB);
                if(s.contains("GDP") || s.contains("GTP") || s.contains("/G/DP/")){
                    for(int i = 0; i < s.length()-2; i++){
                        if(s.charAt(i) == '(' && s.charAt(i+2) == ')'){
                            if(s.charAt(i+1) != 'B'){
                                int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                                baserunnerSpots[oldBase] = -1;
                            }
                            outs++;
                            pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                        }
                    }
                    if(!s.contains("(B)")){
                        outs++; //batter
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                    }
                } else if(s.contains("LDP") || s.contains("LTP")){
                    int outs_on_play = 0;
                    for(int i = 0; i < s.length()-2; i++){
                        if(s.charAt(i) == '(' && s.charAt(i+2) == ')'){
                            int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                            baserunnerSpots[oldBase] = -1;
                            // outs++;
                            outs_on_play++;
                            pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
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
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
            } else if(s.startsWith("C/E")){ //catcher's interference
                bAdvance = 1;
            } else if(s.startsWith("E")){
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            }
        }
        
        readBaserunning(battingTeam, pitchingTeam, batter, pitcher, bsrEvent, bAdvance);
        if(involvesBatter){
            pitcher.incrementInningBF();
            incrementSpot();
            inngPA++; //For end-of-inning LOB calculation
        }
    }

    /**
     * 
     * @param battingTeam
     * @param pitchingTeam
     * @param batter
     * @param pitcher
     * @param bsrEvent
     * @param bAdvance
     */
    private static void readBaserunning(Team battingTeam, Team pitchingTeam, 
        BxScrPositionPlayer batter, BxScrPitcher pitcher, String bsrEvent, int bAdvance)
    {
        if(bsrEvent.equals("")){
            if(bAdvance == 4){
                batter.incrementStats(BaseballPlayer.KEY_R);
                batter.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
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
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                        bAdvance = 0;
                    }
                }
            } else { //All other runners (1-, 2-, 3-)
                int startBase = Integer.parseInt(String.valueOf(s.charAt(0)))-1;
                BxScrPositionPlayer r;
                try{ //TEMPORARY FIX.
                    r  = battingTeam.getLineupSpot(baserunnerSpots[startBase]);
                } catch (IndexOutOfBoundsException e){
                    r = battingTeam.getLineupSpot(0);
                }
                if(s.contains("-")){ //Runner movement
                    if(s.charAt(2) == 'H'){ //Runner scores
                        baserunnerSpots[startBase] = -1;
                        r.incrementStats(BaseballPlayer.KEY_R);
                        if(!s.contains("(NR)"))
                            batter.incrementStats(BaseballPlayer.KEY_RBI);
                        try{
                            r.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
                        } catch (NullPointerException e){
                            pitcher.incrementStats(BaseballPlayer.KEY_R);
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
                            r.incrementStats(BaseballPlayer.KEY_R);
                            r.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
                            inngRuns++;
                        } else { //Runner moves to new base on error
                            int endBase = Integer.parseInt(String.valueOf(s.charAt(2)))-1;
                            baserunnerSpots[endBase] = baserunnerSpots[startBase];
                            baserunnerSpots[startBase] = -1;
                        }
                    } else { //Runner tagged out
                        baserunnerSpots[startBase] = -1;
                        outs++;
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                    }
                }
            }
        }

        if(bAdvance == 4){
            batter.incrementStats(BaseballPlayer.KEY_R);
            batter.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
            inngRuns++;
        } else if(bAdvance > 0 && bAdvance < 4){
            baserunnerSpots[bAdvance-1] = batter.getLineupSpot()-1;
        }
        
    }

    /** */
    private static void incrementSpot(){
        if(homeBatting)
            homeSpot = (homeSpot+1)%9;
        else 
            visSpot  = (visSpot+1)%9;
    }

    /**  */
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

    /** @return */
    public static int getOuts(){
        return outs;
    }

    /** @return */
    public static int getInng(){
        return inng; //index for play.Team.linescore 
    }
}