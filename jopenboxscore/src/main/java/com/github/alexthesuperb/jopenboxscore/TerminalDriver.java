/* C-Style comment with Author & Copyright info... */

package com.github.alexthesuperb.jopenboxscore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;

public class TerminalDriver {

    /* Flags */
    static boolean readInFile;
    static boolean readYear;
    static boolean readStartDate;
    static boolean readEndDate;
    static boolean readOutFile;
    static boolean readGameID;
    static boolean readInRosDir;
    static boolean readSummaryFile;

    /** If <code>true</code>, print boxscores in ascending order by date.*/
    static boolean writeInOrder;

    /** If <code>false</code>, output to terminal. */
    static boolean hasOutFile;

    /** If <code>true</code>, write summary. */
    static boolean hasSummaryFile;

    /* From String[] args */
    static String year;
    static String startDate;
    static String endDate;
    static String outFileName;
    static String summaryFileName;
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

    static BufferedWriter summaryWriter;

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
        readSummaryFile = false;
    }

    /**
     * Javadoc comment...
     */
    public static void main(String[] args) {
        queryType = QUERY_ALL_GAMES;
        writeInOrder = false;
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
                if (args[i].equalsIgnoreCase("-h") || 
                        args[i].equalsIgnoreCase("-help")) {
                    printHelp();
                    System.exit(0);
                } else if (args[i].equalsIgnoreCase("-y") ||
                        args[i].equalsIgnoreCase("-year")) {
                    resetFlags();
                    readYear = true;
                } else if (args[i].equalsIgnoreCase("-o") ||
                        args[i].equalsIgnoreCase("-order")) {
                    resetFlags();
                    writeInOrder = true;
                } else if (args[i].equalsIgnoreCase("-p") ||
                        args[i].equalsIgnoreCase("-path")) {
                    resetFlags();
                    readInRosDir = true;
                }  else if (args[i].equalsIgnoreCase("-i") ||
                        args[i].equalsIgnoreCase("-id")) {
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
                } else if (args[i].equalsIgnoreCase("-s") ||
                        args[i].equalsIgnoreCase("-start")) {

                    /* Query may only contain 1 start date. */
                    if (startDate != null) {
                        throw new IllegalArgumentException();
                    }
                    resetFlags();
                    readStartDate = true;
                    queryType = QUERY_BY_DATES;
                } else if (args[i].equalsIgnoreCase("-e") ||
                        args[i].equalsIgnoreCase("-end")) {

                    /* Query may only contain 1 end date. */
                    if (endDate != null) {
                        throw new IllegalArgumentException();
                    }
                    resetFlags();
                    readEndDate = true;
                    queryType = QUERY_BY_DATES;
                } else if (args[i].equalsIgnoreCase("-d") || 
                        args[i].equalsIgnoreCase("-dest")) {
                    hasOutFile = true;
                    resetFlags();
                    readOutFile = true;
                } else if (args[i].equalsIgnoreCase("-summary")) {
                    hasSummaryFile = true;
                    resetFlags();
                    readSummaryFile = true;
                } else if (args[i].equalsIgnoreCase("-q")) {

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
                    } else if (readSummaryFile) {
                        summaryFileName = args[i];
                        readSummaryFile = false;
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

        /* 
         * Initialized BufferedWriter for Summary. If user has entered 
         * 'CONSOLE' as the file name, then write summary to console. 
         */
        try {
            if (hasSummaryFile) {
                if (summaryFileName.equalsIgnoreCase("CONSOLE")) {
                    throw new IOException();
                }
                summaryWriter = new BufferedWriter(new FileWriter(summaryFileName));
            }
        } catch (IOException ioe) {
            summaryWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        /* Read files */
        String currFile = inFileNames.getFirst();
        LinkedList<BoxscoreGameAccount> games = new LinkedList<>();
        try {
            for (String s : inFileNames) {
                currFile = s;
                RetrosheetEveReader boxReader = new RetrosheetEveReader(s, year, rosDir);

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

                // /* Print boxscores of games read from this file. */
                // for (BxScrGameAccount game: boxReader.getGameAccounts()) {
                //     game.printBoxscore(outWriter);
                // }

                /* Add game accounts produced by file to master list. */
                for (BoxscoreGameAccount game : boxReader.getGameAccounts()) {
                    games.add(game);
                }

            }
        } catch (Exception e) {

            /* Exception thrown by BxScrFileReader */
            System.out.println("\nAn error has occured in file while processing " +
                "file " + currFile + ". Cause: \n");
            e.printStackTrace();
            System.exit(0);
        }

        /* 
         * Sort games so that they can be printed in ascending order, regardless 
         * of the file from which they originated. 
         */
        if (writeInOrder) {
            Collections.sort(games);
        }
        try {
            for (BoxscoreGameAccount g : games) {
                g.printBoxscore(outWriter, BoxscoreFactory.NEWSPAPER_BOXSCORE_KEY);
            }
        } catch (IOException e) {
            System.out.println("\nAn error occured while printing boxscores.\n");
            e.printStackTrace();
            System.exit(0);
        }

        if (hasSummaryFile) {
            NewspaperSummary summary = new NewspaperSummary(summaryWriter);
            summary.addGames(games);
            try {
                summary.write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Finish program. */
        System.out.println("[Program terminated successfully.]");
    }

    /** Print a help message to the terminal. */
    static void printHelp() {

        System.out.println(
            "\njopenboxscore is an open-source alternative to Retrosheet.org's\n" +
            "BOX.EXE boxscore-generating software. It converts Retrosheet\n" +
            "play-by-play files (written in the Project Scoresheet format and\n" +
            "ending with file extension .EVA, .EVE, or .EVN) into classic\n" +
            "newspaper-style boxscores.\n"
        );

        System.out.println(
            "Usage: jopenboxscore [options] [files...]\n"
        );

        System.out.println(
            " where options include:\n" +
            "    -y <year>\n" +
            "    -year <year>\n" +
            "                  The year included in the names of .ROS and TEAM\n" +
            "                  files necessary to process the requested files.\n" +
            "    -i [game IDs...]\n" +
            "    -id [game IDs...]\n" +
            "                  A whitespace-delimitted list of specific game IDs\n" +
            "                  to process within the requested files.\n"+
            "    -s <start date>\n" +
            "    -start <start date>\n" +
            "                  The start date (MMDD) of a range through which games\n" +
            "                  should be processed. If no end date is specified, games\n" +
            "                  will be processed from this start date to the end of each.\n" +
            "                  file.\n" +
            "    -e <end date>\n" +
            "    -end <end date>\n" +
            "                  The end date (MMDD) of a range through which games should\n" +
            "                  be processed. If no start date is specified, games will be\n" +
            "                  processed from the start of each file through this end date.\n" +
            "    -d <file name>\n" +
            "    -dest <file name>\n" +
            "                  Specify a file to which results will be printed. By default,\n" +
            "                  results are printed to this terminal.\n" +
            "    -summary <file name>\n" +
            "                  Specify a file location to which an overall statistical summary of\n" +
            "                  the processed files will be written. If 'CONSOLE' is entered as the\n" +
            "                  argument, this summary will be printed to the terminal.\n" +
            "    -p <directory>\n" +
            "    -path <directory>\n" +
            "                  Specify a directory containing the necessary .ROS and TEAM\n" +
            "                  files. By default, this is assumed to be the directory from\n" +
            "                  which this program is executed.\n" +
            "    -o -order     Print boxscores ordered by date. By default, results are\n" +
            "                  printed in the order in which they were read.\n" + 
            "    -h -help      Print this message.\n"
        );
    }

    /* Print query in plain English to terminal. (DEBUGGING) */
    static private void printQuery() {
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