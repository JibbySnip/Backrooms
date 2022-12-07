import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TileImpl implements Tile{
    private BufferedImage texture;
    private int w, h;
    private InteractResult interact;
    public boolean canCollide;
    private static final InteractResult nothing = new InteractResult(0, null, null);
    public static Tile voidTile = new TileImpl(makeSolidImage(Color.BLACK), true);
    public static Tile floorTile = new TileImpl(makeSolidImage(Color.lightGray), false);
    public static Tile exitTile = new TileImpl(
            "resources/trapdoor.png",
            false);
    public static Tile woodTile = new TileImpl(
            "resources/wood.png",
            false);

    public TileImpl(BufferedImage texture, InteractResult interact, boolean canCollide) {
        init(texture, interact, canCollide);
        this.interact = interact;
    }

    public TileImpl(BufferedImage texture, boolean canCollide) {
        init(texture, nothing, canCollide);
    }

    public TileImpl(String filePath, boolean canCollide) {
        try {
            BufferedImage i = ImageIO.read(new File(filePath));
            init(i, nothing, canCollide);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TileImpl(String filePath, InteractResult interact, boolean canCollide) throws FileNotFoundException {
        try {
            BufferedImage i = ImageIO.read(new File(filePath));
            init(i, interact, canCollide);
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    public TileImpl(Tile base, InteractResult interact) {
        init(base.getGraphics(), interact, base.canCollide());
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
    public InteractResult interact() {
        InteractResult t = interact;
        interact = new InteractResult(0, t.exitTaken(), t.originRoom());
        return t;
    }

    private static BufferedImage makeSolidImage(Color c) {
        BufferedImage b = new BufferedImage(TILE_DIM, TILE_DIM, BufferedImage.TYPE_INT_RGB);
        Graphics g = b.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, TILE_DIM, TILE_DIM);
        return b;
    }

    public boolean canCollide() {
        return canCollide;
    }

}
