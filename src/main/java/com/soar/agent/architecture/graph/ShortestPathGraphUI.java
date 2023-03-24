package com.soar.agent.architecture.graph;

import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.jsoar.debugger.util.SwingTools;
import org.jsoar.kernel.events.InputEvent;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;

import com.soar.agent.architecture.enums.GraphEnum;
import com.soar.agent.architecture.world.World;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.awt.Color;

public class ShortestPathGraphUI extends JPanel {
    private static ShortestPathGraphUI nodeGraphInstance;
    private final JFrame mainFrame;
    private ThreadedAgent agent;
    private JPanel startControlPanel;
    public Graph graph = new SingleGraph("Map Nodes/Matrix");
    public SwingViewer viewer;
    public View view;
    public ShortestPathGraph shortestPathGraph;
    public int[][] mapMatrix;
    public World world;

    public ShortestPathGraphUI(ThreadedAgent agent, int[][] mapMatrix, World world) throws IOException {
        super(new BorderLayout());
        this.agent = agent;
        this.mapMatrix = mapMatrix;
        this.world = world;

        mainFrame = new JFrame();
        shortestPathGraph = new ShortestPathGraph(mapMatrix, world);

        initialise();
    }

    public static ShortestPathGraphUI getInstance(ThreadedAgent agent, int[][] mapMatrix, World world)
            throws IOException {
        if (nodeGraphInstance == null) {
            nodeGraphInstance = new ShortestPathGraphUI(agent, mapMatrix, world);

        } else {
            nodeGraphInstance.agent = agent;
            nodeGraphInstance.world = world;
            nodeGraphInstance.shortestPathGraph = new ShortestPathGraph(mapMatrix, world);
            nodeGraphInstance.initialise();

            nodeGraphInstance.mainFrame.setVisible(true);
        }

        return nodeGraphInstance;
    }

    private void initialise() {
        removeAll();
        revalidate();
        repaint();

        initMemoryInputListener();
        initGraphUI();
        initButtonControlUI();
    }

    private void initGraphUI() {
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            // if(mainFrame == null) mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            try {
                // add this class as the content pane instead
                mainFrame.setContentPane(nodeGraphInstance);
                mainFrame.setSize(1100, 900);
                mainFrame.setVisible(true);
                mainFrame.setTitle(GraphEnum.FRAME_TITLE_MATRIX.getName());
                mainFrame.setLocationRelativeTo(null);

                // call(the nodegrapg is final variable now which is initialised in the
                // constrcutre) and add the viewer to UI from this class.

                add((DefaultView) shortestPathGraph.view, BorderLayout.CENTER);

            } catch (Exception e) {
                System.err.println("****** ERROR in matrix graph init *****");
                e.printStackTrace();
            }
        });
    }

    private void initMemoryInputListener() {
        agent.getEvents().addListener(InputEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {

                try {
                    nodeGraphInstance.shortestPathGraph.updateAgentNode(world.getAgentMapMatrixX(),
                            world.getAgentMapMatrixY(), world.getSecondAgentMapMatrixX(),
                            world.getSecondAgentMapMatrixY());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void initButtonControlUI() {
        JPanel containerPanel = new JPanel();
        containerPanel.setOpaque(true);
        containerPanel.setBackground(Color.WHITE);

        startControlPanel = new JPanel(new BorderLayout());
        startControlPanel.setOpaque(true);
        startControlPanel.setBackground(Color.WHITE);

        JButton startPathCalculation = new JButton("Find Shortest Path(s)");
        JButton resetGraph = new JButton("Reset Graph");

        startPathCalculation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shortestPathGraph.execute();
            }
        });

        resetGraph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    shortestPathGraph = new ShortestPathGraph(mapMatrix, world);
                    initialise();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

        containerPanel.add(startPathCalculation);
        containerPanel.add(resetGraph);

        startControlPanel.add(containerPanel, BorderLayout.LINE_END);

        add(startControlPanel, BorderLayout.PAGE_END);
    }

    public void setFrameVisibility(boolean showFrame) {
        nodeGraphInstance.mainFrame.setVisible(showFrame);
    }

}
