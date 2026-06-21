package rvo2example;

import java.util.List;
import java.util.LinkedList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.awt.Color;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;
import rvo2.Goal;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import java.util.Random;

/**
 * TC8: Two Intersections - 6 Rules Agents
 */

/*This is used for the baseline */
public class TestCase08_setting2 extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;
//======================PARAMETERS==========================

    public TestCase08_setting2() {
        scenarioName = "TC8: Two Intersections - 6 Rules";
        trafficType = "2-intersection-6-rules";
        totalAgents = 25;
    }
    private int spawnIndex = 0;
    private List<int[]> flowOrder = new ArrayList<>();
    int leftToTop = 0; //0-4
    int leftToRight = 0; //0-6
    int bottom1ToTop1 = 0; //3-4
    int bottom1ToRight = 0; //3-6
    int bottom1ToTop2  = 0; //3-8
    // Added counters for second intersection
    int leftToTop2 = 0; // 0-8
    int bottom2ToRight = 0; // 7-6
    int bottom2ToTop2 = 0; // 7-8
    

    double startTime = 0;
    double endTime = 0;
    static Random rand = new Random();

    int[] sources = {0, 3, 7}; // Added 7 for second bottom
    int[] destinations = {4, 6, 8}; // Added 6 and 8 for second intersection

    int spawnedAgents = 0;
    double spawnInterval = 60;
    double lastSpawnTime = 0;
    private Graph graph;
    // Coordinate of the circle
    int x_C = 0;
    int y_C = 0;
    // Added positions for nodes 1 and 5
    private Vector2D node1Pos;
    private Vector2D node5Pos;

    //======================Functions==========================
    // This function help create a node between 2 created nodes.
//    private void addTwoIntermediateNodes(
//            java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> nodePoints,
//            int indexA,
//            int indexB) {
//
//        org.apache.commons.math3.geometry.euclidean.twod.Vector2D A = nodePoints.get(indexA);
//        org.apache.commons.math3.geometry.euclidean.twod.Vector2D B = nodePoints.get(indexB);
//
//        // midpoint 
//        org.apache.commons.math3.geometry.euclidean.twod.Vector2D mid
//                = new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(
//                        (A.getX() + B.getX()) / 2.0,
//                        (A.getY() + B.getY()) / 2.0
//                );
//
//        nodePoints.add(mid);
//    }
    //This functions will return the src/dst base on Normal Distribution
    public static int chooseByNormal(int[] options, double mean, double std) {

        double gaussian = rand.nextGaussian();
        double value = mean + std * gaussian;

        // Clamp between 0 and 1
        value = Math.max(0, Math.min(1, value));

        int index = (value < 0.5) ? 0 : 1;

        return options[index];
    }

    //This function helps create the 2 edges A-->B-->C 
    private void createEdge(int indexA, int indexB, int indexC, java.util.List<algorithms.dijkstra.model.Vertex> nodes, java.util.List<algorithms.dijkstra.model.Edge> edges) {
        edges.add(new algorithms.dijkstra.model.Edge(
                "E_" + indexA + "_" + indexB,
                nodes.get(indexA),
                nodes.get(indexB),
                nodes.get(indexA).getVertex1().distance(nodes.get(indexB).getVertex1())
        ));
        edges.add(new algorithms.dijkstra.model.Edge(
                "E_" + indexB + "_" + indexC,
                nodes.get(indexB),
                nodes.get(indexC),
                nodes.get(indexB).getVertex1().distance(nodes.get(indexC).getVertex1())
        ));

    }

    private void shuffleFlows(Random rand) {

        flowOrder.clear();

        flowOrder.add(new int[]{0, 6});
        flowOrder.add(new int[]{0, 4});
        flowOrder.add(new int[]{0, 8});
        flowOrder.add(new int[]{3, 6});
        flowOrder.add(new int[]{3, 4});
        // Added flow for second bottom to top
        flowOrder.add(new int[]{3, 8});
        flowOrder.add(new int[]{7, 6});
        flowOrder.add(new int[]{7, 8});
        Collections.shuffle(flowOrder, rand);
    }

    //This function will spawn one agent with defined Dst and Src
    private void spawnOneAgent(Graph graph, double[][] behavior, Random rand, double gap) {
//Normal
//        int src = chooseByNormal(sources, 0.5, 1);
//        int dst = chooseByNormal(destinations, 0.5, 1);

//        Uniform Distribution
        if (spawnIndex % 8 == 0) { // Changed to 5 for added flow
            shuffleFlows(rand);
        }

        int[] flow = flowOrder.get(spawnIndex % 8); // Changed to 5

        int src = flow[0];
        int dst = flow[1];

        spawnIndex++;
//
//        
//	 Just Random
        ////	  int src = sources[rand.nextInt(2)];
////	  int dst = destinations[rand.nextInt(2)];
        if (src == 0 && dst == 4) {
            leftToTop++;
        }
        if (src == 0 && dst == 6) {
            leftToRight++;
        }
        if (src == 3 && dst == 4) {
            bottom1ToTop1++;
        }
        if (src == 3 && dst == 6) {
            bottom1ToRight++;
        }

        if (src == 3 && dst == 8) {
                bottom1ToTop2++;
        }
        // Added counters for second intersection
        if (src == 0 && dst == 8) {
            leftToTop2++;
        }
        if (src == 7 && dst == 6) {
            bottom2ToRight++;
        }
        if (src == 7 && dst == 8) {
            bottom2ToTop2++; // Added for second bottom to top
        }

        int k = Simulator.instance.getNumAgents();
        int behaviorIndex = rand.nextInt(7);

        Simulator.instance.addAgent(
                translateCoordinateVector(
                        graph.getVertexes().get(src).getVertex1()
                )
        );

        Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
        Simulator.instance.setAgentMaxNeighbors(k, (int) behavior[behaviorIndex][1]);
        Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);

        double radius = gap * 0.21;
        Simulator.instance.setAgentRadius(k, radius);
        Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);

        Goal goal = new Goal(
                translateCoordinateVector(
                        graph.getVertexes().get(dst).getVertex1()
                )
        );

        goal.setColor(getColor());
        goal.setFlag(1);
        goal.setSource(src);
        goal.setDestination(dst);

        

        int intersectionNode = (src == 0 || src == 3) ? 1 : 5; // Choose intersection based on source
        Vector2D point = translateCoordinateVector(
                graph.getVertexes().get(intersectionNode).getVertex1()
        );

        goal.add(point);

        

        if ((src == 0 || src == 3) && (dst == 6 || dst == 8)) {
            goal.add(translateCoordinateVector(graph.getVertexes().get(5).getVertex1()));
        }


        // if (BDI_ENABLE && src != dst) {
        //     DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        //     LinkedList<Vertex> path = getShortestPath(dijkstra, graph.getVertexes().get(src), graph.getVertexes().get(dst));
        //     if (path != null && path.size() > 2) {
        //         for (int i = 1; i < path.size() - 1; i++) {
        //             goal.add(translateCoordinateVector(path.get(i).getVertex1()));
        //         }
        //     }
        // }


        goalsList.add(goal);

    }

    //This is a setup function which only run once to define the environment
    @Override
    protected void setupScenario() {

        startSearchTime = System.currentTimeMillis();
        startTime = Simulator.instance.getGlobalTime();

        Simulator.instance.setTimeStep(1);
        Simulator.instance.setAgentDefaults(80, 40, 38, 2.5, 3.55, 2.0,
                org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO);

        double[][] behavior = createBehaviorMatrix();

        //=========OBSTACLES CREATION===============
        obsList1 = new java.util.ArrayList<Obs>();
        int rowsObs = 2;
        int colsObs = 3; // Changed from 2 to 3 for two intersections

        int decreScale = 1;

        double blockWidth = 450 / decreScale;  // pixels
        double blockHeight = 375 / decreScale; // pixels
        double gap = 50 / decreScale;          // pixels between blocks

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

        //=============NODE CREATION===============
        java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> nodePoints = new java.util.ArrayList<>();

        //=====Coordinates for Nodes Creation=======
        double yMid = startY + blockHeight + gap / 2.0;
        y_C = (int) (yMid);
        double xLeft = startX;
        double xBetween = startX + blockWidth + gap / 2.0;
        x_C = (int) (xBetween);
        double xRight = startX + blockWidth * 3 + gap * 2; // Updated for two intersections
        double xBetween2 = startX + blockWidth * 2 + gap * 1.5; // Corrected position for second intersection
        double xRight2 = startX + blockWidth * 3 + gap * 2; // Added for second intersection
        double yTop = startY;
        double yBottom = yTop + blockHeight * 2 + gap;
        double xMid12 = (xBetween + xBetween2) / 2.0;  
        //     4      8
        // 0   1   2  5   6
        //     3      7     

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft, yMid)); //0

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yMid)); //1

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xMid12, yMid)); //2

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yBottom)); //3

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yTop)); //4

        // Added nodes for second intersection
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yMid)); //5

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight2, yMid)); //6

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yBottom)); //7

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yTop)); //8

        java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
        java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();
        for (int i = 0; i < nodePoints.size(); i++) {
            org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
            algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex("node_" + i, i, 0, "node_" + i, p, p, 0);
            nodes.add(v);
        }

        createEdge(0, 1, 2, nodes, edges);
        createEdge(3, 1, 4, nodes, edges);
        // Added edges for second intersection
        createEdge(2, 5, 6, nodes, edges);
        createEdge(7, 5, 8, nodes, edges);

        // createEdge(1, 2, 5, nodes, edges);

        graph = new Graph(nodes, edges);
        System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());

        // Added: Store positions for painting
        node1Pos = translateCoordinateVector(graph.getVertexes().get(1).getVertex1());
        node5Pos = translateCoordinateVector(graph.getVertexes().get(5).getVertex1());

        spawnOneAgent(graph, behavior, rand, gap);
        spawnedAgents++;
        lastSpawnTime = 0;

//        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
//        if (BDI_ENABLE) {
//            for (int k = 0; k < Simulator.instance.getNumAgents(); k++) {
//                if (goalsList.get(k).getSource() != goalsList.get(k).getDestination()) {
//                    LinkedList<Vertex> path = getShortestPath(dijkstra,
//                            graph.getVertexes().get(goalsList.get(k).getSource()),
//                            graph.getVertexes().get(goalsList.get(k).getDestination()));
//                    if (path == null) {
//                        Simulator.instance.setAgentMaxSpeed(k, 0.0);
//                        continue;
//                    }
//                    for (Vertex v : path) {
//                        goalsList.get(k).add(translateCoordinateVector(v.getVertex1()));
//                    }
//                }
//            }
//        }
        System.out.println("Scenario initialized: " + scenarioName);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int radius = 31;
        g2.setColor(Color.GREEN);

        //  Draw circle at node 1
        if (node1Pos != null) {
            g2.drawOval(
                    //  (int) node1Pos.getX() - radius,
                    //  (int) node1Pos.getY() - radius,
                    710 - radius,
                    540 - radius,
                    radius * 2,
                    radius * 2
            );
        }

        //  Draw circle at node 5
        if (node5Pos != null) {
            g2.drawOval(
                    //  (int) node5Pos.getX() - radius,
                    //  (int) node5Pos.getY() - radius,
                    1210 - radius,
                    540 - radius,
                    radius * 2,
                    radius * 2
            );
        }

    }

    @Override
    protected void setPreferredVelocities() {
        super.setPreferredVelocities();
        double[][] behavior = createBehaviorMatrix();
        final double forwardDotThreshold = 0.85;
        double currentTime = Simulator.instance.getGlobalTime();
//        System.out.println(currentTime+" THIS IS CURRRENT TIME: StartTime " + startTime);
        int gap = 50;
        if (spawnedAgents < totalAgents
                && currentTime - lastSpawnTime >= spawnInterval) {

            spawnOneAgent(graph, behavior, rand, gap);

            spawnedAgents++;

            lastSpawnTime = currentTime;
        }
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
            double checkDistance = neighborDistance;

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
        TestCase08_setting2 testCase = new TestCase08_setting2();
        testCase.runSimulation();
    }

    @Override
    protected void printResults() {
        super.printResults();
        System.out.println("Agent Start and End Nodes:");
        for (int i = 0; i < goalsList.size(); i++) {
            Goal goal = goalsList.get(i);
            System.out.println("Agent " + i + ": Start Node " + goal.getSource() + " -> End Node " + goal.getDestination());
        }
    }
}
