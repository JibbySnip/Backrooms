import java.awt.*;
import java.awt.image.BufferedImage;

public interface Tile {

    public static final int TILE_DIM = 32;

    /**
     * Gets the position of the tile within the level
     * @return A point representing the top left corner of the tile
     */
    BufferedImage getGraphics();
    Dimension getSize();

    InteractResult interact();

    boolean canCollide();

}
