import java.awt.*;
/**
 * The Board class models the ROWS-by-COLS game board.
 */
public class Board {
    // Define named constants
    public static final int ROWS = 3;  // ROWS x COLS cells
    public static final int COLS = 3;
    // Define named constants for drawing
    public static final int CANVAS_WIDTH = Cell.SIZE * COLS;  // the drawing canvas
    public static final int CANVAS_HEIGHT = Cell.SIZE * ROWS;
    public static final int GRID_WIDTH = 8;  // Grid-line's width
    public static final int GRID_WIDTH_HALF = GRID_WIDTH / 2; // Grid-line's half-width
    public static final Color COLOR_GRID = Color.LIGHT_GRAY;  // grid lines
    public static final int Y_OFFSET = 1;  // Fine tune for better display

    // Define properties (package-visible)
    /** Composes of 2D array of ROWS-by-COLS Cell instances */
    Cell[][] cells;

    /** Constructor to initialize the game board */
    public Board() {
        initGame();
    }

    /** Initialize the game objects (run once) */
    public void initGame() {
        cells = new Cell[ROWS][COLS]; // allocate the array
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                // Allocate element of the array
                cells[row][col] = new Cell(row, col);
                // Cells are initialized in the constructor
            }
        }
    }

    /** Reset the game board, ready for new game */
    public void newGame() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].newGame(); // clear the cell content
            }
        }
    }

    /**
     *  The given player makes a move on (selectedRow, selectedCol).
     *  Update cells[selectedRow][selectedCol]. Compute and return the
     *  new game state (PLAYING, DRAW, CROSS_WON, NOUGHT_WON).
     */
    public State stepGame(Seed player, int selectedRow, int selectedCol) {
        // Update game board
        cells[selectedRow][selectedCol].content = player;

        if (hasWon(player, selectedRow, selectedCol)) {
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }

        if (isFull()) {
            return State.DRAW;
        }

        return State.PLAYING;
    }

    // Digunakan oleh AI untuk mengecek apakah langkah tertentu menghasilkan kemenangan
    public boolean hasWon(Seed player, int row, int col) {
        return (cells[row][0].content == player && cells[row][1].content == player && cells[row][2].content == player) || // baris
                (cells[0][col].content == player && cells[1][col].content == player && cells[2][col].content == player) || // kolom
                (row == col && cells[0][0].content == player && cells[1][1].content == player && cells[2][2].content == player) || // diagonal utama
                (row + col == 2 && cells[0][2].content == player && cells[1][1].content == player && cells[2][0].content == player);  // diagonal sebalik
    }

    public boolean isWinning(Seed player) {
        for (int row = 0; row < ROWS; row++) {
            if (cells[row][0].content == player &&
                    cells[row][1].content == player &&
                    cells[row][2].content == player) return true;
        }

        for (int col = 0; col < COLS; col++) {
            if (cells[0][col].content == player &&
                    cells[1][col].content == player &&
                    cells[2][col].content == player) return true;
        }

        if (cells[0][0].content == player &&
                cells[1][1].content == player &&
                cells[2][2].content == player) return true;

        if (cells[0][2].content == player &&
                cells[1][1].content == player &&
                cells[2][0].content == player) return true;

        return false;
    }

    public boolean isFull() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (cells[row][col].content == Seed.NO_SEED) return false;
            }
        }
        return true;
    }

    // Gambar papan dan sel-selnya
    public void paint(Graphics g) {
        // Garis grid
        g.setColor(COLOR_GRID);
        for (int row = 1; row < ROWS; ++row) {
            g.fillRoundRect(0, Cell.SIZE * row - GRID_WIDTH_HALF,
                    CANVAS_WIDTH - 1, GRID_WIDTH,
                    GRID_WIDTH, GRID_WIDTH);
        }
        for (int col = 1; col < COLS; ++col) {
            g.fillRoundRect(Cell.SIZE * col - GRID_WIDTH_HALF, 0 + Y_OFFSET,
                    GRID_WIDTH, CANVAS_HEIGHT - 1,
                    GRID_WIDTH, GRID_WIDTH);
        }

        // Gambar semua sel
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].paint(g);
            }
        }
    }
}

