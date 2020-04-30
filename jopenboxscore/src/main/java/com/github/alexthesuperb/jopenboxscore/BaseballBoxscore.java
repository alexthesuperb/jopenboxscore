package com.github.alexthesuperb.jopenboxscore;

import java.io.IOException;

/**
 * <p>
 * The root interface of the <i>Boxscore</i> hierarchy. Classes
 * implementing this interface can take on many different forms: 
 * some may return human-readable game accounts while others may
 * generate JSON, CSV, SQL, or some other format. 
 * </p><p>
 * However, all implementations should be made as 
 * straightforward as possible to users: the provided method,
 * <code>write()</code>, should be its only public point of 
 * access.
 * </p><p>
 * The <code>Boxscore</code> interface serves another important
 * purpose: only instances of classes implementing it may access
 * data stored in in <code>BxScrGameAccount</code>.
 * </p>
 */
public interface BaseballBoxscore {
    
    public void write() throws IOException;

}