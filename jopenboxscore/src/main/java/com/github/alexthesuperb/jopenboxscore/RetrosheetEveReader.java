package com.github.alexthesuperb.jopenboxscore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Read Retrosheet Event (.EVE, .EVA, and .EVN) files.
 */
public class RetrosheetEveReader {

    /** Name of file to be read. */
    private String fileName;

    /** The number of lines read by <code>pbpReader</code>. */
    private int lineNum;

    /** Look for team and roster files containing this year. */
    private String year;

    /** <code>BufferedReader</code> to read file. */
    private BufferedReader pbpReader;

    /** A list of the games processed by this instance. */
    private List<BoxscoreGameAccount> gameAccounts;

    /** 
     * A directory containing TEAM and roster files. This object is
     * passed into each <code>BoxscoreGameAccount</code> instance.
     */
    private File teamRosDir;

    public RetrosheetEveReader(String eveFileName, String year, File teamRosDir)
            throws IOException {
        gameAccounts = new LinkedList<BoxscoreGameAccount>();
        this.fileName = eveFileName;
        this.year = year;
        this.teamRosDir = teamRosDir;
        lineNum = 0;

        /* Check if file with name fileName exists. If it does not, throw
        a new FileNotFoundException. */
        File tempFile = new File(eveFileName);
        if (tempFile.exists()) {
            pbpReader = new BufferedReader(new FileReader(eveFileName));
        } else {
            throw new FileNotFoundException("Event file " + eveFileName + " not found.");
        }
    }

    /**
     * @throws FileNotFoundException thrown from <code>BxScrGameAccount</code>
     *         object if a required TEAM roster file cannot be found.
     * @throws IOException thrown from <code>BxScrGameAccount</code> object if
     *         required data cannot be found in TEAM file or roster file.
     * @throws IllegalArgumentException if a line read to <code>BxScrGameAccount</code>
     *         object does not conform to expected structure.
     */
    public void readAll() throws FileNotFoundException, IOException, 
            IllegalArgumentException {
        String line;
        BoxscoreGameAccount currGame = null;

        /* 
         * Read through the entire file, one line at a time.
         * A line beginning with the field "id" signifies the 
         * start of a new game -- add the previous BxScrGameAccount
         * to this instance's gameAccounts list and initialize a new
         * game. The local variable currGame points to this game object. 
         */
        while ((line = pbpReader.readLine()) != null) {
            lineNum++;
            if (line.startsWith("id,")) {
                if (currGame != null) {
                    currGame.finalize();
                    gameAccounts.add(currGame);
                }
                currGame = new BoxscoreGameAccount(line.substring(3), year, fileName, 
                    teamRosDir);
            }
            if (currGame != null) {
                currGame.addLine(line, lineNum);
            }
        }

        /* 
         * Since the file doesn't end on an "id" line, 
         * add final game account to list.
         */
        if (currGame != null) {
            currGame.finalize();
            gameAccounts.add(currGame);
        }
    }

    /**
     * @throws FileNotFoundException thrown from <code>BxScrGameAccount</code>
     *         object if a required TEAM roster file cannot be found.
     * @throws IOException thrown from <code>BxScrGameAccount</code> object if
     *         required data cannot be found in TEAM file or roster file.
     * @throws IllegalArgumentException if a line read to <code>BxScrGameAccount</code>
     *         object does not conform to expected structure.
     */
    public void readGamesByID(List<String> gameIDs) throws FileNotFoundException, 
            IOException, IllegalArgumentException {
        String line;
        BoxscoreGameAccount currGame = null;
        boolean readThisGame = false;
        
        /* 
         * Each time a line beginning with 'id' flag is encountered, 
         * add previous game to gameAccounts and set readThisGame
         * flag to false. If gameIDs contains the current id, set
         * readThisGame to true and initialize a new BxScrGameAccount
         * object. Only read in lines to this object while readThisGame
         * is true.
         */
        while ((line = pbpReader.readLine()) != null) {
            /* 
             * Increment file line file line number with each line read in. 
             * This is passed into the currGame.addLine(line, lineNum) for
             * error reporting.
             */
            lineNum++;
            /*
             * "id" lines mark the beginning of a new game account. If the 
             * previous game was read (signified by a non-null GameAccount
             * object and flag readThisGame == true), add that game to 
             * gameAccounts. 
             * 
             * Additionally, mark readThisGame false and set currGame to null.
             * This prevents unwanted games from being read and the same 
             * currGame being added to gameAccounts multiple times.
            */
            if (line.startsWith("id,")) {
                if ((currGame != null) && readThisGame) {
                    currGame.finalize();
                    gameAccounts.add(currGame);
                }
                readThisGame = false;
                currGame = null;

                if (gameIDs.contains(line.substring(3))) {
                    readThisGame = true;
                    currGame = new BoxscoreGameAccount(line.substring(3), year, fileName, 
                        teamRosDir);
                    gameIDs.remove(currGame.getGameID());
                }
            }
            if (readThisGame && currGame != null) {
                currGame.addLine(line, lineNum);
            }
        }

        /* 
         * Since the file doesn't end on an "id" line, 
         * add final game account to list.
         */
        if (currGame != null) {
            currGame.finalize();
            gameAccounts.add(currGame);
        }
    }
    
    /**
     * @throws FileNotFoundException thrown from <code>BxScrGameAccount</code>
     *         object if a required TEAM roster file cannot be found.
     * @throws IOException thrown from <code>BxScrGameAccount</code> object if
     *         required data cannot be found in TEAM file or roster file.
     * @throws IllegalArgumentException if <code>start</code> or <code>end</code> are 
     *         not integers, or if an individual line in file does not conform to 
     *         its expected format.
     */
    public void readGamesWithinRange(String start, String end) 
            throws FileNotFoundException, IOException, IllegalArgumentException {
        String line;
        int startInt;
        int endInt;
        int gameDateInt;
        String gameID;
        BoxscoreGameAccount currGame = null;
        boolean readThisGame = false;

        /* Check that start and end are valid dates. */
        try {
            startInt = Integer.parseInt(start);
            endInt = Integer.parseInt(end);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("One or both of the dates in" +
            "requested range is not a valid argument. Dates must conform to " +
            "the 4-digit integer code MMDD, where MM is a month and DD is a date.");
        }

        /* Read the file. */
        while ((line = pbpReader.readLine()) != null) {
            lineNum++;
            if (line.startsWith("id,")) {
                if (currGame != null && readThisGame) {
                    currGame.finalize();
                    gameAccounts.add(currGame);
                }
                readThisGame = false;
                currGame = null;   

                gameID = line.substring(3);
                try {
                    gameDateInt = Integer.parseInt(gameID.substring(7,11));
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Game IDs must conform to " +
                        "the format TTTYYYYMMDDN, where TTT is the home team's ID, " +
                        "YYYY is the year, MM is the month, DD is the day, and game " +
                        "number. Line: " + line);
                }
                
                /* Check if game falls into range. If it does, read. */
                if (gameDateInt >= startInt && gameDateInt <= endInt) {
                    readThisGame = true;
                    currGame = new BoxscoreGameAccount(gameID, year, fileName, 
                        teamRosDir);
                }   
            }
            if (readThisGame && currGame != null) {
                currGame.addLine(line, lineNum);
            }
        }

        /* 
         * Since the file doesn't end on an "id" line, 
         * add final game account to list.
         */
        if (currGame != null) {
            currGame.finalize();
            gameAccounts.add(currGame);
        }
    }
    
    // public void readNexGame() throws IOException {
    //     /* Implement here... */
        
    // }
    
    // public String getInfoNextGame() throws IOException {
    //     /* Implement here... */
        
    //     /* Return a String describing the next unread game... */
    //     return null;
    // }
    
    /**
     * <code>BxScrFileReader</code>'s only output: a <code>List</code>
     * of game accounts.
     * 
     * @return a list of game accounts, each containing the teams, score,
     * and other boxscore information on a particular game.
     */
    public List<BoxscoreGameAccount> getGameAccounts() {
        return gameAccounts;
    }

    /**
     * Closes private <code>BufferedReader</code> object and returns a 
     * list of games processed.
     * 
     * @return a <code>List</code> of <code>BxScrGameAccount</code> objects
     *         processed by this instance.
     * @throws IOException if a problem occurs while closing 
     *         <code>BufferedReader</code>.
     */
    public List<BoxscoreGameAccount> close() throws IOException {
        pbpReader.close();
        return gameAccounts;
    }
    
}