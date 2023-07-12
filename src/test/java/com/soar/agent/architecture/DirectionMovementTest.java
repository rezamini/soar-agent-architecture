package com.soar.agent.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DirectionMovementTest {
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
                world.reset();
                world = mapLoader.load(new ByteArrayInputStream(worldString.getBytes()));

                worldPanel.fit();
                utilityResponder.addAllListeners();
                robot = world.getRobots().iterator().next();
                robotAgent.setRobot(robot);
                worldPanel.repaint();

                //add other specific values
                robot.setSpeed(0.5);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the EAST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the EAST, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToEastWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the WEST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the WEST, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToWestWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to west direction
                robot.setYaw(Math.toRadians(DirectionEnum.WEST.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the NORTH without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the NORTH, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToNorthWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to north direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTH.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the SOUTH without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the SOUTH, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToSouthWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to south direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTH.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the NORTH-EAST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the NORTH-EAST, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToNorthEastWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to north-east direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTHEAST.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the NORTH-WEST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the NORTH-WEST, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToNorthWestWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to north-west direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTHWEST.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the SOUTH-EAST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the SOUTH-EAST, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToSouthEastWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to south-east direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTHEAST.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify the successful movement of the agent to the SOUTH-WEST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the SOUTH-WEST, the agent can move in that direction and reach its destination effectively.")
        public void testAgentMovementToSouthWestWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to south-west direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTHWEST.getAngle()));

                // get the result of the new move if agent has successfuly moved or not
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        //multiple movements tests
        @Test
        @DisplayName("Verify multiple movements of the agent to the EAST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the EAST, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToEastWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   R #").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify multiple movements of the agent to the WEST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the WEST, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToWestWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("       ").append("\n")
                                .append("   # R   ").append("\n")
                                .append("       ");

                createNewWorld(sb.toString());

                // set the yaw to west direction
                robot.setYaw(Math.toRadians(DirectionEnum.WEST.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify multiple movements of the agent to the NORTH without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the NORTH, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToNorthWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("    #   ").append("\n")
                                .append("       ").append("\n")
                                .append("    R   ");

                createNewWorld(sb.toString());

                // set the yaw to north direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTH.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }        

        @Test
        @DisplayName("Verify multiple movements of the agent to the SOUTH without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the SOUTH, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToSouthWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("    R   ").append("\n")
                                .append("        ").append("\n")
                                .append("    #   ");

                createNewWorld(sb.toString());

                // set the yaw to south direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTH.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify multiple movements of the agent to the NORTH-EAST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the NORTH-EAST, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToNorthEastWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("     #####").append("\n")
                                .append("        ").append("\n")
                                .append("    R   ");

                createNewWorld(sb.toString());

                // set the yaw to north-east direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTHEAST.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }

        @Test
        @DisplayName("Verify multiple movements of the agent to the NORTH-WEST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the NORTH-WEST, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToNorthWestWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("#####   ").append("\n")
                                .append("        ").append("\n")
                                .append("    R   ");

                createNewWorld(sb.toString());

                // set the yaw to north-west direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTHWEST.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }   

        @Test
        @DisplayName("Verify multiple movements of the agent to the SOUTH-EAST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the SOUTH-EAST, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToSouthEASTWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("    R     ").append("\n")
                                .append("          ").append("\n")
                                .append("    ######");

                createNewWorld(sb.toString());

                // set the yaw to SOUTH-EAST direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTHEAST.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        } 
        
        @Test
        @DisplayName("Verify multiple movements of the agent to the SOUTH-WEST without encountering any obstacles. " +
                        "The goal is to ensure that when no obstacles exist in the SOUTH-WEST, the agent can move in that direction multiple times and reach its destination effectively.")
        public void testAgentMultipleMovementToSouthWestWithoutObstacle() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("    R     ").append("\n")
                                .append("          ").append("\n")
                                .append("#####     ");

                createNewWorld(sb.toString());

                // set the yaw to south-west direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTHWEST.getAngle()));

                // first move
                robot.updateAndMove(0);

                // second move
                robot.updateAndMove(0);

                //third move and the result
                boolean result = robot.updateAndMove(0);

                // make sure the agent successful moved and assert is true
                assertTrue(result);
        }         
        
}
