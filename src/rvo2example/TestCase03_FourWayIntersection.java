package rvo2example;

import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;

/**
 * TC3: Ngã tư giao thông - 4 luồng đối lập Scenario: 4 hướng: (+,+), (-,+),
 * (+,-), (-,-) Agents: 64 (8x8) Use case: Kiểm tra tình huống giao lộ phức tạp
 */
public class TestCase03_FourWayIntersection extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;

    public TestCase03_FourWayIntersection() {
        scenarioName = "TC3: Four-Way Intersection";
        trafficType = "4-way";
        totalAgents = 64; // 8x8
    }

    @Override
    protected void setupScenario() {
        startSearchTime = System.currentTimeMillis();
        Simulator.instance.setTimeStep(0.25);
        Simulator.instance.setAgentDefaults(15.0, 10, 5.0, 5.0, 2.0, 2.0,
                org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO);

        double[][] behavior = createBehaviorMatrix();

        GraphGenerator graphGen = new GraphGenerator();
        Graph graph = graphGen.genGraph("graph-connects.json");
        System.out.println("Graph vertices: " + graph.getVertexes().size());

        int agentNo = 0;
        int gridSize = 8;

        // Chia grid thành 4 phần, mỗi phần một hướng
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int behaviorIndex;

                // Xác định hướng dựa vào vị trí trong grid
                if (i < gridSize / 2 && j < gridSize / 2) {
                    behaviorIndex = 0; // Top-left: +,+ (direction 1)
                } else if (i < gridSize / 2 && j >= gridSize / 2) {
                    behaviorIndex = 2; // Top-right: -,+ (direction 2)
                } else if (i >= gridSize / 2 && j < gridSize / 2) {
                    behaviorIndex = 3; // Bottom-left: +,- (direction 3)
                } else {
                    behaviorIndex = 5; // Bottom-right: -,- (direction 4)
                }

                addAgent(graph, behaviorIndex, behavior, agentNo);
                agentNo++;
            }
        }

        setupObstacles();

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
        TestCase03_FourWayIntersection testCase = new TestCase03_FourWayIntersection();
        testCase.runSimulation();
    }
}
