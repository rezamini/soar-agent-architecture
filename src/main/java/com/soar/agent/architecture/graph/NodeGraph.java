package com.soar.agent.architecture.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.StreamSupport;

import javax.swing.JPanel;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
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
import java.awt.Color;

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

        // ViewPanel viewPanel = (ViewPanel) view;

        // viewPanel.addMouseWheelListener(new MouseWheelListener() {
        // @Override
        // public void mouseWheelMoved(MouseWheelEvent mwe) {
        // zoomGraphMouseWheelMoved(mwe, viewPanel);
        // }

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

    //remove the main parent ID which is usually I2 from the graph.
    void removeInputParentNode(Wme parentWme) {
        if (graph.getNode(parentWme.getIdentifier().toString()) != null) {
            graph.removeNode(parentWme.getIdentifier().toString());
        }
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

    void addTopNodesAndChildren(Wme parent, List<Wme> childs) {
        // set parent node values; parent node is the one calling this method. for
        // example I2
        // nodeValue : it is the memory value such as L1, I2 or actual value...
        // node id : it is be the actual name such as self, pose, landmark

        Node parentNode = graph.addNode(parent.getAttribute().toString());
        parentNode.setAttribute("nodeValue", parent.getValue().toString());

        addInputParentNode(parent, parentNode);

        // Get a random color for this chunk of memory
        Color randomColor = getRandomNodeColor();

        addChildNodes(parentNode, parent, childs, randomColor);
        // addChildNodes(parentNode, parent, childs);
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

    private void addChildNodes(Node mainNode, Wme parentWme, List<Wme> childs) {
        // Node parentNode = graph.addNode(parent.getIdentifier().toString() +
        // parent.getAttribute().toString());
        // parentNode.setAttribute("nodeValue", parent.getValue().toString());

        // check if parent node is last node
        if (childs.size() == 0) {
            mainNode.setAttribute("isLastNode", true);
        }

        for (Wme current : childs) {
            // for (Iterator<Wme> iter = childs; iter.hasNext();) {
            // Wme current = iter.next();

            // example: landmark-aname, landmarka-distance, viewnorth
            String edgeId = parentWme.getAttribute().toString() + current.getAttribute().toString();

            // example: L2direction-command, L2distance, S10name, V1northeast
            Node childNode = graph.addNode(current.getIdentifier().toString() + current.getAttribute().toString());
            childNode.setAttribute("nodeValue", current.getValue().toString());

            Edge edge = graph.addEdge(edgeId, mainNode, childNode, true);
            edge.setAttribute("edgeValue", current.getAttribute().toString());

            List<Wme> tempList = new ArrayList<Wme>();
            current.getChildren().forEachRemaining(tempList::add);
            if (tempList.size() > 0) {
                addChildNodes(childNode, current, tempList);
            } else {
                childNode.setAttribute("isLastNode", true);
            }
        }
    }

    // method overloading: this method add a childs with a predetermined color
    private void addChildNodes(Node mainNode, Wme parentWme, List<Wme> childs, Color color) {
        // Node parentNode = graph.addNode(parent.getIdentifier().toString() +
        // parent.getAttribute().toString());
        // parentNode.setAttribute("nodeValue", parent.getValue().toString());

        // check if parent node is last node
        if (childs.size() == 0) {
            mainNode.setAttribute("isLastNode", true);
        } else {

            // only set the color if it was not set previously otherwise the color will
            // change on every iteration
            if (mainNode.getAttribute("ui.color") == null) {
                mainNode.setAttribute("ui.color", color);
            }
        }

        for (Wme current : childs) {
            // for (Iterator<Wme> iter = childs; iter.hasNext();) {
            // Wme current = iter.next();

            // example: landmark-aname, landmarka-distance, viewnorth
            String edgeId = parentWme.getAttribute().toString() + current.getAttribute().toString();

            // example: L2direction-command, L2distance, S10name, V1northeast
            Node childNode = graph.addNode(current.getIdentifier().toString() + current.getAttribute().toString());
            childNode.setAttribute("nodeValue", current.getValue().toString());

            Edge edge = graph.addEdge(edgeId, mainNode, childNode, true);
            edge.setAttribute("edgeValue", current.getAttribute().toString());

            List<Wme> tempList = new ArrayList<Wme>();
            current.getChildren().forEachRemaining(tempList::add);
            if (tempList.size() > 0) {

                // set the parent color instead of setting individually. This will avoid having
                // different childs that would be added later on . ex: the former-locale
                if (mainNode.getAttribute("ui.color") != null) {
                    childNode.setAttribute("ui.color", mainNode.getAttribute("ui.color"));
                }

                // addChildNodes(childNode, current, tempList); //call the overloaded method
                // with color argument instead
                addChildNodes(childNode, current, tempList, color);
            } else {
                childNode.setAttribute("isLastNode", true);
            }
        }
    }

    void zoomIn() {
        double newViewPercent = view.getCamera().getViewPercent() - 0.09;
        view.getCamera().setViewPercent(newViewPercent);
    }

    void zoomOut() {
        double newViewPercent = view.getCamera().getViewPercent() + 0.09;
        view.getCamera().setViewPercent(newViewPercent);
    }

    // public static void zoomGraphMouseWheelMoved(MouseWheelEvent mwe, ViewPanel
    // view_panel){
    // if (Event.ALT_MASK != 0) {
    // if (mwe.getWheelRotation() > 0) {
    // double new_view_percent = view_panel.getCamera().getViewPercent() + 0.05;
    // view_panel.getCamera().setViewPercent(new_view_percent);
    // } else if (mwe.getWheelRotation() < 0) {
    // double current_view_percent = view_panel.getCamera().getViewPercent();
    // if(current_view_percent > 0.05){
    // view_panel.getCamera().setViewPercent(current_view_percent - 0.05);
    // }
    // }
    // }
    // }

    public void explore(Node source) {
        Iterator<? extends Node> k = source.getBreadthFirstIterator();

        while (k.hasNext()) {
            Node next = k.next();
            next.setAttribute("ui.class", "marked");
            sleep();
        }
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
