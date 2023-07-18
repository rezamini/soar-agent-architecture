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

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for one landmark in east direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForOneLandmarkInEastDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("  R   a  ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 1);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for two landmarks in east direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForTwoLandmarksInEastDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("  R   a b  ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 2);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for three landmarks in east direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForThreeLandmarksInEastDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("  R   a b c  ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to east direction
                robot.setYaw(Math.toRadians(DirectionEnum.EAST.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 3);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for one landmark in west direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForOneLandmarkInWestDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("   a  R   ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to west direction
                robot.setYaw(Math.toRadians(DirectionEnum.WEST.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 1);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for two landmarks in west direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForTwoLandmarksInWestDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("  a b  R   ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to west direction
                robot.setYaw(Math.toRadians(DirectionEnum.WEST.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 2);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for three landmarks in west direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForThreeLandmarksInWestDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("  a b c  R   ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to west direction
                robot.setYaw(Math.toRadians(DirectionEnum.WEST.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 3);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for one landmark in north direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForOneLandmarkInNorthDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("   a     ").append("\n")
                                .append("   R   ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to north direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTH.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 1);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for two landmarks in north direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForTwoLandmarksInNorthDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("   b     ").append("\n")
                                .append("   a     ").append("\n")
                                .append("   R   ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to north direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTH.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 2);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for three landmarks in north direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForThreeLandmarksInNorthDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("    c    ").append("\n")
                                .append("    b    ").append("\n")
                                .append("    a    ").append("\n")
                                .append("    R   ").append("\n")
                                .append("        ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to north direction
                robot.setYaw(Math.toRadians(DirectionEnum.NORTH.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 3);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for one landmark in south direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForOneLandmarkInSouthDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("        ").append("\n")
                                .append("   R   ").append("\n")
                                .append("   a     ").append("\n")
                                .append("        ");

                createNewWorld(sb.toString());

                // set the yaw to south direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTH.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 1);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for two landmarks in south direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForTwoLandmarksInSouthDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("   R   ").append("\n")
                                .append("   a    ").append("\n")
                                .append("   b     ").append("\n");

                createNewWorld(sb.toString());

                // set the yaw to south direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTH.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 2);
        }

        @Test
        @DisplayName("Verify the number of landmarks detected by the agent's radar for three landmarks in south direction. "
                        +
                        "The objective is to ensure that the radar accurately identifies the correct number of landmarks within its range."
                        +
                        "This test will check if the reported count of landmarks matches the expected number, providing confidence in the radar's ability to detect and identify landmarks..")
        public void testRadarLandmarkDetectionCountForThreeLandmarksInSouthDirection() throws IOException {
                StringBuilder sb = new StringBuilder();
                sb.append(" ").append("\n")
                                .append("    R   ").append("\n")
                                .append("    a    ").append("\n")
                                .append("    b    ").append("\n")
                                .append("    c    ").append("\n");

                createNewWorld(sb.toString());

                // set the yaw to south direction
                robot.setYaw(Math.toRadians(DirectionEnum.SOUTH.getAngle()));

                // move the agent so the yaw will get updated
                robot.updateAndMove(0);

                // get the detected landmarks map
                Map<Landmark, Boolean> detectedLandmarks = world.getDetectedRadarLandmarks();

                // assert that detected landmark size and actual number of landmarks in the test
                // map are same
                assertEquals(detectedLandmarks.size(), 3);
        }
}
