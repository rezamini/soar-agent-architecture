package com.soar.agent.architecture.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.Color;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.jsoar.kernel.events.InputEvent;
import org.jsoar.kernel.io.InputOutput;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;

import com.google.common.collect.Iterators;
import com.soar.agent.architecture.robot.RobotAgent;
import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

public class NodeGraph {
    public Graph graph;
    private ThreadedAgent agent;
    private SpriteManager spriteManager;
    public SwingViewer viewer;
    public View view;

    public NodeGraph(ThreadedAgent agent) throws IOException {
        // System.setProperty("org.graphstream.ui", "swing");

        this.graph = new MultiGraph("Memory Graph");
        this.agent = agent;
        this.spriteManager = new SpriteManager(graph);
        startGraph();

        // Sprite s = spriteManager.addSprite("test");
        // s.attachToEdge(edgeId);
        // s.setPosition(0.5);
        // s.detach();
    }

    public void startGraph() {
        graph.setAttribute("ui.title", "Soar Working Memory Visualisation");
        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        // Viewer viewer = graph.display();
        viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();

        view = viewer.addDefaultView(false, new SwingGraphRenderer());

        //ViewPanel viewPanel = (ViewPanel) view;

        // viewPanel.addMouseWheelListener(new MouseWheelListener() {
        //     @Override
        //     public void mouseWheelMoved(MouseWheelEvent mwe) {
        //         zoomGraphMouseWheelMoved(mwe, viewPanel);
        //     }

        // });

        // View view = viewer.getDefaultView();
        // view.getCamera().setViewPercent(0.5);
        // ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
        // viewPanel.getCamera().setViewPercent(0.5);
        // viewPanel.resizeFrame(1000, 800);

        // add((DefaultView) viewer.addDefaultView(false, new SwingGraphRenderer()),
        // BorderLayout.CENTER);

    }

    /**
     * Set the parent of all the nodes if required. Which is I2.
     * This create a main input node and connect all the other nodes(childs) as part
     * of this node.
     * 
     * @param parentWme
     * @param parentNode
     */
    private void addInputParentNode(Wme parentWme, Node parentNode) {
        // The main input id will be like, I2area, I2landmarks etc to make it unique
        String inputMainId = parentWme.getIdentifier().toString() + parentWme.getAttribute().toString();

        Node inputMainNode = graph.addNode(parentWme.getIdentifier().toString());
        inputMainNode.setAttribute("nodeValue", parentWme.getIdentifier().toString());

        // set the css property for the main node (I2)
        inputMainNode.setAttribute("ui.class", "main");

        // this edge connects all the nodes to the main memory node which is I2
        // this becomes the first parent of all the other nodes.
        Edge edge = graph.addEdge(inputMainId, inputMainNode, parentNode, true);
        edge.setAttribute("edgeValue", parentWme.getAttribute().toString());
    }

    void setGraphNodeAndEdgeNames() {
        for (Node node : graph) {

            if (node.hasAttribute("nodeValue")) {
                node.setAttribute("ui.label", node.getAttribute("nodeValue"));

                if (node.hasAttribute("isLastNode")) {
                    node.setAttribute("ui.class", "value");
                }

            }
        }

        graph.edges().forEach(edge -> {

            if (edge.hasAttribute("edgeValue")) {
                edge.setAttribute("ui.label", "^" + edge.getAttribute("edgeValue"));
            }
        });
    }

    void addTopNodesAndChildren(Wme parent, Iterator<Wme> childs) {
        // set parent node values; parent node is the one calling this method. for
        // example I2
        // nodeValue : it is the memory value such as L1, I2 or actual value...
        // node id : it is be the actual name such as self, pose, landmark

        Node parentNode = graph.addNode(parent.getAttribute().toString());
        parentNode.setAttribute("nodeValue", parent.getValue().toString());

        addInputParentNode(parent, parentNode);
        addChildNodes(parentNode, parent, childs);
    }

    void removeTopNodesAndChildren(Wme parent, Iterator<Wme> childs) {

        // remove parent node
        Node parentNode = graph.getNode(parent.getAttribute().toString());
        if (graph.getNode(parent.getAttribute().toString()) != null) {
            graph.removeNode(parent.getAttribute().toString());
        }

        removeChildNodes(parentNode, parent, childs);
    }

    private void removeChildNodes(Node mainNode, Wme parentWme, Iterator<Wme> childs) {
        for (Iterator<Wme> iter = childs; iter.hasNext();) {
            Wme current = iter.next();

            // example: L2direction-command, L2distance, S10name, V1northeast
            Node childNode = graph.addNode(current.getIdentifier().toString() + current.getAttribute().toString());

            if (childNode != null) {
                graph.removeNode(childNode);
            }

            if (Iterators.size(current.getChildren()) > 0) {
                removeChildNodes(childNode, current, current.getChildren());
            }
        }
    }

    private void addChildNodes(Node mainNode, Wme parentWme, Iterator<Wme> childs) {
        // Node parentNode = graph.addNode(parent.getIdentifier().toString() +
        // parent.getAttribute().toString());
        // parentNode.setAttribute("nodeValue", parent.getValue().toString());

        for (Iterator<Wme> iter = childs; iter.hasNext();) {
            Wme current = iter.next();

            // example: landmark-aname, landmarka-distance, viewnorth
            String edgeId = parentWme.getAttribute().toString() + current.getAttribute().toString();

            // example: L2direction-command, L2distance, S10name, V1northeast
            Node childNode = graph.addNode(current.getIdentifier().toString() + current.getAttribute().toString());
            childNode.setAttribute("nodeValue", current.getValue().toString());

            Edge edge = graph.addEdge(edgeId, mainNode, childNode, true);
            edge.setAttribute("edgeValue", current.getAttribute().toString());

            if (Iterators.size(current.getChildren()) > 0) {
                addChildNodes(childNode, current, current.getChildren());
            } else {
                childNode.setAttribute("isLastNode", true);
            }
        }
    }

    void zoomIn(){
        double newViewPercent = view.getCamera().getViewPercent() - 0.09;
        view.getCamera().setViewPercent(newViewPercent);
    }

    void zoomOut(){
        double newViewPercent = view.getCamera().getViewPercent() + 0.09;
        view.getCamera().setViewPercent(newViewPercent);
    }


    // public static void zoomGraphMouseWheelMoved(MouseWheelEvent mwe, ViewPanel view_panel){
    //     if (Event.ALT_MASK != 0) {            
    //         if (mwe.getWheelRotation() > 0) {
    //             double new_view_percent = view_panel.getCamera().getViewPercent() + 0.05;
    //             view_panel.getCamera().setViewPercent(new_view_percent);               
    //         } else if (mwe.getWheelRotation() < 0) {
    //             double current_view_percent = view_panel.getCamera().getViewPercent();
    //             if(current_view_percent > 0.05){
    //                 view_panel.getCamera().setViewPercent(current_view_percent - 0.05);                
    //             }
    //         }
    //     }                     
    // }

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

    protected static String styleSheet = "node {" +
            "fill-color: green, blue;" +
            "size: 25px;" +
            "text-size: 10;	" +
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
            "	fill-color: blue;" +
            "fill-mode: dyn-plain; " +
            "}" +
            "edge {" +
            "arrow-shape: arrow;" +
            "size: 2; " +
            "text-size: 10;	" +
            "}";
}
