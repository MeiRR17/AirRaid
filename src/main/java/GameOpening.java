import javax.swing.*;
import java.awt.*;

public class GameOpening {
    private final JPanel titlePanel;
    private final JButton startButton;

    public GameOpening(JFrame jFrame, int GAME_OPENING_WIDTH, int GAME_OPENING_HEIGHT) {

        titlePanel = new JPanel();
        titlePanel.setBounds(GAME_OPENING_WIDTH / 8, GAME_OPENING_HEIGHT / 6, GAME_OPENING_WIDTH / 4 * 3, GAME_OPENING_HEIGHT / 4);

        // Set the layout of titlePanel to null to allow custom positioning
        titlePanel.setLayout(null);

        // Create a JLabel to hold the GIF
        JLabel backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, titlePanel.getWidth(), titlePanel.getHeight());

        // Load the GIF file and create an ImageIcon
        ImageIcon gifIcon = new ImageIcon(Assets.url("/Resource/General/FLY_.gif"));

        // Set the ImageIcon as the icon for the JLabel
        backgroundLabel.setIcon(gifIcon);

        // Add the JLabel to the titlePanel
        titlePanel.add(backgroundLabel);

        JLabel titleNameLabel = new JLabel("AIR-RAID");
        titleNameLabel.setForeground(Color.WHITE);
        titleNameLabel.setFont(new Font("Times New Roman", Font.PLAIN, 90));
        titlePanel.add(titleNameLabel);

        JPanel startButtonPanel = new JPanel();
        startButtonPanel.setBounds(GAME_OPENING_WIDTH / 8 * 3, GAME_OPENING_HEIGHT / 6 * 4, GAME_OPENING_WIDTH / 4, GAME_OPENING_HEIGHT / 8);

        startButton = new JButton("START");
        startButton.setBackground(Color.GRAY.brighter());

        startButton.setForeground(Color.BLACK);
        startButton.setFont(new Font("Times New Roman", Font.PLAIN, 55));
        startButtonPanel.add(startButton);
        titlePanel.add(startButtonPanel);
        jFrame.add(startButtonPanel);
    }

    public JPanel getTitlePanel() {
        return titlePanel;
    }

    public JButton getStartButton() {
        return startButton;
    }
}
