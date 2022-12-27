package com.soar.agent.architecture.robot;

import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.world.World;
import java.awt.geom.*;

public class Robot {
    public final double widthMultiplier = 4.0;
    public final double heightMultiplier = 1.5;
    public final double shapeStartingPoint = 0.4;
    public final Rectangle2D shape = new Rectangle2D.Double(-0.4, -0.4, shapeStartingPoint * widthMultiplier,
            shapeStartingPoint * heightMultiplier);
    private final World world;
    private final String name;
    private double yaw;
    private double tempYaw; // for checking agent temp surrounding
    private double speed;
    private double turnRate;
    public final double radius = shape.getWidth() * shape.getHeight() + shapeStartingPoint;
    public Radar[] ranges;

    public Arc2D radarArc = new Arc2D.Double(0, 0, 0, 0, -10.0, 25.0, Arc2D.PIE);

    private double radarBattery;
    private boolean toggleRadar;

    public Robot(World game, String name) {
        this.world = game;
        this.name = name;
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
        shape.setFrameFromCenter(newX, newY, newX + radius, newY + radius);
    }

    public void updateAndMove(double dt) {
        yaw += dt * turnRate;
        // yaw += dt;

        while (yaw < 0.0)
            yaw += 2.0 * Math.PI;
        while (yaw > 2.0 * Math.PI)
            yaw -= 2.0 * Math.PI;

        final double dx = Math.cos(yaw) * speed;
        final double dy = Math.sin(yaw) * speed;

        final double newX = shape.getCenterX() + dx;
        final double newY = shape.getCenterY() + dy;
        if (!world.willCollide(this, newX, newY)) {
            move(newX, newY);
        }

        //update radar related data only if radar is on
        
        if(toggleRadar){
            updateRadarBattery();

            //check the toggle again maybe battery has level has changed it
            if(toggleRadar){
                for (Radar range : ranges) {
                    range.setRadarRange(world.getCollisionRange(this, range.getRadarAngle() + yaw));
                    // world.radarDetectLandmark(this, range);
                }    
            }
        }
    }

    public boolean tempUpdate(double dt, DirectionEnum currentDirection) {

        tempYaw = Math.toRadians(currentDirection.getAngle());
        tempYaw += dt * turnRate;

        while (tempYaw < 0.0)
            tempYaw += 2.0 * Math.PI;
        while (tempYaw > 2.0 * Math.PI)
            tempYaw -= 2.0 * Math.PI;

        final double dx = Math.cos(tempYaw) * speed;
        final double dy = Math.sin(tempYaw) * speed;

        final double newX = shape.getCenterX() + dx;
        final double newY = shape.getCenterY() + dy;

        // check to see if it will collide in every enum direction
        boolean isObstacle = world.willCollide(this, newX, newY);

        return isObstacle;
    }

    public void updateRadarBattery() {
        // simply update and decrease radar battery on every move by 0.5
        if (radarBattery > 0) {
            radarBattery = radarBattery - 0.5;

            //incase after the calculation the battery is at 0 or lower then switch off the radar
            if(radarBattery <= 0 ) toggleRadar = false;
        }else{
            //off the radar if battery is not above 0
            toggleRadar = false;
        }
    }

    public World getWorld() {
        return world;
    }

    public String getName() {
        return name;
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

    public double getRadius() {
        return radius;
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

    public Arc2D getRadarArc() {
        return radarArc;
    }

    public void setRadarArc(Arc2D radarArc) {
        this.radarArc = radarArc;
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


}
