package com.soar.agent.architecture;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.soar.agent.architecture.events.MoveResponder;
import com.soar.agent.architecture.graph.NodeGraphUI;
import com.soar.agent.architecture.graph.ShortestPathGraphUI;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;
import com.soar.agent.architecture.world.PanelUI;

/**
 * The start of the application/simulator/agent
 *
 */
public class AppMain {

    private Map<String, RobotAgent> agents = new HashMap<String, RobotAgent>();
    private MoveResponder moveResponder = new MoveResponder();
    private static PanelUI panelUI;
    private NodeGraphUI graph;
    private ShortestPathGraphUI matrixGraph;

    public static void main(String[] args) throws IOException {
        // try {
        // panelUI = new PanelUI(new AppMain());
        panelUI = new PanelUI();
        panelUI.initUI();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
    }

    public void updateAgents() {
        final Set<RobotAgent> deadAgents = new HashSet<RobotAgent>(agents.values());

        for (Robot robot : PanelUI.getWorld().getRobots()) {
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

    public void startAgent() {
        for (RobotAgent agent : agents.values()) {
            agent.addListener(moveResponder);
            agent.start();
        }
        PanelUI.getWorldPanel().repaint();
    }

    public void stopAgent() {
        for (RobotAgent agent : agents.values()) {
            agent.stop();
        }
    }

    public void stepAgent() {
        for (RobotAgent agent : agents.values()) {
            agent.addListener(moveResponder);
            agent.step();
        }
        PanelUI.getWorldPanel().repaint();
    }

    public void reInitializeAgent() throws IOException {
        panelUI.initUI();

        for (RobotAgent agent : agents.values()) {
            synchronized (agent) {
                agent.reInitialize();
            }

        }

        // close graph if any instance is open
        closeGraph();

        //close matrix graph if any instance is open
        closeMatrixGraph();

        // close debugger if any instance is open
        closeDebugger();
    }

    public void openDebugger() {
        // open a signle debugger if any agent exists
        if (agents != null && agents.size() > 0) {
            RobotAgent agent = (RobotAgent) agents.values().toArray()[0];
            agent.openDebugger();
        }
    }

    public void closeDebugger() {
        // close a signle debugger if any agent exists
        if (agents != null && agents.size() > 0) {
            RobotAgent agent = (RobotAgent) agents.values().toArray()[0];
            agent.closeDebugger();
        }
    }

    public static void PerformUIRePaint() {
        PanelUI.getWorldPanel().repaint();
    }

    public void startGraph() throws IOException {
        if (agents != null && agents.size() > 0) {
            RobotAgent agent = (RobotAgent) agents.values().toArray()[0];

            // only get one instance from nodeGraphui. Singleton pattern using getInstance
            // method.
            graph = NodeGraphUI.getInstance(agent.getThreadedAgent());
        }
    }

    public void startMatrixGraph() throws IOException {
        if (agents != null && agents.size() > 0) {
            RobotAgent agent = (RobotAgent) agents.values().toArray()[0];

            // only get one instance from matrixGraph. Singleton pattern using getInstance
            // method.
            matrixGraph = ShortestPathGraphUI.getInstance(agent.getThreadedAgent(),
                    PanelUI.getWorld().getMapMatrix(), PanelUI.getWorld());
        }
    }

    public void closeGraph() throws IOException {
        if (graph != null) {
            graph.setFrameVisibility(false);
        }
    }

    public void closeMatrixGraph() throws IOException {
        if (matrixGraph != null) {
            matrixGraph.setFrameVisibility(false);
        }
    }

}
