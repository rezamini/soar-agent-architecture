package com.soar.agent.architecture.events;

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
        // qMemory.remove("area.view");
        // qMemory.setString("area.view.type", "none");
        String type = CellTypeEnum.NORMAL.getName();
        for (DirectionEnum directionEnum : DirectionEnum.values()) {

            synchronized (qMemory) {
                // call robot to get surrounding directions with tempYaw
                boolean isObstacle = robot.tempUpdate(0, directionEnum);

                if(isObstacle){
                    type = CellTypeEnum.BLOCK.getName();
                }

                // initiate every cell as normal and change if required
                //String currentType = CellTypeEnum.NORMAL.getName();

                // set former cell info before changing the cell type
                //setFormerLocaleInfo(qMemory, currentType);
                // updateOppositeCell(qMemory, formerDirection, directionEnum, isObstacle);

                if (currentYawDegree == directionEnum.getAngle()) {

                    //setLocaleInfo(qMemory, directionEnum.getName(), currentType);
                    updateOppositeCell(qMemory, directionEnum.getName());
                    setViewMemory(qMemory, directionEnum.getName(), type, isObstacle);

                } else if (isObstacle) {
                    setViewMemory(qMemory, directionEnum.getName(), type, isObstacle);

                } else {
                    setViewMemory(qMemory, directionEnum.getName(), type, isObstacle);
                }

            }

        }
    }

    private void setViewMemory(QMemory qMemory, String directionName, String type, boolean isObstacle) {
        synchronized (qMemory) {
            // create a area/surround sub memory
            boolean hasPath = qMemory.hasPath(getAreaSubMemoryPath("view", directionName, "type"));

            if (!hasPath) {
                QMemory sub = qMemory.subMemory(getAreaSubMemoryPath("view", directionName, null));
                sub.setString("type", type);
                sub.setInteger("obstacle", isObstacle ? 1 : 0); // 0=false 1=true
            }
        }
    }

    //update opposit direction of the current cell to None.
    public void updateOppositeCell(QMemory qMemory, DirectionEnum currentDirectionEnum) {
        DirectionEnum currentDirectionOpposite = currentDirectionEnum.getOppositeDirection();
        
        if (currentDirectionOpposite.getName() != null) {
            setViewMemory(qMemory, currentDirectionOpposite.getName(), CellTypeEnum.NONE.getName(), false);
        }
    }

    public void updateOppositeCell(QMemory qMemory, String currentDirection) {
        if(currentDirection != null && !currentDirection.equals("")){
            DirectionEnum currentDirectionOpposite = DirectionEnum.getOppositeDirection(DirectionEnum.findByName(currentDirection));
        
            if (currentDirectionOpposite.getName() != null) {
                setViewMemory(qMemory, currentDirectionOpposite.getName(), CellTypeEnum.NONE.getName(), false);
            }
        }
    }

        // private void updateOppositeCell(QMemory qMemory, String formerDirection,
    // DirectionEnum currentDirectionEnum, boolean isObstacle) {
    // // DirectionEnum result = null;

    // if (formerDirection != null) {

    // DirectionEnum formerDirectionOpposite = DirectionEnum
    // .getOppositeDirection(DirectionEnum.findByName(formerDirection));
    // DirectionEnum currentDirectionOpposite =
    // currentDirectionEnum.getOppositeDirection();

    // if (formerDirectionOpposite != null
    // &&
    // formerDirectionOpposite.getName().equalsIgnoreCase(currentDirectionOpposite.getName()))
    // {

    // setViewMemory(qMemory, formerDirectionOpposite.getName(),
    // CellTypeEnum.NONE.getName(), isObstacle);

    // // result = formerDirectionOpposite;
    // }
    // }

    // // return result;
    // }

    public String setFormerLocaleInfo(QMemory qMemory, String formerType) {
        String formerDirection;
        synchronized (qMemory) {
            formerDirection = qMemory.getString(getAreaSubMemoryPath("locale", null, "direction"));
            // String formerType = qMemory.getString(getAreaSubMemoryPath("view",
            // formerDirection, "type"));
            
            // set former locale info
            QMemory subFormerMemory = qMemory.subMemory(getAreaSubMemoryPath("former-locale", null, null));
            subFormerMemory.setString("type", formerType);
            subFormerMemory.setString("direction", formerDirection);
        }
        System.out.println("XXXXXXXXXXXX FORMER : ");
        System.out.println(formerDirection);
        return formerDirection;
    }
    

    public void setLocaleInfo(QMemory qMemory, String directionName, String currentType) {

        synchronized (qMemory) {
            // String currentType = qMemory.getString(getAreaSubMemoryPath("view",
            // directionName, "type"));
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
