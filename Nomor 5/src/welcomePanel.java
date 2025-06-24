import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class welcomePanel extends JPanel {
    private JFrame parentFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Image backgroundImage;

    public welcomePanel(JFrame frame) {
        this.parentFrame = frame;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.backgroundImage = new ImageIcon(getClass().getResource("/image/Background3.jpg")).getImage();

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
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JButton startButton = new JButton("Mulai Permainan");
        JButton instructionButton = new JButton("Petunjuk Game");
        JButton exitButton = new JButton("Keluar");

        startButton.setPreferredSize(new Dimension(180, 35));
        instructionButton.setPreferredSize(new Dimension(180, 35));
        exitButton.setPreferredSize(new Dimension(180, 35));

        startButton.setBackground(new Color(59, 89, 182));
        instructionButton.setBackground(new Color(76, 175, 80));
        instructionButton.setForeground(Color.WHITE);
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);

        gbc.gridy++;
        panel.add(startButton, gbc);
        gbc.gridy++;
        panel.add(instructionButton, gbc);
        gbc.gridy++;
        panel.add(exitButton, gbc);

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
                5. Terdapat timer yang telah disediakan, apabila melewati batas waktu maka akan dilanjutkan oleh pemain selanjutnya
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
        whiteBox.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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