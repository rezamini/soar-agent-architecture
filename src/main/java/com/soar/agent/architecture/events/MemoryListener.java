package com.soar.agent.architecture.events;

import org.jsoar.util.events.SoarEvent;

import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public abstract class MemoryListener implements SoarEvent{
    public final RobotAgent robotAgent;
    public final Robot robot;

    public MemoryListener(RobotAgent robotAgent, Robot robot){
        this.robotAgent = robotAgent;
        this.robot = robot;
    }

    public abstract void updateRobotMemory();
    public abstract void updateMemoryLandmarks();
}
