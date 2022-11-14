package com.soar.agent.architecture.events;

import org.jsoar.kernel.events.AfterDecisionCycleEvent;
import org.jsoar.kernel.events.AfterInitSoarEvent;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;
import org.jsoar.util.events.SoarEventManager;

import com.soar.agent.architecture.enums.CellTypeEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class UtilityResponder extends UtilityListener {
    private MemoryResponder memoryResponder;
    private AreaResponder areaResponder;

    public UtilityResponder(RobotAgent robotAgent, Robot robot, SoarEventManager events) {
        super(robotAgent, robot, events);

        memoryResponder = new MemoryResponder(robot, robotAgent);
        areaResponder = new AreaResponder(robot, robotAgent);
    }

    @Override
    public void addAllListeners() {
        initAfterInitListener();
        initAfterDecisionListener();

        events.addListener(MemoryResponder.class, event -> {
            memoryResponder.updateRobotMemory();
        });

        events.addListener(AreaResponder.class, event -> {
            if (robotAgent.getMove() != null && robotAgent.getMove().getDirection() != null && !robotAgent.getMove().getDirection().equals("")) {
                areaResponder.setFormerLocaleInfo(robotAgent.getQMemory(), CellTypeEnum.NONE.getName());
                areaResponder.setLocaleInfo(robotAgent.getQMemory(), robotAgent.getMove().getDirection(), CellTypeEnum.NORMAL.getName());
            }
        });
    }

    @Override
    public void initAfterDecisionListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(AfterDecisionCycleEvent.class, new SoarEventListener() {
            @Override
            public void onEvent(SoarEvent event) {
                events.fireEvent(memoryResponder);
            }
        });
    }

    @Override
    public void initAfterInitListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(AfterInitSoarEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {
                events.fireEvent(memoryResponder);
            }
        });
    }

}
