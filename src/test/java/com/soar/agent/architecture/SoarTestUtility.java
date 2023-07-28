package com.soar.agent.architecture;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.soar.agent.architecture.events.UtilityResponder;
import com.soar.agent.architecture.loader.MapLoader;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;
import com.soar.agent.architecture.world.World;
import com.soar.agent.architecture.world.WorldPanel;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SoarTestUtility {
    @Autowired
    protected MapLoader mapLoader;

    @Autowired
    protected WorldPanel worldPanel;

    @Autowired
    protected RobotAgent robotAgent;

    @Autowired
    protected UtilityResponder utilityResponder;

    @Autowired
    protected World world;

    @Autowired
    protected Robot robot;

    @BeforeAll
    public static void setUp() {
            System.setProperty("java.awt.headless", "false");
    }

    protected void createNewWorld(String worldString) throws IOException {
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
}
