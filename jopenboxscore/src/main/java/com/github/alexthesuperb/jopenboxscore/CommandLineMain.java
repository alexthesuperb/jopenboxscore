/*
 * C-style comment with author & copyright information
 */

package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Program <code>MainClass</code>. Parse command line arguments for
 * bounds of file search. If arguments do not conform to program's
 * guidelines, a message will be printed to the terminal and the program
 * will be terminated. 
 */
class CommandLineMain {

    /** Flag signalling to read all games in requested files. */
    public static final int QUERY_ALL_GAMES = 1;

    /** Flag signalling to read all games matching provided IDs. */
    public static final int QUERY_BY_ID = 2;

    /** 
     * Flag signalling program to ask user about each game 
     * before processing. 
     */
    public static final int QUERY_ASK_USER = 3;

    /** 
     * Flag signalling program to process all games between provided
     * dates.
     */
    public static final int QUERY_DATE_RANGE = 4;

    /** Unique game IDs entered by user to process */
    private static LinkedList<String> gameIDs;

    /** Unique list of file names entered by user. */
    private static LinkedList<String> readFileNames;

    /** List of <code>File</code> objects to read. */
    private static LinkedList<File> readFiles;

    /** Name of file output is to be written to. */
    private static String writeFileName;

    /** Output writer object. */
    private static BufferedWriter outWriter;

    /** 
     * Object used to read team name information from 
     * <code>"TEAM"</code> file.
     */
    private static RandomAccessFile teamReader;

    /** Flag signifying user query type. */
    private static int query;

    /** User-inputed year. */
    private static String year;

    /** If applicable, user-inputed query start date. */
    private static String startDate;

    /** If applicable, user-inputed query end date. */
    private static String endDate;
    
    /**
     * Print a method explaining the program's various useage flags to
     * terminal.
     */
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

    /**
     * Parse command line arguments and check that they form a valid
     * query. If they do, process files provided under specified guidelines.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
        
        gameIDs = new LinkedList<String>();
        readFileNames = new LinkedList<String>();
        readFiles = new LinkedList<File>();
        writeFileName = "";
        year = "";
        startDate = "";
        endDate = "";
        
        int argc = args.length;

        try{

            /* If user inputs -h, print help message and exit program. */
            if(args[0].equals("-h")){
                printHelp();
                System.exit(0);
            }

            /* If user does not specify year, exit system. */
            if(!args[0].equals("-y")){
                System.out.println("Please begin argument by specifying a year." + 
                "Enter -h for help.");
                System.exit(1);
            }

            year = args[1];

            /* Check for TEAM file */
            File tmp = new File("TEAM" + year);
            try{
                teamReader = new RandomAccessFile(tmp,"r");
            } catch (FileNotFoundException e){
                System.out.println("[Can't find file TEAM" + year +".]");
                System.exit(1);
            }

            /* Case 1: No other modifiers, print all games in file */
            if(args[2].endsWith(".EVE") || args[2].endsWith(".EVA") 
                || args[2].endsWith(".EVN"))
            {
                query = QUERY_ALL_GAMES;
                getFileNames(args, 2, argc);
            } 
            /* Case 2: Search for games by ID */
            else if(args[2].equals("-i")){
                query = QUERY_BY_ID;
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
            /* Case 3: Ask user about each game */
            else if(args[2].equals("-q")){
                query = QUERY_ASK_USER;
                getFileNames(args, 3, argc);
            } 
            /* Case 4: Search for games between two dates */
            else if(args[2].equals("-s")){
                query = QUERY_DATE_RANGE;
                /* Check if next argument is a valid MMDD date. */
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
            /* Default: Print error message and exit */
            else {
                printError(-1);
                System.exit(1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            printError(-1);
            System.exit(1);
        }
        /* End input validation block */
        
        /* For each file name provided, create a File object */
        for(String s : readFileNames){
            readFiles.add(new File(s));
        }

        /* Initialize output stream */
        try{
            if(!writeFileName.equals("")){
                outWriter = new BufferedWriter(new FileWriter(writeFileName));
            } else {
                outWriter = new BufferedWriter(new OutputStreamWriter(System.out));
            }
        } catch (IOException e){
            outWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        /* For each file, read based on query requested */
        for(File f : readFiles){
            switch(query){
                case QUERY_ALL_GAMES:
                    ReadRetrosheetFile.readPBPFile(f, outWriter, teamReader, year);
                    break;
                case QUERY_BY_ID:
                    ReadRetrosheetFile.readPBPFile(f, outWriter, teamReader, year, gameIDs);
                    break; 
                case QUERY_DATE_RANGE:
                    ReadRetrosheetFile.readPBPFile(f, outWriter, teamReader, year,
                        startDate,endDate);
                    break;
                case QUERY_ASK_USER:
                    ReadRetrosheetFile.askAboutPBPFile(f, outWriter, teamReader, year);
                    break;
                default:
                    printError(-1);
                    System.exit(1);
            }
        }
        System.out.println("[File(s) processed. Program terminated successfully.]");
    }

    /**
     * Print an error message to the terminal. 
     * 
     * @param type The type of error. As currently written, this parameter
     * goes unused by the method.  
     */
    public static void printError(int type){
        System.out.println("Argument could not be understood as entered.");
        System.out.println("Enter -h for help.");
    }

    /**
     * 
     * @param args
     * @param i_start
     * @param argc
     */
    static void getFileNames(String[] args, int i_start, int argc){

        /* After modifiers, all other arguments should be file names. */
        for(int i = i_start; i < argc; i++){
            /* 
             * Argument following -p modifier is output file
             * Any arguments after that output file are ignored 
             */
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
