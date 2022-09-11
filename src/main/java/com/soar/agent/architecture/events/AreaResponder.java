package com.soar.agent.architecture.events;

import java.util.Random;

import org.jsoar.kernel.io.quick.QMemory;

import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class AreaResponder extends SurroundListener {

    public AreaResponder(Robot robot, RobotAgent robotAgent) {
        super(robot, robotAgent);
        updateSurroundingMemory();
    }

    @Override
    public void updateSurroundingMemory() {
        QMemory qMemory = robotAgent.getQMemory();

        setLocaleInfo(qMemory);
        double currentYawDegree = Math.toDegrees(robot.getYaw());

        for (DirectionEnum directionEnum : DirectionEnum.values()) {

            synchronized (qMemory) {
                // call robot to get surrounding directions with tempYaw
                boolean isObstacle = robot.tempUpdate(0, directionEnum);
                QMemory sub = qMemory.subMemory(getSurroundingSubMemoryPath("view", directionEnum.getName()));

                sub.setString("type",
                        isObstacle ? "obstacleCell" : currentYawDegree == directionEnum.getAngle() ? "none" : "normal");
                sub.setInteger("obstacle", isObstacle ? 1 : 0); // 0=false 1=true
            }
        }
    }

    private void setLocaleInfo(QMemory qMemory) {
        synchronized (qMemory) {
            qMemory.subMemory(getSurroundingSubMemoryPath("locale", null)).setString("type", "none");
        }
    }

    private String getSurroundingSubMemoryPath(String subPathName, String direction) {
        return new StringBuilder()
                .append("area")
                .append(".")
                .append(subPathName != null ? subPathName : "")
                .append(subPathName != null ? "." : "")
                .append(direction != null ? direction : "")
                .toString();
    }

}
