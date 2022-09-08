package com.soar.agent.architecture.events;

import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

public interface MoveListenerEvent {
    void moveCompleted(Move move, Robot robot, RobotAgent robotAgent);
}
