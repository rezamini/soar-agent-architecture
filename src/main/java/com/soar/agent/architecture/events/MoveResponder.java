package com.soar.agent.architecture.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soar.agent.architecture.AppMain;
import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;
import com.soar.agent.architecture.world.WorldPanel;

@Service
public class MoveResponder implements MoveListenerEvent{

    @Autowired
    private WorldPanel worldPanel;

    @Autowired
    private AreaResponder areaResponder;
    
    DirectionEnum currentDirection;

    @Override
    @Autowired
    public void moveCompleted(Move move, Robot robot, RobotAgent robotAgent) {
        currentDirection = DirectionEnum.findByName(move.getDirection());
        System.out.println("XXXXXXXXXXXXXXXX IN MOVE : "+move.getDirection());
        if(currentDirection != null){
            robot.setYaw(Math.toRadians(currentDirection.getAngle()));
            robot.getWorld().updateAndMoveAgents(0);
    
            worldPanel.repaint();

            // robotAgent.getEvents().fireEvent(areaResponder);
            
        }else{
            System.out.println("The move command direction is not found !");
        }
        
    }
    
}
