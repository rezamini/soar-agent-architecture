package com.soar.agent.architecture.world;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.awt.geom.*;

import org.jsoar.util.events.SoarEventManager;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.graph.ShortestPathGraph;
import com.soar.agent.architecture.robot.Robot;

public class World {
    public final SoarEventManager events = new SoarEventManager();
    public final Rectangle2D extents = new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0);
    private final List<Robot> robots = new ArrayList<Robot>();
    private final List<Shape> obstacles = new ArrayList<Shape>();
    private final List<Landmark> landmarks = new ArrayList<Landmark>();
    private int[][] mapMatrix;
    private int[][] completeMapMatrix;
    private Integer agentMapMatrixX = null;
    private Integer agentMapMatrixY = null;
    private Integer secondAgentMapMatrixX = null;
    private Integer secondAgentMapMatrixY = null;

    private Integer agentMapMatrixX2 = null;
    private Integer agentMapMatrixY2 = null;
    private Integer secondAgentMapMatrixX2 = null;
    private Integer secondAgentMapMatrixY2 = null;

    public Integer getAgentMapMatrixX2() {
        return agentMapMatrixX2;
    }

    public void setAgentMapMatrixX2(Integer agentMapMatrixX2) {
        this.agentMapMatrixX2 = agentMapMatrixX2;
    }

    public Integer getAgentMapMatrixY2() {
        return agentMapMatrixY2;
    }

    public void setAgentMapMatrixY2(Integer agentMapMatrixY2) {
        this.agentMapMatrixY2 = agentMapMatrixY2;
    }

    public Integer getSecondAgentMapMatrixX2() {
        return secondAgentMapMatrixX2;
    }

    public void setSecondAgentMapMatrixX2(Integer secondAgentMapMatrixX2) {
        this.secondAgentMapMatrixX2 = secondAgentMapMatrixX2;
    }

    public Integer getSecondAgentMapMatrixY2() {
        return secondAgentMapMatrixY2;
    }

    public void setSecondAgentMapMatrixY2(Integer secondAgentMapMatrixY2) {
        this.secondAgentMapMatrixY2 = secondAgentMapMatrixY2;
    }

    public Integer getSecondAgentMapMatrixX() {
        return secondAgentMapMatrixX;
    }

    public void setSecondAgentMapMatrixX(Integer secondAgentMapMatrixX) {
        this.secondAgentMapMatrixX = secondAgentMapMatrixX;
    }

    public Integer getSecondAgentMapMatrixY() {
        return secondAgentMapMatrixY;
    }

    public void setSecondAgentMapMatrixY(Integer secondAgentMapMatrixY) {
        this.secondAgentMapMatrixY = secondAgentMapMatrixY;
    }

    // the boolean value is to indicate if landmark is reached by the agent
    private final Map<Landmark, Boolean> landmarkMap = new LinkedHashMap<Landmark, Boolean>();

    // the Boolean value is to indicate if the landmark is within radar/if its live
    private Map<Landmark, Boolean> detectedRadarLandmarks = new HashMap<Landmark, Boolean>();

    private ShortestPathGraph shortestPathGraph;
    private ShortestPathGraph shortestPathGraphComplete;
    private Map<Landmark, List<String>> shortestLandmarkDirections = new LinkedHashMap<Landmark, List<String>>();

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

    public int[][] getMapMatrix() {
        return mapMatrix;
    }

    public void setMapMatrix(int[][] mapMatrix) {
        this.mapMatrix = mapMatrix;
    }

    public int[][] getCompleteMapMatrix() {
        return completeMapMatrix;
    }

    public void setCompleteMapMatrix(int[][] completeMapMatrix) {
        this.completeMapMatrix = completeMapMatrix;
    }

    public Integer getAgentMapMatrixX() {
        return agentMapMatrixX;
    }

    public void setAgentMapMatrixX(Integer agentMapMatrixX) {
        this.agentMapMatrixX = agentMapMatrixX;
    }

    public Integer getAgentMapMatrixY() {
        return agentMapMatrixY;
    }

    public void setAgentMapMatrixY(int agentMapMatrixY) {
        this.agentMapMatrixY = agentMapMatrixY;
    }

    public Map<Landmark, List<String>> getShortestLandmarkDirections() {
        return shortestLandmarkDirections;
    }

    public void setShortestLandmarkDirections(Map<Landmark, List<String>> shortestLandmarkDirections) {
        this.shortestLandmarkDirections = shortestLandmarkDirections;
    }

    public ShortestPathGraph getShortestPathGraph() {
        return shortestPathGraph;
    }

    public void setShortestPathGraph(ShortestPathGraph shortestPathGraph) {
        this.shortestPathGraph = shortestPathGraph;
    }

    public ShortestPathGraph getShortestPathGraphComplete() {
        return shortestPathGraphComplete;
    }

    public void setShortestPathGraphComplete(ShortestPathGraph shortestPathGraphComplete) {
        this.shortestPathGraphComplete = shortestPathGraphComplete;
    }

    public void updateAndMoveAgents(double dt) {
        for (Robot robot : robots) {
            robot.updateAndMove(dt);
        }
    }

    public boolean isLandmarkReached(Landmark landmark, Robot robot) {
        boolean result = false;

        Path2D tempAgentShape = robot.createTempAgentShape();
        double distance = landmark.getLocation()
        .distance(tempAgentShape.getBounds().getCenterX(), tempAgentShape.getBounds().getCenterY());

        if (tempAgentShape.contains(landmark.getLocation()) || distance <= 1.59) {
            result = true;
        }

        return result;
    }

    public boolean willCollide(Robot r, double newX, double newY, Path2D tempAgentShape) {

        if (!extents.contains(tempAgentShape.getBounds2D())) {
            return true;
        }

        for (Shape s : obstacles) {
            if (tempAgentShape.intersects(s.getBounds2D())) {
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

    public double getCollisionRange(Robot source, double angle, boolean detectLandmarks) {
        // final double delta = source.getShapeArea() / 2.0;
        double delta = 0.2;
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

        // set all the values to false and then check the landmarks one by one. later in
        // the loop the values will be set to true if its within radar. this has to be
        // called outside the while loop
        if (detectLandmarks) {
            detectedRadarLandmarks.replaceAll((k, v) -> v = false);
        }
        if (collides(source, x, y, range)) {
            return 0.0;
        }
        while (!collides(source, x, y, range)) {
            x += dx;
            y += dy;
            newX_2 += dx;
            newY_2 += dy;
            range += delta;

            if (detectLandmarks) {
                radarDetectLandmark(source, x, y, range);
            }
        }
        // if (collides(source.getShape(), x, y, newX_2, newY_2)) {
        // return 0.0;
        // }
        // while (!collides(source.getShape(), x, y, newX_2, newY_2)) {
        // x += dx;
        // y += dy;
        // newX_2 += dx;
        // newY_2 += dy;
        // range += delta;

        // radarDetectLandmark(source, x, y, range);
        // }

        return range - delta;
    }

    // check colides with Path2D radar shape
    private boolean collides(Shape ignore, double x, double y, double newX_2, double newY_2) {
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

        // for (Landmark l : landmarks) {
        // double landmarkX = l.getLocation().getX();
        // double landmarkXY = l.getLocation().getY();

        // double distance = l.getLocation().distance(x, y);

        // }

        return false;
    }

    private boolean collides(Robot robot, double x, double y, double radarRange) {
        // get and create shape radar with current data
        Path2D shapeRadar = robot.calcShapeRadar(robot.getShape().getCenterX(), robot.getShape().getCenterY(),
                radarRange);

        if (!extents.contains(shapeRadar.getBounds2D())) {
            return true;
        }

        for (Shape s : obstacles) {
            if (shapeRadar.intersects(s.getBounds2D()) && s.intersects(shapeRadar.getBounds2D())) {
                return true;
            }
        }
        return false;
    }

    public void radarDetectLandmark(Robot robot, double radarX, double radarY, double radarRange) {
        // get a instance of the agent shape
        Rectangle2D agentShape = (Rectangle2D) robot.getShape().clone();

        // get and create shape radar with current data
        Path2D shapeRadar = robot.calcShapeRadar(agentShape.getCenterX(), agentShape.getCenterY(), radarRange);

        // simulate a move with the radar positions
        agentShape.setFrameFromCenter(radarX, radarY, radarX + robot.getShapeWidth(), radarY + robot.getShapeHeight());

        // check if the agent will reach/hit any landmark with the current landmark
        // positions (aka if radar can see and agent can hit the landmark)
        // Or if the created Path2D radar shape contains any shape. both are needed to
        // perform better detections

        for (Landmark landmark : landmarks) {
            if (agentShape.contains(landmark.getLocation()) || shapeRadar.contains(landmark.getLocation())) {
                detectedRadarLandmarks.put(landmark, true);
            }
        }
    }

    public void updateShortestPath() {
        if (shortestPathGraph != null) {
            try {
                shortestPathGraph.updateAgentNode(agentMapMatrixX, agentMapMatrixY, secondAgentMapMatrixX,
                        secondAgentMapMatrixY);
                shortestPathGraph.calculateShortPath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (shortestPathGraphComplete != null) {
            try {
                shortestPathGraphComplete.updateAgentNode(agentMapMatrixX2, agentMapMatrixY2, secondAgentMapMatrixX2,
                        secondAgentMapMatrixY2);
                shortestPathGraphComplete.calculateShortPath();

                if (shortestPathGraphComplete.getComputedPathDirections() != null) {
                    shortestLandmarkDirections = shortestPathGraphComplete.getComputedPathDirections();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /*
     * validate the computed shortest path with the next upcoming move direction
     * this checks for collision and object avoidance and if there is a block the
     * therecalculate take place.
     */
    public boolean validNextShortestPathMove(Robot robot, Landmark landmark, int index) {
        boolean result = true;

        if (shortestLandmarkDirections != null && shortestLandmarkDirections.size() > 0) {
            List<String> currentPath = shortestLandmarkDirections.get(landmark);
            if (currentPath != null && currentPath.size() > 0) {

                // make sure the index(landmark-cycle-count) exist in the current computed paths
                if (index >= 0 && index < currentPath.size()) {
                    String computedDirection = currentPath.get(index);

                    DirectionEnum directionEnum = DirectionEnum.findByName(computedDirection);

                    boolean isBlocked = robot.tempUpdate(0, directionEnum);
                    if (isBlocked) {
                        result = false;
                    }
                }

            }
        }

        return result;
    }

    /*
     * validate the computed shortest path with the new move direction(already
     * performed)
     * if they are not the same recalculate the path. it means the agent is out of
     * correct path/direction sequence.
     */
    public boolean validShortestPathMove(Landmark landmark, int index, String currentDirection) {
        boolean result = true;

        // we need to deduct the index in case to compare the current direction with
        // previous calculated
        // direction. if we do not deduct, then it means that the index is ahead of the
        // comparison and we are
        // comparing the next path value with the current value. A.k.a the decuction
        // compare the current with previous direction
        // to make sure we are in correct path and correct computed direction is
        // executed.
        index = index - 1;

        // in case the substraction goes under 0 set it to first index otherwise it will
        // skip it
        if (index < 0)
            index = 0;

        if (currentDirection != null) {
            List<String> currentPath = shortestLandmarkDirections.get(landmark);
            if (currentPath != null && currentPath.size() > 0) {

                // make sure the index(landmark-cycle-count) exist in the current computed paths
                if (index >= 0 && index < currentPath.size()) {
                    String computedDirection = currentPath.get(index);

                    if (!computedDirection.equalsIgnoreCase(currentDirection)) {
                        result = false;
                    }
                }

            }
        }

        return result;
    }

    // public void radarDetectLandmark(Robot robot, Radar radar) {

    // // robot position
    // double robotCurrentX = robot.getShape().getCenterX();
    // double robotCurrentY = robot.getShape().getCenterY();
    // double radarRange = radar.getRadarRange();

    // Path2D shapeRadar = robot.calcShapeRadar(robotCurrentX, robotCurrentY,
    // radarRange);

    // for (Landmark landmark : landmarks) {
    // if (shapeRadar.contains(landmark.getLocation())) {
    // System.out.println(landmark.getName());
    // }
    // }

    // // Arc2D arc = robot.getRadarArc();
    // // double radarRange = radar.getRadarRange();
    // // arc.setArcByCenter(robotCurrentX, robotCurrentY, radarRange, -10, 25,
    // // Arc2D.PIE);

    // // // end position of the range from the robot
    // // double dx = Math.cos(robot.getYaw() + radar.getRadarAngle()) *
    // // radar.getRadarRange();
    // // double dy = Math.sin(robot.getYaw() + radar.getRadarAngle()) *
    // // radar.getRadarRange();
    // // double newX = robotCurrentX + dx;
    // // double newY = robotCurrentY + dy;

    // // // otherside of the arc
    // // double newX_2 = newX * Math.cos(25) - newY * Math.sin(25);
    // // double newY_2 = newX * Math.sin(25) + newY * Math.cos(25);

    // // for (Landmark landmark : landmarks) {
    // // double pointX = landmark.getLocation().getX();
    // // double pointY = landmark.getLocation().getY();

    // // }
    // }

}
