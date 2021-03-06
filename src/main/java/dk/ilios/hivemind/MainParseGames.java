package dk.ilios.hivemind;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.parser.BoardspaceGameParser;
import dk.ilios.hivemind.parser.BoardspaceGameType;
import dk.ilios.hivemind.parser.filters.BoardspaceNetMasterPlayersFilter;
import dk.ilios.hivemind.parser.filters.Filter;
import dk.ilios.hivemind.parser.filters.HivePLGamesOnlyFilter;
import dk.ilios.hivemind.parser.filters.QueenAsStartingPieceFilter;
import dk.ilios.hivemind.parser.metric.*;

import java.io.File;
import java.util.*;

public class MainParseGames {

    public static final String ALL_DIR = "./plays/all/"; // tournament plays
    public static final String TOURNAMENT_DIR = "./plays/tournament/"; // tournament plays
    public static final String PLAYER_DIR  = "./plays/players/"; // games between players
    public static final String DUMBOT_DIR = "./plays/dumbot/";  // games between Dumbot and a human player

    List<Metric> metrics = new ArrayList<Metric>();
    List<Filter> filters = new ArrayList<Filter>();
    Map<BoardspaceGameType, List<File>>  files = new HashMap<BoardspaceGameType, List<File>>();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new MainParseGames().start();
        System.out.println("Done: " + (System.currentTimeMillis() - start)/1000 + " s.");
    }

    public void start() {
        setupMetrics();
        setupFilters();
        setupGameFiles();

        // Start parsing
        // If a game breaks, quit parsing and report progress so far
        for (BoardspaceGameType type : files.keySet()) {
            List<File> fileList = files.get(type);
            for (File file : fileList) {
                BoardspaceGameParser parser = null;
                Game game = null;

                try {
                    parser = new BoardspaceGameParser(file);
                    game = parser.parse();
                } catch (RuntimeException e) {
                    System.out.println("Game could not be parsed: " + file.getName() + " -> " + e);
                    continue;
                }

                boolean analyseGame = true;
                for (Filter filter : filters) {
                    if (!filter.analyseGame(parser.getGameType(), game)) {
                        analyseGame = false;
                        break;
                    }
                }

                if (analyseGame) {
                    for (Metric metric : metrics) {
                        game.setReplayMode(false);
                        metric.analyzeGame(type, parser.getGameType(), game);
                    }
                }
            }
        }

        saveMetrics();
  }

    private void setupGameFiles() {
        // Setup directories to parse
//        Stack<File> dirs = new Stack<File>();
//        dirs.add(new File(TOURNAMENT_DIR));

        // Test
//        List<File> testFiles = new ArrayList<File>();
//        testFiles.add(new File("./plays/test.sgf"));
//        files.put(BoardspaceGameType.PLAYER, testFiles);

//        Stack<File> tournamentFiles = new Stack<File>();
//        tournamentFiles.add(new File(TOURNAMENT_DIR));
//        files.put(BoardspaceGameType.TOURNAMENT, findAllFiles(tournamentFiles, new ArrayList<File>()));

//        Stack<File> playerFiles = new Stack<File>();
//        playerFiles.add(new File(PLAYER_DIR));
//        files.put(BoardspaceGameType.PLAYER, findAllFiles(playerFiles, new ArrayList<File>()));

        Stack<File> dumbotFiles = new Stack<File>();
        dumbotFiles.add(new File(DUMBOT_DIR));
        files.put(BoardspaceGameType.DUMBOT, findAllFiles(dumbotFiles, new ArrayList<File>()));

    }


    // Setup metrics to report on
    private void setupMetrics() {
//        metrics.add(new GamesAnalyzedMetric());
//        metrics.add(new GameResultPrColorMetric(true));
//        metrics.add(new GameDurationTurnsMetric());
//        metrics.add(new GameDurationTimeMetric());
//        metrics.add(new OpeningTokenMetric());
//        metrics.add(new LastTokenMetric());
//        metrics.add(new BugsInSupplyAtGameEndMetric());
//        metrics.add(new BugsOnBoardAtGameEndMetric());
//        metrics.add(new OpeningsMetric());
//        metrics.add(new WinnerOpeningMetric());
//        metrics.add(new FreeTokensPrTurnMetric(15));
//        metrics.add(new FreeTokensPrTurnMetric(25));
//        metrics.add(new TokensAroundQueenMetric(15));
//        metrics.add(new TokensAroundQueenMetric(25));
//        metrics.add(new HighRankedGamesMetric());
//        metrics.add(new CompareOpeningsMetric());
//        metrics.add(new LeastMovedTokenMetric());
        metrics.add(new IngersollOpeningsMetric());
    }

    private void setupFilters() {
//        filters.add(new BoardspaceNetMasterPlayersFilter());
//        filters.add(new HivePLGamesOnlyFilter());
        filters.add(new QueenAsStartingPieceFilter());
    }


    // Recursively find all files in the dirs then return result when done
    private List<File> findAllFiles(Stack<File> dirs, ArrayList<File> result) {
        if (dirs.isEmpty()) {
            return result;
        } else {
            File[] files = dirs.pop().listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    dirs.push(file);
                } else {
                    result.add(file);
                }
            }

            return findAllFiles(dirs, result);
        }
    }

    private void saveMetrics() {
        for (Metric metric : metrics) {
            metric.save();
        }
    }
}
