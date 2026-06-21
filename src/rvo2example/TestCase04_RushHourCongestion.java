package rvo2example;

import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;

/**
 * TC4: Giờ cao điểm - Tắc đường Scenario: Mật độ cao, agent dày đặc, một chiều
 * Agents: 150 (15x10) Use case: Stress test, kiểm tra hiệu suất khi tắc đường
 */
public class TestCase04_RushHourCongestion extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;

    public TestCase04_RushHourCongestion() {
        scenarioName = "TC4: Rush Hour Congestion";
        trafficType = "1-way-heavy";
        totalAgents = 150;
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
        int rows = 15;
        int cols = 10;

        // Tất cả agent đi cùng hướng, nhưng mật độ cao hơn
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Mix các loại hành vi - phần lớn là hành vi bình thường (0)
                int behaviorIndex = (i % 5 == 0) ? (i / 5) % 3 : 0;
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
        TestCase04_RushHourCongestion testCase = new TestCase04_RushHourCongestion();
        testCase.runSimulation();
    }
}
