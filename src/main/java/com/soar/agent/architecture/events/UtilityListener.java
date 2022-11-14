package com.soar.agent.architecture.events;

import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventManager;

import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public abstract class UtilityListener implements SoarEvent {
    public final RobotAgent robotAgent;
    public final Robot robot;
    public final SoarEventManager events;
    

    public UtilityListener(RobotAgent robotAgent, Robot robot, SoarEventManager events){
        this.robotAgent = robotAgent;
        this.robot = robot;
        this.events = events;

    }
    public abstract void addAllListeners();
    public abstract void initAfterDecisionListener();
    public abstract void initAfterInitListener();
}
