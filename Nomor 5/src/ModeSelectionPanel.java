// ModeSelectionPanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ModeSelectionPanel extends JPanel {
    private JFrame parentFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JComboBox<String> difficultyBox;
    private JTextField player1Field;
    private JTextField player2Field;
    private JComboBox<String> timerBox;
    private JRadioButton twoPlayerLocal, vsAI;
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

        // Mode selection
        twoPlayerLocal = new JRadioButton("2 Pemain (Lokal)", true);
        vsAI = new JRadioButton("vs AI");

        ButtonGroup group = new ButtonGroup();
        group.add(twoPlayerLocal);
        group.add(vsAI);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        add(twoPlayerLocal, gbc);
        gbc.gridx = 1;
        add(vsAI, gbc);

        // Difficulty selection
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel difficultyLabel = new JLabel("Tingkat AI:");
        difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyLabel.setEnabled(false);
        difficultyBox.setEnabled(false);
        add(difficultyLabel, gbc);
        gbc.gridx = 1;
        add(difficultyBox, gbc);

        // Timer per turn
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel timerLabel = new JLabel("Waktu per Giliran (detik):");
        timerBox = new JComboBox<>(new String[]{"5", "10", "15", "30"});
        timerBox.setSelectedIndex(1); // Default 10 detik
        add(timerLabel, gbc);
        gbc.gridx = 1;
        add(timerBox, gbc);

        // Player names
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel player1Label = new JLabel("Nama Pemain 1:");
        player1Field = new JTextField(10);
        add(player1Label, gbc);
        gbc.gridx = 1;
        add(player1Field, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel player2Label = new JLabel("Nama Pemain 2:");
        player2Field = new JTextField(10);
        add(player2Label, gbc);
        gbc.gridx = 1;
        add(player2Field, gbc);

        // Buttons
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton startButton = new JButton("Mulai Game");
        add(startButton, gbc);

        gbc.gridy++;
        JButton backButton = new JButton("Kembali");
        add(backButton, gbc);

        // Action listeners
        twoPlayerLocal.addActionListener(e -> {
            difficultyLabel.setEnabled(false);
            difficultyBox.setEnabled(false);
        });

        vsAI.addActionListener(e -> {
            difficultyLabel.setEnabled(true);
            difficultyBox.setEnabled(true);
        });

        startButton.addActionListener(e -> {
            int time = Integer.parseInt((String) timerBox.getSelectedItem());
            boolean isVsAI = vsAI.isSelected();

            if (isVsAI) {
                String difficulty = (String) difficultyBox.getSelectedItem();
                GameMain game = new GameMain(true, difficulty, time, "Player", "AI");
                parentFrame.setContentPane(game);
                parentFrame.revalidate();
            } else {
                String p1 = player1Field.getText().trim();
                String p2 = player2Field.getText().trim();
                if (p1.isEmpty() || p2.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan nama kedua pemain.");
                    return;
                }
                GameMain game = new GameMain(false, "None", time, p1, p2);
                parentFrame.setContentPane(game);
                parentFrame.revalidate();
            }
        });

        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MAIN_MENU"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}