package com.soar.agent.architecture.events;

import java.util.Random;

import org.jsoar.kernel.io.quick.QMemory;

import com.soar.agent.architecture.enums.CellTypeEnum;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class AreaResponder extends AreaListener {

    public AreaResponder(Robot robot, RobotAgent robotAgent) {
        super(robot, robotAgent);
        // updateSurroundingMemory();
    }

    @Override
    public void updateAreaMemory() {
        QMemory qMemory = robotAgent.getQMemory();

        double currentYawDegree = Math.toDegrees(robot.getYaw());
        //  qMemory.remove("area.view");
        qMemory.setString("area.view.type", "none");
        for (DirectionEnum directionEnum : DirectionEnum.values()) {

            synchronized (qMemory) {
                // call robot to get surrounding directions with tempYaw
                boolean isObstacle = robot.tempUpdate(0, directionEnum);

                // create a area/surround sub memory
                QMemory sub = qMemory.subMemory(getAreaSubMemoryPath("view", directionEnum.getName(), null));

                // initiate every cell as normal and change if required
                String currentType = CellTypeEnum.NORMAL.getName();

                if (currentYawDegree == directionEnum.getAngle()) {
                    // set former cell info before changing the cell type
                    String formerDirection = setFormerLocaleInfo(qMemory, currentType);

                    if(formerDirection != null){
                        qMemory.subMemory(getAreaSubMemoryPath("view", formerDirection, null)).setString("type", "none");;
                    }

                    // update current cell info
                    currentType = CellTypeEnum.NONE.getName();
                    setLocaleInfo(qMemory, directionEnum.getName(), currentType);

                } else if (isObstacle) {
                    currentType = CellTypeEnum.BLOCK.getName();
                }
                
                sub.setString("type", currentType);
                sub.setInteger("obstacle", isObstacle ? 1 : 0); // 0=false 1=true

            }
            
        }
    }

    private String setFormerLocaleInfo(QMemory qMemory, String formerType) {
        String formerDirection;
        synchronized (qMemory) {
            formerDirection = qMemory.getString(getAreaSubMemoryPath("locale", null, "direction"));
            // String formerType = qMemory.getString(getAreaSubMemoryPath("view", formerDirection, "type"));

            QMemory subFormerMemory = qMemory.subMemory(getAreaSubMemoryPath("former-locale", null, null));
            subFormerMemory.setString("type", formerType);
            subFormerMemory.setString("direction", formerDirection);
        }

        return formerDirection;
    }
    

    private void setLocaleInfo(QMemory qMemory, String directionName, String currentType) {

        synchronized (qMemory) {
            // String currentType = qMemory.getString(getAreaSubMemoryPath("view", directionName, "type"));
            QMemory subLocaleMemory = qMemory.subMemory(getAreaSubMemoryPath("locale", null, null));

            subLocaleMemory.setString("type", currentType);
            subLocaleMemory.setString("direction", directionName);
        }
    }

    private String getAreaSubMemoryPath(String subPathName, String direction, String endNode) {
        return new StringBuilder()
                .append("area")
                .append(".")
                .append(subPathName != null ? subPathName : "")
                .append(direction != null ? "." : "")
                .append(direction != null ? direction : "")
                .append(endNode != null ? "." : "")
                .append(endNode != null ? endNode : "")
                .toString();
    }

}
