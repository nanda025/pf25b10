import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;

public class GameMain extends JPanel {
    public static final String TITLE = "Kelompok B10";
    public static final Color COLOR_BG = new Color(245, 245, 220);
    public static final Color COLOR_BG_STATUS = new Color(216, 216, 216);
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
    private boolean soundPlayed = false;

    private Image backgroundImage;
    private String player1Name;
    private String player2Name;

    public GameMain(boolean isVsAI, String aiDifficulty, int timePerTurn, String player1Name, String player2Name) {
        this.vsComputer = isVsAI;
        this.aiLevel = (aiDifficulty != null) ? aiDifficulty.toLowerCase() : "none";
        this.turnTime = timePerTurn;
        this.player1Name = player1Name != null ? player1Name : "Player 1";
        this.player2Name = player2Name != null ? player2Name : "Player 2";

        super.setLayout(new BorderLayout());

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                playClickSound();
                if (currentState != State.PLAYING) {
                    newGame();
                    repaint();
                    return;
                }
                int mouseX = e.getX();
                int mouseY = e.getY();
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS &&
                        board.cells[row][col].content == Seed.NO_SEED) {
                    currentState = board.stepGame(currentPlayer, row, col);
                    hasMovedThisTurn = true;
                    stopTimer();
                    currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    repaint();

                    if (vsComputer && currentPlayer == Seed.NOUGHT && currentState == State.PLAYING) {
                        Timer aiTimer = new Timer(300, evt -> {
                            computerMove();
                            repaint();
                            if (currentState == State.PLAYING) startTimer();
                        });
                        aiTimer.setRepeats(false);
                        aiTimer.start();
                    } else if (currentState == State.PLAYING) {
                        startTimer();
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

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusBar, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(countdownLabel);
        rightPanel.add(restartButton);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        super.add(bottomPanel, BorderLayout.PAGE_END);
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

        try {
            backgroundImage = new ImageIcon(getClass().getResource("/image/Background.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("Background image not found!");
            backgroundImage = null;
        }
        initGame();
        newGame();
    }
    private void initGame() {
        board = new Board();
    }
    private void newGame() {
        board.newGame();
        currentPlayer = Seed.CROSS;
        currentState = State.PLAYING;
        soundPlayed = false;
        repaint();
        startTimer();
    }
    private void startTimer() {
        stopTimer();
        hasMovedThisTurn = false;
        long startTime = System.currentTimeMillis();
        timeLeft = turnTime;
        countdownLabel.setText("Time: " + timeLeft);

        moveTimer = new Timer(200, e -> {
            if (hasMovedThisTurn) moveTimer.stop();
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            int remaining = turnTime - (int) elapsedSeconds;
            if (remaining != timeLeft) {
                timeLeft = remaining;
                countdownLabel.setText("Time: " + timeLeft);
            }
            if (remaining <= 0) {
                moveTimer.stop();
                skipMessage = "TIME OUT!";
                currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                repaint();
                Timer clearMsg = new Timer(1500, evt -> {
                    skipMessage = null;
                    repaint();
                });
                clearMsg.setRepeats(false);
                clearMsg.start();
                startTimer();
            }
        });
        moveTimer.start();
    }
    private void stopTimer() {
        if (moveTimer != null && moveTimer.isRunning()) moveTimer.stop();
    }
    private void computerMove() {
        if (currentState != State.PLAYING) return;
        AI ai = new AI(board, currentPlayer, aiLevel);
        Point move = ai.getMove();
        if (move != null) {
            currentState = board.stepGame(currentPlayer, move.x, move.y);
            currentPlayer = Seed.CROSS;
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            setBackground(COLOR_BG);
        }

        board.paint(g);

        if ((currentState == State.CROSS_WON || currentState == State.NOUGHT_WON) && !soundPlayed) {
            boolean isPlayer1Win = (currentState == State.CROSS_WON && currentPlayer == Seed.NOUGHT) ||
                    (currentState == State.NOUGHT_WON && currentPlayer == Seed.CROSS);
            playWinOrLoseSound(isPlayer1Win);
            soundPlayed = true;
        }
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
        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            String name = (currentPlayer == Seed.CROSS) ? player1Name : player2Name;
            statusBar.setText("Giliran " + name + " (" + currentPlayer.getDisplayName() + ")");
        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("Seri! Klik Restart.");
        } else {
            String winner = (currentState == State.CROSS_WON) ? player1Name : player2Name;
            statusBar.setForeground(Color.RED);
            statusBar.setText(winner + " Menang! Klik Restart.");
        }
        if (skipMessage != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(skipMessage);
            int boxWidth = msgWidth + 24;
            int boxHeight = fm.getHeight() + 12;
            int x = (getWidth() - boxWidth) / 2;
            int y = (getHeight() - boxHeight) / 2;
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);
            g2d.drawString(skipMessage, x + 12, y + fm.getAscent() + 6);
        }
    }
    private void playClickSound() {
        playSound("click.wav");
    }
    private void playWinOrLoseSound(boolean isWin) {
        if (isWin) {
            playSound("menang.wav");
        } else {
            playSound("kalah.wav");
        }
    }
    private void playSound(String fileName) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/audio/" + fileName));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Sound error: " + e.getMessage());
        }
    }
}