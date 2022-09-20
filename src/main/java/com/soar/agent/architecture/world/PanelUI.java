package com.soar.agent.architecture.world;


import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import org.jsoar.debugger.util.SwingTools;
import com.soar.agent.architecture.AppMain;
import com.soar.agent.architecture.loader.MapLoader;
import com.soar.agent.architecture.loader.MapLoader.Result;

public class PanelUI extends JPanel{

    private static WorldPanel worldPanel;
    private static World world;
    private AppMain appMain = new AppMain();

    public PanelUI() throws IOException{
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
                f.setSize(640, 640);
                f.setVisible(true);
                PanelUI.worldPanel.fit();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setSimulationToolbar(WorldPanel worldPanel) {
        final JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        add(worldPanel, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);

        bar.add(new AbstractAction("Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                appMain.startAgent();
            }
        });

        bar.add(new AbstractAction("Stop") {

            @Override
            public void actionPerformed(ActionEvent e) {
                appMain.stopAgent();

            }

        });

        bar.add(new AbstractAction("Step") {
            @Override
            public void actionPerformed(ActionEvent e) {
                appMain.stepAgent();
            }
        });

        JCheckBox debuggerCheckBox = new JCheckBox("Open Debugger");
        debuggerCheckBox.addActionListener((event) -> {
            if (debuggerCheckBox.isSelected()) {
                appMain.openDebugger();
            } else {
                appMain.closeDebugger();
            }
        });
        bar.add(debuggerCheckBox);

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