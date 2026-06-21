package rvo2example;

import java.util.LinkedList;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;

/**
 * TC5: Vòng xoay/Roundabout - Luồng tuần hoàn Scenario: Agent đi theo chiều
 * tròn, tạo vòng Agents: 80 (10x8) Use case: Kiểm tra tình huống vòng xoay giao
 * thông
 */
public class TestCase05_Roundabout extends BaseTrafficScenario {

    private static final long serialVersionUID = 1L;

    public TestCase05_Roundabout() {
        scenarioName = "TC5: Roundabout Traffic";
        trafficType = "roundabout";
        totalAgents = 80;
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
        int rows = 10;
        int cols = 8;

        // Tạo vòng xoay: agent từ 4 hướng nhập vào
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int behaviorIndex;

                // Phía trên: hướng xuống
                if (i < rows / 4) {
                    behaviorIndex = 2; // Direction down
                } // Phía trái: hướng phải
                else if (j < cols / 4) {
                    behaviorIndex = 0; // Direction right
                } // Phía dưới: hướng lên
                else if (i >= rows * 3 / 4) {
                    behaviorIndex = 3; // Direction up
                } // Phía phải: hướng trái
                else if (j >= cols * 3 / 4) {
                    behaviorIndex = 5; // Direction left
                } // Giữa: mix hành vi
                else {
                    behaviorIndex = (i + j) % 3;
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
        TestCase05_Roundabout testCase = new TestCase05_Roundabout();
        testCase.runSimulation();
    }
}
