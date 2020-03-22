package com.github.alexthesuperb.jopenboxscore;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

class Main {
    public static List<String> gameIDs = new LinkedList<String>();
    public static List<String> readFileNames = new LinkedList<String>();
    public static List<File> readFiles = new LinkedList<File>();
    public static String writeFileName = "";
    public static BufferedWriter outWriter;
    public static RandomAccessFile teamReader;
    public static QueryType query;
    public static String year = "";
    public static String startDate = "";
    public static String endDate = "";
    
    public static void printHelp(){
        System.out.println("\nJBOX is an open-source recreation of Retrosheet.org's " +
            "BOX.EXE software written by Alex Wimer. All queries must begin by specifying " +
            "a year. Optionally, the scope of a query may be limited with additional flags.\n");
        System.out.println("Flags:");
        System.out.println("-y\tyear");
        System.out.println("-s\tstart date");
        System.out.println("-e\tend date");
        System.out.println("-i\tgame by id");
        System.out.println("-q\task about each game");
        System.out.println("-p\twrite results to file\n");

        System.out.println("Open-source. Written by Alex C. Wimer, 3/2/2020.\n");
    }
    
    public static void main(String[] args) throws IOException{
        int argc = args.length;
        try{
            //If user inputs -h, print help message and exit program.
            if(args[0].equals("-h")){
                printHelp();
                System.exit(0);
            }
            //If user does not specify year, exit system.
            if(!args[0].equals("-y")){
                System.out.println("Please begin argument by specifying a year." + 
                "Enter -h for help.");
                System.exit(1);
            }
            year = args[1];

            //Check for TEAM file
            File tmp = new File("TEAM" + year);
            try{
                teamReader = new RandomAccessFile(tmp,"r");
            } catch (FileNotFoundException e){
                System.out.println("[Can't find file TEAM" + year +".]");
                System.exit(1);
            }

            //Case 1: No other modifiers, print all games in file
            if(args[2].endsWith(".EVE") || args[2].endsWith(".EVA") 
                || args[2].endsWith(".EVN"))
            {
                query = QueryType.ALL_GAMES;
                getFileNames(args, 2, argc);
            } 
            //Case 2: Search for games by ID
            else if(args[2].equals("-i")){
                query = QueryType.BY_ID;
                for(int i = 3; i < argc; i++){
                    if(args[i].endsWith(".EVE") || args[i].endsWith(".EVA") 
                        || args[i].endsWith(".EVN"))
                    {
                        getFileNames(args, i, argc);
                        break;
                    } else {
                        gameIDs.add(args[i]); 
                    }
                }
            } 
            //Case 3: Ask user about each game
            else if(args[2].equals("-q")){
                query = QueryType.ASK_USER;
                getFileNames(args, 3, argc);
            } 
            //Case 4: Search for games between two dates
            else if(args[2].equals("-s")){
                query = QueryType.DATE_RANGE;
                //Check if next argument is a valid MMDD date.
                if(args[3].length() == 4){
                    startDate = args[3];
                    if(args[4].equals("-e")){
                        if(args[5].length() == 4){
                            endDate = args[5];
                            getFileNames(args,6,argc);
                        } else {
                            printError(-1);
                            System.exit(1);
                        }
                    } else {
                        printError(-1);
                        System.exit(1);
                    }
                } else {
                    printError(-1);
                    System.exit(1);
                }
            }
            //Default: Print error message and exit
            else {
                printError(-1);
                System.exit(1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            printError(-1);
            System.exit(1);
        }
        
        //For each file name provided, create a File object
        for(String s : readFileNames){
            readFiles.add(new File(s));
        }

        //Initialize output stream
        try{
            if(!writeFileName.equals("")){
                outWriter = new BufferedWriter(new FileWriter(writeFileName));
            } else {
                outWriter = new BufferedWriter(new OutputStreamWriter(System.out));
            }
        } catch (IOException e){
            outWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        //For each file, read based on query requested
        for(File f : readFiles){
            switch(query){
                case ALL_GAMES:
                    ReadRetrosheetFile.readPBPFile(f, outWriter, teamReader, year);
                    break;
                case BY_ID:
                    ReadRetrosheetFile.readPBPFile(f, outWriter, teamReader, year, gameIDs);
                    break; 
                case DATE_RANGE:
                    ReadRetrosheetFile.readPBPFile(f, outWriter, teamReader, year,
                        startDate,endDate);
                    break;
                case ASK_USER:
                    ReadRetrosheetFile.askAboutPBPFile(f, outWriter, teamReader, year);
                    break;
                default:
                    printError(-1);
                    System.exit(1);
            }
        }
        System.out.println("[File(s) processed. Program terminated successfully.]");
    }
    public static void printError(int type){
        System.out.println("Argument could not be understood as entered.");
        System.out.println("Enter -h for help.");
    }
    static void getFileNames(String[] args, int i_start, int argc){
        //After modifiers, all other arguments should be file names.
        for(int i = i_start; i < argc; i++){
            //Argument following -p modifier is output file
            //Any arguments after that output file are ignored
            if(args[i].equals("-p")){
                if(i+1 <= argc-1)
                    writeFileName = args[i+1]; 
                break;
            } else {
                readFileNames.add(args[i]);
            }
        }
    }
}
