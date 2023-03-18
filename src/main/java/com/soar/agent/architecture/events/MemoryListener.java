package com.soar.agent.architecture.events;

import java.util.List;
import java.util.Map;

import org.jsoar.util.events.SoarEvent;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public abstract class MemoryListener implements SoarEvent{
    public final RobotAgent robotAgent;
    public final Robot robot;

    public MemoryListener(Robot robot, RobotAgent robotAgent){
        this.robot = robot;
        this.robotAgent = robotAgent;
    }

    public abstract void updateRobotMemory();
    public abstract void updateMemoryLandmarks();
    // public abstract void updateAndRemoveMemoryLandmarks();
    public abstract void updateMemoryRadar();

    //for radar purpose
    public abstract void updateMemoryLandmarks(Map<Landmark, Boolean> detectedLandmark);
}
