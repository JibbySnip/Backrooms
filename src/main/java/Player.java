import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Player {
    private Point2D pos = new Point2D.Double(-1, -1);
    private double velX, velY;
    public static final double BOUND_X = 1, BOUND_Y = 1;
    public static final int VEL_MAX=1;
    private static final int COLLISION_SUBELEMS = 2;
    private static final int PLAYER_RENDER_SIZE = 10;
    private Level level;
    private BufferedImage playerTexture;

    /**
     * This uses a little heuristic algorithm collision thing I came up with. Basically, if
     * it's not possible to move the full distance, then move by progressions of a fraction of
     * dx until an invalid position is found, then move to the position before that. This isn't
     * totally solid for a few reasons: the heuristic obviously isn't totally accurate, so it won't
     * totally reach a barrier, and it also can "stick" if it's bounded in one direction but not
     * the other while moving diagonally. Either way, it's a half-decent approximation, and it's
     * the best you're getting.
     */
    public void move() {
        double dx = 0;
        double dy = 0;

        if ((velX != 0 || velY != 0)) {
            double norm = 1 / Math.sqrt(Math.pow(velX, 2) + Math.pow(velY, 2));
            dx = VEL_MAX * velX * norm;
            dy = VEL_MAX * velY * norm;
            int count = 0;
            double last_dx = 0;
            double last_dy = 0;
            while (count <= COLLISION_SUBELEMS) {

                if (level.collision(getBoundingBox(
                        (count * dx) /  COLLISION_SUBELEMS,
                        (count * dy) / COLLISION_SUBELEMS
                        )
                )) {
                    dx = last_dx;
                    dy = last_dy;
                    break;
                } else {
                    last_dx = (count * dx) / COLLISION_SUBELEMS;
                    last_dy = (count * dy) / COLLISION_SUBELEMS;
                }
                count++;
            }
        }
        transformPos(dx, dy);
    }

    public void setPos(Point2D pos) {
        this.pos = pos;
    }

    private void transformPos(double dx, double dy) {
        this.pos = new Point2D.Double(
                pos.getX()+dx,
                pos.getY()+dy
        );
    }

    public void setLevel(Level l) {
        this.level = l;
        this.pos = new Point2D.Double(l.spawnX, l.spawnY);
    }

    public void setLevel(Level l, int posX, int posY) {
        this.level = l;
        this.pos = new Point2D.Double(posX, posY);
    }

    public BufferedImage getTexture() {
        return playerTexture;
    }

    public Rectangle2D getBoundingBox(double dx, double dy) {
        return new Rectangle2D.Double(
             (pos.getX() - BOUND_X/2 + dx),
             (pos.getY()  - BOUND_Y/2+ dy),
            BOUND_X,
            BOUND_Y
        );
    }

    public Point2D getPos() {
        return pos;
    }

    public void setVelocity(double x, double y) {
        // TODO: 12/4/22 Some refactoring and input validation
        this.velX = x;
        this.velY = y;
    }

    public void setVelX(double x) {
        setVelocity(x, velY);
    }

    public void setVelY(double y) {
        setVelocity(velX, y);
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public void draw(Graphics g, double x, double y) {
//        g.drawImage(playerTexture, (int) dxPx-5, (int) dyPx-5, null);
        g.fillOval(
                (int) x - PLAYER_RENDER_SIZE/2,
                (int) y - PLAYER_RENDER_SIZE/2,
                PLAYER_RENDER_SIZE,
                PLAYER_RENDER_SIZE);
    }
}
