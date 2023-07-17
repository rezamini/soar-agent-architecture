package com.soar.agent.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.soar.agent.architecture.beans.Landmark;
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
public class RadarTest {
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

        private DecimalFormat batteryDecimalFormat = new DecimalFormat("0.#");

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

                // add other specific values
                robot.setSpeed(0.5);
                robot.setBatteryDeduction(0.1);
        }

        @Test
        @DisplayName("Verify the battery percentage of the agent after performing one movement. " +
                        "The objective is to ensure that the agent's radar accurately updates the battery percentage based on the"
                        +
                        "energy consumed during movement. This test will check if the battery percentage decreases by the expected amount after the agent moves.")
        public void testAgentRadarBatteryAfterOneMovement() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("    R   ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // expected battery, original battery before movement - deduction
                double expectedBattery = Double.valueOf(
                                batteryDecimalFormat.format(robot.getRadarBattery() - robot.getBatteryDeduction()));

                // move the agent
                robot.updateAndMove(0);

                // get the battery result
                double batteryResult = robot.getRadarBattery();

                // make sure the agent battery result and expected result are same
                assertEquals(batteryResult, expectedBattery);
        }

        @Test
        @DisplayName("Verify the battery percentage of the agent after performing two movements. " +
                        "The objective is to ensure that the agent's radar accurately updates the battery percentage based on the"
                        +
                        "energy consumed during movement. This test will check if the battery percentage decreases by the expected amount after the agent moves.")
        public void testAgentRadarBatteryAfterTwoMovements() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("    R   ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // original battery percentage before any movement
                double originalBattery = robot.getRadarBattery();

                // move the agent 2 times
                robot.updateAndMove(0);
                robot.updateAndMove(0);

                // get the battery result
                double batteryResult = robot.getRadarBattery();

                // expected battery after all movements
                double expectedBattery = Double.valueOf(
                                batteryDecimalFormat.format(originalBattery - (robot.getBatteryDeduction() * 2)));

                // make sure the agent battery result and expected result are same
                assertEquals(batteryResult, expectedBattery);
        }

        @Test
        @DisplayName("Verify the battery percentage of the agent after performing three movements. " +
                        "The objective is to ensure that the agent's radar accurately updates the battery percentage based on the"
                        +
                        "energy consumed during movement. This test will check if the battery percentage decreases by the expected amount after the agent moves.")
        public void testAgentRadarBatteryAfterThreeMovements() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("         ").append("\n")
                                .append("    R    ").append("\n")
                                .append("         ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // original battery percentage before any movement
                double originalBattery = robot.getRadarBattery();

                // move the agent 3 times
                robot.updateAndMove(0);
                robot.updateAndMove(0);
                robot.updateAndMove(0);

                // get the battery result
                double batteryResult = robot.getRadarBattery();

                // expected battery after all movements
                double expectedBattery = Double.valueOf(
                                batteryDecimalFormat.format(originalBattery - (robot.getBatteryDeduction() * 3)));

                // make sure the agent battery result and expected result are same
                assertEquals(batteryResult, expectedBattery);
        }

        @Test
        @DisplayName("Verify the battery percentage of the agent after performing one movement with one battery reduction. "
                        +
                        "The objective is to ensure that the agent's radar accurately updates the battery percentage based on the"
                        +
                        "energy consumed during movement. This test will check if the battery percentage decreases by the expected amount after the agent moves.")
        public void testAgentRadarBatteryAfterOneMovementWithOneReduction() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("    R   ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // reset the batery redction values
                robot.setBatteryDeduction(1);

                // expected battery, original battery before movement - deduction
                double expectedBattery = Double.valueOf(robot.getRadarBattery() - robot.getBatteryDeduction());

                // move the agent
                robot.updateAndMove(0);

                // get the battery result
                double batteryResult = robot.getRadarBattery();

                // make sure the agent battery result and expected result are same
                assertEquals(batteryResult, expectedBattery);
        }

        @Test
        @DisplayName("Verify the battery percentage of the agent after performing two movements with one battery reduction. "
                        +
                        "The objective is to ensure that the agent's radar accurately updates the battery percentage based on the"
                        +
                        "energy consumed during movement. This test will check if the battery percentage decreases by the expected amount after the agent moves.")
        public void testAgentRadarBatteryAfterTwoMovementsWithOneReduction() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("    R   ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // reset the batery redction values
                robot.setBatteryDeduction(1);

                // original battery percentage before any movement
                double originalBattery = robot.getRadarBattery();

                // move the agent 2 times
                robot.updateAndMove(0);
                robot.updateAndMove(0);

                // get the battery result
                double batteryResult = robot.getRadarBattery();

                // expected battery after all movements
                double expectedBattery = Double.valueOf(
                                batteryDecimalFormat.format(originalBattery - (robot.getBatteryDeduction() * 2)));

                // make sure the agent battery result and expected result are same
                assertEquals(batteryResult, expectedBattery);
        }

        @Test
        @DisplayName("Verify the battery percentage of the agent after performing three movements with one battery reduction. "
                        +
                        "The objective is to ensure that the agent's radar accurately updates the battery percentage based on the"
                        +
                        "energy consumed during movement. This test will check if the battery percentage decreases by the expected amount after the agent moves.")
        public void testAgentRadarBatteryAfterThreeMovementsWithOneReduction() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("         ").append("\n")
                                .append("    R    ").append("\n")
                                .append("         ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // reset the batery redction values
                robot.setBatteryDeduction(1);

                // original battery percentage before any movement
                double originalBattery = robot.getRadarBattery();

                // move the agent 3 times
                robot.updateAndMove(0);
                robot.updateAndMove(0);
                robot.updateAndMove(0);

                // get the battery result
                double batteryResult = robot.getRadarBattery();

                // expected battery after all movements
                double expectedBattery = Double.valueOf(originalBattery - (robot.getBatteryDeduction() * 3));

                // make sure the agent battery result and expected result are same
                assertEquals(batteryResult, expectedBattery);
        }
}
