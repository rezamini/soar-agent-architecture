package com.soar.agent.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.events.UtilityResponder;
import com.soar.agent.architecture.loader.MapLoader;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;
import com.soar.agent.architecture.world.World;
import com.soar.agent.architecture.world.WorldPanel;

// @RunWith( SpringRunner.class ) //junit 4
@ExtendWith(SpringExtension.class) // junit 5, jupiter
// @SpringBootTest(properties = "map.file.name=map-test.txt")
@SpringBootTest
public class AgentTest {
    @Autowired
    public MapLoader mapLoader;

    @Autowired
    private WorldPanel worldPanel;

    @Autowired
    private RobotAgent robotAgent;

    @Autowired
    private UtilityResponder utilityResponder;

    @Autowired
    private World world;
    
    @Autowired
    private Robot robot;
    
    @BeforeAll
    public static void setUp() {
        System.setProperty("java.awt.headless", "false");
        // mapLoader.load(getClass().getResource("/map/map-test.txt"));
        // worldPanel.fit();
        // updateAgents();
    }
    
    @Test
    @DisplayName("Move agent to east with obstacle")
    public void testAgentCollisionWithEastObstacle() throws IOException {
        world = mapLoader.load(getClass().getResource("/map/map-test.txt"));
        worldPanel.fit();
        utilityResponder.addAllListeners();
        robot = world.getRobots().iterator().next();
        robotAgent.setRobot(robot);
        worldPanel.repaint();

        //set the yaw to east direction
        robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

        //get the result of the new move if agent has successfuly moved or not
        boolean result = robot.updateAndMove(0);

        assertFalse(result, "Agent Collision and Awareness with obstacle on east side. Agent should not move to east and hit the obstacle");
    }
}
