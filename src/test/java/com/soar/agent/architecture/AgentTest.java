package com.soar.agent.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
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
    }

    private void createNewWorld(String worldString) throws IOException {
        // world = mapLoader.load(getClass().getResource("/map/map-test.txt"));
        world = mapLoader.load(new ByteArrayInputStream(worldString.getBytes()));

        worldPanel.fit();
        utilityResponder.addAllListeners();
        robot = world.getRobots().iterator().next();
        robotAgent.setRobot(robot);
        worldPanel.repaint();
    }

    @Test
    @DisplayName("Verify the accurate response of the agent when encountering an obstacle in the EAST. " +
            "The objective is to ensure that when an obstacle exists in the EAST, the agent refrains from moving in that direction.")
    public void testAgentResponseToEastObstacle() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append("\n")
                .append(" ###### ").append("\n")
                .append("    R## ").append("\n")
                .append("  ");

        createNewWorld(sb.toString());

        // set the yaw to east direction
        robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

        // get the result of the new move if agent has successfuly moved or not
        boolean result = robot.updateAndMove(0);

        //make sure the agent did not move and assert is false
        assertFalse(result);
    }

    @Test
    @DisplayName("Verify the accurate response of the agent when encountering an obstacle in the WEST. " +
            "The objective is to ensure that when an obstacle exists in the WEST, the agent refrains from moving in that direction.")
    public void testAgentResponseToWestObstacle() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append("\n")
                .append(" ###### ").append("\n")
                .append("    ##R  ").append("\n")
                .append("  ");

        createNewWorld(sb.toString());

        // set the yaw to west direction
        robot.setYaw(Math.toRadians(DirectionEnum.WEST.getAngle()));

        // get the result of the new move if agent has successfuly moved or not
        boolean result = robot.updateAndMove(0);

        //make sure the agent did not move and assert is false
        assertFalse(result);
    }

    @Test
    @DisplayName("Verify the accurate response of the agent when encountering an obstacle in the NORTH. " +
            "The objective is to ensure that when an obstacle exists in the NORTH, the agent refrains from moving in that direction.")
    public void testAgentResponseToNorthObstacle() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append("\n")
                .append(" ###### ").append("\n")
                .append("    R  ").append("\n")
                .append("  ");

        createNewWorld(sb.toString());

        // set the yaw to north direction
        robot.setYaw(Math.toRadians(DirectionEnum.NORTH.getAngle()));

        // get the result of the new move if agent has successfuly moved or not
        boolean result = robot.updateAndMove(0);

        //make sure the agent did not move and assert is false
        assertFalse(result);
    }

    @Test
    @DisplayName("Verify the accurate response of the agent when encountering an obstacle in the SOUTH. " +
            "The objective is to ensure that when an obstacle exists in the SOUTH, the agent refrains from moving in that direction.")
    public void testAgentResponseToSouthObstacle() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append("\n")
                .append(" ").append("\n")
                .append("    R  ").append("\n")
                .append("  ######");

        createNewWorld(sb.toString());

        // set the yaw to south direction
        robot.setYaw(Math.toRadians(DirectionEnum.SOUTH.getAngle()));

        // get the result of the new move if agent has successfuly moved or not
        boolean result = robot.updateAndMove(0);

        //make sure the agent did not move and assert is false
        assertFalse(result);
    }
}
