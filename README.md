# jopenboxscore

An open-source alternative to Retrosheet's proprietary BOX.EXE newspaper boxscore-generating
software written in the Java Programming Language.

*The information used here was obtained free of charge from and is copyrighted by Retrosheet. Interested parties may contact Retrosheet at www.retrosheet.org*

## Motivation

This project began as an attempt to develop an alternative to Retrosheet's BOX.EXE program that would
run on MacOS Catalina. However, during development, it began to morph into something slightly more 
complex. Jopenboxscore is a Retrosheet play-by-play-parsing Java library capable of generating boxscores, individual batting and pitching statistics, and team win-loss records. 

## Before You Start

It should be noted that while this project can correctly parse approximately 93% of official Retrosheet event file game accounts, it was specifically developed to generate boxscores and 
statistics for personal use (Strat-O-Matic, little league, etc) and works best on files written
by the user.

The game account parsing functionality was written following Retrosheet's Project Scoresheet
syntax. A complete explanation of that syntax can be found [here](https://www.retrosheet.org/eventfile.htm#1).

Additionally, this software relies on Retrosheet roster \(ROS\) and TEAM files, which must be placed in the directory from which the jar is executed. Those support files, along with the event files \(.EVE, .EVA, and .EVN\) to be parsed, can be found [here](https://www.retrosheet.org/game.htm).


## How to use jopenboxscore

Once the jar, desired event files, and necessary team and roster files have been added to your working directory, you may run jopenboxscore.

To illustrate its functionality, let's take a look at the 2018 New York Yankees. To process their home games, we will need that event file, *2018NYA.EVA*, along with the roster files detailing both New York and all of its opponents. Finally, we need *2018TEAM*, which lists each team's ID, name, and league.

Our directory should look something like this:

```
2018ANA.EVA
2018ARA.EVN
2018ATL.EVN
2018BAL.EVA

...

2018NYA.EVA

...

2018TEAM

...

jopenboxscore-1.0.jar
```

To see an old-school *The Sporting News*-style boxscore for each game, we can use the command:

```
java -jar jopenboxscore-1.0.jar -y 2018 2018NYA.EVA
```

where ```-y 2018``` tells the program to look only at roster files with names containing the year 2018.

This is the simplest functionality of jopenboxscore: every single game account stored in the event file will be printed to the terminal. 

Of course, this isn't particularly useful. If we want to save these boxscores to a text file, we can use the command ```-dest filename```. The full command would look like this:

```
java -jar jopenboxscore-1.0.jar -y 2018 2018NYA.EVA -dest 2018yankees.txt
```

Games can also be processed by their unique game IDs, which mark the beginning of each game account within a file, or by games falling within a set of dates. For example,

```
java -jar jopenboxscore-1.0.jar -y 2018 2018NYA.EVA -i NYA201805040
```

would process the Yankees' early-May comeback win over Cleveland, while


```
java -jar jopenboxscore-1.0.jar -y 2018 2018NYA.EVA -start 0501 -end 0531
```

would show us every American League game played in New York for the month of March, 2018.

In the same way, detailed win-loss splits broken down by opponent and home/away, along with complete
hitting and pitching statistics for each team in each game processed, can be generated using the 
```-summary filename.txt``` command:

```
java -jar jopenboxscore-1.0.jar -y 2018 2018NYA.EVA -dest 2018yankess-box.txt -summary 2018yankees-stats.txt
```

To display these statistics in terminal, replace a filename with ```CONSOLE```.

## TODO

- [ ] Use unit testing to check single-game and cumulative statistics against official totals
- [ ] Add the option of generating CSV statistical summaries
- [ ] Jump over games causing null-pointer exceptions, rather than halting the entire program