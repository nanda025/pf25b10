import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Font;

public class welcomePanel extends JPanel {
    private JFrame parentFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Image backgroundImage;

    public welcomePanel(JFrame frame) {
        this.parentFrame = frame;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.backgroundImage = new ImageIcon(getClass().getResource("/image/Background4.gif")).getImage();

        setPreferredSize(new Dimension(420, 650));

        cardPanel.add(createMainMenu(), "MAIN_MENU");
        cardPanel.add(createGameSetupPanel(), "GAME_SETUP");
        cardPanel.add(createInstructionsPanel(), "INSTRUCTIONS");

        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createMainMenu() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));

        JButton startButton = new JButton("Mulai Permainan");
        JButton instructionButton = new JButton("Petunjuk Game");
        JButton exitButton = new JButton("Keluar");

        Dimension buttonSize = new Dimension(180, 35);
        startButton.setPreferredSize(buttonSize);
        instructionButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);

        Font buttonFont = new Font("Goudy Stout", Font.PLAIN, 10);
        startButton.setFont(buttonFont);
        instructionButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        // Mulai Permainan - warna putih
        startButton.setBackground(new Color(59, 89, 182)); // Biru gelap
        startButton.setForeground(Color.WHITE);

        // Petunjuk Game - hijau
        instructionButton.setBackground(new Color(76, 175, 80));
        instructionButton.setForeground(Color.WHITE);

        // Keluar - merah
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);

        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(instructionButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());

        panel.add(buttonPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> cardLayout.show(cardPanel, "GAME_SETUP"));
        instructionButton.addActionListener(e -> cardLayout.show(cardPanel, "INSTRUCTIONS"));
        exitButton.addActionListener(e -> System.exit(0));

        return panel;
    }

    private JPanel createGameSetupPanel() {
        return new ModeSelectionPanel(parentFrame, cardLayout, cardPanel); // Ini class dari sebelumnya
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setOpaque(false);

        JTextArea instructions = new JTextArea();
        instructions.setText("""
                --- Petunjuk Permainan ---

                1. Tic Tac Toe dimainkan oleh dua pemain.
                2. Pemain bergiliran menempatkan X dan O pada papan 3x3.
                3. Pemain pertama yang membuat garis horizontal, vertikal, atau diagonal menang.
                4. Jika semua kotak terisi dan tidak ada pemenang, maka permainan berakhir seri.
                5. Terdapat timer yang telah disediakan, apabila melewati batas waktu maka akan dilanjutkan oleh pemain berikutnya
                6. Untuk mode AI, Anda akan bermain melawan komputer.
                7. Untuk multiplayer online, masukkan ID Game dan nama pengguna Anda.

                Selamat bermain!
                """);
        instructions.setEditable(false);
        instructions.setFont(new Font("Monospaced", Font.PLAIN, 14));
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setForeground(Color.BLACK);
        instructions.setBackground(new Color(255, 255, 255)); // solid white
        instructions.setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(instructions);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel whiteBox = new JPanel(new BorderLayout());
        whiteBox.setBackground(new Color(255, 255, 255, 230)); // semi-transparent
        whiteBox.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        whiteBox.add(scrollPane, BorderLayout.CENTER);

        // tombol kembali
        JButton backButton = new JButton("Kembali");
        backButton.setPreferredSize(new Dimension(100, 30));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MAIN_MENU"));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);

        panel.add(whiteBox, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }
}
