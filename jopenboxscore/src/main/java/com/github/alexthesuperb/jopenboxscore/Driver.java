/* C-Style comment with Author & Copyright info... */

package com.github.alexthesuperb.jopenboxscore;

import java.io.BufferedWriter;
import java.io.File;
// import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

public class Driver {

    /* Flags */
    static boolean readInFile;
    static boolean readYear;
    static boolean readStartDate;
    static boolean readEndDate;
    static boolean readOutFile;
    static boolean readGameID;
    static boolean readInRosDir;

    /** If <code>false</code>, output to terminal. */
    static boolean hasOutFile;

    /* From String[] args */
    static String year;
    static String startDate;
    static String endDate;
    static String outFileName;
    static LinkedList<String> inFileNames;
    static LinkedList<String> gameIDs;

    /* Query flags */
    static final int QUERY_ALL_GAMES = 1;
    static final int QUERY_BY_ID = 2;
    static final int QUERY_BY_DATES = 3;
    static final int QUERY_ASK_USER = 4; 

    /* Query chosen by user */
    static int queryType;

    /* Writes output */
    static BufferedWriter outWriter;

    /** Directory containing TEAM and roster files. */
    static File rosDir;

    /** Reset flags. */
    static void resetFlags() {
        readInFile = false;
        readYear = false;
        readStartDate = false;
        readEndDate = false;
        readOutFile = false;
        readGameID = false;
        readInRosDir = false;
    }

    /**
     * Javadoc comment...
     */
    public static void main(String[] args) {
        queryType = QUERY_ALL_GAMES;
        hasOutFile = false;
        year = "";
        inFileNames = new LinkedList<>();
        gameIDs = new LinkedList<>();

        /* If a path is not specified, default to current directory. */
        rosDir = new File(".");
        
        int argc = args.length;
        int i = 0;

        resetFlags();

        try {
            while (i < argc) {

                /* Check for flags */
                if (args[i].equals("-h") || args[i].equals("-help")) {
                    printHelp();
                    System.exit(0);
                } else if (args[i].equals("-y") || args[i].equals("-year")) {
                    resetFlags();
                    readYear = true;  
                } else if (args[i].equals("-p") || args[i].equals("-path")) {
                    resetFlags();
                    readInRosDir = true;
                }  else if (args[i].equals("-i") || args[i].equals("-id")) {
                    /* 
                     * If queryType is not the default, user has already entered
                     * another flag. Conflicting flags throws an exception. 
                     */
                    if (queryType != QUERY_ALL_GAMES) {
                        throw new IllegalArgumentException();
                    }
                    resetFlags();
                    readGameID = true;
                    queryType = QUERY_BY_ID;
                } else if (args[i].equals("-s") || args[i].equals("-start")) {

                    /* Query may only contain 1 start date. */
                    if (startDate != null) {
                        throw new IllegalArgumentException();
                    }
                    resetFlags();
                    readStartDate = true;
                    queryType = QUERY_BY_DATES;
                } else if (args[i].equals("-e") || args[i].equals("-end")) {

                    /* Query may only contain 1 end date. */
                    if (endDate != null) {
                        throw new IllegalArgumentException();
                    }
                    resetFlags();
                    readEndDate = true;
                    queryType = QUERY_BY_DATES;
                } else if (args[i].equals("-d") || args[i].equals("-dest")) {
                    hasOutFile = true;
                    resetFlags();
                    readOutFile = true;
                } else if (args[i].equals("-q")) {

                    /* Conflicting flags, throw exception. */
                    if (queryType != QUERY_ALL_GAMES) {
                        throw new IllegalArgumentException();
                    }
                    resetFlags();
                    queryType = QUERY_ASK_USER;
                } else {
                    if(readYear) {
                        year = args[i];
                        readYear = false;
                    } else if (readInRosDir) {
                        rosDir = new File(args[i]);
                        readInRosDir = false;
                    } else if (readStartDate) {
                        startDate = args[i];
                        readStartDate = false;
                    } else if (readEndDate) {
                        endDate = args[i];
                        readEndDate = false;
                    } else if (readOutFile) {
                        outFileName = args[i];
                        readOutFile = false;
                    } else {
                        if (readGameID) {
                            /* 
                            * If args[i] contains a period, it is a 
                            * file name, and the inputed list of game IDs
                            * has ended. 
                            */
                            if (args[i].contains(".")) { 
                                readGameID = false;
                                inFileNames.add(args[i]);
                            } else {
                                gameIDs.add(args[i]);
                            }
                        } else {
                            
                            /* File names */
                            inFileNames.add(args[i]);
                        }
                    }
                }
                i++;
            }
        } catch (IllegalArgumentException iae) {
            System.out.println("[Query could not be understand as entered. Type " +
                "-h or -help for help.]\n");
            System.exit(1);
        }

        /* Check that user has entered at least one file name. */
        if (inFileNames.isEmpty()) {
            System.out.println("\n[You must enter the names of 1 or more files. " + 
                "Type -h or -help for help.]\n");
            System.exit(1);
        }

        /* Check for missing date. */
        if (queryType == QUERY_BY_DATES) {
            if (startDate == null) {
                startDate = "0000";   
            }
            if (endDate == null) {
                endDate = "9999";
            }
        }

        /* Initialize BufferedWriter for output. */
        try {
            if (hasOutFile) {
                outWriter = new BufferedWriter(new FileWriter(outFileName));
            } else {
                throw new IOException();
            }
        } catch (IOException ioe) {
            outWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        /* Read files */
        String currFile = inFileNames.getFirst();
        try {
            for (String s : inFileNames) {
                currFile = s;
                BxScrFileReader boxReader = new BxScrFileReader(s, year, rosDir);

                if (queryType == QUERY_ALL_GAMES) {
                    boxReader.readAll();
                } else if (queryType == QUERY_BY_DATES) { 
                    boxReader.readGamesWithinRange(startDate, endDate);
                } else if (queryType == QUERY_BY_ID) {
                    boxReader.readGamesByID(gameIDs);
                } else if (queryType == QUERY_ASK_USER) {
                    
                    /* implement here... */
                    System.out.println("\n[Whoops! Not yet supported.]\n");
                    System.exit(0);
                }

                /* Print boxscores of games read from this file. */
                for (BxScrGameAccount game: boxReader.getGameAccounts()) {
                    game.printBoxscore(outWriter);
                }
            }
        } catch (Exception e) {

            /* Exception thrown by BxScrFileReader */
            System.out.println("\nAn error has occured in file while processing " +
                "file " + currFile + ". Cause: \n");
            e.printStackTrace();
            System.exit(0);
        }

        /* Finish program. */
        System.out.println("[Program terminated successfully.]");
    }

    /** Print a help message to the terminal. */
    static void printHelp() {
        System.out.println("\n\nJBoxscore is an open-source alternative to " +
            "Retrosheet.org's BOX.EXE boxscore-generating software. It converts " +
            "Retrosheet play-by-play files (ending in .EVE, .EVA, or .EVN) into " +
            "classic newspaper boxscores. Arguments must end with a series of Event " +
            "files to read. " +
            "By default, the entirety of the requested files will be read. However, " +
            "the breadth of that search may be manipulated with the following flags.\n");

        System.out.println(String.format("%10s%10s%10s", "-h", "-help", "") + 
            "Print this help to terminal.");
        System.out.println(String.format("%10s%10s%10s", "-y", "-year", "") + 
            "Set the year of inquiry.");
        System.out.println(String.format("%10s%10s%10s", "-i", "-id", "") + 
            "Read only games specified by their IDs. This flag must be followed by " +
            "one or more 12-character Retrosheet game IDs.");
        System.out.println(String.format("%10s%10s%10s", "-s", "-start", "") + 
            "Specify the date of the earliest game to read. This flag must be " +
            "followed by a 4-digit (MMDD) date.");
        System.out.println(String.format("%10s%10s%10s", "-e", "-end", "") + 
            "Specify the date of the last game to read. This flag must be followed by " +
            "a 4-digit (MMDD) date.");
        System.out.println(String.format("%10s%10s%10s", "-d", "-dest", "") + 
            "Specify output file. By default, output prints to console.");
        System.out.println(String.format("%10s%10s%10s", "-p", "-path", "") + 
            "Specify the directory containg TEAM and .ROS files. By default, " +
            "this is assumed to be the directory from which this program is executed.");
    }

    /* Print query in plain English to terminal. */
    static void printQuery() {
        System.out.println("Year: " + year);
        System.out.print("Query Type: ");
        
        if(queryType == QUERY_ALL_GAMES) {
            System.out.println("All Games");
        } else if (queryType == QUERY_ASK_USER) {
            System.out.println("Ask User");
        } else if (queryType == QUERY_BY_DATES) { 
            System.out.println("Within Date Range");
            System.out.println("From " + startDate + " to " + endDate);
        } else if (queryType == QUERY_BY_ID) {
            System.out.println("Games By ID");
            System.out.print("IDs: ");
            for (int i = 0; i < gameIDs.size() - 1; i++) {
                System.out.print(gameIDs.get(i) + ", ");
            }
            System.out.println(gameIDs.getLast());
        }

        System.out.print("Files: ");
        for (int i = 0; i < inFileNames.size() - 1; i++) {
            System.out.println(inFileNames.get(i) + ", ");
        }
        System.out.println(inFileNames.getLast());
    }
}