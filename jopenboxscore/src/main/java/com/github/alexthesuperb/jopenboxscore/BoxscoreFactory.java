package com.github.alexthesuperb.jopenboxscore;

import java.io.BufferedWriter;

public class BoxscoreFactory {

    public static String NEWSPAPER_BOXSCORE_KEY = "NewspaperBoxscore";

    private static BoxscoreFactory boxscoreFactory;

    private BoxscoreFactory() {
        super();
    }

    public static BoxscoreFactory getInstance() {
        
        if (boxscoreFactory == null) {
            boxscoreFactory = new BoxscoreFactory();
        }

        return boxscoreFactory;
    }

    public BaseballBoxscore getBoxscore(BoxscoreGameAccount game, BufferedWriter writer, 
            String type) {
        if (type.equalsIgnoreCase(NEWSPAPER_BOXSCORE_KEY)) {
            return new NewspaperBoxscore(game, writer);
        } else {
            return null;
        }
    }

}