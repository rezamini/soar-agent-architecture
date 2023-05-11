package com.soar.agent.architecture.world;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.*;
import org.jsoar.debugger.util.SwingTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.soar.agent.architecture.AppMain;
import com.soar.agent.architecture.events.MoveResponder;
import com.soar.agent.architecture.graph.NodeGraphUI;
import com.soar.agent.architecture.graph.ShortestPathGraph;
import com.soar.agent.architecture.graph.ShortestPathGraphUI;
import com.soar.agent.architecture.loader.MapLoader;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;

@Component
public class PanelUI extends JPanel {

    // @Autowired
    // private World worldAuto;
    
    @Autowired
    private MapLoader mapLoader;
    
    @Autowired
    private WorldPanel worldPanel;

    @Autowired
    private RobotAgent robotAgent;

    // @Autowired
    // private AppMain appMain;

    private final JFrame mainFrame;

    @Autowired
    private World world;
    private final JToolBar toolBar;

    private Map<String, RobotAgent> agents = new HashMap<String, RobotAgent>();

    @Autowired
    private MoveResponder moveResponder;
    
    private NodeGraphUI graph;
    private ShortestPathGraphUI matrixGraph;
    
    public PanelUI() throws IOException {
        super(new BorderLayout());
        mainFrame = new JFrame();
        toolBar = new JToolBar("Draggable Toolbar");
    }

    @PostConstruct
    private void init(){

        try {
            mapLoader.load(getClass().getResource("/map/map.txt"));
            worldPanel.fit();
            updateAgents();
            setSimulationToolbar(worldPanel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initUI() {
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                // PanelUI content = new PanelUI();
                mainFrame.setContentPane(this);
                mainFrame.setSize(800, 800);
                mainFrame.setVisible(true);

                
                worldPanel.fit();
                worldPanel.repaint();
                worldPanel.revalidate();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private JToggleButton createButton(String mainImagName, String clickImageName, boolean isSelectedIcon) {
        JToggleButton newButton = new JToggleButton();
        ImageIcon btnMainImg = new ImageIcon(PanelUI.class.getResource("/images/" + mainImagName + ".png"));
        newButton.setIcon(btnMainImg);

        ImageIcon btnClickedImg = new ImageIcon(PanelUI.class.getResource("/images/" + clickImageName + ".png"));
        newButton.setRolloverIcon(btnClickedImg);
        newButton.setPressedIcon(btnClickedImg);

        // selected icon color will not change unless toggled or clicked twice
        if (isSelectedIcon) {
            newButton.setSelectedIcon(btnClickedImg);
        }

        newButton.setOpaque(false);
        newButton.setContentAreaFilled(false);
        // newButton.setBorderPainted(false);

        return newButton;
    }

    public void setSimulationToolbar(WorldPanel worldPanel) {
        
        toolBar.setFloatable(true);
        toolBar.setOpaque(true);
        add(toolBar, BorderLayout.SOUTH);
        add(worldPanel, BorderLayout.CENTER);     
        setBackground(Color.LIGHT_GRAY);

        // Run button
        JToggleButton runButton = createButton("start", "start-clicked", true);
        runButton.addActionListener(new AbstractAction("Start") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (runButton.isSelected()) {
                    startAgent();
                } else {
                    stopAgent();
                }
            }
        });

        runButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // set selected icon image after every click
                ImageIcon btnClickedImg = new ImageIcon(PanelUI.class.getResource("/images/start-clicked.png"));
                runButton.setSelectedIcon(btnClickedImg);
                repaint();
            }
        });
        runButton.setToolTipText("Start Agent");
        toolBar.add(runButton);

        // Stop Button
        JToggleButton stopButton = createButton("stop", "stop-clicked", false);
        stopButton.addActionListener(new AbstractAction("Stop") {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAgent();
            }
        });

        stopButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // change and set the Run button icon to default everytime stop button is
                // clicked.
                // it changes the color of the run button.
                runButton.setSelectedIcon(null);
                runButton.setSelected(false);
                repaint();
            }
        });
        stopButton.setToolTipText("Stop Agent");
        toolBar.add(stopButton);

        // Step Button
        JToggleButton stepButton = createButton("step", "step-clicked", false);
        stepButton.addActionListener(new AbstractAction("Step") {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAgent();
            }
        });
        stepButton.setToolTipText("Step Agent");
        toolBar.add(stepButton);

        // Debugger
        JToggleButton debuggerButton = createButton("debug", "debug-clicked", true);
        debuggerButton.addActionListener((event) -> {
            openDebugger();
        });
        debuggerButton.setToolTipText("Open Agent Debugger");
        toolBar.add(debuggerButton);

        // Graph
        JToggleButton graphButton = createButton("graph", "graph-clicked", true);
        graphButton.addActionListener((event) -> {
            try {
                startGraph();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        graphButton.setToolTipText("Open Memory Visualisation");
        toolBar.add(graphButton);

        // TEST SHORT PATH
        JToggleButton graphButton2 = createButton("path", "path-clicked", true);
        graphButton2.addActionListener((event) -> {
            try {
                startMatrixGraph();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        graphButton2.setToolTipText("Open Map Matrix Visualisation");
        toolBar.add(graphButton2);

        // push the rest of the icons/button to the end of the toolbar; right of toolbar
        toolBar.add(Box.createGlue());

        // Re-Initialize / Reset
        JToggleButton resetButton = createButton("reset", "reset-clicked", false);
        resetButton.addActionListener((event) -> {
            try {
               reInitializeAgent();
            } catch (Exception e) {
                // TODO: handle exception
            }
        });
        graphButton2.setToolTipText("Re-Initialize Map & Agent State");
        toolBar.add(resetButton);

        // bar.add(new AbstractAction("Graph") {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // appMain.startGraph();
        // }
        // });

        // bar.add(new AbstractAction("Stop") {

        // @Override
        // public void actionPerformed(ActionEvent e) {
        // appMain.stopAgent();

        // }

        // });

        // bar.add(new AbstractAction("Step") {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // appMain.stepAgent();
        // }
        // });

        // JCheckBox debuggerCheckBox = new JCheckBox("Open Debugger");
        // debuggerCheckBox.addActionListener((event) -> {
        // if (debuggerCheckBox.isSelected()) {
        // appMain.openDebugger();
        // } else {
        // appMain.closeDebugger();
        // }
        // });
        // bar.add(debuggerCheckBox);

    }

    // public void loadMap(World worldResult) throws IOException {
    //     // world = worldResult;
    //     // worldPanel.setWorld(world);
    //     worldPanel.fit();
    //     updateAgents();
    // }

    // public static WorldPanel getWorldPanel() {
    //     return worldPanel;
    // }

    public void updateAgents() {
        final Set<RobotAgent> deadAgents = new HashSet<RobotAgent>(agents.values());

        for (Robot robot : world.getRobots()) {
            final RobotAgent existing = agents.get(robot.getName());
            if (existing != null) {
                deadAgents.remove(existing);
                existing.setRobot(robot);
            } else {
                // final RobotAgent newAgent = new RobotAgent();

                // set an instance of shortest path here otherwise it will not be initialised at
                // first load.
                // this is before memory updates etc
                try {
                    robot.getWorld().setShortestPathGraph(
                            new ShortestPathGraph(world.getMapMatrix(), world));
                    robot.getWorld().setShortestPathGraphComplete(
                            new ShortestPathGraph(world.getCompleteMapMatrix(), world));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                robotAgent.setRobot(robot);
                agents.put(robot.getName(), robotAgent);
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
        worldPanel.repaint();
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
        worldPanel.repaint();
    }

    public void reInitializeAgent() throws IOException {

        mainFrame.setVisible(false);
        mainFrame.dispose();

        // panelUI = new PanelUI();
        initUI();

        // jpanel
        revalidate();
        repaint();

        // jframe
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();

        for (RobotAgent agent : agents.values()) {
            synchronized (agent) {
                agent.reInitialize();
            }

        }

        // close graph if any instance is open
        closeGraph();

        // close matrix graph if any instance is open
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
                    world.getMapMatrix(), world);
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

    // public static World getWorld() {
    //     return world;
    // }

    public JFrame getMainFrame() {
        return mainFrame;
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

}