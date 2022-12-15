import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

public class BackroomsGame implements Runnable {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new BackroomsGame());
    }

    @Override
    public void run() {
        JFrame frame = new JFrame("The Backrooms");

        JPanel topBar = new JPanel();
        topBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        topBar.setLayout(new GridLayout(1, 5));

        JButton instrBtn = new JButton("Instructions");
        topBar.add(instrBtn);

        GamePanel g = new GamePanel();

        frame.setLayout(new BorderLayout());
        frame.add(topBar, BorderLayout.SOUTH);
        frame.add(g, BorderLayout.CENTER);

        instrBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(g,
                    "<html><body> " + "<p style='width: 200px;'>In this game, you must " +
                            "explore a series of ever-shifting caverns, finding " +
                            "your way out before you run out of shekels. You must pay a shekel " +
                            "at each door to unlock " +
                            "it. Chests are hidden throughout the caverns, and you " +
                            "can find more shekels in them. Make it to the rainbow door and " +
                            "escape before you run out of " +
                            "shekels and are trapped in the Backrooms forever. To navigate the " +
                            "caverns, use the arrow " +
                            "keys or WASD to move, and use the space bar to interact with chests" +
                            " and doors. Good luck, brave " +
                            "explorer!</p></body></html>", "The Backrooms", INFORMATION_MESSAGE,
                    new ImageIcon("resources/player1.png"));

            g.requestFocusInWindow();
        });

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        g.requestFocusInWindow();
        g.start();
    }

    class InstructionPanel extends JPanel {
        public InstructionPanel() {
            //            setLayout(new GridLayout(0, 1));
            setLayout(new BorderLayout());
            JLabel title = new JLabel("Welcome to the Backrooms");
            //            title.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
            add(title, BorderLayout.NORTH);

            JLabel preamble = new JLabel(
                    "<html><p>In this game, you must explore a series of ever-shifting caverns, " +
                            "finding " +
                            "your way out before you run out of shekels. You must pay a shekel " +
                            "at each door to unlock " +
                            "it. Chests are hidden throughout the caverns, and you " +
                            "can find more shekels in them. Make it to the rainbow door and " +
                            "escape before you run out of " +
                            "shekels and are trapped in the Backrooms forever. To navigate the " +
                            "caverns, use the arrow " +
                            "keys or WASD to move, and use the space bar to interact with chests" +
                            " and doors. Good luck, brave " +
                            "explorer!</p></html>");

            //            preamble.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            add(preamble, BorderLayout.CENTER);

        }
    }
}
