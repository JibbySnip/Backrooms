import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.random.RandomGenerator;

public class Level extends NullableGraph.Vertex {
    // This can be changed to adjust how large rooms are, on average
    private static final int DIM_MEAN = 30, DIM_STDEV = 4, DIM_MIN = 20;
    private static final double EXIT_MEAN = 2, EXIT_STDEV=1, EXIT_MIN = 1;
    private static final double CHEST_MEAN = 1.5, CHEST_STDEV=1, CHEST_MIN = 0;
    private static final double COIN_MEAN = 1, COIN_STDEV=0, COIN_MIN = 1;
    private static final double FINAL_EXIT_PROB = 0.02;

    public static final double NEW_EXIT_PROB = 0.1;
    private static final List<Point> vonNeumannPoints = Arrays.asList(new Point(0, -1),
            new Point(-1, 0),
            new Point(1, 0),
            new Point(0, 1));

    public static final double CAVE_FLOOR_PROB = 0.6;
    public static final int NUM_ITERATIONS = 3;
    private final RandomGenerator ng;
    private Tile[][] tileMap;
    private int w, h;
    public int spawnX, spawnY;
    private Tile floorTile;
    private final Tile wallTile = TileImpl.voidTile;
    private static final List<Tile> floorStyles = Arrays.asList(TileImpl.woodTile, TileImpl.stoneTile);
    private List<Tile> chests = new LinkedList<>();
    private List<Point> doors = new LinkedList<>();
    public boolean hasFinalExit = false;

    public Level(RandomGenerator ng, NullableGraph g) {
        super(g);
        this.ng = ng;
        generate();
    }

    /**
     * Generates the room. This is done in a couple of steps.
     * First, the room's dimensions are randomly chosen using a normal distribution.
     */
    private void generate(){

        while (w < DIM_MIN) {
            this.w = (int) ng.nextGaussian(DIM_MEAN, DIM_STDEV);
        }
        while (h < DIM_MIN) {
            this.h = (int) ng.nextGaussian(DIM_MEAN, DIM_STDEV);
        }

        int e = -1;
        while ((e < EXIT_MIN && graph.freeEdgeCount() > 1 ) || e < 2) {
            e = (int) ng.nextGaussian(EXIT_MEAN, EXIT_STDEV);
        }

        int c = -1;
        while (c < CHEST_MIN) {
            c = (int) ng.nextGaussian(CHEST_MEAN, CHEST_STDEV);
        }

        this.floorTile = floorStyles.get(ng.nextInt(floorStyles.size()));

        tileMap = new Tile[h][w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                tileMap[i][j] =
                        ng.nextDouble(0,1) < CAVE_FLOOR_PROB ? floorTile : wallTile;
            }
        }

        caveify();

        for (int i = 0; i < e; i++) {
            boolean foundExit = false;
            while (!foundExit) {
                int x = ng.nextInt(0, w);
                int y = ng.nextInt(0, h);
                if (tileMap[y][x] == floorTile) { // TODO: 12/11/22 increase min edge generation if freeEdges <= 1
                    if (ng.nextDouble(0,1) < FINAL_EXIT_PROB) {
                        tileMap[y][x] = TileImpl.exitTile;
                        hasFinalExit = true;
                    } else {
                        doors.add(new Point(x, y));
                        tileMap[y][x] = TileImpl.getDoorTile(addEdge(), this);
                    }
                    foundExit = true;
                }
            }
        }

        for (int i = 0; i < c; i++) {
            boolean foundChest = false;
            while (!foundChest) {
                int x = ng.nextInt(0, w);
                int y = ng.nextInt(0, h);
                if (tileMap[y][x] == floorTile) {

                    int nShekels = (int) Math.max(COIN_MIN, (int)Math.round(ng.nextGaussian(COIN_MEAN, COIN_STDEV)));

                    Tile t = TileImpl.makeChestTile(nShekels);
                    chests.add(t);
                    tileMap[y][x] = t;
                    foundChest = true;
                }
            }
        }

        Player collisionPlayer = new Player();

        while (collision(collisionPlayer.getBoundingBox(0 ,0))) {
            spawnX = ng.nextInt(0, w);
            spawnY = ng.nextInt(0, h);
            collisionPlayer.setPos(new Point2D.Double(spawnX, spawnY));
        }
    }

    private void caveify() {
        // Apply the rules of Conway's Game of Life
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            doAutomataIteration();
        }

        List<Point> unassignedTiles = new LinkedList<>();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Tile t = tileMap[j][i];
                if (t == floorTile) {
                    unassignedTiles.add(new Point(i, j));
                }
            }
        }
        List<Set<Point>> caves = new LinkedList<>();
        while (!unassignedTiles.isEmpty()) {
            Set<Point> cave = new HashSet<>();
            cave.add(unassignedTiles.remove(0));

            int delta = -1;
            while (delta != 0) {
                int oldCount = cave.size();
                for (Point t : new ArrayList<>(cave)) {
                    cave.addAll(vonNeumannNeighbors(tileMap, t));
                    unassignedTiles.removeAll(cave);
                }
                delta = cave.size() - oldCount;
            }
            caves.add(cave);
        }

        Optional<Set<Point>> maxCaveOpt = caves.stream().max(Comparator.comparingInt(Set::size));
        Set<Point> maxCave;
        if (maxCaveOpt.isPresent()) {
            maxCave = maxCaveOpt.get();
        } else {
            throw new RuntimeException("No largest cave");
        }
        for (int i = 0; i < w; i ++) {
            for (int j = 0; j< h; j++) {
                if (maxCave.contains(new Point(i, j))) {
                    tileMap[j][i] = floorTile;
                } else {
                    tileMap[j][i] = wallTile;
                }
            }
        }
    }

    private void doAutomataIteration() {
        for (int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                int nNeighbors = mooreNumNeighbors(tileMap, i, j);
                if (tileMap[j][i] == wallTile && nNeighbors < 3) {
                    tileMap[j][i] = floorTile;
                } else if (tileMap[j][i] == floorTile && (nNeighbors > 4)) {
                    tileMap[j][i] = wallTile;
                }
            }
        }
    }

    private List<Point> vonNeumannNeighbors(Tile[][] tileMap, Point p) {
        LinkedList<Point> neighbors = new LinkedList<>();

        for (Point pt : vonNeumannPoints) {
            int nextX = p.x + pt.x;
            int nextY = p.y + pt.y;
            if (!outOfBounds(tileMap, nextX, nextY) && tileMap[nextY][nextX] == floorTile) {

                neighbors.add(new Point(nextX, nextY));
            }
        }
        return neighbors;
    }

    private int mooreNumNeighbors(Tile[][] tileMap, int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (!(i == 0 && j == 0) && (outOfBounds(tileMap, x+i, y+j) || tileMap[y+j][x+i] == wallTile)) {
                    count++;
                }
            }

        }
        return count;
    }



    public boolean collision(Rectangle2D bB) {
        if (bB.getMinX() < 0 || bB.getMaxX() >= w || bB.getMinY() < 0 || bB.getMaxY() >= h) {
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
        for (Point p : doors) {
            Tile t = tileMap[p.y][p.x];
            if (t.exitTaken() == e0) {
                InteractResult prev = t.interact();
                tileMap[p.y][p.x] = TileImpl.getDoorTile(e1, prev.originRoom());
                tileMap[p.y][p.x].interact();
                return;
            }
        }
    }

    public static boolean outOfBounds(Tile[][] tileMap, int xPos, int yPos) {
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
        for (Point p : doors) {
            Tile t = tileMap[p.y][p.x];
            if (t.exitTaken() == e) {
                return new Point2D.Double(p.x, p.y);
            }
        }
        return null;
    }

    public int shekelsAvailable() {
        int c = 0;
        for (Tile ch : chests) {
            c += ch.shekelCount();
        }
        return c;
    }
}
