import javax.swing.*;
import java.awt.*;

public class RulesWindow extends JFrame {



    public RulesWindow(){
        this.setSize(400,180);
        this.setTitle("Rules Before The Gaming / RBTG");
        JLabel rules = new JLabel("Game Objective:");
        JLabel rules1 = new JLabel("The game has 2 players who control planes that automatically fly.");
        JLabel rules2= new JLabel("The player who gets the most points gathering from targets, wins.");
        JLabel rules3 = new JLabel("The players controls:");
        JLabel rules4 = new JLabel("The orange player shoots with space.");
        JLabel rules5 = new JLabel("The blue player shoots with enter");
        JLabel rules6 = new JLabel("MAY THE BEST PLAYER WIN");
        rules.setBounds(0,0,200,800);
        rules1.setBounds(rules.getX(),rules.getY()+20,500,800);
        rules2.setBounds(0,rules1.getY()+20,rules1.getWidth(),rules1.getHeight());
        rules3.setBounds(rules.getX(),rules2.getY()+20,rules1.getWidth(),rules1.getHeight());
        rules4.setBounds(rules.getX(),rules3.getY()+20,rules1.getWidth(),rules1.getHeight());
        rules5.setBounds(0,rules4.getY()+20,rules1.getWidth(),rules1.getHeight());
        rules6.setBounds(rules.getX(),rules5.getY()+20,rules1.getWidth(),rules1.getHeight());
        Dimension preferredSize = new Dimension(400, 200); // Adjust the size as needed
        rules.setPreferredSize(preferredSize);
        rules.setVerticalAlignment(SwingConstants.TOP);
        rules1.setPreferredSize(preferredSize);
        rules1.setVerticalAlignment(SwingConstants.TOP);
        rules2.setPreferredSize(preferredSize);
        rules2.setVerticalAlignment(SwingConstants.TOP);
        rules3.setPreferredSize(preferredSize);
        rules3.setVerticalAlignment(SwingConstants.TOP);
        rules4.setPreferredSize(preferredSize);
        rules4.setVerticalAlignment(SwingConstants.TOP);
        rules5.setPreferredSize(preferredSize);
        rules5.setVerticalAlignment(SwingConstants.TOP);
        rules6.setPreferredSize(preferredSize);
        rules6.setVerticalAlignment(SwingConstants.TOP);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(null);
        this.setBackground(Color.BLACK);
        this.add(rules);
        this.add(rules1);
        this.add(rules2);
        this.add(rules3);
        this.add(rules4);
        this.add(rules5);
        this.add(rules6);
    }
    public void showWindow(){//This method will make the JFrame visible
        this.setVisible(true);/*JFrame's build-in method to set it to be visible*/
    }
}
