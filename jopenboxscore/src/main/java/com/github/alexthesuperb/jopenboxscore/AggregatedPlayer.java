package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;
import java.util.List;

/**
 * Aggregates a collection of smaller-sized player performances. By extending
 * <code>BaseballPlayer</code>, the statistics stored in this class may be accessed
 * in the same way.
 * 
 * @param <T> A class extending <code>BaseballPlayer</code>. To aggregate a collection
 * of single-game performances, <code>T</code> should be 
 * <code>SingleGamePositionPlayer</code> or <code>SingleGamePitcher</code>.
 */
public class AggregatedPlayer extends BaseballPlayer {
    
    private List<BaseballPlayer> performances;

    /**
     * Construct a new <code>AggregatedPlayer</code>. This method calls superclass
     * <code>BaseballPlayer</code>'s constructor.
     * @param playerID Player's unique Retrosheet ID.
     * @param firstName Player's first name.
     * @param lastName Player's last name.
     */
    public AggregatedPlayer(String playerID, String firstName, String lastName) {
        super(playerID, firstName, lastName);
        performances = new LinkedList<>();
    }

    /**
     * Add an instance of <code>T</code> to this instance. Upon the addition of
     * this performance, this instance's statistical totals are incremented.
     * @param performance The sample to be added to this instance.
     */
    public void addPerformance(BaseballPlayer performance) {
        if (performance.playerId.equals(this.playerId) && (performance != null)) {
            addToTotals(performance);
            this.performances.add(performance);
        }
    }

    /**
     * Add a <code>List</code> of samples to this instance.
     * See <code>addPerformance(T performance)</code>.
     * @param performances a <code>List</code> of performances <code>T</code>.
     */
    public void addAllPerformances(List<BaseballPlayer> performances) {
        for (BaseballPlayer perf : performances) {
            this.addPerformance(perf);
        }
    }

    /**
     * Add statistics stored in <code>performance</code> to
     * <code>AggregatedPlayer</code>'s statistics.
     * @param performance
     */
    private void addToTotals(BaseballPlayer performance) {
        for (String k : performance.stats.keySet()) {
            if (this.stats.containsKey(k)) {
                int oldVal = this.stats.get(k);
                int newVal = performance.stats.get(k);
                this.stats.put(k, oldVal + newVal);
            } else {
                this.stats.put(k, performance.stats.get(k));
            }
        }
    }
}