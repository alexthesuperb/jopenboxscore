package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;
import java.util.List;

public class AggregatedTeam<T extends SingleGameTeam> extends 
        BaseballTeam<AggregatedPlayer, AggregatedPlayer> {

    private List<AggregatedPlayer> aggregatedPitchers;
    private List<AggregatedPlayer> aggregatedPositionPlayers;

    public AggregatedTeam(String id, String city, String name) {
        
        super(id, city, name);
        aggregatedPitchers = new LinkedList<>();
        aggregatedPositionPlayers = new LinkedList<>();
    }

    /**
     * Add a single team's game account to this instance.
     * @param singleGameTeam
     */
    public void addGame(T singleGameTeam) {
        if (singleGameTeam.getTeamId().equals(teamId) && (singleGameTeam != null)) {
            for (SingleGamePitcher p : singleGameTeam.getAllPitchers()) {
                if (containsPitcher(p.getPlayerId())) {
                    getPitcher(p.getPlayerId()).addPerformance(p);
                } else {
                    AggregatedPlayer newPitcher =
                            new AggregatedPlayer(p.getPlayerId(),
                                                 p.getFirstName(),
                                                 p.getLastName());
                    newPitcher.addPerformance(p);
                    aggregatedPitchers.add(newPitcher);
                }
                /* 
                * Because a pitcher's fielding statistics are stored in
                * SingleGamePitcher, we must create a position player 
                * object for him as well. 
                */
                if (!containsPositionPlayer(p.getPlayerId())) {
                    AggregatedPlayer newPlayer =
                            new AggregatedPlayer(
                                p.getPlayerId(),
                                p.getFirstName(),
                                p.getLastName());
                    aggregatedPositionPlayers.add(newPlayer);
                }
                getPositionPlayer(p.getPlayerId()).incrementStats(
                        BaseballPlayer.KEY_E,
                        p.getStat(BaseballPlayer.KEY_E)
                );
            }
            for (List<SingleGamePositionPlayer> list : singleGameTeam.getLineup()) {
                for (SingleGamePositionPlayer b : list) {
                    // singleGamePositionPlayers.add(b);
                    if (containsPositionPlayer(b.getPlayerId())) {
                        getPositionPlayer(b.getPlayerId()).addPerformance(b);
                    } else {
                        AggregatedPlayer newPositionPlayer =
                                new AggregatedPlayer(
                                    b.getPlayerId(),
                                    b.getFirstName(),
                                    b.getLastName());
                        newPositionPlayer.addPerformance(b);
                        aggregatedPositionPlayers.add(newPositionPlayer);
                    }
                }
            }
        }
    }

    public void addAllGames(List<T> singleGameTeams) {
        for (T team : singleGameTeams) {
            addGame(team);
        }
    }

    @Override
    public boolean containsPositionPlayer(String playerId) {
        for (AggregatedPlayer player : 
                aggregatedPositionPlayers) {
            if (player.getPlayerId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsPitcher(String playerId) {
        for (AggregatedPlayer pitcher :
                aggregatedPitchers) {
            if (pitcher.getPlayerId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AggregatedPlayer getPitcher(String playerId) {
        for (AggregatedPlayer pitcher :
                aggregatedPitchers) {
            if (pitcher.getPlayerId().equals(playerId)) {
                return pitcher;
            }
        }
        return null;
    }

    @Override
    public List<AggregatedPlayer> getAllPitchers() {
        return aggregatedPitchers;
    }

    @Override
    public AggregatedPlayer getPositionPlayer(String playerId) {
        for (AggregatedPlayer player : aggregatedPositionPlayers) {
            if (player.getPlayerId().equals(playerId)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public List<AggregatedPlayer> getAllPositionPlayers() {
        return aggregatedPositionPlayers;
    }

    @Override
    public int getStat(String key) {
        int stat = 0;
        for (AggregatedPlayer player : aggregatedPositionPlayers) {
            stat += player.getStat(key);
        }

        /* Do not double-count errors committed or games played. */
        if (!key.equals(BaseballPlayer.KEY_E) && !key.equals(BaseballPlayer.KEY_G)) {
            for (AggregatedPlayer pitcher : aggregatedPitchers) {
                stat += pitcher.getStat(key);
            }
        }
        return stat;
    }

    @Override
    public int[] getStats(String[] keys) {
        int[] stats = new int[keys.length];
        
        for (int i = 0; i < stats.length; i++) {
            stats[i] = getStat(keys[i]);
        }
        return stats;
    }

}