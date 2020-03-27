/*
 * C-style comment with author/copyright information...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.io.BufferedWriter;
import java.io.FileReader;
// import validate.QueryType;


public class ReadRetrosheetFile {
    private static BufferedReader reader;

    //All games
    public static void readPBPFile(File f, BufferedWriter outWriter,
        RandomAccessFile teamReader, String year)
    {
        if(!setBufferedReader(f))
            return;
        try{
            String line;
            while((line = reader.readLine()) != null){
                if(line.startsWith("id,")){
                    ReadGame.readGame(reader, outWriter, teamReader, year);
                    reader.reset();
                }
            }
        } catch (IOException e) {
            return;
        }
    }
    //Games by ID
    public static void readPBPFile(File f, BufferedWriter outWriter,
        RandomAccessFile teamReader, String year, List<String>gameIDs)
    {
        if(!setBufferedReader(f))
            return;
        try{
            String line;
            while((line = reader.readLine()) != null){
                if(line.startsWith("id,")){
                    String id = line.substring(3);
                    if(gameIDs.contains(id)){
                        gameIDs.remove(id);
                        ReadGame.readGame(reader, outWriter, teamReader, year);
                        reader.reset();
                    }
                }
            }
        } catch (IOException e) {
            return;
        }
    }
    //Games within dates
    public static void readPBPFile(File f, BufferedWriter outWriter, 
        RandomAccessFile teamReader, String year, String start, String end)
    {
        if(!setBufferedReader(f))
            return;
        try{
            String line;
            while((line = reader.readLine()) != null){
                if(line.startsWith("id,")){
                    String id = line.substring(3);
                    int date = Integer.parseInt(id.substring(7,11)); //NYA201805040
                    if(date >= Integer.parseInt(start) && date <= Integer.parseInt(end)){
                        ReadGame.readGame(reader,outWriter,teamReader,year);
                        reader.reset();
                    }
                }
            }
        } catch (IOException e) {
            return;
        }   
    }
    //Ask user about each game
    public static void askAboutPBPFile(File f, BufferedWriter outWriter, 
        RandomAccessFile teamReader, String year)
    {
        if(!setBufferedReader(f))
            return;
        try{
            Scanner scan = new Scanner(System.in); 
            String line;
            String home = "", vis = "";
            while((line = reader.readLine()) != null){
                if(line.startsWith("version,"))
                    reader.mark(0);
                if(line.startsWith("info,visteam,"))
                    vis = line.split(",")[2];
                if(line.startsWith("info,hometeam,"))
                    home = line.split(",")[2];
                if(!vis.equals("") && !home.equals("")){
                    System.out.println(vis + " at " + home + "? (ynq)");
                    vis = "";
                    home = "";
                    switch(scan.nextLine().charAt(0)){
                        case 'y':
                        case 'Y':
                            reader.reset();
                            ReadGame.readGame(reader,outWriter,teamReader,year);
                            break;
                        case 'n':
                        case 'N':
                            break;
                        default:
                            System.out.println("[Program terminated.]");
                            System.exit(1);
                    }
                }
                
            }
            scan.close();
        } catch (IOException e){
            return;
        }
    }
    private static boolean setBufferedReader(File f){
        try{
            reader = new BufferedReader(new FileReader(f));
            return true;
        } catch (FileNotFoundException e){
            System.out.println("[Couldn't find file " + f.toString() + ".]");
            return false;
        }
    }

}