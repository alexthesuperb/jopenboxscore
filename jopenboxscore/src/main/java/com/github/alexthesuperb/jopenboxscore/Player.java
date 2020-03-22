package com.github.alexthesuperb.jopenboxscore;

public abstract class Player {
    private String id;
    private String first_name;
    private String last_name;

    public Player(String id, String first, String last){
        this.id = id;
        first_name = first;
        last_name = last;
    }
    public String getBoxscoreName(){
        return last_name + " " + first_name.charAt(0);
    }
    public String getID(){
        return id;
    }
}