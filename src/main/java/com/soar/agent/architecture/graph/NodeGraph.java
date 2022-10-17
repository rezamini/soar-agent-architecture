package com.soar.agent.architecture.graph;

import java.util.Iterator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.jsoar.kernel.io.InputOutput;

public class NodeGraph {
    private static Graph graph;
    private InputOutput io;

    public NodeGraph(InputOutput io){
        System.setProperty("org.graphstream.ui", "swing");
        
        this.graph = new SingleGraph("Memory Graph");
        this.io = io;
    }
    public static void main(String[] args) {

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        Viewer viewer = graph.display();

        graph.addEdge("1", "A", "B", true);
        graph.addEdge("2", "A", "C", true);
        // graph.addEdge("3", "C", "A", true);
        // graph.addEdge("AD", "A", "D", true);
        // graph.addEdge("DE", "D", "E", true);
        // graph.addEdge("DF", "D", "F", true);
        // graph.addEdge("EF", "E", "F", true);

        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }

    }

    public void explore(Node source) {
        Iterator<? extends Node> k = source.getBreadthFirstIterator();

        while (k.hasNext()) {
            Node next = k.next();
            next.setAttribute("ui.class", "marked");
            sleep();
        }
    }

    protected void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
    }

    protected static String styleSheet = 
            "node {" +
            "fill-color: red;" +
            "size: 40px;" +
            "text-size: 20;	" +
            "fill-mode: dyn-plain; " +
            "text-mode: normal; " + 
            "text-alignment: center; " +
            "shadow-mode: gradient-horizontal;" +
            "shadow-color: grey;" +
            "shadow-offset: 0px;" +
            "shadow-width: 5px;" +
            "text-alignment: under;" +
            "text-background-mode: rounded-box;" +
            "text-background-color: gold;" +
            "text-padding: 1px;" +
            "}" +
            "node.marked {" +
            "	fill-color: blue;" +
            "fill-mode: dyn-plain; " + 
            "}" +
            "edge {" +
            // "shape: angle;" +
            "arrow-shape: arrow;" +
            "size: 2; " + 
            "}";
}
