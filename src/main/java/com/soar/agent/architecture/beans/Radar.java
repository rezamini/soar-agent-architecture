package com.soar.agent.architecture.beans;

public class Radar {

    public double radarAngle;
    public double radarRange;

    public Radar(double radarAngle, double radarRange){
        this.radarAngle = radarAngle;
        this.radarRange = radarRange;
    }

    public double getRadarAngle() {
        return radarAngle;
    }
    public void setRadarAngle(double radarAngle) {
        this.radarAngle = radarAngle;
    }
    public double getRadarRange() {
        return radarRange;
    }
    public void setRadarRange(double radarRange) {
        this.radarRange = radarRange;
    }
}
