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

        // Tombol pilihan mode
        JButton vsPlayerButton = new JButton("2 Player Mode");
        vsPlayerButton.setFont(new Font("Arial", Font.BOLD, 18));
        vsPlayerButton.setBackground(new Color(0, 128, 0));
        vsPlayerButton.setForeground(Color.WHITE);
        vsPlayerButton.setFocusPainted(false);
        vsPlayerButton.setPreferredSize(new Dimension(200, 50));

        JButton vsComputerButton = new JButton("Play vs Computer");
        vsComputerButton.setFont(new Font("Arial", Font.BOLD, 18));
        vsComputerButton.setBackground(new Color(0, 0, 128));
        vsComputerButton.setForeground(Color.WHITE);
        vsComputerButton.setFocusPainted(false);
        vsComputerButton.setPreferredSize(new Dimension(200, 50));

        // Panel untuk tombol di tengah
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 220));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.add(vsPlayerButton);
        buttonPanel.add(vsComputerButton);

        add(title, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        // Action Listener untuk mode 2 pemain
        vsPlayerButton.addActionListener(e -> {
            GameMain gamePanel = new GameMain(false); // false = 2 pemain
            frame.setContentPane(gamePanel);
            frame.revalidate();
            gamePanel.requestFocusInWindow();
        });

        // Action Listener untuk melawan komputer
        vsComputerButton.addActionListener(e -> {
            GameMain gamePanel = new GameMain(true); // true = lawan komputer
            frame.setContentPane(gamePanel);
            frame.revalidate();
            gamePanel.requestFocusInWindow();
        });
    }
}
