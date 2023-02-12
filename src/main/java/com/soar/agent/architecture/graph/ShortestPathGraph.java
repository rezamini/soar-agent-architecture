package com.soar.agent.architecture.graph;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.graph.Node;

public class ShortestPathGraph {
    public Graph graph;
    public SwingViewer viewer;
    public View view;
    
    public ShortestPathGraph(int[][] mapMatrix) throws IOException {
        this.graph = new SingleGraph("Map Nodes/Matrix");
        startGraph();
        addMapNodes(mapMatrix);
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
