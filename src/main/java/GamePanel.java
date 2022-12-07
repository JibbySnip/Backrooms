import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GamePanel extends JComponent {
    private Level currLevel;
    private Random r = new Random();
    private NullableGraph levels;
    Player p = new Player();


    private boolean playing = false;
    private boolean transitioning = false;
    private final int desiredTPS = 30;

    public GamePanel() {


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
                    case KeyEvent.VK_SPACE -> interact();
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
        if (playing && !transitioning) {
            p.move();
            repaint();
        }
    }

    private void reset() {
        playing = true;
        levels = new NullableGraph(r);
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
        Level nLevel = new Level(r, levels);
        levels.add(nLevel);
        return nLevel;
    }

    private void interact() {
        Tile t = currLevel.getTileMap()
                [(int) Math.floor(p.getPos().getY())]
                [(int) Math.floor(p.getPos().getX())];

        InteractResult i = t.interact();
        // TODO: 12/6/2022 add shekels
        // TODO: 12/6/2022 Add soft lock protection
        transitioning = true;
        System.out.println("interacting...");
        if (i.exitTaken() != null) {
            System.out.println("Found an exit");
            if (i.exitTaken().hasNullEnd()) {
                if (!levels.hasFreeEdges() || r.nextDouble(0, 1) > Level.NEW_EXIT_PROB) { // TODO: 12/6/2022 come up with better algo for probability
                    // Make a new room with an exit
                    Level l = newLevel();
                    NullableGraph.NullableEdge e0 = l.addEdge();
                    levels.mergeEdges(i.exitTaken(), e0);
                    currLevel = l;
                } else {
                    // Connect to an existing room
                    NullableGraph.NullableEdge e = levels.mergeEdges(i.exitTaken(), levels.findNullEdge()); // TODO: 12/7/2022 make the self-loop edges thing work
                    currLevel = (Level) e.oppositeEnd(i.originRoom());

                }
            } else {
                currLevel = (Level) i.exitTaken().oppositeEnd(i.originRoom());
            }
            p.setLevel(currLevel, currLevel.findExitCoords(i.exitTaken()));


        }
        transitioning = false;
    }
    @Override
    protected void paintComponent(Graphics g) {
        int posPxX = (int) Math.round(p.getPos().getX()*Tile.TILE_DIM);
        int posPxY = (int) Math.round(p.getPos().getY()*Tile.TILE_DIM);

        Dimension viewportSize = this.getSize();
        double dxPx = (viewportSize.getWidth() / 2);
        double dyPx = (viewportSize.getHeight() / 2);

        int xMin = (int) Math.floor((posPxX - dxPx) / Tile.TILE_DIM);
        int xMax = (int) Math.ceil((posPxX + dxPx) / Tile.TILE_DIM);
        int yMin = (int) Math.floor((posPxY - dyPx) / Tile.TILE_DIM);
        int yMax = (int) Math.ceil((posPxY + dyPx) / Tile.TILE_DIM);
        if (currLevel != null) {
            Tile[][] tileMap = currLevel.getTileMap();
            BufferedImage b = new BufferedImage(viewportSize.width, viewportSize.height, BufferedImage.TYPE_INT_RGB);
            // Iterating over tile coordinates in tileMap
            for (int i = yMin; i < yMax; i++) {
                for (int j = xMin; j < xMax; j++) {
                    Tile tileToFill;
                    if (currLevel.outOfBounds(j, i)) {
                        tileToFill = TileImpl.voidTile;
                    } else {
                        tileToFill = tileMap[i][j];
                    }
                    for (int x = 0; x < Tile.TILE_DIM; x++) {
                        for (int y = 0; y < Tile.TILE_DIM; y++) {
                            int pixelCoordX = j * Tile.TILE_DIM - (int) Math.floor(posPxX - dxPx) + x;
                            int pixelCoordY = i * Tile.TILE_DIM - (int) Math.floor(posPxY - dyPx) + y;
                            //                        if (Math.abs(posPxX - pixelCoordX) <= dxPx && Math.abs(posPxY - pixelCoordY) <= dyPx) {
                            if (pixelCoordX > 0 && pixelCoordX < getWidth() && pixelCoordY > 0 && pixelCoordY < getHeight()) {
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
        }

//        debugImage(g);

    }

    private void debugImage(Graphics g) {
        g.setColor(Color.GREEN);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        g.drawLine((int) getWidth()/2, 0, (int) getWidth()/2, getHeight());
        g.drawLine(0, (int)getHeight()/2, (int) getWidth(), getHeight()/2);
        g.drawString("PosX = " + Double.toString(p.getPos().getX()), 10, 20);
        g.drawString("PosY = " + Double.toString(p.getPos().getY()), 10, 40);
        Rectangle2D r = p.getBoundingBox(0,0);
        g.drawRect(
                (int) (getWidth()/2 - r.getWidth()*4),
                (int) (getHeight()/2 - r.getHeight()*4),
                (int) r.getWidth()*Tile.TILE_DIM,
                (int) r.getHeight()*Tile.TILE_DIM
        );
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 600);
    }
}
