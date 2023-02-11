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

public class ShortestPathGraph extends JPanel {
    private static ShortestPathGraph nodeGraphInstance;
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
    protected static String styleSheet = "node {" +
            "fill-color: #DCDCDC;" +
            "size: 40px;" +
            "text-size: 20;	" +
            "fill-mode: dyn-plain; " +
            "text-mode: normal; " +
            "text-alignment: center; " +
            // "shadow-mode: gradient-horizontal;" +
            // "shadow-color: grey;" +
            // "shadow-offset: 0px;" +
            // "shadow-width: 5px;" +
            "}" +
            "node.value {" +
            "stroke-mode: dots;" +
            "padding: 3px;" +
            "fill-mode: dyn-plain; " +
            "shape: box;" +
            "size-mode: fit;" +
            "fill-color: white;" +
            "text-size: 20;" +
            "text-style: bold;" +
            // "text-alignment: under;" +
            // "text-background-mode: rounded-box;" +
            // "text-background-color: gold;" +
            // "text-padding: 1px;" +

            "}" +
            "node.main {" +
            "fill-color: cyan, red;" +
            "fill-mode: dyn-plain; " +
            "size: 35px;" +
            "}" +
            "node.marked {" +
            "fill-color: blue;" +
            "fill-mode: dyn-plain; " +
            "}" +
            "edge {" +
            "arrow-shape: arrow;" +
            "size: 2; " +
            "text-size: 11;	" +
            "}" + 
            "graph { fill-color: #EFEFEF; canvas-color: blue; }";

    static String my_graph = "DGS004\n"
            + "my 0 0\n"
            + "an A xy: 0,1\n"
            + "an B xy: 1,2\n"
            + "an C xy: 2,2\n"
            + "an D xy: 1,0\n"
            + "an E xy: 2,0\n"
            + "an F xy: 3,1\n"
            + "ae AB A B weight:1 \n"
            + "ae AD A D weight:1 \n"
            + "ae BC B C weight:1 \n"
            + "ae CF C F weight:2 \n"
            + "ae DE D E weight:1 \n"
            + "ae EF E F weight:1 \n";

    public ShortestPathGraph(ThreadedAgent agent, int[][] mapMatrix) throws IOException {
        super(new BorderLayout());
        this.agent = agent;

        mainFrame = new JFrame();
        nodesToolbar = new JToolBar(GraphEnum.TOOLBAR_TITLE.getName());

        // graph = exampleGraph();
        graph = addMapNodes(mapMatrix);

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        // viewer.enableAutoLayout();

        view = viewer.addDefaultView(false, new SwingGraphRenderer());

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

    /* add map node from map matrix */
    public Graph addMapNodes(int[][] mapMatrix) {
        for (int i = 0; i < mapMatrix.length; i++) {
            for (int j = 0; j < mapMatrix[0].length; j++) {
                Node node = graph.addNode(i + "-" + j);
                node.setAttribute("y", i);
                node.setAttribute("x", j);

                if (mapMatrix[i][j] == 1) {
                    node.setAttribute("ui.style", "fill-color: #707070;"); // obstacles
                } else if (mapMatrix[i][j] == 2) {
                    node.setAttribute("ui.style", "fill-color: #CCCC00;"); //landmarks
                }else if (mapMatrix[i][j] == 3) {
                    node.setAttribute("ui.style", "fill-color: yellow;"); //agent
                }

                if (i > 0) {
                    graph.addEdge(i + "-" + j + "-" + (i - 1) + "-" + j, i + "-" + j, (i - 1) + "-" + j, false);
                }

                if (j > 0) {
                    graph.addEdge(i + "-" + j + "-" + i + "-" + (j - 1), i + "-" + j, i + "-" + (j - 1), false);
                }
            }
        }

        return graph;
    }

    public Graph exampleGraph() {
        graph.addNode("A").setAttribute("xy", 0, 1);
        graph.addNode("B").setAttribute("xy", 1, 2);
        graph.addNode("C").setAttribute("xy", 1, 1);
        graph.addNode("D").setAttribute("xy", 1, 0);
        graph.addNode("E").setAttribute("xy", 2, 2);
        graph.addNode("F").setAttribute("xy", 2, 1);
        // graph.addEdge("AB", "A", "B").setAttribute("weight", 100);
        // graph.addEdge("AC", "A", "C").setAttribute("weight", 0.5);
        // graph.addEdge("AD", "A", "D").setAttribute("weight", 0.5);
        // graph.addEdge("BC", "B", "C").setAttribute("weight", 0.5);
        // graph.addEdge("CD", "C", "D").setAttribute("weight", 0.5);
        // graph.addEdge("BE", "B", "E").setAttribute("weight", 100);
        // graph.addEdge("CF", "C", "F").setAttribute("weight", 0.5);
        // graph.addEdge("DF", "D", "F").setAttribute("weight", 0.5);
        // graph.addEdge("EF", "E", "F").setAttribute("weight", 0.5);

        // Node obstacle = g.addNode("O");
        // g.addEdge("AO", "A", "O", true).setAttribute("weight", 100.0);

        graph.nodes().forEach(n -> n.setAttribute("label", n.getId()));
        graph.edges().forEach(e -> e.setAttribute("label", "" + (int) e.getNumber("weight")));

        return graph;
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
                mainFrame.setContentPane(this);
                mainFrame.setSize(1100, 900);
                mainFrame.setVisible(true);
                mainFrame.setTitle(GraphEnum.FRAME_TITLE.getName());
                mainFrame.setLocationRelativeTo(null);

                // call(the nodegrapg is final variable now which is initialised in the
                // constrcutre) and add the viewer to UI from this class.

                add((DefaultView) view, BorderLayout.CENTER);

            } catch (Exception e) {
                System.err.println("****** ERROR in graph init *****");
                e.printStackTrace();
            }
        });
    }

}
