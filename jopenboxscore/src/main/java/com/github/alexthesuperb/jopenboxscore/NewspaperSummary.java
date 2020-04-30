package com.github.alexthesuperb.jopenboxscore;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.io.BufferedWriter;

public class NewspaperSummary implements BaseballBoxscore {

    private BufferedWriter writer;

    public NewspaperSummary(List<BoxscoreGameAccount> games, BufferedWriter writer) {
        this.writer = writer;
        LinkedList<SingleGameTeam> teamAccounts = new LinkedList<>();
        
        for (BoxscoreGameAccount game : games) {
            teamAccounts.add(game.getTeam(false, this));
            teamAccounts.add(game.getTeam(true, this));
        }
    }

    @Override
    public void write() throws IOException {
        //TODO: Print all 
    }
}