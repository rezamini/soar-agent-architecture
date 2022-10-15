package com.soar.agent.architecture.graph;

import java.util.Iterator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

public class NodeGraph {
    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("Tutorial 1");
        
        // graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        Viewer viewer = graph.display();

        graph.addEdge("AB", "A", "B", true);
        graph.addEdge("BC", "B", "C", true);
        graph.addEdge("CA", "C", "A", true);
        graph.addEdge("AD", "A", "D", true);
        graph.addEdge("DE", "D", "E", true);
        graph.addEdge("DF", "D", "F", true);
        graph.addEdge("EF", "E", "F", true);

        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }

    }


}
