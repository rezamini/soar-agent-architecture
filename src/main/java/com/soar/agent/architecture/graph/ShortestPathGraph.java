package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.swing.SwingWorker;

import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.world.World;

import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import java.awt.Color;

public class ShortestPathGraph extends SwingWorker {
    public Graph graph;
    public SwingViewer viewer;
    public View view;
    private Node agentNode;
    private Node secondAgentNode;
    private Map<Landmark, Node> landmarkNodes = new LinkedHashMap<Landmark, Node>();
    private World world;
    private Map<Landmark, List<Node>> computedPaths = new LinkedHashMap<Landmark, List<Node>>();
    private Map<Landmark, List<String>> computedPathDirections = new LinkedHashMap<Landmark, List<String>>();

    public Map<Landmark, List<String>> getComputedPathDirections() {
        return computedPathDirections;
    }

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

            // set to compute the cost based on euclidean distance. The computed path is
            // much more straight forward
            // (Euclidean distance between two points in the Euclidean space is defined as
            // the length of the line segment between two points)

            CustomCost customCost = new CustomCost();
            astar.setCosts(customCost);

            landmarkNodes.forEach((key, value) -> {
                // skip the reached landmarks
                if (world.getLandmarkMap().get(key) == true) {
                    if (computedPaths.containsKey(key)) {
                        computedPaths.remove(key);
                    }

                    if (computedPathDirections.containsKey(key)) {
                        computedPathDirections.remove(key);
                    }

                    return;
                }

                astar.compute(agentNode.getId(), value.getId());
                Path path = astar.getShortestPath();
                computedPaths.put(key, path.getNodePath());
            });

            sortPathByValue(computedPaths);

            // convert computed nodes to actual directions
            computedPaths.forEach((k, v) -> {
                convertToActualDirections(k, v);

            });

            // set the computed path/directions to the world
            // world.setShortestLandmarkDirections(computedPathDirections);
        }
    }

    private void convertToActualDirections(Landmark landmark, List<Node> paths) {

        if (landmark == null || paths == null || paths.size() <= 0) {
            return;
        }

        List<String> tempDirectionList = new ArrayList<String>();

        // int tempY = (int) agentNode.getAttribute("y");
        // int tempX = (int) agentNode.getAttribute("x");

        // get the first index which is the agent position
        int tempY = (int) paths.get(0).getAttribute("y");
        int tempX = (int) paths.get(0).getAttribute("x");

        // remove the first index from the computed path which is the agent current
        // position
        // we have a temp copy above for direction calculation
        paths.remove(0);

        int totalDirections = (int) (paths.size() / world.getRobots().get(0).getSpeed());
        int pathMultiplier = totalDirections / paths.size();

        for (int i = 0; i < paths.size(); i++) {

            String[] tempArr = new String[(int) pathMultiplier];

            String direction = "";
            Node path = paths.get(i);

            // set the previous node as temp node
            // this is to compare every two nodes together.
            if (i > 0) {
                tempY = (int) paths.get(i - 1).getAttribute("y");
                tempX = (int) paths.get(i - 1).getAttribute("x");
            }

            int targetY = (int) path.getAttribute("y");
            int targetX = (int) path.getAttribute("x");

            direction += tempY < targetY ? DirectionEnum.NORTH.getName()
                    : tempY > targetY ? DirectionEnum.SOUTH.getName() : "";

            direction += tempX < targetX ? DirectionEnum.EAST.getName()
                    : tempX > targetX ? DirectionEnum.WEST.getName() : "";

            Arrays.fill(tempArr, 0, tempArr.length, direction);

            // if (i == 1) {
            // tempDirectionList.addAll(0, Arrays.asList(tempArr));
            // }

            tempDirectionList.addAll(Arrays.asList(tempArr));

        }

        // System.out.println(tempDirectionList);
        computedPathDirections.put(landmark, tempDirectionList);
    }

    // private void convertToDirectionsManhathanDistance(Landmark landmark,
    // List<Node> paths) {

    // if (landmark == null || paths == null || paths.size() <= 0) {
    // return;
    // }

    // List<String> tempDirectionList = new ArrayList<String>();

    // int agentY = (int) agentNode.getAttribute("y");
    // int agentX = (int) agentNode.getAttribute("x");

    // int landmarkY = (int) paths.get(paths.size() - 1).getAttribute("y");
    // int landmarkX = (int) paths.get(paths.size() - 1).getAttribute("x");

    // // Manhattan distance calculation Manhattan Distance = | x 1 − x 2 | + | y 1
    // − y
    // // 2 |
    // // Manhattan distance is more appropriate for measuring distance on a
    // grid-like
    // // structure, such as a chessboard or a computer screen.
    // // math.abs to make number positive
    // int manhattanDistance = Math.abs(agentX - landmarkX) + Math.abs(agentY -
    // landmarkY);
    // double totalMovements = manhattanDistance /
    // world.getRobots().get(0).getSpeed();

    // double newMovmentPerNode = Math.round(totalMovements / paths.size());
    // // System.out.println(tempArr.length);

    // for (int i = 1; i < paths.size(); i++) {
    // String[] tempArr = new String[(int) newMovmentPerNode];

    // String direction = "";
    // Node path = paths.get(i);

    // int currentY = (int) path.getAttribute("y");
    // int currentX = (int) path.getAttribute("x");

    // direction += agentY < currentY ? DirectionEnum.NORTH.getName()
    // : agentY > currentY ? DirectionEnum.SOUTH.getName() : "";

    // direction += agentX < currentX ? DirectionEnum.EAST.getName()
    // : agentX > currentX ? DirectionEnum.WEST.getName() : "";

    // Arrays.fill(tempArr, 0, tempArr.length, direction);

    // if (i == 1) {
    // tempDirectionList.addAll(0, Arrays.asList(tempArr));
    // }

    // tempDirectionList.addAll(Arrays.asList(tempArr));

    // // temp add extra element for small path sizes in order for landmark
    // detection
    // // to completely happen
    // if (i == paths.size() - 1) {
    // tempDirectionList.add(direction);
    // }

    // }

    // // System.out.println(tempDirectionList);
    // computedPathDirections.put(landmark, tempDirectionList);
    // }

    /*
     * Convert computed landmark node to actual directions.
     * this method has to be called for every landmark separately
     */
    // private void convertToDirections(Landmark landmark, List<Node> paths) {

    // List<String> tempDirectionList = new ArrayList<String>();

    // // last node is the landmark position
    // // Node landmarkNode = paths.get(paths.size() - 1);

    // // start from second index as the first index is the agent current position.
    // // looping from 0 will result in epmty string for 0
    // for (int i = 1; i < paths.size(); i++) {
    // String direction = "";
    // Node path = paths.get(i);

    // int agentY = (int) agentNode.getAttribute("y");
    // int agentX = (int) agentNode.getAttribute("x");

    // int landmarkY = (int) path.getAttribute("y");
    // int landmarkX = (int) path.getAttribute("x");

    // direction += agentY < landmarkY ? DirectionEnum.NORTH.getName()
    // : agentY > landmarkY ? DirectionEnum.SOUTH.getName() : "";

    // direction += agentX < landmarkX ? DirectionEnum.EAST.getName()
    // : agentX > landmarkX ? DirectionEnum.WEST.getName() : "";

    // // copy the index 1 position to index 0 (agent current position)
    // if (i == 1) {
    // tempDirectionList.add(0, direction);
    // }

    // // add each direction twice in order to be similar to the actual environment
    // // movements.
    // tempDirectionList.add(i, direction);
    // tempDirectionList.add(i + 1, direction);
    // }

    // computedPathDirections.put(landmark, tempDirectionList);
    // }

    private void sortPathByValue(Map<Landmark, List<Node>> computedPaths) {
        List<Entry<Landmark, List<Node>>> sortedList = new LinkedList<Entry<Landmark, List<Node>>>(
                computedPaths.entrySet());

        // sort by the list size
        Collections.sort(sortedList, new Comparator<Entry<Landmark, List<Node>>>() {
            @Override
            public int compare(Entry<Landmark, List<Node>> o1, Entry<Landmark, List<Node>> o2) {
                // compare two object and return an integer
                return Integer.compare(o1.getValue().size(), o2.getValue().size());
            }
        });

        // set the sorted value back to computedPaths
        computedPaths.clear();
        for (Entry<Landmark, List<Node>> entry : sortedList) {
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
                    Landmark landmark = (Landmark) world.getLandmarkMap().keySet().toArray()[landmarkCounter];
                    Boolean landmarkValue = (Boolean) world.getLandmarkMap().values().toArray()[landmarkCounter];
                    landmarkCounter++;

                    String name = landmarkValue ? "✓" + landmark.getName() : landmark.getName();

                    node.setAttribute("nodeName", name);
                    // landmarkNodes.add(node);
                    landmarkNodes.put(landmark, node);

                } else if (mapMatrix[i][j] == 3) {
                    node.setAttribute("ui.style", "fill-color: yellow; text-size: 12;"); // agent
                    node.setAttribute("nodeName", "Agent");

                    if (agentNode == null)
                        agentNode = node;

                    // add the node to second node so it could be used if it exists
                    secondAgentNode = node;
                }

                // START: edges for cardinal directions
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

                // END

                // START: edges for intercardinal/ordinal directions
                if (i > 0 && j > 0) {
                    // create edge to the northwest
                    String edgeId = i + "-" + j + "-" + (i - 1) + "-" + (j - 1);
                    Edge edge = graph.addEdge(edgeId, i + "-" + j, (i - 1) + "-" + (j - 1), false);

                    if (mapMatrix[i - 1][j - 1] == 1 || mapMatrix[i][j] == 1) {
                        // edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    } else {
                        // edge.setAttribute("ui.style", "fill-color: green;");
                        edge.setAttribute("weight", 0.5);
                    }
                }

                if (i > 0 && j < mapMatrix[0].length - 1) {
                    // create edge to the northeast
                    String edgeId = i + "-" + j + "-" + (i - 1) + "-" + (j + 1);
                    Edge edge = graph.addEdge(edgeId, i + "-" + j, (i - 1) + "-" + (j + 1), false);

                    if (mapMatrix[i - 1][j + 1] == 1 || mapMatrix[i][j] == 1) {
                        // edge.setAttribute("ui.style", "fill-color: red;"); // obstacles
                        edge.setAttribute("weight", 100);
                    } else {
                        // edge.setAttribute("ui.style", "fill-color: green;");
                        edge.setAttribute("weight", 0.5);
                    }
                }

                // END

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
    public Graph updateAgentNode(Integer newAgentMapMatrixX, Integer newAgentMapMatrixY,
            Integer secondNewAgentMapMatrixX, Integer secondNewAgentMapMatrixY) {

        if (newAgentMapMatrixX == null || newAgentMapMatrixY == null)
            return graph;

        // update the previous node to an empty space
        Node previousAgentNode = graph.getNode(agentNode.getId());
        previousAgentNode.setAttribute("ui.style", "fill-color: #DCDCDC;");
        previousAgentNode.setAttribute("nodeName", "");

        // if the previous node is similar to one of the landmark node
        // update the node name with original landmark name and ✓ to indicate it has
        // reached.
        for (Map.Entry<Landmark, Node> landmarkNode : landmarkNodes.entrySet()) {
            if (landmarkNode.getValue().getId() == previousAgentNode.getId()) {
                // text-size: 20
                previousAgentNode.setAttribute("ui.style", "fill-color: #DCDCDC; text-size: 20;");
                previousAgentNode.setAttribute("nodeName", "✓" +landmarkNode.getKey().getName());
                break;
            }
        }

        // update the new agent node location
        Node newAgentNode = graph.getNode(newAgentMapMatrixY + "-" + newAgentMapMatrixX);
        newAgentNode.setAttribute("ui.style", "fill-color: yellow; text-size: 12;"); // agent
        newAgentNode.setAttribute("nodeName", "Agent");

        // update and keep a copy of the new node
        agentNode = newAgentNode;

        // update second node(s) related data if exists
        if (secondNewAgentMapMatrixY != null && secondNewAgentMapMatrixX != null) {

            // delete the second node previous position if it exists
            // the second node id need to be check with other agent node in order not to
            // delete
            // disappear other agent node on UI
            if (secondAgentNode != null && secondAgentNode.getId() != agentNode.getId()) {
                Node secondPreviousAgentNode = graph.getNode(secondAgentNode.getId());
                secondPreviousAgentNode.setAttribute("ui.style", "fill-color: #DCDCDC;");
                secondPreviousAgentNode.setAttribute("nodeName", "");
            }

            Node secondNewAgentNode = graph.getNode(secondNewAgentMapMatrixY + "-" + secondNewAgentMapMatrixX);
            secondNewAgentNode.setAttribute("ui.style", "fill-color: red; text-size: 12;"); // agent
            secondNewAgentNode.setAttribute("nodeName", "Agent");

            // update and keep a copy of the new second node
            secondAgentNode = secondNewAgentNode;
        }

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
        // calculate the path again because agent node is changed
        calculateShortPath();

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
