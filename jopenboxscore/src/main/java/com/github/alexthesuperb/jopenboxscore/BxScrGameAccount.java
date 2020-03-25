/*
 * C-style comment with author/copyright information...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
// import java.security.InvalidParameterException;

/**
 * <code>BxScrGame</code> implements the interface <code>GameAccount</code>. It
 * is the most class used to store Retrosheet play-by-play data; its purpose is
 * to track a game's state, using only simple statistics, to create
 * human-readable boxscores.
 */
public class BxScrGameAccount implements GameAccount {

    private String gameID;
    private String year;
    private String date;
    private char daynight;
    private int gmNumber;
    private int attendance;
    private int timeOfGame;
    private String wpID;
    private String lpID;
    private String saveID;

    Team visitor;
    Team home;
    
    private RandomAccessFile teamReader;
    private RandomAccessFile visRosReader;
    private RandomAccessFile homeRosReader;

    private String visRosFileName;
    private String homeRosFileName;
    
    private String currentLine;

    /** constructor */
    public BxScrGameAccount(String gameID, String year, RandomAccessFile teamReader) {
        this.gameID = gameID;
        this.year = year;
        this.teamReader = teamReader;
    }

    /** @return the last line read into this object. */
    public String getLastLine() {
        return currentLine;
    }

    /** 
     * Put the finishing touches on an account on a game. For best results,
     * make sure to call this after all lines have been read.
     */
    public void finalize() {
        
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
    public void addLine(String pbpLine) throws FileNotFoundException,
            IOException, IllegalArgumentException {
        /* 
         * Save the value of pbpLine so that it be retrieved if an 
         * exception is thrown.
         */
        currentLine = pbpLine;

        if (pbpLine.startsWith("info,")) {
            /* 
             * Information on game environment 
             */
            String infoLineArr[] = pbpLine.split(",");
            if (infoLineArr.length != 3) {
                throw new IllegalArgumentException("Info lines must contain " +
                    "2 fields. Line: " + currentLine);
            }
            setInfo(pbpLine.split(",")[1], pbpLine.split(",")[2]);

        } else if (pbpLine.startsWith("start,")) {
            /* 
             * For starting lineup assignment, check that line 
             * contains five fields. 
             */
            String idLineArr[] = pbpLine.split(",");
            if (idLineArr.length != 6) {
                throw new IllegalArgumentException("Start/sub lines must " +
                    "contain 5 fields. Line: " + currentLine);
            }
            makeRosterMove(true, idLineArr[1], idLineArr[2], 
                idLineArr[3], idLineArr[4], idLineArr[5]);

        } else if (pbpLine.startsWith("play,")) {
            /* Action on the field */
            
            if (!pbpLine.endsWith(",NP")) {

            }
        } else if (pbpLine.startsWith("sub,")) {
            /* Roster substitution */

        } else if (pbpLine.startsWith("ladj,")) {
            /*
             * Lineup adjustment, in the case of a team 
             * batting out-of-order.
             */

        } else if (pbpLine.startsWith("data,")) {
            // setData(pbpLine.split(","));
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
        if (key.equals("visteam,")) {
            /* 
             * Initialize visiting team's roster reader, 
             * check TEAM file for team's name. Initialize
             * visitor object. 
             */
            visRosFileName = value + year + ".ROS";
            visRosReader = new RandomAccessFile(new File(visRosFileName), "r");
            String[] cityAndName = findTeamCityAndName(value);
            visitor = new Team(value, cityAndName[0], cityAndName[1], false);
        } else if (key.equals("hometeam,")) {
            /* 
             * Initialize visiting team's roster reader,
             * check TEAM file for team's city/name.
             * Initialize visitor object.
             */
            homeRosFileName = value + year + ".ROS";
            homeRosReader = new RandomAccessFile(new File(homeRosFileName), "r");
            String[] cityAndName = findTeamCityAndName(value);
            home = new Team(value, cityAndName[0], cityAndName[1], true);
        } else if (key.equals("data")) {
            /* Set game's date. */
            String[] dateArr = value.split("/");
            date = dateArr[1] + "/" + dateArr[2] + "/" + dateArr[0];
        } else if (key.equals("daynight")){
            if (key.equals("day")) {
                daynight = 'D';
            } else {
                daynight = 'N';
            }
        } else if (key.equals("number")){
            /* 
             * If value == 0: single game on date.
             * If value == 1 or 2: part of double-header.
             * If NumberFormatException occurs, assume single-game. 
             */
            try {
                gmNumber = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                gmNumber = 0;
            }
        } else if (key.equals("attendance")){
            attendance = Integer.parseInt(value);
        } else if (key.equals("timeofgame")){
            timeOfGame = Integer.parseInt(value);
        } else if (key.equals("wp")){
            wpID = value;
        } else if (key.equals("lp")){
            lpID = value;
        } else if (key.equals("save")){
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
     */
    private void makeRosterMove(boolean isStarter, String playerID, String playerName,
            String playerTeam, String batSpot, String position)
            throws IllegalArgumentException, IOException {
        /* Begin by checking that all input is valid */

        /* Check that playerTeam is a 1 or 0. */
        if (!playerTeam.equals("0") && !playerTeam.equals("1")) {
            throw new IllegalArgumentException("Illegal argument in start/sub line. " +
                "Team field must be either 0 (visitor) or 1 (home). Line: " + currentLine);
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
                "Line: " + currentLine);
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
                "Line: " + currentLine);
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

        while ((line = teamReader.readLine()) != null){
            if (line.startsWith(teamID)){
                String[] lineArr = line.split(",");
                String[] cityAndName = {lineArr[2], lineArr[3]};
                return cityAndName;
            }
        }
        throw new IOException("Team " + teamID + " could not be found in file " +
        "TEAM" + year + ". Line: " + currentLine);
    }

}