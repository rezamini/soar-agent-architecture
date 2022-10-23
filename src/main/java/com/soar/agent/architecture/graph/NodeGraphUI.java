package com.soar.agent.architecture.graph;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;
import org.jsoar.debugger.util.SwingTools;
import org.jsoar.runtime.ThreadedAgent;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import java.awt.BorderLayout;

public class NodeGraphUI extends JPanel {

    public ThreadedAgent agent;

    public NodeGraphUI(ThreadedAgent agent) {
        this.agent = agent;

        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                NodeGraph content = new NodeGraph(agent);
                f.setContentPane(content);
                f.setSize(1000, 800);
                f.setVisible(true);
                f.setTitle("Soar Working Memory Visualisation");
                f.setLocationRelativeTo(null);

            } catch (IOException e) {
                System.err.println("****** ERROR in graph init *****");
                e.printStackTrace();
            }
        });
    }
}
