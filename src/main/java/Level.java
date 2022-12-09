import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.random.RandomGenerator;

public class Level extends NullableGraph.Vertex {
    // This can be changed to adjust how large rooms are, on average
    private static final int DIM_MEAN = 20, DIM_STDEV = 4, DIM_MIN = 2;
    private static final int EXIT_MEAN = 3, EXIT_STDEV=1, EXIT_MIN = 1;
    public static final double NEW_EXIT_PROB = 0.1;
    private final RandomGenerator ng;
    private Tile[][] tileMap;
    private int w, h;
    public int spawnX, spawnY;


    public Level(RandomGenerator ng, NullableGraph g) {
        super(g);
        this.ng = ng;
        generate();
    }

    /**
     * Generates the room. This is done in a couple of steps.
     * First, the room's dimensions are randomly chosen using a normal distribution.
     */
    private void generate() {

        while (w < DIM_MIN) {
            this.w = (int) ng.nextGaussian(DIM_MEAN, DIM_STDEV);
        }
        while (h < DIM_MIN) {
            this.h = (int) ng.nextGaussian(DIM_MEAN, DIM_STDEV);
        }

        int e = -1;
        while (e < EXIT_MIN) {
            e = (int) ng.nextGaussian(EXIT_MEAN, EXIT_STDEV);
        }

        Set<Integer> exitIndices = new TreeSet<>();
        for (int i = 0; i < e; i++) {
            while (exitIndices.size() < e) {
                exitIndices.add(ng.nextInt( w*h));
            }
        }

        tileMap = new Tile[h][w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (exitIndices.contains(i*w + j)) {
                    tileMap[i][j] = new TileImpl(
                            TileImpl.exitTile,
                            new InteractResult(0, addEdge(), this));
                } else {
                    tileMap[i][j] = TileImpl.floorTile;
                }
            }
        }

        Player testPlayer = new Player();

        while (collision(testPlayer.getBoundingBox(0 ,0))) {
            spawnX = ng.nextInt(0, w);
            spawnY = ng.nextInt(0, h);
            testPlayer.setPos(new Point2D.Double(spawnX, spawnY));
        }
    }
//
//    private Tile[][] caveify(Tile[][] tileMap, Tile floorTile, Tile wallTile) {
//
//    }



    public boolean collision(Rectangle2D bB) {
        if (bB.getMinX() < 0 || bB.getMaxX() > w || bB.getMinY() < 0 || bB.getMaxY() > h) {
            return true;
        }
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (tileMap[i][j].canCollide() && bB.intersects(new Rectangle2D.Double(j, i,1,1))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void reassignEdge(NullableGraph.NullableEdge e0, NullableGraph.NullableEdge e1) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Tile t = tileMap[j][i];
                if (t.interact().exitTaken() == e0) {
                    InteractResult prev = t.interact();
                    tileMap[j][i] = new TileImpl(t, new InteractResult(prev.shekelsGained(), e1, prev.originRoom()));
                    return;
                }
            }
        }
    }

    public boolean outOfBounds(int xPos, int yPos) {
        return xPos < 0 || xPos >= tileMap[0].length || yPos < 0 || yPos >= tileMap.length;
    }

    public Tile[][] getTileMap() {
        return tileMap;
    }

    public NullableGraph.NullableEdge getRandomUnassignedEdge() {
        List<NullableGraph.NullableEdge> freeEdges = new ArrayList<>();
        for (NullableGraph.NullableEdge e : edges) {
            if (e.hasNullEnd()) {
                freeEdges.add(e);
            }
        }
        if (freeEdges.size() == 0) {
            return null;
        } else {
            return freeEdges.get(ng.nextInt(freeEdges.size()));
        }
    }

    public Point2D findExitCoords(NullableGraph.NullableEdge e) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Tile t = tileMap[j][i];
                if (t.interact().exitTaken() == e) {
                    return new Point2D.Double(i, j);
                }
            }
        }
        return null;
    }
}
