package com.github.alexthesuperb.jopenboxscore;

import java.util.List;

public abstract class BaseballTeam<B extends BaseballPlayer, P extends BaseballPlayer> 
        implements SportsStatContainer {

    public static final String KEY_TEAM_WIN  = "win";
    public static final String KEY_TEAM_LOSS = "loss";
    public static final String KEY_TEAM_TIE  = "tie";

    protected String teamId;
    protected String city;
    protected String name;

    public BaseballTeam(String teamId, String city, String name) {
        this.teamId = teamId;
        this.city = city;
        this.name = name;
    }

    public abstract P getPitcher(String playerId);

    public abstract B getPositionPlayer(String playerId);

    public abstract List<P> getAllPitchers();

    public abstract List<B> getAllPositionPlayers();

    /**
     * @param playerId identifies the position player caller is looking for.
     * @return <code>true</code> if this object contains a player matching
     * <code>playerId</code>.
     */
    public abstract boolean containsPositionPlayer(String playerId);

    /**
     * @param playerId identifies the pitcher caller is looking for.
     * @return <code>true</code> if this object contains a player matching
     * <code>playerId</code>.
     */
    public abstract boolean containsPitcher(String playerId);

     /** @return team's ID. */
    public String getTeamId() {
        return teamId;
    }

    /** @return team's city. */
    public String getCity() {
        return city;
    }

    /** @return team's nickname. */
    public String getName() {
        return name;
    }
}