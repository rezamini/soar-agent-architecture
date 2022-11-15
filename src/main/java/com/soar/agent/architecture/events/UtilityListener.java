package com.soar.agent.architecture.events;
import org.jsoar.util.events.SoarEvent;

import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public abstract class UtilityListener implements SoarEvent {
    public final RobotAgent robotAgent;
    public final Robot robot;
    

    public UtilityListener(RobotAgent robotAgent, Robot robot){
        this.robotAgent = robotAgent;
        this.robot = robot;

    }
    public abstract void addAllListeners();
    public abstract void initAfterDecisionListener();
    public abstract void initAfterInitListener();
}
