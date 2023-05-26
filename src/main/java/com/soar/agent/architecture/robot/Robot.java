package com.soar.agent.architecture.robot;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.world.World;

import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Robot {
    @Autowired
    private World world;

    @Value("${default.param.value:agent}")
    private String name;

    public final double widthMultiplier = 4.0;
    public final double heightMultiplier = 1.5;
    public final double shapeStartingPoint = 0.4;
    public final double shapeWidth = shapeStartingPoint * widthMultiplier;
    public final double shapeHeight = shapeStartingPoint * heightMultiplier;
    public final Rectangle2D shape = new Rectangle2D.Double(-0.4, -0.4, shapeWidth, shapeHeight);
    private double yaw;
    private double tempYaw; // for checking agent temp surrounding
    private double speed;
    private double turnRate;
    // public final double radius = shape.getWidth() * shape.getHeight() +
    public final double shapeArea = shapeWidth * shapeHeight;
    public Radar[] ranges;
    private Path2D shapeRadar = new Path2D.Double();

    private double radarBattery;
    private boolean toggleRadar;
    private double batteryDeduction = 0.1;
    private DecimalFormat batteryDecimalFormat = new DecimalFormat("0.#");
    private Path2D tempShape = new Path2D.Double();;

    public Robot() {
        radarBattery = 100;
        toggleRadar = true;

        // initMultipleRobotRadar();
        initSingleRobotRadar();
    }

    private void initMultipleRobotRadar() {
        ranges = new Radar[4];
        int slot = -(ranges.length / 2);
        for (int i = 0; i < ranges.length; ++i) {
            ranges[i] = new Radar(slot * (Math.PI / ranges.length));
            ranges[i].setRadarRange(i);
            slot++;
        }
    }

    private void initSingleRobotRadar() {
        ranges = new Radar[1];
        ranges[0] = new Radar(0);
        ranges[0].setRadarRange(0);
    }

    public void move(double newX, double newY) {
        // shape.setFrameFromCenter(newX, newY, newX + radius, newY + radius);
        shape.setFrameFromCenter(newX, newY, newX + shapeWidth, newY + shapeHeight);

        // calc radar range and data in order to have radar data such as "live" at the
        // beginning of agent phase
        // this have to be called here to calc the radar from the agent new position and
        // have the radar data from the beginning of agent lifecycle.
        // alternative way is to call it from updateAndMove but the agent will not have
        // some of the radar data at the beginning and
        // some senarios might lack some data (for example if we need the radar to be on
        // if it sees a landmark
        // and there is one in the current direction, then the radar will not turn on
        // because of lack of initialise radar calc. as per
        // explore-map-radar_1.0.soar)
        calcRadar();

    }

    public void updateAndMove(double dt) {
        yaw += dt * turnRate;

        while (yaw < 0.0)
            yaw += 2.0 * Math.PI;
        while (yaw > 2.0 * Math.PI)
            yaw -= 2.0 * Math.PI;

        final double dx = Math.round(Math.cos(yaw)) * speed;
        final double dy = Math.round(Math.sin(yaw)) * speed;

        // final double dx = Math.cos(yaw) * speed;
        // final double dy = Math.sin(yaw) * speed;

        final double newX = shape.getCenterX() + dx;
        final double newY = shape.getCenterY() + dy;

        // double[] dimensions = calcAgentDimensionsForDirection(dx, dy);
        Path2D tempAgentShape = createTempAgentShape(newX + dx, newY + dy, yaw);

        if (!world.willCollide(this, newX, newY, tempAgentShape)) {
            move(newX, newY);
            // tempShape = new Path2D.Double();
            // tempShape = tempAgentShape;

            updateMapMatrix(newX, newY);
            updateCompleteMapMatrix(newX, newY, dx, dy, tempAgentShape);
        }
    }

    public void updateMapMatrix(double newX, double newY) {
        int agentMatrixX = (int) Math.round((newX - 1) / 2);
        int agentMatrixY = (int) Math.round((newY - 1) / 2);

        // set the agent matrix location on a separate field that could be used in other
        // places without looping the entire map if required
        world.setAgentMapMatrixX(agentMatrixX);
        world.setAgentMapMatrixY(agentMatrixY);

        // update the agent location on the map matrix
        int[][] currentMapMatrix = world.getMapMatrix();
        for (int i = 0; i < currentMapMatrix.length; i++) {
            for (int j = 0; j < currentMapMatrix[i].length; j++) {

                // check if the cell is 3 which represents an agent
                if (currentMapMatrix[i][j] == 3) {
                    // update the current position to empty space
                    currentMapMatrix[i][j] = 0;

                    // set the new agent position
                    currentMapMatrix[agentMatrixY][agentMatrixX] = 3;
                    break;
                }
            }
        }

        // update landmark locations again
        updateMatrixLandmarkInfo(currentMapMatrix);

        world.setMapMatrix(currentMapMatrix);
    }

    private void updateMatrixLandmarkInfo(int[][] currentMapMatrix) {
        if (world.getLandmarkMap() != null) {
            for (Map.Entry<Landmark, Boolean> entry : world.getLandmarkMap().entrySet()) {
                double cx = entry.getKey().getLocation().getX();
                double cy = entry.getKey().getLocation().getY();

                int x = (int) ((cx - 2.0 / 2.0) / 2.0); // simplified matrix convert
                int y = (int) ((cy - 2.0 / 2.0) / 2.0); // simplified matrix convert

                // if the agent is currently not there update it back to landmark
                if (currentMapMatrix[y][x] != 3) {
                    currentMapMatrix[y][x] = 2;
                }
            }
        }
    }

    public void updateCompleteMapMatrix(double newX, double newY, double dx, double dy, Path2D tempAgentShape) {
        // newX = newX * 2.0 + 2.0 / 2.0;
        // newY = newY * 2.0 + 2.0 / 2.0;
        // int column = (int) Math.round(tempAgentShape.getBounds().getCenterX() - 1);
        // int row = (int) Math.round(tempAgentShape.getBounds().getCenterY() - 1);
        int column = (int) Math.round(newX -1);
        int row = (int) Math.round(newY-1);
        int column2 = (int) Math.round(column + dx + dx);
        int row2 = (int) Math.round(row + dy + dy);

        // int agentMatrixX = (int) tempAgentShape.getBounds().getCenterX() ;
        // int agentMatrixY = (int) tempAgentShape.getBounds().getCenterY();
        // int agentMatrixX2 = (int) (agentMatrixX + dx + dx);
        // int agentMatrixY2 = (int) (agentMatrixY + dy + dy);

        // set the agent matrix location on a separate field that could be used in other
        // places without looping the entire map if required
        world.setAgentMapMatrixX2(column);
        world.setAgentMapMatrixY2(row);

        // update the agent location on the map matrix
        int[][] currentMapMatrix = world.getCompleteMapMatrix();

        for (int i = 0; i < currentMapMatrix.length; i++) {
            for (int j = 0; j < currentMapMatrix[i].length; j++) {

                // check if the cell is 3 which represents an agent
                if (currentMapMatrix[i][j] == 3) {
                    // update the current position to empty space
                    currentMapMatrix[i][j] = 0;
                }
            }
        }

        currentMapMatrix[row][column] = 3;

        if (row2 >= 0 && row2 < currentMapMatrix.length &&
                column2 >= 0 && column2 < currentMapMatrix[row2].length) {
            currentMapMatrix[row2][column2] = 3;
            world.setSecondAgentMapMatrixX2(column2);
            world.setSecondAgentMapMatrixY2(row2);
        }

        // int toFillX = 0;
        // int fromFillX = (int) Math.round(newX);

        // int toFillY = 0;
        // int fromFillY = (int) Math.round(newY);

        // // left or right
        // if ((dx > 0 || dx < 0) && dy == 0) {
        // toFillY = (int) Math.round(newY);

        // if(dx > 0){
        // fromFillX = (int) Math.round(newX);
        // toFillX = (int) Math.round(newX + 2);
        // }

        // if(dx < 0){
        // fromFillX = (int) Math.round(newX - 2);
        // toFillX = (int) Math.round(newX);
        // }

        // Arrays.fill(currentMapMatrix[fromFillY], fromFillX, toFillX, 3);
        // Arrays.fill(currentMapMatrix[toFillY], fromFillX, toFillX, 3);

        // // up or down
        // } else if ((dy > 0 || dy < 0) && dx == 0) {
        // toFillX = (int) Math.round(newX + 1);
        // fromFillY = (int) Math.round(newY);

        // if(dy > 0){
        // toFillY = (int) Math.round(newY + 1);
        // }

        // if(dy < 0){
        // toFillY = (int) Math.round(newY - 1);
        // }

        // Arrays.fill(currentMapMatrix[fromFillY], fromFillX, toFillX, 3);
        // Arrays.fill(currentMapMatrix[toFillY], fromFillX, toFillX, 3);

        // // top right || top left
        // } else if ((dx > 0 && dy > 0) || (dx < 0 && dy > 0)) {
        // if(dx > 0 && dy > 0){
        // fromFillY = (int) Math.round(newY);
        // toFillY = (int) Math.round(newY + 1);

        // fromFillX = (int) Math.round(newX);
        // toFillX = (int) Math.round(newX + 1);

        // Arrays.fill(currentMapMatrix[fromFillY], fromFillX, toFillX, 3);
        // Arrays.fill(currentMapMatrix[toFillY], fromFillX + 1, toFillX + 1, 3);
        // }

        // if(dx < 0 && dy > 0){
        // fromFillY = (int) Math.round(newY);
        // toFillY = (int) Math.round(newY + 1);

        // fromFillX = (int) Math.round(newX - 1);
        // toFillX = (int) Math.round(newX);

        // Arrays.fill(currentMapMatrix[fromFillY], fromFillX, toFillX, 3);
        // Arrays.fill(currentMapMatrix[toFillY], fromFillX - 1, toFillX - 1, 3);
        // }

        // // bottom right || bottom left
        // } else if ((dx > 0 && dy < 0) || (dx < 0 && dy < 0)) {
        // if(dx < 0 && dy < 0){
        // fromFillY = (int) Math.round(newY);
        // toFillY = (int) Math.round(newY - 1);

        // fromFillX = (int) Math.round(newX - 1);
        // toFillX = (int) Math.round(newX);

        // Arrays.fill(currentMapMatrix[fromFillY], fromFillX, toFillX, 3);
        // Arrays.fill(currentMapMatrix[toFillY], fromFillX - 1, toFillX - 1, 3);
        // }

        // if(dx > 0 && dy < 0){
        // fromFillY = (int) Math.round(newY);
        // toFillY = (int) Math.round(newY - 1);

        // fromFillX = (int) Math.round(newX);
        // toFillX = (int) Math.round(newX + 1);

        // Arrays.fill(currentMapMatrix[fromFillY], fromFillX, toFillX, 3);
        // Arrays.fill(currentMapMatrix[toFillY], fromFillX + 1, toFillX + 1, 3);
        // }
        // }

        // world.setSecondAgentMapMatrixX((int) Math.round(agentMatrixX + dx + dx ));
        // world.setSecondAgentMapMatrixY((int) Math.round(agentMatrixY + dy + dy ));

        world.setCompleteMapMatrix(currentMapMatrix);
    }

    // public Map<Path2D, Boolean> tempNewLocationUpdate2(DirectionEnum
    // currentDirection, double x, double y) {
    // Map<Path2D, Boolean> result = new HashMap<Path2D, Boolean>();

    // double tempNewLocationYaw = Math.toRadians(currentDirection.getAngle());

    // while (tempNewLocationYaw < 0.0)
    // tempNewLocationYaw += 2.0 * Math.PI;
    // while (tempNewLocationYaw > 2.0 * Math.PI)
    // tempNewLocationYaw -= 2.0 * Math.PI;

    // double dx = Math.round(Math.cos(tempNewLocationYaw)) * speed;
    // double dy = Math.round(Math.sin(tempNewLocationYaw)) * speed;

    // double newX = x + dx;
    // double newY = y + dy;

    // Path2D tempAgentShape = createTempAgentShape(newX, newY, tempNewLocationYaw);

    // // check to see if it will collide in every enum direction that is passed
    // // throught this method
    // boolean isObstacle = world.willCollide(this, newX, newY, tempAgentShape);

    // result.put(tempAgentShape, isObstacle);
    // return result;
    // }

    // * This is temporary making a new agent at a new location and checks if it
    // hits an obstacle at that location. it could be used to get back the temporary
    // agent shape and send it back in order to calculate the new position with
    // previous temporary position. in other words it could be used to
    // simulate/virutalised a path such as the shortest path or convertion of
    // shortest path direction*/
    public Map<Path2D, Boolean> tempNewLocationUpdate(DirectionEnum currentDirection, Path2D currentShape) {
        Map<Path2D, Boolean> result = new HashMap<Path2D, Boolean>();

        double tempNewLocationYaw = Math.toRadians(currentDirection.getAngle());

        while (tempNewLocationYaw < 0.0)
            tempNewLocationYaw += 2.0 * Math.PI;
        while (tempNewLocationYaw > 2.0 * Math.PI)
            tempNewLocationYaw -= 2.0 * Math.PI;

        double dx = Math.round(Math.cos(tempNewLocationYaw)) * speed;
        double dy = Math.round(Math.sin(tempNewLocationYaw)) * speed;

        double centerX = currentShape != null ? currentShape.getBounds().getCenterX() : shape.getCenterX();
        double centerY = currentShape != null ? currentShape.getBounds().getCenterY() : shape.getCenterY();

        double newX = centerX + dx;
        double newY = centerY + dy;

        Path2D tempAgentShapeForCollision = createTempAgentShape(newX + dx, newY + dy, tempNewLocationYaw);
        Path2D tempAgentShapeForTracking = createTempAgentShape(newX, newY, tempNewLocationYaw);

        // if(currentDirection.getName().equalsIgnoreCase("northwest")){
        // tempShape = new Path2D.Double();
        // tempShape = tempAgentShapeForCollision;
        // }

        // check to see if it will collide in every enum direction that is passed
        // throught this method
        boolean isObstacle = world.willCollide(this, newX, newY, tempAgentShapeForCollision);
        
        result.put(tempAgentShapeForTracking, isObstacle);
        return result;
    }

    public boolean tempUpdate(double dt, DirectionEnum currentDirection) {
        tempYaw = Math.toRadians(currentDirection.getAngle());
        tempYaw += dt * turnRate;

        while (tempYaw < 0.0)
            tempYaw += 2.0 * Math.PI;
        while (tempYaw > 2.0 * Math.PI)
            tempYaw -= 2.0 * Math.PI;

        final double dx = Math.round(Math.cos(tempYaw)) * speed;
        final double dy = Math.round(Math.sin(tempYaw)) * speed;

        final double newX = shape.getCenterX() + dx + dx;
        final double newY = shape.getCenterY() + dy + dy;

        double[] dimensions = calcAgentDimensionsForDirection(dx, dy);
        Path2D tempAgentShape = createTempAgentShape(newX, newY, tempYaw);

        // if (currentDirection.getName().equalsIgnoreCase("northwest")) {
        //     tempShape = tempAgentShape;
        // }

        // check to see if it will collide in every enum direction that is passed
        // throught this method
        boolean isObstacle = world.willCollide(this, newX, newY, tempAgentShape);

        return isObstacle;
    }

    public void updateRadarBattery() {
        // simply update and decrease radar battery on every move by batteryDeduction
        if (radarBattery > 0) {
            radarBattery = Double.valueOf(batteryDecimalFormat.format(radarBattery - batteryDeduction));

            // incase after the calculation the battery is at 0 or lower then switch off the
            // radar
            if (radarBattery <= 0)
                toggleRadar = false;
        } else {
            // off the radar if battery is not above 0
            toggleRadar = false;
        }
    }

    public void calcRadar() {

        if (toggleRadar) {
            updateRadarBattery();

            // check the toggle again maybe battery has level has changed it
            if (toggleRadar) {
                for (Radar range : ranges) {
                    range.setRadarRange(world.getCollisionRange(this, range.getRadarAngle() + yaw, true));
                    // world.radarDetectLandmark(this, range);
                }
            }
        } else {
            for (Radar range : ranges) {
                range.setRadarRange(world.getCollisionRange(this, range.getRadarAngle() + yaw, false));
                // world.radarDetectLandmark(this, range);
            }
            // world.getDetectedRadarLandmarks().replaceAll((k, v) -> v = false);
        }
    }

    private Path2D createTempAgentShape(double newX, double newY, double angle) {

        // newX = newX - shapeWidth / 2;
        // newY = newY - shapeHeight / 2;

        // Rectangle2D rect = new Rectangle2D.Double(newX, newY, shapeWidth, shapeHeight);

        Rectangle2D rect = new Rectangle2D.Double();
        rect.setFrameFromCenter(newX, newY, newX + (shapeWidth + 0.3) / 2.0, newY + (shapeHeight + 0.3) / 2.0);

        // rect.setFrameFromCenter(newX, newY, newX + (shapeWidth ) / 2, newY +
        // (shapeHeight ) / 2);

        // rect.setFrameFromCenter(newX, newY, newX + shapeWidth / 2, newY + shapeHeight
        // / 2);
        // rect.setFrameFromCenter(newX, newY, newX + shapeWidth - 0.1, newY +
        // shapeHeight - 0.1);

        AffineTransform transform = new AffineTransform();
        transform.rotate(angle, rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

        Path2D tempAgentShape = new Path2D.Double(rect, transform);

        return tempAgentShape;
    }

    /*
     * Create temp agent shape with actual yaw/direction of the agent from the
     * original shape
     */
    public Path2D createTempAgentShape() {
        Rectangle2D rect = new Rectangle2D.Double();
        // subtracting 0.1 is to make it slightly smaller so the tight edges and
        // situations will be passed
        // and if used for collision it will not be so strict
        rect.setFrameFromCenter(shape.getCenterX(), shape.getCenterY(),
                shape.getCenterX() + shapeWidth - 0.1, shape.getCenterY() + shapeHeight - 0.1);

        AffineTransform transform = new AffineTransform();
        transform.rotate(yaw, rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

        Path2D tempAgentShape = new Path2D.Double(rect, transform);

        return tempAgentShape;
    }

    /*
     * Calculate the agent width and height based on the new direction which is
     * caluculated in new dx and dy
     */
    private double[] calcAgentDimensionsForDirection(double dx, double dy) {
        double result[] = new double[2];

        double agentWidth = 0;
        double agentHeight = 0;
        // (dx > 0 && dy > 0)

        // left or right
        if ((dx > 0 || dx < 0) && dy == 0) {
            agentWidth = shapeWidth;
            agentHeight = shapeHeight;

            // up or down
        } else if ((dy > 0 || dy < 0) && dx == 0) {
            agentHeight = shapeWidth;
            agentWidth = shapeHeight;

            // top right || top left
        } else if ((dx > 0 && dy > 0) || (dx < 0 && dy > 0)) {
            agentWidth = shapeHeight + 0.1;
            agentHeight = shapeWidth + 0.1;

            // bottom right || bottom left
        } else if ((dx > 0 && dy < 0) || (dx < 0 && dy < 0)) {
            agentWidth = shapeHeight + 0.1;
            agentHeight = shapeWidth + 0.1;
        }

        result[0] = agentWidth - 0.1;
        result[1] = agentHeight - 0.1;

        return result;
    }

    public Path2D calcShapeRadar(double agentX, double agentY, double radarRange) {
        shapeRadar.reset();
        shapeRadar.moveTo(agentX, agentY);
        shapeRadar.append(new Arc2D.Double(agentX - radarRange, agentY - radarRange,
                2 * radarRange, 2 * radarRange, Math.toDegrees(-yaw) - 10, 25, Arc2D.PIE), true);

        return shapeRadar;
    }

    public World getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rectangle2D getShape() {
        return shape;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getTurnRate() {
        return turnRate;
    }

    public void setTurnRate(double turnRate) {
        this.turnRate = turnRate;
    }

    public double getShapeArea() {
        return shapeArea;
    }

    public double getShapeWidth() {
        return shapeWidth;
    }

    public double getShapeHeight() {
        return shapeHeight;
    }

    public double getWidthMultiplier() {
        return widthMultiplier;
    }

    public double getHeightMultiplier() {
        return heightMultiplier;
    }

    public double getShapeStartingPoint() {
        return shapeStartingPoint;
    }

    public double getRadarBattery() {
        return radarBattery;
    }

    public void setRadarBattery(double radarBattery) {
        this.radarBattery = radarBattery;
    }

    public boolean isToggleRadar() {
        return toggleRadar;
    }

    public void setToggleRadar(boolean toggleRadar) {
        this.toggleRadar = toggleRadar;
    }

    public Path2D getShapeRadar() {
        return shapeRadar;
    }

    public void setShapeRadar(Path2D shapeRadar) {
        this.shapeRadar = shapeRadar;
    }

    public double getTempYaw() {
        return tempYaw;
    }

    public Path2D getTempShape() {
        return tempShape;
    }

    public void setTempShape(Path2D tempShape) {
        this.tempShape = tempShape;
    }
}
