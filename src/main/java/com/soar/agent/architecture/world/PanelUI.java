package com.soar.agent.architecture.world;

import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.*;
import org.jsoar.debugger.util.SwingTools;
import com.soar.agent.architecture.AppMain;
import com.soar.agent.architecture.loader.MapLoader;
import com.soar.agent.architecture.loader.MapLoader.Result;

public class PanelUI extends JPanel {

    private static WorldPanel worldPanel;
    private static World world;
    private AppMain appMain = new AppMain();

    public PanelUI() throws IOException {
        super(new BorderLayout());
        worldPanel = new WorldPanel();
        loadMap(new MapLoader().load(getClass().getResource("/map/map.txt")));
        setSimulationToolbar(worldPanel);
    }

    public static void initUI() {
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                PanelUI content = new PanelUI();
                f.setContentPane(content);
                f.setSize(800, 800);
                f.setVisible(true);
                PanelUI.worldPanel.fit();

            } catch (IOException e) {
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
        final JToolBar bar = new JToolBar("Draggable Toolbar");
        bar.setFloatable(true);
        add(worldPanel, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);
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
        bar.add(runButton);

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
        bar.add(stopButton);

        // Step Button
        JToggleButton stepButton = createButton("step", "step-clicked", false);
        stepButton.addActionListener(new AbstractAction("Step") {
            @Override
            public void actionPerformed(ActionEvent e) {
                appMain.stepAgent();
            }
        });
        bar.add(stepButton);

        // Debugger
        JToggleButton debuggerButton = createButton("debug", "debug-clicked", true);
        debuggerButton.addActionListener((event) -> {
            appMain.openDebugger();
        });
        bar.add(debuggerButton);

        // bar.add(new AbstractAction("Run") {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // appMain.startAgent();
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

    public void loadMap(Result loadResult) throws IOException {
        world = loadResult.world;
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

}