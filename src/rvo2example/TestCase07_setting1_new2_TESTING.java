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
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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
import algorithms.dijkstra.model.Edge;
import rvo2.Simulator;
import rvo2.Goal;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

/**
 * TC7: One intersection - 8 two-way flows.
 *
 * Layout:
 * 0 = A (left source)
 * 1 = B (old intersection center, now only used as geometric reference)
 * 2 = C (right exit)
 * 3 = D (bottom source)
 * 4 = E (top exit)
 *
 * Routes:
 * A -> C and C -> A : horizontal straight, two separate lanes
 * D -> E and E -> D : vertical straight, two separate lanes
 * A -> E, E -> A, D -> C, C -> D : curved/right-turn movements
 */
public class TestCase07_setting1_new2_TESTING extends BaseTrafficScenario_S1 {

    private static final long serialVersionUID = 1L;

    public TestCase07_setting1_new2_TESTING() {
        scenarioName = "TC7: One Intersection - 8 Two-Way Flows";
        trafficType = "1-intersection-8-two-way-flows";
        totalAgents = 25;
    }

    // ====================== CURVE / GRAPH PARAMETERS ==========================
    private static final int TURN_SEGMENTS = 6;
    private static final double INTERSECTION_OFFSET_RATIO = 0.16;
    private static final double LANE_OFFSET_RATIO = 0.24;
    private static final double ARC_RADIUS_RATIO = 0.9;
    private static final double CONNECTOR_STEP_RATIO = 0.45;
    private static final double AGENT_RADIUS = 10.0;


    private double roadGap = 50.0;

    private enum CornerType {
        NW, NE, SE, SW
    }

    private static class IntersectionNodes {
        int westAnchor;
        int southAnchor;
        int northAnchor;
        int eastAnchor;

        List<Integer> westToNorthArc = new ArrayList<>();
        List<Integer> southToEastArc = new ArrayList<>();

        List<Integer> straightWestToEast = new ArrayList<>();
        List<Integer> straightSouthToNorth = new ArrayList<>();
    }

    // ====================== STATE ==========================
    private int spawnIndex = 0;
    private List<int[]> flowOrder = new ArrayList<>();

    private boolean isBusy = false;
    private long nextSwitchWallTimeMs = 0L;
    private static final long MIN_STATUS_DURATION_MS = 4000L;
    private static final long MAX_STATUS_DURATION_MS = 7000L;

    private static final double DETECTION_RADIUS = 60.0;
    private static final double MIN_SPEED_FACTOR = 0.15;

    private int throughExitCount = 0;
    private int slowExitCount = 0;

    int leftToRight = 0;      // A -> C
    int rightToLeft = 0;      // C -> A
    int bottomToTop = 0;      // D -> E
    int topToBottom = 0;      // E -> D
    int leftToTop = 0;        // A -> E
    int topToLeft = 0;        // E -> A
    int bottomToRight = 0;    // D -> C
    int rightToBottom = 0;    // C -> D

    double startTime = 0;
    double endTime = 0;
    static Random rand = new Random();

    String disType = "Uniform Distribution";

    int[] sources = {0, 2, 3, 4};
    int[] destinations = {0, 2, 3, 4};

    int spawnedAgents = 0;
    double spawnInterval = 60;
    double lastSpawnTime = 0;
    private double minSpawnInterval = 15.0;
    private double maxSpawnInterval = 120.0;

    private DISTRIBUTION_TYPE distributionType = DISTRIBUTION_TYPE.Uniformed;
    private double meanSpawnInterval = 60.0;
    private double stdSpawnInterval = 15.0;
    private double randomMinSpawnInterval = 20.0;
    private double randomMaxSpawnInterval = 100.0;

    private Graph graph;
    private DijkstraAlgorithm dijkstra;
    private Map<String, List<Integer>> routePaths = new HashMap<>();

    private int graphA;
    private int graphC;
    private int graphD;
    private int graphE;

    int x_C = 0;
    int y_C = 0;

    private List<Vector2D> nodePoints = new ArrayList<>();
    Map<Integer, Agent> agents = new HashMap<>();

    private final List<Double> timeHistory = new ArrayList<>();
    private final List<Integer> agentHistory = new ArrayList<>();
    private long initialMillis = System.currentTimeMillis();
    private double time = 0.0;

    private final List<Double> spawnTimesSec = new ArrayList<>();
    private final List<Double> spawnIntervalsSec = new ArrayList<>();
    private final Object spawnChartLock = new Object();

    private boolean showPathsDuringSimulation = false;
    private JButton togglePathButton;

    enum DISTRIBUTION_TYPE {
        Uniformed, Normal, Randomed
    }

    private static final Color[] FLOW_COLORS = new Color[]{
        Color.BLACK,              // flow 0: A-C
        Color.RED,                // flow 1: C-A
        Color.ORANGE,             // flow 2: D-E
        Color.GREEN,              // flow 3: E-D
        Color.CYAN,               // flow 4: A-E
        Color.MAGENTA,            // flow 5: E-A
        Color.BLUE,               // flow 6: D-C
        new Color(128, 0, 128)    // flow 7: C-D
    };

    private static final int[][] LEGAL_FLOWS = new int[][]{
        {0, 2}, // A -> C
        {2, 0}, // C -> A
        {3, 4}, // D -> E
        {4, 3}, // E -> D
        {0, 4}, // A -> E
        {4, 0}, // E -> A
        {3, 2}, // D -> C
        {2, 3}  // C -> D
    };

    /*
     * Weighted spawn table. Repeating a flow makes it appear more often.
     * Tune this table when you want different in/out volumes per direction.
     */
    private static final int[][] WEIGHTED_FLOWS = new int[][]{
        {0, 2}, {0, 2}, {0, 2}, {0, 2}, // A -> C high
        {2, 0}, {2, 0},                 // C -> A medium
        {3, 4}, {3, 4}, {3, 4},         // D -> E medium-high
        {4, 3},                         // E -> D low
        {0, 4},                         // A -> E turn
        {4, 0},                         // E -> A turn
        {3, 2},                         // D -> C turn
        {2, 3}                          // C -> D turn
    };


    // ====================== TWO-WAY INTERSECTION RESERVATION ==========================
    private static final double INTERSECTION_REQUEST_RADIUS = 60.0;
    private static final double INTERSECTION_ENTER_RADIUS = 30.0;
    private static final double INTERSECTION_RELEASE_RADIUS = 30.0;
    private static final double MAX_TURN_WAIT_TIME = 5.0;
    private static final int MAX_COMPATIBLE_OWNERS = 1;

    private enum MovementKind {
        HORIZONTAL_STRAIGHT,
        VERTICAL_STRAIGHT,
        TURN
    }

    private static class IntersectionReservation {
        Vector2D center;
        final Set<Integer> owners = new HashSet<>();
        final Queue<Integer> queue = new LinkedList<>();
        final Map<Integer, Double> queuedSince = new HashMap<>();
        final Set<Integer> entered = new HashSet<>();
        final Set<Integer> cleared = new HashSet<>();

        void reset(Vector2D center) {
            this.center = center;
            owners.clear();
            queue.clear();
            queuedSince.clear();
            entered.clear();
            cleared.clear();
        }
    }

    private IntersectionReservation intersectionReservation = new IntersectionReservation();

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

    // ====================== GEOMETRY / GRAPH HELPERS ==========================
    private int addVertex(List<Vertex> nodes, String id, Vector2D p) {
        int index = nodes.size();
        Vertex v = new Vertex(id, index, 0, id, p, p, 0);
        nodes.add(v);
        return index;
    }

    private void addDirectedEdge(List<Edge> edges, List<Vertex> nodes, int from, int to) {
        Vector2D a = nodes.get(from).getVertex1();
        Vector2D b = nodes.get(to).getVertex1();
        double dist = a.distance(b);

        edges.add(new Edge(
                "E_" + from + "_" + to + "_" + edges.size(),
                nodes.get(from),
                nodes.get(to),
                dist
        ));
    }

    private void addPathEdges(List<Edge> edges, List<Vertex> nodes, List<Integer> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            addDirectedEdge(edges, nodes, path.get(i), path.get(i + 1));
        }
    }

    private Vector2D lerp(Vector2D a, Vector2D b, double t) {
        return new Vector2D(
                a.getX() + (b.getX() - a.getX()) * t,
                a.getY() + (b.getY() - a.getY()) * t
        );
    }

    private List<Vector2D> generateArcPoints(
            Vector2D center,
            double radius,
            double startAngleDeg,
            double endAngleDeg,
            int segments
    ) {
        List<Vector2D> points = new ArrayList<>();

        double startRad = Math.toRadians(startAngleDeg);
        double endRad = Math.toRadians(endAngleDeg);

        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            double angle = startRad + (endRad - startRad) * t;

            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() - radius * Math.sin(angle);

            points.add(new Vector2D(x, y));
        }

        return points;
    }

    private List<Integer> createArcVertexChain(
            String prefix,
            Vector2D intersectionCenter,
            CornerType corner,
            List<Vertex> nodes
    ) {
        double laneOffset = roadGap * INTERSECTION_OFFSET_RATIO;
        double radius = laneOffset * ARC_RADIUS_RATIO;

        Vector2D arcCenter;
        double startAngleDeg;
        double endAngleDeg;

        switch (corner) {
            case NW:
                arcCenter = new Vector2D(
                        intersectionCenter.getX() - laneOffset,
                        intersectionCenter.getY() - laneOffset
                );
                startAngleDeg = 180;
                endAngleDeg = 90;
                break;

            case NE:
                arcCenter = new Vector2D(
                        intersectionCenter.getX() + laneOffset,
                        intersectionCenter.getY() - laneOffset
                );
                startAngleDeg = 90;
                endAngleDeg = 0;
                break;

            case SE:
                arcCenter = new Vector2D(
                        intersectionCenter.getX() + laneOffset,
                        intersectionCenter.getY() + laneOffset
                );
                startAngleDeg = 270;
                endAngleDeg = 360;
                break;

            case SW:
                arcCenter = new Vector2D(
                        intersectionCenter.getX() - laneOffset,
                        intersectionCenter.getY() + laneOffset
                );
                startAngleDeg = 180;
                endAngleDeg = 270;
                break;

            default:
                return Collections.emptyList();
        }

        List<Vector2D> arcPoints = generateArcPoints(
                arcCenter,
                radius,
                startAngleDeg,
                endAngleDeg,
                TURN_SEGMENTS
        );

        List<Integer> arcIds = new ArrayList<>();
        for (int i = 0; i < arcPoints.size(); i++) {
            int id = addVertex(nodes, prefix + "_ARC_" + corner + "_" + i, arcPoints.get(i));
            arcIds.add(id);
        }

        return arcIds;
    }

    private List<Integer> createStraightConnectorChain(
            String prefix,
            Vector2D from,
            Vector2D to,
            int segments,
            List<Vertex> nodes
    ) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= segments; i++) {
            double t = (double) i / (segments + 1);
            Vector2D p = lerp(from, to, t);
            int id = addVertex(nodes, prefix + "_MID_" + i, p);
            ids.add(id);
        }
        return ids;
    }



    private String routeKey(int src, int dst) {
        return src + "->" + dst;
    }

    private Vector2D p(double x, double y) {
        return new Vector2D(x, y);
    }

    private void addRoutePath(
            List<Vertex> nodes,
            List<Edge> edges,
            int logicalSrc,
            int graphSrc,
            int logicalDst,
            int graphDst,
            List<Vector2D> waypoints
    ) {
        List<Integer> path = new ArrayList<>();
        path.add(graphSrc);
        for (int i = 0; i < waypoints.size(); i++) {
            path.add(addVertex(nodes, "R_" + logicalSrc + "_" + logicalDst + "_" + i, waypoints.get(i)));
        }
        path.add(graphDst);
        addPathEdges(edges, nodes, path);
        routePaths.put(routeKey(logicalSrc, logicalDst), path);
    }

    private void assignPathFromRoute(Goal goal, int logicalSrc, int logicalDst) {
        List<Integer> path = routePaths.get(routeKey(logicalSrc, logicalDst));
        if (path == null || path.size() < 2) {
            return;
        }
        for (int i = 1; i < path.size(); i++) {
            goal.add(translateCoordinateVector(graph.getVertexes().get(path.get(i)).getVertex1()));
        }
    }

    private IntersectionNodes buildIntersectionSubgraph(
            String prefix,
            Vector2D center,
            List<Vertex> nodes,
            List<Edge> edges
    ) {
        IntersectionNodes result = new IntersectionNodes();

        double connectorStep = roadGap * CONNECTOR_STEP_RATIO;

        Vector2D westAnchorPos = new Vector2D(center.getX() - connectorStep, center.getY());
        Vector2D southAnchorPos = new Vector2D(center.getX(), center.getY() + connectorStep);
        Vector2D northAnchorPos = new Vector2D(center.getX(), center.getY() - connectorStep);
        Vector2D eastAnchorPos = new Vector2D(center.getX() + connectorStep, center.getY());

        result.westAnchor = addVertex(nodes, prefix + "_W", westAnchorPos);
        result.southAnchor = addVertex(nodes, prefix + "_S", southAnchorPos);
        result.northAnchor = addVertex(nodes, prefix + "_N", northAnchorPos);
        result.eastAnchor = addVertex(nodes, prefix + "_E", eastAnchorPos);

        result.westToNorthArc = createArcVertexChain(prefix + "_WN", center, CornerType.NW, nodes);
        result.southToEastArc = createArcVertexChain(prefix + "_SE", center, CornerType.SE, nodes);

        result.straightWestToEast = createStraightConnectorChain(
                prefix + "_WE",
                westAnchorPos,
                eastAnchorPos,
                2,
                nodes
        );

        result.straightSouthToNorth = createStraightConnectorChain(
                prefix + "_SN",
                southAnchorPos,
                northAnchorPos,
                2,
                nodes
        );

        {
            List<Integer> path = new ArrayList<>();
            path.add(result.westAnchor);
            path.addAll(result.westToNorthArc);
            path.add(result.northAnchor);
            addPathEdges(edges, nodes, path);
        }

        {
            List<Integer> path = new ArrayList<>();
            path.add(result.southAnchor);
            path.addAll(result.southToEastArc);
            path.add(result.eastAnchor);
            addPathEdges(edges, nodes, path);
        }

        {
            List<Integer> path = new ArrayList<>();
            path.add(result.westAnchor);
            path.addAll(result.straightWestToEast);
            path.add(result.eastAnchor);
            addPathEdges(edges, nodes, path);
        }

        {
            List<Integer> path = new ArrayList<>();
            path.add(result.southAnchor);
            path.addAll(result.straightSouthToNorth);
            path.add(result.northAnchor);
            addPathEdges(edges, nodes, path);
        }

        return result;
    }

    private void assignPathFromGraph(Goal goal, Graph graph, int src, int dst) {
        if (src == dst) {
            return;
        }

        LinkedList<Vertex> path = getShortestPath(
                dijkstra,
                graph.getVertexes().get(src),
                graph.getVertexes().get(dst)
        );

        if (path == null || path.isEmpty()) {
            return;
        }

        for (int i = 1; i < path.size(); i++) {
            goal.add(translateCoordinateVector(path.get(i).getVertex1()));
        }
    }

    // ====================== HELPERS ==========================
    private void updateTrafficState() {
        long currentWallTimeMs = System.currentTimeMillis();

        if (nextSwitchWallTimeMs == 0L) {
            long durationMs = MIN_STATUS_DURATION_MS
                    + (long) (rand.nextDouble() * (MAX_STATUS_DURATION_MS - MIN_STATUS_DURATION_MS));
            nextSwitchWallTimeMs = currentWallTimeMs + durationMs;
            return;
        }

        if (currentWallTimeMs >= nextSwitchWallTimeMs) {
            isBusy = !isBusy;

            long durationMs = MIN_STATUS_DURATION_MS
                    + (long) (rand.nextDouble() * (MAX_STATUS_DURATION_MS - MIN_STATUS_DURATION_MS));
            nextSwitchWallTimeMs = currentWallTimeMs + durationMs;

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

    private void shuffleFlows(Random rand) {
        flowOrder.clear();
        for (int[] flow : WEIGHTED_FLOWS) {
            flowOrder.add(new int[]{flow[0], flow[1]});
        }
        Collections.shuffle(flowOrder, rand);
    }

    private int getFlowId(int src, int dst) {
        if (src == 0 && dst == 2) return 0; // A-C
        if (src == 2 && dst == 0) return 1; // C-A
        if (src == 3 && dst == 4) return 2; // D-E
        if (src == 4 && dst == 3) return 3; // E-D
        if (src == 0 && dst == 4) return 4; // A-E
        if (src == 4 && dst == 0) return 5; // E-A
        if (src == 3 && dst == 2) return 6; // D-C
        if (src == 2 && dst == 3) return 7; // C-D
        return 0;
    }

    private String getFlowName(int flow) {
        switch (flow) {
            case 0: return "A-C";
            case 1: return "C-A";
            case 2: return "D-E";
            case 3: return "E-D";
            case 4: return "A-E";
            case 5: return "E-A";
            case 6: return "D-C";
            case 7: return "C-D";
            default: return "Unknown";
        }
    }

    private void incrementFlowCounter(int flow) {
        switch (flow) {
            case 0: leftToRight++; break;
            case 1: rightToLeft++; break;
            case 2: bottomToTop++; break;
            case 3: topToBottom++; break;
            case 4: leftToTop++; break;
            case 5: topToLeft++; break;
            case 6: bottomToRight++; break;
            case 7: rightToBottom++; break;
            default: break;
        }
    }

    private int countSource(int src) {
        int count = 0;
        for (Agent a : agents.values()) {
            if (a != null && a.flow >= 0) {
                // Count only spawned agents by checking a nonzero start time.
            }
        }
        switch (src) {
            case 0: return leftToRight + leftToTop;
            case 2: return rightToLeft + rightToBottom;
            case 3: return bottomToTop + bottomToRight;
            case 4: return topToBottom + topToLeft;
            default: return count;
        }
    }

    private int countDestination(int dst) {
        switch (dst) {
            case 0: return rightToLeft + topToLeft;
            case 2: return leftToRight + bottomToRight;
            case 3: return topToBottom + rightToBottom;
            case 4: return bottomToTop + leftToTop;
            default: return 0;
        }
    }

    private void recordSpawnEvent(double simTimeSec, double usedIntervalSec) {
        synchronized (spawnChartLock) {
            spawnTimesSec.add(simTimeSec);
            spawnIntervalsSec.add(usedIntervalSec);
        }
    }

    private double getNextSpawnInterval() {
        double next;
        switch (distributionType) {
            case Normal:
                disType = "Normal Distribution";
                next = meanSpawnInterval + stdSpawnInterval * rand.nextGaussian();
                next = Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
                break;
            case Randomed:
                disType = "Random Distribution";
                double r = rand.nextDouble();
                next = randomMinSpawnInterval + (randomMaxSpawnInterval - randomMinSpawnInterval) * Math.sqrt(r);
                break;
            case Uniformed:
            default:
                disType = "Uniform Distribution";
                next = minSpawnInterval + rand.nextDouble() * (maxSpawnInterval - minSpawnInterval);
                break;
        }
        return Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
    }

    // ====================== SPAWN ==========================
    private void spawnOneAgent(Graph graph, double[][] behavior, Random rand, double gap) {
        int logicalSrc;
        int logicalDst;

        if (distributionType == DISTRIBUTION_TYPE.Normal || distributionType == DISTRIBUTION_TYPE.Randomed) {
            int[] flow = WEIGHTED_FLOWS[rand.nextInt(WEIGHTED_FLOWS.length)];
            logicalSrc = flow[0];
            logicalDst = flow[1];
        } else {
            if (spawnIndex % WEIGHTED_FLOWS.length == 0) {
                shuffleFlows(rand);
            }
            int[] flow = flowOrder.get(spawnIndex % WEIGHTED_FLOWS.length);
            logicalSrc = flow[0];
            logicalDst = flow[1];
        }

        int k = Simulator.instance.getNumAgents();

        Agent agent = agents.get(k);
        if (agent == null) {
            agent = new Agent(k, 0, 0, 0, 0);
            agents.put(k, agent);
        }
        agent.startTime = System.currentTimeMillis();
        agent.flow = getFlowId(logicalSrc, logicalDst);

        incrementFlowCounter(agent.flow);

        int graphSrc = logicalSrc;
        int graphDst = logicalDst;

        if (logicalSrc == 0) graphSrc = graphA;
        else if (logicalSrc == 2) graphSrc = graphC;
        else if (logicalSrc == 3) graphSrc = graphD;
        else if (logicalSrc == 4) graphSrc = graphE;

        if (logicalDst == 0) graphDst = graphA;
        else if (logicalDst == 2) graphDst = graphC;
        else if (logicalDst == 3) graphDst = graphD;
        else if (logicalDst == 4) graphDst = graphE;

        int behaviorIndex = rand.nextInt(7);

        Simulator.instance.addAgent(
                translateCoordinateVector(graph.getVertexes().get(graphSrc).getVertex1())
        );

        Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
        Simulator.instance.setAgentMaxNeighbors(k, (int) behavior[behaviorIndex][1]);
        Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);

        Simulator.instance.setAgentRadius(k, AGENT_RADIUS);
        Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);

        Goal goal = new Goal(
                translateCoordinateVector(graph.getVertexes().get(graphDst).getVertex1())
        );

        goal.setColor(getColor());
        goal.setFlag(1);
        goal.setSource(logicalSrc);
        goal.setDestination(logicalDst);
        goal.setGlobalGoal(
                translateCoordinateVector(graph.getVertexes().get(graphDst).getVertex1())
        );

        assignPathFromRoute(goal, logicalSrc, logicalDst);

        goalsList.add(goal);
        spawnIndex++;
    }

    // ====================== SETUP ==========================
    @Override
    protected void setupScenario() {
        startSearchTime = System.currentTimeMillis();
        initialMillis = System.currentTimeMillis();
        startTime = Simulator.instance.getGlobalTime();

        isBusy = false;
        nextSwitchWallTimeMs = 0L;
        throughExitCount = 0;
        slowExitCount = 0;

        nodePoints.clear();
        timeHistory.clear();
        agentHistory.clear();
        agents.clear();

        synchronized (spawnChartLock) {
            spawnTimesSec.clear();
            spawnIntervalsSec.clear();
        }

        leftToRight = 0;
        rightToLeft = 0;
        bottomToTop = 0;
        topToBottom = 0;
        leftToTop = 0;
        topToLeft = 0;
        bottomToRight = 0;
        rightToBottom = 0;
        spawnIndex = 0;
        spawnedAgents = 0;

        Simulator.instance.setTimeStep(3);
        Simulator.instance.setAgentDefaults(
                80, 40, 38, 2.5, 3.55, 2.0,
                org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO
        );

        double[][] behavior = createBehaviorMatrix();

        obsList1 = new java.util.ArrayList<Obs>();
        int rowsObs = 2;
        int colsObs = 2;

        // Keep the city blocks at normal size and widen the road gap for two lanes.
        double blockWidth = 450.0;
        double blockHeight = 375.0;
        double gap = 120.0;
        roadGap = gap;

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

        double yMid = startY + blockHeight + gap / 2.0;
        y_C = (int) yMid;

        double xLeft = startX;
        double xBetween = startX + blockWidth + gap / 2.0;
        x_C = (int) xBetween;

        double xRight = startX + blockWidth * 2 + gap;
        double yTop = startY;
        double yBottom = yTop + blockHeight * 2 + gap;

        nodePoints.add(new Vector2D(xLeft, yMid));    // 0 = A
        nodePoints.add(new Vector2D(xBetween, yMid)); // 1 = B center only
        nodePoints.add(new Vector2D(xRight, yMid));   // 2 = C
        nodePoints.add(new Vector2D(xBetween, yBottom)); // 3 = D
        nodePoints.add(new Vector2D(xBetween, yTop));    // 4 = E

        List<Vertex> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        graphA = addVertex(nodes, "A", nodePoints.get(0));
        graphC = addVertex(nodes, "C", nodePoints.get(2));
        graphD = addVertex(nodes, "D", nodePoints.get(3));
        graphE = addVertex(nodes, "E", nodePoints.get(4));

        routePaths.clear();

        double laneOffset = roadGap * LANE_OFFSET_RATIO;
        double approach = roadGap * 0.70;
        Vector2D center = nodePoints.get(1);
        double bx = center.getX();
        double by = center.getY();

        // Right-hand style lanes:
        // A->C uses the lower horizontal lane, C->A uses the upper lane.
        // D->E uses the right vertical lane, E->D uses the left vertical lane.
        addRoutePath(nodes, edges, 0, graphA, 2, graphC, java.util.Arrays.asList(
                p(nodePoints.get(0).getX() + approach, by + laneOffset),
                p(bx - approach, by + laneOffset),
                p(bx + approach, by + laneOffset),
                p(nodePoints.get(2).getX() - approach, by + laneOffset)
        ));

        addRoutePath(nodes, edges, 2, graphC, 0, graphA, java.util.Arrays.asList(
                p(nodePoints.get(2).getX() - approach, by - laneOffset),
                p(bx + approach, by - laneOffset),
                p(bx - approach, by - laneOffset),
                p(nodePoints.get(0).getX() + approach, by - laneOffset)
        ));

        addRoutePath(nodes, edges, 3, graphD, 4, graphE, java.util.Arrays.asList(
                p(bx + laneOffset, nodePoints.get(3).getY() - approach),
                p(bx + laneOffset, by + approach),
                p(bx + laneOffset, by - approach),
                p(bx + laneOffset, nodePoints.get(4).getY() + approach)
        ));

        addRoutePath(nodes, edges, 4, graphE, 3, graphD, java.util.Arrays.asList(
                p(bx - laneOffset, nodePoints.get(4).getY() + approach),
                p(bx - laneOffset, by - approach),
                p(bx - laneOffset, by + approach),
                p(bx - laneOffset, nodePoints.get(3).getY() - approach)
        ));

        addRoutePath(nodes, edges, 0, graphA, 4, graphE, java.util.Arrays.asList(
                p(nodePoints.get(0).getX() + approach, by + laneOffset),
                p(bx - approach, by + laneOffset),
                p(bx - laneOffset, by + laneOffset),
                p(bx + laneOffset, by - laneOffset),
                p(bx + laneOffset, nodePoints.get(4).getY() + approach)
        ));

        addRoutePath(nodes, edges, 4, graphE, 0, graphA, java.util.Arrays.asList(
                p(bx - laneOffset, nodePoints.get(4).getY() + approach),
                p(bx - laneOffset, by - approach),
                p(bx - laneOffset, by - laneOffset),
                p(bx - approach, by - laneOffset),
                p(nodePoints.get(0).getX() + approach, by - laneOffset)
        ));

        addRoutePath(nodes, edges, 3, graphD, 2, graphC, java.util.Arrays.asList(
                p(bx + laneOffset, nodePoints.get(3).getY() - approach),
                p(bx + laneOffset, by + approach),
                p(bx + laneOffset, by + laneOffset),
                p(bx + approach, by + laneOffset),
                p(nodePoints.get(2).getX() - approach, by + laneOffset)
        ));

        addRoutePath(nodes, edges, 2, graphC, 3, graphD, java.util.Arrays.asList(
                p(nodePoints.get(2).getX() - approach, by - laneOffset),
                p(bx + approach, by - laneOffset),
                p(bx - laneOffset, by - laneOffset),
                p(bx - laneOffset, by + approach),
                p(bx - laneOffset, nodePoints.get(3).getY() - approach)
        ));

        graph = new Graph(nodes, edges);
        dijkstra = new DijkstraAlgorithm(graph);

        intersectionReservation.reset(translateCoordinateVector(nodePoints.get(1)));

        System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());
        System.out.println("Auto-generated Graph edges: " + graph.getEdges().size());

        for (int i = 0; i < totalAgents; i++) {
            Agent agent = new Agent(i, 0, 0, 0, 0);
            agents.put(i, agent);
        }

        spawnOneAgent(graph, behavior, rand, gap);
        spawnedAgents++;

        recordSpawnEvent(Simulator.instance.getGlobalTime(), 0.0);
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

        double barGap = 50.0;
        double barThickness = 10.0;

        int[] exitNodes = {0, 2, 3, 4};
        for (int idx : exitNodes) {
            Vector2D p = nodePoints.get(idx);

            g2.setColor(isBusy ? Color.RED : Color.GREEN);

            if (idx == 0 || idx == 2) {
                g2.fillRect((int) (p.getX() - 5), (int) (p.getY() - barGap / 2), (int) barThickness, (int) barGap);
            } else {
                g2.fillRect((int) (p.getX() - barGap / 2), (int) (p.getY() - 5), (int) barGap, (int) barThickness);
            }
        }

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
                    g2.fillOval((int) agentPosCoord.getX() - 2, (int) agentPosCoord.getY() - 2, 4, 4);
                    g2.fillOval((int) agentPosCoord.getX() - radius, (int) agentPosCoord.getY() - radius, radius * 2, radius * 2);
                }

                if ((ended || showPathsDuringSimulation) && totalSteps < 10000) {
                    int pointSize = goalsList.get(agentNo).getaddMovingStepSize();
                    if (pointSize > 1) {
                        g2.setColor(goalsList.get(agentNo).getColor());
                        for (int i = 0; i < pointSize - 1; i += Math.max(1, pointSize / 50)) {
                            Vector2D curr = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(i));
                            Vector2D next = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(Math.min(i + 1, pointSize - 1)));
                            g2.drawLine((int) curr.getX(), (int) curr.getY(), (int) next.getX(), (int) next.getY());
                        }
                    }
                }
            }
        }

        int radius = (int) INTERSECTION_ENTER_RADIUS;
        g2.setColor(Color.GREEN);
        g2.drawOval(x_C - radius, y_C - radius, radius * 2, radius * 2);

        int startX = 20;
        int startY = 150;
        int circleSize = 12;
        int spacing = 20;

        g2.setColor(Color.BLACK);
        g2.drawString(disType, startX, startY - 50);
        g2.drawString("Simulation Time(s)", startX, startY - 30);
        g2.drawString("Agent's Color | Count | Time(s)", startX, startY - 10);

        g2.setColor(isBusy ? Color.RED : new Color(0, 150, 0));
        g2.drawString("EXIT STATUS: " + (isBusy ? "BUSY" : "FREE"), startX + 150, startY - 70);

        g2.setColor(new Color(0, 120, 0));
        g2.drawString("Normal Exits: " + throughExitCount, startX + 150, startY - 50);
        g2.setColor(Color.RED);
        g2.drawString("Slow Exits: " + slowExitCount, startX + 150, startY - 35);

        int[] flowCount = {
            leftToRight,
            rightToLeft,
            bottomToTop,
            topToBottom,
            leftToTop,
            topToLeft,
            bottomToRight,
            rightToBottom
        };

        int[] srcDstCount = {
            countSource(0), countSource(2), countSource(3), countSource(4),
            countDestination(0), countDestination(2), countDestination(3), countDestination(4)
        };

        double[] avgTime = new double[flowCount.length];
        int[] countFlow = new int[flowCount.length];

        if (ended) {
            for (int i = 0; i < totalAgents; i++) {
                Agent agent = agents.get(i);
                if (agent != null && agent.endTime > 0) {
                    int f = agent.flow;
                    avgTime[f] += agent.getDuration();
                    countFlow[f]++;
                }
            }

            for (int i = 0; i < flowCount.length; i++) {
                if (countFlow[i] > 0) {
                    avgTime[i] /= countFlow[i];
                }
            }

            long endTime = System.currentTimeMillis();
            double simTime = (endTime - startSearchTime) * 0.001;
            g2.drawString(String.format("%.2f", simTime), startX + 110, startY - 30);
        }

        for (int i = 0; i < flowCount.length; i++) {
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

        int startY2 = startY + flowCount.length * spacing + 30;
        int colLeftX = startX;
        int colRightX = startX + 90;

        g2.setColor(Color.BLACK);
        g2.drawString("Source Count", colLeftX, startY2 - 20);
        g2.drawString("Destination Count", colRightX, startY2 - 20);

        String[] sourceLabels = {"A", "C", "D", "E"};
        String[] destLabels = {"A", "C", "D", "E"};

        for (int i = 0; i < 4; i++) {
            int y = startY2 + i * spacing;
            g2.drawString(sourceLabels[i] + ": " + srcDstCount[i], colLeftX, y);
            g2.drawString(destLabels[i] + ": " + srcDstCount[i + 4], colRightX, y);
        }

        int graphX = startX + 15;
        int graphY = startY2 + 4 * spacing + 10;
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

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString(String.valueOf(maxCount), x + 10, py + 5);
        g.drawString(String.valueOf(maxCount / 2), x + 10, yMidPix + 5);
        g.drawString("0", x + 18, py + ph + 5);

        String minLabel = String.format("%.0f", minVal);
        String maxLabel = String.format("%.0f", maxVal);
        g.drawString(minLabel, px - 5, y + height - 40);
        g.drawString(maxLabel, px + pw - 20, y + height - 40);
        g.drawString("seconds", px + pw / 2 - 20, y + height - 20);

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

                    Simulator.instance.setAgentPosition(agentNo, new Vector2D(-10000.0, -10000.0));
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



    private MovementKind getMovementKind(Goal goal) {
        int src = goal.getSource();
        int dst = goal.getDestination();
        if ((src == 0 && dst == 2) || (src == 2 && dst == 0)) {
            return MovementKind.HORIZONTAL_STRAIGHT;
        }
        if ((src == 3 && dst == 4) || (src == 4 && dst == 3)) {
            return MovementKind.VERTICAL_STRAIGHT;
        }
        return MovementKind.TURN;
    }

    private int movementPriorityRank(int agentNo) {
        if (agentNo >= goalsList.size()) {
            return 99;
        }

        Goal goal = goalsList.get(agentNo);
        MovementKind kind = getMovementKind(goal);

        // Same idea as the Setting 2 controller: the reservation system owns
        // the intersection decision. Straight movements normally go first;
        // turns are allowed by aging so they cannot starve forever.
        if (kind == MovementKind.HORIZONTAL_STRAIGHT || kind == MovementKind.VERTICAL_STRAIGHT) {
            return 0;
        }
        return 1;
    }

    private Integer pickNextIntersectionOwner() {
        Integer best = null;
        double now = Simulator.instance.getGlobalTime();

        for (Integer candidate : intersectionReservation.queue) {
            if (candidate == null || candidate >= goalsList.size()) {
                continue;
            }
            if (goalsList.get(candidate).getFlag() != 1 || intersectionReservation.cleared.contains(candidate)) {
                continue;
            }

            double wait = now - intersectionReservation.queuedSince.getOrDefault(candidate, now);
            boolean candidateAged = wait >= MAX_TURN_WAIT_TIME;

            if (best == null) {
                best = candidate;
                continue;
            }

            double bestWait = now - intersectionReservation.queuedSince.getOrDefault(best, now);
            boolean bestAged = bestWait >= MAX_TURN_WAIT_TIME;

            if (candidateAged && !bestAged) {
                best = candidate;
                continue;
            }
            if (candidateAged == bestAged && movementPriorityRank(candidate) < movementPriorityRank(best)) {
                best = candidate;
            }
        }

        return best;
    }

    private void refreshIntersectionReservation() {
        for (Integer owner : new ArrayList<>(intersectionReservation.owners)) {
            if (owner >= goalsList.size() || goalsList.get(owner).getFlag() != 1) {
                intersectionReservation.owners.remove(owner);
                intersectionReservation.entered.remove(owner);
                intersectionReservation.cleared.add(owner);
                continue;
            }

            Vector2D pos = Simulator.instance.getAgentPosition(owner);
            if (pos == null) {
                continue;
            }
            double dist = pos.distance(intersectionReservation.center);

            // Important fix: request radius and actual intersection-enter radius
            // are different. The previous version marked an agent as "entered"
            // too early, which could release reservations at the wrong time and
            // let several agents crowd the center.
            if (dist < INTERSECTION_ENTER_RADIUS) {
                intersectionReservation.entered.add(owner);
            }
            if (intersectionReservation.entered.contains(owner) && dist > INTERSECTION_RELEASE_RADIUS) {
                intersectionReservation.owners.remove(owner);
                intersectionReservation.entered.remove(owner);
                intersectionReservation.cleared.add(owner);
            }
        }

        intersectionReservation.queue.removeIf(agentNo -> {
            boolean remove = agentNo >= goalsList.size()
                    || goalsList.get(agentNo).getFlag() != 1
                    || intersectionReservation.cleared.contains(agentNo);
            if (remove) {
                intersectionReservation.queuedSince.remove(agentNo);
            }
            return remove;
        });
    }

    private boolean ownsOrCanReserveIntersection(int agentNo) {
        if (agentNo >= goalsList.size() || goalsList.get(agentNo).getFlag() != 1) {
            return true;
        }
        if (intersectionReservation.cleared.contains(agentNo)) {
            return true;
        }
        if (intersectionReservation.owners.contains(agentNo)) {
            return true;
        }

        Vector2D pos = Simulator.instance.getAgentPosition(agentNo);
        if (pos == null || pos.distance(intersectionReservation.center) > INTERSECTION_REQUEST_RADIUS) {
            return true;
        }

        if (!intersectionReservation.queue.contains(agentNo)) {
            intersectionReservation.queue.add(agentNo);
            intersectionReservation.queuedSince.put(agentNo, Simulator.instance.getGlobalTime());
        }

        // Setting 2 style: only one owner enters the conflict zone. This is
        // safer for the first two-way implementation and prevents the visible
        // intersection pile-up. We can later re-enable compatible simultaneous
        // straight movements after the lane geometry is fully validated.
        if (intersectionReservation.owners.isEmpty()) {
            Integer chosen = pickNextIntersectionOwner();
            if (chosen != null) {
                intersectionReservation.queue.remove(chosen);
                intersectionReservation.queuedSince.remove(chosen);
                intersectionReservation.owners.add(chosen);
            }
        }

        return intersectionReservation.owners.contains(agentNo);
    }

    // ====================== PREFERRED VELOCITIES ==========================
    @Override
    protected void setPreferredVelocities() {
        super.setPreferredVelocities();
        time = (System.currentTimeMillis() - initialMillis) * 0.001;

        double currentTime = Simulator.instance.getGlobalTime();
        updateTrafficState();
        refreshIntersectionReservation();

        if (spawnedAgents < totalAgents && currentTime - lastSpawnTime >= spawnInterval) {
            double usedInterval = spawnInterval;
            spawnOneAgent(graph, createBehaviorMatrix(), rand, roadGap);
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

            if (!ownsOrCanReserveIntersection(agentNo)) {
                Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
                continue;
            }

            if (goal.getNext() > 0) {
                if (isBusy && (goal.getDestination() == 0 || goal.getDestination() == 2 || goal.getDestination() == 3 || goal.getDestination() == 4)) {
                    Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
                    Vector2D targetPos = goal.getGlobalGoal();
                    double distToGoal = targetPos.subtract(agentPos).getNorm();

                    if (distToGoal < DETECTION_RADIUS) {
                        double speedFactor = distToGoal / DETECTION_RADIUS;
                        speedFactor = Math.max(MIN_SPEED_FACTOR, speedFactor);
                        pref = pref.scalarMultiply(speedFactor);
                    }
                }
            }

            double prefNorm = pref.getNorm();
            Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);

            /*
             * Do not apply the forward-neighbor braking inside the reservation
             * request zone. Near the intersection, the reservation controller is
             * already deciding who may enter. If braking is also applied there,
             * the current owner can be slowed to almost zero before reaching the
             * ENTER radius, so it keeps the reservation while everyone else waits.
             */
            if (intersectionReservation.center != null
                    && agentPos.distance(intersectionReservation.center) < INTERSECTION_REQUEST_RADIUS) {
                Simulator.instance.setAgentPreferredVelocity(agentNo, pref);
                continue;
            }

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

                double safety = Simulator.instance.getAgentRadius(agentNo)
                        + Simulator.instance.getAgentRadius(other) + 1.0;
                double desiredSpacing = Math.max(neighborDistance, safety + 1.0);

                double scale = (dist - safety) / (desiredSpacing - safety);
                scale = Math.max(0.0, Math.min(1.0, scale));

                if (scale < minScale) {
                    minScale = scale;
                }
            }

            Vector2D finalPref = pref.scalarMultiply(minScale);
            Simulator.instance.setAgentPreferredVelocity(agentNo, finalPref);
        }
    }

    public static void main(String[] args) {
        TestCase07_setting1_new2_TESTING testCase = new TestCase07_setting1_new2_TESTING();
        testCase.runSimulation();
    }

    @Override
    protected void runSimulation() {
        setupScenario();

        JFrame frame = new JFrame(scenarioName);
        frame.setLayout(new BorderLayout());
        frame.setSize(w, h);

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

            if (agentCount == totalAgents) {
                break;
            }
        } while (!reachedGoal());

        ended = true;
        updateVisualization();
        printResults();
    }
}