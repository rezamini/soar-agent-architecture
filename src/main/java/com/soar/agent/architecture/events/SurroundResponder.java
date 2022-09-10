package com.soar.agent.architecture.events;

import java.util.Random;

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
        Random randomizer = new Random();
        DirectionEnum enumTest = DirectionEnum.values()[randomizer.nextInt(DirectionEnum.values().length)];

        qMemory.subMemory(getSurroundingSubMemoryPath(null)).setString("type", "");
        // sub.setString("test", "aaa");
        for (DirectionEnum directionEnum : DirectionEnum.values()) {

            synchronized (qMemory) {
                // call robot to get surrounding directions with tempYaw
                boolean isObstacle = robot.tempUpdate(0, directionEnum);

                // QMemory sub = qMemory.subMemory("view."+directionEnum.getName());
                QMemory sub = qMemory.subMemory(getSurroundingSubMemoryPath(directionEnum.getName()));

                // sub2.setString("type", isObstacle ? "none" : "normal");
                sub.setString("type", enumTest.getName().equals(directionEnum.getName()) ? "same" : "normal");
                sub.setInteger("obstacle", isObstacle ? 1 : 0); // 0=false 1=true
            }
        }
    }

    private String getSurroundingSubMemoryPath(String direction) {
        return new StringBuilder()
                .append("view")
                .append(".")
                .append(direction != null ? direction : "")
                .toString();
    }

}
