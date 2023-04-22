package com.soar.agent.architecture.beans;

import java.awt.Color;
import java.awt.geom.Point2D;

public class Landmark {
    private String name;
    private Point2D location;
    private Color color;

    public Landmark(String name, Point2D location, Color color) {
        this.name = name;
        this.location = location;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
