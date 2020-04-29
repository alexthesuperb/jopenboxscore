package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * <code>NewspaperBoxscore</code> is an implementation of <code>Boxscore</code>
 * used to write a classic, human-readable newspaper-style boxscore to a <code>
 * BufferedWriter</code>.
 */
public class NewspaperBoxscore implements Boxscore {

    private BufferedWriter writer;
    private Team visitor;
    private Team home;
    private String date;
    private char dayNight;
    private int gameNumber;
    private int timeOfGame;
    private int attendance;
    private int outs;
    private LinkedList<Character> symbols;
    private LinkedList<String> pitching_info_strings;

    private static final String pitchingStatColumns = 
            String.format("%3s%3s%3s%3s%3s%3s", "IP", "H", "R", "ER", "BB", "SO");

    private static final String battingStatColumns = 
            String.format("%3s%3s%3s%4s", "AB", "R", "H", "RBI");

    public NewspaperBoxscore(BxScrGameAccount game, BufferedWriter writer) {
        this.writer = writer;
        visitor = game.getTeam(false, this);
        home = game.getTeam(true, this);
        date = game.getDate();
        dayNight = game.getDayNight();
        gameNumber = game.getGameNumber();
        timeOfGame = game.getTimeOfGame();
        attendance = game.getAttendance();
        outs = game.getOuts();
        symbols = new LinkedList<Character>();
        pitching_info_strings = new LinkedList<String>();

        symbols.push('~');
        symbols.push('^');
        symbols.push('%');
        symbols.push('+');
        symbols.push('#');
        symbols.push('*');
    }

    /**
     * Write the boxscore game account to the provided <code>BufferedWriter</code>.
     * @throws IOException if an I/O exception occurs.
     */
    public void write() throws IOException {

        writeHeadline();

        /* Print lineups */
        printBatting();

        /* Print linescore */
        printLinescore();

        /* Print pitching lines */
        printPitching();

        /* Print additional statistical information */
        printAdditionalInfo();

        /* Print attendance and time (in hours). */
        String timeFormatted = Integer.toString(timeOfGame / 60) + 
                ":" + String.format("%02d", timeOfGame % 60);

        writer.write("T -- " + timeFormatted + "\n");
        writer.write("A -- " + Integer.toString(attendance) + "\n\n");

        writer.flush();
    }

    private void writeHeadline() throws IOException {
        String headline = "Game of " + date + " -- " + visitor.getCity() + 
                " at " + home.getCity() + " (" + dayNight + ")";

        if (gameNumber > 0)
            headline += " -- game " + gameNumber;
        writer.write(String.format("%5s", "") + headline + "\n\n");
    }

    private String getBoxscoreLine(BxScrPositionPlayer p) {
        String s1 = p.getName() + ", " + p.getPositionString();
        int[] batting_stats = p.getBxScrStats();

        String s2 = String.format("%3d%3d%3d%3d", 
                batting_stats[0], batting_stats[1], batting_stats[2],
                batting_stats[3]);
        
        return String.format("%-20s", s1) + s2;
    }

    private  String getBoxscoreLine(BxScrPitcher p) {
        
        String s1 = p.getName();
        char decision = p.getDecision();
        
        if (decision != '\0') {
            s1 += " (" + decision + ")";
        }

        if (p.removedFromInningWithoutRecordingOut()) {
            String c = String.valueOf(symbols.pop());
            s1 += c;
            String tmp = c + " Pitched to " + p.getInningBF() + " ";
            tmp += (p.getInningBF() == 1) ? "batter" : "batters";
            tmp += " in ";
            if (p.getInningRemoved() == 1) {
                tmp += "1st";
            } else if (p.getInningRemoved() == 2) {
                tmp += "2nd";
            } else if (p.getInningRemoved() == 3) {
                tmp += "3rd";
            } else {
                tmp += p.getInningRemoved() + "th";
            }
            pitching_info_strings.add(tmp);
        }

        int[] pitching_stats = p.getBxScrStats();
        String inng = BxScrPitcher.convertToIP(pitching_stats[0]);

        String s2 = String.format("%3s%3d%3d%3d%3d%3d", 
            inng, pitching_stats[1], pitching_stats[2], pitching_stats[3],
            pitching_stats[4], pitching_stats[5]);
        
        return String.format("%-22s", s1) + s2;
    }

    private void printBatting() throws IOException {
        LinkedList<String> visLineup = new LinkedList<>();
        LinkedList<String> homeLineup = new LinkedList<>();

        for (LinkedList<BxScrPositionPlayer> ar : visitor.getLineup()) {
            for (BxScrPositionPlayer p : ar) {
                visLineup.add(getBoxscoreLine(p));
            }
        }
        
        for (LinkedList<BxScrPositionPlayer> ar : home.getLineup()) {
            for (BxScrPositionPlayer p : ar) {
                homeLineup.add(getBoxscoreLine(p));
            }
        
        }
        
        int max = (visLineup.size() >= homeLineup.size()) ? 
            visLineup.size() : homeLineup.size();
        
        LinkedList<String> battingLines = new LinkedList<>();
        
        battingLines.add(
            String.format("%2s", "") + String.format("%-18s", visitor.getCity()) + 
            battingStatColumns + String.format("%4s", "") + String.format("%-18s", home.getCity()) + 
            battingStatColumns);
        
        for (int i = 0; i < max; i++) {
            String v = (i > visLineup.size() - 1) ? String.format("%35s", "")
                    : visLineup.get(i) + String.format("%3s", "");
            String h = (i > homeLineup.size() - 1) ? "" : homeLineup.get(i);
            battingLines.add(v + h);
        }
        
        battingLines.add(String.format("%20s%3s%3s%3s%3s", "", "--", "--", "--", "--") + 
            String.format("%3s", "") + 
            String.format("%20s%3s%3s%3s%3s", "", "--", "--", "--", "--"));
        
        int[] visStats = visitor.getLineupStats();
        int[] homeStats = home.getLineupStats();
        
        battingLines.add(String.format("%20s%3d%3d%3d%3d", 
            "", visStats[0], visStats[1], visStats[2], visStats[3], "") + 
            String.format("%3s", "") +
            String.format("%20s%3d%3d%3d%3d", 
                "", homeStats[0], homeStats[1], homeStats[2], homeStats[3]));
        
        for (String s : battingLines) {
            writer.write(s + "\n");
        }

        writer.write("\n");
        writer.flush();
    }

    private void printLinescore() throws IOException {
        String v = visitor.linescoreToString(3, 1);
        String h = home.linescoreToString(3, 1);
        
        if (v.length() > h.length()) {
            while (v.length() - 1 > h.length())
                h += " ";
            h += "x";
        }
        
        writer.write(String.format("%-17s", visitor.getCity()) + 
            v + " --" + String.format("%3d", visitor.getTotalRuns()));
        writer.write("\n");
        writer.write(String.format("%-17s", home.getCity()) + 
            h + " --" + String.format("%3d", home.getTotalRuns()));
        writer.write("\n");
        
        if (outs < 3) {
            String outs_str = (outs == 1) ? "1 out" : outs + " outs";
            writer.write(String.format("%2s", ""));
            if (visitor.getLinescore().length > home.getLinescore().length) {
                writer.write(outs_str + " when game ended.");
            } else {
                writer.write(outs_str + " when winning run scored.");
            }
            writer.write("\n");
        }
        
        writer.write("\n");
        writer.flush();
    }

    private void printPitching() throws IOException {
        symbols.clear();
        pitching_info_strings.clear();
        symbols.push('~');
        symbols.push('^');
        symbols.push('%');
        symbols.push('+');
        symbols.push('#');
        symbols.push('*');

        writer.write(String.format("%2s%-20s", 
            "", visitor.getCity()) + pitchingStatColumns + "\n");
        
        for (BxScrPitcher p : visitor.getPitchingStaff()) {
            writer.write(getBoxscoreLine(p) + "\n");
        }
        writer.write("\n");
        
        writer.write(String.format("%2s%-20s", 
            "", home.getCity()) + pitchingStatColumns + "\n");
        
        for (BxScrPitcher p : home.getPitchingStaff()) {
            writer.write(getBoxscoreLine(p) + "\n");
        }

        for (String s : pitching_info_strings) {
            writer.write(String.format("%2s", ""));
            writer.write(s);
            writer.write("\n");
        }

        writer.write("\n");
        writer.flush();
    }

    private void printAdditionalInfo() throws IOException {

        String errorStr = getSpecialStatString(BaseballPlayer.KEY_E, visitor, home, false);
        String doubleStr = getSpecialStatString(BaseballPlayer.KEY_2B, visitor, home, true);
        String tripleStr = getSpecialStatString(BaseballPlayer.KEY_3B, visitor, home, true);
        String hrStr = getSpecialStatString(BaseballPlayer.KEY_HR, visitor, home, true);
        String sbStr = getSpecialStatString(BaseballPlayer.KEY_SB, visitor, home, true);
        String csStr = getSpecialStatString(BaseballPlayer.KEY_CS, visitor, home, true);
        String shStr = getSpecialStatString(BaseballPlayer.KEY_SH, visitor, home, true);
        String sfStr = getSpecialStatString(BaseballPlayer.KEY_SF, visitor, home, true);
        String hbpStr = getSpecialStatString(BaseballPlayer.KEY_HBP, visitor, home, false);
        String wpStr = getSpecialStatString(BaseballPlayer.KEY_WP, visitor, home, false);
        String pbStr = getSpecialStatString(BaseballPlayer.KEY_PB, visitor, home, false);

        if (errorStr.length() > 0) {
            writer.write("E -- " + errorStr + "\n");
        }
        /* Double plays */
        if (visitor.get_double_triple_plays(true) > 0 || 
                home.get_double_triple_plays(true) > 0) {
            String dpStr = "";
            if (visitor.get_double_triple_plays(true) > 0) {
                dpStr += visitor.getCity() + " " + visitor.get_double_triple_plays(true);
            }
            if (visitor.get_double_triple_plays(true) > 0 && 
                    home.get_double_triple_plays(true) > 0) {
                dpStr += ", ";

            }
            if (home.get_double_triple_plays(true) > 0) {
                dpStr += home.getCity() + " " + home.get_double_triple_plays(true);
            }
            writer.write("DP -- " + dpStr + "\n");
        }

        /* Triple plays */
        if (visitor.get_double_triple_plays(false) > 0 ||
                home.get_double_triple_plays(false) > 0) {
            String tpStr = "";
            if (visitor.get_double_triple_plays(false) > 0) {
                tpStr += visitor.getCity() + " " + visitor.get_double_triple_plays(false);

            }
            if (visitor.get_double_triple_plays(false) > 0 &&
                     home.get_double_triple_plays(false) > 0) {
                tpStr += ", ";
            }
            if (home.get_double_triple_plays(false) > 0) {
                tpStr += home.getCity() + " " + home.get_double_triple_plays(false);
            }
            writer.write("TP -- " + tpStr + "\n");
        }

        writer.write("LOB -- " + visitor.getCity() + " " + visitor.get_lob() + 
            ", " + home.getCity() + " " + home.get_lob() + "\n");

        if (doubleStr.length() > 0) {
            writer.write("2B -- " + doubleStr + "\n");
        }

        if (tripleStr.length() > 0) {
            writer.write("3B -- " + tripleStr + "\n");
        }

        if (hrStr.length() > 0) {
            writer.write("HR -- " + hrStr + "\n");
        }

        if (sbStr.length() > 0) {
            writer.write("SB -- " + sbStr + "\n");
        }

        if (csStr.length() > 0) {
            writer.write("CS -- " + csStr + "\n");
        }

        if (shStr.length() > 0) {
            writer.write("SH -- " + shStr + "\n");
        }

        if (sfStr.length() > 0) {
            writer.write("SF -- " + sfStr + "\n");
        }

        if (hbpStr.length() > 0) {
            writer.write("HBP -- " + hbpStr + "\n");
        }

        if (wpStr.length() > 0) {
            writer.write("WP -- " + wpStr + "\n");
        }

        if (pbStr.length() > 0) {
            writer.write("PB -- " + pbStr + "\n");
        }
    }

    private String getSpecialStatString(String statKey, 
            Team visitor, Team home, boolean isBattingStat) {
        String str = "";
        
        if (isBattingStat || statKey.equals(BaseballPlayer.KEY_PB)) {
            for (LinkedList<BxScrPositionPlayer> ar : visitor.getLineup()) {
                for (BxScrPositionPlayer p : ar) {
                    if (p.getStat(statKey) > 0) {
                        if (p.getStat(statKey) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(statKey);
                        str += ", ";
                    }
                }
            }

            for (LinkedList<BxScrPositionPlayer> ar : home.getLineup()) {
                for (BxScrPositionPlayer p : ar) {
                    if (p.getStat(statKey) > 0) {
                        if (p.getStat(statKey) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(statKey);
                        str += ", ";
                    }
                }
            }

        } else { // errors and pitching stats
            
            if (statKey.equals("e")) {
                str = getSpecialStatString("e", visitor, home, true);
                str += ", ";
            }
            
            if (statKey.equals(BaseballPlayer.KEY_HBP)) {
                for (BxScrPitcher p : visitor.getPitchingStaff()) {
                    if (p.getBattersHBP().size() > 0) {
                        str += "by " + p.getName() + " (";
                        for (BxScrPositionPlayer batter : p.getBattersHBP()) {
                            str += batter.getName() + ", ";
                        }
                        str = str.substring(0, str.length() - 2);
                        str += "), ";
                    }
                }
                for (BxScrPitcher p : home.getPitchingStaff()) {
                    if (p.getBattersHBP().size() > 0) {
                        str += "by " + p.getName() + " (";
                        for (BxScrPositionPlayer batter : p.getBattersHBP()) {
                            str += batter.getName() + ", ";
                        }
                        str = str.substring(0, str.length() - 2);
                        str += "), ";
                    }
                }
            } else {
                for (BxScrPitcher p : visitor.getPitchingStaff()) {
                    if (p.getStat(statKey) > 0) {
                        if (p.getStat(statKey) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(statKey);
                        str += ", ";
                    }
                }
                for (BxScrPitcher p : home.getPitchingStaff()) {
                    if (p.getStat(statKey) > 0) {
                        if (p.getStat(statKey) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(statKey);
                        str += ", ";
                    }
                }
            }
        }

        if (str.length() > 0) {
            str = str.substring(0, str.length() - 2);
        }
        return str;
    }
}