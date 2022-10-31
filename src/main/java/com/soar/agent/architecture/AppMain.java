package com.soar.agent.architecture;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.soar.agent.architecture.events.MoveResponder;
import com.soar.agent.architecture.graph.NodeGraph;
import com.soar.agent.architecture.graph.NodeGraphUI;
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
    // private static PanelUI panelUI;
    

    public static void main(String[] args) {
        // try {
            // panelUI = new PanelUI(new AppMain());
            PanelUI.initUI();
        // } catch (Exception e) {
        //     e.printStackTrace();
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
            agent.updateRobotMemory();
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
            agent.updateRobotMemory();
            agent.addListener(moveResponder);
            agent.step();
        }
        PanelUI.getWorldPanel().repaint();
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

    public void startGraph(){
        if (agents != null && agents.size() > 0) {
            RobotAgent agent = (RobotAgent) agents.values().toArray()[0];

            //only get one instance from nodeGraphui. Singleton pattern using getInstance method.
            NodeGraphUI graph = NodeGraphUI.getInstance(agent.getThreadedAgent());
        }
    }
}
