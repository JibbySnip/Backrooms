import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackroomsGame implements Runnable {
    @Override
    public void run() {
        JFrame frame = new JFrame("The Backrooms");

        JPanel topBar = new JPanel();
        topBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        topBar.setLayout(new GridLayout(1, 5));

        JButton instrBtn = new JButton("Instructions");
        topBar.add(instrBtn);
        AtomicBoolean instrShown = new AtomicBoolean(false);
        instrBtn.addActionListener(e -> {
                if (!instrShown.get()) {
                    instrShown.set(true);
                    JFrame instrFrame = new JFrame("Instructions");
                    instrFrame.add(new InstructionPanel());
                    frame.pack();
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            super.windowClosed(e);
                            instrShown.set(false);
                        }
                    });

                    frame.setVisible(true);
                }
            }
        );
        GamePanel g = new GamePanel();
        frame.setLayout(new BorderLayout());
        frame.add(topBar, BorderLayout.SOUTH);
        frame.add(g, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        g.start();
    }

    class InstructionPanel extends JComponent {
        // ceditor,
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new BackroomsGame());
    }
}
