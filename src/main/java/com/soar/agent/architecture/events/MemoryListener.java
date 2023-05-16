package com.soar.agent.architecture.events;

import java.util.List;
import java.util.Map;

import org.jsoar.util.events.SoarEvent;
import org.springframework.stereotype.Repository;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

@Repository
public abstract class MemoryListener implements SoarEvent{
    public final RobotAgent robotAgent;
    public final Robot robot;
    public AreaResponder areaResponder;

    public MemoryListener(Robot robot, RobotAgent robotAgent, AreaResponder areaResponder){
        this.robot = robot;
        this.robotAgent = robotAgent;
        this.areaResponder = areaResponder;
    }

    public abstract void updateRobotMemory();
    public abstract void updateMemoryLandmarks();
    // public abstract void updateAndRemoveMemoryLandmarks();
    public abstract void updateMemoryRadar();

    //for radar purpose
    public abstract void updateMemoryLandmarks(Map<Landmark, Boolean> detectedLandmark);
}
