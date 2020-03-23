/* C-style comment with author/copyright information... */

package com.github.alexthesuperb.jopenboxscore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

/** */
public class BxScrFileReader implements EventFileReader {

    /** Name of file to be read. */
    private String fileName;

    /** Look for team and roster files containing this year. */
    private String year;

    /** <code>BufferedReader</code> to read file. */
    private BufferedReader pbpReader;

    /** to read TEAM file, which contains team name information. */
    private RandomAccessFile teamReader;

    /** A list of the games processed by this instance. */
    private List<BxScrGameAccount> gameAccounts;

    /**
     * Open a new object of type <code>BxScrReader</code>.
     * 
     * @param fileName The name of the file to read from.
     * @param year The year of the query, for team and roster files.
     * @throws FileNotFoundException if <code>RandomAccessFile</code> fails to initialize.
     */
    public BxScrFileReader(String fileName, String year) throws FileNotFoundException {
        this(fileName, year, new RandomAccessFile(new File("TEAM" + year),"r"));
    }

    /**
     * Open a new object of type <code>BxScrReader</code>.
     * 
     * @param fileName The name of the file to read from.
     * @param year The year of the query, for team and roster files.
     * @param teamReader <code>RandomAccessFile</code> for reading team information.
     * @throws FileNotFoundException if <code>BufferedReader</code> fails to initialize.
     */
    public BxScrFileReader(String fileName, String year, RandomAccessFile teamReader) 
            throws FileNotFoundException {
        this.fileName = fileName;
        this.year = year;
        this.teamReader = teamReader;
        pbpReader = new BufferedReader(new FileReader(fileName));
        gameAccounts = new LinkedList<BxScrGameAccount>();
    }


    public void readAll() throws IOException {
        
    }
    
    public void readGamesByID(List<String> gameIDs) throws IOException {
        // TODO Auto-generated method stub
    }
    
    public void readGamesWithinRange(String start, String end) throws IOException {
        // TODO Auto-generated method stub
        
    }
    
    public void readNexGame() throws IOException {
        // TODO Auto-generated method stub
        
    }
    
    public String getInfoNextGame() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * <code>BxScrFileReader</code>'s only output: a <code>List</code>
     * of game accounts.
     * 
     * @return a list of game accounts, each containing the teams, score,
     * and other boxscore information on a particular game.
     */
    public List<BxScrGameAccount> getGameAccounts() {
        return gameAccounts;
    }
    
}