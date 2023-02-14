package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class ShortestPathGraph {
    public Graph graph;
    public SwingViewer viewer;
    public View view;
    private Node agentNode;
    private List<Node> landmarkNodes = new ArrayList<Node>();
    
    public ShortestPathGraph(int[][] mapMatrix) throws IOException {
        this.graph = new SingleGraph("Map Nodes/Matrix");
        startGraph();
        addMapNodes(mapMatrix);
        calculateShortPath();
    }

    private void calculateShortPath() {
        if(landmarkNodes != null && landmarkNodes.size() > 0){
            AStar astar = new AStar(graph);

            landmarkNodes.forEach(landmark -> {
                astar.compute(agentNode.getId(), landmark.getId());
                Path path = astar.getShortestPath();
                colorPath(path.getNodePath());
            });
        }
    }

    private void colorPath(List<Node> nodes){
        Color color = getRandomNodeColor();
        for(Node node: nodes){

            if(landmarkNodes.contains(node) || node.getId().equalsIgnoreCase(agentNode.getId())) continue; //its the landmark or agent node, dont color it
            // System.out.println(getRandomNodeColor());
            // node.setAttribute("ui.style", "fill-color: "+ "rgb(255, 255, 255)" + ";");
            node.setAttribute("ui.color", color);
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
                    node.setAttribute("ui.style", "fill-color: #CCCC00;"); //landmarks
                    landmarkNodes.add(node);

                }else if (mapMatrix[i][j] == 3) {
                    node.setAttribute("ui.style", "fill-color: yellow;"); //agent
                    agentNode = node;
                }

                if (i > 0) {
                    String edgeId = i + "-" + j + "<-" + (i - 1) + "-" + j;
                    Edge edge = graph.addEdge(edgeId, (i - 1) + "-" + j, i + "-" + j, false);

                    if (mapMatrix[i-1][j] == 1 || mapMatrix[i][j] == 1) {
                        edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    }else{
                        edge.setAttribute("ui.style", "fill-color: green;"); 
                        edge.setAttribute("weight", 0.5); 
                    }
                }

                if (j > 0) {
                    String edgeId = i + "-" + j + "<-" + i + "-" + (j - 1);
                    Edge edge = graph.addEdge(edgeId, i + "-" + (j - 1), i + "-" + j, false);

                    if (mapMatrix[i][j-1] == 1 || mapMatrix[i][j] == 1) {
                        edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    }else{
                        edge.setAttribute("ui.style", "fill-color: green;"); 
                        edge.setAttribute("weight", 0.5); 
                    }
                }

                // if (i > 0) {
                //     Edge edge = graph.addEdge(i + "-" + j + "-" + (i - 1) + "-" + j, i + "-" + j, (i - 1) + "-" + j, true);


                //     if (mapMatrix[i][j] == 1) {
                //         edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                //     }
                //     // edge.setAttribute("ui.style", "fill-color: green;");
                //     // edge.setAttribute("weight", 2);
                // }

                // if (j > 0) {
                //     Edge edge = graph.addEdge(i + "-" + j + "-" + i + "-" + (j - 1), i + "-" + j, i + "-" + (j - 1), true);

                //     if (mapMatrix[i][j] == 1) {
                //         edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                //     }

                //     // edge.setAttribute("ui.style", "fill-color: blue;");
                //     // edge.setAttribute("weight", 3);
                // }

                // if (i < mapMatrix.length - 1) {
                //     Edge edge = graph.addEdge(i + "-" + j + "-" + (i + 1) + "-" + j, i + "-" + j, (i + 1) + "-" + j, true);
                //     if (mapMatrix[i][j] == 1) {
                //         edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                //     }

                //     // edge.setAttribute("ui.style", "fill-color: yellow;");
                //     // edge.setAttribute("weight", 4);
                // }

                // if (j < mapMatrix[0].length - 1) {
                //     Edge edge = graph.addEdge(i + "-" + j + "-" + i + "-" + (j + 1), i + "-" + j, i + "-" + (j + 1), true);
                //     if (mapMatrix[i][j] == 1) {
                //         edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                //     }

                //     // edge.setAttribute("ui.style", "fill-color: pink;");
                //     // edge.setAttribute("weight", 5);
                // }

                // if (i > 0) {
                //     Edge edgeI = graph.addEdge(i + "-" + j + "-" + (i - 1) + "-" + j, i + "-" + j, (i - 1) + "-" + j, false);
                //     if (mapMatrix[i][j] == 1) {
                //         graph.getad
                //         edgeI.setAttribute("ui.style", "fill-color: red;"); // obstacles
                //         edgeI.setAttribute("weight", 100);
                //     } 
                    
                //     // else if (mapMatrix[i][j] == 2) {
                //     //     graph.addEdge(i + "-" + j + "-" + (i - 1) + "-" + j, i + "-" + j, (i - 1) + "-" + j, false); //landmarks
                //     // }else if (mapMatrix[i][j] == 3) {
                //     //     graph.addEdge(i + "-" + j + "-" + (i - 1) + "-" + j, i + "-" + j, (i - 1) + "-" + j, false); //agent
                //     // }

                    
                // }

                // if (j > 0) {
                //     Edge edgeJ = graph.addEdge(i + "-" + j + "-" + i + "-" + (j - 1), i + "-" + j, i + "-" + (j - 1), false);

                //     if (mapMatrix[i][j] == 1) {
                //         edgeJ.setAttribute("ui.style", "fill-color: red;"); // obstacles
                //         edgeJ.setAttribute("weight", 100);
                //     } 
                // }
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

    public Color getRandomNodeColor() {
        Random random = new Random();

        // float hue = random.nextFloat();
        // // Saturation between 0.1 and 0.3
        // float saturation = (random.nextInt(2000) + 1000) / 10000f;
        // float luminance = 0.9f;
        // Color color = Color.getHSBColor(hue, saturation, luminance);

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
