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

    // --- VARIABEL BARU UNTUK MULTIPLAYER ONLINE ---
    private boolean isOnlineMultiplayer = false;
    private String myUsername; // Username pemain saya
    private String onlineGameId; // ID game online yang sedang dimainkan
    private boolean amIPlayer1Cross; // true jika saya player X (CROSS), false jika player O (NOUGHT)
    private int lastFetchedMoveNumber = -1; // Untuk melacak move terakhir yang diambil dari DB
    private ScheduledExecutorService scheduler; // Untuk polling
    private Image backgroundImage;


    // Konstruktor baru dengan parameter multiplayer
    public GameMain(boolean isVsAI, String aiDifficulty, int timePerTurn, String gameId, String username, Boolean amIPlayer1Cross)
    {
        this.vsComputer = vsComputer;
        this.aiLevel = (aiLevel != null) ? aiLevel.toLowerCase() : "none";
        this.turnTime = turnTime;
        this.onlineGameId = onlineGameId;
        this.myUsername = myUsername;
        this.amIPlayer1Cross = amIPlayer1Cross;

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Jika game sudah selesai, klik akan memulai game baru (baik lokal maupun online)
                if (currentState != State.PLAYING) {
                    newGame(); // Reset game
                    repaint();
                    return;
                }

                if (isOnlineMultiplayer) {
                    // Dalam mode online, hanya izinkan klik jika giliran saya
                    Seed mySeed = amIPlayer1Cross ? Seed.CROSS : Seed.NOUGHT;
                    if (currentPlayer != mySeed) {
                        System.out.println("Bukan giliran Anda.");
                        return; // Bukan giliran pemain ini
                    }
                }

                int mouseX = e.getX();
                int mouseY = e.getY();
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS &&
                        board.cells[row][col].content == Seed.NO_SEED) {

                    if (isOnlineMultiplayer) {
                        // Untuk Multiplayer Online: Masukkan gerakan ke database
                        sendMoveToDatabase(row, col);
                    } else {
                        // Untuk Mode Lokal / AI: Lakukan gerakan secara lokal
                        currentState = board.stepGame(currentPlayer, row, col);

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
                            });
                            aiTimer.setRepeats(false);
                            aiTimer.start();
                        }
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

        countdownLabel = new JLabel("Time: " + turnTime);
        countdownLabel.setFont(FONT_STATUS);
        countdownLabel.setBackground(COLOR_BG_STATUS);
        countdownLabel.setOpaque(true);
        countdownLabel.setPreferredSize(new Dimension(100, 30));
        countdownLabel.setHorizontalAlignment(JLabel.RIGHT);
        countdownLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

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
        // Load background image
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
        newGame(); // Panggil newGame untuk setup awal
    }

    // Metode baru untuk mengatur mode multiplayer online
    public void setOnlineMultiplayer(boolean isOnline, String username, boolean amIPlayer1) {
        this.isOnlineMultiplayer = isOnline;
        this.myUsername = username;
        this.amIPlayer1Cross = amIPlayer1;
        // Inisialisasi lastFetchedMoveNumber jika ini adalah game baru atau join
        this.lastFetchedMoveNumber = -1; // Akan diperbarui setelah fetch pertama
    }

    public void initGame() {
        board = new Board();
    }

    public void newGame() {
        board.newGame();
        if (isOnlineMultiplayer) {
            // Dalam mode online, status game dan giliran ditentukan oleh gerakan di DB
            currentState = State.PLAYING;
            currentPlayer = Seed.NO_SEED; // Sementara, akan di-override oleh polling
            lastFetchedMoveNumber = -1; // Reset untuk game baru
            stopPolling(); // Hentikan scheduler lama jika ada

            // Clear moves for this game in DB if creating a new one (only by the creator, otherwise just fetch)
            if (amIPlayer1Cross) { // Asumsi hanya player X yang bisa mereset game ini
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            DatabaseManager.clearGameMoves(onlineGameId);
                            System.out.println("Cleared old moves for game: " + onlineGameId);
                        } catch (SQLException | ClassNotFoundException ex) {
                            System.err.println("Error clearing game moves: " + ex.getMessage());
                            // Handle error, maybe show a message
                        }
                        return null;
                    }
                    @Override
                    protected void done() {
                        pollForGameUpdates(); // Mulai polling setelah board dibersihkan
                    }
                }.execute();
            } else {
                pollForGameUpdates(); // Player O hanya langsung polling
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

    private void startTimer() {
        stopTimer();
        timeLeft = turnTime;
        countdownLabel.setText("Time: " + timeLeft);

        moveTimer = new Timer(1000, e -> {
            timeLeft--;
            countdownLabel.setText("Time: " + timeLeft);

            if (timeLeft <= 0) {
                moveTimer.stop();
                if (!isOnlineMultiplayer) { // Hanya jika mode lokal
                    currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                }
                // Di mode online, pergantian giliran dikelola oleh update dari DB,
                // bukan oleh timer lokal ini. Timer ini hanya untuk hitung mundur giliran
                // jika giliran adalah milik kita.
                repaint();
                startTimer();
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

    // --- METODE BARU UNTUK KOMUNIKASI DENGAN DATABASE ---

    // Mengirim gerakan ke database
    private void sendMoveToDatabase(int row, int col) {
        if (onlineGameId == null || myUsername == null) return;
        if (currentState != State.PLAYING) return; // Jangan kirim gerakan jika game sudah selesai

        final Seed playerSeedEnum = amIPlayer1Cross ? Seed.CROSS : Seed.NOUGHT;
        final String playerSeedStr = playerSeedEnum.getDisplayName(); // "X" atau "O"

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Cari moveNumber berikutnya. Ini adalah masalah umum dalam JDBC langsung.
                    // Cara sederhana: ambil move terakhir + 1. Ini rentan race condition.
                    // Cara lebih baik (tapi lebih kompleks): gunakan transaction di sisi server,
                    // atau AtomicInteger di server jika ada single point of entry.
                    // Untuk demo ini, kita akan coba ambil dari DB dulu.
                    List<DatabaseManager.Move> allCurrentMoves = DatabaseManager.fetchMoves(onlineGameId, -1);
                    int nextMoveNumber = 0;
                    if (!allCurrentMoves.isEmpty()) {
                        nextMoveNumber = allCurrentMoves.get(allCurrentMoves.size() - 1).moveNumber + 1;
                    }

                    DatabaseManager.insertMove(onlineGameId, nextMoveNumber, myUsername, playerSeedStr, row, col);
                    // Setelah insert, paksa fetch update untuk segera memperbarui board lokal
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

    // Memulai polling untuk update game dari database
    public void pollForGameUpdates() {
        stopPolling(); // Pastikan tidak ada polling ganda
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Poll setiap 1 detik. Sesuaikan interval sesuai kebutuhan.
        scheduler.scheduleAtFixedRate(this::fetchNewMovesAndApply, 0, 1, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    // Mengambil gerakan baru dari database dan menerapkannya ke board
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
                        // Terapkan setiap gerakan baru ke papan
                        for (DatabaseManager.Move move : newMoves) {
                            if (move.moveNumber > lastFetchedMoveNumber) {
                                Seed seed = (move.playerSeed.equals("X")) ? Seed.CROSS : Seed.NOUGHT;
                                // PENTING: Gunakan board.stepGame untuk menerapkan gerakan
                                // dan cek status game.
                                // Kita harus memastikan urutan gerakan benar.
                                // Cek board.cells[move.row][move.col].content == Seed.NO_SEED
                                // mungkin tidak perlu jika move_number dijamin berurutan dan valid.
                                if (board.cells[move.row][move.col].content == Seed.NO_SEED) {
                                    currentState = board.stepGame(seed, move.row, move.col);
                                    // Update lastFetchedMoveNumber
                                    lastFetchedMoveNumber = move.moveNumber;
                                } else {
                                    System.err.println("DatabaseManager: Menerima gerakan ke cell yang sudah terisi! (" + move.row + "," + move.col + ")");
                                }
                            }
                        }
                        // Setelah menerapkan semua gerakan, tentukan giliran berikutnya
                        // Giliran adalah milik pemain yang TIDAK membuat gerakan terakhir
                        if (!newMoves.isEmpty()) {
                            DatabaseManager.Move lastMove = newMoves.get(newMoves.size() - 1);
                            if (lastMove.playerSeed.equals("X")) { // Jika X yang terakhir bergerak
                                currentPlayer = Seed.NOUGHT; // Giliran O
                            } else { // Jika O yang terakhir bergerak
                                currentPlayer = Seed.CROSS; // Giliran X
                            }
                        } else if (lastFetchedMoveNumber == -1) { // Jika belum ada gerakan sama sekali
                            currentPlayer = Seed.CROSS; // Default Player X (CROSS) mulai duluan
                        }


//                        // Mainkan suara jika game masih bermain setelah gerakan baru
//                        if (currentState == State.PLAYING) {
//                            // SoundEffect.EAT_FOOD.play(); // Bisa diputar jika ada gerakan baru
//                        } else {
//                            SoundEffect.DIE.play(); // Sound jika game selesai
//                            stopPolling(); // Hentikan polling jika game selesai
//                        }

                        repaint(); // Perbarui tampilan
                        startTimer(); // Reset timer untuk giliran baru
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Error processing fetched moves: " + ex.getMessage());
                    // stopPolling(); // Bisa hentikan polling jika error parah
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

        // ✅ Gambar background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            setBackground(COLOR_BG); // fallback warna background jika gambar gagal load
        }

        // Gambar board
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
            stopPolling(); // Hentikan polling jika game selesai
        }
        // Print status-bar message
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
            // Logika status bar untuk mode lokal (sudah ada)
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
    }

    /**
     * The entry "main" method
     */
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

    // Pastikan untuk menghentikan scheduler saat aplikasi ditutup
    @Override
    public void addNotify() {
        super.addNotify();
        // Ketika komponen ditambahkan ke hirarki, ini dipanggil
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // Ketika komponen dihapus dari hirarki, ini dipanggil
        // Hentikan scheduler untuk menghindari memory leak atau thread yang berjalan di latar belakang
        stopPolling();
    }
}