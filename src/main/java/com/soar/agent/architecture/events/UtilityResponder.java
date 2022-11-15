package com.soar.agent.architecture.events;

import org.jsoar.kernel.events.AfterDecisionCycleEvent;
import org.jsoar.kernel.events.AfterInitSoarEvent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;

import com.soar.agent.architecture.enums.CellTypeEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class UtilityResponder extends UtilityListener {
    private MemoryResponder memoryResponder;
    private AreaResponder areaResponder;

    public UtilityResponder(RobotAgent robotAgent, Robot robot) {
        super(robotAgent, robot);

        memoryResponder = new MemoryResponder(robot, robotAgent);
        areaResponder = new AreaResponder(robot, robotAgent);
    }

    @Override
    public void addAllListeners() {
        initAfterInitListener();
        initAfterDecisionListener();

        robotAgent.getEvents().addListener(MemoryResponder.class, event -> {
            memoryResponder.updateRobotMemory();
        });

        robotAgent.getEvents().addListener(AreaResponder.class, event -> {
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
                robotAgent.getEvents().fireEvent(memoryResponder);
            }
        });
    }

    @Override
    public void initAfterInitListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(AfterInitSoarEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {
                robotAgent.getEvents().fireEvent(memoryResponder);
            }
        });
    }

}
