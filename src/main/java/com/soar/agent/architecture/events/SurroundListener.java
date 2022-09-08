package com.soar.agent.architecture.events;

import org.jsoar.util.events.SoarEvent;

import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

/**
 * SurroundingListener
 */
public abstract class SurroundListener implements SoarEvent{
    public final RobotAgent robotAgent;
    public final Robot robot;

    public SurroundListener(Robot robot, RobotAgent robotAgent){
        this.robotAgent = robotAgent;
        this.robot = robot;
    }

    public abstract void updateSurroundingMemory();
    
}