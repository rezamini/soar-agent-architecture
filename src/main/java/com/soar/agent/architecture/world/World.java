package com.soar.agent.architecture.world;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.geom.*;
import java.awt.Polygon;
import java.awt.geom.Area;

import org.jsoar.util.events.SoarEventManager;

import com.lowagie.text.Rectangle;
import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.robot.Robot;

public class World {
    public final SoarEventManager events = new SoarEventManager();
    public final Rectangle2D extents = new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0);
    private final List<Robot> robots = new ArrayList<Robot>();
    private final List<Shape> obstacles = new ArrayList<Shape>();
    private final List<Landmark> landmarks = new ArrayList<Landmark>();

    // the boolean value is to indicate if landmark is reached by the agent
    private final Map<Landmark, Boolean> landmarkMap = new HashMap<Landmark, Boolean>();

    // the Boolean value is to indicate if the landmark is within radar/if its live
    private Map<Landmark, Boolean> detectedRadarLandmarks = new HashMap<Landmark, Boolean>();

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

    public void addLandmarkMap(Landmark landmark, boolean isReached) {
        landmarkMap.put(landmark, isReached);
    }

    public Map<Landmark, Boolean> getLandmarkMap() {
        return landmarkMap;
    }

    public Map<Landmark, Boolean> getDetectedRadarLandmarks() {
        return detectedRadarLandmarks;
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

    public boolean willCollide(Robot r, double newX, double newY, double[] dimensions) {
        Rectangle2D tempShape = (Rectangle2D) r.getShape().clone();

        double area = r.getShapeArea() + r.getShapeStartingPoint();
        double agentWidth = dimensions[0];
        double agentHeight = dimensions[1];

        tempShape.setFrameFromCenter(newX, newY, newX + agentWidth, newY + agentHeight);

        // if (!extents.contains(newX + area, newY + area) ||
        // !extents.contains(newX + area, newY - area) ||
        // !extents.contains(newX - area, newY - area) ||
        // !extents.contains(newX - area, newY + area)) {
        // return true;
        // }

        if (!extents.contains(tempShape)) {
            return true;
        }

        for (Shape s : obstacles) {
            if (tempShape.intersects(s.getBounds2D()) && s.intersects(tempShape)) {
                return true;
            }
        }
        return false;
    }

    public boolean willCollide(Robot r, double newX, double newY) {
        double area = r.getShapeArea() + r.getShapeStartingPoint();

        if (!extents.contains(newX + area, newY + area) ||
                !extents.contains(newX + area, newY - area) ||
                !extents.contains(newX - area, newY - area) ||
                !extents.contains(newX - area, newY + area)) {
            return true;
        }

        for (Shape s : obstacles) {

            if (s.contains(newX + area, newY + area) ||
                    s.contains(newX + area, newY - area) ||
                    s.contains(newX - area, newY - area) ||
                    s.contains(newX - area, newY + area)) {
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

        // final Point2D newPoint = new Point2D.Double(newX, newY);
        // for (Robot other : robots) {
        // if (r != other) {
        // if (newPoint.distance(other.getShape().getCenterX(),
        // other.getShape().getCenterY()) < area
        // + other.getShapeArea()) {
        // return true;
        // }
        // }
        // }
        return false;
    }

    public double getCollisionRange(Robot source, double angle) {
        // final double delta = source.getShapeArea() / 2.0;
        double delta = 0.1;
        final double dx = delta * Math.round(Math.cos(angle));
        final double dy = delta * Math.round(Math.sin(angle));
        double range = delta;
        double x = source.getShape().getCenterX() + dx;
        double y = source.getShape().getCenterY() + dy;
        double newX_2 = x;
        double newY_2 = y;

        if (dy == 0) {
            newY_2 = x * Math.sin(25) + y * Math.cos(25);
        }

        if (dy < 0 || dy > 0) {
            newY_2 = (x - source.getShape().getCenterX()) * Math.sin(25)
                    + (y - source.getShape().getCenterY()) * Math.cos(25)
                    + source.getShape().getCenterY();
        }

        if (dx > 0 || dx < 0) {
            newX_2 = (x - source.getShape().getCenterX()) * Math.cos(25)
                    - (y - source.getShape().getCenterY()) * Math.sin(25)
                    + source.getShape().getCenterX();
        }

        if (dx == 0) {
            newX_2 = x * Math.cos(25) - y * Math.sin(25);
        }

        Arc2D arc = source.getRadarArc();
        arc.setArcByCenter(source.getShape().getCenterX(), source.getShape().getCenterY(), range, -10, 25, Arc2D.PIE);

        // set all the values to false and then check the landmarks one by one. later in
        // the loop the values will be set to true if its within radar. this has to be
        // called outside the while loop
        detectedRadarLandmarks.replaceAll((k, v) -> v = false);
        if (collides(source.getShape(), x, y, newX_2, newY_2, arc)) {
            return 0.0;
        }
        while (!collides(source.getShape(), x, y, newX_2, newY_2, arc)) {
            x += dx;
            y += dy;
            newX_2 += dx;
            newY_2 += dy;
            range += delta;
            arc.setArcByCenter(source.getShape().getCenterX(), source.getShape().getCenterY(), range, -10, 25, Arc2D.PIE);

            radarDetectLandmark(source, x, y, range);
        }

        return range - delta;
    }

    private boolean collides(Shape ignore, double x, double y, double newX_2, double newY_2, Arc2D arc) {
        if (!extents.contains(x, y)) {

            return true;
        }

        // if (!extents.contains(newX_2, newY_2) || !extents.intersects(newX_2, newY_2,
        // newX_2, newY_2)) {
        // return true;
        // }

        for (Shape s : obstacles) {
            if (ignore != s && s.contains(x, y)) {
                return true;
            }

            if (ignore != s && s.contains(newX_2, newY_2)) {
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

            Area arcArea = new Area(arc);

            if(arcArea.contains(landmarkX, landmarkXY)){
                System.out.println(l.getName());
            }
            

        }
        return false;
    }

    public void radarDetectLandmark(Robot robot, double radarX, double radarY, double radarRange) {
        // get a instance of the agent shape
        Rectangle2D agentShape = (Rectangle2D) robot.getShape().clone();

        // simulate a move with the radar positions
        agentShape.setFrameFromCenter(radarX, radarY, radarX + robot.getShapeWidth(), radarY + robot.getShapeHeight());

        // check if the agent will reach/hit any landmark with the current landmark
        // positions (aka if radar can see and agent can hit the landmark)
        // List<Landmark> tempLandmarks = new ArrayList<Landmark>();

        for (Landmark landmark : landmarks) {

            if (agentShape.contains(landmark.getLocation())) {
                detectedRadarLandmarks.put(landmark, true);

                // if(!tempLandmarks.contains(landmark)){
                // tempLandmarks.add(landmark);
                // }
            }
        }
    }

    public void radarDetectLandmark(Robot robot, Radar radar) {
        

        // robot position
        double robotCurrentX = robot.getShape().getCenterX();
        double robotCurrentY = robot.getShape().getCenterY();

        Arc2D arc = robot.getRadarArc();
        double radarRange = radar.getRadarRange();
        arc.setArcByCenter(robotCurrentX, robotCurrentY, radarRange, -10, 25, Arc2D.PIE);

        // // end position of the range from the robot
        // double dx = Math.cos(robot.getYaw() + radar.getRadarAngle()) *
        // radar.getRadarRange();
        // double dy = Math.sin(robot.getYaw() + radar.getRadarAngle()) *
        // radar.getRadarRange();
        // double newX = robotCurrentX + dx;
        // double newY = robotCurrentY + dy;

        // // otherside of the arc
        // double newX_2 = newX * Math.cos(25) - newY * Math.sin(25);
        // double newY_2 = newX * Math.sin(25) + newY * Math.cos(25);



        // for (Landmark landmark : landmarks) {
        //     double pointX = landmark.getLocation().getX();
        //     double pointY = landmark.getLocation().getY();

        // }
    }

}
