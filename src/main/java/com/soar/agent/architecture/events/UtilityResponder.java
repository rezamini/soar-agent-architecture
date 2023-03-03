package com.soar.agent.architecture.events;

import org.jsoar.kernel.events.AfterDecisionCycleEvent;
import org.jsoar.kernel.events.AfterInitSoarEvent;
import org.jsoar.kernel.events.InputEvent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;

import com.soar.agent.architecture.enums.CellTypeEnum;
import com.soar.agent.architecture.enums.DirectionEnum;
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
        startAfterInitSoarEventListener();
        startAfterDecisionCycleEventListener();

        robotAgent.getEvents().addListener(MemoryResponder.class, event -> {
            memoryResponder.updateRobotMemory();
        });

        robotAgent.getEvents().addListener(AreaResponder.class, event -> {
            if (robotAgent.getMove() != null && robotAgent.getMove().getDirection() != null
                    && !robotAgent.getMove().getDirection().equals("")) {
                areaResponder.setFormerLocaleInfo(robotAgent.getQMemory(), CellTypeEnum.NONE.getName());
                areaResponder.setLocaleInfo(robotAgent.getQMemory(), robotAgent.getMove().getDirection(),
                        CellTypeEnum.NORMAL.getName());
            }
        });
    }

    @Override
    public void startAfterDecisionCycleEventListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(AfterDecisionCycleEvent.class, new SoarEventListener() {
            @Override
            public void onEvent(SoarEvent event) {
                robotAgent.getEvents().fireEvent(memoryResponder);
            }
        });
    }

    @Override
    public void startAfterInitSoarEventListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(AfterInitSoarEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {
                //calculate the shortest landmarks(s) path in the start
                robot.getWorld().updateShortestPath();

                robotAgent.getEvents().fireEvent(memoryResponder);

                // update the starting locale of the agent

                double currentYaw = robot.getYaw(); // get the starting yaw

                // convert back the yaw to degree and get the starting direction by the
                // converted yaw which is the angle degree
                DirectionEnum startingDirection = DirectionEnum.findByAngleDegree((int) Math.toDegrees(currentYaw));

                robotAgent.getMove().setDirection(startingDirection.getName());
                robotAgent.getEvents().fireEvent(areaResponder);
            }
        });
    }

    @Override
    public void startInputEventListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(InputEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {
                // InputEvent ie = (InputEvent) event;
            }
        });
    }

    @Override
    public void startOutputEventListener() {

    }

    @Override
    public void startWorkingMemoryChangedEventListener() {

    }

}
