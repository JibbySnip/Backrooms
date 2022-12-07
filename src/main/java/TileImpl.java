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
    private static InteractResult nothing = new InteractResult(0, -1);
    public static Tile voidTile = new TileImpl(makeSolidImage(8, 8, Color.BLUE), true);
    public static Tile floorTile = new TileImpl(makeSolidImage(8, 8, Color.lightGray), false);

    public TileImpl(BufferedImage texture, InteractResult interact, boolean canCollide) {
        init(texture, interact, canCollide);
        this.interact = interact;
    }

    public TileImpl(BufferedImage texture, boolean canCollide) {
        init(texture, nothing, canCollide);
    }

    public TileImpl(String filePath, boolean canCollide) throws FileNotFoundException {
        try {
            BufferedImage i = ImageIO.read(new File(filePath));
            init(i, nothing, canCollide);
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
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

    private void init(BufferedImage texture, InteractResult interact, boolean canCollide) {
        // TODO: 12/4/22 require 8x8 image
        this.interact = interact;
        this.canCollide = canCollide;
        this.texture = texture;
        if (texture.getWidth() != texture.getHeight()) {
            throw new IllegalArgumentException("Tile texture map must be square");
        }
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
        return interact;
    }

    private static BufferedImage makeSolidImage(int w, int h, Color c) {
        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = b.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, w, h);
        return b;
    }

    public boolean canCollide() {
        return canCollide;
    }

}
