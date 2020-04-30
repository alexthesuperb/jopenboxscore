/*
 * C-Style comment with author and copyright info...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <p>
 * This interface describes an account of a game transcribed in a play-by-play
 * text file. Implementations of this interface should only accept input in the
 * form of single play-by-play lines through the method <code>addLine(String pbpLine,
 *  int lineNum)</code>.
 * </p><p>
 * Its original implementation, <code>BoxscoreGameAccount</code>, reads through
 * Retrosheet.org's play-by-play accounts of baseball games (called <i>event files</i>),
 * and through its method <code>printBoxscore(BufferedWriter outWriter)</code>, creates
 * human-readable newspaper-style boxscores.
 */
public interface GameAccount {

    /**
     * This interface was designed to encourage a single point of entry to the 
     * classes that implement it.
     * @param pbpLine a line describing an event in a game.
     * @param lineNum the number of the line within the file from which it originates.
     * @throws FileNotFoundException if some required supplementary file (for example,
     * a file containing player or team names) cannot be found.
     * @throws IOException if an I/O exception occurs while reading from a supplementary
     * file.
     * @throws IllegalArgumentException if <code>pbpLine</code> does not follow the
     * correct syntax.
     */
    public void addLine(String pbpLine, int lineNum) throws FileNotFoundException,
            IOException, IllegalArgumentException;
    
}