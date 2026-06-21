package rvo2example;

import java.util.List;
import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;
import rvo2.Goal;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import java.util.Random;

/**
 * TC7: One intersection - 6 rules Agents: 30 (5x6)
 */

/*This is used for the baseline */
public class TestCase07_setting1 extends BaseTrafficScenario {

    private enum SpawnMode {
        UNIFORM,
        NORMAL,
        RANDOM
    }

    // private SpawnMode spawnMode = SpawnMode.RANDOM; 

    private SpawnMode flowSelectionMode = SpawnMode.UNIFORM;
    private SpawnMode spawnIntervalMode = SpawnMode.NORMAL;

    private SpawnMode spawnPositionMode = SpawnMode.UNIFORM;

    private Graph graphRef;
    private double[][] behaviorRef;
    private double gapRef;
    private Random rand = new Random();
    private double minSpawnGap = 20.0;
    private int[] flowSources = {0, 0, 3, 3};   // A, A, E, E
    private int[] flowDests   = {2, 4, 4, 2};   // C, D, D, C
    private int[] remainingPerFlow = {5, 3, 6, 7};
    private int simStep = 0;
    private int[] nextSpawnStep = {0, 0, 0, 0};

    private static final long serialVersionUID = 1L;

    public TestCase07_setting1() {
        scenarioName = "TC7: One Intersection - 6 Rules";
        trafficType = "1-intersection-6-rules";
        totalAgents = 21; // 5 + 3 + 6 + 7 = 21
    }

    private void addTwoIntermediateNodes(
            java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> nodePoints,
            int indexA,
            int indexB) {

        org.apache.commons.math3.geometry.euclidean.twod.Vector2D A = nodePoints.get(indexA);
        org.apache.commons.math3.geometry.euclidean.twod.Vector2D B = nodePoints.get(indexB);

        // midpoint 
        org.apache.commons.math3.geometry.euclidean.twod.Vector2D mid
                = new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(
                        (A.getX() + B.getX()) / 2.0,
                        (A.getY() + B.getY()) / 2.0
                );

        nodePoints.add(mid);
    }

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

    private boolean hasRemainingAgentsToSpawn() {
        for (int x : remainingPerFlow) {
            if (x > 0) return true;
        }
        return false;
    }

    private boolean canSpawnAt(Vector2D spawnPos, double minGap) {
        int numAgents = Simulator.instance.getNumAgents();
        for (int i = 0; i < numAgents; i++) {
            Vector2D p = Simulator.instance.getAgentPosition(i);
            if (p != null && p.distance(spawnPos) < minGap) {
                return false;
            }
        }
        return true;
    }


    // Normal Distribution Supporter
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int sampleSpawnInterval() {
        switch (spawnIntervalMode) {
            case UNIFORM:
                // đều trong [8, 14]
                return 8 + rand.nextInt(7);

            case NORMAL: {
                // trung bình 10 step, lệch chuẩn 2
                int val = (int) Math.round(10 + rand.nextGaussian() * 2.0);
                return Math.max(4, val);
            }

            case RANDOM:
            default:
                // chọn ngẫu nhiên từ vài mốc rời rạc
                int[] options = {4, 6, 9, 12, 15};
                return options[rand.nextInt(options.length)];
        }
    }

    private int pickFlowIndex() {
        java.util.List<Integer> available = new java.util.ArrayList<>();
        for (int i = 0; i < remainingPerFlow.length; i++) {
            if (remainingPerFlow[i] > 0 && simStep >= nextSpawnStep[i]) {
                available.add(i);
            }
        }

        if (available.isEmpty()) return -1;

        switch (flowSelectionMode) {
            case UNIFORM:
                return available.get(rand.nextInt(available.size()));

            case NORMAL: {
                // ưu tiên flow gần giữa: index ~ 1.5
                double mean = 1.5;
                double std = 1.0;

                for (int tries = 0; tries < 20; tries++) {
                    int idx = (int) Math.round(mean + rand.nextGaussian() * std);
                    if (idx >= 0 && idx < remainingPerFlow.length
                            && remainingPerFlow[idx] > 0
                            && simStep >= nextSpawnStep[idx]) {
                        return idx;
                    }
                }
                // fallback
                return available.get(rand.nextInt(available.size()));
            }

            case RANDOM:
            default: {
                // trọng số ngẫu nhiên mỗi lần
                double totalWeight = 0.0;
                double[] weights = new double[remainingPerFlow.length];

                for (int i = 0; i < remainingPerFlow.length; i++) {
                    if (remainingPerFlow[i] > 0 && simStep >= nextSpawnStep[i]) {
                        weights[i] = 0.5 + rand.nextDouble() * 2.0;
                        totalWeight += weights[i];
                    }
                }

                double r = rand.nextDouble() * totalWeight;
                double acc = 0.0;
                for (int i = 0; i < weights.length; i++) {
                    acc += weights[i];
                    if (weights[i] > 0 && r <= acc) {
                        return i;
                    }
                }

                return available.get(available.size() - 1);
            }
        }
    }

    // private Vector2D sampleSpawnPosition(Vector2D baseSpawn, int src) {
    //     // src = 0 (A) là lane ngang -> lệch nhẹ theo Y
    //     // src = 3 (E) là lane dọc   -> lệch nhẹ theo X

    //     boolean flag = true; // tạm thời chưa lệch

    //     if (flag) {
    //         return baseSpawn; // tạm thời chưa lệch
    //     }

    //     double uniformRange = gapRef * 0.10;
    //     double sigma = gapRef * 0.06;
    //     double maxOffset = gapRef * 0.15;

    //     double offset;

    //     switch (spawnMode) {
    //         case UNIFORM:
    //             offset = (rand.nextDouble() * 2.0 - 1.0) * uniformRange;
    //             break;

    //         case NORMAL:
    //             offset = clamp(rand.nextGaussian() * sigma, -maxOffset, maxOffset);
    //             break;

    //         case RANDOM:
    //         default:
    //             // random "tự do" hơn một chút
    //             offset = (rand.nextDouble() * 2.0 - 1.0) * (gapRef * 0.15);
    //             break;
    //     }

    //     if (src == 0) {
    //         // A: lane ngang, chỉ lệch Y
    //         return baseSpawn.add(new Vector2D(0, offset));
    //     } else {
    //         // E: lane dọc, chỉ lệch X
    //         return baseSpawn.add(new Vector2D(offset, 0));
    //     }
    // }

    private Vector2D sampleSpawnPosition(Vector2D baseSpawn, int src) {
        
        boolean flag = true; // tạm thời chưa lệch
        if (flag) {
            return baseSpawn; // tạm thời chưa lệch
        }

        double uniformRange = gapRef * 0.10;
        double sigma = gapRef * 0.06;
        double maxOffset = gapRef * 0.15;

        double offset;

        switch (spawnPositionMode) {
            case UNIFORM:
                offset = (rand.nextDouble() * 2.0 - 1.0) * uniformRange;
                break;

            case NORMAL:
                offset = clamp(rand.nextGaussian() * sigma, -maxOffset, maxOffset);
                break;

            case RANDOM:
            default:
                offset = (rand.nextDouble() * 2.0 - 1.0) * (gapRef * 0.15);
                break;
        }

        if (src == 0) {
            return baseSpawn.add(new Vector2D(0, offset));
        } else {
            return baseSpawn.add(new Vector2D(offset, 0));
        }
    }


    private void spawnOneAgentAtPosition(Vector2D spawnPos, int src, int dst) {
        int k = Simulator.instance.getNumAgents();
        int behaviorIndex = rand.nextInt(7);

        Simulator.instance.addAgent(spawnPos);

        Simulator.instance.setAgentNeighborDistance(k, behaviorRef[behaviorIndex][0]);
        Simulator.instance.setAgentMaxNeighbors(k, (int) behaviorRef[behaviorIndex][1]);
        Simulator.instance.setAgentTimeHorizonAgents(k, behaviorRef[behaviorIndex][2]);

        double radius = gapRef * 0.25;
        Simulator.instance.setAgentRadius(k, radius);
        Simulator.instance.setAgentMaxSpeed(k, behaviorRef[behaviorIndex][4]);

        Goal goal = new Goal(
                translateCoordinateVector(
                        graphRef.getVertexes().get(dst).getVertex1()
                )
        );

        goal.setColor(getColor());
        goal.setFlag(1);
        goal.setSource(src);
        goal.setDestination(dst);

        // TÍNH PATH NGAY KHI SPAWN
        if (BDI_ENABLE && src != dst) {
            DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graphRef);

            LinkedList<Vertex> path = getShortestPath(
                    dijkstra,
                    graphRef.getVertexes().get(src),
                    graphRef.getVertexes().get(dst)
            );

            if (path != null) {
                for (Vertex v : path) {
                    goal.add(translateCoordinateVector(v.getVertex1()));
                }
            }
        }

        goalsList.add(goal);
    }

    private void spawnAgentsIfPossible() {
        if (!hasRemainingAgentsToSpawn()) {
            simStep++;
            return;
        }

        int flow = pickFlowIndex();
        if (flow == -1) {
            simStep++;
            return;
        }

        int src = flowSources[flow];
        int dst = flowDests[flow];

        Vector2D baseSpawn = translateCoordinateVector(
                graphRef.getVertexes().get(src).getVertex1()
        );

        if (canSpawnAt(baseSpawn, minSpawnGap)) {
            Vector2D actualSpawn = sampleSpawnPosition(baseSpawn, src);
            spawnOneAgentAtPosition(actualSpawn, src, dst);

            remainingPerFlow[flow]--;
            nextSpawnStep[flow] = simStep + sampleSpawnInterval();
        }

        simStep++;
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
        int colsObs = 2;

        int decreScale = 1; // user requested: mạnh (2×)

        double blockWidth = 450/decreScale;  // pixels
        double blockHeight = 375/decreScale; // pixels
        double gap = 45/decreScale;          // pixels between blocks

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
        double xBetween = startX + blockWidth + gap / 2.0;
        double xRight = startX + totalWidth + (w - (startX + totalWidth)) / 2.0;
        double yTop = startY / 2.0;
        double yBottom = startY + totalHeight + (h - (startY + totalHeight)) / 2.0;
        //     4
        //     8      
        // 0 5 1 6 2
        //     7
        //     3     

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft+115, yMid)); //0

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yMid)); //1

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight-90, yMid)); //2

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yBottom-80)); //3
        
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yTop+80)); //4
        
        
        addTwoIntermediateNodes(nodePoints, 0, 1); //5
        
        addTwoIntermediateNodes(nodePoints, 1, 2); //6
        
        addTwoIntermediateNodes(nodePoints, 1, 3);//7

        // addTwoIntermediateNodes(nodePoints, 4, 1);//8

        // addTwoIntermediateNodes(nodePoints, 0, 5); //9
        
        // addTwoIntermediateNodes(nodePoints, 5, 1); //10
        
        // addTwoIntermediateNodes(nodePoints, 1, 6);//11

        // addTwoIntermediateNodes(nodePoints, 6, 2);//12

        // addTwoIntermediateNodes(nodePoints, 3, 7); //13
        
        // addTwoIntermediateNodes(nodePoints, 1, 7); //14
        
        // addTwoIntermediateNodes(nodePoints, 1, 8);//15

        // addTwoIntermediateNodes(nodePoints, 8, 4);//8

        //ADDING
        addTwoIntermediateNodes(nodePoints, 5, 1); //~8
        
        addTwoIntermediateNodes(nodePoints, 7, 1); //~9


        java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
        java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();
        for (int i = 0; i < nodePoints.size(); i++) {
            org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
            algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex("node_" + i, i, 0, "node_" + i, p, p, 0);
            nodes.add(v);
        }

//        for (int i = 0; i < 3; i++) {
//            algorithms.dijkstra.model.Vertex a = nodes.get(i);
//            algorithms.dijkstra.model.Vertex b = nodes.get(i + 1);
//            double dist = a.getVertex1().distance(b.getVertex1());
//            edges.add(new algorithms.dijkstra.model.Edge("E_" + i + "_" + (i + 1), a, b, dist));
//
//        }
        ////         Update supporting point
////             4   6   
////         0 - 1 - 2 - 3
////             5   7  
//        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_top", nodes.get(1), nodes.get(4), nodes.get(1).getVertex1().distance(nodes.get(4).getVertex1())));
//        edges.add(new algorithms.dijkstra.model.Edge("E_v_1_bot_r", nodes.get(5), nodes.get(1), nodes.get(5).getVertex1().distance(nodes.get(1).getVertex1())));
//
//        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_top_r", nodes.get(6), nodes.get(2), nodes.get(6).getVertex1().distance(nodes.get(2).getVertex1())));
//        edges.add(new algorithms.dijkstra.model.Edge("E_v_2_bot", nodes.get(2), nodes.get(7), nodes.get(2).getVertex1().distance(nodes.get(7).getVertex1())));


    
//     // ===== horizontal =====

     // 0 → 5 → 1
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_0_5",
        //         nodes.get(0),
        //         nodes.get(5),
        //         nodes.get(0).getVertex1().distance(nodes.get(5).getVertex1())
        // ));
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_5_1",
        //         nodes.get(5),
        //         nodes.get(1),
        //         nodes.get(5).getVertex1().distance(nodes.get(1).getVertex1())
        // ));

        // 0 → ~8→ 1
        createEdge(0, 8, 1, nodes, edges);
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_0_1",
        //         nodes.get(0),
        //         nodes.get(1),
        //         nodes.get(0).getVertex1().distance(nodes.get(1).getVertex1())
        // ));

        // 1 → 6 → 2
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_1_6",
        //         nodes.get(1),
        //         nodes.get(6),
        //         nodes.get(1).getVertex1().distance(nodes.get(6).getVertex1())
        // ));
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_6_2",
        //         nodes.get(6),
        //         nodes.get(2),
        //         nodes.get(6).getVertex1().distance(nodes.get(2).getVertex1())
        // ));

        // 1 → 2
        edges.add(new algorithms.dijkstra.model.Edge(
                "E_1_2",
                nodes.get(1),
                nodes.get(2),
                nodes.get(1).getVertex1().distance(nodes.get(2).getVertex1())
        ));

        // ===== vertical =====
        // 3 → 7 → 1
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_3_7",
        //         nodes.get(3),
        //         nodes.get(7),
        //         nodes.get(3).getVertex1().distance(nodes.get(7).getVertex1())
        // ));
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_7_1",
        //         nodes.get(7),
        //         nodes.get(1),
        //         nodes.get(7).getVertex1().distance(nodes.get(1).getVertex1())
        // ));

        // 3 → ~9 → 1
        createEdge(3, 9, 1, nodes, edges);
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_3_1",
        //         nodes.get(3),
        //         nodes.get(1),
        //         nodes.get(3).getVertex1().distance(nodes.get(1).getVertex1())
        // ));

        // 1 → 8 → 4
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_1_8",
        //         nodes.get(1),
        //         nodes.get(8),
        //         nodes.get(1).getVertex1().distance(nodes.get(8).getVertex1())
        // ));
        // edges.add(new algorithms.dijkstra.model.Edge(
        //         "E_8_4",
        //         nodes.get(8),
        //         nodes.get(4),
        //         nodes.get(8).getVertex1().distance(nodes.get(4).getVertex1())
        // ));

        // 1 → 4
        edges.add(new algorithms.dijkstra.model.Edge(
                "E_1_4",
                nodes.get(1),
                nodes.get(4),
                nodes.get(1).getVertex1().distance(nodes.get(4).getVertex1())
        ));

        // createEdge(0, 9, 5, nodes, edges);
        // createEdge(5, 10, 1, nodes, edges);
        // createEdge(3, 13, 7, nodes, edges);
        // createEdge(7, 14, 1, nodes, edges);

        // ===== All sources =====

        int nAgents = totalAgents;
        Random rand = new Random();
        

        Graph graph = new Graph(nodes, edges);
        System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());
        this.graphRef = graph;
        this.behaviorRef = behavior;
        this.gapRef = gap;
        this.minSpawnGap = gap * 1.2;
        
        // ===== spawn =====

        
        // DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        // if (BDI_ENABLE) {
        //     for (int k = 0; k < Simulator.instance.getNumAgents(); k++) {
        //         if (goalsList.get(k).getSource() != goalsList.get(k).getDestination()) {
        //             LinkedList<Vertex> path = getShortestPath(dijkstra,
        //                     graph.getVertexes().get(goalsList.get(k).getSource()),
        //                     graph.getVertexes().get(goalsList.get(k).getDestination()));
        //             if (path == null) {
        //                 Simulator.instance.setAgentMaxSpeed(k, 0.0);
        //                 continue;
        //             }
        //             for (Vertex v : path) {
        //                 goalsList.get(k).add(translateCoordinateVector(v.getVertex1()));
        //             }
        //         }
        //     }
        // }

        System.out.println("Scenario initialized: " + scenarioName);
    }

    @Override
    protected void setPreferredVelocities() {

        spawnAgentsIfPossible();

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
        TestCase07_setting1 testCase = new TestCase07_setting1();
        testCase.runSimulation();
    }
}
