package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingWorker;

import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import java.awt.Color;

public class ShortestPathGraph extends SwingWorker {
    public Graph graph;
    public SwingViewer viewer;
    public View view;
    private Node agentNode;
    private List<Node> landmarkNodes = new ArrayList<Node>();
    
    public ShortestPathGraph(int[][] mapMatrix) throws IOException {
        this.graph = new SingleGraph("Map Nodes/Matrix");
        startGraph();
        addMapNodes(mapMatrix);
    }

    public void calculateShortPath() {
        if(landmarkNodes != null && landmarkNodes.size() > 0){
            AStar astar = new AStar(graph);

            landmarkNodes.forEach(landmark -> {
                astar.compute(agentNode.getId(), landmark.getId());
                Path path = astar.getShortestPath();
                colorPath(path.getNodePath(), (Color) landmark.getAttribute("ui.color"));
            });
        }
    }

    private void colorPath(List<Node> nodes, Color color){
        for(Node node: nodes){

            if(node.getId().equalsIgnoreCase(agentNode.getId())) continue; //its agent node, dont color it

            node.setAttribute("ui.color", color);
            sleep();
            
        }
    }

    public void startGraph(){
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
        for (int i = 0; i < mapMatrix.length; i++) {
            for (int j = 0; j < mapMatrix[0].length; j++) {
                Node node = graph.addNode(i + "-" + j);
                node.setAttribute("y", i);
                node.setAttribute("x", j);

                if (mapMatrix[i][j] == 1) {
                    node.setAttribute("ui.style", "fill-color: #707070;"); // obstacles

                } else if (mapMatrix[i][j] == 2) {
                    node.setAttribute("ui.color", getRandomNodeColor()); //landmarks
                    landmarkNodes.add(node);

                }else if (mapMatrix[i][j] == 3) {
                    node.setAttribute("ui.style", "fill-color: yellow;"); //agent
                    agentNode = node;
                }

                if (i > 0) {
                    String edgeId = i + "-" + j + "<-" + (i - 1) + "-" + j;
                    Edge edge = graph.addEdge(edgeId, (i - 1) + "-" + j, i + "-" + j, false);

                    if (mapMatrix[i-1][j] == 1 || mapMatrix[i][j] == 1) {
                        // edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    }else{
                        // edge.setAttribute("ui.style", "fill-color: green;"); 
                        edge.setAttribute("weight", 0.5); 
                    }
                }

                if (j > 0) {
                    String edgeId = i + "-" + j + "<-" + i + "-" + (j - 1);
                    Edge edge = graph.addEdge(edgeId, i + "-" + (j - 1), i + "-" + j, false);

                    if (mapMatrix[i][j-1] == 1 || mapMatrix[i][j] == 1) {
                        // edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    }else{
                        // edge.setAttribute("ui.style", "fill-color: green;"); 
                        edge.setAttribute("weight", 0.5); 
                    }
                }

                // graph.nodes().forEach(n -> n.setAttribute("label", n.getId()));
                // graph.edges().forEach(e -> e.setAttribute("label", "" + (int) e.getNumber("weight")));

            }
        }

        return graph;
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
        try { Thread.sleep(1000); } catch (Exception e) {}
    }

    @Override
    protected Object doInBackground() throws Exception {
        calculateShortPath();
        return null;
    }


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



}
