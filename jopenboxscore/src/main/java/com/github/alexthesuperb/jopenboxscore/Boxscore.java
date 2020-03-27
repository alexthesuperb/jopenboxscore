/*
 * C-style comment with author/copyright information...
 */

package com.github.alexthesuperb.jopenboxscore;

import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.IOException;

public class Boxscore {
    
    /** Formatted <code>String</code> of pitching stat labels. */
    private static String pColumns = String.format("%3s%3s%3s%3s%3s%3s", "IP", "H", "R", "ER", "BB", "SO");
    
    /** 
     * A <code>LinkedList</code> of symbols to display beside the names of
     * pitchers who were removed from an inning without recording an out.
     */
    private static LinkedList<Character> symbols = new LinkedList<>();
    
    /**
     * A <code>LinkedList</code> of information nothing the inning and number of
     * batters faced by pitchers removed from an inning before recording an out.
     */
    private static LinkedList<String> pitching_info_strings = new LinkedList<>();

    /**
     * 
     * @param writer
     * @param visitor
     * @param home
     * @param date
     * @param dayNight
     * @param gmNumber
     * @param timeOfGame
     * @param attend
     * @param outs
     * @throws IOException
     */
    public static void printBoxscore(BufferedWriter writer, Team visitor, Team home, String date, char dayNight,
            int gmNumber, int timeOfGame, int attend, int outs) throws IOException {
        
        // Print headline
        String headline = "Game of " + date + " -- " + visitor.getCity() + " at " + home.getCity() + " (" + dayNight
                + ")";
        if (gmNumber > 0)
            headline += " -- game " + gmNumber;
        writer.write(String.format("%5s", "") + headline + "\n\n");

        // Print lineups
        printBatting(writer, visitor, home);

        // Print linescore
        printLinescore(writer, visitor, home, outs);

        // Print pitching lines
        printPitching(writer, visitor, home);

        // Print additional statistical information
        printAdditionalInfo(writer, visitor, home);

        // Print additional information
        String timeFormatted = Integer.toString(timeOfGame / 60) + ":" + String.format("%02d", timeOfGame % 60);
        writer.write("T -- " + timeFormatted + "\n");
        writer.write("A -- " + Integer.toString(attend) + "\n\n");

        writer.flush();
    }

    /**
     * Print inning run totals for each team, indenting each 3 innings. If game ends
     * mid-inning (either on a walk off or on weather delay), indicate the number of
     * outs recorded in that half- inning.
     * 
     * @param writer
     * @param visitor
     * @param home
     * @param outs
     * @throws IOException
     */
    private static void printLinescore(BufferedWriter writer, Team visitor, Team home, int outs) throws IOException {
        
        String v = visitor.linescoreToString();
        String h = home.linescoreToString();
        
        if (v.length() > h.length()) {
            while (v.length() - 1 > h.length())
                h += " ";
            h += "x";
        }
        
        writer.write(
                String.format("%-17s", visitor.getCity()) + v + " --" + String.format("%3d", visitor.getTotalRuns()));
        writer.write("\n");
        writer.write(String.format("%-17s", home.getCity()) + h + " --" + String.format("%3d", home.getTotalRuns()));
        writer.write("\n");
        
        if (outs < 3) {
            String outs_str = (outs == 1) ? "1 out" : outs + " outs";
            writer.write(String.format("%2s", ""));
            if (visitor.getLinescore().size() > home.getLinescore().size()) {
                writer.write(outs_str + " when game ended.");
            } else {
                writer.write(outs_str + " when winning run scored.");
            }
            writer.write("\n");
        }
        
        writer.write("\n");
        writer.flush();
    }

    /**
     * Print each team's batting boxscore lines in parallel columns. Below each
     * team's batting column, print its totals.
     * 
     * @param writer
     * @param visitor
     * @param home
     * @throws IOException
     */
    private static void printBatting(BufferedWriter writer, Team visitor, Team home) throws IOException {
        
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
        
        int max = (visLineup.size() >= homeLineup.size()) ? visLineup.size() : homeLineup.size();
        
        LinkedList<String> battingLines = new LinkedList<>();
        
        battingLines.add(String.format("%2s", "") + String.format("%-18s", visitor.getCity())
                + String.format("%3s%3s%3s%4s", "AB", "R", "H", "RBI") + String.format("%4s", "")
                + String.format("%-18s", home.getCity()) + String.format("%3s%3s%3s%4s", "AB", "R", "H", "RBI"));
        
                for (int i = 0; i < max; i++) {
            String v = (i > visLineup.size() - 1) ? String.format("%35s", "")
                    : visLineup.get(i) + String.format("%3s", "");
            String h = (i > homeLineup.size() - 1) ? "" : homeLineup.get(i);
            battingLines.add(v + h);
        }
        
        battingLines.add(String.format("%20s%3s%3s%3s%3s", "", "--", "--", "--", "--") + String.format("%3s", "")
            + String.format("%20s%3s%3s%3s%3s", "", "--", "--", "--", "--"));
        
        int[] visStats = visitor.getLineupStats();
        int[] homeStats = home.getLineupStats();
        
        battingLines.add(String.format("%20s%3d%3d%3d%3d", "", visStats[0], visStats[1], visStats[2], visStats[3], "")
                + String.format("%3s", "")
                + String.format("%20s%3d%3d%3d%3d", "", homeStats[0], homeStats[1], homeStats[2], homeStats[3]));
        
        for (String s : battingLines) {
            writer.write(s + "\n");
        }

        writer.write("\n");
        writer.flush();
    }

    /**
     * Return a boxscore-ready <code>String</code> displaying the players who earned
     * a special statistic. 
     * @param stat_code The statistical category.
     * @param visitor Visiting team.
     * @param home Home team.
     * @param isBattingStat <code>true</code> for batting statistics, <code>false</code>
     * for pitching and fielding statistics.
     * @return a boxscore-style <code>String</code> of players who garnered some
     * statistic.
     */
    private static String getSpecialStatString(String stat_code, Team visitor, Team home, boolean isBattingStat) {
        
        String str = "";
        
        if (isBattingStat || stat_code.equals("pb")) {
            for (LinkedList<BxScrPositionPlayer> ar : visitor.getLineup()) {
                for (BxScrPositionPlayer p : ar) {
                    if (p.getStat(stat_code) > 0) {
                        if (p.getStat(stat_code) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(stat_code);
                        str += ", ";
                    }
                }
            }

            for (LinkedList<BxScrPositionPlayer> ar : home.getLineup()) {
                for (BxScrPositionPlayer p : ar) {
                    if (p.getStat(stat_code) > 0) {
                        if (p.getStat(stat_code) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(stat_code);
                        str += ", ";
                    }
                }
            }

        } else { // errors and pitching stats
            
            if (stat_code.equals("e")) {
                str = getSpecialStatString("e", visitor, home, true);
                str += ", ";
            }
            
            if (stat_code.equals("hp") || stat_code.equals("hbp")) {
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
                    if (p.getStat(stat_code) > 0) {
                        if (p.getStat(stat_code) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(stat_code);
                        str += ", ";
                    }
                }
                for (BxScrPitcher p : home.getPitchingStaff()) {
                    if (p.getStat(stat_code) > 0) {
                        if (p.getStat(stat_code) == 1)
                            str += p.getName();
                        else
                            str += p.getName() + " " + p.getStat(stat_code);
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

    /**
     * 
     * @param writer
     * @param visitor
     * @param home
     * @throws IOException
     */
    private static void printAdditionalInfo(BufferedWriter writer, Team visitor, Team home) throws IOException {
        
        String errorStr = getSpecialStatString("e", visitor, home, false);
        String doubleStr = getSpecialStatString("d", visitor, home, true);
        String tripleStr = getSpecialStatString("t", visitor, home, true);
        String hrStr = getSpecialStatString("hr", visitor, home, true);
        String sbStr = getSpecialStatString("sb", visitor, home, true);
        String csStr = getSpecialStatString("cs", visitor, home, true);
        String shStr = getSpecialStatString("sh", visitor, home, true);
        String sfStr = getSpecialStatString("sf", visitor, home, true);
        String hbpStr = getSpecialStatString("hp", visitor, home, false);
        String wpStr = getSpecialStatString("wp", visitor, home, false);
        String pbStr = getSpecialStatString("pb", visitor, home, false);

        if (errorStr.length() > 0) {
            writer.write("E -- " + errorStr + "\n");
        }
        /* Double plays */
        if (visitor.get_double_triple_plays(true) > 0 || visitor.get_double_triple_plays(true) > 0) {
            String dpStr = "";
            if (visitor.get_double_triple_plays(true) > 0)
                dpStr += visitor.getCity() + " " + visitor.get_double_triple_plays(true);
            if (visitor.get_double_triple_plays(true) > 0 && home.get_double_triple_plays(true) > 0)
                dpStr += ", ";
            if (home.get_double_triple_plays(true) > 0) {
                dpStr += home.getCity() + " " + home.get_double_triple_plays(true);
            }
            writer.write("DP -- " + dpStr + "\n");
        }

        /* Triple plays */
        if (visitor.get_double_triple_plays(false) > 0 || visitor.get_double_triple_plays(false) > 0) {
            String tpStr = "";
            if (visitor.get_double_triple_plays(false) > 0)
                tpStr += visitor.getCity() + " " + visitor.get_double_triple_plays(false);
            if (visitor.get_double_triple_plays(false) > 0 && home.get_double_triple_plays(false) > 0)
                tpStr += ", ";
            if (home.get_double_triple_plays(false) > 0) {
                tpStr += home.getCity() + " " + home.get_double_triple_plays(false);
            }
            writer.write("TP -- " + tpStr + "\n");
        }

        writer.write("LOB -- " + visitor.getCity() + " " + visitor.get_lob() + ", " + home.getCity() + " "
                + home.get_lob() + "\n");

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

    /**
     * Print each team's pitching lines: visitors, followed by home.
     * @param writer
     * @param visitor
     * @param home
     * @throws IOException
     */
    private static void printPitching(BufferedWriter writer, Team visitor, Team home) throws IOException {
        
        symbols.clear();
        pitching_info_strings.clear();
        symbols.push('~');
        symbols.push('^');
        symbols.push('%');
        symbols.push('+');
        symbols.push('#');
        symbols.push('*');

        writer.write(String.format("%2s%-20s", "", visitor.getCity()) + pColumns + "\n");
        
        for (BxScrPitcher p : visitor.getPitchingStaff()) {
            writer.write(getBoxscoreLine(p) + "\n");
        }
        writer.write("\n");
        
        writer.write(String.format("%2s%-20s", "", home.getCity()) + pColumns + "\n");
        
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

    /**
     * @param p
     * @return input pitcher's boxscore line: name (decision), IP, H, R, ER, BB, SO
     */
    private static String getBoxscoreLine(BxScrPitcher p) {
        
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

        String s2 = String.format("%3s%3d%3d%3d%3d%3d", inng, pitching_stats[1], pitching_stats[2], pitching_stats[3],
            pitching_stats[4], pitching_stats[5]);
        
        return String.format("%-22s", s1) + s2;
    }

    /**
     * 
     * @param p
     * @return input batter's boxscore line: name, AB, R, H, RBI
     */
    private static String getBoxscoreLine(BxScrPositionPlayer p) {

        String s1 = p.getName() + ", " + p.getPositionString();
        
        int[] batting_stats = p.getBxScrStats();
        
        String s2 = String.format("%3d%3d%3d%3d", batting_stats[0], batting_stats[1], batting_stats[2],
            batting_stats[3]);
        
        return String.format("%-20s", s1) + s2;
    }
}