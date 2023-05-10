package com.soar.agent.architecture.world;

import java.io.IOException;

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
import com.soar.agent.architecture.loader.MapLoader;

@Component
public class PanelUI extends JPanel {

    // @Autowired
    // private World worldAuto;
    
    @Autowired
    private MapLoader mapLoader;

    private final JFrame mainFrame;
    private static WorldPanel worldPanel;
    private static World world;
    private AppMain appMain = new AppMain();
    private final JToolBar toolBar;

    public PanelUI() throws IOException {
        super(new BorderLayout());
        mainFrame = new JFrame();
        toolBar = new JToolBar("Draggable Toolbar");

        worldPanel = new WorldPanel();
        
        setSimulationToolbar(worldPanel);

        
    }

    @PostConstruct
    private void init(){

        try {
            loadMap(mapLoader.load(getClass().getResource("/map/map.txt")));
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

                
                PanelUI.worldPanel.fit();
                
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
                    appMain.startAgent();
                } else {
                    appMain.stopAgent();
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
                appMain.stopAgent();
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
                appMain.stepAgent();
            }
        });
        stepButton.setToolTipText("Step Agent");
        toolBar.add(stepButton);

        // Debugger
        JToggleButton debuggerButton = createButton("debug", "debug-clicked", true);
        debuggerButton.addActionListener((event) -> {
            appMain.openDebugger();
        });
        debuggerButton.setToolTipText("Open Agent Debugger");
        toolBar.add(debuggerButton);

        // Graph
        JToggleButton graphButton = createButton("graph", "graph-clicked", true);
        graphButton.addActionListener((event) -> {
            try {
                appMain.startGraph();
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
                appMain.startMatrixGraph();
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
                appMain.reInitializeAgent();
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

    public void loadMap(World worldResult) throws IOException {
        world = worldResult;
        worldPanel.setWorld(world);
        worldPanel.fit();
        appMain.updateAgents();
    }

    public static WorldPanel getWorldPanel() {
        return worldPanel;
    }

    public static World getWorld() {
        return world;
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

}