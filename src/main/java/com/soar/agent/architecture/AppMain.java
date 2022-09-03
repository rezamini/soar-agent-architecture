package com.soar.agent.architecture;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.*;

import org.jsoar.debugger.util.SwingTools;

import com.soar.agent.architecture.loader.MapLoader;
import com.soar.agent.architecture.loader.MapLoader.Result;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;
import com.soar.agent.architecture.world.World;
import com.soar.agent.architecture.world.WorldPanel;

/**
 * The start of the application/simulator/agent
 *
 */
public class AppMain extends JPanel {

    private WorldPanel worldPanel;
    private World world;
    private Map<String, RobotAgent> agents = new HashMap<String, RobotAgent>();

    public AppMain() throws IOException {
        super(new BorderLayout());
        this.worldPanel = new WorldPanel();
        loadMap(new MapLoader().load(AppMain.class.getResource("/map/map.txt")));
        setSimulationToolbar(getWorldPanel());

    }

    public static void main(String[] args) {
        initSwing();
    }

    public void setSimulationToolbar(WorldPanel worldPanel) {
        final JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        add(worldPanel, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);

        bar.add(new AbstractAction("Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAgent();
            }
        });

        bar.add(new AbstractAction("Step") {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAgent();
            }
        });
    }

    public void loadMap(Result loadResult) throws IOException {
        this.world = loadResult.world;
        this.worldPanel.setWorld(this.world);
        this.worldPanel.fit();
        updateAgents();
    }

    private static void initSwing() {
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                AppMain content = new AppMain();
                f.setContentPane(content);
                f.setSize(640, 640);
                f.setVisible(true);
                content.worldPanel.fit();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateAgents() {
        final Set<RobotAgent> deadAgents = new HashSet<RobotAgent>(agents.values());
        for (Robot robot : world.getRobots()) {
            final RobotAgent existing = agents.get(robot.getName());
            if (existing != null) {
                deadAgents.remove(existing);
                existing.setRobot(robot);
            } else {
                final RobotAgent newAgent = new RobotAgent();
                newAgent.setRobot(robot);
                agents.put(robot.getName(), newAgent);
            }
        }

        for (RobotAgent agent : deadAgents) {
            agents.values().remove(agent);
            agent.dispose();
        }
    }

    private void startAgent(){
        for(RobotAgent agent : agents.values())
        {
            agent.start();

        }
    }

    private void stepAgent(){
        for(RobotAgent agent : agents.values())
        {
            agent.step();

        }
    }

    public WorldPanel getWorldPanel() {
        return worldPanel;
    }

    public World getWorld() {
        return world;
    }
}
