package com.soar.agent.architecture.events;

import org.jsoar.util.events.SoarEvent;
import org.springframework.stereotype.Repository;

import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

/**
 * SurroundingListener
 */
@Repository
public abstract class AreaListener implements SoarEvent{
    public final RobotAgent robotAgent;
    public final Robot robot;

    public AreaListener(Robot robot, RobotAgent robotAgent){
        this.robotAgent = robotAgent;
        this.robot = robot;
    }

    public abstract void updateAreaMemory();
    
}