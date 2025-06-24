import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

public class welcomePanel extends JPanel {
    private JFrame parentFrame;
    private JTextField gameIdField;
    private JTextField usernameField;
    private JRadioButton onlineMultiplayer;
    private JRadioButton twoPlayerLocal;
    private JRadioButton vsAI;
    private Image backgroundImage;

    public welcomePanel(JFrame frame) {
        this.parentFrame = frame;
        this.backgroundImage = new ImageIcon(getClass().getResource("/image/Background2.jpg")).getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Pilih Mode Permainan");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        twoPlayerLocal = new JRadioButton("2 Player (Lokal)");
        vsAI = new JRadioButton("Lawan AI");
        onlineMultiplayer = new JRadioButton("Online Multiplayer");

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(twoPlayerLocal);
        modeGroup.add(vsAI);
        modeGroup.add(onlineMultiplayer);
        twoPlayerLocal.setSelected(true);

        gbc.gridx = 0; add(twoPlayerLocal, gbc);
        gbc.gridx = 1; add(vsAI, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(onlineMultiplayer, gbc);

        gbc.gridy++;
        JLabel difficultyLabel = new JLabel("Tingkat Kesulitan AI:");
        JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyLabel.setEnabled(false);
        difficultyBox.setEnabled(false);
        gbc.gridx = 0; add(difficultyLabel, gbc);
        gbc.gridx = 1; add(difficultyBox, gbc);

        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(10);
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        gbc.gridx = 0; add(usernameLabel, gbc);
        gbc.gridx = 1; add(usernameField, gbc);

        gbc.gridy++;
        JLabel gameIdLabel = new JLabel("Game ID:");
        gameIdField = new JTextField(15);
        gameIdLabel.setEnabled(false);
        gameIdField.setEnabled(false);
        gbc.gridx = 0; add(gameIdLabel, gbc);
        gbc.gridx = 1; add(gameIdField, gbc);

        ActionListener modeListener = e -> {
            boolean isVsAI = vsAI.isSelected();
            boolean isOnline = onlineMultiplayer.isSelected();

            difficultyLabel.setEnabled(isVsAI);
            difficultyBox.setEnabled(isVsAI);
            usernameLabel.setEnabled(isOnline);
            usernameField.setEnabled(isOnline);
            gameIdLabel.setEnabled(isOnline);
            gameIdField.setEnabled(isOnline);
        };

        twoPlayerLocal.addActionListener(modeListener);
        vsAI.addActionListener(modeListener);
        onlineMultiplayer.addActionListener(modeListener);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel timerLabel = new JLabel("Waktu per Giliran (detik):");
        JComboBox<String> timerBox = new JComboBox<>(new String[]{"5", "10", "15", "30"});
        timerBox.setSelectedIndex(1);
        add(timerLabel, gbc);
        gbc.gridx = 1;
        add(timerBox, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton playButton = new JButton("Mulai");
        playButton.setFont(new Font("Arial", Font.BOLD, 14));
        add(playButton, gbc);

        playButton.addActionListener(e -> {
            int selectedTime = Integer.parseInt((String) timerBox.getSelectedItem());

            if (twoPlayerLocal.isSelected()) {
                GameMain gamePanel = new GameMain(false, "None", selectedTime, null, null, true);
                parentFrame.setContentPane(gamePanel);
            } else if (vsAI.isSelected()) {
                String selectedDifficulty = (String) difficultyBox.getSelectedItem();
                GameMain gamePanel = new GameMain(true, selectedDifficulty, selectedTime, null, null, true);
                parentFrame.setContentPane(gamePanel);
            } else if (onlineMultiplayer.isSelected()) {
                String username = usernameField.getText().trim();
                String gameIdInput = gameIdField.getText().trim();

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan username Anda.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (gameIdInput.isEmpty()) {
                    String newGameId = UUID.randomUUID().toString();
                    JOptionPane.showMessageDialog(this, "Game dibuat! ID: " + newGameId, "Game Created", JOptionPane.INFORMATION_MESSAGE);
                    startGameOnline(newGameId, username, selectedTime, true);
                } else {
                    JOptionPane.showMessageDialog(this, "Bergabung ke Game ID: " + gameIdInput, "Joining Game", JOptionPane.INFORMATION_MESSAGE);
                    startGameOnline(gameIdInput, username, selectedTime, false);
                }
            }

            parentFrame.revalidate();
            parentFrame.repaint();
        });
    }

    private void startGameOnline(String gameId, String username, int turnTime, boolean amIPlayer1Cross) {
        SwingUtilities.invokeLater(() -> {
            GameMain gamePanel = new GameMain(false, "None", turnTime, gameId, username, amIPlayer1Cross);
            gamePanel.setOnlineMultiplayer(true, username, amIPlayer1Cross);
            parentFrame.setContentPane(gamePanel);
            parentFrame.revalidate();
            parentFrame.repaint();
            gamePanel.pollForGameUpdates();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
