/*
 * File: ReadGame.java
 * Description: Class containing all static methods. These methods read, record, and 
 * produce a boxscore for a single Project Scoresheet game. A GAME is defined as beginning
 * with an ID (i.e. NYA201809030) and ending on the line directly preceding the next game
 * ID. A game includes team, weather, lineup, and play-by-play information. Once the game
 * is read through, its contents are printed in a classic newspaper boxscore style to the
 * stream.
 */

package com.github.alexthesuperb.jopenboxscore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.ArrayIndexOutOfBoundsException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;

public class ReadGame {
    private static RandomAccessFile visFileReader, homeFileReader;
    private static Team visitor, home;
    private static String visID, homeID, wpID, lpID, saveID, date, year;
    private static int timeOfGame, attendance, gmNumber;
    private static char daynight;
    // private static boolean firstPlayRead; //debugging

    public static void readGame(BufferedReader reader, BufferedWriter writer,
        RandomAccessFile teamReader, String inYear) throws IOException
    {
        // firstPlayRead = false; //debugging
        wpID = ""; lpID = ""; saveID = "";
        visitor = new Team();
        home = new Team();
        year = inYear;   
        String line;

        Play.reset();

        while((line = reader.readLine()) != null && !line.startsWith("id,")){
            //Mark current position in read file so that next instance can begin
            //at top of next game.
            reader.mark(0);

            if(line.startsWith("info,"))
                setInfo(line.split(","), teamReader);
            else if(line.startsWith("start,"))
                makeRosterMove(true,line.split(","));
            else if(line.startsWith("play,")){
                Play.readPlay(visitor,home,line.split(",")[6]);
            }
            else if(line.startsWith("sub,")){
                makeRosterMove(false,line.split(","));
            }
            else if(line.startsWith("ladj,")){
                Play.adjustBattingSpot(Integer.parseInt(line.split(",")[1]), 
                    Integer.parseInt(line.split(",")[2]));
            }
            else if(line.startsWith("data,"))
                setData(line.split(","));
        }
        //Wrap up game, print boxscore
        Play.endGame(visitor, home);
        visitor.setPitDecisions(wpID, lpID, saveID);
        home.setPitDecisions(wpID, lpID, saveID);
        Boxscore.printBoxscore(writer, visitor, home, date, daynight, gmNumber, 
            timeOfGame, attendance, Play.getOuts());
        return;
    }
    public static void setData(String[] line){
        if(line[1].equals("er")){
            if(visitor.setEarnedRuns(line[2],Integer.parseInt(line[3])))
                return;
            else
                home.setEarnedRuns(line[2],Integer.parseInt(line[3])); 
        }
    }
    private static void setInfo(String[] line, RandomAccessFile teamReader) throws IOException{
        if(line.length < 3)
            return;
        try{
            if (line[1].equals("visteam")){
                visID = line[2];
                try{
                    visFileReader = new RandomAccessFile(new File(visID + year + ".ROS"),"r");
                    createTeam(teamReader, visID, false);
                } catch (FileNotFoundException e){
                    System.out.println("[Couldn't find file " + visID + year + ".ROS.]");
                    System.exit(1);
                } catch (IOException e){
                    System.out.println("[An IOException occured while trying to access TEAM file.]");
                    System.exit(1);
                }
            } else if (line[1].equals("hometeam")){
                homeID = line[2];
                try{
                    homeFileReader = new RandomAccessFile(new File(homeID + year + ".ROS"),"r");
                    createTeam(teamReader, homeID, true);
                } catch (FileNotFoundException e){
                    System.out.println("[Couldn't find file " + homeID + year + ".ROS.]");
                    System.exit(1);
                } catch (IOException e){
                    System.out.println("[An IOException occured while trying to access TEAM file.]");
                    System.exit(1);
                }
            } else if (line[1].equals("date")){
                String[] dateArr = line[2].split("/");
                date = dateArr[1] + "/" + dateArr[2] + "/" + dateArr[0];
            } else if (line[1].equals("daynight")){
                if(line[2].equals("day"))
                    daynight = 'D';
                else if (line[2].equals("night"))
                    daynight = 'N';
                else
                    daynight = '\0';
            } else if(line[1].equals("number")){
                gmNumber = Integer.parseInt(line[2]);
            } else if (line[1].equals("attendance")){
                attendance = Integer.parseInt(line[2]);
            } else if (line[1].equals("timeofgame")){
                timeOfGame = Integer.parseInt(line[2]);
            } else if (line[1].equals("wp")){
                wpID = line[2];
            } else if (line[1].equals("lp")){
                lpID = line[2];
            } else if (line[1].equals("save")){
                saveID = line[2];
            }
        } catch (ArrayIndexOutOfBoundsException e){
            return;
        }
    }
    private static void makeRosterMove(boolean startTF, String[] line){
        try{
            String playerID = line[1];
            String homeAway = line[3];
            String lineupSpot = line[4];
            String position = line[5];
            if(homeAway.equals("0")){
                String[] first_last = getPlayerName(visFileReader,playerID);
                visitor.addPlayer(startTF,playerID,first_last[0],first_last[1],lineupSpot,position);
            } else {
                String[] first_last = getPlayerName(homeFileReader,playerID);
                home.addPlayer(startTF,playerID,first_last[0],first_last[1],lineupSpot,position);
            }
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("[An error occured while initializing " +
            "starting lineups.]");
            System.exit(1);
        }
    }
    private static String[] getPlayerName(RandomAccessFile rosReader, String playerID){
        try{
            String line;
            rosReader.seek(0);
            while((line = rosReader.readLine()) != null){
                if(line.startsWith(playerID)){
                    String[] lineArr = line.split(",");
                    String[] first_last = {lineArr[2],lineArr[1]};
                    return first_last;
                }
            }
        } catch (Exception e){
            System.out.println("[An error occured while scanning ROSTER file for "+
                "player id " + playerID + ".]");
            System.exit(1);
        }
        System.out.println("[Could not find player " + playerID + " in roster file.]");
        System.exit(1);
        return null;
    }
    private static void createTeam(RandomAccessFile teamReader, String teamID, boolean homeTF) 
        throws IOException
    {
        teamReader.seek(0);
        String line;
        while((line = teamReader.readLine()) != null){
            if(line.startsWith(teamID)){
                String[] lineArr = line.split(",");
                if(line.length() < 4)
                    continue;
                if(homeTF){
                    home.setTeamInfo(teamID,lineArr[2],lineArr[3],true);
                    return;
                }
                else{
                    visitor.setTeamInfo(teamID,lineArr[2],lineArr[3],false);
                    return;
                }
            }
        }
        System.out.println("[Could not find team " + teamID + " in associated TEAM file.]");
        System.exit(1);
    }
}