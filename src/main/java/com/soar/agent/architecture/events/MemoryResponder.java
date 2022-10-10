package com.soar.agent.architecture.events;

import java.util.Iterator;

import org.jsoar.kernel.io.quick.QMemory;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.enums.UtilitiesEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public class MemoryResponder extends MemoryListener{

    QMemory qMemory = robotAgent.getQMemory();
    private AreaResponder areaResponder;

    public MemoryResponder(Robot robot, RobotAgent robotAgent) {
        super(robot, robotAgent);
        areaResponder = new AreaResponder(robot, robotAgent);
        
    }

    @Override
    public void updateRobotMemory() {
        
        synchronized (qMemory) {
            qMemory.setString("self.name", robot.getName());
            qMemory.setDouble("self.radius", robot.getRadius()); // change to minimum bounding box - mbb

            final double x = robot.getShape().getCenterX();
            final double y = robot.getShape().getCenterY();
            qMemory.setDouble("self.pose.x", x);
            qMemory.setDouble("self.pose.y", y);
            qMemory.setDouble("self.pose.yaw", Math.toDegrees(robot.getYaw()));

            areaResponder.updateAreaMemory();
            // events.fireEvent(areaResponder);

            //addMemoryLandmarks(qMemory, robot);
            // addMemoryLandmarks(qMemory, robot);
            updateMemoryLandmarks();
        }
        
    }

    @Override
    public void updateMemoryLandmarks() {
        synchronized (qMemory) {
            QMemory landmarks = qMemory.subMemory("landmarks");

            for (Iterator<Landmark> iter = robot.getWorld().getLandmarks().iterator(); iter.hasNext();) {
                Landmark landmark = iter.next();
                boolean isAgentReached = robot.getWorld().isLandmarkReached(landmark, robot);

                // create a sub landmark with the landmark name
                // String subName = "landmark-" + landmark.name + "-" +
                // threadedAgent.getAgent().getRandom().nextInt(99);
                String subName = "landmark-" + landmark.name;

                QMemory subLandmark = landmarks.subMemory(subName);

                // get current agent and landmark positions
                double agentXPose = qMemory.getDouble("self.pose.x");
                double agentYPose = qMemory.getDouble("self.pose.y");
                double landmarkX = landmark.getLocation().getX();
                double landmarkY = landmark.getLocation().getY();

                // Calculate where and which direction the landmark is located from agent
                // current position. Dynamic values & movements
                String landmarkDirection = calcLandmarkDirection(agentXPose, agentYPose, landmarkX, landmarkY);

                // set basic landmark information
                subLandmark.setString("name", landmark.name);
                subLandmark.setDouble("x", landmarkX);
                subLandmark.setDouble("y", landmarkY);
                subLandmark.setDouble("distance", landmark.getLocation().distance(agentXPose, agentYPose));
                subLandmark.setString("direction-command", landmarkDirection);

                // if its true it means the agent reached at this specific landmark, so remove
                // the landmark and update the direction command to Here regardless
                if (isAgentReached) {
                    subLandmark.setDouble("distance", 0.0);
                    subLandmark.setString("direction-command", UtilitiesEnum.REACHEDSTATUS.getName());

                    iter.remove();
                }

                /* Note: Might need to use below code in the future */
                // double bearing = Math.toDegrees(Math.atan2(agentYPose - landmarkY, agentXPose
                // - landmarkX) - robot.getYaw());
                // while(bearing <= -180.0) bearing += 180.0;
                // while(bearing >= 180.0) bearing -= 180.0;
                // subLandmark.setDouble("relative-bearing", bearing);
            }

            // set the status of the overal landmarks
            landmarks.setString("status",
                    robot.getWorld().getLandmarks().size() == 0 ? UtilitiesEnum.INACTIVESTATUS.getName()
                            : UtilitiesEnum.ACTIVESTATUS.getName());
        }
        
    }

    private String calcLandmarkDirectionSimple(double agentX, double agentY, double landmarkX, double landmarkY) {
        String direction = "";
        direction += agentY < landmarkY ? DirectionEnum.NORTH.getName()
                : agentY > landmarkY ? DirectionEnum.SOUTH.getName() : "";

        if (direction.equals("")) {
            direction += agentX < landmarkX ? DirectionEnum.EAST.getName()
                    : agentX > landmarkX ? DirectionEnum.WEST.getName() : "";
        }

        return direction.equals("") ? UtilitiesEnum.REACHEDSTATUS.getName() : direction;
    }

    private String calcLandmarkDirection(double agentX, double agentY, double landmarkX, double landmarkY) {
        String direction = "";
        direction += agentY < landmarkY ? DirectionEnum.NORTH.getName()
                : agentY > landmarkY ? DirectionEnum.SOUTH.getName() : "";

        direction += agentX < landmarkX ? DirectionEnum.EAST.getName()
                : agentX > landmarkX ? DirectionEnum.WEST.getName() : "";

        return direction.equals("") ? UtilitiesEnum.REACHEDSTATUS.getName() : direction;
    }
    
}
