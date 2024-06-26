package com.soar.agent.architecture.events;

import org.jsoar.kernel.SoarProperties;
import org.jsoar.kernel.io.quick.QMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soar.agent.architecture.enums.CellTypeEnum;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

@Service
public class AreaResponder extends AreaListener {

    @Autowired
    public AreaResponder(Robot robot, RobotAgent robotAgent) {
        super(robot, robotAgent);
        // updateSurroundingMemory();
    }

    @Override
    public void updateAreaMemory() {
        QMemory qMemory = robotAgent.getQMemory();

        // double currentYawDegree = Math.toDegrees(robot.getYaw());
        // qMemory.remove("area.view");
        // qMemory.setString("area.view.type", "none");
        String type = CellTypeEnum.NORMAL.getName();

        // DirectionEnum updatedCellEnum = updateOppositeCell(qMemory);

        for (DirectionEnum directionEnum : DirectionEnum.values()) {
            // if(directionEnum.equals(updatedCellEnum)) continue;

            synchronized (qMemory) {
                // call robot to get surrounding directions with tempYaw
                boolean isObstacle = robot.tempUpdate(0, directionEnum);

                // if (currentYawDegree == directionEnum.getAngle()) {

                //     type = isObstacle ? CellTypeEnum.BLOCK.getName() : CellTypeEnum.NORMAL.getName();
                //     setViewMemory(qMemory, directionEnum.getName(), type, isObstacle);

                // } 
                
                if (isObstacle) {
                    type = CellTypeEnum.BLOCK.getName();
                    setViewMemory(qMemory, directionEnum.getName(), type, isObstacle);

                } else {
                    type = CellTypeEnum.NORMAL.getName();
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

    // update opposit direction of the current cell to None.
    // public void updateOppositeCell(QMemory qMemory, DirectionEnum currentDirectionEnum) {
    //     DirectionEnum currentDirectionOpposite = currentDirectionEnum.getOppositeDirection();

    //     if (currentDirectionOpposite.getName() != null) {
    //         setViewMemory(qMemory, currentDirectionOpposite.getName(), CellTypeEnum.NONE.getName(), false);
    //     }
    // }

    // public void updateOppositeCell(QMemory qMemory, String currentDirection) {
    //     if (currentDirection != null && !currentDirection.equals("")) {
    //         DirectionEnum currentDirectionOpposite = DirectionEnum
    //                 .getOppositeDirection(DirectionEnum.findByName(currentDirection));

    //         if (currentDirectionOpposite.getName() != null) {
    //             setViewMemory(qMemory, currentDirectionOpposite.getName(), CellTypeEnum.NONE.getName(), false);
    //         }
    //     }
    // }

    public DirectionEnum updateOppositeCell(QMemory qMemory) {
        DirectionEnum currentDirectionOpposite = null;

        String formerDirection = qMemory.getString(getAreaSubMemoryPath("locale", null, "direction"));

        if (formerDirection != null && !formerDirection.equals("")) {
            currentDirectionOpposite = DirectionEnum.getOppositeDirection(DirectionEnum.findByName(formerDirection));

            if (currentDirectionOpposite.getName() != null) {
                setViewMemory(qMemory, currentDirectionOpposite.getName(), CellTypeEnum.NORMAL.getName(), false);
            }
        }
        return currentDirectionOpposite;
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
        String formerDirection = null;

        //get the cycle count, this cycle count at this stage is 1 cycle behind what is in the qmemory
        Long cycleCount = robotAgent.getThreadedAgent().getProperties().get(SoarProperties.D_CYCLE_COUNT);
        
        synchronized (qMemory) {

            // at the start of program it doesnt update the former direction and type and only set to to blank values
            if(cycleCount == 0){
                formerType = "";
                formerDirection = "";
            }else{
                formerDirection = qMemory.getString(getAreaSubMemoryPath("locale", null, "direction"));
            }
            // String formerType = qMemory.getString(getAreaSubMemoryPath("view",
            // formerDirection, "type"));

            // set former locale info
            QMemory subFormerMemory = qMemory.subMemory(getAreaSubMemoryPath("former-locale", null, null));
            subFormerMemory.setString("type", formerType);
            subFormerMemory.setString("direction", formerDirection);
        }
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
