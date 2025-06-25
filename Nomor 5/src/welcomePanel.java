import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.net.URL;

public class welcomePanel extends JPanel {
    private JFrame parentFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Image backgroundImage;
    private Clip clip;  // Untuk sound

    public welcomePanel(JFrame frame) {
        this.parentFrame = frame;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        // Load background image
        try {
            this.backgroundImage = new ImageIcon(getClass().getResource("/image/BG-Awal.gif")).getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            this.backgroundImage = null;
        }

        setPreferredSize(new Dimension(420, 650));

        // Tambah panel ke cardPanel
        cardPanel.add(createMainMenu(), "MAIN_MENU");

        // Masukkan ModeSelectionPanel
        ModeSelectionPanel modeSelectionPanel = new ModeSelectionPanel(parentFrame, cardLayout, cardPanel);
        cardPanel.add(modeSelectionPanel, "GAME_SETUP");

        // Panel petunjuk
        cardPanel.add(createInstructionsPanel(), "INSTRUCTIONS");

        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "MAIN_MENU");

        // Play music
        playSound("/audio/toy-story.wav");
    }

    private JPanel createMainMenu() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(173, 216, 230));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));

        JButton startButton = new JButton("MULAI PERMAINAN");
        JButton instructionButton = new JButton("PETUNJUK GAME");
        JButton exitButton = new JButton("KELUAR");

        Dimension buttonSize = new Dimension(180, 45);
        Font buttonFont = new Font("Comic Sans MS", Font.BOLD, 15);
        JButton[] buttons = {startButton, instructionButton, exitButton};

        for (JButton b : buttons) {
            b.setPreferredSize(buttonSize);
            b.setFont(buttonFont);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        // Styling
        startButton.setBackground(new Color(59, 89, 182));
        startButton.setForeground(Color.WHITE);
        instructionButton.setBackground(new Color(76, 175, 80));
        instructionButton.setForeground(Color.WHITE);
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);

        // Hover effect
        startButton.addMouseListener(new HoverEffect(startButton, new Color(80, 120, 200), new Color(59, 89, 182)));
        instructionButton.addMouseListener(new HoverEffect(instructionButton, new Color(100, 190, 100), new Color(76, 175, 80)));
        exitButton.addMouseListener(new HoverEffect(exitButton, new Color(255, 100, 100), Color.RED));

        // Button actions (stop sound when clicked)
        startButton.addActionListener(e -> {
            stopSound();
            cardLayout.show(cardPanel, "GAME_SETUP");
        });

        instructionButton.addActionListener(e -> {
            stopSound();
            cardLayout.show(cardPanel, "INSTRUCTIONS");
        });

        exitButton.addActionListener(e -> {
            stopSound();
            System.exit(0);
        });

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(instructionButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(173, 216, 230));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        panel.setOpaque(false);

        JTextArea instructions = new JTextArea("""
                --- Petunjuk Permainan ---

                1. Tic Tac Toe dimainkan oleh dua pemain.
                2. Pemain bergiliran menempatkan X dan O pada papan 3x3.
                3. Pemain pertama yang membuat garis horizontal, vertikal, atau diagonal menang.
                4. Jika semua kotak terisi dan tidak ada pemenang, maka permainan berakhir seri.
                5. Terdapat timer yang telah disediakan, apabila melewati batas waktu maka akan dilanjutkan oleh pemain berikutnya.
                6. Untuk mode AI, Anda akan bermain melawan komputer.
                7. Untuk multiplayer lokal, masukkan nama pemain sebelum memulai.

                Selamat bermain!
                """);
        instructions.setEditable(false);
        instructions.setFont(new Font("Monospaced", Font.PLAIN, 14));
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setOpaque(true);
        instructions.setBackground(new Color(255, 255, 255));

        JScrollPane scrollPane = new JScrollPane(instructions);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel whiteBox = new JPanel(new BorderLayout());
        whiteBox.setBackground(new Color(255, 255, 255, 230));
        whiteBox.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        whiteBox.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali");
        backButton.setPreferredSize(new Dimension(100, 30));
        backButton.addMouseListener(new HoverEffect(backButton, new Color(200, 200, 200), UIManager.getColor("Button.background")));
        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "MAIN_MENU");
            playSound("/audio/toy-story.wav");
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);
        panel.add(whiteBox, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }
    private void playSound(String path) {
        try {
            stopSound();
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSound() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
    private static class HoverEffect extends MouseAdapter {
        private final JButton button;
        private final Color hoverColor, normalColor;

        public HoverEffect(JButton button, Color hoverColor, Color normalColor) {
            this.button = button;
            this.hoverColor = hoverColor;
            this.normalColor = normalColor;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            button.setBackground(hoverColor);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(normalColor);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tic Tac Toe Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(420, 650);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);

            welcomePanel welcome = new welcomePanel(frame);
            frame.setContentPane(welcome);
            frame.setVisible(true);
        });
    }
}
