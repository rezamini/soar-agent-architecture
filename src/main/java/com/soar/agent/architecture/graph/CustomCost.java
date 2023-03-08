package com.soar.agent.architecture.graph;

import org.graphstream.algorithm.AStar.Costs;
import org.graphstream.algorithm.AStar.DistanceCosts;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.edgeLength;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;

public class CustomCost implements Costs {

    @Override
    public double heuristic(Node node, Node target) {

        // return 0;

        double xy1[] = nodePosition(node);
        double xy2[] = nodePosition(target);

        double x = xy2[0] - xy1[0];
        double y = xy2[1] - xy1[1];
        double z = (xy1.length > 2 && xy2.length > 2) ? (xy2[2] - xy1[2])
                : 0;

        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    @Override
    public double cost(Node parent, Edge edge, Node next) {
        if (edge != null && edge.hasNumber("weight"))
            return ((Number) edge.getNumber("weight")).doubleValue();

        return 1;

        // return edgeLength(edge);
    }
}
