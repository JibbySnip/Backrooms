import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JComponent {
    private Level currLevel;
    private ArrayList<Level> levels = new ArrayList<>();
    private Random r = new Random();
    Player p = new Player();

    private boolean playing = false;
    private final int desiredTPS = 30;

    public GamePanel() {
        currLevel = newLevel();

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> p.setVelX(-p.VEL_MAX);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> p.setVelX(p.VEL_MAX);
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> p.setVelY(-p.VEL_MAX);
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> p.setVelY(p.VEL_MAX);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_A, KeyEvent.VK_D -> p.setVelX(0);
                    case KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_S, KeyEvent.VK_W -> p.setVelY(0);
                }
            }
        });

    }

    private void tick() {
        if (playing) {
            p.move();
            repaint();
        }
    }

    private void reset() {
        playing = true;
        levels = new ArrayList<>();
        currLevel = newLevel();
        p = new Player();
        p.setLevel(currLevel);
    }

    public void start() {
        reset();
        Timer timer = new Timer(1000/desiredTPS, e -> tick());
        timer.start(); // MAKE SURE TO START THE TIMER!
    }
    public Level newLevel() {
        Level nLevel = new Level(r);
        levels.add(nLevel);
        return nLevel;
    }
    @Override
    protected void paintComponent(Graphics g) {
        int posPxX = (int) Math.round(p.getPos().getX()*8);
        int posPxY = (int) Math.round(p.getPos().getY()*8);

        Dimension viewportSize = this.getSize();
        double dxPx = (viewportSize.getWidth() / 2);
        double dyPx = (viewportSize.getHeight() / 2);

        int xMin = (int) Math.floor((posPxX - dxPx) / 8);
        int xMax = (int) Math.ceil((posPxX + dxPx) / 8);
        int yMin = (int) Math.floor((posPxY - dyPx) / 8);
        int yMax = (int) Math.ceil((posPxY + dyPx) / 8);
        Tile[][] tileMap = currLevel.getTileMap();
        BufferedImage b = new BufferedImage(viewportSize.width, viewportSize.height, BufferedImage.TYPE_INT_RGB);
        // Iterating over tile coordinates in tileMap
        for (int i = yMin ; i < yMax; i++) {
            for (int j = xMin ; j < xMax; j++) {
                Tile tileToFill;
                if (currLevel.outOfBounds(j, i)) {
                    tileToFill = TileImpl.voidTile;
                } else {
                    tileToFill = tileMap[i][j];
                }
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        int pixelCoordX = j*8 - (int) Math.floor(posPxX - dxPx) + x;
                        int pixelCoordY = i*8 - (int) Math.floor(posPxY - dyPx) + y;
//                        if (Math.abs(posPxX - pixelCoordX) <= dxPx && Math.abs(posPxY - pixelCoordY) <= dyPx) {
                        if (pixelCoordX > 0 && pixelCoordX < getWidth() && pixelCoordY > 0 && pixelCoordY < getHeight() ) {
                            b.setRGB(
                                    pixelCoordX,
                                    pixelCoordY,
                                    tileToFill.getGraphics().getRGB(x, y));
                        }
                    }
                }
            }
        }

        g.setColor(Color.BLACK);
        g.drawImage(b, 0, 0, this);
        p.draw(g, dxPx, dyPx);

//        debugImage(g);

    }

    private void debugImage(Graphics g) {
        g.setColor(Color.GREEN);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        g.drawLine((int) getWidth()/2, 0, (int) getWidth()/2, getHeight());
        g.drawLine(0, (int)getHeight()/2, (int) getWidth(), getHeight()/2);
        g.drawString("VelX = " + Double.toString(p.getVelX()), 10, 20);
        g.drawString("VelY = " + Double.toString(p.getVelY()), 10, 40);
        Rectangle2D r = p.getBoundingBox(0,0);
        g.drawRect(
                (int) (getWidth()/2 - r.getWidth()*4),
                (int) (getHeight()/2 - r.getHeight()*4),
                (int) r.getWidth()*8,
                (int) r.getHeight()*8
        );
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 300);
    }
}
