package com.soar.agent.architecture.world;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jsoar.util.events.SoarEventManager;

import com.soar.agent.architecture.robot.Robot;

public class World {
    public final SoarEventManager events = new SoarEventManager();
    public final Rectangle2D extents = new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0);
    private final List<Robot> robots = new ArrayList<Robot>();
    private final List<Shape> obstacles = new ArrayList<Shape>();

    public World() {

    }

    public SoarEventManager getEvents() {
        return events;
    }

    public Rectangle2D getExtents() {
        return extents;
    }

    public void addRobot(Robot robot) {
        this.robots.add(robot);
    }

    public void removeRobot(Robot robot) {
        this.robots.remove(robot);
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public void addObstacle(Shape shape) {
        this.obstacles.add(shape);
    }

    public void removeObstacle(Shape shape) {
        this.obstacles.remove(shape);
    }

    public List<Shape> getObstacles() {
        return obstacles;
    }

    public void update(double dt) {
        for (Robot robot : robots) {
            robot.update(dt);
        }
    }

    public boolean willCollide(Robot r, double newX, double newY) {
        final double radius = r.getRadius();
        if (!extents.contains(newX + radius, newY + radius) ||
                !extents.contains(newX + radius, newY - radius) ||
                !extents.contains(newX - radius, newY - radius) ||
                !extents.contains(newX - radius, newY + radius)) {
            return true;
        }

        for (Shape s : obstacles) {
            if (s.contains(newX + radius, newY + radius) ||
                    s.contains(newX + radius, newY - radius) ||
                    s.contains(newX - radius, newY - radius) ||
                    s.contains(newX - radius, newY + radius)) {
                return true;
            }
        }
        final Point2D newPoint = new Point2D.Double(newX, newY);
        for (Robot other : robots) {
            if (r != other) {
                if (newPoint.distance(other.getShape().getCenterX(), other.getShape().getCenterY()) < radius
                        + other.getRadius()) {
                    return true;
                }
            }
        }
        return false;
    }

    public double getCollisionRange(Robot source, double angle) {
        final double delta = source.getRadius() / 2.0;
        final double dx = delta * Math.cos(angle);
        final double dy = delta * Math.sin(angle);
        double range = 2 * delta;
        double x = source.getShape().getCenterX() + 2 * dx;
        double y = source.getShape().getCenterY() + 2 * dy;
        if (collides(source.getShape(), x, y)) {
            return 0.0;
        }
        while (!collides(source.getShape(), x, y)) {
            x += dx;
            y += dy;
            range += delta;
        }
        return range - delta;
    }

    private boolean collides(Shape ignore, double x, double y) {
        if (!extents.contains(x, y)) {
            return true;
        }

        for (Shape s : obstacles) {
            if (ignore != s && s.contains(x, y)) {
                return true;
            }
        }
        for (Robot r : robots) {
            if (ignore != r.getShape() && r.getShape().contains(x, y)) {
                return true;
            }
        }
        return false;
    }

}
