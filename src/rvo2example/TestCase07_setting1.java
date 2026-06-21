package rvo2example;

import java.util.List;
import java.util.LinkedList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Simulator;
import rvo2.Goal;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

/**
 * TC7: One intersection - 4 flows
 */
public class TestCase07_setting1 extends BaseTrafficScenario_S1 {

    private static final long serialVersionUID = 1L;

    // ====================== PARAMETERS ==========================
    public TestCase07_setting1() {
        scenarioName = "TC7: One Intersection - 4 Flows";
        trafficType = "1-intersection-4-flows";
        totalAgents = 25;
    }

    private int spawnIndex = 0;
    private List<int[]> flowOrder = new ArrayList<>();

    private boolean isBusy = false;
    private double nextSwitchTime = 0;
    private static final double DETECTION_RADIUS = 60.0;
    private static final double MIN_SPEED_FACTOR = 0.15;
    private int throughExitCount = 0;
    private int slowExitCount = 0;

    int leftToTop = 0;      // A-B-C (0 -> 2)
    int leftToRight = 0;    // A-B-E (0 -> 4)
    int bottomToTop = 0;    // D-B-C (3 -> 2)
    int bottomToRight = 0;  // D-B-E (3 -> 4)

    double startTime = 0;
    double endTime = 0;
    static Random rand = new Random();

    String disType = "Normal Distribution";

    int[] sources = {0, 3};
    int[] destinations = {2, 4};

    int spawnedAgents = 0;
    double spawnInterval = 60;
    double lastSpawnTime = 0;
    private double minSpawnInterval = 15.0;
    private double maxSpawnInterval = 120.0;
    // spawn interval settings
    private DISTRIBUTION_TYPE distributionType = DISTRIBUTION_TYPE.Uniformed;
    private double meanSpawnInterval = 60.0;
    private double stdSpawnInterval = 15.0;
    private double randomMinSpawnInterval = 20.0;
    private double randomMaxSpawnInterval = 100.0;

    private Graph graph;

    // Coordinate of the priority circle center
    int x_C = 0;
    int y_C = 0;

    // node points for drawing labels A,B,C,D,E
    private List<Vector2D> nodePoints = new ArrayList<>();

    // agent metadata
    Map<Integer, Agent> agents = new HashMap<>();

    // realtime graph
    private final List<Double> timeHistory = new ArrayList<>();
    private final List<Integer> agentHistory = new ArrayList<>();
    private long initialMillis = System.currentTimeMillis();
    private double time = 0.0;

    // spawn histogram
    private final List<Double> spawnTimesSec = new ArrayList<>();
    private final List<Double> spawnIntervalsSec = new ArrayList<>();
    private final Object spawnChartLock = new Object();

    //adding type for distribution
    private boolean showPathsDuringSimulation = false;
    private JButton togglePathButton;

    enum DISTRIBUTION_TYPE {
        Uniformed, Normal, Randomed
    }

    // flow colors
    private static final Color[] FLOW_COLORS = new Color[]{
        Color.BLACK, // flow 0: A-B-C
        Color.RED, // flow 1: A-B-E
        Color.ORANGE, // flow 2: D-B-C
        Color.GREEN // flow 3: D-B-E
    };

    // ====================== INNER CLASS ==========================
    class Agent {

        int id;
        double startTime;
        double endTime;
        int flow;
        double spawnProb;

        public Agent(int id, double startTime, double endTime, int flow, double spawnProb) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
            this.flow = flow;
            this.spawnProb = spawnProb;
        }

        public double getDuration() {
            return endTime - startTime;
        }
    }

    // ====================== HELPERS ==========================
    private void updateTrafficState() {
        double currentTime = Simulator.instance.getGlobalTime();
        if (currentTime >= nextSwitchTime) {
            isBusy = !isBusy;
            // Random 4-7 giây tiếp theo (giả sử timeStep = 1 nên 1 unit = 1s)
            nextSwitchTime = currentTime + (4 + rand.nextDouble() * 3);
            System.out.println("Traffic State Changed: " + (isBusy ? "BUSY" : "FREE"));
        }
    }

    public static int chooseByNormal(int[] options, double mean, double std) {
        double gaussian = rand.nextGaussian();
        double value = mean + std * gaussian;

        value = Math.max(0, Math.min(1, value));
        int index = (value < 0.5) ? 0 : 1;

        return options[index];
    }

    private void createEdge(
            int indexA,
            int indexB,
            int indexC,
            java.util.List<algorithms.dijkstra.model.Vertex> nodes,
            java.util.List<algorithms.dijkstra.model.Edge> edges
    ) {
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
        flowOrder.add(new int[]{0, 2}); // A-B-C
        flowOrder.add(new int[]{0, 4}); // A-B-E
        flowOrder.add(new int[]{3, 2}); // D-B-C
        flowOrder.add(new int[]{3, 4}); // D-B-E
        Collections.shuffle(flowOrder, rand);
    }

    private int getFlowId(int src, int dst) {
        if (src == 0 && dst == 2) {
            return 0; // A-B-C

        }
        if (src == 0 && dst == 4) {
            return 1; // A-B-E

        }
        if (src == 3 && dst == 2) {
            return 2; // D-B-C

        }
        if (src == 3 && dst == 4) {
            return 3; // D-B-E

        }
        return 0;
    }

    private String getFlowName(int flow) {
        switch (flow) {
            case 0:
                return "A-B-C";
            case 1:
                return "A-B-E";
            case 2:
                return "D-B-C";
            case 3:
                return "D-B-E";
            default:
                return "Unknown";
        }
    }

    private void recordSpawnEvent(double simTimeSec, double usedIntervalSec) {
        synchronized (spawnChartLock) {
            spawnTimesSec.add(simTimeSec);
            spawnIntervalsSec.add(usedIntervalSec);
        }
    }

    // single point for spawn interval strategy
    private double getNextSpawnInterval() {
        double next;
        switch (distributionType) {
            case Normal:
                disType = "Normal Distribution";
                next = meanSpawnInterval + stdSpawnInterval * rand.nextGaussian();
                // avoid close-to-zero/negative values, clamp within safe range
                next = Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
                break;
            case Randomed:
                // non-uniform random distribution (different than uniform)
                disType = "Random Distribution";
                double r = rand.nextDouble();
                next = randomMinSpawnInterval + (randomMaxSpawnInterval - randomMinSpawnInterval) * Math.sqrt(r);
                break;
            case Uniformed:
            default:
                disType = "Uniform Distribution";
                next = minSpawnInterval + rand.nextDouble() * (maxSpawnInterval - minSpawnInterval);
                // next = meanSpawnInterval; // fixed interval for uniform distribution
                break;
        }
        // global safety clamp
        return Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
    }

    // ====================== SPAWN ==========================
    private void spawnOneAgent(Graph graph, double[][] behavior, Random rand, double gap) {
        int src = sources[rand.nextInt(2)];
        int dst = destinations[rand.nextInt(2)];
        if (src == 0 && dst == 2) {
            leftToTop++;
        }
        if (src == 0 && dst == 4) {
            leftToRight++;
        }
        if (src == 3 && dst == 2) {
            bottomToTop++;
        }
        if (src == 3 && dst == 4) {
            bottomToRight++;
        }

        int k = Simulator.instance.getNumAgents();

        Agent agent = agents.get(k);
        if (agent == null) {
            agent = new Agent(k, 0, 0, 0, 0);
            agents.put(k, agent);
        }
        agent.startTime = System.currentTimeMillis();
        agent.flow = getFlowId(src, dst);

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
        goal.setGlobalGoal(
                translateCoordinateVector(
                        graph.getVertexes().get(dst).getVertex1()
                )
        );

        Vector2D point = translateCoordinateVector(
                graph.getVertexes().get(1).getVertex1()
        );
        goal.add(point);

        goalsList.add(goal);
        spawnIndex++;
    }

    // ====================== SETUP ==========================
    @Override
    protected void setupScenario() {

        startSearchTime = System.currentTimeMillis();
        initialMillis = System.currentTimeMillis();
        startTime = Simulator.instance.getGlobalTime();

        nodePoints.clear();
        timeHistory.clear();
        agentHistory.clear();
        agents.clear();

        synchronized (spawnChartLock) {
            spawnTimesSec.clear();
            spawnIntervalsSec.clear();
        }

        leftToTop = 0;
        leftToRight = 0;
        bottomToTop = 0;
        bottomToRight = 0;

        Simulator.instance.setTimeStep(1);
        Simulator.instance.setAgentDefaults(
                80, 40, 38, 2.5, 3.55, 2.0,
                org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO
        );

        double[][] behavior = createBehaviorMatrix();

        // ========= OBSTACLES CREATION ===============
        obsList1 = new java.util.ArrayList<Obs>();
        int rowsObs = 2;
        int colsObs = 2;

        int decreScale = 1;

        double blockWidth = 450 / decreScale;
        double blockHeight = 375 / decreScale;
        double gap = 50 / decreScale;

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

        // ============= NODE CREATION ===============
        double yMid = startY + blockHeight + gap / 2.0;
        y_C = (int) (yMid);

        double xLeft = startX;
        double xBetween = startX + blockWidth + gap / 2.0;
        x_C = (int) (xBetween);

        double xRight = startX + blockWidth * 2 + gap;
        double yTop = startY;
        double yBottom = yTop + blockHeight * 2 + gap;

        //     E
        // A   B   C
        //     D
        // reality index:
        // 0   1   2
        //     3
        //     4(top)
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft, yMid));    // 0 = A
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yMid)); // 1 = B
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight, yMid));   // 2 = C
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yBottom)); // 3 = D
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yTop));    // 4 = E

        java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
        java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();

        for (int i = 0; i < nodePoints.size(); i++) {
            org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
            algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex(
                    "node_" + i, i, 0, "node_" + i, p, p, 0
            );
            nodes.add(v);
        }

        createEdge(0, 1, 2, nodes, edges);
        createEdge(3, 1, 4, nodes, edges);

        graph = new Graph(nodes, edges);
        System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());

        for (int i = 0; i < totalAgents; i++) {
            Agent agent = new Agent(i, 0, 0, 0, 0);
            agents.put(i, agent);
        }

        spawnOneAgent(graph, behavior, rand, gap);
        spawnedAgents++;

        recordSpawnEvent(Simulator.instance.getGlobalTime(), 0.0); // first spawn has no previous interval
        spawnInterval = getNextSpawnInterval();
        lastSpawnTime = Simulator.instance.getGlobalTime();

        System.out.println("Scenario initialized: " + scenarioName);
    }

    // ====================== PAINT ==========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ===== 1. Draw Gate Status Bars =====
        double barGap = 50.0;
        double barThickness = 10.0;

        int[] exitNodes = {2, 4}; // Node C và Node E
        for (int idx : exitNodes) {
            Vector2D p = nodePoints.get(idx);

            if (isBusy) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(Color.GREEN);
            }

            if (idx == 2) { // Right (C) - vertical gate bar.
                g2.fillRect((int) (p.getX() - 5), (int) (p.getY() - barGap / 2), (int) barThickness, (int) barGap);
            } else if (idx == 4) { // Top (E) - horizontal gate bars.
                g2.fillRect((int) (p.getX() - barGap / 2), (int) (p.getY() - 5), (int) barGap, (int) barThickness);
            }
        }

        // ===== Draw agents with flow colors =====
        if (Simulator.instance != null) {
            int numAgents = Math.min(Simulator.instance.getNumAgents(), goalsList.size());

            for (int agentNo = 0; agentNo < numAgents; agentNo++) {
                Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
                if (agentPos == null) {
                    continue;
                }

                Vector2D agentPosCoord = translateCoordinateBackVector(agentPos);
                int radius = (int) Simulator.instance.getAgentRadius(agentNo);

                if (goalsList.get(agentNo).getFlag() == 1) {
                    Agent agent = agents.get(agentNo);
                    if (agent == null) {
                        continue;
                    }

                    int flow = agent.flow;
                    Color agentColor = FLOW_COLORS[flow % FLOW_COLORS.length];

                    g2.setColor(agentColor);

                    g2.fillOval(
                            (int) agentPosCoord.getX() - 2,
                            (int) agentPosCoord.getY() - 2,
                            4,
                            4
                    );

                    g2.fillOval(
                            (int) agentPosCoord.getX() - radius,
                            (int) agentPosCoord.getY() - radius,
                            radius * 2,
                            radius * 2
                    );
                }

                // Draw the path way during simulation if toggle is on, or after finishing
                if ((ended || showPathsDuringSimulation) && totalSteps < 10000) {
                    int point_size = goalsList.get(agentNo).getaddMovingStepSize();
                    if (point_size > 1) {
                        g2.setColor(goalsList.get(agentNo).getColor());
                        for (int i = 0; i < point_size - 1; i += Math.max(1, point_size / 50)) {
                            Vector2D curr = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(i));
                            Vector2D next = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(Math.min(i + 1, point_size - 1)));
                            g2.drawLine((int) curr.getX(), (int) curr.getY(), (int) next.getX(), (int) next.getY());
                        }
                    }
                }
            }
        }

        // ===== Draw priority circle at intersection B =====
        int radius = 31;
        g2.setColor(Color.GREEN);
        g2.drawOval(
                x_C - radius,
                y_C - radius,
                radius * 2,
                radius * 2
        );

        // ===== Draw information block =====
        int startX = 20;
        int startY = 150;
        int circleSize = 12;
        int spacing = 20;

        g2.setColor(Color.BLACK);
        g2.drawString(disType, startX, startY - 50);
        g2.drawString("Simulation Time(s)", startX, startY - 30);
        g2.drawString("Agent's Color | Count | Time(s)", startX, startY - 10);

        // --- Hiển thị thêm Gate Stats & Status (Thêm mới) ---
        g2.setColor(isBusy ? Color.RED : new Color(0, 150, 0));
        g2.drawString("EXIT STATUS: " + (isBusy ? "BUSY" : "FREE"), startX + 150, startY - 70);

        g2.setColor(new Color(0, 120, 0));
        g2.drawString("Normal Exits: " + throughExitCount, startX + 150, startY - 50);
        g2.setColor(Color.RED);
        g2.drawString("Slow Exits: " + slowExitCount, startX + 150, startY - 35);

        int[] flowCount = {
            leftToTop,
            leftToRight,
            bottomToTop,
            bottomToRight
        };

        int[] srcDstCount = {
            leftToTop + leftToRight, // source A
            bottomToTop + bottomToRight, // source D
            leftToTop + bottomToTop, // destination C
            leftToRight + bottomToRight // destination E
        };

        double[] avgTime = new double[4];
        int[] countFlow = new int[4];

        if (ended) {
            for (int i = 0; i < totalAgents; i++) {
                Agent agent = agents.get(i);
                if (agent != null && agent.endTime > 0) {
                    int f = agent.flow;
                    avgTime[f] += agent.getDuration();
                    countFlow[f]++;
                }
            }

            for (int i = 0; i < 4; i++) {
                if (countFlow[i] > 0) {
                    avgTime[i] /= countFlow[i];
                }
            }

            long endTime = System.currentTimeMillis();
            double simTime = (endTime - startSearchTime) * 0.001;
            g2.drawString(String.format("%.2f", simTime), startX + 110, startY - 30);
        }

        for (int i = 0; i < 4; i++) {
            int y = startY + i * spacing;

            g2.setColor(FLOW_COLORS[i]);
            g2.fillOval(startX, y - circleSize + 9, circleSize, circleSize);

            g2.setColor(Color.BLACK);

            int col1X = startX + 20;
            int col2X = startX + 100;
            int col3X = startX + 140;

            g2.drawString(getFlowName(i), col1X, y + 9);
            g2.drawString(String.valueOf(flowCount[i]), col2X, y + 9);

            if (ended && countFlow[i] > 0) {
                g2.drawString(String.format("%.2f", avgTime[i] * 0.001), col3X, y + 9);
            } else {
                g2.drawString("-", col3X, y + 9);
            }
        }

        // ===== Source / Destination count =====
        int startY2 = startY + 4 * spacing + 30;
        int colLeftX = startX;
        int colRightX = startX + 90;

        g2.setColor(Color.BLACK);
        g2.drawString("Source Count", colLeftX, startY2 - 20);
        g2.drawString("Destination Count", colRightX, startY2 - 20);

        String[] sourceLabels = {"A", "D"};
        String[] destLabels = {"C", "E"};

        for (int i = 0; i < 2; i++) {
            int y = startY2 + i * spacing;
            g2.drawString(sourceLabels[i] + ": " + srcDstCount[i], colLeftX, y);
            g2.drawString(destLabels[i] + ": " + srcDstCount[i + 2], colRightX, y);
        }

        // ===== Realtime graph =====
        int graphX = startX + 15;
        int graphY = startY2 + 2 * spacing + 10;
        int graphWidth = 150;
        int graphHeight = 80;

        double maxTimeRange = 80.0;
        int maxAgentRange = totalAgents;

        g2.setColor(Color.BLACK);
        g2.drawRect(graphX, graphY, graphWidth, graphHeight);

        g2.drawString("Agents", graphX, graphY - 5);
        g2.drawString("0", graphX - 10, graphY + graphHeight);
        g2.drawString(String.valueOf(maxAgentRange), graphX - 25, graphY + 10);
        g2.drawString("0", graphX, graphY + graphHeight + 15);
        g2.drawString(String.valueOf((int) maxTimeRange), graphX + graphWidth - 20, graphY + graphHeight + 15);
        g2.drawString("Time(s)", graphX + graphWidth + 5, graphY + graphHeight + 7);

        if (timeHistory.size() > 1) {
            g2.setColor(Color.BLUE);

            for (int i = 1; i < timeHistory.size(); i++) {
                double t1 = Math.min(timeHistory.get(i - 1), maxTimeRange);
                double t2 = Math.min(timeHistory.get(i), maxTimeRange);

                int a1 = Math.min(agentHistory.get(i - 1), maxAgentRange);
                int a2 = Math.min(agentHistory.get(i), maxAgentRange);

                int x1 = graphX + (int) (t1 / maxTimeRange * graphWidth);
                int x2 = graphX + (int) (t2 / maxTimeRange * graphWidth);

                int y1 = graphY + graphHeight - (int) ((double) a1 / maxAgentRange * graphHeight);
                int y2 = graphY + graphHeight - (int) ((double) a2 / maxAgentRange * graphHeight);

                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // ===== Draw node labels =====
        if (nodePoints != null) {
            Graphics2D gText = (Graphics2D) g2.create();
            gText.setFont(new Font("Lexend", Font.BOLD, 24));
            gText.setColor(Color.BLACK);

            for (int i = 0; i < nodePoints.size(); i++) {
                Vector2D screenPos = nodePoints.get(i);
                char label = (char) ('A' + i);
                String text = String.valueOf(label);

                FontMetrics fm = gText.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();

                int x = (int) screenPos.getX() - textWidth / 2;
                int y = (int) screenPos.getY() + textHeight / 2;

                gText.drawString(text, x, y);
            }

            gText.dispose();
        }

        // ===== Spawn histogram =====
        int chartWidth = 210;
        int chartHeight = 320;
        int chartX = 10;
        int chartY = 425;

        drawSpawnHistogram(g2, chartX, chartY, chartWidth, chartHeight);
    }

    // ====================== HISTOGRAM ==========================
    private void drawSpawnHistogram(Graphics2D g2, int x, int y, int width, int height) {
        List<Double> intervals;

        synchronized (spawnChartLock) {
            intervals = new ArrayList<>(spawnIntervalsSec);
        }

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, width, height, 18, 18);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, width, height, 18, 18);

        g.setFont(new Font("Arial", Font.BOLD, 13));
        String title = "Spawn Interval Histogram";
        FontMetrics titleFm = g.getFontMetrics();
        int titleX = x + (width - titleFm.stringWidth(title)) / 2;
        g.drawString(title, titleX, y + 38);

        List<Double> values = new ArrayList<>();
        for (double v : intervals) {
            if (v > 0.0) {
                values.add(v);
            }
        }

        if (values.size() < 2) {
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("In Process", x + 20, y + 70);
            g.dispose();
            return;
        }

        int leftPad = 44;
        int rightPad = 20;
        int topPad = 58;
        int bottomPad = 72;

        int px = x + leftPad;
        int py = y + topPad;
        int pw = width - leftPad - rightPad;
        int ph = height - topPad - bottomPad;

        g.setColor(new Color(235, 235, 242));
        g.fillRect(px, py, pw, ph);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2.2f));
        g.drawRect(px, py, pw, ph);

        // ===== histogram bins =====
        int binCount = 8;

        double minVal = Double.MAX_VALUE;
        double maxVal = -Double.MAX_VALUE;
        for (double v : values) {
            if (v < minVal) {
                minVal = v;
            }
            if (v > maxVal) {
                maxVal = v;
            }
        }

        if (Math.abs(maxVal - minVal) < 1e-9) {
            maxVal = minVal + 1.0;
        }

        double binSize = (maxVal - minVal) / binCount;
        int[] counts = new int[binCount];

        for (double v : values) {
            int bin = (int) ((v - minVal) / binSize);
            if (bin >= binCount) {
                bin = binCount - 1;
            }
            if (bin < 0) {
                bin = 0;
            }
            counts[bin]++;
        }

        int maxCount = 1;
        for (int c : counts) {
            if (c > maxCount) {
                maxCount = c;
            }
        }

        Stroke oldStroke = g.getStroke();
        Stroke dashed = new BasicStroke(
                1.2f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0,
                new float[]{8f, 8f},
                0
        );
        g.setStroke(dashed);
        g.setColor(Color.DARK_GRAY);

        int yMidPix = py + ph / 2;
        g.drawLine(px, yMidPix, px + pw, yMidPix);

        g.setStroke(oldStroke);
        g.setColor(Color.BLACK);

        int gap = 4;
        int barWidth = Math.max(6, (pw - gap * (binCount + 1)) / binCount);

        for (int i = 0; i < binCount; i++) {
            int barHeight = (int) Math.round((counts[i] / (double) maxCount) * (ph - 10));
            int bx = px + gap + i * (barWidth + gap);
            int by = py + ph - barHeight;

            g.setColor(new Color(100, 149, 237));
            g.fillRect(bx, by, barWidth, barHeight);

            g.setColor(Color.BLACK);
            g.drawRect(bx, by, barWidth, barHeight);
        }

        // ===== y labels (count) =====
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString(String.valueOf(maxCount), x + 10, py + 5);
        g.drawString(String.valueOf(maxCount / 2), x + 10, yMidPix + 5);
        g.drawString("0", x + 18, py + ph + 5);

        // ===== x labels (interval range) =====
        String minLabel = String.format("%.0f", minVal);
        String maxLabel = String.format("%.0f", maxVal);
        g.drawString(minLabel, px - 5, y + height - 40);
        g.drawString(maxLabel, px + pw - 20, y + height - 40);
        g.drawString("seconds", px + pw / 2 - 20, y + height - 20);

        // ===== summary =====
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        double mean = sum / values.size();

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        String status = String.format("n=%d, mean=%.1fs", values.size(), mean);
        g.drawString(status, x + 20, y + height - 3);

        g.dispose();
    }

    // ====================== UPDATE VISUALIZATION ==========================
    @Override
    protected void updateVisualization() {
        if ((Simulator.instance.getGlobalTime() % 100) == 0) {
            System.out.println("Step: " + Simulator.instance.getGlobalTime()
                    + " -> Completed: " + agentCount + "/" + totalAgents);
        }

        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);

            if (goalsList.get(agentNo).getFlag() == 1) {
                goalsList.get(agentNo).addMovingStep(agentPos);

                if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) < 10.0) {
                    Agent agent = agents.get(agentNo);
                    if (isBusy) {
                        slowExitCount++;
                    } else {
                        throughExitCount++;
                    }

                    if (agent != null) {
                        agent.endTime = System.currentTimeMillis();
                    }

                    agentCount++;
                    long elapsed = System.currentTimeMillis() - startSearchTime;
                    goalsList.get(agentNo).setReachGoalms(elapsed);
                    goalsList.get(agentNo).setFlag(2);
                    sumTime += elapsed;

                    Simulator.instance.setAgentMaxSpeed(agentNo, 0.0);
                    Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);

                    Simulator.instance.setAgentRadius(agentNo, 0.0);
                    Simulator.instance.setAgentNeighborDistance(agentNo, 0.0);
                    Simulator.instance.setAgentMaxNeighbors(agentNo, 0);

                    Simulator.instance.setAgentPosition(agentNo, new Vector2D(-1e9, -1e9));
                }
            }
        }

        int currentAgents = spawnedAgents - agentCount;
        timeHistory.add(time);
        agentHistory.add(currentAgents);

        if (timeHistory.size() > 5000) {
            timeHistory.remove(0);
            agentHistory.remove(0);
        }

        repaint();
    }

    // ====================== PREFERRED VELOCITIES ==========================
    @Override
    // protected void setPreferredVelocities() {
    //     super.setPreferredVelocities();

    //     double[][] behavior = createBehaviorMatrix();
    //     final double forwardDotThreshold = 0.85;
    //     double currentTime = Simulator.instance.getGlobalTime();
    //     int gap = 50;
    //     time = (System.currentTimeMillis() - initialMillis) * 0.001;
    //     if (spawnedAgents < totalAgents
    //             && currentTime - lastSpawnTime >= spawnInterval) {
    //         double usedInterval = spawnInterval;
    //         spawnOneAgent(graph, behavior, rand, gap);
    //         spawnedAgents++;
    //         lastSpawnTime = currentTime;
    //         recordSpawnEvent(currentTime, usedInterval);
    //         spawnInterval = getNextSpawnInterval();
    //     }
    //     int numAgents = Simulator.instance.getNumAgents();
    //     for (int agentNo = 0; agentNo < numAgents; agentNo++) {
    //         Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
    //         if (pref == null) {
    //             continue;
    //         }
    //         double prefNorm = pref.getNorm();
    //         if (prefNorm < 1e-6) {
    //             continue;
    //         }
    //         Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
    //         Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
    //         double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
    //         double checkDistance = neighborDistance;
    //         double minScale = 1.0;
    //         for (int other = 0; other < numAgents; other++) {
    //             if (other == agentNo) {
    //                 continue;
    //             }
    //             Vector2D otherPos = Simulator.instance.getAgentPosition(other);
    //             if (otherPos == null) {
    //                 continue;
    //             }
    //             Vector2D rel = otherPos.subtract(agentPos);
    //             double dist = rel.getNorm();
    //             if (dist < 1e-6 || dist > checkDistance) {
    //                 continue;
    //             }
    //             double forwardness = prefDir.dotProduct(rel.scalarMultiply(1.0 / dist));
    //             if (forwardness < forwardDotThreshold) {
    //                 continue;
    //             }
    //             double safety = Simulator.instance.getAgentRadius(agentNo)
    //                     + Simulator.instance.getAgentRadius(other) + 1.0;
    //             double desiredSpacing = Math.max(checkDistance, safety + 1.0);
    //             double scale = (dist - safety) / (desiredSpacing - safety);
    //             scale = Math.max(0.0, Math.min(1.0, scale));
    //             if (scale < minScale) {
    //                 minScale = scale;
    //             }
    //         }
    //         if (minScale < 1.0) {
    //             Vector2D newPref = pref.scalarMultiply(minScale);
    //             Simulator.instance.setAgentPreferredVelocity(agentNo, newPref);
    //         }
    //     }
    // }
    protected void setPreferredVelocities() {
        super.setPreferredVelocities();
        time = (System.currentTimeMillis() - initialMillis) * 0.001;

        double currentTime = Simulator.instance.getGlobalTime();

        if (time >= nextSwitchTime) {
            isBusy = !isBusy;
            nextSwitchTime = time + 7.0;
        }

        if (spawnedAgents < totalAgents && currentTime - lastSpawnTime >= spawnInterval) {
            double usedInterval = spawnInterval;
            spawnOneAgent(graph, createBehaviorMatrix(), rand, 50);
            spawnedAgents++;
            lastSpawnTime = currentTime;
            recordSpawnEvent(currentTime, usedInterval);
            spawnInterval = getNextSpawnInterval();
        }

        int numAgents = Simulator.instance.getNumAgents();
        for (int agentNo = 0; agentNo < numAgents; agentNo++) {
            Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
            if (pref == null || pref.getNorm() < 1e-6) {
                continue;
            }

            Goal goal = goalsList.get(agentNo);
            if (goal == null || goal.getFlag() != 1) {
                continue;
            }

            // --- BƯỚC QUAN TRỌNG: KIỂM TRA NEXT NODE ---
            // Chỉ khi getNext > 0 (đã tìm được đường đi tiếp theo) mới xử lý logic Busy
            if (goal.getNext() > 0) {

                // Kiểm tra xem đích cuối cùng có phải là Node 2 hoặc 4 không
                if (isBusy && (goal.getDestination() == 2 || goal.getDestination() == 4)) {

                    Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
                    Vector2D targetPos = goal.getGlobalGoal(); // Đích cuối cùng
                    double distToGoal = targetPos.subtract(agentPos).getNorm();

                    // Nếu nằm trong vùng ảnh hưởng, tiến hành giảm tốc
                    if (distToGoal < DETECTION_RADIUS) {
                        double speedFactor = distToGoal / DETECTION_RADIUS;
                        speedFactor = Math.max(MIN_SPEED_FACTOR, speedFactor);

                        // Giảm vận tốc pref dựa trên khoảng cách tới đích cuối
                        pref = pref.scalarMultiply(speedFactor);
                    }
                }
            }

            // --- LOGIC NÉ TRÁNH VÀ SCALE VẬN TỐC ---
            double prefNorm = pref.getNorm();
            Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);

            double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
            double minScale = 1.0;

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
                if (dist < 1e-6 || dist > neighborDistance) {
                    continue;
                }

                double forwardness = prefDir.dotProduct(rel.scalarMultiply(1.0 / dist));
                if (forwardness < 0.85) {
                    continue;
                }

                double safety = Simulator.instance.getAgentRadius(agentNo) + Simulator.instance.getAgentRadius(other) + 1.0;
                double desiredSpacing = Math.max(neighborDistance, safety + 1.0);

                double scale = (dist - safety) / (desiredSpacing - safety);
                scale = Math.max(0.0, Math.min(1.0, scale));

                if (scale < minScale) {
                    minScale = scale;
                }
            }

            // Thiết lập vận tốc cuối cùng sau khi đã áp dụng Busy (nếu có) và Né tránh
            Vector2D finalPref = pref.scalarMultiply(minScale);
            Simulator.instance.setAgentPreferredVelocity(agentNo, finalPref);
        }
    }

    public static void main(String[] args) {
        TestCase07_setting1 testCase = new TestCase07_setting1();
        testCase.runSimulation();
    }

    @Override
    protected void runSimulation() {
        setupScenario();

        JFrame frame = new JFrame(scenarioName);
        frame.setLayout(new BorderLayout());
        frame.setSize(w, h);

        // Add toggle button
        togglePathButton = new JButton("Show Paths: OFF");
        togglePathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPathsDuringSimulation = !showPathsDuringSimulation;
                togglePathButton.setText("Show Paths: " + (showPathsDuringSimulation ? "ON" : "OFF"));
            }
        });
        frame.add(togglePathButton, BorderLayout.NORTH);

        frame.add(this, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final int TARGET_FPS = 60;
        final long FRAME_TIME = 1000 / TARGET_FPS;

        long lastFrameTime = System.currentTimeMillis();
        do {
            long frameStart = System.currentTimeMillis();

            updateVisualization();
            setPreferredVelocities();
            updateSubGoal();
            Simulator.instance.doStep();

            totalSteps++;

            long frameElapsed = System.currentTimeMillis() - frameStart;
            if (frameElapsed < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - frameElapsed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            lastFrameTime = System.currentTimeMillis();
            if (agentCount == totalAgents) {
                break;
            }
        } while (!reachedGoal());

        ended = true;
        updateVisualization();
        printResults();
    }
}
// package rvo2example;

// import java.util.List;
// import java.util.LinkedList;
// import java.awt.Graphics;
// import java.awt.Graphics2D;
// import java.awt.Font;
// import java.awt.FontMetrics;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Random;
// import java.util.HashMap;
// import java.util.Map;
// import java.awt.Color;
// import java.awt.BasicStroke;
// import java.awt.RenderingHints;
// import java.awt.Stroke;
// import java.awt.BorderLayout;
// import javax.swing.JButton;
// import java.awt.event.ActionListener;
// import java.awt.event.ActionEvent;
// import javax.swing.JFrame;
// import javax.swing.WindowConstants;

// import algorithms.dijkstra.engine.DijkstraAlgorithm;
// import algorithms.dijkstra.model.Graph;
// import algorithms.dijkstra.model.Vertex;
// import rvo2.Simulator;
// import rvo2.Goal;
// import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
// import org.apache.commons.math3.util.FastMath;

// /**
//  * TC7: One intersection - 4 flows.
//  *
//  * Layout: A and D are sources, C and E are exits, and B is the only
//  * intersection sub-goal. The class also records flow statistics, active-agent
//  * count over time, spawn-interval histogram data, and optional path traces.
//  */
// public class TestCase07_setting1 extends BaseTrafficScenario_S1 {

//     private static final long serialVersionUID = 1L;

//     // ====================== PARAMETERS ==========================
//     public TestCase07_setting1() {
//         scenarioName = "TC7: One Intersection - 4 Flows";
//         trafficType = "1-intersection-4-flows";
//         totalAgents = 25;
//     }

//     // Counts how many agents have been spawned. It is also useful when rotating shuffled flows.
//     private int spawnIndex = 0;

//     // Temporary source/destination sequence used by the uniform flow scheduler.
//     private List<int[]> flowOrder = new ArrayList<>();

//     // Dynamic exit state. BUSY means agents slow down when approaching C/E.
//     private boolean isBusy = false;

//     // Use real wall-clock time for BUSY/FREE switching.
//     // Do NOT use Simulator.instance.getGlobalTime() here because the simulation
//     // advances once per frame, so 4-7 simulation steps can pass in less than
//     // one real second when the UI runs at 60 FPS.
//     private long nextSwitchWallTimeMs = 0L;
//     private static final long MIN_STATUS_DURATION_MS = 4000L;
//     private static final long MAX_STATUS_DURATION_MS = 7000L;

//     // Radius around an exit where BUSY-state slow down is applied.
//     private static final double DETECTION_RADIUS = 60.0;

//     // Minimum speed multiplier during BUSY state; prevents agents from freezing completely.
//     private static final double MIN_SPEED_FACTOR = 0.15;

//     // Exit counters shown on the simulation dashboard.
//     private int throughExitCount = 0;
//     private int slowExitCount = 0;

//     int leftToTop = 0;      // thực chất: A-B-C (0 -> 2)
//     int leftToRight = 0;    // thực chất: A-B-E (0 -> 4)
//     int bottomToTop = 0;    // thực chất: D-B-C (3 -> 2)
//     int bottomToRight = 0;  // thực chất: D-B-E (3 -> 4)

//     double startTime = 0;
//     double endTime = 0;
//     static Random rand = new Random();

//     String disType = "Normal Distribution";

//     int[] sources = {0, 3};
//     int[] destinations = {2, 4};

//     int spawnedAgents = 0;
//     double spawnInterval = 60;
//     double lastSpawnTime = 0;
//     private double minSpawnInterval = 15.0;
//     private double maxSpawnInterval = 120.0;
//     // spawn interval settings
//     private DISTRIBUTION_TYPE distributionType = DISTRIBUTION_TYPE.Uniformed;
//     private double meanSpawnInterval = 60.0;
//     private double stdSpawnInterval = 15.0;
//     private double randomMinSpawnInterval = 20.0;
//     private double randomMaxSpawnInterval = 100.0;

//     private Graph graph;

//     // Coordinate of the priority circle center
//     int x_C = 0;
//     int y_C = 0;

//     // node points for drawing labels A,B,C,D,E
//     private List<Vector2D> nodePoints = new ArrayList<>();

//     // agent metadata
//     Map<Integer, Agent> agents = new HashMap<>();

//     // realtime graph
//     private final List<Double> timeHistory = new ArrayList<>();
//     private final List<Integer> agentHistory = new ArrayList<>();
//     private long initialMillis = System.currentTimeMillis();
//     private double time = 0.0;

//     // spawn histogram
//     private final List<Double> spawnTimesSec = new ArrayList<>();
//     private final List<Double> spawnIntervalsSec = new ArrayList<>();
//     private final Object spawnChartLock = new Object();

//     //adding type for distribution
//     private boolean showPathsDuringSimulation = false;
//     private JButton togglePathButton;

//     enum DISTRIBUTION_TYPE {
//         Uniformed, Normal, Randomed
//     }

//     // flow colors
//     private static final Color[] FLOW_COLORS = new Color[]{
//         Color.BLACK, // flow 0: A-B-C
//         Color.RED, // flow 1: A-B-E
//         Color.ORANGE, // flow 2: D-B-C
//         Color.GREEN // flow 3: D-B-E
//     };

//     // ====================== INNER CLASS ==========================
//     class Agent {

//         int id;
//         double startTime;
//         double endTime;
//         int flow;
//         double spawnProb;

//         public Agent(int id, double startTime, double endTime, int flow, double spawnProb) {
//             this.id = id;
//             this.startTime = startTime;
//             this.endTime = endTime;
//             this.flow = flow;
//             this.spawnProb = spawnProb;
//         }

//         public double getDuration() {
//             return endTime - startTime;
//         }
//     }

//     // ====================== HELPERS ==========================
//     /**
//      * Toggle the exit state between BUSY and FREE using real wall-clock time.
//      *
//      * This prevents BUSY/FREE from flashing too quickly. The simulator global
//      * time advances every doStep(), and runSimulation() calls doStep() near 60
//      * times per real second. Therefore, a 4-7 simulation-step interval would
//      * only last around 0.07-0.12 real seconds.
//      */
//     private void updateTrafficState() {
//         long currentWallTimeMs = System.currentTimeMillis();

//         // First call: schedule the first status change but keep the initial state.
//         if (nextSwitchWallTimeMs == 0L) {
//             long durationMs = MIN_STATUS_DURATION_MS
//                     + (long) (rand.nextDouble() * (MAX_STATUS_DURATION_MS - MIN_STATUS_DURATION_MS));
//             nextSwitchWallTimeMs = currentWallTimeMs + durationMs;
//             return;
//         }

//         if (currentWallTimeMs >= nextSwitchWallTimeMs) {
//             isBusy = !isBusy;

//             long durationMs = MIN_STATUS_DURATION_MS
//                     + (long) (rand.nextDouble() * (MAX_STATUS_DURATION_MS - MIN_STATUS_DURATION_MS));
//             nextSwitchWallTimeMs = currentWallTimeMs + durationMs;

//             System.out.println("Traffic State Changed: " + (isBusy ? "BUSY" : "FREE"));
//         }
//     }

//     public static int chooseByNormal(int[] options, double mean, double std) {
//         double gaussian = rand.nextGaussian();
//         double value = mean + std * gaussian;

//         value = Math.max(0, Math.min(1, value));
//         int index = (value < 0.5) ? 0 : 1;

//         return options[index];
//     }

//     private void createEdge(
//             int indexA,
//             int indexB,
//             int indexC,
//             java.util.List<algorithms.dijkstra.model.Vertex> nodes,
//             java.util.List<algorithms.dijkstra.model.Edge> edges
//     ) {
//         edges.add(new algorithms.dijkstra.model.Edge(
//                 "E_" + indexA + "_" + indexB,
//                 nodes.get(indexA),
//                 nodes.get(indexB),
//                 nodes.get(indexA).getVertex1().distance(nodes.get(indexB).getVertex1())
//         ));
//         edges.add(new algorithms.dijkstra.model.Edge(
//                 "E_" + indexB + "_" + indexC,
//                 nodes.get(indexB),
//                 nodes.get(indexC),
//                 nodes.get(indexB).getVertex1().distance(nodes.get(indexC).getVertex1())
//         ));
//     }

//     private void shuffleFlows(Random rand) {
//         flowOrder.clear();
//         flowOrder.add(new int[]{0, 2}); // A-B-C
//         flowOrder.add(new int[]{0, 4}); // A-B-E
//         flowOrder.add(new int[]{3, 2}); // D-B-C
//         flowOrder.add(new int[]{3, 4}); // D-B-E
//         Collections.shuffle(flowOrder, rand);
//     }

//     /**
//      * Convert source/destination node pair to a flow id used by color and statistics.
//      */
//     private int getFlowId(int src, int dst) {
//         if (src == 0 && dst == 2) {
//             return 0; // A-B-C

//         }
//         if (src == 0 && dst == 4) {
//             return 1; // A-B-E

//         }
//         if (src == 3 && dst == 2) {
//             return 2; // D-B-C

//         }
//         if (src == 3 && dst == 4) {
//             return 3; // D-B-E

//         }
//         return 0;
//     }

//     /**
//      * Convert internal flow id to a readable route label for the dashboard.
//      */
//     private String getFlowName(int flow) {
//         switch (flow) {
//             case 0:
//                 return "A-B-C";
//             case 1:
//                 return "A-B-E";
//             case 2:
//                 return "D-B-C";
//             case 3:
//                 return "D-B-E";
//             default:
//                 return "Unknown";
//         }
//     }

//     private void recordSpawnEvent(double simTimeSec, double usedIntervalSec) {
//         synchronized (spawnChartLock) {
//             spawnTimesSec.add(simTimeSec);
//             spawnIntervalsSec.add(usedIntervalSec);
//         }
//     }

//     // single point for spawn interval strategy
//     private double getNextSpawnInterval() {
//         double next;
//         switch (distributionType) {
//             case Normal:
//                 disType = "Normal Distribution";
//                 next = meanSpawnInterval + stdSpawnInterval * rand.nextGaussian();
//                 // avoid close-to-zero/negative values, clamp within safe range
//                 next = Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
//                 break;
//             case Randomed:
//                 // non-uniform random distribution (different than uniform)
//                 disType = "Random Distribution";
//                 double r = rand.nextDouble();
//                 next = randomMinSpawnInterval + (randomMaxSpawnInterval - randomMinSpawnInterval) * Math.sqrt(r);
//                 break;
//             case Uniformed:
//             default:
//                 disType = "Uniform Distribution";
//                 next = minSpawnInterval + rand.nextDouble() * (maxSpawnInterval - minSpawnInterval);
//                 // next = meanSpawnInterval; // fixed interval for uniform distribution
//                 break;
//         }
//         // global safety clamp
//         return Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
//     }

//     // ====================== SPAWN ==========================
//     private void spawnOneAgent(Graph graph, double[][] behavior, Random rand, double gap) {
//         int src = sources[rand.nextInt(2)];
//         int dst = destinations[rand.nextInt(2)];
//         if (src == 0 && dst == 2) {
//             leftToTop++;
//         }
//         if (src == 0 && dst == 4) {
//             leftToRight++;
//         }
//         if (src == 3 && dst == 2) {
//             bottomToTop++;
//         }
//         if (src == 3 && dst == 4) {
//             bottomToRight++;
//         }

//         int k = Simulator.instance.getNumAgents();

//         Agent agent = agents.get(k);
//         if (agent == null) {
//             agent = new Agent(k, 0, 0, 0, 0);
//             agents.put(k, agent);
//         }
//         agent.startTime = System.currentTimeMillis();
//         agent.flow = getFlowId(src, dst);

//         int behaviorIndex = rand.nextInt(7);

//         Simulator.instance.addAgent(
//                 translateCoordinateVector(
//                         graph.getVertexes().get(src).getVertex1()
//                 )
//         );

//         Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
//         Simulator.instance.setAgentMaxNeighbors(k, (int) behavior[behaviorIndex][1]);
//         Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);

//         double radius = gap * 0.21;
//         Simulator.instance.setAgentRadius(k, radius);
//         Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);

//         Goal goal = new Goal(
//                 translateCoordinateVector(
//                         graph.getVertexes().get(dst).getVertex1()
//                 )
//         );

//         goal.setColor(getColor());
//         goal.setFlag(1);
//         goal.setSource(src);
//         goal.setDestination(dst);
//         goal.setGlobalGoal(
//                 translateCoordinateVector(
//                         graph.getVertexes().get(dst).getVertex1()
//                 )
//         );

//         // TC7 chỉ có 1 intersection ở node 1 = B
//         Vector2D point = translateCoordinateVector(
//                 graph.getVertexes().get(1).getVertex1()
//         );
//         goal.add(point);

//         goalsList.add(goal);
//         spawnIndex++;
//     }

//     // ====================== SETUP ==========================
//     @Override
//     protected void setupScenario() {

//         startSearchTime = System.currentTimeMillis();
//         initialMillis = System.currentTimeMillis();
//         startTime = Simulator.instance.getGlobalTime();

//         // Reset gate status and exit counters for a fresh run.
//         isBusy = false;
//         nextSwitchWallTimeMs = 0L;
//         throughExitCount = 0;
//         slowExitCount = 0;

//         nodePoints.clear();
//         timeHistory.clear();
//         agentHistory.clear();
//         agents.clear();

//         synchronized (spawnChartLock) {
//             spawnTimesSec.clear();
//             spawnIntervalsSec.clear();
//         }

//         leftToTop = 0;
//         leftToRight = 0;
//         bottomToTop = 0;
//         bottomToRight = 0;

//         Simulator.instance.setTimeStep(0.5);
//         Simulator.instance.setAgentDefaults(
//                 80, 40, 38, 2.5, 3.55, 2.0,
//                 org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO
//         );

//         double[][] behavior = createBehaviorMatrix();

//         // ========= OBSTACLES CREATION ===============
//         obsList1 = new java.util.ArrayList<Obs>();
//         int rowsObs = 2;
//         int colsObs = 2;

//         int decreScale = 1;

//         double blockWidth = 450 / decreScale;
//         double blockHeight = 375 / decreScale;
//         double gap = 50 / decreScale;

//         double totalWidth = colsObs * blockWidth + (colsObs - 1) * gap;
//         double totalHeight = rowsObs * blockHeight + (rowsObs - 1) * gap;

//         double startX = (w - totalWidth) / 2.0;
//         double startY = (h - totalHeight) / 2.0;

//         for (int r = 0; r < rowsObs; r++) {
//             for (int c = 0; c < colsObs; c++) {
//                 double x = startX + c * (blockWidth + gap);
//                 double y = startY + r * (blockHeight + gap);

//                 java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> coord1 = new java.util.ArrayList<>();
//                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y));
//                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y));
//                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y + blockHeight));
//                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y + blockHeight));

//                 java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> coord2 = new java.util.ArrayList<>();
//                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y)));
//                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y + blockHeight)));
//                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y + blockHeight)));
//                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y)));

//                 Obs block = new Obs(coord1, coord2);
//                 obsList1.add(block);
//                 Simulator.instance.addObstacle(block.coord2);
//             }
//         }
//         Simulator.instance.processObstacles();

//         // ============= NODE CREATION ===============
//         double yMid = startY + blockHeight + gap / 2.0;
//         y_C = (int) (yMid);

//         double xLeft = startX;
//         double xBetween = startX + blockWidth + gap / 2.0;
//         x_C = (int) (xBetween);

//         double xRight = startX + blockWidth * 2 + gap;
//         double yTop = startY;
//         double yBottom = yTop + blockHeight * 2 + gap;

//         //     E
//         // A   B   C
//         //     D
//         // thực tế index:
//         // 0   1   2
//         //     3
//         //     4(top)
//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft, yMid));    // 0 = A
//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yMid)); // 1 = B
//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight, yMid));   // 2 = C
//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yBottom)); // 3 = D
//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yTop));    // 4 = E

//         java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
//         java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();

//         for (int i = 0; i < nodePoints.size(); i++) {
//             org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
//             algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex(
//                     "node_" + i, i, 0, "node_" + i, p, p, 0
//             );
//             nodes.add(v);
//         }

//         createEdge(0, 1, 2, nodes, edges);
//         createEdge(3, 1, 4, nodes, edges);

//         graph = new Graph(nodes, edges);
//         System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());

//         for (int i = 0; i < totalAgents; i++) {
//             Agent agent = new Agent(i, 0, 0, 0, 0);
//             agents.put(i, agent);
//         }

//         spawnOneAgent(graph, behavior, rand, gap);
//         spawnedAgents++;

//         recordSpawnEvent(Simulator.instance.getGlobalTime(), 0.0); // first spawn has no previous interval
//         spawnInterval = getNextSpawnInterval();
//         lastSpawnTime = Simulator.instance.getGlobalTime();

//         System.out.println("Scenario initialized: " + scenarioName);
//     }

//     // ====================== PAINT ==========================
//     @Override
//     protected void paintComponent(Graphics g) {
//         super.paintComponent(g);

//         Graphics2D g2 = (Graphics2D) g;

//         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//         // ===== 1. Draw Gate Status Bars (Thêm mới) =====
//         double barGap = 50.0;
//         double barThickness = 10.0;

//         int[] exitNodes = {2, 4}; // Node C và Node E
//         for (int idx : exitNodes) {
//             Vector2D p = nodePoints.get(idx);

//             if (isBusy) {
//                 g2.setColor(Color.RED);
//             } else {
//                 g2.setColor(Color.GREEN);
//             }

//             if (idx == 2) { // Cổng Right (C) - Vẽ thanh dọc
//                 g2.fillRect((int) (p.getX() - 5), (int) (p.getY() - barGap / 2), (int) barThickness, (int) barGap);
//             } else if (idx == 4) { // Cổng Top (E) - Vẽ thanh ngang
//                 g2.fillRect((int) (p.getX() - barGap / 2), (int) (p.getY() - 5), (int) barGap, (int) barThickness);
//             }
//         }

//         // ===== Draw agents with flow colors =====
//         if (Simulator.instance != null) {
//             int numAgents = Math.min(Simulator.instance.getNumAgents(), goalsList.size());

//             for (int agentNo = 0; agentNo < numAgents; agentNo++) {
//                 Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//                 if (agentPos == null) {
//                     continue;
//                 }

//                 Vector2D agentPosCoord = translateCoordinateBackVector(agentPos);
//                 int radius = (int) Simulator.instance.getAgentRadius(agentNo);

//                 if (goalsList.get(agentNo).getFlag() == 1) {
//                     Agent agent = agents.get(agentNo);
//                     if (agent == null) {
//                         continue;
//                     }

//                     int flow = agent.flow;
//                     Color agentColor = FLOW_COLORS[flow % FLOW_COLORS.length];

//                     g2.setColor(agentColor);

//                     g2.fillOval(
//                             (int) agentPosCoord.getX() - 2,
//                             (int) agentPosCoord.getY() - 2,
//                             4,
//                             4
//                     );

//                     g2.fillOval(
//                             (int) agentPosCoord.getX() - radius,
//                             (int) agentPosCoord.getY() - radius,
//                             radius * 2,
//                             radius * 2
//                     );
//                 }

//                 // Draw the path way during simulation if toggle is on, or after finishing
//                 if ((ended || showPathsDuringSimulation) && totalSteps < 10000) {
//                     int point_size = goalsList.get(agentNo).getaddMovingStepSize();
//                     if (point_size > 1) {
//                         g2.setColor(goalsList.get(agentNo).getColor());
//                         for (int i = 0; i < point_size - 1; i += Math.max(1, point_size / 50)) {
//                             Vector2D curr = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(i));
//                             Vector2D next = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(Math.min(i + 1, point_size - 1)));
//                             g2.drawLine((int) curr.getX(), (int) curr.getY(), (int) next.getX(), (int) next.getY());
//                         }
//                     }
//                 }
//             }
//         }

//         // ===== Draw priority circle at intersection B =====
//         int radius = 31;
//         g2.setColor(Color.GREEN);
//         g2.drawOval(
//                 x_C - radius,
//                 y_C - radius,
//                 radius * 2,
//                 radius * 2
//         );

//         // ===== Draw information block =====
//         int startX = 20;
//         int startY = 150;
//         int circleSize = 12;
//         int spacing = 20;

//         g2.setColor(Color.BLACK);
//         g2.drawString(disType, startX, startY - 50);
//         g2.drawString("Simulation Time(s)", startX, startY - 30);
//         g2.drawString("Agent's Color | Count | Time(s)", startX, startY - 10);

//         // --- Hiển thị thêm Gate Stats & Status (Thêm mới) ---
//         g2.setColor(isBusy ? Color.RED : new Color(0, 150, 0));
//         g2.drawString("EXIT STATUS: " + (isBusy ? "BUSY" : "FREE"), startX + 150, startY - 70);

//         g2.setColor(new Color(0, 120, 0));
//         g2.drawString("Normal Exits: " + throughExitCount, startX + 150, startY - 50);
//         g2.setColor(Color.RED);
//         g2.drawString("Slow Exits: " + slowExitCount, startX + 150, startY - 35);

//         int[] flowCount = {
//             leftToTop,
//             leftToRight,
//             bottomToTop,
//             bottomToRight
//         };

//         int[] srcDstCount = {
//             leftToTop + leftToRight, // source A
//             bottomToTop + bottomToRight, // source D
//             leftToTop + bottomToTop, // destination C
//             leftToRight + bottomToRight // destination E
//         };

//         double[] avgTime = new double[4];
//         int[] countFlow = new int[4];

//         if (ended) {
//             for (int i = 0; i < totalAgents; i++) {
//                 Agent agent = agents.get(i);
//                 if (agent != null && agent.endTime > 0) {
//                     int f = agent.flow;
//                     avgTime[f] += agent.getDuration();
//                     countFlow[f]++;
//                 }
//             }

//             for (int i = 0; i < 4; i++) {
//                 if (countFlow[i] > 0) {
//                     avgTime[i] /= countFlow[i];
//                 }
//             }

//             long endTime = System.currentTimeMillis();
//             double simTime = (endTime - startSearchTime) * 0.001;
//             g2.drawString(String.format("%.2f", simTime), startX + 110, startY - 30);
//         }

//         for (int i = 0; i < 4; i++) {
//             int y = startY + i * spacing;

//             g2.setColor(FLOW_COLORS[i]);
//             g2.fillOval(startX, y - circleSize + 9, circleSize, circleSize);

//             g2.setColor(Color.BLACK);

//             int col1X = startX + 20;
//             int col2X = startX + 100;
//             int col3X = startX + 140;

//             g2.drawString(getFlowName(i), col1X, y + 9);
//             g2.drawString(String.valueOf(flowCount[i]), col2X, y + 9);

//             if (ended && countFlow[i] > 0) {
//                 g2.drawString(String.format("%.2f", avgTime[i] * 0.001), col3X, y + 9);
//             } else {
//                 g2.drawString("-", col3X, y + 9);
//             }
//         }

//         // ===== Source / Destination count =====
//         int startY2 = startY + 4 * spacing + 30;
//         int colLeftX = startX;
//         int colRightX = startX + 90;

//         g2.setColor(Color.BLACK);
//         g2.drawString("Source Count", colLeftX, startY2 - 20);
//         g2.drawString("Destination Count", colRightX, startY2 - 20);

//         String[] sourceLabels = {"A", "D"};
//         String[] destLabels = {"C", "E"};

//         for (int i = 0; i < 2; i++) {
//             int y = startY2 + i * spacing;
//             g2.drawString(sourceLabels[i] + ": " + srcDstCount[i], colLeftX, y);
//             g2.drawString(destLabels[i] + ": " + srcDstCount[i + 2], colRightX, y);
//         }

//         // ===== Realtime graph =====
//         int graphX = startX + 15;
//         int graphY = startY2 + 2 * spacing + 10;
//         int graphWidth = 150;
//         int graphHeight = 80;

//         double maxTimeRange = 80.0;
//         int maxAgentRange = totalAgents;

//         g2.setColor(Color.BLACK);
//         g2.drawRect(graphX, graphY, graphWidth, graphHeight);

//         g2.drawString("Agents", graphX, graphY - 5);
//         g2.drawString("0", graphX - 10, graphY + graphHeight);
//         g2.drawString(String.valueOf(maxAgentRange), graphX - 25, graphY + 10);
//         g2.drawString("0", graphX, graphY + graphHeight + 15);
//         g2.drawString(String.valueOf((int) maxTimeRange), graphX + graphWidth - 20, graphY + graphHeight + 15);
//         g2.drawString("Time(s)", graphX + graphWidth + 5, graphY + graphHeight + 7);

//         if (timeHistory.size() > 1) {
//             g2.setColor(Color.BLUE);

//             for (int i = 1; i < timeHistory.size(); i++) {
//                 double t1 = Math.min(timeHistory.get(i - 1), maxTimeRange);
//                 double t2 = Math.min(timeHistory.get(i), maxTimeRange);

//                 int a1 = Math.min(agentHistory.get(i - 1), maxAgentRange);
//                 int a2 = Math.min(agentHistory.get(i), maxAgentRange);

//                 int x1 = graphX + (int) (t1 / maxTimeRange * graphWidth);
//                 int x2 = graphX + (int) (t2 / maxTimeRange * graphWidth);

//                 int y1 = graphY + graphHeight - (int) ((double) a1 / maxAgentRange * graphHeight);
//                 int y2 = graphY + graphHeight - (int) ((double) a2 / maxAgentRange * graphHeight);

//                 g2.drawLine(x1, y1, x2, y2);
//             }
//         }

//         // ===== Draw node labels =====
//         if (nodePoints != null) {
//             Graphics2D gText = (Graphics2D) g2.create();
//             gText.setFont(new Font("Lexend", Font.BOLD, 24));
//             gText.setColor(Color.BLACK);

//             for (int i = 0; i < nodePoints.size(); i++) {
//                 Vector2D screenPos = nodePoints.get(i);
//                 char label = (char) ('A' + i);
//                 String text = String.valueOf(label);

//                 FontMetrics fm = gText.getFontMetrics();
//                 int textWidth = fm.stringWidth(text);
//                 int textHeight = fm.getAscent();

//                 int x = (int) screenPos.getX() - textWidth / 2;
//                 int y = (int) screenPos.getY() + textHeight / 2;

//                 gText.drawString(text, x, y);
//             }

//             gText.dispose();
//         }

//         // ===== Spawn histogram =====
//         int chartWidth = 210;
//         int chartHeight = 320;
//         int chartX = 10;
//         int chartY = 425;

//         drawSpawnHistogram(g2, chartX, chartY, chartWidth, chartHeight);
//     }

//     // ====================== HISTOGRAM ==========================
//     private void drawSpawnHistogram(Graphics2D g2, int x, int y, int width, int height) {
//         List<Double> intervals;

//         synchronized (spawnChartLock) {
//             intervals = new ArrayList<>(spawnIntervalsSec);
//         }

//         Graphics2D g = (Graphics2D) g2.create();
//         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

//         g.setColor(Color.WHITE);
//         g.fillRoundRect(x, y, width, height, 18, 18);
//         g.setColor(Color.BLACK);
//         g.setStroke(new BasicStroke(2f));
//         g.drawRoundRect(x, y, width, height, 18, 18);

//         g.setFont(new Font("Arial", Font.BOLD, 13));
//         String title = "Spawn Interval Histogram";
//         FontMetrics titleFm = g.getFontMetrics();
//         int titleX = x + (width - titleFm.stringWidth(title)) / 2;
//         g.drawString(title, titleX, y + 38);

//         List<Double> values = new ArrayList<>();
//         for (double v : intervals) {
//             if (v > 0.0) {
//                 values.add(v);
//             }
//         }

//         if (values.size() < 2) {
//             g.setFont(new Font("Arial", Font.PLAIN, 12));
//             g.drawString("In Process", x + 20, y + 70);
//             g.dispose();
//             return;
//         }

//         int leftPad = 44;
//         int rightPad = 20;
//         int topPad = 58;
//         int bottomPad = 72;

//         int px = x + leftPad;
//         int py = y + topPad;
//         int pw = width - leftPad - rightPad;
//         int ph = height - topPad - bottomPad;

//         g.setColor(new Color(235, 235, 242));
//         g.fillRect(px, py, pw, ph);

//         g.setColor(Color.BLACK);
//         g.setStroke(new BasicStroke(2.2f));
//         g.drawRect(px, py, pw, ph);

//         // ===== histogram bins =====
//         int binCount = 8;

//         double minVal = Double.MAX_VALUE;
//         double maxVal = -Double.MAX_VALUE;
//         for (double v : values) {
//             if (v < minVal) {
//                 minVal = v;
//             }
//             if (v > maxVal) {
//                 maxVal = v;
//             }
//         }

//         if (Math.abs(maxVal - minVal) < 1e-9) {
//             maxVal = minVal + 1.0;
//         }

//         double binSize = (maxVal - minVal) / binCount;
//         int[] counts = new int[binCount];

//         for (double v : values) {
//             int bin = (int) ((v - minVal) / binSize);
//             if (bin >= binCount) {
//                 bin = binCount - 1;
//             }
//             if (bin < 0) {
//                 bin = 0;
//             }
//             counts[bin]++;
//         }

//         int maxCount = 1;
//         for (int c : counts) {
//             if (c > maxCount) {
//                 maxCount = c;
//             }
//         }

//         Stroke oldStroke = g.getStroke();
//         Stroke dashed = new BasicStroke(
//                 1.2f,
//                 BasicStroke.CAP_BUTT,
//                 BasicStroke.JOIN_BEVEL,
//                 0,
//                 new float[]{8f, 8f},
//                 0
//         );
//         g.setStroke(dashed);
//         g.setColor(Color.DARK_GRAY);

//         int yMidPix = py + ph / 2;
//         g.drawLine(px, yMidPix, px + pw, yMidPix);

//         g.setStroke(oldStroke);
//         g.setColor(Color.BLACK);

//         int gap = 4;
//         int barWidth = Math.max(6, (pw - gap * (binCount + 1)) / binCount);

//         for (int i = 0; i < binCount; i++) {
//             int barHeight = (int) Math.round((counts[i] / (double) maxCount) * (ph - 10));
//             int bx = px + gap + i * (barWidth + gap);
//             int by = py + ph - barHeight;

//             g.setColor(new Color(100, 149, 237));
//             g.fillRect(bx, by, barWidth, barHeight);

//             g.setColor(Color.BLACK);
//             g.drawRect(bx, by, barWidth, barHeight);
//         }

//         // ===== y labels (count) =====
//         g.setFont(new Font("Arial", Font.PLAIN, 12));
//         g.drawString(String.valueOf(maxCount), x + 10, py + 5);
//         g.drawString(String.valueOf(maxCount / 2), x + 10, yMidPix + 5);
//         g.drawString("0", x + 18, py + ph + 5);

//         // ===== x labels (interval range) =====
//         String minLabel = String.format("%.0f", minVal);
//         String maxLabel = String.format("%.0f", maxVal);
//         g.drawString(minLabel, px - 5, y + height - 40);
//         g.drawString(maxLabel, px + pw - 20, y + height - 40);
//         g.drawString("seconds", px + pw / 2 - 20, y + height - 20);

//         // ===== summary =====
//         double sum = 0.0;
//         for (double v : values) {
//             sum += v;
//         }
//         double mean = sum / values.size();

//         g.setFont(new Font("Arial", Font.PLAIN, 12));
//         String status = String.format("n=%d, mean=%.1fs", values.size(), mean);
//         g.drawString(status, x + 20, y + height - 3);

//         g.dispose();
//     }

//     // ====================== UPDATE VISUALIZATION ==========================
//     @Override
//     protected void updateVisualization() {
//         if ((Simulator.instance.getGlobalTime() % 100) == 0) {
//             System.out.println("Step: " + Simulator.instance.getGlobalTime()
//                     + " -> Completed: " + agentCount + "/" + totalAgents);
//         }

//         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
//             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);

//             if (goalsList.get(agentNo).getFlag() == 1) {
//                 goalsList.get(agentNo).addMovingStep(agentPos);

//                 if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) < 10.0) {
//                     Agent agent = agents.get(agentNo);
//                     if (isBusy) {
//                         slowExitCount++;
//                     } else {
//                         throughExitCount++;
//                     }

//                     if (agent != null) {
//                         agent.endTime = System.currentTimeMillis();
//                     }

//                     agentCount++;
//                     long elapsed = System.currentTimeMillis() - startSearchTime;
//                     goalsList.get(agentNo).setReachGoalms(elapsed);
//                     goalsList.get(agentNo).setFlag(2);
//                     sumTime += elapsed;

//                     Simulator.instance.setAgentMaxSpeed(agentNo, 0.0);
//                     Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);

//                     Simulator.instance.setAgentRadius(agentNo, 0.0);
//                     Simulator.instance.setAgentNeighborDistance(agentNo, 0.0);
//                     Simulator.instance.setAgentMaxNeighbors(agentNo, 0);

//                     Simulator.instance.setAgentPosition(agentNo, new Vector2D(-1e9, -1e9));
//                 }
//             }
//         }

//         int currentAgents = spawnedAgents - agentCount;
//         timeHistory.add(time);
//         agentHistory.add(currentAgents);

//         if (timeHistory.size() > 5000) {
//             timeHistory.remove(0);
//             agentHistory.remove(0);
//         }

//         repaint();
//     }

//     // ====================== PREFERRED VELOCITIES ==========================
//     @Override
//     // protected void setPreferredVelocities() {
//     //     super.setPreferredVelocities();

//     //     double[][] behavior = createBehaviorMatrix();
//     //     final double forwardDotThreshold = 0.85;
//     //     double currentTime = Simulator.instance.getGlobalTime();
//     //     int gap = 50;
//     //     time = (System.currentTimeMillis() - initialMillis) * 0.001;
//     //     if (spawnedAgents < totalAgents
//     //             && currentTime - lastSpawnTime >= spawnInterval) {
//     //         double usedInterval = spawnInterval;
//     //         spawnOneAgent(graph, behavior, rand, gap);
//     //         spawnedAgents++;
//     //         lastSpawnTime = currentTime;
//     //         recordSpawnEvent(currentTime, usedInterval);
//     //         spawnInterval = getNextSpawnInterval();
//     //     }
//     //     int numAgents = Simulator.instance.getNumAgents();
//     //     for (int agentNo = 0; agentNo < numAgents; agentNo++) {
//     //         Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
//     //         if (pref == null) {
//     //             continue;
//     //         }
//     //         double prefNorm = pref.getNorm();
//     //         if (prefNorm < 1e-6) {
//     //             continue;
//     //         }
//     //         Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
//     //         Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//     //         double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
//     //         double checkDistance = neighborDistance;
//     //         double minScale = 1.0;
//     //         for (int other = 0; other < numAgents; other++) {
//     //             if (other == agentNo) {
//     //                 continue;
//     //             }
//     //             Vector2D otherPos = Simulator.instance.getAgentPosition(other);
//     //             if (otherPos == null) {
//     //                 continue;
//     //             }
//     //             Vector2D rel = otherPos.subtract(agentPos);
//     //             double dist = rel.getNorm();
//     //             if (dist < 1e-6 || dist > checkDistance) {
//     //                 continue;
//     //             }
//     //             double forwardness = prefDir.dotProduct(rel.scalarMultiply(1.0 / dist));
//     //             if (forwardness < forwardDotThreshold) {
//     //                 continue;
//     //             }
//     //             double safety = Simulator.instance.getAgentRadius(agentNo)
//     //                     + Simulator.instance.getAgentRadius(other) + 1.0;
//     //             double desiredSpacing = Math.max(checkDistance, safety + 1.0);
//     //             double scale = (dist - safety) / (desiredSpacing - safety);
//     //             scale = Math.max(0.0, Math.min(1.0, scale));
//     //             if (scale < minScale) {
//     //                 minScale = scale;
//     //             }
//     //         }
//     //         if (minScale < 1.0) {
//     //             Vector2D newPref = pref.scalarMultiply(minScale);
//     //             Simulator.instance.setAgentPreferredVelocity(agentNo, newPref);
//     //         }
//     //     }
//     // }
//     protected void setPreferredVelocities() {
//         super.setPreferredVelocities();
//         time = (System.currentTimeMillis() - initialMillis) * 0.001;

//         double currentTime = Simulator.instance.getGlobalTime();

//         // Update dynamic BUSY/FREE exit status before speed control.
//         updateTrafficState();

//         if (spawnedAgents < totalAgents && currentTime - lastSpawnTime >= spawnInterval) {
//             double usedInterval = spawnInterval;
//             spawnOneAgent(graph, createBehaviorMatrix(), rand, 50);
//             spawnedAgents++;
//             lastSpawnTime = currentTime;
//             recordSpawnEvent(currentTime, usedInterval);
//             spawnInterval = getNextSpawnInterval();
//         }

//         int numAgents = Simulator.instance.getNumAgents();
//         for (int agentNo = 0; agentNo < numAgents; agentNo++) {
//             Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
//             if (pref == null || pref.getNorm() < 1e-6) {
//                 continue;
//             }

//             Goal goal = goalsList.get(agentNo);
//             if (goal == null || goal.getFlag() != 1) {
//                 continue;
//             }

//             // Apply BUSY-gate logic only when the route has an active next sub-goal.
//             if (goal.getNext() > 0) {

//                 // Final exits in TC7 are C(2) and E(4).
//                 if (isBusy && (goal.getDestination() == 2 || goal.getDestination() == 4)) {

//                     Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//                     Vector2D targetPos = goal.getGlobalGoal(); // final destination
//                     double distToGoal = targetPos.subtract(agentPos).getNorm();

//                     // Inside the detection radius, smoothly reduce the preferred velocity.
//                     if (distToGoal < DETECTION_RADIUS) {
//                         double speedFactor = distToGoal / DETECTION_RADIUS;
//                         speedFactor = Math.max(MIN_SPEED_FACTOR, speedFactor);

//                         // Scale velocity by distance to the final gate.
//                         pref = pref.scalarMultiply(speedFactor);
//                     }
//                 }
//             }

//             // Local forward-neighbor braking to reduce pushing/rear-end collisions.
//             double prefNorm = pref.getNorm();
//             Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
//             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);

//             double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
//             double minScale = 1.0;

//             for (int other = 0; other < numAgents; other++) {
//                 if (other == agentNo) {
//                     continue;
//                 }

//                 Vector2D otherPos = Simulator.instance.getAgentPosition(other);
//                 if (otherPos == null) {
//                     continue;
//                 }

//                 Vector2D rel = otherPos.subtract(agentPos);
//                 double dist = rel.getNorm();
//                 if (dist < 1e-6 || dist > neighborDistance) {
//                     continue;
//                 }

//                 double forwardness = prefDir.dotProduct(rel.scalarMultiply(1.0 / dist));
//                 if (forwardness < 0.85) {
//                     continue;
//                 }

//                 double safety = Simulator.instance.getAgentRadius(agentNo) + Simulator.instance.getAgentRadius(other) + 1.0;
//                 double desiredSpacing = Math.max(neighborDistance, safety + 1.0);

//                 double scale = (dist - safety) / (desiredSpacing - safety);
//                 scale = Math.max(0.0, Math.min(1.0, scale));

//                 if (scale < minScale) {
//                     minScale = scale;
//                 }
//             }

//             // Apply the final velocity after gate control and local braking.
//             Vector2D finalPref = pref.scalarMultiply(minScale);
//             Simulator.instance.setAgentPreferredVelocity(agentNo, finalPref);
//         }
//     }

//     public static void main(String[] args) {
//         TestCase07_setting1 testCase = new TestCase07_setting1();
//         testCase.runSimulation();
//     }

//     @Override
//     protected void runSimulation() {
//         setupScenario();

//         JFrame frame = new JFrame(scenarioName);
//         frame.setLayout(new BorderLayout());
//         frame.setSize(w, h);

//         // Add a runtime button for showing/hiding agent trajectory traces.
//         togglePathButton = new JButton("Show Paths: OFF");
//         togglePathButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 showPathsDuringSimulation = !showPathsDuringSimulation;
//                 togglePathButton.setText("Show Paths: " + (showPathsDuringSimulation ? "ON" : "OFF"));
//             }
//         });
//         frame.add(togglePathButton, BorderLayout.NORTH);

//         frame.add(this, BorderLayout.CENTER);
//         frame.pack();
//         frame.setVisible(true);
//         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//         final int TARGET_FPS = 60;
//         final long FRAME_TIME = 1000 / TARGET_FPS;

//         long lastFrameTime = System.currentTimeMillis();
//         do {
//             long frameStart = System.currentTimeMillis();

//             updateVisualization();
//             setPreferredVelocities();
//             updateSubGoal();
//             Simulator.instance.doStep();

//             totalSteps++;

//             long frameElapsed = System.currentTimeMillis() - frameStart;
//             if (frameElapsed < FRAME_TIME) {
//                 try {
//                     Thread.sleep(FRAME_TIME - frameElapsed);
//                 } catch (InterruptedException e) {
//                     e.printStackTrace();
//                 }
//             }

//             lastFrameTime = System.currentTimeMillis();
//             if (agentCount == totalAgents) {
//                 break;
//             }
//         } while (!reachedGoal());

//         ended = true;
//         updateVisualization();
//         printResults();
//     }
// }
