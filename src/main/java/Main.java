import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        /*Main Class*/
        SwingUtilities.invokeLater(() -> {
            Window window = new Window(Assets.image("/Resource/General/background.png"));
            window.showWindow();
            RulesWindow rules = new RulesWindow();
            rules.showWindow();
        });
    }
}
