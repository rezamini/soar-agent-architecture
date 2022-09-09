package com.soar.agent.architecture.events;

import org.jsoar.kernel.io.quick.QMemory;

import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class SurroundResponder extends SurroundListener {

    public SurroundResponder(Robot robot, RobotAgent robotAgent) {
        super(robot, robotAgent);
        updateSurroundingMemory();
    }

    @Override
    public void updateSurroundingMemory() {
        QMemory qMemory = robotAgent.getQMemory();
        for (DirectionEnum directionEnum : DirectionEnum.values()) {
            synchronized (qMemory) {
                // call robot to get surrounding directions with tempYaw
                boolean isObstacle = robot.tempUpdate(0, directionEnum);

                final QMemory sub = qMemory.subMemory("view." + directionEnum.getName() + "");
                sub.setString("type", "normal");
                sub.setInteger("obstacle", isObstacle ? 1 : 0); // 0=false 1=true
            }
        }
    }

}
