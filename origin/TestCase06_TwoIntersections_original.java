package rvo2example;

import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;
import rvo2.Goal;

/**
 * TC6: Two continuous intersections - simple linear two-intersection scenario
 * Agents: 30 (5x6)
 */
public class TestCase06_TwoIntersections extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;

    public TestCase06_TwoIntersections() {
        scenarioName = "TC6: Two Continuous Intersections";
        trafficType = "2-intersections";
        totalAgents = 100; // 5 x 6 grid
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

        int rowsObs = 2;
        int colsObs = 3;
        double blockWidth = 300;  // pixels
        double blockHeight = 250; // pixels
        double gap = 60;          // pixels between blocks

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
        double xBetween2 = startX + 2 * (blockWidth + gap) + gap / 2.0;
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
            edges.add(new algorithms.dijkstra.model.Edge("E_" + (i + 1) + "_" + i, b, a, dist));
        }

        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_top", nodes.get(1), nodes.get(4), nodes.get(1).getVertex1().distance(nodes.get(4).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_top_r", nodes.get(4), nodes.get(1), nodes.get(4).getVertex1().distance(nodes.get(1).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_bot", nodes.get(1), nodes.get(5), nodes.get(1).getVertex1().distance(nodes.get(5).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_bot_r", nodes.get(5), nodes.get(1), nodes.get(5).getVertex1().distance(nodes.get(1).getVertex1())));

        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_top", nodes.get(2), nodes.get(6), nodes.get(2).getVertex1().distance(nodes.get(6).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_top_r", nodes.get(6), nodes.get(2), nodes.get(6).getVertex1().distance(nodes.get(2).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_bot", nodes.get(2), nodes.get(7), nodes.get(2).getVertex1().distance(nodes.get(7).getVertex1())));
        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_bot_r", nodes.get(7), nodes.get(2), nodes.get(7).getVertex1().distance(nodes.get(2).getVertex1())));

        Graph graph = new Graph(nodes, edges);
        System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());

        // Add deterministic agents to create two opposing flows across the intersections
        int nAgents = totalAgents;
        for (int k = 0; k < nAgents; k++) {
            int src, dst, behaviorIndex;
            if (k < nAgents / 2) {
                behaviorIndex = 0; // left->right
                src = 0 + (k % 2); // node 0 or 1
                dst = graph.getVertexes().size() - 1 - (k % 2); // use far nodes
            } else {
                behaviorIndex = 2; // right->left
                src = graph.getVertexes().size() - 1 - (k % 2);
                dst = 0 + (k % 2);
            }

            Simulator.instance.addAgent(translateCoordinateVector(graph.getVertexes().get(src).getVertex1()));
            Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
            Simulator.instance.setAgentMaxNeighbors(k, (int) behavior[behaviorIndex][1]);
            Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);
            Simulator.instance.setAgentRadius(k, behavior[behaviorIndex][3]);
            Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);

            goalsList.add(new Goal(translateCoordinateVector(graph.getVertexes().get(dst).getVertex1())));
            goalsList.get(k).setColor(getColor());
            goalsList.get(k).setFlag(1);
            goalsList.get(k).setSource(src);
            goalsList.get(k).setDestination(dst);
        }

        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        if (BDI_ENABLE) {
            for (int k = 0; k < Simulator.instance.getNumAgents(); k++) {
                if (goalsList.get(k).getSource() != goalsList.get(k).getDestination()) {
                    LinkedList<Vertex> path = getShortestPath(dijkstra,
                            graph.getVertexes().get(goalsList.get(k).getSource()),
                            graph.getVertexes().get(goalsList.get(k).getDestination()));
                    for (Vertex v : path) {
                        goalsList.get(k).add(translateCoordinateVector(v.getVertex1()));
                    }
                }
            }
        }

        System.out.println("Scenario initialized: " + scenarioName);
    }

    public static void main(String[] args) {
        TestCase06_TwoIntersections testCase = new TestCase06_TwoIntersections();
        testCase.runSimulation();
    }
}
