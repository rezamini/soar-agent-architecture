package com.soar.agent.architecture.world;

import javax.swing.JPanel;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.robot.Robot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.Point;

import org.jsoar.debugger.util.SwingTools;

/**
 * WorldPanel
 */
public class WorldPanel extends JPanel {

    private double pixelsPerMeter = 55;
    private double panX = 0.0;
    private double panY = 0.0;
    private Robot follow = null;
    private World world = new World();
    private Robot selection = null;

    public WorldPanel() {
        setBackground(Color.BLACK);

        addMouseWheelListener(new MouseAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see
             * java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
             */
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    pixelsPerMeter += e.getWheelRotation() * 0.5;
                    if (pixelsPerMeter <= 0) {
                        pixelsPerMeter = 1;
                    }
                    repaint();
                }
            }
        });
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void fit() {
        pixelsPerMeter = (getWidth() / world.extents.getWidth()) * 0.8;
        panX = -world.extents.getCenterX();
        panY = -world.extents.getCenterY();
        repaint();
    }

    public Robot getSelection() {
        return selection;
    }

    @Override
    protected void paintComponent(Graphics g) {
        SwingTools.enableAntiAliasing(g);
        super.paintComponent(g);

        final Graphics2D g2d = setupWorldTransform((Graphics2D) g.create());

        g2d.setStroke(new BasicStroke(2 * (1.0f / (float) pixelsPerMeter)));

        drawShape(g2d, world.extents, Color.LIGHT_GRAY, Color.BLUE);

        for (Robot robot : world.getRobots()) {
            drawRobot(g2d, robot);
        }

        for (Landmark w : world.getLandmarks()) {
            drawLandmark(g2d, w);
        }

        for (Shape s : world.getObstacles()) {
            drawObstacle(g2d, s);
        }
        g2d.dispose();

    }

    private Graphics2D setupWorldTransform(final Graphics2D g2d) {
        final AffineTransform transform = new AffineTransform();
        transform.translate(getWidth() / 2.0, getHeight() / 2.0);
        transform.scale(pixelsPerMeter, -pixelsPerMeter);
        if (follow != null) {
            panX = -follow.getShape().getCenterX();
            panY = -follow.getShape().getCenterY();
            transform.rotate(-(follow.getYaw() - Math.toRadians(90)));
        }
        transform.translate(panX, panY);

        g2d.transform(transform);
        return g2d;
    }

    private void drawShape(Graphics2D g2d, Shape shape, Color fill, Color stroke) {
        g2d.setColor(fill);
        g2d.fill(shape);
        g2d.setColor(stroke);
        g2d.draw(shape);
    }

    public void drawCar(Graphics2D g2d, Shape shape, Shape firstWheel, Shape secondWheel, Shape roof, Color fill,
            Color stroke) {
        g2d.setColor(fill);
        g2d.fill(shape);
        g2d.setColor(stroke);
        g2d.fill(firstWheel);
        g2d.fill(secondWheel);
        g2d.fill(roof);
    }

    private void drawRobot(Graphics2D g2dIn, Robot robot) {
        final Graphics2D g2d = (Graphics2D) g2dIn.create();
        final AffineTransform transform = new AffineTransform();
        transform.translate(robot.getShape().getCenterX(), robot.getShape().getCenterY());
        transform.rotate(robot.getYaw());
        g2d.transform(transform);

        drawRanges(g2d, robot);

        if (robot == selection) {
            final double selR = robot.getRadius() * 1.4;
            final Ellipse2D sel = new Ellipse2D.Double(-selR, -selR, selR * 2.0, selR * 2.0);
            g2d.setColor(Color.BLUE);
            g2d.fill(sel);
        }

        final double r = robot.getShapeStartingPoint();
        // final Ellipse2D body = new Ellipse2D.Double(-r, -r, r * 2.0, r * 2.0);
        // drawShape(g2d, body, Color.WHITE, Color.BLACK);

        Rectangle2D.Double body = new Rectangle2D.Double(-r, -r, r * robot.getWidthMultiplier(),
                r * robot.getHeightMultiplier());
        Ellipse2D firstWheel = new Ellipse2D.Double(-r + 0.2, -r - 0.2, 0.3, 0.3);
        Ellipse2D secondWheel = new Ellipse2D.Double(-r + 1, -r - 0.2, 0.3, 0.3);
        Rectangle2D.Double roof = new Rectangle2D.Double(-r + 0.2, -r + 0.6, r * robot.getWidthMultiplier() / 1.5,
                r * robot.getHeightMultiplier() / 2);

        drawCar(g2d, body, firstWheel, secondWheel, roof, Color.YELLOW, Color.BLACK);

        final double dirR = r / 4.0;
        // final double dirR = r / 5.0;
        // final Ellipse2D dir = new Ellipse2D.Double(r - dirR, -dirR, dirR * 2.0, dirR
        // * 2.0);
        final Ellipse2D dir = new Ellipse2D.Double(body.getMaxX() - dirR, body.getCenterY() - dirR, dirR * 2.0,
                dirR * 2.0);
        drawShape(g2d, dir, Color.RED, Color.BLACK);

        g2d.rotate(follow == null ? -robot.getYaw() : Math.toRadians(-90.0));

        // final double fontHeight = robot.getRadius() * 1.5;
        // prepareFont(g2d, fontHeight);
        // final Rectangle2D bounds = g2d.getFont().getStringBounds(robot.getName(),
        // g2d.getFontRenderContext());
        // g2d.setColor(Color.BLACK);
        // g2d.drawString(robot.getName(), (float) (-bounds.getWidth() / 2.0), (float)
        // (-(fontHeight / 3.0)));

        g2d.dispose();
    }

    private Font prepareFont(Graphics2D g2d, double fontHeight) {
        final AffineTransform fontTransform = AffineTransform.getScaleInstance(1.0, -1.0);
        final Font font = g2d.getFont().deriveFont(Font.BOLD, (float) fontHeight).deriveFont(fontTransform);
        g2d.setFont(font);
        return font;
    }

    private void drawObstacle(Graphics2D g2dIn, Shape shape) {
        final Graphics2D g2d = (Graphics2D) g2dIn.create();

        drawShape(g2d, shape, Color.GRAY, Color.BLACK);
        g2d.dispose();
    }

    private void drawRanges(Graphics2D g2dIn, Robot robot) {
        for (Radar range : robot.ranges) {
            final Arc2D arc = new Arc2D.Double(-range.getRadarRange(), -range.getRadarRange(), 2 *
                    range.getRadarRange(), 2 * range.getRadarRange(),
                    Math.toDegrees(-range.getRadarAngle()) - 10.0, 25.0, Arc2D.PIE);
            drawShape(g2dIn, arc, Color.GREEN, Color.GREEN);
        }
    }

    private void drawLandmark(Graphics2D g2dIn, Landmark landmark) {
        final Graphics2D g2d = (Graphics2D) g2dIn.create();
        final Point2D p = landmark.getLocation();
        final double r = 0.2;
        final Ellipse2D circle1 = new Ellipse2D.Double(p.getX() - r, p.getY() - r, 2
                * r, 2 * r);

        drawShape(g2d, circle1, Color.ORANGE, Color.BLACK);

        final double r2 = r * 1.4;
        final Ellipse2D circle2 = new Ellipse2D.Double(p.getX() - r2, p.getY() - r2,
                2 * r2, 2 * r2);
        g2d.draw(circle2);

        final double fontHeight = r * 1.5;
        prepareFont(g2d, fontHeight);
        final Rectangle2D bounds = g2d.getFont().getStringBounds(landmark.name,
                g2d.getFontRenderContext());
        g2d.setColor(Color.BLACK);
        g2d.drawString(landmark.name, (float) (p.getX() - bounds.getWidth() / 2.0),
                (float) (p.getY() - fontHeight / 3.0));

        g2d.dispose();

        /* This is to add + line on the landmark */
        // g2d.draw(new Line2D.Double(p.getX(), p.getY() - 1.5 * r, p.getX(), p.getY()+
        // 1.5 * r));
        // g2d.draw(new Line2D.Double(p.getX() - 1.5 * r, p.getY(), p.getX() + 1.5 *r,
        // p.getY()));
    }

}