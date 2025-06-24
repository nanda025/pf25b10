// GameMain.java
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final String TITLE = "Kelompok B10";
    public static final Color COLOR_BG = new Color(245, 245, 220);
    public static final Color COLOR_BG_STATUS = new Color(216, 216, 216);
    public static final Color COLOR_CROSS = new Color(255, 255, 0);
    public static final Color COLOR_NOUGHT = new Color(0, 128, 0);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);

    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private JLabel statusBar;
    private JLabel countdownLabel;
    private boolean vsComputer;
    private String aiLevel;
    private int turnTime;
    private int timeLeft;
    private Timer moveTimer;
    private boolean hasMovedThisTurn = false;
    private String skipMessage = null;
    private long skipMessageStartTime = 0;


    private boolean isOnlineMultiplayer = false;
    private String myUsername;
    private String onlineGameId;
    private boolean amIPlayer1Cross;
    private int lastFetchedMoveNumber = -1;
    private ScheduledExecutorService scheduler;
    private Image backgroundImage;

    public GameMain(boolean isVsAI, String aiDifficulty, int timePerTurn, String gameId, String username, Boolean amIPlayer1Cross)
    {
        this.vsComputer = isVsAI;
        this.aiLevel = (aiDifficulty != null) ? aiDifficulty.toLowerCase() : "none";
        this.turnTime = timePerTurn;
        this.onlineGameId = onlineGameId;
        this.myUsername = myUsername;
        this.amIPlayer1Cross = amIPlayer1Cross;

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentState != State.PLAYING) {
                    newGame();
                    repaint();
                    return;
                }

                if (isOnlineMultiplayer) {
                    Seed mySeed = amIPlayer1Cross ? Seed.CROSS : Seed.NOUGHT;
                    if (currentPlayer != mySeed) {
                        System.out.println("Bukan giliran Anda.");
                        return;
                    }
                }

                int mouseX = e.getX();
                int mouseY = e.getY();
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS &&
                        board.cells[row][col].content == Seed.NO_SEED) {

                    if (isOnlineMultiplayer) {
                        sendMoveToDatabase(row, col);
                        hasMovedThisTurn = true;
                        stopTimer();
                    } else {
                        currentState = board.stepGame(currentPlayer, row, col);
                        hasMovedThisTurn = true;
                        stopTimer();

                        if (currentState == State.PLAYING) {
                            SoundEffect.EAT_FOOD.play();
                        } else {
                            SoundEffect.DIE.play();
                        }

                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        repaint();

                        if (vsComputer && currentPlayer == Seed.NOUGHT && currentState == State.PLAYING) {
                            Timer aiTimer = new Timer(300, evt -> {
                                computerMove();
                                repaint();
                                if (currentState == State.PLAYING) {
                                    startTimer();
                                }
                            });
                            aiTimer.setRepeats(false);
                            aiTimer.start();
                        } else if (currentState == State.PLAYING) {
                            startTimer();
                        }
                    }
                }
            }
        });


        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        countdownLabel = new JLabel("Time: " + turnTime);
        countdownLabel.setFont(FONT_STATUS);
        countdownLabel.setBackground(COLOR_BG_STATUS);
        countdownLabel.setOpaque(true);
        countdownLabel.setPreferredSize(new Dimension(100, 30));
        countdownLabel.setHorizontalAlignment(JLabel.RIGHT);
        countdownLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(COLOR_BG_STATUS);
        infoPanel.add(countdownLabel, BorderLayout.EAST);

        super.add(infoPanel, BorderLayout.NORTH);

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

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(countdownLabel);
        rightPanel.add(restartButton);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        super.setLayout(new BorderLayout());
        try {
            backgroundImage = new ImageIcon(getClass().getResource("image/Background.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("Background image not found!");
            backgroundImage = null;
        }

        super.add(bottomPanel, BorderLayout.PAGE_END);
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

        initGame();
        newGame();
    }

    public void setOnlineMultiplayer(boolean isOnline, String username, boolean amIPlayer1) {
        this.isOnlineMultiplayer = isOnline;
        this.myUsername = username;
        this.amIPlayer1Cross = amIPlayer1;
        this.lastFetchedMoveNumber = -1;
    }

    public void initGame() {
        board = new Board();
    }

    public void newGame() {
        board.newGame();
        if (isOnlineMultiplayer) {
            currentState = State.PLAYING;
            currentPlayer = Seed.NO_SEED;
            lastFetchedMoveNumber = -1;
            stopPolling();

            if (amIPlayer1Cross) {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            DatabaseManager.clearGameMoves(onlineGameId);
                            System.out.println("Cleared old moves for game: " + onlineGameId);
                        } catch (SQLException | ClassNotFoundException ex) {
                            System.err.println("Error clearing game moves: " + ex.getMessage());
                        }
                        return null;
                    }
                    @Override
                    protected void done() {
                        pollForGameUpdates();
                    }
                }.execute();
            } else {
                pollForGameUpdates();
            }

        } else {
            currentPlayer = Seed.CROSS;
            currentState = State.PLAYING;
            if (vsComputer && currentPlayer == Seed.NOUGHT) {
                computerMove();
            }
        }
        repaint();
        startTimer();
    }

    private long startTime;

    private void startTimer() {
        stopTimer();
        hasMovedThisTurn = false;
        startTime = System.currentTimeMillis();
        timeLeft = turnTime;
        countdownLabel.setText("Time: " + timeLeft);

        moveTimer = new Timer(200, e -> {
            if (hasMovedThisTurn) {
                moveTimer.stop();
                return;
            }

            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            int remainingSeconds = turnTime - (int) elapsedSeconds;

            if (remainingSeconds != timeLeft) {
                timeLeft = remainingSeconds;
                countdownLabel.setText("Time: " + timeLeft);
            }
            if (remainingSeconds <= 0) {
                moveTimer.stop();
                if (!isOnlineMultiplayer) {
                    // Tampilkan pesan skip di tengah
                    skipMessage = "TIMES OUT!";
                    skipMessageStartTime = System.currentTimeMillis();

                    // Ganti pemain
                    currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    repaint();

                    Timer clearMessageTimer = new Timer(1500, evt -> {
                        skipMessage = null;
                        repaint();
                    });
                    clearMessageTimer.setRepeats(false);
                    clearMessageTimer.start();

                    if (vsComputer && currentPlayer == Seed.NOUGHT) {
                        Timer aiTimer = new Timer(300, evt -> {
                            computerMove();
                            repaint();
                            if (currentState == State.PLAYING) {
                                startTimer();
                            }
                        });
                        aiTimer.setRepeats(false);
                        aiTimer.start();
                    } else {
                        startTimer();
                    }
                }
            }

        });
        moveTimer.start();
    }

    private void stopTimer() {
        if (moveTimer != null && moveTimer.isRunning()) {
            moveTimer.stop();
        }
    }

    private void computerMove() {
        if (currentState != State.PLAYING) return;
        AI ai = new AI(board, currentPlayer, aiLevel);
        Point move = ai.getMove();

        if (move != null) {
            currentState = board.stepGame(currentPlayer, move.x, move.y);
            if (currentState == State.PLAYING) {
                SoundEffect.EAT_FOOD.play();
            } else {
                SoundEffect.DIE.play();
            }
            currentPlayer = Seed.CROSS;
        }
    }

    private void sendMoveToDatabase(int row, int col) {
        if (onlineGameId == null || myUsername == null) return;
        if (currentState != State.PLAYING) return;
        final Seed playerSeedEnum = amIPlayer1Cross ? Seed.CROSS : Seed.NOUGHT;
        final String playerSeedStr = playerSeedEnum.getDisplayName(); // "X" atau "O"
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {

                    List<DatabaseManager.Move> allCurrentMoves = DatabaseManager.fetchMoves(onlineGameId, -1);
                    int nextMoveNumber = 0;
                    if (!allCurrentMoves.isEmpty()) {
                        nextMoveNumber = allCurrentMoves.get(allCurrentMoves.size() - 1).moveNumber + 1;
                    }
                    DatabaseManager.insertMove(onlineGameId, nextMoveNumber, myUsername, playerSeedStr, row, col);
                    fetchNewMovesAndApply();
                    SoundEffect.EAT_FOOD.play();
                } catch (SQLException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(GameMain.this, "Error inserting move: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        }.execute();
    }

    public void pollForGameUpdates() {
        stopPolling();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::fetchNewMovesAndApply, 0, 1, TimeUnit.SECONDS);
    }
    private void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void fetchNewMovesAndApply() {
        if (onlineGameId == null) return;

        new SwingWorker<List<DatabaseManager.Move>, Void>() {
            @Override
            protected List<DatabaseManager.Move> doInBackground() throws Exception {
                try {
                    return DatabaseManager.fetchMoves(onlineGameId, lastFetchedMoveNumber);
                } catch (SQLException | ClassNotFoundException ex) {
                    throw new RuntimeException("Error fetching moves: " + ex.getMessage(), ex);
                }
            }

            @Override
            protected void done() {
                try {
                    List<DatabaseManager.Move> newMoves = get();
                    if (!newMoves.isEmpty()) {
                        System.out.println("Fetched " + newMoves.size() + " new moves.");
                        for (DatabaseManager.Move move : newMoves) {
                            if (move.moveNumber > lastFetchedMoveNumber) {
                                Seed seed = (move.playerSeed.equals("X")) ? Seed.CROSS : Seed.NOUGHT;
                                if (board.cells[move.row][move.col].content == Seed.NO_SEED) {
                                    currentState = board.stepGame(seed, move.row, move.col);
                                    lastFetchedMoveNumber = move.moveNumber;
                                } else {
                                    System.err.println("DatabaseManager: Menerima gerakan ke cell yang sudah terisi! (" + move.row + "," + move.col + ")");
                                }
                            }
                        }
                        if (!newMoves.isEmpty()) {
                            DatabaseManager.Move lastMove = newMoves.get(newMoves.size() - 1);
                            if (lastMove.playerSeed.equals("X")) {
                                currentPlayer = Seed.NOUGHT;
                            } else {
                                currentPlayer = Seed.CROSS;
                            }
                        } else if (lastFetchedMoveNumber == -1) {
                            currentPlayer = Seed.CROSS;
                        }

                        repaint();
                        startTimer();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Error processing fetched moves: " + ex.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Custom painting codes on this JPanel
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            setBackground(COLOR_BG);
        }
        board.paint(g);

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
            stopPolling();
        }
        if (isOnlineMultiplayer) {
            statusBar.setForeground(Color.BLACK);
            if (currentState == State.PLAYING) {
                if (currentPlayer == Seed.NO_SEED) {
                    statusBar.setText("Waiting for initial game state...");
                } else if ((amIPlayer1Cross && currentPlayer == Seed.CROSS) || (!amIPlayer1Cross && currentPlayer == Seed.NOUGHT)) {
                    statusBar.setText("Giliran Anda! (" + myUsername + " - " + currentPlayer.getDisplayName() + ")");
                } else {
                    statusBar.setText("Menunggu lawan (" + currentPlayer.getDisplayName() + " turn)");
                }
            } else if (currentState == State.DRAW) {
                statusBar.setForeground(Color.RED);
                statusBar.setText("Seri! Klik Restart.");
            } else if (currentState == State.CROSS_WON) {
                statusBar.setForeground(Color.RED);
                statusBar.setText("'X' Menang! Klik Restart.");
            } else if (currentState == State.NOUGHT_WON) {
                statusBar.setForeground(Color.RED);
                statusBar.setText("'O' Menang! Klik Restart.");
            }
        } else {
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

            if (skipMessage != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                int msgWidth = fm.stringWidth(skipMessage);
                int msgHeight = fm.getHeight();

                int padding = 12;
                int boxWidth = msgWidth + 2 * padding;
                int boxHeight = msgHeight + padding;

                int x = (getWidth() - boxWidth) / 2;
                int y = (getHeight() - boxHeight) / 2;

                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);

                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);

                int textX = x + padding;
                int textY = y + padding + fm.getAscent() - 4;
                g2d.drawString(skipMessage, textX, textY);
            }
        }
    }

    /**
     * The entry "main" method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 450); // default size
            frame.setLocationRelativeTo(null); // center

            welcomePanel welcomePanel = new welcomePanel(frame);
            frame.setContentPane(welcomePanel);

            frame.setVisible(true);
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // Ketika komponen dihapus dari hirarki, ini dipanggil
        // Hentikan scheduler untuk menghindari memory leak atau thread yang berjalan di latar belakang
        stopPolling();
    }
}