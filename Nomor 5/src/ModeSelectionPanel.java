import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

public class ModeSelectionPanel extends JPanel {
    private JFrame parentFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTextField usernameField;
    private JTextField gameIdField;
    private JComboBox<String> difficultyBox;
    private JTextField player1Field;
    private JTextField player2Field;
    private JLabel player1Label;
    private JLabel player2Label;
    private JComboBox<String> timerBox;
    private JRadioButton twoPlayerLocal, vsAI, onlineMultiplayer;
    private Image backgroundImage;

    public ModeSelectionPanel(JFrame frame, CardLayout layout, JPanel panel) {
        this.parentFrame = frame;
        this.cardLayout = layout;
        this.cardPanel = panel;
        this.backgroundImage = new ImageIcon(getClass().getResource("/image/Background2.jpg")).getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Pilih Mode Permainan");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        twoPlayerLocal = new JRadioButton("2 Pemain (Lokal)");
        vsAI = new JRadioButton("vs AI");
        onlineMultiplayer = new JRadioButton("Multiplayer Online");

        ButtonGroup group = new ButtonGroup();
        group.add(twoPlayerLocal);
        group.add(vsAI);
        group.add(onlineMultiplayer);

        twoPlayerLocal.setSelected(true);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        add(twoPlayerLocal, gbc);
        gbc.gridx = 1;
        add(vsAI, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(onlineMultiplayer, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel difficultyLabel = new JLabel("Tingkat AI:");
        difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyLabel.setEnabled(false);
        difficultyBox.setEnabled(false);
        add(difficultyLabel, gbc);
        gbc.gridx = 1;
        add(difficultyBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(10);
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        add(usernameLabel, gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel gameIdLabel = new JLabel("Game ID (Kosongkan untuk baru):");
        gameIdField = new JTextField(10);
        gameIdLabel.setEnabled(false);
        gameIdField.setEnabled(false);
        add(gameIdLabel, gbc);
        gbc.gridx = 1;
        add(gameIdField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel timerLabel = new JLabel("Waktu per Giliran (detik):");
        timerBox = new JComboBox<>(new String[]{"5", "10", "15", "30"});
        timerBox.setSelectedIndex(1);
        add(timerLabel, gbc);
        gbc.gridx = 1;
        add(timerBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        player1Label = new JLabel("Nama Pemain 1:");
        player1Field = new JTextField(10);
        add(player1Label, gbc);
        gbc.gridx = 1;
        add(player1Field, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        player2Label = new JLabel("Nama Pemain 2:");
        player2Field = new JTextField(10);
        add(player2Label, gbc);
        gbc.gridx = 1;
        add(player2Field, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton startButton = new JButton("Mulai Game");
        add(startButton, gbc);

        gbc.gridy++;
        JButton backButton = new JButton("Kembali");
        add(backButton, gbc);

        // Action Listeners
        ActionListener modeListener = e -> {
            boolean isVsAI = vsAI.isSelected();
            boolean isOnline = onlineMultiplayer.isSelected();
            boolean isTwoPlayer = twoPlayerLocal.isSelected();
            difficultyLabel.setEnabled(isVsAI);
            difficultyBox.setEnabled(isVsAI);
            usernameLabel.setEnabled(isOnline);
            usernameField.setEnabled(isOnline);
            gameIdLabel.setEnabled(isOnline);
            gameIdField.setEnabled(isOnline);
            player1Label.setVisible(isTwoPlayer);
            player1Field.setVisible(isTwoPlayer);
            player2Label.setVisible(isTwoPlayer);
            player2Field.setVisible(isTwoPlayer);
        };
        twoPlayerLocal.addActionListener(modeListener);
        vsAI.addActionListener(modeListener);
        onlineMultiplayer.addActionListener(modeListener);

        startButton.addActionListener(e -> {
            int time = Integer.parseInt((String) timerBox.getSelectedItem());

            if (twoPlayerLocal.isSelected()) {
                String p1 = player1Field.getText().trim();
                String p2 = player2Field.getText().trim();
                if (p1.isEmpty() || p2.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan nama kedua pemain.");
                    return;
                }
                GameMain game = new GameMain(false, "None", time, null, null, true, p1, p2);
                parentFrame.setContentPane(game);
                parentFrame.revalidate();
            } else if (vsAI.isSelected()) {
                String difficulty = (String) difficultyBox.getSelectedItem();
                GameMain game = new GameMain(true, difficulty, time, null, null, true, null, null);
                parentFrame.setContentPane(game);
                parentFrame.revalidate();
            } else {
                String username = usernameField.getText().trim();
                String id = gameIdField.getText().trim();

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan username.");
                    return;
                }
                if (id.isEmpty()) {
                    id = UUID.randomUUID().toString();
                    JOptionPane.showMessageDialog(this, "Game dibuat! ID: " + id);
                    startOnlineGame(id, username, time, true);
                } else {
                    JOptionPane.showMessageDialog(this, "Bergabung ke game ID: " + id);
                    startOnlineGame(id, username, time, false);
                }
            }
        });

        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MAIN_MENU"));
    }

    private void startOnlineGame(String id, String username, int time, boolean isPlayer1) {
        GameMain game = new GameMain(false, "None", time, id, username, isPlayer1, null, null);
        game.setOnlineMultiplayer(true, username, isPlayer1);
        parentFrame.setContentPane(game);
        parentFrame.revalidate();
        parentFrame.repaint();
        game.pollForGameUpdates();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}

