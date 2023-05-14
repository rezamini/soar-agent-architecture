package com.soar.agent.architecture.beans;

import java.awt.Color;
import java.awt.geom.Point2D;

public class Landmark {
    private String name;
    private Point2D location;
    private Color paintColor;
    private String colorName;

    public Landmark(String name, Point2D location, Color paintColor, String colorName) {
        this.name = name;
        this.location = location;
        this.paintColor = paintColor;
        this.colorName = colorName;
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

    public Color getPaintColor() {
        return paintColor;
    }

    public void setPaintColor(Color paintColor) {
        this.paintColor = paintColor;
    }

    public String getColorName(){
        return colorName;
    }

    public void setColorName(String colorName){
        this.colorName = colorName;
    }
}
