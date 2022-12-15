import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class TileImpl implements Tile {
    private static final InteractResult nothing = new InteractResult(0, null,
            null, false);
    private final static Tile openChestTile = new TileImpl("textures/openChest.png",
            false);
    private static final Tile unlockedTrapdoor = new TileImpl("textures/openTrapdoor.png",
            false);
    public static Tile voidTile = new TileImpl(makeSolidImage(Color.BLACK), true);
    public static Tile stoneTile = new TileImpl("textures/stone.png", false);
    public static Tile woodTile = new TileImpl("textures/wood.png", false);
    public static Tile exitTile =
            new TileImpl("textures/exit.png", new InteractResult(0,
                    null, null, true), false);
    public boolean canCollide;
    InteractResult interact;
    private BufferedImage texture;
    private int w, h;


    public TileImpl(BufferedImage texture, InteractResult interact, boolean canCollide) {
        init(texture, interact, canCollide);
        this.interact = interact;
    }

    public TileImpl(BufferedImage texture, boolean canCollide) {
        init(texture, nothing, canCollide);
    }

    public TileImpl(String filePath, boolean canCollide) {
        try {
            BufferedImage i = loadImg(filePath);
            init(i, nothing, canCollide);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TileImpl(String filePath, InteractResult interact, boolean canCollide) {
        try {
            BufferedImage i = loadImg(filePath);
            init(i, interact, canCollide);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TileImpl(Tile base, InteractResult interact) {
        init(base.getGraphics(), interact, base.canCollide());
    }

    private static BufferedImage makeSolidImage(Color c) {
        BufferedImage b = new BufferedImage(TILE_DIM, TILE_DIM, BufferedImage.TYPE_INT_RGB);
        Graphics g = b.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, TILE_DIM, TILE_DIM);
        return b;
    }

    public static Tile makeChestTile(int numShekels) {
        return new TileImpl("textures/closedChest.png", new InteractResult(numShekels,
                null, null, false),
                false) {
            private boolean isOpen = false;

            @Override
            public BufferedImage getGraphics() {
                if (isOpen) {
                    return openChestTile.getGraphics();
                }
                else {
                    return super.getGraphics();
                }
            }

            @Override
            public InteractResult interact() {
                isOpen = true;
                InteractResult i = this.interact;
                this.interact = new InteractResult(0, null, null,
                        false);
                return i;

            }

        };
    }

    public static BufferedImage loadImg(String fp) throws IOException {
        return ImageIO.read(Objects.requireNonNull(TileImpl.class.getResource(fp)));

    }

    public static Tile getDoorTile(NullableGraph.NullableEdge e, NullableGraph.Vertex v) {
        return new TileImpl("textures/trapdoor.png", new InteractResult(-1,
                e, v, false), false) {
            private boolean isLocked = true;

            @Override
            public BufferedImage getGraphics() {
                if (isLocked) {
                    return super.getGraphics();
                }
                else {
                    return unlockedTrapdoor.getGraphics();
                }
            }

            @Override
            public InteractResult interact() {
                isLocked = false;
                InteractResult temp = super.interact();
                this.interact = new InteractResult(0, e, v, false);
                return temp;
            }
        };
    }

    private void init(BufferedImage texture, InteractResult interact, boolean canCollide) {
        this.interact = interact;
        this.canCollide = canCollide;
        if (texture.getWidth() != texture.getHeight()) {
            throw new IllegalArgumentException("Tile texture map must be square");
        }
        Image i = texture.getScaledInstance(TILE_DIM, TILE_DIM, Image.SCALE_DEFAULT);
        BufferedImage b = new BufferedImage(TILE_DIM, TILE_DIM, BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.drawImage(i, 0, 0, null);
        this.texture = b;

        this.h = texture.getWidth();
        this.w = texture.getHeight();
    }

    @Override
    public BufferedImage getGraphics() {
        return texture;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(w, h);
    }

    @Override
    public NullableGraph.NullableEdge exitTaken() {
        return this.interact.exitTaken();
    }

    @Override
    public int shekelCount() {
        return interact.shekelsGained();
    }

    @Override
    public InteractResult interact() {
        //        InteractResult t = interact;
        //        interact = new InteractResult(0, t.exitTaken(), t.originRoom());
        return interact;
    }

    public boolean canCollide() {
        return canCollide;
    }

}
