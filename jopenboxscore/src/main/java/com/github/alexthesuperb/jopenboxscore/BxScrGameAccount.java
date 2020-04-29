/*
 * C-Style comment containing author/copyright info...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * <code>BxScrGame</code> implements the interface <code>GameAccount</code>. It
 * is the most class used to store Retrosheet play-by-play data; its purpose is
 * to track a game's state, using only simple statistics, to create
 * human-readable boxscores.
 */
public class BxScrGameAccount implements GameAccount, Comparable<BxScrGameAccount> {

    private String gameID;
    private String year;

    /** MM/DD/YYYY */
    private String usaDateString;

    /** YYYY/MM/DD */
    private String stdDateString;

    private char daynight;
    private int gmNumber;
    private int attendance;
    private int timeOfGame;
    private String wpID;
    private String lpID;
    private String saveID;

    private Team visitor;
    private Team home;
    
    private RandomAccessFile teamReader;
    private RandomAccessFile visRosReader;
    private RandomAccessFile homeRosReader;

    File rosDir;
    private String visRosFileName;
    private String homeRosFileName;
    
    private String currentLine;
    private int lineNum;
    private String eveFileName;
    
    private boolean homeBatting;
    private int inng;
    private int outs;
    private int visSpot;
    private int homeSpot;
    private int inngRuns;
    private int inngPA;
    private int[] baserunnerSpots;

    /**
     * Summarize the events of this game account in a concise, human-readable
     * boxscore format.
     * 
     * @param outWriter a <code>BufferedWriter</code>
     * @throws IOException if an I/O exception originating from the 
     * <code>BufferedWriter</code> occurs.
     */
    public void printBoxscore(BufferedWriter outWriter) throws IOException {
        NewspaperBoxscore boxscore = new NewspaperBoxscore(this, outWriter);
        boxscore.write();
    }

    @Override
    public int compareTo(BxScrGameAccount anotherGame) {
        int compare = stdDateString.compareTo(anotherGame.getStdDateString());
        if (compare == 0) {
            if (daynight == anotherGame.getDayNight()) {
                return 0;
            } else if ((daynight == 'N') && (anotherGame.getDayNight() == 'D')) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return compare;
        }
    }

    /**
     * Returns a <code>Team</code> object containing the statistics counted by this
     * game account. As a precaution, the caller should be sent as an argument so
     * that this method is only accessible to classes that implement 
     * <code>Boxscore</code>.
     * @param isHome <code>true</code> to receive home team, <code>false</code> to
     * receive visiting team.
     * @param obj an object that implements <code>Boxscore</code>.
     * @return a <code>Team</code> if <code>obj</code> implements <code>Boxscore</code>,
     * or <code>null</code> otherwise.
     */
    public Team getTeam(boolean isHome, Object obj) {

        if (obj instanceof Boxscore) {
            return (isHome) ?  home : visitor;
        } else {
            return null;
        }
    }

    /**
     * <p>Construct a new <code>BxScrGameAccount</code> object for game of ID
     * <code>gameID</code> in file <code>fileName</code>, occurring in year
     * <code>year</code>. Team information should be derived from file 
     * <code>teamReader</code>.</p>
     * 
     * <p>While technically slower to reopen a new instance of
     * <code>RandomAccessFile</code> for each instance of <code>BxScGameAccount</code>,
     * this choice was made to improve the class' modularity. Its typical caller,
     * <code>BxScrFileReader</code>, need only pass in the directory containing 
     * the required roster and TEAM file.</p>
     * 
     * @param gameID This game's ID.
     * @param year The year the game occured.
     * @param fileName The name of the file containing this game account.
     * @param rosDir The directory containing the TEAM and ROS files used for this game.
     * @throws FileNotFoundException if TEAM file can not be found in <code>rosDir</code>.
     * @throws IOException if TEAM file cannot be opened.
     */
    public BxScrGameAccount(String gameID, String year, String fileName, File rosDir)
            throws FileNotFoundException, IOException {
        
        /* Initialize game environment variables. */
        this.gameID = gameID;
        this.year = year;
        this.eveFileName = fileName;
        this.rosDir = rosDir;
        /* 
         * Initialize game-state variables to reflect bottom 
         * of 1st inning.
         */
        inng = 0;
        outs = 0;
        visSpot = 0;
        homeSpot = 0;
        inngRuns = 0;
        inngPA = 0;
        homeBatting = false;
        baserunnerSpots = new int[3];
        
        for (int i = 0; i < 3; i++) {
            baserunnerSpots[i] = -1;
        }

        /* Try to open TEAM file */
        boolean openedTeamFile = false;
        String teamFileName = "TEAM" + year;
        try {
             /* Find filereader */
            for (String s : rosDir.list()) {
                if (s.contains(teamFileName)) {
                    teamReader = new RandomAccessFile(
                        new File(rosDir.getPath(), teamFileName),
                        "r"
                    );
                    openedTeamFile = true;
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new IOException("File " + teamFileName + " in directory " + 
                rosDir.getPath() + " could not be opened.");
        }

        /* If team file is not found in the provided directory, throw exception. */
        if (!openedTeamFile) {
            throw new FileNotFoundException("Could not find file " + teamFileName + 
                " in directory " + rosDir.getPath());
        }
    }

    /** 
     * Put the finishing touches on an account of a game. For best results,
     * make sure to call this after all lines have been read.
     */
    public void finalize() {
        /*
         * Update linescore, LOB, and final game's pitcher with 
         * information from last inning played.
         */
        if (homeBatting) {
            home.linescoreAdd(inngRuns);
            home.addLOB(inngPA,inngRuns,outs);
            visitor.getCurrentPitcher().setInningRemoved(inng);
        }
        else{
            visitor.linescoreAdd(inngRuns);
            visitor.addLOB(inngPA,inngRuns,outs);
            home.getCurrentPitcher().setInningRemoved(inng);
        }

        /* Award pitching decisions */
        if (visitor.containsPitcher(wpID)) {
            /* 
             * If visitor contains winning pitcher, then home must
             * contain losing pitcher.
             */
            visitor.setPitDecision(BaseballPlayer.DECISION_WIN, wpID);
            home.setPitDecision(BaseballPlayer.DECISION_LOSS, lpID);
            visitor.setPitDecision(BaseballPlayer.DECISION_SAVE, saveID);
        } else {
            home.setPitDecision(BaseballPlayer.DECISION_WIN, wpID);
            visitor.setPitDecision(BaseballPlayer.DECISION_LOSS, lpID);
            home.setPitDecision(BaseballPlayer.DECISION_SAVE, saveID);
        }
    }

    /**
     * Add a Retrosheet play-by-play line to the game account.
     * 
     * @param pbpLine The next input line.
     * @throws FileNotFoundException if home or away team's roster file cannot be opened.
     * @throws IOException if expected data is not found in queried TEAM/roster file.
     * @throws IllegalArgumentException if <code>pbpLine</code> does not match its expected
     *         format.
     */
    public void addLine(String pbpLine, int lineNum) throws FileNotFoundException,
            IOException, IllegalArgumentException {
        /* 
         * Save the value of pbpLine so that it be retrieved if an 
         * exception is thrown.
         */
        currentLine = pbpLine;
        this.lineNum = lineNum;

        if (pbpLine.startsWith("info,")) {
            /* 
             * Information on game environment 
             */
            String infoLineArr[] = pbpLine.split(",");

            /* 
             * Special case: for pitching decisions, info line
             * may contain a key but not a value.
             */
            if (infoLineArr.length == 2 && 
                    (infoLineArr[1].equals("wp") || 
                    infoLineArr[1].equals("lp") ||
                    infoLineArr[1].equals("save"))) {
                
                /* Do nothing */
                return;
            }

            /* Check that line is valid input */
            if (infoLineArr.length != 3) {
                throw new IllegalArgumentException("Info lines must contain " +
                    "2 fields. File  " + eveFileName + ", line " + 
                    lineNum + ": " + currentLine);
            }
            // setInfo(pbpLine.split(",")[1], pbpLine.split(",")[2]);
            setInfo(infoLineArr[1], infoLineArr[2]);

        } else if (pbpLine.startsWith("start,") || pbpLine.startsWith("sub,")) {
            /* 
            * For lineup assignment, check that line 
            * contains five fields (excluding start/sub flag)
            */
            String idLineArr[] = pbpLine.split(",");

            /* Check that line is valid input */
            if (idLineArr.length != 6) {
                throw new IllegalArgumentException("Start/sub lines must " +
                    "contain 5 fields. File " + eveFileName + ", line " + lineNum + ": " + currentLine);
            }

            /* Make roster move. */
            if (pbpLine.startsWith("start,")) {
                makeRosterMove(true, idLineArr[1], idLineArr[2], 
                    idLineArr[3], idLineArr[4], idLineArr[5]);
            } else {
                makeRosterMove(false, idLineArr[1], idLineArr[2], 
                    idLineArr[3], idLineArr[4], idLineArr[5]);
            }
        } else if (pbpLine.startsWith("play,")) {
            /* 
             * Action on the field. Lines ending with NP
             * precede lineup moves and should be ignored. 
             */
            if (!pbpLine.endsWith(",NP")) {
                String[] playLineArr = pbpLine.split(",");

                /* Check that line is valid input */
                if (playLineArr.length != 7) {
                    throw new IllegalArgumentException("Play lines must " +
                    "consist of 6 fields. File " + eveFileName + ", line " + lineNum + ": " + currentLine);
                }
                readPlay(playLineArr[3], playLineArr[6]);
            }
        } else if (pbpLine.startsWith("ladj,")) {
            /*
             * Lineup adjustment, in the case of a team 
             * batting out-of-order.
             */

        } else if (pbpLine.startsWith("data,")) {
            String dataLineArr[] = pbpLine.split(",");
            
            /* Check that data is the appropriate length */
            if (dataLineArr.length != 4) {
                throw new IllegalArgumentException("Data lines must " +
                    "contain 3 fields (type, player ID, and value). File " + eveFileName + 
                    ", line " + lineNum + ": " + pbpLine);
            }
            setData(dataLineArr[1], dataLineArr[2], dataLineArr[3]);
        }
    }

    /**
     * If the conditions to start a new inning have been met,
     * reset inning state variables.
     */
    private void newInning() {
        /* If third out was previously made, new inning has begun. */
        if (outs == 3) {

            /* Add previous inning's stats to batting team object. */
            if (homeBatting) {
                home.linescoreAdd(inngRuns);
                home.addLOB(inngPA-inngRuns-outs);
            }
            else{
                visitor.linescoreAdd(inngRuns);
                visitor.addLOB(inngPA-inngRuns-outs);
            }

            /* Reverse homeBatting and reset inning state. */
            homeBatting = !homeBatting;

            for (int i = 0; i < 3; i++) {
                baserunnerSpots[i] = -1;
            }
            outs = 0;
            inngRuns = 0;
            inngPA = 0;

            /* If visitor is batting, a new inning has begun. */
            if (!homeBatting) {
                inng++;
            }

            /* Reset pitcher-innings. */
            home.getCurrentPitcher().startNewInning();
            visitor.getCurrentPitcher().startNewInning();
        }
    }

    /**
     * Read an event line.
     * @param playerID The batter's ID
     * @param event The event, containing both action at the plate and 
     *        (sometimes) baserunning information.
     */
    private void readPlay(String playerID, String event) {
        String plateEve;
        String bsrEve;

        /* Check if a new inning has begun */
        newInning();

        /* Period (.) implies baserunning component of event */
        if (event.contains(".")) {
            String[] eveArr = event.split("\\.");
            plateEve = eveArr[0];
            bsrEve = eveArr[1];
        } else {
            plateEve = event;
            bsrEve = "";
        }

        /* If home team is batting, then visitor is on defense. */
        if (homeBatting) {
            readPlateEvent(home, visitor, plateEve, bsrEve);
        } else {
            readPlateEvent(visitor, home, plateEve, bsrEve);
        }
    }

    /**
     * Parse 'play' line.
     * 
     * @param batTeam The team on offense
     * @param pitTeam The team on defense
     * @param plateEvent Event involving the batter (or in the case of a stolen base,
     *        wild pitch, etc, baserunners not reacting to a play made by the batter).
     * @param bsrEvent Event involving baserunners, occuring after <code>plateEvent</code>.
     */
    private void readPlateEvent(Team batTeam, Team pitTeam, String plateEvent, 
            String bsrEvent) { 
        int spot = (homeBatting) ? homeSpot : visSpot;
        BxScrPitcher pitcher = pitTeam.getCurrentPitcher();
        BxScrPositionPlayer batter = batTeam.getLineupSpot(spot); 
        batter.setPitcherCharged(pitcher); 
        boolean involvesBatter = true;      //Marked false for SB, WP, etc.
        int bAdvance = 0;                   //Bases taken by the batter
        pitcher.setInningRemoved(inng+1);   //+1 because inng is an inde

        /* Award errors on both plate and baserunning events */
        for (int i = 0; i < plateEvent.length()-1; i++) {
            if (plateEvent.charAt(i) == 'E') {
                int pos = Integer.parseInt(String.valueOf(plateEvent.charAt(i+1)));
                pitTeam.awardFieldingStats("E", pos);
            }
        }
        for (int i = 0; i < bsrEvent.length()-1; i++) {
            if (bsrEvent.charAt(i) == 'E') {
                int pos = Integer.parseInt(String.valueOf(bsrEvent.charAt(i+1)));
                pitTeam.awardFieldingStats("E", pos);
            }
        }

        /* Award double and triple play credit */
        if (plateEvent.contains("TP")) {
            pitTeam.add_double_triple_plays(false, 1);
        }
        if (plateEvent.contains("DP")) {
            pitTeam.add_double_triple_plays(true, 1);
        }
        
        /* 
         * Multiple steals may occur in the same plateEvent.
         * To catch this (admittedly rare) case, plateEvent
         * is split by the regex ";" and read in pieces.
         */
        for (String s : plateEvent.split(";")) {
            /* 
             * Check for baserunning events not involving batter.
             * For these events, set involvesBatter to false. 
             */
            if (s.startsWith("BK")) {           //Balk
                pitcher.incrementStats(
                    BaseballPlayer.KEY_BK,1);
                involvesBatter = false;
            } else if (s.startsWith("DI")) {    //Defensive indifference
                involvesBatter = false;
            } else if (s.startsWith("WP")) {    //Wild pitch
                pitcher.incrementStats(
                    BaseballPlayer.KEY_WP,1);
                involvesBatter = false;
            } else if (s.startsWith("PB")) {    //Passed ball
                pitTeam.awardFieldingStats(
                    BaseballPlayer.KEY_PB, 1);
                involvesBatter = false;
            } else if (s.startsWith("OA")) {    //Catch-all for other events.
                involvesBatter = false;
            } else if (s.startsWith("POCS")) {  //Pick-off + caught-stealing
                /* 
                 * "E" signifies an error occurred on the play, and
                 * is a special case. Typically, basestealing plays 
                 * containing an error result in a caught runner reaching
                 * base safely. 
                 */
                if (!s.contains("E")) {

                    /* Remove runner from basepaths */
                    if (s.charAt(4) == 'H') {
                        baserunnerSpots[2] = -1;
                    } else {
                        int strtBase = Integer.parseInt(String.valueOf(s.charAt(4)))-2;
                        baserunnerSpots[strtBase] = -1;
                    }

                    /* Award pitcher the out and increment outs in inning. */
                    outs++;
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
                involvesBatter = false;
            } else if (s.startsWith("PO") || s.startsWith("CS")) {

                /* See "POCS" case for this section's logical breakdown. */
                if (!s.contains("E")) {
                    if (s.charAt(2) == 'H') {
                        batTeam.getLineupSpot(baserunnerSpots[2]).incrementStats(BaseballPlayer.KEY_CS);
                        baserunnerSpots[2] = -1;
                    } else {
                        int strtBase = Integer.parseInt(String.valueOf(s.charAt(2)))-1;

                        batTeam.getLineupSpot(strtBase).incrementStats(BaseballPlayer.KEY_CS);
                        baserunnerSpots[strtBase] = -1;
                    }
                    outs++;
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
                involvesBatter = false;
            } else if (s.startsWith("SB")) {
                /* 
                 * If a player has stolen home, award him a run and 
                 * penalize the current pitcher. Increment the runs
                 * scored in the ining and remove his lineup index 
                 * from the third base element. 
                 */
                if (s.charAt(2) == 'H') { //Stole home -- increment runs.
                    BxScrPositionPlayer r = batTeam.getLineupSpot(baserunnerSpots[2]);
                    inngRuns++;
                    r.incrementStats(BaseballPlayer.KEY_R);
                    r.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
                    r.incrementStats(BaseballPlayer.KEY_SB,1);
                    baserunnerSpots[2] = -1;
                } else{
                    /* 
                     * For all other stolen bases, begin by checking that
                     * bsrEvent is blank: if an error occured while 
                     * fielding the stolen base, then the baserunner 
                     * may have reached a subsequent base, and this 
                     * batEvent has been overridden.
                     */
                    if (bsrEvent.equals("")) {
                        int endBase = 0, startBase = 0;
                        if (s.charAt(2)=='2') {
                            endBase = 1;
                            startBase = 0;
                        } else if (s.charAt(2) == '3') {
                            endBase = 2;
                            startBase = 1;
                        }
                        /* 
                         * Finally, increment player's SB stats and
                         * remove him from the basepaths.
                         */
                        batTeam.getLineupSpot(startBase).incrementStats(
                            BaseballPlayer.KEY_SB);
                        baserunnerSpots[endBase] = baserunnerSpots[startBase];
                        baserunnerSpots[startBase] = -1;
                    }
                }
                involvesBatter = false;
            }
            else if (s.contains("BOOT")) {
                /*
                 * SPECIAL CASE: A detected batting out of order results
                 * in outs being recorded.
                 * 
                 * NOTE: this section is still unfinished, and may need
                 * some more fine-tuning. */
                bAdvance = 0;
                // pitcher.add_outs(1);
                pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                outs++;
            }
            else if (s.startsWith("HP")) {                          //Hit-by-pitch
                pitcher.addBattersHBP(batter);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if (s.startsWith("S")) {                         //Single
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if (s.startsWith("D")) {                         //Double
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                batter.incrementStats(BaseballPlayer.KEY_2B);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 2;
            } else if (s.startsWith("T")) {                         //Triple
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                batter.incrementStats(BaseballPlayer.KEY_3B);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 3;
            } else if (s.startsWith("HR") || s.startsWith("H")) {   //Homerun
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.incrementStats(BaseballPlayer.KEY_H);
                batter.incrementStats(BaseballPlayer.KEY_HR);
                batter.incrementStats(BaseballPlayer.KEY_RBI);
                pitcher.incrementStats(BaseballPlayer.KEY_H);
                batter.setPitcherCharged(pitcher);
                bAdvance = 4;
            } else if (s.startsWith("W") || s.startsWith("IW")) {   //Walk
                pitcher.incrementStats(BaseballPlayer.KEY_BB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
                /* 
                 * If a walk contains a "+", the event contains 
                 * some component not involving the batter 
                 * (i.e. a stolen base). 
                 */
                if (s.contains("+")) {
                    readPlateEvent(batTeam,pitTeam,
                        s.split("[+]")[1],bsrEvent);
                    /* 
                     * Because this call is done recursively,
                     * ignore the baserunning so that it is
                     * not double-counted. 
                     */
                    bsrEvent = "";
                }
            } else if (s.startsWith("K")) {                         //Strikeout
                batter.incrementStats(BaseballPlayer.KEY_AB);
                pitcher.incrementStats(BaseballPlayer.KEY_SO);
                /* 
                 * Like walks, a non-batter event may occur 
                 * on the play. Otherwise, increment inning
                 * outs as usual.
                 */
                if (s.contains("+")) {
                    readPlateEvent(batTeam, pitTeam, s.split("[+]")[1], bsrEvent);
                    bsrEvent = "";
                    /* 
                     * If the batter does not reach base,
                     * increment outs. 
                     */
                    if (!bsrEvent.contains("B-")) {
                        outs++;
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                    }
                } else {
                    outs++;
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
            } else if (s.startsWith("FC")) {                        //Fielder's choice
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            } else if (s.contains("FO")) {                          //Force-out
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;

                for (int i = 0; i < s.length()-2; i++) {
                    if (s.charAt(i) == '(' && s.charAt(i+2) == ')') {
                        if (s.charAt(i+1) != 'B') {
                            int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                            baserunnerSpots[oldBase] = -1;
                        }
                        outs++;
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                    }
                }
            } else if (s.contains("SH")) {                          //Sac hit (bunt)
                batter.incrementStats(BaseballPlayer.KEY_SH);
                if (!bsrEvent.contains("B-")) { //Temporary fix
                    outs++;
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
            } else if (s.contains("SF")) {                          //Sac fly
                batter.incrementStats(BaseballPlayer.KEY_SF);
                outs++;
                pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
            } else if (Character.isDigit(s.charAt(0))) {            //Fielded out
                batter.incrementStats(BaseballPlayer.KEY_AB);
                
                if (s.contains("GDP") || s.contains("GTP") || s.contains("/G/DP/")) {    
                    //THIS WHOLE THING COULD USE SOME WORK!
                    for (int i = 0; i < s.length()-2; i++) {
                        if (s.charAt(i) == '(' && s.charAt(i+2) == ')') {
                            if (s.charAt(i+1) != 'B') {
                                int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                                baserunnerSpots[oldBase] = -1;
                            }
                            outs++;
                            pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                        }
                    }
                    if (!s.contains("(B)")) {
                        outs++; //batter
                        pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                    }
                } else if (s.contains("LDP") || s.contains("LTP")) {
                    int outs_on_play = 0;
                    for (int i = 0; i < s.length()-2; i++) {
                        if (s.charAt(i) == '(' && s.charAt(i+2) == ')') {
                            if (s.charAt(i+1) != 'B') {
                                int oldBase = Integer.parseInt(String.valueOf(s.charAt(i+1)))-1;
                                baserunnerSpots[oldBase] = -1;
                            }
                            outs_on_play++;
                            pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                        }
                    }
                    outs += outs_on_play;
                } else if (s.contains("E")) { //Error made by some subsequent fielder
                    batter.setPitcherCharged(pitcher);
                    bAdvance = 1;
                } else {
                    outs++;
                    pitcher.incrementStats(BaseballPlayer.KEY_BATTERS_RETIRED);
                }
            } else if (s.startsWith("C/E")) { //catcher's interference
                bAdvance = 1;
            } else if (s.startsWith("E")) {
                batter.incrementStats(BaseballPlayer.KEY_AB);
                batter.setPitcherCharged(pitcher);
                bAdvance = 1;
            }
        }

        readBaserunning(batTeam, pitTeam, batter, pitcher, bsrEvent, bAdvance);

        /* Increment spot, and other inning-specific stats. */
        if (involvesBatter) {
            inngPA++;                       //For inning LOB calculation
            incrementSpot();
            pitcher.incrementInningBF();  
        }
    }

    private void readBaserunning(Team battingTeam, Team pitchingTeam, 
        BxScrPositionPlayer batter, BxScrPitcher pitcher, String bsrEvent, int bAdvance)
    {
        if (bsrEvent.equals("")) {
            if (bAdvance == 4) {
                batter.incrementStats(BaseballPlayer.KEY_R);
                batter.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
                inngRuns++;
            } else if (bAdvance > 0 && bAdvance < 4) {
                baserunnerSpots[bAdvance-1] = batter.getLineupSpot()-1;
            }
            return;
        }

        for (String s : bsrEvent.split(";")) {
            if (s.startsWith("B")) {
                if (s.startsWith("B-")) {
                    if (s.charAt(2) == 'H')
                        bAdvance = 4;
                    else
                        bAdvance = Integer.parseInt(String.valueOf(s.charAt(2)));
                } else if (s.startsWith("BX")) {
                    if (s.contains("E")) { //Error negates the out
                        if (s.charAt(2) == 'H')
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
                } catch (IndexOutOfBoundsException e) {
                    r = battingTeam.getLineupSpot(0);
                }
                if (s.contains("-")) { //Runner movement
                    if (s.charAt(2) == 'H') { //Runner scores
                        baserunnerSpots[startBase] = -1;
                        r.incrementStats(BaseballPlayer.KEY_R);
                        if (!s.contains("(NR)"))
                            batter.incrementStats(BaseballPlayer.KEY_RBI);
                        try{
                            r.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
                        } catch (NullPointerException e) {
                            pitcher.incrementStats(BaseballPlayer.KEY_R);
                        }
                        // r.getPitcherCharged().add_runs(1);
                        inngRuns++;
                    } else {  //Runner moves to new base
                        int endBase = Integer.parseInt(String.valueOf(s.charAt(2)))-1;
                        if (startBase != endBase) {
                            baserunnerSpots[endBase] = baserunnerSpots[startBase];
                            baserunnerSpots[startBase] = -1;
                        }
                    }
                } else if (s.contains("X")) { //Runner tagged out
                    if (s.contains("E")) { //Error negates out
                        if (s.charAt(2) == 'H') { //Runner scores on error
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

        if (bAdvance == 4) {
            batter.incrementStats(BaseballPlayer.KEY_R);
            batter.getPitcherCharged().incrementStats(BaseballPlayer.KEY_R);
            inngRuns++;
        } else if (bAdvance > 0 && bAdvance < 4) {
            baserunnerSpots[bAdvance-1] = batter.getLineupSpot()-1;
        }
        
    }

    /** 
     * Increment batting spot of the batting team by 1, 
     * wrapping from 8 to 0. 
     */
    private void incrementSpot() {
        if (homeBatting)
            homeSpot = (homeSpot+1)%9;
        else 
            visSpot  = (visSpot+1)%9;
    }

    /**
     * Award end-of-game data such as earned runs.
     * 
     * @param key The type of data. Currently, this should always be "er".
     * @param playerID The player's ID.
     * @param value The data's value.
     */
    public void setData(String key, String playerID, String value) {
        int valueInt;
        BxScrPitcher tmpPitcher;

        /* Check that value is an integer */
        try {
            valueInt = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Data lines must end with an integer " +
                "value. File " + eveFileName + ", line " + lineNum + ": " + currentLine);
        }
        
        if (key.equals("er")) {
            /* Check each team for player. When found, award earned runs. */
            tmpPitcher = visitor.getPitcher(playerID);
            if (tmpPitcher != null) {
                tmpPitcher.incrementStats(BaseballPlayer.KEY_ER, valueInt);
            }
            tmpPitcher = home.getPitcher(playerID);
            if (tmpPitcher != null) {
                tmpPitcher.incrementStats(BaseballPlayer.KEY_ER, valueInt);
            }
        }
    }

    /**
     * Set game enviroment information.
     * 
     * @param  key The type of information.
     * @param  value The information.
     * @throws FileNotFoundException If team roster files cannot be opened.
     * @throws IOException if a team's city/name cannot be found in TEAM file.
     */
    private void setInfo(String key, String value) throws FileNotFoundException,
            IOException {
        /* 
        * Check key for the type of information stored. Note that 
        * with the exception of visteam and hometeam fields, this 
        * data is purely situation-describing, and has no impact on
        * the running of this class.
        */
        String[] cityAndName;

        if (key.equals("visteam")) {
            /* 
            * Initialize visiting team's roster reader, 
            * check TEAM file for team's name. Initialize
            * visitor object. 
            */
            String[] rosDirList = rosDir.list();
            visRosFileName = value + year + ".ROS";
            boolean openedRosFile = false;
            
            /* Try to open visitor's roster file */
            for (String s : rosDirList) {
                if (s.contains(visRosFileName)) {       
                    visRosReader = new RandomAccessFile(
                        new File(rosDir.getPath(), visRosFileName),
                        "r"
                    );
                    openedRosFile = true;
                    break;
                }
            }

            /* If roster file is not found, throw exception. */
            if (!openedRosFile) {
                throw new FileNotFoundException("Could not find file " + visRosFileName + 
                " in directory " + rosDir.getPath());
            }

            /* Initialize visitor object. */
            cityAndName = findTeamCityAndName(value);
            visitor = new Team(value, cityAndName[0], cityAndName[1], false);

        } else if (key.equals("hometeam")) {
            /* 
             * Initialize visiting team's roster reader,
             * check TEAM file for team's city/name.
             * Initialize visitor object.
             */
            String[] rosDirList = rosDir.list();
            homeRosFileName = value + year + ".ROS";
            boolean openedRosFile = false;

            /* Try to open visitor's roster file */
            for (String s : rosDirList) {
                if (s.contains(homeRosFileName)) {
                    homeRosReader = new RandomAccessFile(
                        new File(rosDir.getPath(), homeRosFileName),
                        "r"
                    );
                    openedRosFile = true;
                    break;
                }
            }

            /* If roster file is not found, throw exception. */
            if (!openedRosFile) {
                throw new FileNotFoundException("Could not find file " + visRosFileName + 
                " in directory " + rosDir.getPath());
            }

            /* Initialize visitor object. */
            cityAndName = findTeamCityAndName(value);
            home = new Team(value, cityAndName[0], cityAndName[1], true);

        } else if (key.equals("date")) {
            
            /* Set game's date. */
            stdDateString = value;
            String[] dateArr = value.split("/");
            try {
                usaDateString = dateArr[1] + "/" + dateArr[2] + "/" + dateArr[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                usaDateString = "??/??/????";
            }

        } else if (key.equals("daynight")) {
            /* 
             * Check if game was played during day or 
             * at night. If value is unreadable, default to day.
             */
            if (value.equals("night")) {
                daynight = 'N';
            } else {
                daynight = 'D';
            }
        } else if (key.equals("number")) {
            /* 
             * If value equals 0: single game on date.
             * If value equals 1 or 2: part of double-header.
             * If NumberFormatException occurs, assume single-game. 
             */
            try {
                gmNumber = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                gmNumber = 0;
            }
        } else if (key.equals("attendance")) {
            try {
                attendance = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                attendance = 0;
            }
        } else if (key.equals("timeofgame")) {
            try {
                timeOfGame = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                timeOfGame = 0;
            }
        } else if (key.equals("wp")) {
            wpID = value;
        } else if (key.equals("lp")) {
            lpID = value;
        } else if (key.equals("save")) {
            saveID = value;
        }
    }

    /**
     * Add a player to team's rosters.
     * 
     * @param  isStarter <code>true</code> for starters, <code>false</code> for subs.
     * @param  playerID The player's unique ID.
     * @param  playerName The player's name.
     * @param  playerTeam <code>"0"</code> for visiting team, <code>"1"</code> for home.
     * @param  batSpot Player's spot in batting order (1-9 or 0 for pitchers in DH games).
     * @param  position Player's position (1-12).
     * @throws IllegalArgumentException Thrown if any argument does not conform to 
     *         specifications.
     * @throws IOException if a player's name is not found in corresponding roster file.
     */
    private void makeRosterMove(boolean isStarter, String playerID, String playerName,
            String playerTeam, String batSpot, String position)
            throws IllegalArgumentException, IOException {
        /* Begin by checking that all input is valid */

        /* Check that playerTeam is a 1 or 0. */
        if (!playerTeam.equals("0") && !playerTeam.equals("1")) {
            throw new IllegalArgumentException("Illegal argument in start/sub line. " +
                "Team field must be either 0 (visitor) or 1 (home). File " + eveFileName + 
                ", line " + lineNum + ": " + currentLine);
        }

        /* Check if batSpot is a valid number between 0 and 9. */
        try {
            int spotInt = Integer.parseInt(batSpot);
            if (spotInt < 0 || spotInt >= 10) { 
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Illegal argument in start/sub line. " +
                "Player's batting order spot must be an integer between 1-9 for " + 
                "position players, or 0 for pitchers in games using the DH rule. " + 
                "File " + eveFileName + ", line " + lineNum + ": " + currentLine);
        }

        /* Check if position is a valid number between 1 and 12. */
        try {
            int posInt = Integer.parseInt(position);
            if (posInt < 1 || posInt > 12) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Illegal argument in start/sub line. " +
                "Player's position must be an integer between 1 and 12. " + 
                "File " + eveFileName + ", line " + lineNum + ": " + currentLine);
        }

        /* Get player's name. */
        // String[] firstLast = {"",""};
        // try {
        //     if (playerTeam.equals("0")) { 
        //         firstLast = getPlayerName(false, playerID);
        //     } else {
        //         firstLast = getPlayerName(true, playerID);
        //     }
        // } catch (IOException ioe) {
        //     /* If isn't found in roster file, parse the name provided. */
        //     String nameArr[] = playerName.split(" ");
        //     if (nameArr.length == 2) {
        //         firstLast[0] = nameArr[0];
        //         firstLast[1] = nameArr[1];
        //     } else {
        //         firstLast[1] = nameArr[nameArr.length - 1];
        //         for (int i = 0; i < nameArr.length - 1; i++) {
        //             firstLast[0] += nameArr[i];
        //         }
        //     }
        // }

        /* Check roster file for player's name, then add to team. */
        if (playerTeam.equals("0")) {
            String[] firstLast = getPlayerName(false, playerID);
            visitor.addPlayer(isStarter, playerID, firstLast[0], firstLast[1],
                batSpot, position);
        } else {
            String[] firstLast = getPlayerName(true, playerID);
            home.addPlayer(isStarter, playerID, firstLast[0], firstLast[1],
                batSpot, position);
        }
    }

    /**
     * 
     * @param isHome <code>true</code> for home team, <code>false</code> for visitor.
     * @param playerID The ID of the player to search for.
     * @return A <code>String</code> array containing the player's first and last names.
     * @throws IOException If player could not be found in the team's roster file.
     */
    private String[] getPlayerName(boolean isHome, String playerID)
            throws IOException {
        String line;
        RandomAccessFile rosReader;

        /* Get correct reader and reset to top of file. */
        if (!isHome) {
            rosReader = visRosReader;
        } else {
            rosReader = homeRosReader;
        }
        rosReader.seek(0);
        
        /*
         * Check roster file for line matching playerID.
         * If none found, throws IOException.
         */
        while ((line = rosReader.readLine()) != null) {
            if (line.startsWith(playerID)) {
                String[] lineArr = line.split(",");
                String[] first_last = {lineArr[2],lineArr[1]};
                return first_last;
            }
        }

        /* If player could not be found, throw an exception. */
        if (!isHome) { 
            throw new IOException("Player " + playerID + " could not be found in " + 
                "file " + visRosFileName + ". Line: " + currentLine);
        } else {
            throw new IOException("Player " + playerID + " could not be found in " +
                "file " + homeRosFileName + ". Line: " + currentLine);
        }
    }

    /**
     * Search <code>teamReader</code> and find the city and name belonging
     * correponding to the input <code>teamID</code>
     * 
     * @param  teamID The 3-letter team ID.
     * @return An array whose first element is the team's city and second element is
     *         its name. If the team cannot be found in the TEAM file, <code>teamID</code> 
     *         is returned as both elements of the array.
     * @throws IOException If TEAM file cannot be read or if team cannot be found in file.
     */
    private String[] findTeamCityAndName(String teamID) throws IOException {
        teamReader.seek(0);
        String line;

        while ((line = teamReader.readLine()) != null) {
            if (line.startsWith(teamID)) {
                String[] lineArr = line.split(",");
                String[] cityAndName = {lineArr[2], lineArr[3]};
                return cityAndName;
            }
        }
        throw new IOException("Team " + teamID + " could not be found in file " +
        "TEAM" + year + "File " + eveFileName + ",  line " + lineNum + ": " + currentLine);
    }

    /** @return game's attendance. */
    public int getAttendance() {
        return attendance;
    }

    /**  
     * @return game's number. <code>0</code> for single-game, 
     * <code>1</code> or <code>2</code> if part of double-header.
     */
    public int getGameNumber() {
        return gmNumber;
    }

    /** 
     * @return <code>'D'</code> for day game, <code>'N'</code> 
     * for night day. 
     */
    public char getDayNight() {
        return daynight;
    }

    /** @return time of game in minutes. */
    public int getTimeOfGame() {
        return timeOfGame;
    }

    /** @return date (MM/DD/YYYY) on which game occurred. */
    public String getUsaDateString() {
        return usaDateString;
    }

    public String getStdDateString() {
        return stdDateString;
    }

    /** @return game's unique Retrosheet ID. */
    public String getGameID() {
        return gameID;
    }

    /** @return the last line read into this object. */
    public String getLastLine() {
        return currentLine;
    }

    public int getOuts() {
        return outs;
    }

}