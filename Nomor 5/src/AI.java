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
        switch (level) {
            case "easy":
                return getRandomMove();
            case "medium":
                return getMediumMove();
            case "hard":
                return getBestMove();  // basic minimax
            default:
                return getRandomMove();
        }
    }

    // ===== LEVEL EASY: random move =====
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

    // ===== LEVEL MEDIUM: try to win or block =====
    private Point getMediumMove() {
        // 1. Try to win
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = aiSeed;
                    if (board.isWinning(aiSeed)) {
                        board.cells[row][col].content = Seed.NO_SEED;
                        return new Point(row, col);
                    }
                    board.cells[row][col].content = Seed.NO_SEED;
                }
            }
        }

        // 2. Try to block
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = playerSeed;
                    if (board.isWinning(playerSeed)) {
                        board.cells[row][col].content = Seed.NO_SEED;
                        return new Point(row, col);
                    }
                    board.cells[row][col].content = Seed.NO_SEED;
                }
            }
        }

        // 3. Else random
        return getRandomMove();
    }

    // ===== LEVEL HARD: minimax simple version =====
    private Point getBestMove() {
        int bestScore = Integer.MIN_VALUE;
        Point bestMove = null;

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = aiSeed;
                    int score = minimax(false);
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

    private int minimax(boolean isMaximizing) {
        if (board.isWinning(aiSeed)) return 10;
        if (board.isWinning(playerSeed)) return -10;
        if (board.isFull()) return 0;

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = isMaximizing ? aiSeed : playerSeed;
                    int score = minimax(!isMaximizing);
                    board.cells[row][col].content = Seed.NO_SEED;

                    bestScore = isMaximizing ?
                            Math.max(score, bestScore) : Math.min(score, bestScore);
                }
            }
        }

        return bestScore;
    }
}

