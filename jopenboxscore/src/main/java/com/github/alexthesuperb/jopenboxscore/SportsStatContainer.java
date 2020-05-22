package com.github.alexthesuperb.jopenboxscore;

/**
 * Classes implementing this interface can be queried for statistics,
 * either by a single key, or by an array of keys.
 */
public interface SportsStatContainer {
    
    /**
     * Get a single counting stat.
     * 
     * @param key The statistic's key.
     * @return a counting stat
     */
    public int getStat(String key);

    /**
     * Get an array of counting statistics.
     * 
     * @param keys An array of keys.
     * @return an array of counting statistics.
     */
    public int[] getStats(String[] keys);
}