package com.soar.agent.architecture.world;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jsoar.util.events.SoarEventManager;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.robot.Robot;

public class World {
    public final SoarEventManager events = new SoarEventManager();
    public final Rectangle2D extents = new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0);
    private final List<Robot> robots = new ArrayList<Robot>();
    private final List<Shape> obstacles = new ArrayList<Shape>();
    private final List<Landmark> landmarks = new ArrayList<Landmark>();

    public World() {

    }

    public SoarEventManager getEvents() {
        return events;
    }

    public Rectangle2D getExtents() {
        return extents;
    }

    public void addRobot(Robot robot) {
        robots.add(robot);
    }

    public void removeRobot(Robot robot) {
        robots.remove(robot);
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public void addObstacle(Shape shape) {
        obstacles.add(shape);
    }

    public void removeObstacle(Shape shape) {
        obstacles.remove(shape);
    }

    public List<Shape> getObstacles() {
        return obstacles;
    }

    public void addLandmark(Landmark landmark) {
        landmarks.add(landmark);
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public void updateAndMoveAgents(double dt) {
        for (Robot robot : robots) {
            robot.updateAndMove(dt);
        }
    }

    public boolean isLandmarkReached(Landmark landmark, Robot robot) {
        boolean result = false;

        if (robot.getShape().contains(landmark.getLocation())) {
            result = true;
        }

        return result;
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

        // for (Landmark landmark : landmarks) {
        // System.out.println(landmark.getLocation().distance(newX+radius,
        // newY+radius));
        // // pose is x=5, y=11 for the north obstacle

        // if (landmark.getLocation().distance(newX + radius, newY + radius) <
        // Double.valueOf(1.40)
        // ||
        // landmark.getLocation().distance(newX + radius, newY - radius) <
        // Double.valueOf(1.40) ||
        // landmark.getLocation().distance(newX - radius, newY - radius) <
        // Double.valueOf(1.40)||
        // landmark.getLocation().distance(newX - radius, newY + radius)<
        // Double.valueOf(1.40)

        // ) {
        // // return true;
        // }
        // }

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

        for (Landmark l : landmarks) {
            double landmarkX = l.getLocation().getX();
            double landmarkXY = l.getLocation().getY();

            double distance = l.getLocation().distance(x, y);

            // if(distance < 1){

            // }
        }
        return false;
    }

    public void radarDetectLandmark(Robot robot, Radar radar) {
        Arc2D arc = robot.getRadarArc();
        
        // robot position
        double robotCurrentX = robot.getShape().getCenterX();
        double robotCurrentY = robot.getShape().getCenterY();

        // end position of the range from the robot
        double dx = Math.cos(robot.getYaw() + radar.getRadarAngle()) * radar.getRadarRange();
        double dy = Math.sin(robot.getYaw() + radar.getRadarAngle()) * radar.getRadarRange();
        double newX = robotCurrentX + dx;
        double newY = robotCurrentY + dy;

        // otherside of the arc
        double newX_2 = newX * Math.cos(25) - newY * Math.sin(25);
        double newY_2 = newX * Math.sin(25) + newY * Math.cos(25);

        Rectangle2D newRec = new Rectangle2D.Double(newX, newY, 2 * radar.getRadarRange(), 2 * radar.getRadarRange());
        arc.setFrame(newRec);
        robot.setRadarArc(arc);

    }

}
