package rvo2example;

import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;

/**
 * TC1: Đường một chiều - Buổi sáng nhẹ Scenario: Đường một chiều, tất cả agent
 * đi cùng hướng (+,+) Agents: 25 (5x5) Use case: Kiểm tra hiệu suất cơ bản khi
 * không có xung đột
 */
public class TestCase01_OneWayLight extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;

    public TestCase01_OneWayLight() {
        scenarioName = "TC1: One-Way Light Traffic";
        trafficType = "1-way";
        totalAgents = 25;
    }

    @Override
    protected void setupScenario() {
        startSearchTime = System.currentTimeMillis();
        Simulator.instance.setTimeStep(0.25); // Đúng timestep
        Simulator.instance.setAgentDefaults(15.0, 10, 5.0, 5.0, 2.0, 2.0,
                org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO);

        double[][] behavior = createBehaviorMatrix();

        GraphGenerator graphGen = new GraphGenerator();
        Graph graph = graphGen.genGraph("graph-connects.json");
        System.out.println("Graph vertices: " + graph.getVertexes().size());

        int agentNo = 0;
        int gridSize = 5; // 5x5 = 25 agents

        // Tất cả agent đi cùng hướng (+,+)
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int behaviorIndex = j % 3; // Mix 3 loại hành vi
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
        TestCase01_OneWayLight testCase = new TestCase01_OneWayLight();
        testCase.runSimulation();
    }
}
