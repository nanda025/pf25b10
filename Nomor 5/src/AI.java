import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI {
    private final Board board;
    private final Seed aiSeed;
    private final Seed playerSeed;
    private final String level;
    private final Random rand = new Random();

    public AI(Board board, Seed aiSeed, String level) {
        this.board = board;
        this.aiSeed = aiSeed;
        this.playerSeed = (aiSeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
        this.level = level.toLowerCase();
    }

    public Point getMove() {
        if (board == null) return null;

        switch (level) {
            case "easy":
                return getRandomMove();
            case "medium":
                return getMediumMove();
            case "hard":
                return getBestMove();  // minimax sederhana
            default:
                return getRandomMove();
        }
    }

    private Point getRandomMove() {
        List<Point> availableMoves = new ArrayList<>();
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    availableMoves.add(new Point(row, col));
                }
            }
        }
        return availableMoves.isEmpty() ? null : availableMoves.get(rand.nextInt(availableMoves.size()));
    }

    private Point getMediumMove() {
        // 1. Menang jika bisa
        Point winMove = canWinAt(aiSeed);
        if (winMove != null) return winMove;

        // 2. Blokir lawan jika hampir menang
        Point blockMove = canWinAt(playerSeed);
        if (blockMove != null) return blockMove;

        // 3. Random kalau tidak ada
        return getRandomMove();
    }

    private Point canWinAt(Seed seed) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = seed;
                    boolean isWinning = board.isWinning(seed);
                    board.cells[row][col].content = Seed.NO_SEED;
                    if (isWinning) return new Point(row, col);
                }
            }
        }
        return null;
    }

    private Point getBestMove() {
        int bestScore = Integer.MIN_VALUE;
        Point bestMove = null;

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = aiSeed;
                    int score = minimax(0, false);
                    board.cells[row][col].content = Seed.NO_SEED;

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new Point(row, col);
                    }
                }
            }
        }

        return bestMove;
    }

    private int minimax(int depth, boolean isMaximizing) {
        if (board.isWinning(aiSeed)) return 10 - depth;
        if (board.isWinning(playerSeed)) return depth - 10;
        if (board.isFull()) return 0;

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = isMaximizing ? aiSeed : playerSeed;
                    int score = minimax(depth + 1, !isMaximizing);
                    board.cells[row][col].content = Seed.NO_SEED;

                    bestScore = isMaximizing ?
                            Math.max(score, bestScore) :
                            Math.min(score, bestScore);
                }
            }
        }

        return bestScore;
    }
}
