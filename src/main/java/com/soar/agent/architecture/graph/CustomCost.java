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
        // the heuristic function is an estimate of the distance between the current
        // node and the target node.

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
        double edgeCost;
        if (edge != null && edge.hasNumber("weight")) {
            edgeCost = ((Number) edge.getNumber("weight")).doubleValue();
        } else {
            edgeCost = 1.0;
        }

        double edgeLength = edgeLength(edge);
        if (edgeLength < 0) {
            edgeLength = 0;
        }

        double totalCost = edgeCost + (2 * edgeLength); // adjust the weight of the edge length term as needed

        // edgeLength or testEdgeLength return 1.4142135623730951 for intermediate
        // directions ex: northeast
        // edgeLength or testEdgeLength return 1.0 for cardinal directions ex: north

        return totalCost;
    }

    // Multiplying the edge length by a factor of 2 will effectively double the cost
    // of traversing each edge, which will in turn cause the algorithm to try to
    // find paths with fewer edges. This can be beneficial if you want to find the
    // shortest path in terms of both distance and number of edges.

    // multiplying the edge length by 2 works
    // better than simply using the edge length as-is, it could be because the A*
    // algorithm is getting "stuck" in certain areas of the graph and zig-zagging
    // back and forth instead of finding the most direct path. By increasing the
    // cost of traversing each edge, the algorithm will be more likely to explore
    // alternative routes that may be more direct.

    // By multiplying the edge length by 2, you are effectively making the cost of
    // moving along an edge longer. This will discourage the algorithm from
    // exploring paths that are longer but closer to the target node. Instead, it
    // will explore paths that are shorter but may be farther from the target node.

    // the A* algorithm uses a heuristic function to estimate the remaining cost
    // from the current node to the target node. This heuristic function is used to
    // guide the search towards the goal and can greatly affect the performance of
    // the algorithm. In your case, the heuristic function is the Euclidean distance
    // between the current node and the target node.

    // When the edge length is multiplied by 2, the heuristic function becomes more
    // accurate, as it now reflects the true distance of the path if it were to
    // follow a straight line.

    // private double testEdgeLength(Edge edge){

    // double xy1[] = nodePosition(edge.getNode0());
    // double xy2[] = nodePosition(edge.getNode1());

    // double x = xy2[0] - xy1[0];
    // double y = xy2[1] - xy1[1];
    // double z = (xy1.length > 2 && xy2.length > 2) ? (xy2[2] - xy1[2])
    // : 0;

    // return Math.sqrt((x * x) + (y * y) + (z * z));
    // }
}
