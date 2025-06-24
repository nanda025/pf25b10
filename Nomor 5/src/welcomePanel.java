import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class welcomePanel extends JPanel {
    private JFrame parentFrame;

    public welcomePanel(JFrame frame) {
        this.parentFrame = frame;
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 220)); // krem

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
        JRadioButton twoPlayer = new JRadioButton("2 Player");
        JRadioButton vsAI = new JRadioButton("vs AI");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(twoPlayer);
        modeGroup.add(vsAI);
        twoPlayer.setSelected(true);

        add(twoPlayer, gbc);
        gbc.gridx = 1;
        add(vsAI, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel difficultyLabel = new JLabel("AI Difficulty:");
        JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyLabel.setEnabled(false);
        difficultyBox.setEnabled(false);

        add(difficultyLabel, gbc);
        gbc.gridx = 1;
        add(difficultyBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel timerLabel = new JLabel("Time per Turn (s):");
        JComboBox<String> timerBox = new JComboBox<>(new String[]{"5", "10", "15", "30"});
        timerBox.setSelectedIndex(1); // default 10 detik

        add(timerLabel, gbc);
        gbc.gridx = 1;
        add(timerBox, gbc);

        // Toggle difficulty dropdown only if vsAI selected
        vsAI.addActionListener(e -> {
            difficultyLabel.setEnabled(true);
            difficultyBox.setEnabled(true);
        });
        twoPlayer.addActionListener(e -> {
            difficultyLabel.setEnabled(false);
            difficultyBox.setEnabled(false);
        });

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        add(startButton, gbc);

        startButton.addActionListener(e -> {
            boolean isVsAI = vsAI.isSelected();
            String selectedDifficulty = (String) difficultyBox.getSelectedItem();
            if (!isVsAI) selectedDifficulty = "None"; // Untuk 2 Player mode

            int selectedTime = Integer.parseInt((String) timerBox.getSelectedItem());

            // Panggil GameMain dengan info AI level
            GameMain gamePanel = new GameMain(isVsAI, selectedDifficulty, selectedTime);
            parentFrame.setContentPane(gamePanel);
            parentFrame.revalidate();
            parentFrame.repaint();
        });
    }
}
