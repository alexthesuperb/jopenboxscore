/*
 * C-style header comment...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.io.IOException;
import java.util.List;

/**
 * <code>EventFileReader</code> is an interface describing the options a user
 * may have for parsing a Retrosheet.org play-by-play file. 
 */
public interface EventFileReader {

    /** Read all games in file. 
     * @throws IOException*/
    public void readAll() throws IOException;

    /**
     * Search file and read games matching argument IDs.
     * @param gameIDs The IDs of games to read.
     * @throws IOException
     */
    public void readGamesByID(List<String> gameIDs) throws IOException;

    /**
     * Search file and read only games whose dates fall within
     * range.
     * @param start Starting date of range.
     * @param end Ending date of range.
     * @throws IOException
     */
    public void readGamesWithinRange(String start, String end) throws IOException;

    /** Read the next unread game in file. 
     * @throws IOException*/
    public void readNexGame() throws IOException;

    /**
     * Return a <code>String</code> containing information on the next
     * unread game in file.
     * @return information on next unread game, like its date or matchup.
     * @throws IOException
     */
    public String getInfoNextGame() throws IOException;

    // /**
    //  * @return a <code>List</code> containing all game accounts created
    //  * by this instance.
    //  */
    // public List<GameAccount> getGameAccounts();

}