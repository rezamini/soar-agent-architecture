package com.soar.agent.architecture.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;
import org.jsoar.kernel.events.InputEvent;
import org.jsoar.kernel.io.InputOutput;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;

import com.soar.agent.architecture.robot.RobotAgent;

public class NodeGraph {
    private static Graph graph;
    private ThreadedAgent agent;
    private SpriteManager spriteManager;

    public NodeGraph(ThreadedAgent agent) {
        System.setProperty("org.graphstream.ui", "swing");

        this.graph = new MultiGraph("Memory Graph");
        this.agent = agent;
        this.spriteManager = new SpriteManager(graph);

        // Sprite s = spriteManager.addSprite("test");
        // s.attachToEdge(edgeId);
        // s.setPosition(0.5);
        // s.detach();
    }

    public void startGraph() {
        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        Viewer viewer = graph.display();
        viewer.enableAutoLayout();

        initMemoryInputListener();

        // graph.addEdge("1", "A", "B", true);
        // graph.addEdge("2", "A", "C", true);
        
        // graph.addEdge("3", "C", "A", true);
        // graph.addEdge("AD", "A", "D", true);
        // graph.addEdge("DE", "D", "E", true);
        // graph.addEdge("DF", "D", "F", true);
        // graph.addEdge("EF", "E", "F", true);


    }

    private void initMemoryInputListener() {


        agent.getEvents().addListener(InputEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {      
                List<Wme> wmeList = new ArrayList<Wme>();
                agent.getInputOutput().getInputLink().getWmes().forEachRemaining(wmeList::add);

                
                if(wmeList != null && wmeList.size() > 0){
                    
                    for(int i=0; i<wmeList.size(); i++){
                        Wme current = wmeList.get(i);
                        
                        if(current.getChildren() != null){
                            addChildrenNodes(current, current.getChildren());
                        }
                    }

                    for (Node node : graph) {
                        
                        if(node.hasAttribute("nodeValue")){
                            node.setAttribute("ui.label", node.getAttribute("nodeValue"));
                            // node.setAttribute("ui.label", node.getId());

                        }

                        // node.setAttribute("xy", 1);
                    }

                    // graph.edges().forEach(edge -> {
                    //     edge.setAttribute("ui.label", "test");
                    // });

                    // explore(graph.getNode("landmarks"));
                }
            }

        });
    }

    public static void main(String[] args) {

    }

    private void addChildrenNodes(Wme parent, Iterator<Wme> childs){
        
        //set parent node values; parent node is the one calling this method. for example I2
        //nodeValue : it is the memory value such as L1, I2 or actual value...
        //node id : it is be the actual name such as self, pose, landmark

        Node parentNode = graph.addNode(parent.getAttribute().toString());
        parentNode.setAttribute("nodeValue", parent.getValue().toString());

        for(Iterator<Wme> iter = childs; iter.hasNext();){
            Wme current = iter.next();
            String edgeId = parent.getAttribute().toString() + current.getAttribute().toString();

            Node childNode = graph.addNode(current.getAttribute().toString());
            childNode.setAttribute("nodeValue", current.getValue().toString());

            graph.addEdge(edgeId, parentNode, childNode, true);
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

    protected static String styleSheet = "node {" +
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
            // "text-alignment: under;" +
            // "text-background-mode: rounded-box;" +
            // "text-background-color: gold;" +
            // "text-padding: 1px;" +
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
