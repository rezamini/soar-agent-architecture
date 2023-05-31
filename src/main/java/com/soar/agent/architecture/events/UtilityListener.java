package com.soar.agent.architecture.events;
import org.jsoar.util.events.SoarEvent;
import org.springframework.stereotype.Repository;

import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;
import com.soar.agent.architecture.world.WorldPanel;

@Repository
public abstract class UtilityListener implements SoarEvent {
    public final RobotAgent robotAgent;
    public final Robot robot;
    public MemoryResponder memoryResponder;
    public AreaResponder areaResponder;
    public WorldPanel worldPanel;
    

    public UtilityListener(RobotAgent robotAgent, Robot robot, MemoryResponder memoryResponder, AreaResponder areaResponder, WorldPanel worldPanel){
        this.robotAgent = robotAgent;
        this.robot = robot;
        this.memoryResponder = memoryResponder;
        this.areaResponder = areaResponder;
        this.worldPanel = worldPanel;
    }
    public abstract void addAllListeners();

    //AfterInitSoarEvent.class
    public abstract void startAfterInitSoarEventListener();

    //AfterDecisionCycleEvent.class
    public abstract void startAfterDecisionCycleEventListener();

    //InputEvent.class
    public abstract void startInputEventListener();

    //OutputEvent.class
    public abstract void startOutputEventListener();

    //WorkingMemoryChangedEvent.class
    public abstract void startWorkingMemoryChangedEventListener();
    
}
