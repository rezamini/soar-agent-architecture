package com.soar.agent.architecture.robot;

import com.soar.agent.architecture.world.World;
import java.awt.geom.*;

/**
 * @author ray
 */
public class Robot {
    public final double widthMultiplier = 4.0;
    public final double heightMultiplier = 1.5;
    public final double shapeStartingPoint = 0.4;
    public final Rectangle2D shape = new Rectangle2D.Double(-0.4, -0.4,  shapeStartingPoint * widthMultiplier, shapeStartingPoint * heightMultiplier);
    private final World world;
    private final String name;
    private double yaw;
    private double speed;
    private double turnRate;
    public final double radius = shape.getWidth() * shape.getHeight() + shapeStartingPoint;

    public Robot(World game, String name) {
        this.world = game;
        this.name = name;
    }

    public void move(double newX, double newY) {
        shape.setFrameFromCenter(newX, newY, newX + radius, newY + radius);
    }

    public void update(double dt) {
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

}
