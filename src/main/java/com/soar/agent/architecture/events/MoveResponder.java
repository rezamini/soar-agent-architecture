package com.soar.agent.architecture.events;

import com.soar.agent.architecture.AppMain;
import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class MoveResponder implements MoveListenerEvent{
    
    DirectionEnum currentDirection;

    @Override
    public void moveCompleted(Move move, Robot robot, RobotAgent robotAgent) {
        currentDirection = DirectionEnum.findByName(move.getDirection());

        if(currentDirection != null){
            robot.setYaw(Math.toRadians(currentDirection.getAngle()));
            robot.getWorld().updateAndMoveAgents(0);

            AppMain.worldPanel.repaint();
            
        }else{
            System.out.println("The move command direction is not found !");
        }
        
    }
    
}
