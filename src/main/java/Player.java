import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Player {
    private Point2D pos = new Point2D.Double(-1, -1);
    private double velX, velY;

    public static final double BOUND_X = 0.8, BOUND_Y = 0.8;
    public static final double VEL_MAX=0.6;
    private static final int COLLISION_SUBELEMS = 5;
    private Level level;
    private int animCount = 0;

    private int animCycle = 8;
    private int dir = -1;
    private final BufferedImage playerTextureLeft1,playerTextureLeft2,playerTextureRight1,playerTextureRight2;

    public Player() {
        try {
            playerTextureLeft1 = ImageIO.read(new File("resources/player1.png"));
            playerTextureLeft2 = ImageIO.read(new File("resources/player2.png"));
            playerTextureRight1 = flipImg(playerTextureLeft1);

            playerTextureRight2 = flipImg(playerTextureLeft2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage flipImg(BufferedImage i) {
        AffineTransform flip = new AffineTransform();
        flip.concatenate(AffineTransform.getScaleInstance(-1, 1));
        flip.concatenate(AffineTransform.getTranslateInstance(-playerTextureLeft1.getWidth(), 0));

        BufferedImage img = new BufferedImage(
                i.getWidth(),
                i.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = img.createGraphics();
        g.transform(flip);
        g.drawImage(i, 0,0, null);
        return img;
    }

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
                this.pos.getX()+dx,
                this.pos.getY()+dy
        );
    }

    public void setLevel(Level l) {
        this.level = l;
        this.pos = new Point2D.Double(l.spawnX, l.spawnY);
    }

    public void setLevel(Level l, Point2D pos) {
        this.level = l;
        this.pos = pos;
    }

    public BufferedImage getTexture() {
        animCount++;
        if (velX == 0 && velY == 0 || animCount == animCycle) {
            animCount = 0;
        }
        if (animCount % animCycle > animCycle/2 || animCount % animCycle == 0) {
            if (dir < 0) {
                return playerTextureLeft1;
            } else {
                return playerTextureRight1;
            }
        } else {
            if (dir < 0) {
                return playerTextureLeft2;
            } else {
                return playerTextureRight2;
            }
        }
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
        if (x < 0) {
            dir = -1;
        } else if (x > 0) {
            dir = 1;
        }
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
        BufferedImage i = getTexture();
        g.drawImage(i,
                (int) x - i.getWidth()/2 - 1,
                (int) y - i.getHeight()/2 - 1,
                null);
    }
}
