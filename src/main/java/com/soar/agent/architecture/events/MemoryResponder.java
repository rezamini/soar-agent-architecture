package com.soar.agent.architecture.events;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.jsoar.kernel.io.quick.QMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.enums.MemoryEnum;
import com.soar.agent.architecture.enums.UtilitiesEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

@Service
public class MemoryResponder extends MemoryListener {
        private int landmarkCycleCount = 0;
        private boolean allLandmarksReached = false;

        QMemory qMemory = robotAgent.getQMemory();

        @Autowired
        private Move move;

        @Autowired
        public MemoryResponder(Robot robot, RobotAgent robotAgent) {
                super(robot, robotAgent);
        }

        @Override
        public void updateRobotMemory() {

                synchronized (qMemory) {
                        // agent name
                        qMemory.setString(
                                        buildMemoryPath(MemoryEnum.IDENTITY.getName(), MemoryEnum.BASIC_NAME.getName(),
                                                        null),
                                        robot.getName());
                        // mbb
                        qMemory.setDouble(
                                        buildMemoryPath(MemoryEnum.IDENTITY.getName(),
                                                        MemoryEnum.MINIMUM_BOUNDING_BOX.getName(), null),
                                        robot.getShapeArea());

                        final double x = robot.getShape().getBounds2D().getCenterX();
                        final double y = robot.getShape().getBounds2D().getCenterY();

                        // X position
                        qMemory.setDouble(
                                        buildMemoryPath(MemoryEnum.IDENTITY.getName(), MemoryEnum.POSITION.getName(),
                                                        MemoryEnum.POSITION_X.getName()),
                                        x);

                        // Y position
                        qMemory.setDouble(
                                        buildMemoryPath(MemoryEnum.IDENTITY.getName(), MemoryEnum.POSITION.getName(),
                                                        MemoryEnum.POSITION_Y.getName()),
                                        y);

                        // Yaw degree
                        qMemory.setDouble(
                                        buildMemoryPath(MemoryEnum.IDENTITY.getName(), MemoryEnum.POSITION.getName(),
                                                        MemoryEnum.YAW.getName()),
                                        Math.toDegrees(robot.getYaw()));

                        // add and increment landmark-cycle-count here. it will reset to 0 later if
                        // landmark is reached.
                        // qMemory.setString(MemoryEnum.LANDMARK_CYCLE_COUNT.getName(),
                        // String.valueOf(landmarkCycleCount++));

                        // areaResponder.updateAreaMemory();
                        // robotAgent.getEvents().fireEvent(areaResponder);

                        // memory radar has to be called before memory landmarks.
                        updateMemoryRadar();
                        updateMemoryLandmarks();
                        updateLandmarkCycleCount();
                }

        }

        @Override
        public void updateMemoryLandmarks() {
                synchronized (qMemory) {
                        // Main Landmarks Hierarchy
                        QMemory landmarks = qMemory.subMemory(MemoryEnum.LANDMARK_MAIN.getName());

                        for (Entry<Landmark, Boolean> entry : robot.getWorld().getLandmarkMap().entrySet()) {
                                // if landmark is already reached then continue
                                if (entry.getValue() == true)
                                        continue;

                                Landmark landmark = entry.getKey();
                                boolean isAgentReached = robot.getWorld().isLandmarkReached(landmark, robot);

                                // check and validate the shortest path here so if new path need to be
                                // calculated
                                // it could take place before adding to the memory
                                Optional<Map.Entry<Landmark, List<String>>> minLandmarkEntry = robot.getWorld()
                                                .getShortestLandmarkDirections().entrySet()
                                                .stream()
                                                .min(Comparator.comparing(entryValue -> entryValue.getValue().size()));

                                if (minLandmarkEntry.isPresent()) {
                                        boolean validShortestMove = robot.getWorld().validShortestPathMove(
                                                        minLandmarkEntry.get().getKey(),
                                                        landmarkCycleCount, move.getDirection());

                                        // reset the landmark-cycle-count to 0 and update the shortest path if it was
                                        // not valid shortest path move
                                        if (!validShortestMove) {
                                                landmarkCycleCount = 0;
                                                robot.getWorld().updateShortestPath();
                                        }
                                }

                                // check validity of the next shortest path
                                if (minLandmarkEntry.isPresent()) {
                                        boolean validShortestMove = robot.getWorld().validNextShortestPathMove(
                                                        robot,
                                                        minLandmarkEntry.get().getKey(),
                                                        landmarkCycleCount);

                                        // reset the landmark-cycle-count to 0 and update the shortest path if it was
                                        // not a valid next upcoming move
                                        if (!validShortestMove) {
                                                landmarkCycleCount = 0;
                                                robot.getWorld().updateShortestPath();
                                        }
                                }

                                // create a sub landmark with the landmark name - [name of landmark]
                                String subName = MemoryEnum.LANDMARK_SUB.getName()
                                                + UtilitiesEnum.DASHSEPERATOR.getName()
                                                + landmark.getName();
                                QMemory subLandmark = landmarks.subMemory(subName);

                                // get current agent and landmark positions
                                // Agent X position
                                double agentXPose = qMemory
                                                .getDouble(buildMemoryPath(MemoryEnum.IDENTITY.getName(),
                                                                MemoryEnum.POSITION.getName(),
                                                                MemoryEnum.POSITION_X.getName()));

                                // Agent Y position
                                double agentYPose = qMemory
                                                .getDouble(buildMemoryPath(MemoryEnum.IDENTITY.getName(),
                                                                MemoryEnum.POSITION.getName(),
                                                                MemoryEnum.POSITION_Y.getName()));

                                double landmarkX = landmark.getLocation().getX();
                                double landmarkY = landmark.getLocation().getY();

                                // Calculate where and which direction the landmark is located from the agent
                                // current position. Dynamic values & movements
                                String landmarkDirection = calcLandmarkDirection(agentXPose, agentYPose, landmarkX,
                                                landmarkY);

                                String landmarkRelativeDirection = calcRelativeLandmarkDirection(agentXPose, agentYPose,
                                                landmarkX,
                                                landmarkY);

                                // set basic landmark information
                                subLandmark.setString(MemoryEnum.BASIC_NAME.getName(), landmark.getName());
                                subLandmark.setDouble(MemoryEnum.POSITION_X.getName(), landmarkX);
                                subLandmark.setDouble(MemoryEnum.POSITION_Y.getName(), landmarkY);
                                subLandmark.setDouble(MemoryEnum.DISTANCE.getName(),
                                                landmark.getLocation().distance(agentXPose, agentYPose));
                                subLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(), landmarkDirection);
                                subLandmark.setString(MemoryEnum.RELATIVE_DIRECTION_COMMAND.getName(),
                                                landmarkRelativeDirection);
                                subLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
                                                UtilitiesEnum.ACTIVESTATUS.getName());

                                // if its true it means the agent reached at this specific landmark, so remove
                                // the landmark and update the direction command to Here regardless
                                if (isAgentReached) {
                                        subLandmark.setDouble(MemoryEnum.DISTANCE.getName(), 0.0);
                                        subLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(),
                                                        UtilitiesEnum.REACHEDSTATUS.getName());

                                        subLandmark.setString(MemoryEnum.RELATIVE_DIRECTION_COMMAND.getName(),
                                                        UtilitiesEnum.REACHEDSTATUS.getName());

                                        // add a inactive status; this could be helpful in future usuage within .soar
                                        // files
                                        subLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
                                                        UtilitiesEnum.INACTIVESTATUS.getName());

                                        // reset the landmark-cycle-count to 0 so the cycle will start for the new
                                        // landmark
                                        landmarkCycleCount = 0;

                                        // update landmark map to indicate this landmark is reached;
                                        entry.setValue(true);

                                        robot.getWorld().updateShortestPath();
                                }

                        }

                        // call and create shortest path hierarchy after the loop.
                        // because the shortest path directions might have been be re-calculated in the
                        // previous loop
                        createShortPathForLandmarks(landmarks);

                        // set the status of the overal landmarks
                        // Alternative way:
                        // robot.getWorld().getLandmarkMap().values().stream().allMatch(value -> true)
                        allLandmarksReached = !robot.getWorld().getLandmarkMap().containsValue(false);

                        landmarks.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
                                        allLandmarksReached ? UtilitiesEnum.INACTIVESTATUS.getName()
                                                        : UtilitiesEnum.ACTIVESTATUS.getName());
                        // add total landmarks
                        landmarks.setInteger(MemoryEnum.TOTAL_LANDMARKS.getName(),
                                        robot.getWorld().getLandmarkMap().size());
                }

        }

        /*
         * Create a ^path sub Qmemory for the landmarks that contains shortest path(s).
         * it could be use to add to current hierarchy
         */
        private void createShortPathForLandmarks(QMemory currentMemory) {
                robot.getWorld().getLandmarkMap().forEach((landmark, v) -> {

                        String subName = MemoryEnum.LANDMARK_SUB.getName()
                                        + UtilitiesEnum.DASHSEPERATOR.getName()
                                        + landmark.getName();
                        QMemory result = currentMemory.subMemory(subName).subMemory(MemoryEnum.LANDMARK_PATH.getName());

                        // if landmark is already reached then continue and dont update the values
                        if (v == true) {
                                // this will keep the ^path hirerachy otherwise
                                // the ^path is entirely removed(from RobotAgent) if the list is empty
                                result.setString(String.valueOf(0), UtilitiesEnum.REACHEDSTATUS.getName());
                                return;
                        }

                        if (robot.getWorld().getShortestLandmarkDirections() != null) {
                                List<String> landmarkPath = robot.getWorld().getShortestLandmarkDirections()
                                                .get(landmark);

                                if (landmarkPath != null && landmarkPath.size() > 0) {
                                        for (int i = 0; i < landmarkPath.size(); i++) {
                                                result.setString(String.valueOf(i), landmarkPath.get(i));
                                        }
                                }

                        }
                });
        }

        // @Override
        // public void updateAndRemoveMemoryLandmarks() {
        // synchronized (qMemory) {
        // // Main Landmarks Hierarchy
        // QMemory landmarks = qMemory.subMemory(MemoryEnum.LANDMARK_MAIN.getName());

        // for (Iterator<Landmark> iter = robot.getWorld().getLandmarks().iterator();
        // iter.hasNext();) {
        // Landmark landmark = iter.next();
        // boolean isAgentReached = robot.getWorld().isLandmarkReached(landmark, robot);

        // // create a sub landmark with the landmark name - [name of landmark]
        // String subName = MemoryEnum.LANDMARK_SUB.getName()
        // + UtilitiesEnum.DASHSEPERATOR.getName()
        // + landmark.getName();
        // QMemory subLandmark = landmarks.subMemory(subName);

        // // get current agent and landmark positions
        // // Agent X position
        // double agentXPose = qMemory
        // .getDouble(buildMemoryPath(MemoryEnum.IDENTITY.getName(),
        // MemoryEnum.POSITION.getName(),
        // MemoryEnum.POSITION_X.getName()));

        // // Agent Y position
        // double agentYPose = qMemory
        // .getDouble(buildMemoryPath(MemoryEnum.IDENTITY.getName(),
        // MemoryEnum.POSITION.getName(),
        // MemoryEnum.POSITION_Y.getName()));

        // double landmarkX = landmark.getLocation().getX();
        // double landmarkY = landmark.getLocation().getY();

        // // Calculate where and which direction the landmark is located from the agent
        // // current position. Dynamic values & movements
        // String landmarkDirection = calcLandmarkDirection(agentXPose, agentYPose,
        // landmarkX,
        // landmarkY);

        // // set basic landmark information
        // subLandmark.setString(MemoryEnum.BASIC_NAME.getName(), landmark.getName());
        // subLandmark.setDouble(MemoryEnum.POSITION_X.getName(), landmarkX);
        // subLandmark.setDouble(MemoryEnum.POSITION_Y.getName(), landmarkY);
        // subLandmark.setDouble(MemoryEnum.DISTANCE.getName(),
        // landmark.getLocation().distance(agentXPose, agentYPose));
        // subLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(),
        // landmarkDirection);
        // subLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
        // UtilitiesEnum.ACTIVESTATUS.getName());

        // // if its true it means the agent reached at this specific landmark, so
        // remove
        // // the landmark and update the direction command to Here regardless
        // if (isAgentReached) {
        // subLandmark.setDouble(MemoryEnum.DISTANCE.getName(), 0.0);
        // subLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(),
        // UtilitiesEnum.REACHEDSTATUS.getName());

        // // add a inactive status; this could be helpful in future usuage within .soar
        // // files
        // subLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
        // UtilitiesEnum.INACTIVESTATUS.getName());
        // iter.remove();
        // }

        // /* Note: Might need to use below code in the future */
        // // double bearing = Math.toDegrees(Math.atan2(agentYPose - landmarkY,
        // agentXPose
        // // - landmarkX) - robot.getYaw());
        // // while(bearing <= -180.0) bearing += 180.0;
        // // while(bearing >= 180.0) bearing -= 180.0;
        // // subLandmark.setDouble("relative-bearing", bearing);
        // }

        // // set the status of the overal landmarks
        // landmarks.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
        // robot.getWorld().getLandmarks().size() == 0
        // ? UtilitiesEnum.INACTIVESTATUS.getName()
        // : UtilitiesEnum.ACTIVESTATUS.getName());
        // }

        // }

        /*
         * Overloaded method to update memory landmarks, specifically for the detected
         * radar landmarks
         * it accepts list of landmarks, in this case the detectedLandmark by the radar
         */
        @Override
        public void updateMemoryLandmarks(Map<Landmark, Boolean> detectedLandmark) {
                synchronized (qMemory) {
                        // Main Landmarks Hierarchy
                        // QMemory radar = qMemory.subMemory("ranges.range");
                        QMemory radar = qMemory.subMemory(MemoryEnum.RADAR_BASE.getName());

                        QMemory landmarks = radar.subMemory(MemoryEnum.LANDMARK_MAIN.getName());

                        for (Entry<Landmark, Boolean> entry : robot.getWorld().getDetectedRadarLandmarks().entrySet()) {
                                Landmark landmark = entry.getKey();
                                boolean isLive = entry.getValue(); // is within the radar
                                boolean isAgentReached = robot.getWorld().isLandmarkReached(landmark, robot);

                                // create a sub landmark with the landmark name - [name of landmark]
                                String subName = MemoryEnum.LANDMARK_SUB.getName()
                                                + UtilitiesEnum.DASHSEPERATOR.getName()
                                                + landmark.getName();
                                QMemory subLandmark = landmarks.subMemory(subName);

                                // get current agent and landmark positions
                                // Agent X position
                                double agentXPose = qMemory
                                                .getDouble(buildMemoryPath(MemoryEnum.IDENTITY.getName(),
                                                                MemoryEnum.POSITION.getName(),
                                                                MemoryEnum.POSITION_X.getName()));

                                // Agent Y position
                                double agentYPose = qMemory
                                                .getDouble(buildMemoryPath(MemoryEnum.IDENTITY.getName(),
                                                                MemoryEnum.POSITION.getName(),
                                                                MemoryEnum.POSITION_Y.getName()));

                                double landmarkX = landmark.getLocation().getX();
                                double landmarkY = landmark.getLocation().getY();

                                // Calculate where and which direction the landmark is located from the agent
                                // current position. Dynamic values & movements
                                String landmarkDirection = calcLandmarkDirection(agentXPose, agentYPose,
                                                landmarkX,
                                                landmarkY);

                                // add total landmarks
                                landmarks.setInteger(MemoryEnum.TOTAL_LANDMARKS.getName(),
                                                robot.getWorld().getDetectedRadarLandmarks().size());

                                // Calculate where and which direction the landmark is located from the agent
                                // current position. This provide a relative landmark and is not exact and
                                // specific as
                                // the original calcLandmarkDirection method. this could be useful in certain
                                // scenarios
                                // specially to avoid zigzag movements and direction when agent is moving
                                // towards
                                // the landmark
                                String landmarkRelativeDirection = calcRelativeLandmarkDirection(agentXPose, agentYPose,
                                                landmarkX,
                                                landmarkY);

                                // cross check this detected landmark with the all landmark map to see if it is
                                // visited.
                                // the all landmark map already contain a value to indicate this
                                boolean isVisited = robot.getWorld().getLandmarkMap().get(landmark);

                                // only add or update the values if the agent did not visit this landmark
                                if (!isVisited) {

                                        // set basic landmark information
                                        subLandmark.setString(MemoryEnum.BASIC_NAME.getName(), landmark.getName());
                                        subLandmark.setDouble(MemoryEnum.POSITION_X.getName(), landmarkX);
                                        subLandmark.setDouble(MemoryEnum.POSITION_Y.getName(), landmarkY);
                                        subLandmark.setDouble(MemoryEnum.DISTANCE.getName(),
                                                        landmark.getLocation().distance(agentXPose, agentYPose));
                                        subLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(),
                                                        landmarkDirection);
                                        subLandmark.setString(MemoryEnum.RELATIVE_DIRECTION_COMMAND.getName(),
                                                        landmarkRelativeDirection);
                                        subLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
                                                        UtilitiesEnum.ACTIVESTATUS.getName());

                                        if (isAgentReached) {
                                                subLandmark.setDouble(MemoryEnum.DISTANCE.getName(), 0.0);
                                                subLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(),
                                                                UtilitiesEnum.REACHEDSTATUS.getName());

                                                subLandmark.setString(MemoryEnum.RELATIVE_DIRECTION_COMMAND.getName(),
                                                                UtilitiesEnum.REACHEDSTATUS.getName());

                                                subLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
                                                                UtilitiesEnum.INACTIVESTATUS.getName());
                                        }
                                }

                                // update the live data of radar. if any landmark is within radar range
                                if (isLive) {
                                        // create a live and landmarks structure similar to detected landmarks
                                        QMemory live = radar.subMemory(MemoryEnum.RADAR_LIVE.getName());
                                        QMemory liveLandmarks = live.subMemory(MemoryEnum.LANDMARK_MAIN.getName());
                                        QMemory liveSubLandmark = liveLandmarks.subMemory(subName);

                                        // set basic landmark information
                                        liveSubLandmark.setString(MemoryEnum.BASIC_NAME.getName(), landmark.getName());
                                        liveSubLandmark.setDouble(MemoryEnum.POSITION_X.getName(), landmarkX);
                                        liveSubLandmark.setDouble(MemoryEnum.POSITION_Y.getName(), landmarkY);
                                        liveSubLandmark.setDouble(MemoryEnum.DISTANCE.getName(),
                                                        landmark.getLocation().distance(agentXPose, agentYPose));
                                        liveSubLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(),
                                                        landmarkDirection);
                                        liveSubLandmark.setString(MemoryEnum.RELATIVE_DIRECTION_COMMAND.getName(),
                                                        landmarkRelativeDirection);
                                        liveSubLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
                                                        UtilitiesEnum.ACTIVESTATUS.getName());

                                        if (isAgentReached) {
                                                liveSubLandmark.setDouble(MemoryEnum.DISTANCE.getName(), 0.0);
                                                liveSubLandmark.setString(MemoryEnum.DIRECTION_COMMAND.getName(),
                                                                UtilitiesEnum.REACHEDSTATUS.getName());

                                                liveSubLandmark.setString(
                                                                MemoryEnum.RELATIVE_DIRECTION_COMMAND.getName(),
                                                                UtilitiesEnum.REACHEDSTATUS.getName());

                                                liveSubLandmark.setString(UtilitiesEnum.MEMORYSTATUS.getName(),
                                                                UtilitiesEnum.INACTIVESTATUS.getName());

                                                // if is live and is reached then change the value to false so in the
                                                // next round
                                                // this landmark is not added to live structure.
                                                // this will enhance a senario that it sees the landmark and it off the
                                                // radar.
                                                // since the radar is off and has no knowledge we change the value to
                                                // let it know we reached it so it can turn on again.
                                                // a.k.a : we pass the landmark turn on the radar again even if its same
                                                // direction.

                                                entry.setValue(false);
                                        }

                                }
                        }
                }

        }

        @Override
        public void updateMemoryRadar() {
                synchronized (qMemory) {
                        for (int i = 0; i < robot.ranges.length; ++i) {
                                Radar r = robot.ranges[i];
                                // QMemory sub = qMemory.subMemory("ranges.range[" + i + "]");
                                QMemory sub = qMemory.subMemory(MemoryEnum.RADAR_BASE.getName());

                                sub.setInteger(MemoryEnum.RADAR_ID.getName(), i - robot.ranges.length / 2);
                                sub.setDouble(MemoryEnum.RADAR_DISTANCE.getName(), r.getRadarRange());
                                sub.setDouble(MemoryEnum.RADAR_ANGLE.getName(), Math.toDegrees(r.getRadarAngle()));
                                sub.setDouble(MemoryEnum.RADAR_BATTERY.getName(), robot.getRadarBattery());
                                sub.setString(MemoryEnum.RADAR_STATUS.getName(),
                                                robot.isToggleRadar() ? UtilitiesEnum.ON_STATUS.getName()
                                                                : UtilitiesEnum.OFF_STATUS.getName());

                                // update detected landmarks
                                updateMemoryLandmarks(robot.getWorld().getDetectedRadarLandmarks());
                        }
                }

        }

        private void updateLandmarkCycleCount() {
                synchronized (qMemory) {
                        qMemory.setString(MemoryEnum.LANDMARK_CYCLE_COUNT.getName(),
                                        String.valueOf(landmarkCycleCount++));
                }
        }

        // this will try to calculate the relative landmark direction to avoid zigzag
        // situations
        private String calcRelativeLandmarkDirection(double agentX, double agentY, double landmarkX, double landmarkY) {
                double angle = Math.atan2(landmarkY - agentY, landmarkX - agentX);

                // with 10 degrees
                if (angle >= -Math.PI / 8 && angle < Math.PI / 8) {
                        return DirectionEnum.EAST.getName();
                } else if (angle >= Math.PI / 8 && angle < 3 * Math.PI / 8) {
                        return DirectionEnum.NORTHEAST.getName();
                } else if (angle >= 3 * Math.PI / 8 && angle < 5 * Math.PI / 8) {
                        return DirectionEnum.NORTH.getName();
                } else if (angle >= 5 * Math.PI / 8 && angle < 7 * Math.PI / 8) {
                        return DirectionEnum.NORTHWEST.getName();
                } else if (angle >= 7 * Math.PI / 8 || angle < -7 * Math.PI / 8) {
                        return DirectionEnum.WEST.getName();
                } else if (angle >= -7 * Math.PI / 8 && angle < -5 * Math.PI / 8) {
                        return DirectionEnum.SOUTHWEST.getName();
                } else if (angle >= -5 * Math.PI / 8 && angle < -3 * Math.PI / 8) {
                        return DirectionEnum.SOUTH.getName();
                } else if (angle >= -3 * Math.PI / 8 && angle < -Math.PI / 8) {
                        return DirectionEnum.SOUTHEAST.getName();
                } else {
                        return DirectionEnum.EAST.getName();
                }

                // double distance = Math.sqrt((landmarkX - agentX) * (landmarkX - agentX)
                // + (landmarkY - agentY) * (landmarkY - agentY));
                // if (distance < threshold) {
                // return UtilitiesEnum.REACHEDSTATUS.getName();
                // }
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

        private String buildMemoryPath(String arg1, String arg2, String arg3) {
                StringBuilder sb = new StringBuilder();

                sb.append(arg1 != null ? arg1 : "")
                                .append(arg2 != null ? UtilitiesEnum.DOTSEPERATOR.getName() : "")
                                .append(arg2 != null ? arg2 : "")
                                .append(arg3 != null ? UtilitiesEnum.DOTSEPERATOR.getName() : "")
                                .append(arg3 != null ? arg3 : "");

                return sb.toString();
        }

        public boolean isAllLandmarksReached() {
                return allLandmarksReached;
        }

}
