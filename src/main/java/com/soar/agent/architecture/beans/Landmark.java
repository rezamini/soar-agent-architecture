package com.soar.agent.architecture.beans;

import java.awt.geom.Point2D;

public class Landmark {
    public String name;
    public Point2D location;

    public Landmark(String name, Point2D location) {
        this.name = name;
        this.location = location;
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
}
