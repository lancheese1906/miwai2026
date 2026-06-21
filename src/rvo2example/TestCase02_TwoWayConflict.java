package rvo2example;

import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;

/**
 * TC2: Đường hai chiều - Xung đột trực tiếp Scenario: Nửa agent đi hướng (+,+),
 * nửa đi hướng (-,-) Agents: 50 (7x7 xấp xỉ) Use case: Kiểm tra xử lý xung đột
 * khi hai luồng đối lập
 */
public class TestCase02_TwoWayConflict extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;

    public TestCase02_TwoWayConflict() {
        scenarioName = "TC2: Two-Way Conflict Traffic";
        trafficType = "2-way-conflict";
        totalAgents = 49; // 7x7
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
        int gridSize = 7;

        // Tạo 2 luồng đối lập
        // Top half: direction 1 (hành vi 0)
        for (int i = 0; i < gridSize / 2; i++) {
            for (int j = 0; j < gridSize; j++) {
                int behaviorIndex = 0; // Direction 1
                addAgent(graph, behaviorIndex, behavior, agentNo);
                agentNo++;
            }
        }

        // Bottom half: direction 2 (hành vi 2 - ngược chiều)
        for (int i = gridSize / 2; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int behaviorIndex = 2; // Direction 2 (opposite)
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
        TestCase02_TwoWayConflict testCase = new TestCase02_TwoWayConflict();
        testCase.runSimulation();
    }
}
