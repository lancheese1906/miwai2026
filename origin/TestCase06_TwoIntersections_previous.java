package rvo2example;

import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;
import rvo2.Goal;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import java.util.Random;

/**
 * TC6: Two continuous intersections - simple linear two-intersection scenario
 * Agents: 30 (5x6)
 */

/*This is used for the baseline */
public class TestCase06_TwoIntersections extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;

    public TestCase06_TwoIntersections() {
        scenarioName = "TC6: Two Continuous Intersections";
        trafficType = "2-intersections";
        totalAgents = 30; // 5 x 6 grid
    }

    @Override
    protected void setupScenario() {
        startSearchTime = System.currentTimeMillis();
        Simulator.instance.setTimeStep(0.25);
        Simulator.instance.setAgentDefaults(15.0, 10, 5.0, 5.0, 2.0, 2.0,
                org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO);

        double[][] behavior = createBehaviorMatrix();

        // Graph will be auto-generated from the obstacle layout below (after we compute block sizes and positions).
        // Create 6 manually positioned obstacle blocks (2 rows x 3 cols) scaled to fill the view
        obsList1 = new java.util.ArrayList<Obs>();

        //increase the number of blocks 
        int rowsObs = 2;
        int colsObs = 3;
        double blockWidth = 450;  // pixels
        double blockHeight = 375; // pixels
        double gap = 30;          // pixels between blocks

        double totalWidth = colsObs * blockWidth + (colsObs - 1) * gap;
        double totalHeight = rowsObs * blockHeight + (rowsObs - 1) * gap;

        double startX = (w - totalWidth) / 2.0;
        double startY = (h - totalHeight) / 2.0;

        for (int r = 0; r < rowsObs; r++) {
            for (int c = 0; c < colsObs; c++) {
                double x = startX + c * (blockWidth + gap);
                double y = startY + r * (blockHeight + gap);

                java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> coord1 = new java.util.ArrayList<>();
                coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y));
                coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y));
                coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y + blockHeight));
                coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y + blockHeight));

                java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> coord2 = new java.util.ArrayList<>();
                coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y)));
                coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y + blockHeight)));
                coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y + blockHeight)));
                coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y)));

                Obs block = new Obs(coord1, coord2);
                obsList1.add(block);
                Simulator.instance.addObstacle(block.coord2);
            }
        }
        Simulator.instance.processObstacles();

        // Auto-generate graph from the obstacle layout so nodes are placed in corridors
        // (use values computed above for totalWidth/totalHeight/startX/startY)
        java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> nodePoints = new java.util.ArrayList<>();

        double yMid = startY + blockHeight + gap / 2.0;

        double xLeft = startX / 2.0;
        double xBetween1 = startX + blockWidth + gap / 2.0;
        double xBetween2 = startX + 2 * blockWidth + gap + gap / 2.0;
        double xRight = startX + totalWidth + (w - (startX + totalWidth)) / 2.0;

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft, yMid));
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween1, yMid));
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yMid));
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight, yMid));

        double yTop = startY / 2.0;
        double yBottom = startY + totalHeight + (h - (startY + totalHeight)) / 2.0;
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween1, yTop));
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween1, yBottom));

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yTop));
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yBottom));

        java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
        java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();
        for (int i = 0; i < nodePoints.size(); i++) {
            org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
            algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex("node_" + i, i, 0, "node_" + i, p, p, 0);
            nodes.add(v);
        }

        for (int i = 0; i < 3; i++) {
            algorithms.dijkstra.model.Vertex a = nodes.get(i);
            algorithms.dijkstra.model.Vertex b = nodes.get(i + 1);
            double dist = a.getVertex1().distance(b.getVertex1());
            edges.add(new algorithms.dijkstra.model.Edge("E_" + i + "_" + (i + 1), a, b, dist));
//            edges.add(new algorithms.dijkstra.model.Edge("E_" + (i + 1) + "_" + i, b, a, dist));
        }
        //     4   6   
        // 0 - 1 - 2 - 3
        //     5   7  
        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_top", nodes.get(1), nodes.get(4), nodes.get(1).getVertex1().distance(nodes.get(4).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_bot_r", nodes.get(5), nodes.get(1), nodes.get(5).getVertex1().distance(nodes.get(1).getVertex1())));

        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_top_r", nodes.get(6), nodes.get(2), nodes.get(6).getVertex1().distance(nodes.get(2).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_bot", nodes.get(2), nodes.get(7), nodes.get(2).getVertex1().distance(nodes.get(7).getVertex1())));

        Graph graph = new Graph(nodes, edges);
        System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());

        // Add deterministic agents to create two opposing flows across the intersections
//        int nAgents = totalAgents;
//        for (int k = 0; k < nAgents; k++) {
//            int src, dst, behaviorIndex;
//            if (k < nAgents / 2) {
//                behaviorIndex = 0; // left->right
//                src = 0 + (k % 2); // node 0 or 1
//                dst = graph.getVertexes().size() - 1 - (k % 2); // use far nodes
//            } else {
//                behaviorIndex = 2; // right->left
//                src = graph.getVertexes().size() - 1 - (k % 2);
//                dst = 0 + (k % 2);
//            }
//
//            Simulator.instance.addAgent(translateCoordinateVector(graph.getVertexes().get(src).getVertex1()));
//            Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
//            Simulator.instance.setAgentMaxNeighbors(k, (int) behavior[behaviorIndex][1]);
//            Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);
//            Simulator.instance.setAgentRadius(k, behavior[behaviorIndex][3]);
//            Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);
//
//            goalsList.add(new Goal(translateCoordinateVector(graph.getVertexes().get(dst).getVertex1())));
//            goalsList.get(k).setColor(getColor());
//            goalsList.get(k).setFlag(1);
//            goalsList.get(k).setSource(src);
//            goalsList.get(k).setDestination(dst);
//            
//        }
//        int nAgents = totalAgents;
//
//        int flow1Count = nAgents / 3;
//        int flow2Count = nAgents / 3;
//        int flow3Count = nAgents - flow1Count - flow2Count;
//        for (int k = 0; k < nAgents; k++) {
//
//            int src, dst, behaviorIndex;
//
//            if (k < flow1Count) {
//                // ===== FLOW 1: 0 → 1 → 2 → 3 =====
//                behaviorIndex = 0;      // left -> right
//                src = 0;
//                dst = 3;
//
//            } else if (k < flow1Count + flow2Count) {
//                // ===== FLOW 2: 5 → 1 → 4 =====
//                behaviorIndex = 1;      // vertical
//                src = 5;
//                dst = 4;
//
//            } else {
//                // ===== FLOW 3: 7 → 2 → 6 =====
//                behaviorIndex = 2;      // vertical
//                src = 6;
//                dst = 7;
//            }
//
//            // Spawn agent
//            Simulator.instance.addAgent(
//                translateCoordinateVector(
//                    graph.getVertexes().get(src).getVertex1()
//                )
//            );
//
//            // Set behavior
//            Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
//            Simulator.instance.setAgentMaxNeighbors(k, (int) behavior[behaviorIndex][1]);
//            Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);
//            Simulator.instance.setAgentRadius(k, behavior[behaviorIndex][3]);
//            Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);
//
//            // Goal
//            Goal goal = new Goal(
//                translateCoordinateVector(
//                    graph.getVertexes().get(dst).getVertex1()
//                )
//            );
//            goal.setColor(getColor());
//            goal.setFlag(1);
//            goal.setSource(src);
//            goal.setDestination(dst);
//
//            goalsList.add(goal);
//        }
        int nAgents = totalAgents;

        Random rand = new Random();
        int[] des_indexes = {3, 4, 7};
        int[] src_indexes = {0, 1, 2, 5, 6};

        // Obtain a number between [0 - 49].
        for (int k = 0; k < nAgents; k++) {

            int dst = des_indexes[rand.nextInt(3)];
            int src = src_indexes[rand.nextInt(5)];
//            int src = src_indexes[k];

            int behaviorIndex = rand.nextInt(7);

            // Spawn agent
            Simulator.instance.addAgent(
                    translateCoordinateVector(
                            graph.getVertexes().get(src).getVertex1()
                    )
            );

            // Set behavior
            // Increase neighbor-distance by 1.5x in this scenario to reduce agent crowding
            Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0] * 15);

            Simulator.instance.setAgentMaxNeighbors(k, (int) behavior[behaviorIndex][1]);
            Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);

            // Simulator.instance.setAgentRadius(k, behavior[behaviorIndex][3]);
            double randVal = 7.5 + (10 - 7.5) * Math.random();
            Simulator.instance.setAgentRadius(k, randVal); // randomize radius for more realism

            Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);

            // Goal
            Goal goal = new Goal(
                    translateCoordinateVector(
                            graph.getVertexes().get(dst).getVertex1()
                    )
            );
            goal.setColor(getColor());
            goal.setFlag(1);
            goal.setSource(src);
            goal.setDestination(dst);

            goalsList.add(goal);
        }

        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        if (BDI_ENABLE) {
            for (int k = 0; k < Simulator.instance.getNumAgents(); k++) {
                if (goalsList.get(k).getSource() != goalsList.get(k).getDestination()) {
                    LinkedList<Vertex> path = getShortestPath(dijkstra,
                            graph.getVertexes().get(goalsList.get(k).getSource()),
                            graph.getVertexes().get(goalsList.get(k).getDestination()));
                    if (path == null) {
                        Simulator.instance.setAgentMaxSpeed(k, 0.0);
                        continue;
                    }
                    for (Vertex v : path) {
                        goalsList.get(k).add(translateCoordinateVector(v.getVertex1()));
                    }
                }
            }
        }

        System.out.println("Scenario initialized: " + scenarioName);
    }

    @Override
    protected void setPreferredVelocities() {
        // First use the base behaviour (waypoint following + small perturbation)
        super.setPreferredVelocities();

        // Scenario-specific spacing when agents move in same direction
        final double spacingMultiplier = 2.0; // user requested: mạnh (2.0×)
        final double forwardDotThreshold = 0.85; // cos(angle) threshold to consider 'in front'

        int numAgents = Simulator.instance.getNumAgents();
        for (int agentNo = 0; agentNo < numAgents; agentNo++) {
            Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
            if (pref == null) {
                continue;
            }
            double prefNorm = pref.getNorm();
            if (prefNorm < 1e-6) {
                continue; // not moving
            }
            Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);

            double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
            double checkDistance = neighborDistance * spacingMultiplier;

            double minScale = 1.0; // will reduce preferred speed if someone ahead is too close

            for (int other = 0; other < numAgents; other++) {
                if (other == agentNo) {
                    continue;
                }
                Vector2D otherPos = Simulator.instance.getAgentPosition(other);
                if (otherPos == null) {
                    continue;
                }

                Vector2D rel = otherPos.subtract(agentPos);
                double dist = rel.getNorm();
                if (dist < 1e-6 || dist > checkDistance) {
                    continue;
                }

                double forwardness = prefDir.dotProduct(rel.scalarMultiply(1.0 / dist));
                if (forwardness < forwardDotThreshold) {
                    continue; // not roughly ahead
                }
                double safety = Simulator.instance.getAgentRadius(agentNo) + Simulator.instance.getAgentRadius(other) + 1.0;
                double desiredSpacing = Math.max(checkDistance, safety + 1.0);

                double scale = (dist - safety) / (desiredSpacing - safety);
                scale = Math.max(0.0, Math.min(1.0, scale));

                if (scale < minScale) {
                    minScale = scale;
                }
            }

            if (minScale < 1.0) {
                Vector2D newPref = pref.scalarMultiply(minScale);
                Simulator.instance.setAgentPreferredVelocity(agentNo, newPref);
            }
        }
    }

    public static void main(String[] args) {
        TestCase06_TwoIntersections testCase = new TestCase06_TwoIntersections();
        testCase.runSimulation();
    }
}
