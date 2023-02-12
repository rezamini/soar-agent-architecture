package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.graphstream.algorithm.AStar;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.jsoar.debugger.util.SwingTools;
import org.jsoar.kernel.events.InputEvent;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;
import org.graphstream.algorithm.AStar.DistanceCosts;

import com.soar.agent.architecture.enums.GraphEnum;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;

public class ShortestPathGraphUI extends JPanel {
    private static ShortestPathGraphUI nodeGraphInstance;
    private final JFrame mainFrame;
    private ThreadedAgent agent;
    private final JToolBar nodesToolbar;
    private JPanel zoomControlPanel;
    private Map<String, JCheckBox> checboxMap = new HashMap<String, JCheckBox>();
    private List<Wme> inputList = new ArrayList<Wme>();
    private Set<String> uncheckNodeNames = new HashSet<String>();
    public Graph graph = new SingleGraph("Map Nodes/Matrix");
    public SwingViewer viewer;
    public View view;
    public ShortestPathGraph shortestPathGraph;

    public ShortestPathGraphUI(ThreadedAgent agent, int[][] mapMatrix) throws IOException {
        super(new BorderLayout());
        this.agent = agent;

        mainFrame = new JFrame();
        shortestPathGraph = new ShortestPathGraph(mapMatrix);
        nodesToolbar = new JToolBar(GraphEnum.TOOLBAR_TITLE.getName());

        // graph = exampleGraph();
        // graph = addMapNodes(mapMatrix);

        // graph.setAttribute("ui.stylesheet", styleSheet);
        // graph.setStrict(false);
        // graph.setAutoCreate(true);
        // graph.setAttribute("ui.quality");
        // graph.setAttribute("ui.antialias");

        // viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        // viewer.enableAutoLayout();

        // view = viewer.addDefaultView(false, new SwingGraphRenderer());

        // AStar astar = new AStar(graph);
        // // astar.setCosts(new DistanceCosts());
        // astar.compute("A", "E");

        // System.out.println(astar.getShortestPath());

        // // Edge lengths are stored in an attribute called "length"
        // // The length of a path is the sum of the lengths of its edges
        // Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
        // // Compute the shortest paths in g from A to all nodes
        // dijkstra.init(graph);
        // dijkstra.setSource(graph.getNode("A"));
        // dijkstra.compute();

        // // Print the shortest path from A to B
        // System.out.println(dijkstra.getPath(graph.getNode("E")));

        // // Print the lengths of all the shortest paths
        // for (Node node : graph)
        // System.out.printf("%s->%s:%10.2f%n", dijkstra.getSource(), node,
        // dijkstra.getPathLength(node));

        // // Color in blue all the nodes on the shortest path form A to B
        // for (Node node : dijkstra.getPathNodes(graph.getNode("B")))
        // node.setAttribute("ui.style", "fill-color: blue;");

        // // Color in red all the edges in the shortest path tree
        // for (Edge edge : dijkstra.getTreeEdges())
        // edge.setAttribute("ui.style", "fill-color: red;");

        initialise();
    }

    public static ShortestPathGraphUI getInstance(ThreadedAgent agent, int[][] mapMatrix) throws IOException {
        if (nodeGraphInstance == null) {
            nodeGraphInstance = new ShortestPathGraphUI(agent, mapMatrix);

        } else {
            nodeGraphInstance.agent = agent;
            nodeGraphInstance.shortestPathGraph = new ShortestPathGraph(mapMatrix);
            nodeGraphInstance.initialise();

            nodeGraphInstance.mainFrame.setVisible(true);
        }

        return nodeGraphInstance;
    }

    private void initialise() {
        removeAll();
        revalidate();
        repaint();

        initGraphUI();
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
                mainFrame.setTitle(GraphEnum.FRAME_TITLE.getName());
                mainFrame.setLocationRelativeTo(null);

                // call(the nodegrapg is final variable now which is initialised in the
                // constrcutre) and add the viewer to UI from this class.

                add((DefaultView) shortestPathGraph.view, BorderLayout.CENTER);

            } catch (Exception e) {
                System.err.println("****** ERROR in graph init *****");
                e.printStackTrace();
            }
        });
    }

    public void setFrameVisibility(boolean showFrame) {
        nodeGraphInstance.mainFrame.setVisible(showFrame);
    }

}
