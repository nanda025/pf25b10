import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class welcomePanel extends JPanel {
    public welcomePanel(JFrame frame) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 220)); // Warna krem

        JLabel title = new JLabel("Welcome to Tic Tac Toe!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setBackground(new Color(0, 128, 0));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setPreferredSize(new Dimension(200, 50));

        // Panel untuk tombol di tengah
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 220));
        buttonPanel.add(startButton);

        add(title, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        // Action saat tombol diklik
        startButton.addActionListener(e -> {
            GameMain gamePanel = new GameMain();
            frame.setContentPane(gamePanel);
            frame.revalidate();
            gamePanel.requestFocusInWindow();
        });
    }
}
