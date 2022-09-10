package com.soar.agent.architecture.events;

import org.jsoar.util.events.SoarEventManager;

import com.soar.agent.architecture.AppMain;
import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class MoveResponder implements MoveListenerEvent{
    // public final SoarEventManager events = new SoarEventManager();
    
    DirectionEnum currentDirection;
    // SurroundResponder surroundResponder;

    @Override
    public void moveCompleted(Move move, Robot robot, RobotAgent robotAgent) {
        currentDirection = DirectionEnum.findByName(move.getDirection());
        // surroundResponder = new SurroundResponder(robot, robotAgent);

        if(currentDirection != null){
            robot.setYaw(Math.toRadians(currentDirection.getAngle()));
            robot.getWorld().updateAndMoveAgents(0);
            // surroundResponder.updateSurroundingMemory();
            
            // events.fireEvent(surroundResponder);

            AppMain.worldPanel.repaint();
            
        }else{
            System.out.println("The move command direction is not found !");
        }
        
    }
    
}
