package com.soar.agent.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

// @RunWith( SpringRunner.class ) //junit 4
@ExtendWith(SpringExtension.class) // junit 5, jupiter
@SpringBootTest(properties = "map.file.name=map-test.txt")
public class AgentTest {
    // @Autowired
    // public MapLoader mapLoader;

    // @Autowired
    // private WorldPanel worldPanel;

    // @Autowired
    // private RobotAgent robotAgent;

    // @Autowired
    // private UtilityResponder utilityResponder;

    // @Autowired
    // private World world;
    
    @Autowired
    private Robot robot;
    
    @BeforeAll
    public static void setUp() {
        System.setProperty("java.awt.headless", "false");
    }
    
    @Test
    public void sampleTest() {
        boolean test = true;
        // System.out.println("XXXXXXXX2XXXXXXXXXX tests :
        // "+robot.getShape().getBounds2D());
        // System.out.println("XXXXXXXXXXXXXXXXXX IN TEST " + robot.getShape().getBounds2D().getCenterX());

        assertEquals(test, true);
    }
}
