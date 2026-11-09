import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    private static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 800;
    Image icon = Assets.image("/Resource/Window/gameIcon.jpg");
    private final Image background;

    public Window(Image background){
        this.background = background;
        setIconImage(icon);
        this.setTitle("Air Raid");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT); /*Set the size of the JFrame*/
        this.setLocationRelativeTo(null); /*Make the Window pop-up in the middle of the screen (width, height)*/
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); /*Make the program end when closing the window with code 0*/
        this.setLayout(null); /*Make the ability to control how the components added to the container are arranged and sized*/
        this.setResizable(false); /*Disable window's resize*/

        GameOpening openingGame = new GameOpening(this, WINDOW_WIDTH, WINDOW_HEIGHT);
        add(openingGame.getTitlePanel());

        Gameplay gameplay = new Gameplay(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        openingGame.getStartButton().addActionListener(e -> {
            openingGame.getTitlePanel().setVisible(false);
            openingGame.getStartButton().setVisible(false);
            add(gameplay);
            gameplay.requestFocusInWindow();
        });
        /*Create new Gameplay Object and make it the same size as the window*/
    }
    public void paint(Graphics g){
        super.paint(g);
        g.drawImage(background, 0, 0, null);
    }
    public void showWindow(){//This method will make the JFrame visible
        this.setVisible(true);/*JFrame's build-in method to set it to be visible*/
    }
}