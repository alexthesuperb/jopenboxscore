package com.github.alexthesuperb.jopenboxscore;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.io.BufferedWriter;

/**
 * Write a detailed statistical summary of each player, by team, in a human-readable
 * format.
 */
public class NewspaperSummary implements BaseballBoxscore {

    private BufferedWriter writer;
    HashMap<String, AggregatedTeam<SingleGameTeam>> teams;

    private static final String[] battingKeys =    {BaseballPlayer.KEY_G,   //0
                                                    BaseballPlayer.KEY_AB,  //1
                                                    BaseballPlayer.KEY_R,   //2
                                                    BaseballPlayer.KEY_H,   //3
                                                    BaseballPlayer.KEY_2B,  //4
                                                    BaseballPlayer.KEY_3B,  //5
                                                    BaseballPlayer.KEY_HR,  //6
                                                    BaseballPlayer.KEY_RBI, //7
                                                    BaseballPlayer.KEY_BB,  //8
                                                    BaseballPlayer.KEY_SO,  //9
                                                    BaseballPlayer.KEY_SB,  //10
                                                    BaseballPlayer.KEY_CS,  //11
                                                    BaseballPlayer.KEY_HBP, //12
                                                    BaseballPlayer.KEY_SF,  //13
                                                    BaseballPlayer.KEY_E};  //14

    private static final String[] pitchingKeys =   {BaseballPlayer.KEY_G,
                                                    BaseballPlayer.KEY_PITCHER_WINS,
                                                    BaseballPlayer.KEY_PITCHER_LOSSES,
                                                    BaseballPlayer.KEY_PITCHER_SAVES,
                                                    BaseballPlayer.KEY_BATTERS_RETIRED,
                                                    BaseballPlayer.KEY_PITCHER_H,
                                                    BaseballPlayer.KEY_PITCHER_R,
                                                    BaseballPlayer.KEY_PITCHER_ER,
                                                    BaseballPlayer.KEY_PITCHER_BB,
                                                    BaseballPlayer.KEY_PITCHER_SO,
                                                    BaseballPlayer.KEY_PITCHER_GS};
    
    public NewspaperSummary(BufferedWriter writer) {
        this.writer = writer;
        teams = new HashMap<>();
    }

    public <T extends BoxscoreGameAccount> void addGame(T game) {
        SingleGameTeam visitor = game.getTeam(false, this);
        SingleGameTeam home = game.getTeam(true, this);

        addTeam(visitor.getTeamId(), visitor);
        addTeam(home.getTeamId(), home);
    }
    
    public void addGames(List<? extends BoxscoreGameAccount> games) {
        for (BoxscoreGameAccount game : games) {
            addGame(game);
        }
    }

    private void addTeam(String teamId, SingleGameTeam team) {
        if (teams.containsKey(teamId)) {
            teams.get(teamId).addGame(team);
        } else {
            AggregatedTeam<SingleGameTeam> newTeam = 
                    new AggregatedTeam<>(teamId,
                                         team.getCity(),
                                         team.getName());
            newTeam.addGame(team);
            teams.put(teamId, newTeam);
        }
    }


    @Override
    public void write() throws IOException {
        List<String> sortedKeys = new LinkedList<String>(teams.keySet());
        Collections.sort(sortedKeys);

        for (String k : sortedKeys) {
            AggregatedTeam<SingleGameTeam> team = teams.get(k);
            writeHomeAwayRecord(team);
            writeRows(team, false);
            writeRows(team, true);
            writer.write("\n");
        }
        writer.flush();
    }

    private String formatRateStat(float stat, int decimals, boolean leadingZero) {
        String statStr = String.format("%." + decimals + "f", stat);
       
        if (Float.isNaN(stat)) {
            return "";
        }

        if (Float.isInfinite(stat)) {
            return "inf";
        }

        if (!leadingZero) {
            if (statStr.charAt(0) == '0') {
                statStr = statStr.substring(1);
            }
        }
        return statStr;
    }

    private void writeHomeAwayRecord(AggregatedTeam<SingleGameTeam> team) throws IOException {
        int record[];
        String totalStr = String.format("%15s", "Total") + String.format("%15s","");
        String homeStr = String.format("%15s", "Home") + String.format("%15s","");
        String roadStr = String.format("%15s", "Road") + String.format("%15s","");
        String columns = String.format("%5s%5s%5s%5s%5s%5s","G", "W", "L", "T", "RS", "RA");

        /* Print section header */
        writer.write(team.getCity() + " " + team.getName() + " Win/Loss Record");
        writer.write("\n\n");

        /* Write columns titles */
        writer.write(String.format("%7s", "") + totalStr + 
                String.format("%2s", "") + homeStr + 
                String.format("%2s", "") + roadStr);
        writer.write("\n");
        writer.write(String.format("%-7s", "Team") + columns + 
                String.format("%2s", "") + columns +
                String.format("%2s", "") + columns);
        writer.write("\n");

        record = team.getTotalRecord();
        writer.write(String.format("%-7s", "Total") + 
                String.format("%5d%5d%5d%5d%5d%5d",
                        record[RecordMatrix.INDEX_TOTAL_GAMES],
                        record[RecordMatrix.INDEX_TOTAL_WINS],
                        record[RecordMatrix.INDEX_TOTAL_LOSSES],
                        record[RecordMatrix.INDEX_TOTAL_TIES],
                        record[RecordMatrix.INDEX_TOTAL_RS],
                        record[RecordMatrix.INDEX_TOTAL_RA]) +
                String.format("%2s", "") +
                String.format("%5d%5d%5d%5d%5d%5d",
                        record[RecordMatrix.INDEX_HOME_GAMES],
                        record[RecordMatrix.INDEX_HOME_WINS],
                        record[RecordMatrix.INDEX_HOME_LOSSES],
                        record[RecordMatrix.INDEX_HOME_TIES],
                        record[RecordMatrix.INDEX_HOME_RS],
                        record[RecordMatrix.INDEX_HOME_RA]) + 
                String.format("%2s", "") +
                String.format("%5d%5d%5d%5d%5d%5d",
                        record[RecordMatrix.INDEX_ROAD_GAMES],
                        record[RecordMatrix.INDEX_ROAD_WINS],
                        record[RecordMatrix.INDEX_ROAD_LOSSES],
                        record[RecordMatrix.INDEX_ROAD_TIES],
                        record[RecordMatrix.INDEX_ROAD_RS],
                        record[RecordMatrix.INDEX_ROAD_RA]));
        writer.write("\n");

        for (String id : team.getOpponentIds()) {
            record = team.getRecordVersusTeam(id);
            writer.write(String.format("%-7s", id) + 
                String.format("%5d%5d%5d%5d%5d%5d",
                        record[RecordMatrix.INDEX_TOTAL_GAMES],
                        record[RecordMatrix.INDEX_TOTAL_WINS],
                        record[RecordMatrix.INDEX_TOTAL_LOSSES],
                        record[RecordMatrix.INDEX_TOTAL_TIES],
                        record[RecordMatrix.INDEX_TOTAL_RS],
                        record[RecordMatrix.INDEX_TOTAL_RA]) +
                String.format("%2s", "") +
                String.format("%5d%5d%5d%5d%5d%5d",
                        record[RecordMatrix.INDEX_HOME_GAMES],
                        record[RecordMatrix.INDEX_HOME_WINS],
                        record[RecordMatrix.INDEX_HOME_LOSSES],
                        record[RecordMatrix.INDEX_HOME_TIES],
                        record[RecordMatrix.INDEX_HOME_RS],
                        record[RecordMatrix.INDEX_HOME_RA]) + 
                String.format("%2s", "") +
                String.format("%5d%5d%5d%5d%5d%5d",
                        record[RecordMatrix.INDEX_ROAD_GAMES],
                        record[RecordMatrix.INDEX_ROAD_WINS],
                        record[RecordMatrix.INDEX_ROAD_LOSSES],
                        record[RecordMatrix.INDEX_ROAD_TIES],
                        record[RecordMatrix.INDEX_ROAD_RS],
                        record[RecordMatrix.INDEX_ROAD_RA]));
            writer.write("\n");
        }

        writer.write("\n");
        writer.flush();
    }

    private void writeRows(AggregatedTeam<SingleGameTeam> team, boolean isPitching) 
            throws IOException {
        List<? extends BaseballPlayer> players;
        String type;

        if (isPitching) {
            players = team.getAllPitchers();
            type = "Pitching";
        } else {
            players = team.getAllPositionPlayers();
            type = "Batting";
        }
        Collections.sort(players);

        writer.write(team.getCity() + " " + team.getName() + " " + type);
        writer.write("\n");

        if (isPitching) {
            writer.write(String.format("%-15s","") +
                        String.format("%5s%5s%7s%5s%5s%5s",
                                        "g", "gs", "era", "w", "l", "sv") +
                        String.format("%7s%5s%5s%5s%5s%5s",
                                        "ip", "h", "r", "er", "bb", "so"));
        } else {
            writer.write(String.format("%-15s","") +
                     String.format("%5s%5s%5s%5s%5s%5s%5s%5s%5s%5s",
                                   "g","ab","r","h","2b","3b","hr","rbi","bb","so") +
                     String.format("%7s%7s%7s", "avg","obp","slg") +
                     String.format("%5s%5s%5s", "sb", "cs", "e"));
        }
        writer.write("\n");

        for (BaseballPlayer player : players) {
            writer.write(String.format("%-15s", player.getName()));
            writer.write(getStatLineString(player, isPitching));
            writer.write("\n");
        }

        if (isPitching) {
            writer.write(String.format("%-15s","") +
                     String.format("%5s%5s%7s%5s%5s%5s",
                                    "---", "---", "---", "---", "---", "---") +
                     String.format("%7s%5s%5s%5s%5s%5s",
                                    "-----", "---", "---", "---", "---", "---"));
        } else {
            writer.write(String.format("%-15s","") +
                    String.format("%5s%5s%5s%5s%5s%5s%5s%5s%5s%5s",
                                   "---", "---", "---", "---", "---",
                                   "---", "---", "---", "---", "---") + 
                    String.format("%7s%7s%7s", "----", "----", "----") + 
                    String.format("%5s%5s%5s", "---", "---", "---"));
        }
        writer.write("\n");
        writer.write(String.format("%-15s", "") + getStatLineString(team, isPitching));
        
        writer.write("\n\n");
        writer.flush();
    }

    /**
     * 
     * @param obj
     * @param isPitching
     * @return
     */
    private String getStatLineString(SportsStatContainer obj, boolean isPitching) {
        String line;
        int[] stats;

        if (isPitching) {
            stats = obj.getStats(pitchingKeys);
            String ip = BaseballPlayer.convertToIP(stats[4]);
            float era = BaseballPlayer.getEarnedRunAverage(stats[7], stats[4]);
            String eraString = formatRateStat(era, 2, true);
            line = String.format("%5d%5d%7s%5d%5d%5d",
                                        stats[0], stats[10], eraString, stats[1], 
                                        stats[2], stats[3]) +
                          String.format("%7s%5s%5s%5s%5s%5s",
                                        ip, stats[5], stats[6], stats[7], 
                                        stats[8], stats[9]);
        } else {
            stats = obj.getStats(battingKeys);
            float avg = BaseballPlayer.getBattingAverage(stats[1], stats[3]);
            float obp = BaseballPlayer.getOnBasePercentage(stats[1], stats[3], stats[8], 
                                                        stats[12], stats[13]);
            float slg = BaseballPlayer.getSluggingPercentage(stats[1], stats[3], stats[4], 
                                                        stats[5], stats[6]);
            
            String avgStr = formatRateStat(avg, 3, false);
            String slgStr = formatRateStat(slg, 3, false);
            String obpStr = formatRateStat(obp, 3, false);

            line = String.format("%5d%5d%5d%5d%5d%5d%5d%5d%5d%5d",
                                            stats[0], stats[1], stats[2], stats[3],
                                            stats[4], stats[5], stats[6], stats[7],
                                            stats[8], stats[9]);
            line += String.format("%7s%7s%7s", avgStr, obpStr, slgStr);
            line += String.format("%5d%5d%5d", stats[10], stats[11], stats[14]);
        }

        return line;
    }

}