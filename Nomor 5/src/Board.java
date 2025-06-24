import java.awt.*;

public class Board {
    public static final int ROWS = 3;
    public static final int COLS = 3;
    public static final int CANVAS_WIDTH = Cell.SIZE * COLS;
    public static final int CANVAS_HEIGHT = Cell.SIZE * ROWS;
    public static final int GRID_WIDTH = 8;
    public static final int GRID_WIDTH_HALF = GRID_WIDTH / 2;
    public static final Color COLOR_GRID = Color.LIGHT_GRAY;
    public static final int Y_OFFSET = 1;

    Cell[][] cells;

    // Variabel garis kemenangan
    private int winStartX, winStartY, winEndX, winEndY;
    private boolean hasWinnerLine = false;

    public Board() {
        initGame();
    }

    public void initGame() {
        cells = new Cell[ROWS][COLS];
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    public void newGame() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].newGame();
            }
        }
        hasWinnerLine = false;
    }

    public State stepGame(Seed player, int selectedRow, int selectedCol) {
        cells[selectedRow][selectedCol].content = player;

        // Cek baris
        if (cells[selectedRow][0].content == player &&
                cells[selectedRow][1].content == player &&
                cells[selectedRow][2].content == player) {
            winStartX = 0;
            winStartY = selectedRow * Cell.SIZE + Cell.SIZE / 2;
            winEndX = CANVAS_WIDTH;
            winEndY = winStartY;
            hasWinnerLine = true;
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }

        // Cek kolom
        if (cells[0][selectedCol].content == player &&
                cells[1][selectedCol].content == player &&
                cells[2][selectedCol].content == player) {
            winStartX = selectedCol * Cell.SIZE + Cell.SIZE / 2;
            winStartY = 0;
            winEndX = winStartX;
            winEndY = CANVAS_HEIGHT;
            hasWinnerLine = true;
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }

        // Cek diagonal utama
        if (selectedRow == selectedCol &&
                cells[0][0].content == player &&
                cells[1][1].content == player &&
                cells[2][2].content == player) {
            winStartX = 0;
            winStartY = 0;
            winEndX = CANVAS_WIDTH;
            winEndY = CANVAS_HEIGHT;
            hasWinnerLine = true;
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }

        // Cek diagonal lawan
        if (selectedRow + selectedCol == 2 &&
                cells[0][2].content == player &&
                cells[1][1].content == player &&
                cells[2][0].content == player) {
            winStartX = CANVAS_WIDTH;
            winStartY = 0;
            winEndX = 0;
            winEndY = CANVAS_HEIGHT;
            hasWinnerLine = true;
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }

        // Cek apakah masih ada cell kosong
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (cells[row][col].content == Seed.NO_SEED) {
                    return State.PLAYING;
                }
            }
        }

        return State.DRAW;
    }

    public void paint(Graphics g) {
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

        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].paint(g);
            }
        }

        // Gambar garis kemenangan jika ada
        if (hasWinnerLine) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(6));
            g2.drawLine(winStartX, winStartY, winEndX, winEndY);
        }
    }
    // Cek apakah pemain tertentu sedang menang
    public boolean isWinning(Seed player) {
        // Cek baris
        for (int row = 0; row < ROWS; row++) {
            if (cells[row][0].content == player &&
                    cells[row][1].content == player &&
                    cells[row][2].content == player) {
                return true;
            }
        }

        // Cek kolom
        for (int col = 0; col < COLS; col++) {
            if (cells[0][col].content == player &&
                    cells[1][col].content == player &&
                    cells[2][col].content == player) {
                return true;
            }
        }

        // Cek diagonal utama
        if (cells[0][0].content == player &&
                cells[1][1].content == player &&
                cells[2][2].content == player) {
            return true;
        }

        // Cek diagonal lawan
        if (cells[0][2].content == player &&
                cells[1][1].content == player &&
                cells[2][0].content == player) {
            return true;
        }

        return false;
    }

    // Cek apakah semua cell sudah terisi (untuk minimax)
    public boolean isFull() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (cells[row][col].content == Seed.NO_SEED) {
                    return false;
                }
            }
        }
        return true;
    }

}
