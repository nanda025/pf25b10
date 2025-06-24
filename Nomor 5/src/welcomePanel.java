// welcomePanel.java (dengan background gambar)
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

        JLabel title = new JLabel("Tic-Tac-Toe");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        twoPlayerLocal = new JRadioButton("2 Player (Lokal)");
        vsAI = new JRadioButton("vs AI");
        onlineMultiplayer = new JRadioButton("Multiplayer Online");
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
        JLabel difficultyLabel = new JLabel("AI Difficulty:");
        JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyLabel.setEnabled(false);
        difficultyBox.setEnabled(false);
        gbc.gridx = 0; add(difficultyLabel, gbc);
        gbc.gridx = 1; add(difficultyBox, gbc);

        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Your Username:");
        usernameField = new JTextField(10);
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        gbc.gridx = 0; add(usernameLabel, gbc);
        gbc.gridx = 1; add(usernameField, gbc);

        gbc.gridy++;
        JLabel gameIdLabel = new JLabel("Game ID (Kosongkan utk Buat Baru):");
        gameIdField = new JTextField(15);
        gameIdLabel.setEnabled(false);
        gameIdField.setEnabled(false);
        gbc.gridx = 0; add(gameIdLabel, gbc);
        gbc.gridx = 1; add(gameIdField, gbc);

        ActionListener modeListener = e -> {
            boolean isVsAI = vsAI.isSelected();
            boolean isOnlineMultiplayer = onlineMultiplayer.isSelected();

            difficultyLabel.setEnabled(isVsAI);
            difficultyBox.setEnabled(isVsAI);

            usernameLabel.setEnabled(isOnlineMultiplayer);
            usernameField.setEnabled(isOnlineMultiplayer);
            gameIdLabel.setEnabled(isOnlineMultiplayer);
            gameIdField.setEnabled(isOnlineMultiplayer);
        };
        twoPlayerLocal.addActionListener(modeListener);
        vsAI.addActionListener(modeListener);
        onlineMultiplayer.addActionListener(modeListener);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel timerLabel = new JLabel("Time per Turn (s):");
        JComboBox<String> timerBox = new JComboBox<>(new String[]{"5", "10", "15", "30"});
        timerBox.setSelectedIndex(1);

        add(timerLabel, gbc);
        gbc.gridx = 1;
        add(timerBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        add(startButton, gbc);

        startButton.addActionListener(e -> {
            int selectedTime = Integer.parseInt((String) timerBox.getSelectedItem());

            if (twoPlayerLocal.isSelected()) {
                GameMain gamePanel = new GameMain(false, "None", selectedTime, null, null, true);
                parentFrame.setContentPane(gamePanel);
                parentFrame.revalidate();
                parentFrame.repaint();
            } else if (vsAI.isSelected()) {
                String selectedDifficulty = (String) difficultyBox.getSelectedItem();
                GameMain gamePanel = new GameMain(true, selectedDifficulty, selectedTime, null, null, true);
                parentFrame.setContentPane(gamePanel);
                parentFrame.revalidate();
                parentFrame.repaint();
            } else if (onlineMultiplayer.isSelected()) {
                String username = usernameField.getText().trim();
                String gameIdInput = gameIdField.getText().trim();

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter your username.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (gameIdInput.isEmpty()) {
                    String newGameId = UUID.randomUUID().toString();
                    JOptionPane.showMessageDialog(this, "Game created! Share this ID: " + newGameId + "\nYou are Player X (Cross).", "Game Created", JOptionPane.INFORMATION_MESSAGE);
                    startGameOnline(newGameId, username, selectedTime, true);
                } else {
                    JOptionPane.showMessageDialog(this, "Attempting to join game: " + gameIdInput + "\nYou are Player O (Nought).", "Joining Game", JOptionPane.INFORMATION_MESSAGE);
                    startGameOnline(gameIdInput, username, selectedTime, false);
                }
            }
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
