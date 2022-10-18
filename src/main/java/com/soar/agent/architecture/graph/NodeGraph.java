package com.soar.agent.architecture.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
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

    public NodeGraph(ThreadedAgent agent) {
        System.setProperty("org.graphstream.ui", "swing");

        this.graph = new SingleGraph("Memory Graph");
        this.agent = agent;
    }

    public void startGraph() {
        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        Viewer viewer = graph.display();

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
                        node.setAttribute("ui.label", node.getId());
                    }
                }
            }

        });
    }

    public static void main(String[] args) {

    }

    private void addChildrenNodes(Wme Parent, Iterator<Wme> childs){

        int id = 0;
        for(Iterator<Wme> iter = childs; iter.hasNext();){
            Wme current = iter.next();

            graph.addEdge(current.getAttribute().toString()+current.getTimetag(), Parent.getAttribute().toString(), current.getAttribute().toString(), true);
            
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
