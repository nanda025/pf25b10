import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 * Tic-Tac-Toe: Two-player Graphic version with better OO design.
 * The Board and Cell classes are separated in their own classes.
 */
public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L; // to prevent serializable warning

    // Define named constants for the drawing graphics
    public static final String TITLE = "Kelompok B10";
    public static final Color COLOR_BG = new Color(245, 245, 220);       // Krem
    public static final Color COLOR_BG_STATUS = new Color(216, 216, 216);
    public static final Color COLOR_CROSS = new Color(255, 255, 0);      // Kuning
    public static final Color COLOR_NOUGHT = new Color(0, 128, 0);       // Hijau
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);



    // Define game objects
    private Board board;         // the game board
    private State currentState;  // the current state of the game
    private Seed currentPlayer;  // the current player
    private JLabel statusBar;    // for displaying status message
    private boolean vsComputer;

    /** Constructor to setup the UI and game components */
    public GameMain(boolean vsComputer) {
        this.vsComputer = vsComputer;

        // This JPanel fires MouseEvent
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {  // mouse-clicked handler
                if (currentState != State.PLAYING) {
                    newGame();
                    repaint();
                    return;
                }

                int mouseX = e.getX();
                int mouseY = e.getY();
                // Get the row and column clicked
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                        && board.cells[row][col].content == Seed.NO_SEED) {
                    currentState = board.stepGame(currentPlayer, row, col);

                    if (currentState == State.PLAYING) {
                        SoundEffect.EAT_FOOD.play();
                    } else {
                        SoundEffect.DIE.play();
                    }

                    repaint();

                    if (vsComputer && currentPlayer == Seed.CROSS && currentState == State.PLAYING) {
                        currentPlayer = Seed.NOUGHT;
                        Timer aiTimer = new Timer(300, evt -> {
                            computerMove();
                            repaint();
                        });
                        aiTimer.setRepeats(false);
                        aiTimer.start();
                    } else {
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    }
                }
            }
        });

        // Setup the status bar (JLabel) to display status message
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> {
            newGame();
            repaint();
        });

        JToggleButton soundToggle = new JToggleButton("Sound: ON");
        soundToggle.setFont(new Font("Arial", Font.BOLD, 14));
        soundToggle.setFocusPainted(false);
        soundToggle.addActionListener(e -> {
            if (soundToggle.isSelected()) {
                SoundEffect.volume = SoundEffect.Volume.MUTE;
                soundToggle.setText("Sound: OFF");
            } else {
                SoundEffect.volume = SoundEffect.Volume.LOW;
                soundToggle.setText("Sound: ON");
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(soundToggle, BorderLayout.WEST);
        bottomPanel.add(statusBar, BorderLayout.CENTER);
        bottomPanel.add(restartButton, BorderLayout.EAST);

        super.setLayout(new BorderLayout());
        super.add(bottomPanel, BorderLayout.PAGE_END);
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        // account for statusBar in height
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

        // Set up Game
        initGame();
        newGame();
    }

    /** Initialize the game (run once) */
    public void initGame() {
        board = new Board();  // allocate the game-board
    }

    /** Reset the game-board contents and the current-state, ready for new game */
    public void newGame() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED; // all cells empty
            }
        }
        currentPlayer = Seed.CROSS;    // cross plays first
        currentState = State.PLAYING;  // ready to play

        if (vsComputer && currentPlayer == Seed.NOUGHT) {
            computerMove();
        }
    }
    /** Komputer bermain secara otomatis */
    private void computerMove() {
        if (currentState != State.PLAYING) return;

        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    currentState = board.stepGame(currentPlayer, row, col);

                    if (currentState == State.PLAYING) {
                        SoundEffect.EAT_FOOD.play();
                    } else {
                        SoundEffect.DIE.play();
                    }

                    // Setelah komputer (NOUGHT) jalan, ganti ke CROSS
                    currentPlayer = Seed.CROSS;
                    return;
                }
            }
        }
    }
    /** Custom painting codes on this JPanel */
    @Override
    public void paintComponent(Graphics g) {  // Callback via repaint()
        super.paintComponent(g);
        setBackground(COLOR_BG); // set its background color

        board.paint(g);  // ask the game board to paint itself

        if (currentState == State.CROSS_WON || currentState == State.NOUGHT_WON || currentState == State.DRAW) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.setColor(Color.RED);
            String msg = (currentState == State.DRAW) ? "It's a Draw!" :
                    (currentState == State.CROSS_WON) ? "X Wins!" : "O Wins!";
            FontMetrics fm = g.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            int x = (Board.CANVAS_WIDTH - msgWidth) / 2;
            int y = Board.CANVAS_HEIGHT / 2;
            g.drawString(msg, x, y);
        }
        // Print status-bar message
        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText((currentPlayer == Seed.CROSS) ? "X's Turn" : "O's Turn");
        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("It's a Draw! Click to play again.");
        } else if (currentState == State.CROSS_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'X' Won! Click to play again.");
        } else if (currentState == State.NOUGHT_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'O' Won! Click to play again.");
        }
    }

    /** The entry "main" method */
    public static void main(String[] args) {
        // Run GUI construction codes in Event-Dispatching thread for thread safety
                SwingUtilities.invokeLater(() -> {
                    JFrame frame = new JFrame(TITLE);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(400, 450); // default size
                    frame.setLocationRelativeTo(null); // center

                    // Tampilkan Welcome Panel dulu
                    welcomePanel welcomePanel = new welcomePanel(frame);
                    frame.setContentPane(welcomePanel);

                    frame.setVisible(true);
                });
    }
}