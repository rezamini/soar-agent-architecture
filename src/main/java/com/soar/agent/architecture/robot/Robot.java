package com.soar.agent.architecture.robot;

import java.awt.geom.Ellipse2D;

import com.soar.agent.architecture.world.World;

/**
 * @author ray
 */
public class Robot {
    private final World world;
    private final String name;
    private final Ellipse2D shape = new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0);
    private double yaw;
    private double speed;
    private double turnRate;
    private final double radius = 0.4;

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

    public Ellipse2D getShape() {
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

}
