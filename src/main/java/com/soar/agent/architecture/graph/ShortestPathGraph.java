package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.SwingWorker;

import org.checkerframework.checker.units.qual.C;
import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.world.World;

import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import java.awt.Color;

public class ShortestPathGraph extends SwingWorker {
    public Graph graph;
    public SwingViewer viewer;
    public View view;
    private Node agentNode;
    private Map<Landmark, Node> landmarkNodes = new LinkedHashMap<Landmark, Node>();
    private World world;
    private Map<Landmark, List<Node>> computedPaths = new LinkedHashMap<Landmark, List<Node>>();

    public ShortestPathGraph(int[][] mapMatrix, World world) throws IOException {
        this.graph = new SingleGraph("Map Nodes/Matrix");
        this.world = world;

        startGraph();
        addMapNodes(mapMatrix);
        calculateShortPath();
        displayNodeAndEdgeNames();
    }

    public void calculateShortPath() {
        if (landmarkNodes != null && landmarkNodes.size() > 0) {
            AStar astar = new AStar(graph);

            landmarkNodes.forEach((key, value) -> {
                astar.compute(agentNode.getId(), value.getId());
                Path path = astar.getShortestPath();
                computedPaths.put(key, path.getNodePath());
            });

            sortPathByValue(computedPaths);
        }
    }

    private void sortPathByValue(Map<Landmark, List<Node>> computedPaths) {
        List<Entry<Landmark, List<Node>>> sortedList = new LinkedList<Entry<Landmark, List<Node>>>(computedPaths.entrySet());

        // sort by the list size
        Collections.sort(sortedList, new Comparator<Entry<Landmark, List<Node>>>() {
            @Override
            public int compare(Entry<Landmark, List<Node>> o1, Entry<Landmark, List<Node>> o2) {
                    // compare two object and return an integer
                    return Integer.compare(o1.getValue().size(), o2.getValue().size());
            }
        });

        //set the sorted value back to computedPaths
        computedPaths.clear();
        for(Entry<Landmark, List<Node>> entry: sortedList){
            computedPaths.put(entry.getKey(), entry.getValue());
        }
    }

    private void colorPath(List<Node> nodes, Color color) {
        for (Node node : nodes) {

            if (node.getId().equalsIgnoreCase(agentNode.getId()))
                continue; // its agent node, dont color it

            node.setAttribute("ui.color", color);
            sleep();

        }
    }

    public void startGraph() {
        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        // viewer.enableAutoLayout();

        view = viewer.addDefaultView(false, new SwingGraphRenderer());
    }

    public Graph addMapNodes(int[][] mapMatrix) {

        int landmarkCounter = 0;
        for (int i = 0; i < mapMatrix.length; i++) {
            for (int j = 0; j < mapMatrix[0].length; j++) {
                Node node = graph.addNode(i + "-" + j);
                node.setAttribute("y", i);
                node.setAttribute("x", j);

                if (mapMatrix[i][j] == 1) {
                    node.setAttribute("ui.style", "fill-color: #707070; text-size: 12;"); // obstacles
                    node.setAttribute("nodeName", "Block");

                } else if (mapMatrix[i][j] == 2) {
                    node.setAttribute("ui.color", getRandomNodeColor()); // landmarks
                    Landmark landmark = (Landmark) world.getLandmarkMap().keySet().toArray()[landmarkCounter++];
                    node.setAttribute("nodeName", landmark.getName());
                    // landmarkNodes.add(node);
                    landmarkNodes.put(landmark, node);

                } else if (mapMatrix[i][j] == 3) {
                    node.setAttribute("ui.style", "fill-color: yellow; text-size: 12;"); // agent
                    node.setAttribute("nodeName", "Agent");
                    agentNode = node;
                }

                if (i > 0) {
                    String edgeId = i + "-" + j + "<-" + (i - 1) + "-" + j;
                    Edge edge = graph.addEdge(edgeId, (i - 1) + "-" + j, i + "-" + j, false);

                    if (mapMatrix[i - 1][j] == 1 || mapMatrix[i][j] == 1) {
                        // edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    } else {
                        // edge.setAttribute("ui.style", "fill-color: green;");
                        edge.setAttribute("weight", 0.5);
                    }
                }

                if (j > 0) {
                    String edgeId = i + "-" + j + "<-" + i + "-" + (j - 1);
                    Edge edge = graph.addEdge(edgeId, i + "-" + (j - 1), i + "-" + j, false);

                    if (mapMatrix[i][j - 1] == 1 || mapMatrix[i][j] == 1) {
                        // edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    } else {
                        // edge.setAttribute("ui.style", "fill-color: green;");
                        edge.setAttribute("weight", 0.5);
                    }
                }

            }
        }

        return graph;
    }

    /*
     * Update agent node location based on looping, finding and updating the
     * mapMatrix 2d array
     */
    public Graph updateAgentNode(int[][] mapMatrix) {
        // update the previous node to an empty space
        Node previousAgentNode = graph.getNode(agentNode.getId());
        previousAgentNode.setAttribute("ui.style", "fill-color: #DCDCDC;");
        previousAgentNode.setAttribute("nodeName", "");

        for (int i = 0; i < mapMatrix.length; i++) {
            for (int j = 0; j < mapMatrix[0].length; j++) {

                if (mapMatrix[i][j] == 3) {
                    Node node = graph.getNode(i + "-" + j);
                    node.setAttribute("y", i);
                    node.setAttribute("x", j);

                    node.setAttribute("ui.style", "fill-color: yellow; text-size: 12;"); // agent
                    node.setAttribute("nodeName", "Agent");
                    agentNode = node;
                    break;
                }
            }
        }

        displayNodeAndEdgeNames();
        return graph;
    }

    /*
     * Update agent node location based on new agent matrix x,y
     */
    public Graph updateAgentNode(Integer newAgentMapMatrixX, Integer newAgentMapMatrixY) {
        if (newAgentMapMatrixX == null || newAgentMapMatrixY == null)
            return graph;

        // update the previous node to an empty space
        Node previousAgentNode = graph.getNode(agentNode.getId());
        previousAgentNode.setAttribute("ui.style", "fill-color: #DCDCDC;");
        previousAgentNode.setAttribute("nodeName", "");

        // update the new agent node location
        Node newAgentNode = graph.getNode(newAgentMapMatrixY + "-" + newAgentMapMatrixX);
        newAgentNode.setAttribute("ui.style", "fill-color: yellow; text-size: 12;"); // agent
        newAgentNode.setAttribute("nodeName", "Agent");

        // update and keep a copy of the new node
        agentNode = newAgentNode;

        displayNodeAndEdgeNames();
        return graph;
    }

    private void displayNodeAndEdgeNames() {
        graph.nodes().forEach(n -> {

            n.setAttribute("label", n.hasAttribute("nodeName") ? n.getAttribute("nodeName") : "");
        });
        // graph.edges().forEach(e -> e.setAttribute("label", "" + (int)
        // e.getNumber("weight")));
    }

    public Color getRandomNodeColor() {
        Random random = new Random();

        int R = (int) (Math.random() * 256);
        int G = (int) (Math.random() * 256);
        int B = (int) (Math.random() * 256);
        Color color = new Color(R, G, B); // random color, but can be bright or dull

        final float hue = random.nextFloat();
        final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
        final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
        color = Color.getHSBColor(hue, saturation, luminance);

        return color;
    }

    protected void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
    }

    @Override
    protected Object doInBackground() throws Exception {
        // loop throught the computed paths
        if (computedPaths != null && computedPaths.size() > 0) {
            computedPaths.forEach((key, value) -> {

                // check and get the original landmark node color, to be used for path coloring
                if (landmarkNodes.get(key) != null) {
                    colorPath(value, (Color) landmarkNodes.get(key).getAttribute("ui.color"));
                }
            });
        }
        return null;
    }

    protected static String styleSheet = "node {" +
            "fill-color: #DCDCDC;" +
            "size: 40px;" +
            "text-size: 20;	" +
            "fill-mode: dyn-plain; " +
            "text-mode: normal; " +
            "text-alignment: center; " +
            // "text-style: bold;" +
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

}
