// // package rvo2example;

// // import algorithms.dijkstra.engine.DijkstraAlgorithm;
// // import algorithms.dijkstra.model.Graph;
// // import algorithms.dijkstra.model.Vertex;
// // import algorithms.dijkstra.model.Edge;
// // import java.awt.Color;
// // import java.awt.Font;
// // import java.awt.FontMetrics;
// // import java.awt.Graphics;
// // import java.awt.Graphics2D;
// // import java.awt.BorderLayout;
// // import java.util.*;

// // import javax.swing.JButton;
// // import javax.swing.JFrame;
// // import javax.swing.WindowConstants;
// // import java.awt.event.ActionListener;
// // import java.awt.event.ActionEvent;

// // import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
// // import org.apache.commons.math3.util.FastMath;
// // import rvo2.Goal;
// // import rvo2.Simulator;

// // //adding library for chart
// // import java.awt.BasicStroke;
// // import java.awt.RenderingHints;
// // import java.awt.Stroke;

// // /**
// //  * TC8: Two intersections with eight possible flows.
// //  *
// //  * Layout mapping: A=0, B=1, C=2, D=3, E=4, F=5, G=6, H=7, I=8. B and F are
// //  * intersections. Agents can spawn from A, D, or H and exit at E, G, or I.
// //  *
// //  * This version includes the two missing Setting 1 features: 1) dynamic
// //  * BUSY/FREE exit-state logic, and 2) custom runSimulation() with a
// //  * trajectory-display toggle button.
// //  */
// // public class TestCase08_setting2_TESTING extends BaseTrafficScenario2_TESTING {

// //     private static final long serialVersionUID = 1L;

// //     //======================PARAMETERS==========================
// //     public TestCase08_setting2_TESTING() {
// //         scenarioName = "TC8: Two Intersections - 6 Rules";
// //         trafficType = "2-intersection-6-rules";
// //         totalAgents = 64;
// //     }

// //     // Counts spawned agents and helps rotate through shuffled uniform flows.
// //     private int spawnIndex = 0;

// //     // Temporary source/destination sequence used by the uniform flow scheduler.
// //     private List<int[]> flowOrder = new ArrayList<>();

// //     // Dynamic exit state copied from Setting 1. BUSY means agents slow down near exits.
// //     private boolean isBusy = false;

// //     // Use real wall-clock time for BUSY/FREE switching.
// //     // Do NOT use Simulator.instance.getGlobalTime() here because the simulation
// //     // advances once per frame, so 4-7 simulation steps can pass in less than
// //     // one real second when the UI runs at 60 FPS.
// //     //FOR CURVE CODE
// //     private static final int TURN_SEGMENTS = 6;
// //     private static final double INTERSECTION_OFFSET_RATIO = 0.10;
// //     private static final double ARC_RADIUS_RATIO = 1.0;
// //     private static final double CONNECTOR_STEP_RATIO = 0.45;

// //     private double roadGap = 50.0;

// //     private DijkstraAlgorithm dijkstra;
// //     private int graphA;
// //     private int graphD;
// //     private int graphE;
// //     private int graphG;
// //     private int graphH;
// //     private int graphI;

// //     private enum CornerType {
// //         NW, NE, SE, SW
// //     }

// //     private static class IntersectionNodes {

// //         int westAnchor;
// //         int southAnchor;
// //         int northAnchor;
// //         int eastAnchor;

// //         List<Integer> westToNorthArc = new ArrayList<>();
// //         List<Integer> southToEastArc = new ArrayList<>();

// //         List<Integer> straightWestToEast = new ArrayList<>();
// //         List<Integer> straightSouthToNorth = new ArrayList<>();
// //     }

// //     private long nextSwitchWallTimeMs = 0L;
// //     private static final long MIN_STATUS_DURATION_MS = 4000L;
// //     private static final long MAX_STATUS_DURATION_MS = 7000L;

// //     private static final double DETECTION_RADIUS = 60.0;
// //     private static final double MIN_SPEED_FACTOR = 0.15;
// //     private int throughExitCount = 0;
// //     private int slowExitCount = 0;

// //     // Runtime trajectory display controlled by the button in runSimulation().
// //     private boolean showPathsDuringSimulation = false;
// //     private JButton togglePathButton;

// //     // Pause/Resume simulation flag
// //     private boolean simulationPaused = false;
// //     private JButton pauseResumeButton;
// //     int leftToTop1 = 0;
// //     int leftToTop2 = 0;
// //     int leftToRight = 0;
// //     int bottom1ToTop1 = 0;
// //     int bottom1ToTop2 = 0;
// //     int bottom1ToRight = 0;
// //     int bottom2ToTop2 = 0;
// //     int bottom2ToRight = 0;
// //     double time;
// //     double startTime = 0;
// //     double endTime = 0;
// //     static Random rand = new Random();
// //     String disType = "Weibull Distribution";
// //     int[] sources = {0, 3, 7}; // Added 7 for second bottom
// //     int[] destinations = {4, 6, 8}; // Added 6 and 8 for second intersection
// //     private List<Vector2D> nodePoints = new ArrayList<>();
// //     int spawnedAgents = 0;
// //     // spawn interval settings
// //     private DISTRIBUTION_TYPE distributionType = DISTRIBUTION_TYPE.Weibull;
// //     private double meanSpawnInterval = 60.0;
// //     private double stdSpawnInterval = 15.0;
// //     private double minSpawnInterval = 15.0;
// //     private double maxSpawnInterval = 120.0;
// //     private double randomMinSpawnInterval = 20.0;
// //     private double randomMaxSpawnInterval = 100.0;
// //     double spawnInterval = meanSpawnInterval;
// //     double lastSpawnTime = 0;
// //     private Graph graph;
// //     private java.util.List<Double> timeHistory = new java.util.ArrayList<>();
// //     private java.util.List<Integer> agentHistory = new java.util.ArrayList<>();
// //     double intialMillis = System.currentTimeMillis();

// //     // Added positions for nodes 1 and 5
// //     private Vector2D node1Pos;
// //     private Vector2D node5Pos;
// //     Map<Integer, Agent> agents = new HashMap<>();

// //     //adding fields for chart
// //     // private final List<Long> spawnTimesMs = new ArrayList<>();
// //     // private final List<Long> spawnIntervalsMs = new ArrayList<>();
// //     // private final Object spawnChartLock = new Object();
// //     // private long previousSpawnMs = -1L;
// //     private final List<Double> spawnTimesSec = new ArrayList<>();
// //     private final List<Double> spawnIntervalsSec = new ArrayList<>();
// //     private final Object spawnChartLock = new Object();

// //     //adding field for spawn rules
// // //   private final double[] spawnCycle = {10, 30, 50, 30};
// //     //adding type for distribution
// //     enum DISTRIBUTION_TYPE {
// //         Uniformed, Normal, Randomed, Weibull, Fixed
// //     }

// // // This class saves metadata of Agents
// //     class Agent {

// //         int id;
// //         double startTime;
// //         double endTime;
// //         int flow;
// //         double spawnProb;

// //         public Agent(
// //                 int id,
// //                 double startTime,
// //                 double endTime,
// //                 int flow,
// //                 double spawnProb
// //         ) {
// //             this.id = id;
// //             this.startTime = startTime;
// //             this.endTime = endTime;
// //             this.flow = flow;
// //             this.spawnProb = spawnProb;
// //         }

// //         public double getDuration() {
// //             return endTime - startTime;
// //         }
// //     }

// //     private int addVertex(List<Vertex> nodes, String id, Vector2D p) {
// //         int index = nodes.size();
// //         Vertex v = new Vertex(id, index, 0, id, p, p, 0);
// //         nodes.add(v);
// //         return index;
// //     }

// //     private void addDirectedEdge(List<Edge> edges, List<Vertex> nodes, int from, int to) {
// //         Vector2D a = nodes.get(from).getVertex1();
// //         Vector2D b = nodes.get(to).getVertex1();
// //         double dist = a.distance(b);

// //         edges.add(new Edge(
// //                 "E_" + from + "_" + to + "_" + edges.size(),
// //                 nodes.get(from),
// //                 nodes.get(to),
// //                 dist
// //         ));
// //     }

// //     private void addPathEdges(List<Edge> edges, List<Vertex> nodes, List<Integer> path) {
// //         for (int i = 0; i < path.size() - 1; i++) {
// //             addDirectedEdge(edges, nodes, path.get(i), path.get(i + 1));
// //         }
// //     }

// //     private Vector2D lerp(Vector2D a, Vector2D b, double t) {
// //         return new Vector2D(
// //                 a.getX() + (b.getX() - a.getX()) * t,
// //                 a.getY() + (b.getY() - a.getY()) * t
// //         );
// //     }

// //     private List<Vector2D> generateArcPoints(
// //             Vector2D center,
// //             double radius,
// //             double startAngleDeg,
// //             double endAngleDeg,
// //             int segments
// //     ) {
// //         List<Vector2D> points = new ArrayList<>();

// //         double startRad = Math.toRadians(startAngleDeg);
// //         double endRad = Math.toRadians(endAngleDeg);

// //         for (int i = 0; i <= segments; i++) {
// //             double t = (double) i / segments;
// //             double angle = startRad + (endRad - startRad) * t;

// //             double x = center.getX() + radius * Math.cos(angle);
// //             double y = center.getY() - radius * Math.sin(angle); // đổi dấu ở đây

// //             points.add(new Vector2D(x, y));
// //         }

// //         return points;
// //     }

// //     private List<Integer> createArcVertexChain(
// //             String prefix,
// //             Vector2D intersectionCenter,
// //             CornerType corner,
// //             List<Vertex> nodes
// //     ) {
// //         double laneOffset = roadGap * INTERSECTION_OFFSET_RATIO;
// //         double radius = laneOffset * ARC_RADIUS_RATIO;

// //         Vector2D arcCenter;
// //         double startAngleDeg;
// //         double endAngleDeg;

// //         switch (corner) {
// //             case NW:
// //                 arcCenter = new Vector2D(
// //                         intersectionCenter.getX() - laneOffset,
// //                         intersectionCenter.getY() - laneOffset
// //                 );
// //                 startAngleDeg = 180;
// //                 endAngleDeg = 90;
// //                 break;

// //             case NE:
// //                 arcCenter = new Vector2D(
// //                         intersectionCenter.getX() + laneOffset,
// //                         intersectionCenter.getY() - laneOffset
// //                 );
// //                 startAngleDeg = 180;
// //                 endAngleDeg = 90;
// //                 break;

// //             case SE:
// //                 arcCenter = new Vector2D(
// //                         intersectionCenter.getX() + laneOffset,
// //                         intersectionCenter.getY() + laneOffset
// //                 );
// //                 startAngleDeg = 270;
// //                 endAngleDeg = 360;
// //                 break;

// //             case SW:
// //                 arcCenter = new Vector2D(
// //                         intersectionCenter.getX() - laneOffset,
// //                         intersectionCenter.getY() + laneOffset
// //                 );
// //                 startAngleDeg = 0;
// //                 endAngleDeg = 270;
// //                 break;

// //             default:
// //                 return Collections.emptyList();
// //         }

// //         List<Vector2D> arcPoints = generateArcPoints(
// //                 arcCenter,
// //                 radius,
// //                 startAngleDeg,
// //                 endAngleDeg,
// //                 TURN_SEGMENTS
// //         );

// //         List<Integer> arcIds = new ArrayList<>();
// //         for (int i = 0; i < arcPoints.size(); i++) {
// //             int id = addVertex(nodes, prefix + "_ARC_" + corner + "_" + i, arcPoints.get(i));
// //             arcIds.add(id);
// //         }

// //         return arcIds;
// //     }

// //     private List<Integer> createStraightConnectorChain(
// //             String prefix,
// //             Vector2D from,
// //             Vector2D to,
// //             int segments,
// //             List<Vertex> nodes
// //     ) {
// //         List<Integer> ids = new ArrayList<>();
// //         for (int i = 1; i <= segments; i++) {
// //             double t = (double) i / (segments + 1);
// //             Vector2D p = lerp(from, to, t);
// //             int id = addVertex(nodes, prefix + "_MID_" + i, p);
// //             ids.add(id);
// //         }
// //         return ids;
// //     }

// //     private IntersectionNodes buildIntersectionSubgraph(
// //             String prefix,
// //             Vector2D center,
// //             List<Vertex> nodes,
// //             List<Edge> edges
// //     ) {
// //         IntersectionNodes result = new IntersectionNodes();

// //         double laneOffset = roadGap * INTERSECTION_OFFSET_RATIO;
// //         double connectorStep = roadGap * CONNECTOR_STEP_RATIO;

// //         Vector2D westAnchorPos = new Vector2D(center.getX() - connectorStep, center.getY());
// //         Vector2D southAnchorPos = new Vector2D(center.getX(), center.getY() + connectorStep);
// //         Vector2D northAnchorPos = new Vector2D(center.getX(), center.getY() - connectorStep);
// //         Vector2D eastAnchorPos = new Vector2D(center.getX() + connectorStep, center.getY());

// //         result.westAnchor = addVertex(nodes, prefix + "_W", westAnchorPos);
// //         result.southAnchor = addVertex(nodes, prefix + "_S", southAnchorPos);
// //         result.northAnchor = addVertex(nodes, prefix + "_N", northAnchorPos);
// //         result.eastAnchor = addVertex(nodes, prefix + "_E", eastAnchorPos);

// //         // turning arcs actually used in your scenario
// //         result.westToNorthArc = createArcVertexChain(prefix + "_WN", center, CornerType.NW, nodes);
// //         result.southToEastArc = createArcVertexChain(prefix + "_SE", center, CornerType.SE, nodes);

// //         // straight connectors
// //         result.straightWestToEast = createStraightConnectorChain(
// //                 prefix + "_WE",
// //                 westAnchorPos,
// //                 eastAnchorPos,
// //                 2,
// //                 nodes
// //         );

// //         result.straightSouthToNorth = createStraightConnectorChain(
// //                 prefix + "_SN",
// //                 southAnchorPos,
// //                 northAnchorPos,
// //                 2,
// //                 nodes
// //         );

// //         // W -> N turn
// //         {
// //             List<Integer> path = new ArrayList<>();
// //             path.add(result.westAnchor);
// //             path.addAll(result.westToNorthArc);
// //             path.add(result.northAnchor);
// //             addPathEdges(edges, nodes, path);
// //         }

// //         // S -> E turn
// //         {
// //             List<Integer> path = new ArrayList<>();
// //             path.add(result.southAnchor);
// //             path.addAll(result.southToEastArc);
// //             path.add(result.eastAnchor);
// //             addPathEdges(edges, nodes, path);
// //         }

// //         // W -> E straight
// //         {
// //             List<Integer> path = new ArrayList<>();
// //             path.add(result.westAnchor);
// //             path.addAll(result.straightWestToEast);
// //             path.add(result.eastAnchor);
// //             addPathEdges(edges, nodes, path);
// //         }

// //         // S -> N straight
// //         {
// //             List<Integer> path = new ArrayList<>();
// //             path.add(result.southAnchor);
// //             path.addAll(result.straightSouthToNorth);
// //             path.add(result.northAnchor);
// //             addPathEdges(edges, nodes, path);
// //         }

// //         return result;
// //     }

// //     private void assignPathFromGraph(Goal goal, Graph graph, int src, int dst) {
// //         if (src == dst) {
// //             return;
// //         }

// //         LinkedList<Vertex> path = getShortestPath(
// //                 dijkstra,
// //                 graph.getVertexes().get(src),
// //                 graph.getVertexes().get(dst)
// //         );

// //         if (path == null || path.isEmpty()) {
// //             return;
// //         }

// //         for (int i = 1; i < path.size(); i++) {
// //             goal.add(translateCoordinateVector(path.get(i).getVertex1()));
// //         }
// //     }
// //     //adding function to record spawn time and interval for chart
// //     // private void recordSpawnEvent(long nowMs) {
// //     //     synchronized (spawnChartLock) {
// //     //         spawnTimesMs.add(nowMs);

// //     //         if (previousSpawnMs < 0) {
// //     //             spawnIntervalsMs.add(0L);   // first point has no previous interval
// //     //         } else {
// //     //             spawnIntervalsMs.add(nowMs - previousSpawnMs);
// //     //         }
// //     //         previousSpawnMs = nowMs;
// //     //     }
// //     // }
// //     private void recordSpawnEvent(double simTimeSec, double usedIntervalSec) {
// //         synchronized (spawnChartLock) {
// //             spawnTimesSec.add(simTimeSec);
// //             spawnIntervalsSec.add(usedIntervalSec);
// //         }
// //     }

// //     /**
// //      * Toggle the exit state between BUSY and FREE using real wall-clock time.
// //      *
// //      * This prevents BUSY/FREE from flashing too quickly. The simulator global
// //      * time advances every doStep(), and runSimulation() calls doStep() near 60
// //      * times per real second. Therefore, a 4-7 simulation-step interval would
// //      * only last around 0.07-0.12 real seconds.
// //      */
// //     private void updateTrafficState() {
// //         long currentWallTimeMs = System.currentTimeMillis();

// //         // First call: schedule the first status change but keep the initial state.
// //         if (nextSwitchWallTimeMs == 0L) {
// //             long durationMs = MIN_STATUS_DURATION_MS
// //                     + (long) (rand.nextDouble() * (MAX_STATUS_DURATION_MS - MIN_STATUS_DURATION_MS));
// //             nextSwitchWallTimeMs = currentWallTimeMs + durationMs;
// //             return;
// //         }

// //         if (currentWallTimeMs >= nextSwitchWallTimeMs) {
// //             isBusy = !isBusy;

// //             long durationMs = MIN_STATUS_DURATION_MS
// //                     + (long) (rand.nextDouble() * (MAX_STATUS_DURATION_MS - MIN_STATUS_DURATION_MS));
// //             nextSwitchWallTimeMs = currentWallTimeMs + durationMs;

// //             System.out.println("Traffic State Changed: " + (isBusy ? "BUSY" : "FREE"));
// //         }
// //     }

// //     /**
// //      * Convert a source/destination pair into the internal flow id.
// //      */
// //     private int getFlowId(int src, int dst) {
// //         if (src == 0 && dst == 4) {
// //             return 0; // A-B-E
// //         }
// //         if (src == 0 && dst == 8) {
// //             return 1; // A-B-F-I
// //         }
// //         if (src == 0 && dst == 6) {
// //             return 2; // A-B-F-G
// //         }
// //         if (src == 3 && dst == 4) {
// //             return 3; // D-B-E
// //         }
// //         if (src == 3 && dst == 8) {
// //             return 4; // D-B-F-I
// //         }
// //         if (src == 3 && dst == 6) {
// //             return 5; // D-B-F-G
// //         }
// //         if (src == 7 && dst == 8) {
// //             return 6; // H-F-I
// //         }
// //         if (src == 7 && dst == 6) {
// //             return 7; // H-F-G
// //         }
// //         return 0;
// //     }

// //     /**
// //      * Convert internal flow id to a readable route label for the dashboard.
// //      */
// //     private String getFlowName(int flow) {
// //         switch (flow) {
// //             case 0:
// //                 return "A-B-E";
// //             case 1:
// //                 return "A-F-I";
// //             case 2:
// //                 return "A-F-G";
// //             case 3:
// //                 return "D-B-E";
// //             case 4:
// //                 return "D-F-I";
// //             case 5:
// //                 return "D-F-G";
// //             case 6:
// //                 return "H-F-I";
// //             case 7:
// //                 return "H-F-G";
// //             default:
// //                 return "Unknown";
// //         }
// //     }

// //     //This functions will return the src/dst base on Normal Distribution
// //     public static int chooseByNormal(int[] options, double mean, double std) {
// //         double arr[] = {0, 0, 0};
// //         double gaussian, value;
// //         for (int i = 0; i < 3; i++) {
// //             gaussian = rand.nextGaussian();
// //             value = mean + std * gaussian;
// //             value = Math.max(0, Math.min(1, value));
// //             arr[i] = value;
// //         }
// //         double max = -1;
// //         int index = 0;
// //         for (int i = 0; i < 3; i++) {
// //             if (max < arr[i]) {
// //                 max = arr[i];
// //                 index = i;
// //             }
// //         }
// //         return options[index];
// //     }

// //     /**
// //      * Centralized spawn-interval policy.
// //      *
// //      * The selected distribution controls only the time between spawns. The
// //      * source/destination selection is handled inside spawnOneAgent().
// //      */
// //     private double getNextSpawnInterval() {
// //         double next;

// //         switch (distributionType) {
// //             case Normal:
// //                 disType = "Normal Distribution";
// //                 next = meanSpawnInterval + stdSpawnInterval * rand.nextGaussian();
// //                 break;

// //             case Randomed:
// //                 disType = "Random Distribution";
// //                 double r = rand.nextDouble();
// //                 // sqrt(r) gives a non-uniform random interval biased toward larger values.
// //                 next = randomMinSpawnInterval + (randomMaxSpawnInterval - randomMinSpawnInterval) * Math.sqrt(r);
// //                 break;

// //             case Weibull:
// //                 disType = "Weibull Distribution";
// //                 // The staged Weibull-like profile is refined in setPreferredVelocities().
// //                 next = 75.0;
// //                 break;

// //             case Fixed:
// //                 disType = "Fixed Distribution";
// //                 next = meanSpawnInterval;
// //                 break;

// //             case Uniformed:
// //             default:
// //                 disType = "Uniform Distribution";
// //                 next = minSpawnInterval + rand.nextDouble() * (maxSpawnInterval - minSpawnInterval);
// //                 break;
// //         }

// //         return Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
// //     }

// //     //This function helps create the 2 edges A-->B-->C 
// //     private void createEdge(int indexA, int indexB, int indexC, java.util.List<algorithms.dijkstra.model.Vertex> nodes, java.util.List<algorithms.dijkstra.model.Edge> edges) {
// //         edges.add(new algorithms.dijkstra.model.Edge(
// //                 "E_" + indexA + "_" + indexB,
// //                 nodes.get(indexA),
// //                 nodes.get(indexB),
// //                 nodes.get(indexA).getVertex1().distance(nodes.get(indexB).getVertex1())
// //         ));
// //         edges.add(new algorithms.dijkstra.model.Edge(
// //                 "E_" + indexB + "_" + indexC,
// //                 nodes.get(indexB),
// //                 nodes.get(indexC),
// //                 nodes.get(indexB).getVertex1().distance(nodes.get(indexC).getVertex1())
// //         ));

// //     }

// //     //This functions shuffleflows for UniformDistribution
// //     private void shuffleFlows(Random rand) {

// //         flowOrder.clear();

// //         flowOrder.add(new int[]{0, 4});
// //         flowOrder.add(new int[]{0, 8});
// //         flowOrder.add(new int[]{0, 6});
// //         flowOrder.add(new int[]{3, 4});
// //         flowOrder.add(new int[]{3, 8});
// //         flowOrder.add(new int[]{3, 6});
// //         flowOrder.add(new int[]{7, 6});
// //         flowOrder.add(new int[]{7, 8});

// //         Collections.shuffle(flowOrder, rand);
// //     }
// //     //This is agents defined color
// //     private static final Color[] FLOW_COLORS = new Color[]{
// //         Color.BLACK, // flow 0
// //         Color.RED, // flow 1
// //         Color.ORANGE, // flow 2
// //         Color.YELLOW, // flow 3
// //         Color.GREEN, // flow 4
// //         Color.CYAN, // flow 5
// //         Color.BLUE, // flow 6
// //         new Color(128, 0, 128) // flow 7 (PURPLE)
// //     };
// //     //This function will spawn one agent with defined Dst and Src

// //     private void spawnOneAgent(
// //             Graph graph,
// //             double[][] behavior,
// //             Random rand,
// //             double gap
// //     ) {
// //         int k = Simulator.instance.getNumAgents();

// //         // //adding x for chart
// //         // long nowMs = System.currentTimeMillis();
// //         // // record chart data
// //         // recordSpawnEvent(nowMs);
// //         // Update agents information
// //         Agent agent = agents.get(k);
// //         if (agent == null) {
// //             agent = new Agent(k, 0, 0, 0, 0);
// //             agents.put(k, agent);
// //         }
// //         agent.startTime = System.currentTimeMillis();
// //         // This is Spawn Location Distribution
// //         //--------------------------------
// //         //        disType = "Normal Distribution";
// //         int src, dst;
// //         int logicalSrc, logicalDst;
// //         if (distributionType == DISTRIBUTION_TYPE.Normal) {
// // //                     src = chooseByNormal(sources, 0.5, 1);
// // //                    dst = chooseByNormal(destinations, 0.5, 1);
// // //                    if(src==7 && dst==4) {
// // //                    	double gaussian = rand.nextGaussian();
// // //                        double value = 0.5 + 1 * gaussian;
// // //                    	int id = ((Math.max(0, Math.min(1, value)))>0.5 ?1:2);
// // //                    	dst = destinations[id];
// // //                    }

// //             logicalSrc = chooseByNormal(new int[]{0, 3, 7}, 0.5, 1);
// //             logicalDst = chooseByNormal(new int[]{4, 6, 8}, 0.5, 1);

// //             if (logicalSrc == 7 && logicalDst == 4) {
// //                 double gaussian = rand.nextGaussian();
// //                 double value = 0.5 + gaussian;
// //                 int id = ((Math.max(0, Math.min(1, value))) > 0.5 ? 1 : 2);
// //                 logicalDst = new int[]{4, 6, 8}[id];
// //             }

// //             src = logicalSrc;
// //             dst = logicalDst;
// //         } //        disType = "Random Distribution";
// //         else if (distributionType == DISTRIBUTION_TYPE.Randomed || distributionType == DISTRIBUTION_TYPE.Weibull) {
// // //            	  src = sources[rand.nextInt(3)];
// // //            	  dst = destinations[rand.nextInt(3)];
// // //                if(src==7 && dst==4) {
// // //            dst = destinations[rand.nextInt(2)+1];
// // //              }
// //             int[] logicalSources = {0, 3, 7};
// //             int[] logicalDestinations = {4, 6, 8};

// //             logicalSrc = logicalSources[rand.nextInt(3)];
// //             logicalDst = logicalDestinations[rand.nextInt(3)];

// //             if (logicalSrc == 7 && logicalDst == 4) {
// //                 logicalDst = logicalDestinations[rand.nextInt(2) + 1];
// //             }

// //             src = logicalSrc;
// //             dst = logicalDst;
// //         } //        disType = "Uniform Distribution";
// //         else {
// //             if (spawnIndex % 8 == 0) {
// //                 shuffleFlows(rand);
// //             }
// // //            int[] flow = flowOrder.get(spawnIndex % 8);
// // //
// // //            src = flow[0];
// // //            dst = flow[1];
// // //
// //             int[] flow = flowOrder.get(spawnIndex % 8);

// //             logicalSrc = flow[0];
// //             logicalDst = flow[1];

// //             src = logicalSrc;
// //             dst = logicalDst;
// //         }

// //         //--------------------------------
// //         disType = distributionType.name() + " Distribution";

// //         spawnIndex++;
// // //NEW
// // //        int graphSrc = src;
// // //        int graphDst = dst;
// // //
// // //        for (Vertex v : graph.getVertexes()) {
// // //            if (v.getName().equals("A") && src == 0) graphSrc = v.getId();
// // //            if (v.getName().equals("D") && src == 3) graphSrc = v.getId();
// // //            if (v.getName().equals("H") && src == 7) graphSrc = v.getId();
// // //
// // //            if (v.getName().equals("E") && dst == 4) graphDst = v.getId();
// // //            if (v.getName().equals("G") && dst == 6) graphDst = v.getId();
// // //            if (v.getName().equals("I") && dst == 8) graphDst = v.getId();
// // //        }
// //         int graphSrc = logicalSrc;
// //         int graphDst = logicalDst;

// //         if (logicalSrc == 0) {
// //             graphSrc = graphA; 
// //         }else if (logicalSrc == 3) {
// //             graphSrc = graphD; 
// //         }else if (logicalSrc == 7) {
// //             graphSrc = graphH;
// //         }

// //         if (logicalDst == 4) {
// //             graphDst = graphE; 
// //         }else if (logicalDst == 6) {
// //             graphDst = graphG; 
// //         }else if (logicalDst == 8) {
// //             graphDst = graphI;
// //         }

// //         // Update metadata for agents
// // //        agent.flow = getFlowId(src, dst);
// //         agent.flow = getFlowId(logicalSrc, logicalDst);
// //         if (logicalSrc == 0 && logicalDst == 4) {
// //             leftToTop1++;
// //             agent.flow = 0;
// //         }
// //         if (logicalSrc == 0 && logicalDst == 8) {
// //             leftToTop2++;
// //             agent.flow = 1;
// //         }
// //         if (logicalSrc == 0 && logicalDst == 6) {
// //             leftToRight++;
// //             agent.flow = 2;
// //         }
// //         if (logicalSrc == 3 && logicalDst == 4) {
// //             bottom1ToTop1++;
// //             agent.flow = 3;
// //         }
// //         if (logicalSrc == 3 && logicalDst == 6) {
// //             bottom1ToRight++;
// //             agent.flow = 5;
// //         }
// //         if (logicalSrc == 3 && logicalDst == 8) {
// //             bottom1ToTop2++;
// //             agent.flow = 4;
// //         }
// //         if (logicalSrc == 7 && logicalDst == 8) {
// //             agent.flow = 6;
// //             bottom2ToTop2++;
// //         }
// //         if (logicalSrc == 7 && logicalDst == 6) {
// //             bottom2ToRight++;
// //             agent.flow = 7;
// //         }
// //         int behaviorIndex = rand.nextInt(7);
// //         System.out.println("\n=== K: " + k + " ===");
// // //
// // //        Simulator.instance.addAgent(
// // //                translateCoordinateVector(graph.getVertexes().get(src).getVertex1())
// // //        );
// //         Simulator.instance.addAgent(
// //                 translateCoordinateVector(graph.getVertexes().get(graphSrc).getVertex1())
// //         );
// //         Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
// //         Simulator.instance.setAgentMaxNeighbors(
// //                 k,
// //                 (int) behavior[behaviorIndex][1]
// //         );
// //         Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);

// //         double radius = gap * 0.21;
// //         Simulator.instance.setAgentRadius(k, radius);
// //         Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);
// // //
// // //        Goal goal = new Goal(
// // //                translateCoordinateVector(graph.getVertexes().get(dst).getVertex1())
// // //        );
// //         Goal goal = new Goal(
// //                 translateCoordinateVector(graph.getVertexes().get(graphDst).getVertex1())
// //         );

// //         goal.setColor(getColor());
// //         goal.setFlag(1);
// // //        goal.setSource(src);
// // //        goal.setDestination(dst);

// //         goal.setSource(logicalSrc);
// //         goal.setDestination(logicalDst);
// // //        Vector2D Global_Goal = translateCoordinateVector(
// // //                graph.getVertexes().get(dst).getVertex1()
// // //        );
// //         Vector2D Global_Goal = translateCoordinateVector(
// //                 graph.getVertexes().get(graphDst).getVertex1()
// //         );
// //         goal.setGlobalGoal(Global_Goal);
// //         assignPathFromGraph(goal, graph, graphSrc, graphDst);

// //         goalsList.add(goal);
// //     }

// //     //This is a setup function which only run once to define the environment
// //     @Override
// //     protected void setupScenario() {

// //         startSearchTime = System.currentTimeMillis();
// //         intialMillis = System.currentTimeMillis();
// //         startTime = Simulator.instance.getGlobalTime();

// //         // Reset runtime state so rerunning the scenario does not keep old data.
// //         nodePoints.clear();
// //         timeHistory.clear();
// //         agentHistory.clear();
// //         agents.clear();
// //         isBusy = false;
// //         nextSwitchWallTimeMs = 0L;
// //         throughExitCount = 0;
// //         slowExitCount = 0;
// //         simulationPaused = false;

// //         leftToTop1 = 0;
// //         leftToTop2 = 0;
// //         leftToRight = 0;
// //         bottom1ToTop1 = 0;
// //         bottom1ToTop2 = 0;
// //         bottom1ToRight = 0;
// //         bottom2ToTop2 = 0;
// //         bottom2ToRight = 0;

// //         synchronized (spawnChartLock) {
// //             spawnTimesSec.clear();
// //             spawnIntervalsSec.clear();
// //         }

// //         //adding chart data reset
// //         // synchronized (spawnChartLock) {
// //         //     spawnTimesMs.clear();
// //         //     spawnIntervalsMs.clear();
// //         //     previousSpawnMs = -1L;
// //         // }
// //         Simulator.instance.setTimeStep(0.1);
// //         Simulator.instance.setAgentDefaults(80, 40, 38, 2.5, 3.55, 2.0,
// //                 org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO);

// //         double[][] behavior = createBehaviorMatrix();

// //         //=========OBSTACLES CREATION===============
// //         obsList1 = new java.util.ArrayList<Obs>();
// //         int rowsObs = 2;
// //         int colsObs = 3; // Changed from 2 to 3 for two intersections

// //         int decreScale = 1;

// //         double blockWidth = 450 / decreScale;  // pixels
// //         double blockHeight = 375 / decreScale; // pixels
// //         double gap = 50 / decreScale;          // pixels between blocks
// //         roadGap = gap;
// //         double totalWidth = colsObs * blockWidth + (colsObs - 1) * gap;
// //         double totalHeight = rowsObs * blockHeight + (rowsObs - 1) * gap;

// //         double startX = (w - totalWidth) / 2.0;
// //         double startY = (h - totalHeight) / 2.0;

// //         for (int r = 0; r < rowsObs; r++) {
// //             for (int c = 0; c < colsObs; c++) {
// //                 double x = startX + c * (blockWidth + gap);
// //                 double y = startY + r * (blockHeight + gap);

// //                 java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> coord1 = new java.util.ArrayList<>();
// //                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y));
// //                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y));
// //                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y + blockHeight));
// //                 coord1.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y + blockHeight));

// //                 java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> coord2 = new java.util.ArrayList<>();
// //                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y)));
// //                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x, y + blockHeight)));
// //                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y + blockHeight)));
// //                 coord2.add(translateCoordinateVector(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(x + blockWidth, y)));

// //                 Obs block = new Obs(coord1, coord2);
// //                 obsList1.add(block);
// //                 Simulator.instance.addObstacle(block.coord2);
// //             }
// //         }
// //         Simulator.instance.processObstacles();

// //         //=============NODE CREATION===============
// // //        java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> nodePoints = new java.util.ArrayList<>();
// //         //=====Coordinates for Nodes Creation=======
// //         double yMid = startY + blockHeight + gap / 2.0;
// //         double xLeft = startX;
// //         double xBetween = startX + blockWidth + gap / 2.0;
// //         double xRight = startX + blockWidth * 3 + gap * 2; // Updated for two intersections
// //         double xBetween2 = startX + blockWidth * 2 + gap * 1.5; // Corrected position for second intersection
// //         double yTop = startY;
// //         double yBottom = yTop + blockHeight * 2 + gap;
// //         //     4     8
// //         // 0   1  2  5   6
// //         //     3     7     
// //         //	  xLeft 
// //         //  	-------------------->    
// //         //yTop  |    4     8	
// //         //yMid  |0   1  2  5   6(xRight)          
// //         //yBot  |    3     7     
// //         //		v   xB    xB2
// //         //      
// //         // for visualization the node location
// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft, yMid)); //0

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yMid)); //1

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(((xBetween2 + xBetween) / 2), yMid)); //2

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yBottom)); //3

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yTop)); //4

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yMid)); //5

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight, yMid)); //6

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yBottom)); //7

// //         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yTop)); //8

// // //        java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
// // //        java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();
// // //        for (int i = 0; i < nodePoints.size(); i++) {
// // //            org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
// // //            algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex("node_" + i, i, 0, "node_" + i, p, p, 0);
// // //            nodes.add(v);
// // //        }
// // //
// // //        createEdge(0, 1, 2, nodes, edges);
// // //        createEdge(2, 5, 6, nodes, edges);
// // //        createEdge(3, 1, 4, nodes, edges);
// // //        createEdge(7, 5, 8, nodes, edges);
// // //
// // //        graph = new Graph(nodes, edges);
// //         List<Vertex> nodes = new ArrayList<>();
// //         List<Edge> edges = new ArrayList<>();

// //         // External endpoints only.
// //         // Keep original IDs for actual source/destination nodes:
// //         // 0=A, 3=D, 4=E, 6=G, 7=H, 8=I
// //         Map<Integer, Integer> logicalToGraph = new HashMap<>();

// //         logicalToGraph.put(0, addVertex(nodes, "A", nodePoints.get(0)));
// //         logicalToGraph.put(3, addVertex(nodes, "D", nodePoints.get(3)));
// //         logicalToGraph.put(4, addVertex(nodes, "E", nodePoints.get(4)));
// //         logicalToGraph.put(6, addVertex(nodes, "G", nodePoints.get(6)));
// //         logicalToGraph.put(7, addVertex(nodes, "H", nodePoints.get(7)));
// //         logicalToGraph.put(8, addVertex(nodes, "I", nodePoints.get(8)));

// //         // Build local subgraph around old node 1 and old node 5.
// //         IntersectionNodes inter1 = buildIntersectionSubgraph("B", nodePoints.get(1), nodes, edges);
// //         IntersectionNodes inter5 = buildIntersectionSubgraph("F", nodePoints.get(5), nodes, edges);

// //         // Connect external roads to local graph of intersection 1.
// //         addDirectedEdge(edges, nodes, logicalToGraph.get(0), inter1.westAnchor);   // A -> B_W
// //         addDirectedEdge(edges, nodes, logicalToGraph.get(3), inter1.southAnchor);  // D -> B_S

// //         addDirectedEdge(edges, nodes, inter1.northAnchor, logicalToGraph.get(4));  // B_N -> E

// //         // Road between intersection 1 and intersection 5:
// //         addDirectedEdge(edges, nodes, inter1.eastAnchor, inter5.westAnchor);        // B_E -> F_W

// //         // Connect external roads to local graph of intersection 5.
// //         addDirectedEdge(edges, nodes, logicalToGraph.get(7), inter5.southAnchor);   // H -> F_S
// //         addDirectedEdge(edges, nodes, inter5.northAnchor, logicalToGraph.get(8));   // F_N -> I
// //         addDirectedEdge(edges, nodes, inter5.eastAnchor, logicalToGraph.get(6));    // F_E -> G

// //         graph = new Graph(nodes, edges);
// //         dijkstra = new DijkstraAlgorithm(graph);

// //         // remap logical source/destination ids so spawn code still uses old labels
// //         Map<Integer, Integer> remap = new HashMap<>();
// //         remap.put(0, logicalToGraph.get(0));
// //         remap.put(3, logicalToGraph.get(3));
// //         remap.put(4, logicalToGraph.get(4));
// //         remap.put(6, logicalToGraph.get(6));
// //         remap.put(7, logicalToGraph.get(7));
// //         remap.put(8, logicalToGraph.get(8));

// //         // store graph-ready ids back into arrays used by spawning
// //         sources = new int[]{remap.get(0), remap.get(3), remap.get(7)};
// //         destinations = new int[]{remap.get(4), remap.get(6), remap.get(8)};

// //         System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());
// //         System.out.println("Auto-generated Graph edges: " + graph.getEdges().size());

// //         graphA = logicalToGraph.get(0);
// //         graphD = logicalToGraph.get(3);
// //         graphE = logicalToGraph.get(4);
// //         graphG = logicalToGraph.get(6);
// //         graphH = logicalToGraph.get(7);
// //         graphI = logicalToGraph.get(8);

// //         // Added: Store positions for painting
// //         node1Pos = translateCoordinateVector(nodePoints.get(1));
// //         node5Pos = translateCoordinateVector(nodePoints.get(5));
// //         for (int i = 0; i < totalAgents; i++) {
// //             double start = rand.nextDouble() * 10;
// //             double end = start + rand.nextDouble() * 10;
// //             int flow = rand.nextInt(8);
// //             double prob = rand.nextDouble();

// //             Agent agent = new Agent(i, start, end, flow, prob);
// //             agents.put(i, agent);
// //         }
// //         spawnOneAgent(graph, behavior, rand, gap);
// //         spawnedAgents++;

// //         // initialize dynamic interval after first spawn
// //         // spawnInterval = getNextSpawnInterval();
// //         recordSpawnEvent(Simulator.instance.getGlobalTime(), 0.0); // first spawn has no previous interval
// //         spawnInterval = getNextSpawnInterval();
// //         lastSpawnTime = Simulator.instance.getGlobalTime();

// //         System.out.println("Scenario initialized: " + scenarioName);
// //     }

// //     @Override
// //     protected void paintComponent(Graphics g) {
// //         super.paintComponent(g);

// //         Graphics2D g2 = (Graphics2D) g;
// //         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

// //         // Draw exit-status bars. Green = FREE, red = BUSY.
// //         if (nodePoints.size() >= 9) {
// //             double barGap = 50.0;
// //             double barThickness = 10.0;
// //             int[] exitNodes = {4, 6, 8};

// //             g2.setColor(isBusy ? Color.RED : Color.GREEN);
// //             for (int idx : exitNodes) {
// //                 Vector2D p = nodePoints.get(idx);
// //                 if (idx == 6) {
// //                     // Right exit G: vertical gate bar.
// //                     g2.fillRect((int) (p.getX() - 5), (int) (p.getY() - barGap / 2), (int) barThickness, (int) barGap);
// //                 } else {
// //                     // Top exits E and I: horizontal gate bars.
// //                     g2.fillRect((int) (p.getX() - barGap / 2), (int) (p.getY() - 5), (int) barGap, (int) barThickness);
// //                 }
// //             }
// //         }

// //         //	 Agent agent = agents.get(agentNo);
// //         if (Simulator.instance != null) {
// //             int numAgents = Math.min(Simulator.instance.getNumAgents(), goalsList.size());
// //             for (int agentNo = 0; agentNo < numAgents; agentNo++) {
// //                 Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
// //                 if (agentPos == null) {
// //                     continue;
// //                 }

// //                 Vector2D agentPos_cood = translateCoordinateBackVector(agentPos);
// //                 int radius = (int) Simulator.instance.getAgentRadius(agentNo);

// //                 if (goalsList.get(agentNo).getFlag() == 1) {
// //                     Agent agent = agents.get(agentNo);
// //                     int flow = agent.flow; // phải có hàm này
// //                     Color agentColor = FLOW_COLORS[flow % 8];
// //                     g2.setColor(agentColor);
// //                     g2.fillOval((int) agentPos_cood.getX() - 2, (int) agentPos_cood.getY() - 2, 4, 4);
// //                     // Draw oval instead of fill the Oval 
// // //                 g2.drawOval((int) agentPos_cood.getX() - radius, (int) agentPos_cood.getY() - radius,
// // //                         radius * 2, radius * 2);
// //                     g2.fillOval(
// //                             (int) agentPos_cood.getX() - radius,
// //                             (int) agentPos_cood.getY() - radius,
// //                             radius * 2,
// //                             radius * 2
// //                     );
// //                 }

// //                 // Draw trajectory while the toggle is ON, or always after the run ends.
// //                 if ((ended || showPathsDuringSimulation) && totalSteps < 10000) {
// //                     int pointSize = goalsList.get(agentNo).getaddMovingStepSize();
// //                     if (pointSize > 1) {
// //                         g2.setColor(goalsList.get(agentNo).getColor());
// //                         for (int i = 0; i < pointSize - 1; i += Math.max(1, pointSize / 50)) {
// //                             Vector2D curr = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(i));
// //                             Vector2D next = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(Math.min(i + 1, pointSize - 1)));
// //                             g2.drawLine((int) curr.getX(), (int) curr.getY(), (int) next.getX(), (int) next.getY());
// //                         }
// //                     }
// //                 }

// //             }
// //         }

// //         // Draw the circle at each intersection where priority flow algorithm will work  
// // //     int radius = 37;
// // //     g2.setColor(Color.GREEN);
// // //
// // //    //  Draw circle at node 1
// // //     if (node1Pos != null) {
// // //         g2.drawOval(
// // //            //  (int) node1Pos.getX() - radius,
// // //            //  (int) node1Pos.getY() - radius,
// // //             710 - radius,
// // //             540 - radius,
// // //             radius * 2,
// // //             radius * 2
// // //         );
// // //     }
// // //
// // //    //  Draw circle at node 5
// // //     if (node5Pos != null) {
// // //         g2.drawOval(
// // //             //  (int) node5Pos.getX() - radius,
// // //             //  (int) node5Pos.getY() - radius,
// // //             1210 - radius,
// // //             540 - radius,
// // //             radius * 2,
// // //             radius * 2
// // //         );
// // //     }
// //         // ===== Draw information on screen ==
// //         int startX = 20;
// //         int startY = 150;
// //         int circleSize = 12;
// //         int spacing = 20;
// //         g2.setColor(Color.BLACK);
// //         g2.drawString(disType, startX, startY - 50);

// //         g2.drawString("Simulation Time(s)", startX, startY - 30);

// //         g2.drawString("Agent's Color | Count | Time(s) " + String.format("%.2f", spawnInterval), startX, startY - 10);

// //         // Display dynamic exit status and exit counters.
// //         g2.setColor(isBusy ? Color.RED : new Color(0, 150, 0));
// //         g2.drawString("EXIT STATUS: " + (isBusy ? "BUSY" : "FREE"), startX + 170, startY - 70);
// //         g2.setColor(new Color(0, 120, 0));
// //         g2.drawString("Normal Exits: " + throughExitCount, startX + 170, startY - 50);
// //         g2.setColor(Color.RED);
// //         g2.drawString("Slow Exits: " + slowExitCount, startX + 170, startY - 35);
// //         g2.setColor(Color.BLACK);

// //         int[] flowCount = {
// //             leftToTop1,
// //             leftToTop2,
// //             leftToRight,
// //             bottom1ToTop1,
// //             bottom1ToTop2,
// //             bottom1ToRight,
// //             bottom2ToTop2,
// //             bottom2ToRight
// //         };
// //         int[] src_dst_count = {
// //             leftToTop1 + leftToTop2 + leftToRight,
// //             bottom1ToTop1 + bottom1ToTop2 + bottom1ToRight,
// //             bottom2ToTop2 + bottom2ToRight,
// //             leftToTop1 + bottom1ToTop1,
// //             leftToTop2 + bottom1ToTop2 + bottom2ToTop2,
// //             leftToRight + bottom1ToRight + bottom2ToRight
// //         };

// //         double[] avgTime = new double[8];
// //         int[] countFlow = new int[8];
// //         if (ended) {
// //             for (int i = 0; i < totalAgents; i++) {
// //                 Agent agent = agents.get(i);
// //                 if (agent.endTime > 0) {
// //                     int f = agent.flow;
// //                     avgTime[f] += agent.getDuration();
// //                     countFlow[f]++;
// //                 }
// //             }

// //             for (int i = 0; i < 8; i++) {
// //                 if (countFlow[i] > 0) {
// //                     avgTime[i] /= countFlow[i];
// //                 }
// //             }
// //             long endTime = System.currentTimeMillis();
// //             double time = (endTime - startSearchTime) * 0.001;

// //             g2.drawString(String.format("%.2f", time), startX + 110, startY - 30);

// //         }

// //         for (int i = 0; i < 8; i++) {
// //             int y = startY + i * spacing;

// //             g2.setColor(FLOW_COLORS[i]);
// //             g2.fillOval(startX, y - circleSize + 9, circleSize, circleSize);

// //             g2.setColor(Color.BLACK);
// //             String flowName = getFlowName(i);

// //             int col1X = startX + 20;
// //             int col2X = startX + 100;
// //             int col3X = startX + 140;
// //             if (ended && countFlow[i] > 0) {
// //                 g2.drawString(String.format("%.2f", avgTime[i] * 0.001), col3X, y + 9);
// //             } else {
// //                 g2.drawString("-", col3X, y + 9);
// //             }
// //             g2.drawString(flowName, col1X, y + 9);

// //             g2.drawString(String.valueOf(flowCount[i]), col2X, y + 9);

// //         }
// //         // ===== SOURCE / DESTINATION COUNT =====

// //         int startY2 = startY + 8 * spacing + 30;

// //         int colLeftX = startX;        // Source
// //         int colRightX = startX + 90; // Destination

// //         g2.setColor(Color.BLACK);
// //         g2.drawString("Source Count", colLeftX, startY2 - 20);
// //         g2.drawString("Destination Count", colRightX, startY2 - 20);

// //         // Source labels
// //         String[] sources = {"A", "D", "H"};
// //         // Destination labels
// //         String[] dests = {"E", "I", "G"};

// //         for (int i = 0; i < 3; i++) {
// //             int y = startY2 + i * spacing;

// //             // SOURCE
// //             g2.drawString(
// //                     sources[i] + ": " + src_dst_count[i],
// //                     colLeftX,
// //                     y
// //             );

// //             // DESTINATION
// //             g2.drawString(
// //                     dests[i] + ": " + src_dst_count[i + 3],
// //                     colRightX,
// //                     y
// //             );
// //         }

// //         //===== REALTIME GRAPH (FIXED SCALE) =====
// //         int graphX = startX + 15;
// //         int graphY = startY2 + 3 * spacing + 400;

// //         int graphWidth = 150;
// //         int graphHeight = 80;

// // //fixed range
// //         double maxTimeRange = 80.0;
// //         int maxAgentRange = 40;

// // //khung
// //         g2.setColor(Color.BLACK);
// //         g2.drawRect(graphX, graphY, graphWidth, graphHeight);

// // //label
// //         g2.drawString("Agents", graphX, graphY - 5);

// //         g2.drawString("0", graphX - 10, graphY + graphHeight);
// //         g2.drawString("40", graphX - 25, graphY + 10);

// //         g2.drawString("0", graphX, graphY + graphHeight + 15);
// //         g2.drawString("80", graphX + graphWidth - 20, graphY + graphHeight + 15);
// //         g2.drawString("Time(s)", graphX + graphWidth + 5, graphY + graphHeight + 7);

// // //vẽ line
// //         if (timeHistory.size() > 1) {
// //             g2.setColor(Color.BLUE);

// //             for (int i = 1; i < timeHistory.size(); i++) {
// //                 double t1 = timeHistory.get(i - 1);
// //                 double t2 = timeHistory.get(i);

// //                 int a1 = agentHistory.get(i - 1);
// //                 int a2 = agentHistory.get(i);

// //                 t1 = Math.min(t1, maxTimeRange);
// //                 t2 = Math.min(t2, maxTimeRange);

// //                 a1 = Math.min(a1, maxAgentRange);
// //                 a2 = Math.min(a2, maxAgentRange);

// //                 int x1 = graphX + (int) (t1 / maxTimeRange * graphWidth);
// //                 int x2 = graphX + (int) (t2 / maxTimeRange * graphWidth);

// //                 int y1 = graphY + graphHeight - (int) ((double) a1 / maxAgentRange * graphHeight);
// //                 int y2 = graphY + graphHeight - (int) ((double) a2 / maxAgentRange * graphHeight);

// //                 g2.drawLine(x1, y1, x2, y2);
// //             }
// //         }

// //         // ===== DRAW NODE LABELS =====
// //         if (nodePoints != null) {

// //             Graphics2D gText = (Graphics2D) g2.create();

// //             gText.setFont(new Font("Lexend", Font.BOLD, 24));
// //             gText.setColor(Color.BLACK);

// //             Random rand = new Random(42);

// //             for (int i = 0; i < nodePoints.size(); i++) {
// //                 Vector2D screenPos = nodePoints.get(i);

// //                 char label = (char) ('A' + i);

// //                 String text = String.valueOf(label);

// //                 FontMetrics fm = gText.getFontMetrics();
// //                 int textWidth = fm.stringWidth(text);
// //                 int textHeight = fm.getAscent();

// //                 int x = (int) screenPos.getX() - textWidth / 2;
// //                 int y = (int) screenPos.getY() + textHeight / 2;

// //                 gText.drawString(text, x, y);
// //             }

// //             gText.dispose();
// //         }

// //         //adding code to draw the chart
// //         int chartWidth = 210;
// //         int chartHeight = 320;
// //         int chartX = 10;
// //         int chartY = 425;
// //         // drawSpawnIntervalChart(g2, chartX, chartY, chartWidth, chartHeight);

// //         drawSpawnHistogram(g2, chartX, chartY, chartWidth, chartHeight);
// //     }

// // //adding method to draw the chart for spawn interval
// //     private void drawSpawnIntervalChart(Graphics2D g2, int x, int y, int width, int height) {
// //         List<Double> times;
// //         List<Double> intervals;

// //         synchronized (spawnChartLock) {
// //             times = new ArrayList<>(spawnTimesSec);
// //             intervals = new ArrayList<>(spawnIntervalsSec);
// //         }

// //         Graphics2D g = (Graphics2D) g2.create();
// //         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
// //         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

// //         // ===== outer panel =====
// //         g.setColor(Color.WHITE);
// //         g.fillRoundRect(x, y, width, height, 18, 18);
// //         g.setColor(Color.BLACK);
// //         g.setStroke(new BasicStroke(2f));
// //         g.drawRoundRect(x, y, width, height, 18, 18);

// //         // ===== title =====
// //         g.setFont(new Font("Arial", Font.BOLD, 15));
// //         String title = "Spawn Interval (Second)";
// //         FontMetrics titleFm = g.getFontMetrics();
// //         int titleX = x + (width - titleFm.stringWidth(title)) / 2;
// //         g.drawString(title, titleX, y + 38);

// //         // no data
// //         if (times.size() < 2) {
// //             g.setFont(new Font("Arial", Font.PLAIN, 12));
// //             g.drawString("Chưa đủ dữ liệu spawn", x + 20, y + 70);
// //             g.dispose();
// //             return;
// //         }

// //         // ===== plot area =====
// //         int leftPad = 44;
// //         int rightPad = 26;
// //         int topPad = 58;
// //         int bottomPad = 72;

// //         int px = x + leftPad;
// //         int py = y + topPad;
// //         int pw = width - leftPad - rightPad;
// //         int ph = height - topPad - bottomPad;

// //         g.setColor(new Color(235, 235, 242));
// //         g.fillRect(px, py, pw, ph);

// //         g.setColor(Color.BLACK);
// //         g.setStroke(new BasicStroke(2.2f));
// //         g.drawRect(px, py, pw, ph);

// //         double firstTime = times.get(0);
// //         double lastTime = times.get(times.size() - 1);

// //         double[] xSecs = new double[times.size()];
// //         double[] ySecs = new double[intervals.size()];

// // //        for (int i = 0; i < times.size(); i++) {
// // //            xSecs[i] = times.get(i) - firstTime;   // simulation elapsed time
// // //            ySecs[i] = intervals.get(i);  // planned/used spawn interval in seconds
// //         ////            if (xSecs[i] > maxX) {
// // ////                maxX = xSecs[i];
// // ////            }
// // ////            if (ySecs[i] > maxY) {
// // ////                maxY = ySecs[i];
// // ////            }
// // //        }

// // //        if (maxX <= 0) {
// // //            maxX = 1.0;
// // //        }
// // //        if (maxY <= 0) {
// // //            maxY = 1.0;
// // //        }

// //       double yTop = totalAgents / 8 + 5;
// // //        double yTop = Math.ceil(maxY / 5.0) * 5.0;
// // //        if (yTop < 5.0) {
// // //            yTop = 5.0;
// // //        }

// //         double yMid = yTop * 0.6;   // giống style mẫu: 1 đường giữa
// //         if (yMid < 1.0) {
// //             yMid = yTop / 2.0;
// //         }
// //         double minX = 15.0;
// //         double maxX = 120.0;

// //         double minY = 0.0;
// //         double maxY = yTop; // giữ nguyên logic của bạn
// //         // ===== dashed guide lines =====
// //         Stroke oldStroke = g.getStroke();
// //         Stroke dashed = new BasicStroke(
// //                 1.4f,
// //                 BasicStroke.CAP_BUTT,
// //                 BasicStroke.JOIN_BEVEL,
// //                 0,
// //                 new float[]{8f, 8f},
// //                 0
// //         );
// //         g.setStroke(dashed);
// //         g.setColor(Color.DARK_GRAY);

// //         int yTopPix = py + (int) ((1.0 - yTop / yTop) * ph);
// //         int yMidPix = py + (int) ((1.0 - yMid / yTop) * ph);

// //         g.drawLine(px, yTopPix, px + pw, yTopPix);
// //         g.drawLine(px, yMidPix, px + pw, yMidPix);

// //         g.setStroke(oldStroke);
// //         g.setColor(Color.BLACK);

// //         // ===== y labels =====
// //         g.setFont(new Font("Arial", Font.PLAIN, 13));
// //         g.drawString(String.format("%.0fs", yTop), x + 8, yTopPix + 5);
// //         g.drawString(String.format("%.0fs", yMid), x + 8, yMidPix + 5);
// //         g.drawString("0", x + 18, py + ph + 5);

// //         // ===== line + markers =====
// //         g.setColor(new Color(30, 110, 200));
// //         g.setStroke(new BasicStroke(2.5f));

// //         int prevX = -1;
// //         int prevY = -1;

// //         for (int i = 0; i < xSecs.length; i++) {
// //             int cx = px + (int) (((xSecs[i] - minX) / (maxX - minX)) * pw);
// //             int cy = py + ph - (int) ((ySecs[i] / yTop) * ph);

// //             if (prevX != -1) {
// //                 g.drawLine(prevX, prevY, cx, cy);
// //             }

// //             prevX = cx;
// //             prevY = cy;
// //         }

// //         // small round markers
// //         for (int i = 0; i < xSecs.length; i++) {
// //             int cx = px + (int) ((xSecs[i] / maxX) * pw);
// //             int cy = py + ph - (int) ((ySecs[i] / yTop) * ph);

// //             int r = 2;

// //             g.setColor(Color.RED);
// //             g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
// //         }

// //         // ===== current status text =====
// //         double currentElapsed = lastTime - firstTime;
// //         double currentInterval = intervals.get(intervals.size() - 1);

// //         g.setFont(new Font("Arial", Font.PLAIN, 12));
// //         g.setColor(Color.BLACK);
// //         String status = String.format("Current: %.1fs (Interval: %.1fs)", currentElapsed, currentInterval);
// //         g.drawString(status, x + 20, y + height - 22);

// //         g.dispose();
// //     }

// //     private void drawSpawnHistogram(Graphics2D g2, int x, int y, int width, int height) {
// //         List<Double> intervals;

// //         synchronized (spawnChartLock) {
// //             intervals = new ArrayList<>(spawnIntervalsSec);
// //         }

// //         Graphics2D g = (Graphics2D) g2.create();
// //         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
// //         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

// //         // ===== outer panel =====
// //         g.setColor(Color.WHITE);
// //         g.fillRoundRect(x, y, width, height, 18, 18);
// //         g.setColor(Color.BLACK);
// //         g.setStroke(new BasicStroke(2f));
// //         g.drawRoundRect(x, y, width, height, 18, 18);

// //         // ===== title =====
// //         g.setFont(new Font("Arial", Font.BOLD, 13));
// //         String title = "Spawn Interval Histogram";
// //         FontMetrics titleFm = g.getFontMetrics();
// //         int titleX = x + (width - titleFm.stringWidth(title)) / 2;
// //         g.drawString(title, titleX, y + 38);

// //         // bỏ điểm đầu tiên = 0.0 nếu có
// //         List<Double> values = new ArrayList<>();
// //         for (double v : intervals) {
// //             if (v > 0.0) {
// //                 values.add(v);
// //             }
// //         }

// //         if (values.size() < 2) {
// //             g.setFont(new Font("Arial", Font.PLAIN, 12));
// //             g.drawString("In Proccess", x + 20, y + 70);
// //             g.dispose();
// //             return;
// //         }

// //         // ===== plot area =====
// //         int leftPad = 44;
// //         int rightPad = 20;
// //         int topPad = 58;
// //         int bottomPad = 72;

// //         int px = x + leftPad;
// //         int py = y + topPad;
// //         int pw = width - leftPad - rightPad;
// //         int ph = height - topPad - bottomPad;

// //         g.setColor(new Color(235, 235, 242));
// //         g.fillRect(px, py, pw, ph);

// //         g.setColor(Color.BLACK);
// //         g.setStroke(new BasicStroke(2.2f));
// //         g.drawRect(px, py, pw, ph);

// //         // ===== histogram bins (FIXED RANGE) =====
// //         double minX = 15.0;
// //         double maxX = 120.0;
// //         int binCount = 8;

// //         double binSize = (maxX - minX) / binCount;
// //         int[] counts = new int[binCount];

// //         for (double v : values) {
// //             double clamped = Math.max(minX, Math.min(v, maxX));

// //             int bin = (int) ((clamped - minX) / binSize);
// //             if (bin >= binCount) {
// //                 bin = binCount - 1;
// //             }
// //             if (bin < 0) {
// //                 bin = 0;
// //             }

// //             counts[bin]++;
// //         }

// //         int maxCount = 1;
// //         for (int c : counts) {
// //             if (c > maxCount) {
// //                 maxCount = c;
// //             }
// //         }

// //         // ===== dashed middle guide =====
// //         Stroke oldStroke = g.getStroke();
// //         Stroke dashed = new BasicStroke(
// //                 1.2f,
// //                 BasicStroke.CAP_BUTT,
// //                 BasicStroke.JOIN_BEVEL,
// //                 0,
// //                 new float[]{8f, 8f},
// //                 0
// //         );
// //         g.setStroke(dashed);
// //         g.setColor(Color.DARK_GRAY);

// //         int yMidPix = py + ph / 2;
// //         g.drawLine(px, yMidPix, px + pw, yMidPix);

// //         g.setStroke(oldStroke);
// //         g.setColor(Color.BLACK);

// //         // ===== draw bars =====
// //         int gap = 4;
// //         int barWidth = Math.max(6, (pw - gap * (binCount + 1)) / binCount);

// //         for (int i = 0; i < binCount; i++) {
// //             int barHeight = (int) Math.round((counts[i] / (double) maxCount) * (ph - 10));
// //             int bx = px + gap + i * (barWidth + gap);
// //             int by = py + ph - barHeight;

// //             g.setColor(new Color(100, 149, 237));
// //             g.fillRect(bx, by, barWidth, barHeight);

// //             g.setColor(Color.BLACK);
// //             g.drawRect(bx, by, barWidth, barHeight);
// //         }

// //         // ===== y labels (count) =====
// //         g.setFont(new Font("Arial", Font.PLAIN, 12));
// //         g.drawString(String.valueOf(maxCount), x + 10, py + 5);
// //         g.drawString(String.valueOf(maxCount / 2), x + 10, yMidPix + 5);
// //         g.drawString("0", x + 18, py + ph + 5);

// //         // ===== x labels (FIXED) =====
// //         g.setFont(new Font("Arial", Font.PLAIN, 12));

// //         // min = 15
// //         g.drawString("15", px - 5, y + height - 40);

// //         // max = 120
// //         g.drawString("120", px + pw - 25, y + height - 40);

// //         // mid
// //         double midX = (minX + maxX) / 2.0;
// //         int midXPix = px + (int) (((midX - minX) / (maxX - minX)) * pw);
// //         g.drawString(String.valueOf((int) midX), midXPix - 10, y + height - 40);

// //         g.drawString("seconds", px + pw / 2 - 20, y + height - 20);

// //         // ===== summary =====
// //         double sum = 0.0;
// //         for (double v : values) {
// //             sum += v;
// //         }
// //         double mean = sum / values.size();

// //         g.setFont(new Font("Arial", Font.PLAIN, 12));
// //         String status = String.format("n=%d, mean=%.1fs", values.size(), mean);
// //         g.drawString(status, x + 20, y + height - 3);

// //         g.dispose();
// //     }

// //     @Override
// //     protected void updateVisualization() {
// //         if ((Simulator.instance.getGlobalTime() % 100) == 0) {
// //             System.out.println("Step: " + Simulator.instance.getGlobalTime()
// //                     + " -> Completed: " + agentCount + "/" + totalAgents);

// //         }

// //         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
// //             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
// //             if (goalsList.get(agentNo).getFlag() == 1) {
// //                 goalsList.get(agentNo).addMovingStep(agentPos);

// //                 if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) < 10.0) {
// //                     Agent agent = agents.get(agentNo);
// //                     if (isBusy) {
// //                         slowExitCount++;
// //                     } else {
// //                         throughExitCount++;
// //                     }
// //                     if (agent != null) {
// //                         agent.endTime = System.currentTimeMillis();
// //                     }

// //                     agentCount++;
// //                     long elapsed = System.currentTimeMillis() - startSearchTime;
// //                     goalsList.get(agentNo).setReachGoalms(elapsed);
// //                     goalsList.get(agentNo).setFlag(2);
// //                     sumTime += elapsed;

// //                     // Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
// //                     // Simulator.instance.setAgentRadius(agentNo, 0);
// //                     Simulator.instance.setAgentMaxSpeed(agentNo, 0.0);
// //                     Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);

// //                     Simulator.instance.setAgentRadius(agentNo, 0.0);
// //                     Simulator.instance.setAgentNeighborDistance(agentNo, 0.0);
// //                     Simulator.instance.setAgentMaxNeighbors(agentNo, 0);

// //                     Simulator.instance.setAgentPosition(agentNo, new Vector2D(-1e9, -1e9));
// //                 }
// //             }
// //         }
// //         int currentAgents = spawnedAgents - agentCount;

// //         timeHistory.add(time);
// //         agentHistory.add(currentAgents);

// //         // giới hạn size để không lag
// //         if (timeHistory.size() > 5000) {
// //             timeHistory.remove(0);
// //             agentHistory.remove(0);
// //         }
// //         repaint();
// //     }

// //     double smooth(double x) {
// //         return x * x * (3 - 2 * x);
// //     }

// //     @Override
// //     protected void setPreferredVelocities() {
// //         super.setPreferredVelocities();

// //         double[][] behavior = createBehaviorMatrix();
// //         double currentTime = Simulator.instance.getGlobalTime();
// //         int gap = 50;
// //         time = (System.currentTimeMillis() - intialMillis) * 0.001;

// //         // Keep the dynamic BUSY/FREE exit state synchronized with simulator time.
// //         updateTrafficState();

// //         // Spawn new agents according to the selected interval distribution.
// //         if (spawnedAgents < totalAgents && currentTime - lastSpawnTime >= spawnInterval) {
// //             double usedInterval = spawnInterval;

// //             spawnOneAgent(graph, behavior, rand, gap);
// //             spawnedAgents++;
// //             lastSpawnTime = currentTime;

// //             // Log the interval that was actually used for the histogram.
// //             recordSpawnEvent(currentTime, usedInterval);

// //             // Generate the interval for the next spawn.
// //             spawnInterval = getNextSpawnInterval();

// //             /*
// //              * Weibull-like staged arrival profile:
// //              * - early period: sparse arrivals,
// //              * - middle period: dense arrivals,
// //              * - final period: sparse arrivals again.
// //              */
// //             if (distributionType == DISTRIBUTION_TYPE.Weibull) {
// //                 time = (System.currentTimeMillis() - intialMillis) * 0.001;

// //                 if (time < 15) {
// //                     spawnInterval = 75;
// //                 } else if (time < 25) {
// //                     double t = (time - 20) / 15.0;
// //                     double s = smooth(t);
// //                     spawnInterval = 80 - 60 * s;
// //                 } else if (time < 50) {
// //                     double t = (time - 35) / 15.0;
// //                     double s = smooth(t);
// //                     spawnInterval = 20 + 60 * s;
// //                 } else {
// //                     spawnInterval = 100;
// //                 }

// //                 spawnInterval = Math.max(minSpawnInterval, Math.min(maxSpawnInterval, spawnInterval));
// //             }
// //         }

// //         int numAgents = Simulator.instance.getNumAgents();

// //         for (int agentNo = 0; agentNo < numAgents; agentNo++) {
// //             Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
// //             if (pref == null || pref.getNorm() < 1e-6) {
// //                 continue;
// //             }

// //             Goal goal = goalsList.get(agentNo);
// //             if (goal == null || goal.getFlag() != 1) {
// //                 continue;
// //             }

// //             /*
// //              * BUSY-gate slow down.
// //              * Final exits in TC8 are E(4), G(6), and I(8). When the exit status
// //              * is BUSY, agents inside DETECTION_RADIUS smoothly reduce speed.
// //              */
// //             if (isBusy && (goal.getDestination() == 4 || goal.getDestination() == 6 || goal.getDestination() == 8)) {
// //                 Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
// //                 Vector2D targetPos = goal.getGlobalGoal();
// //                 double distToGoal = targetPos.subtract(agentPos).getNorm();

// //                 if (distToGoal < DETECTION_RADIUS) {
// //                     double speedFactor = distToGoal / DETECTION_RADIUS;
// //                     speedFactor = Math.max(MIN_SPEED_FACTOR, speedFactor);
// //                     pref = pref.scalarMultiply(speedFactor);
// //                 }
// //             }

// //             /*
// //              * Local forward-neighbor braking.
// //              * If another active agent is almost directly ahead and inside the
// //              * neighbor range, reduce preferred velocity to reduce pushing.
// //              */
// //             double prefNorm = pref.getNorm();
// //             if (prefNorm < 1e-6) {
// //                 continue;
// //             }

// //             Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
// //             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
// //             double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
// //             double minScale = 1.0;

// //             for (int other = 0; other < numAgents; other++) {
// //                 if (other == agentNo) {
// //                     continue;
// //                 }

// //                 Vector2D otherPos = Simulator.instance.getAgentPosition(other);
// //                 if (otherPos == null) {
// //                     continue;
// //                 }

// //                 Vector2D rel = otherPos.subtract(agentPos);
// //                 double dist = rel.getNorm();
// //                 if (dist < 1e-6 || dist > neighborDistance) {
// //                     continue;
// //                 }

// //                 double forwardness = prefDir.dotProduct(rel.scalarMultiply(1.0 / dist));
// //                 if (forwardness < 0.85) {
// //                     continue;
// //                 }

// //                 double safety = Simulator.instance.getAgentRadius(agentNo)
// //                         + Simulator.instance.getAgentRadius(other) + 1.0;
// //                 double desiredSpacing = Math.max(neighborDistance, safety + 1.0);

// //                 double scale = (dist - safety) / (desiredSpacing - safety);
// //                 scale = Math.max(0.0, Math.min(1.0, scale));

// //                 if (scale < minScale) {
// //                     minScale = scale;
// //                 }
// //             }

// //             Vector2D finalPref = pref.scalarMultiply(minScale);
// //             Simulator.instance.setAgentPreferredVelocity(agentNo, finalPref);
// //         }
// //     }

// //     public static void main(String[] args) {
// //         TestCase08_setting2_TESTING testCase = new TestCase08_setting2_TESTING();
// //         testCase.runSimulation();
// //     }

// //     /**
// //      * Custom simulation loop copied/adapted from Setting 1.
// //      *
// //      * The loop adds a Swing button that toggles trajectory drawing during the
// //      * simulation. The step order is kept consistent with the base scenario:
// //      * update visualization, compute preferred velocities, update sub-goals, and
// //      * advance the RVO2 simulator.
// //      */
// //     @Override
// //     protected void runSimulation() {
// //         setupScenario();

// //         JFrame frame = new JFrame(scenarioName);
// //         frame.setLayout(new BorderLayout());
// //         frame.setSize(w, h);

// //         // Reset buttons for new simulation
// //         simulationPaused = false;

// //         togglePathButton = new JButton("Show Paths: OFF");
// //         togglePathButton.addActionListener(new ActionListener() {
// //             @Override
// //             public void actionPerformed(ActionEvent e) {
// //                 showPathsDuringSimulation = !showPathsDuringSimulation;
// //                 togglePathButton.setText("Show Paths: " + (showPathsDuringSimulation ? "ON" : "OFF"));
// //             }
// //         });

// //         pauseResumeButton = new JButton("Pause");
// //         pauseResumeButton.addActionListener(new ActionListener() {
// //             @Override
// //             public void actionPerformed(ActionEvent e) {
// //                 simulationPaused = !simulationPaused;
// //                 pauseResumeButton.setText(simulationPaused ? "Resume" : "Pause");
// //             }
// //         });

// //         javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
// //         buttonPanel.add(togglePathButton);
// //         buttonPanel.add(pauseResumeButton);
// //         frame.add(buttonPanel, BorderLayout.NORTH);

// //         frame.add(this, BorderLayout.CENTER);
// //         frame.pack();
// //         frame.setVisible(true);
// //         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

// //         final int TARGET_FPS = 60;
// //         final long FRAME_TIME = 1000 / TARGET_FPS;

// //         do {
// //             long frameStart = System.currentTimeMillis();

// //             updateVisualization();

// //             // Only step simulation if not paused
// //             if (!simulationPaused) {
// //                 setPreferredVelocities();
// //                 updateSubGoal();
// //                 Simulator.instance.doStep();
// //                 totalSteps++;
// //             } else {
// //                 // Still repaint while paused
// //                 repaint();
// //             }

// //             long frameElapsed = System.currentTimeMillis() - frameStart;
// //             if (frameElapsed < FRAME_TIME) {
// //                 try {
// //                     Thread.sleep(FRAME_TIME - frameElapsed);
// //                 } catch (InterruptedException e) {
// //                     e.printStackTrace();
// //                     Thread.currentThread().interrupt();
// //                 }
// //             }

// //             if (agentCount == totalAgents) {
// //                 break;
// //             }

// //             repaint();
// //         } while (!reachedGoal());

// //         ended = true;
// //         updateVisualization();
// //         printResults();
// //     }
// // }
// package rvo2example;

// import algorithms.dijkstra.engine.DijkstraAlgorithm;
// import algorithms.dijkstra.model.Graph;
// import algorithms.dijkstra.model.Vertex;
// import algorithms.dijkstra.model.Edge;
// import java.awt.Color;
// import java.awt.Font;
// import java.awt.FontMetrics;
// import java.awt.Graphics;
// import java.awt.Graphics2D;
// import java.awt.BorderLayout;
// import java.util.*;

// import javax.swing.JButton;
// import javax.swing.JFrame;
// import javax.swing.WindowConstants;
// import java.awt.event.ActionListener;
// import java.awt.event.ActionEvent;

// import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
// import org.apache.commons.math3.util.FastMath;
// import rvo2.Goal;
// import rvo2.Simulator;

// //adding library for chart
// import java.awt.BasicStroke;
// import java.awt.RenderingHints;
// import java.awt.Stroke;

// /**
//  * TC8: Two intersections with eight possible flows.
//  *
//  * Layout mapping: A=0, B=1, C=2, D=3, E=4, F=5, G=6, H=7, I=8. B and F are
//  * intersections. Agents can spawn from A, D, or H and exit at E, G, or I.
//  *
//  * This version includes the two missing Setting 1 features: 1) dynamic
//  * BUSY/FREE exit-state logic, and 2) custom runSimulation() with a
//  * trajectory-display toggle button.
//  */
// public class TestCase08_setting2_TESTING extends BaseTrafficScenario2_TESTING {

//     private static final long serialVersionUID = 1L;

//     //======================PARAMETERS==========================
//     public TestCase08_setting2_TESTING() {
//         scenarioName = "TC8: Two Intersections - 6 Rules";
//         trafficType = "2-intersection-6-rules";
//         totalAgents = 64;
//     }

//     // Counts spawned agents and helps rotate through shuffled uniform flows.
//     private int spawnIndex = 0;

//     // Temporary source/destination sequence used by the uniform flow scheduler.
//     private List<int[]> flowOrder = new ArrayList<>();

//     // Dynamic exit state copied from Setting 1. BUSY means agents slow down near exits.
//     private boolean isBusy = false;

//     // Use real wall-clock time for BUSY/FREE switching.
//     // Do NOT use Simulator.instance.getGlobalTime() here because the simulation
//     // advances once per frame, so 4-7 simulation steps can pass in less than
//     // one real second when the UI runs at 60 FPS.
//     //FOR CURVE CODE
//     private static final int TURN_SEGMENTS = 6;
//     private static final double INTERSECTION_OFFSET_RATIO = 0.10;
//     private static final double ARC_RADIUS_RATIO = 1.0;
//     private static final double CONNECTOR_STEP_RATIO = 0.45;

//     private double roadGap = 50.0;

//     private DijkstraAlgorithm dijkstra;
//     private int graphA;
//     private int graphD;
//     private int graphE;
//     private int graphG;
//     private int graphH;
//     private int graphI;

//     private enum CornerType {
//         NW, NE, SE, SW
//     }

//     private static class IntersectionNodes {

//         int westAnchor;
//         int southAnchor;
//         int northAnchor;
//         int eastAnchor;

//         List<Integer> westToNorthArc = new ArrayList<>();
//         List<Integer> southToEastArc = new ArrayList<>();

//         List<Integer> straightWestToEast = new ArrayList<>();
//         List<Integer> straightSouthToNorth = new ArrayList<>();
//     }

//     private long nextSwitchWallTimeMs = 0L;
//     private static final long MIN_STATUS_DURATION_MS = 4000L;
//     private static final long MAX_STATUS_DURATION_MS = 7000L;

//     private static final double DETECTION_RADIUS = 60.0;
//     private static final double MIN_SPEED_FACTOR = 0.15;
//     private int throughExitCount = 0;
//     private int slowExitCount = 0;

//     // Runtime trajectory display controlled by the button in runSimulation().
//     private boolean showPathsDuringSimulation = false;
//     private JButton togglePathButton;

//     // Pause/Resume simulation flag
//     private boolean simulationPaused = false;
//     private JButton pauseResumeButton;
//     int leftToTop1 = 0;
//     int leftToTop2 = 0;
//     int leftToRight = 0;
//     int bottom1ToTop1 = 0;
//     int bottom1ToTop2 = 0;
//     int bottom1ToRight = 0;
//     int bottom2ToTop2 = 0;
//     int bottom2ToRight = 0;
//     double time;
//     double startTime = 0;
//     double endTime = 0;
//     static Random rand = new Random();
//     String disType = "Weibull Distribution";
//     int[] sources = {0, 3, 7}; // Added 7 for second bottom
//     int[] destinations = {4, 6, 8}; // Added 6 and 8 for second intersection
//     private List<Vector2D> nodePoints = new ArrayList<>();
//     int spawnedAgents = 0;
//     // spawn interval settings
//     private DISTRIBUTION_TYPE distributionType = DISTRIBUTION_TYPE.Weibull;
//     private double meanSpawnInterval = 60.0;
//     private double stdSpawnInterval = 15.0;
//     private double minSpawnInterval = 15.0;
//     private double maxSpawnInterval = 120.0;
//     private double randomMinSpawnInterval = 20.0;
//     private double randomMaxSpawnInterval = 100.0;
//     double spawnInterval = meanSpawnInterval;
//     double lastSpawnTime = 0;
//     private Graph graph;
//     private java.util.List<Double> timeHistory = new java.util.ArrayList<>();
//     private java.util.List<Integer> agentHistory = new java.util.ArrayList<>();
//     double intialMillis = System.currentTimeMillis();

//     // Added positions for nodes 1 and 5
//     private Vector2D node1Pos;
//     private Vector2D node5Pos;

//     /*
//      * Plan B reservation controller.
//      *
//      * These zones are simulation-level resources, not Java locks. An agent must
//      * reserve the intersection before it can enter the conflict area. Reservations
//      * are FIFO and are released only after the agent has crossed the zone.
//      */
//     private static final double RESERVATION_REQUEST_RADIUS = 70.0;
//     private static final double RESERVATION_ENTER_RADIUS = 21.0;
//     private static final double RESERVATION_RELEASE_RADIUS = 36.0;
//     private static final double CONNECTOR_HALF_WIDTH = 32.0;
//     private static final int MAX_CONNECTOR_AGENTS = 3;

//     private ReservationZone zoneB;
//     private ReservationZone zoneF;

//     private static class ReservationZone {

//         final String name;
//         Vector2D center;
//         int owner = -1;
//         final Queue<Integer> queue = new LinkedList<>();
//         final Set<Integer> cleared = new HashSet<>();
//         final Set<Integer> entered = new HashSet<>();

//         ReservationZone(String name) {
//             this.name = name;
//         }

//         void reset(Vector2D center) {
//             this.center = center;
//             this.owner = -1;
//             this.queue.clear();
//             this.cleared.clear();
//             this.entered.clear();
//         }
//     }

//     Map<Integer, Agent> agents = new HashMap<>();

//     //adding fields for chart
//     // private final List<Long> spawnTimesMs = new ArrayList<>();
//     // private final List<Long> spawnIntervalsMs = new ArrayList<>();
//     // private final Object spawnChartLock = new Object();
//     // private long previousSpawnMs = -1L;
//     private final List<Double> spawnTimesSec = new ArrayList<>();
//     private final List<Double> spawnIntervalsSec = new ArrayList<>();
//     private final Object spawnChartLock = new Object();

//     //adding field for spawn rules
// //   private final double[] spawnCycle = {10, 30, 50, 30};
//     //adding type for distribution
//     enum DISTRIBUTION_TYPE {
//         Uniformed, Normal, Randomed, Weibull, Fixed
//     }

// // This class saves metadata of Agents
//     class Agent {

//         int id;
//         double startTime;
//         double endTime;
//         int flow;
//         double spawnProb;

//         public Agent(
//                 int id,
//                 double startTime,
//                 double endTime,
//                 int flow,
//                 double spawnProb
//         ) {
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

//     private int addVertex(List<Vertex> nodes, String id, Vector2D p) {
//         int index = nodes.size();
//         Vertex v = new Vertex(id, index, 0, id, p, p, 0);
//         nodes.add(v);
//         return index;
//     }

//     private void addDirectedEdge(List<Edge> edges, List<Vertex> nodes, int from, int to) {
//         Vector2D a = nodes.get(from).getVertex1();
//         Vector2D b = nodes.get(to).getVertex1();
//         double dist = a.distance(b);

//         edges.add(new Edge(
//                 "E_" + from + "_" + to + "_" + edges.size(),
//                 nodes.get(from),
//                 nodes.get(to),
//                 dist
//         ));
//     }

//     private void addPathEdges(List<Edge> edges, List<Vertex> nodes, List<Integer> path) {
//         for (int i = 0; i < path.size() - 1; i++) {
//             addDirectedEdge(edges, nodes, path.get(i), path.get(i + 1));
//         }
//     }

//     private Vector2D lerp(Vector2D a, Vector2D b, double t) {
//         return new Vector2D(
//                 a.getX() + (b.getX() - a.getX()) * t,
//                 a.getY() + (b.getY() - a.getY()) * t
//         );
//     }

//     private List<Vector2D> generateArcPoints(
//             Vector2D center,
//             double radius,
//             double startAngleDeg,
//             double endAngleDeg,
//             int segments
//     ) {
//         List<Vector2D> points = new ArrayList<>();

//         double startRad = Math.toRadians(startAngleDeg);
//         double endRad = Math.toRadians(endAngleDeg);

//         for (int i = 0; i <= segments; i++) {
//             double t = (double) i / segments;
//             double angle = startRad + (endRad - startRad) * t;

//             double x = center.getX() + radius * Math.cos(angle);
//             double y = center.getY() - radius * Math.sin(angle); // đổi dấu ở đây

//             points.add(new Vector2D(x, y));
//         }

//         return points;
//     }

//     private List<Integer> createArcVertexChain(
//             String prefix,
//             Vector2D intersectionCenter,
//             CornerType corner,
//             List<Vertex> nodes
//     ) {
//         double laneOffset = roadGap * INTERSECTION_OFFSET_RATIO;
//         double radius = laneOffset * ARC_RADIUS_RATIO;

//         Vector2D arcCenter;
//         double startAngleDeg;
//         double endAngleDeg;

//         switch (corner) {
//             case NW:
//                 arcCenter = new Vector2D(
//                         intersectionCenter.getX() - laneOffset,
//                         intersectionCenter.getY() - laneOffset
//                 );
//                 startAngleDeg = 180;
//                 endAngleDeg = 90;
//                 break;

//             case NE:
//                 arcCenter = new Vector2D(
//                         intersectionCenter.getX() + laneOffset,
//                         intersectionCenter.getY() - laneOffset
//                 );
//                 startAngleDeg = 180;
//                 endAngleDeg = 90;
//                 break;

//             case SE:
//                 arcCenter = new Vector2D(
//                         intersectionCenter.getX() + laneOffset,
//                         intersectionCenter.getY() + laneOffset
//                 );
//                 startAngleDeg = 270;
//                 endAngleDeg = 360;
//                 break;

//             case SW:
//                 arcCenter = new Vector2D(
//                         intersectionCenter.getX() - laneOffset,
//                         intersectionCenter.getY() + laneOffset
//                 );
//                 startAngleDeg = 0;
//                 endAngleDeg = 270;
//                 break;

//             default:
//                 return Collections.emptyList();
//         }

//         List<Vector2D> arcPoints = generateArcPoints(
//                 arcCenter,
//                 radius,
//                 startAngleDeg,
//                 endAngleDeg,
//                 TURN_SEGMENTS
//         );

//         List<Integer> arcIds = new ArrayList<>();
//         for (int i = 0; i < arcPoints.size(); i++) {
//             int id = addVertex(nodes, prefix + "_ARC_" + corner + "_" + i, arcPoints.get(i));
//             arcIds.add(id);
//         }

//         return arcIds;
//     }

//     private List<Integer> createStraightConnectorChain(
//             String prefix,
//             Vector2D from,
//             Vector2D to,
//             int segments,
//             List<Vertex> nodes
//     ) {
//         List<Integer> ids = new ArrayList<>();
//         for (int i = 1; i <= segments; i++) {
//             double t = (double) i / (segments + 1);
//             Vector2D p = lerp(from, to, t);
//             int id = addVertex(nodes, prefix + "_MID_" + i, p);
//             ids.add(id);
//         }
//         return ids;
//     }

//     private IntersectionNodes buildIntersectionSubgraph(
//             String prefix,
//             Vector2D center,
//             List<Vertex> nodes,
//             List<Edge> edges
//     ) {
//         IntersectionNodes result = new IntersectionNodes();

//         double laneOffset = roadGap * INTERSECTION_OFFSET_RATIO;
//         double connectorStep = roadGap * CONNECTOR_STEP_RATIO;

//         Vector2D westAnchorPos = new Vector2D(center.getX() - connectorStep, center.getY());
//         Vector2D southAnchorPos = new Vector2D(center.getX(), center.getY() + connectorStep);
//         Vector2D northAnchorPos = new Vector2D(center.getX(), center.getY() - connectorStep);
//         Vector2D eastAnchorPos = new Vector2D(center.getX() + connectorStep, center.getY());

//         result.westAnchor = addVertex(nodes, prefix + "_W", westAnchorPos);
//         result.southAnchor = addVertex(nodes, prefix + "_S", southAnchorPos);
//         result.northAnchor = addVertex(nodes, prefix + "_N", northAnchorPos);
//         result.eastAnchor = addVertex(nodes, prefix + "_E", eastAnchorPos);

//         // turning arcs actually used in your scenario
//         result.westToNorthArc = createArcVertexChain(prefix + "_WN", center, CornerType.NW, nodes);
//         result.southToEastArc = createArcVertexChain(prefix + "_SE", center, CornerType.SE, nodes);

//         // straight connectors
//         result.straightWestToEast = createStraightConnectorChain(
//                 prefix + "_WE",
//                 westAnchorPos,
//                 eastAnchorPos,
//                 2,
//                 nodes
//         );

//         result.straightSouthToNorth = createStraightConnectorChain(
//                 prefix + "_SN",
//                 southAnchorPos,
//                 northAnchorPos,
//                 2,
//                 nodes
//         );

//         // W -> N turn
//         {
//             List<Integer> path = new ArrayList<>();
//             path.add(result.westAnchor);
//             path.addAll(result.westToNorthArc);
//             path.add(result.northAnchor);
//             addPathEdges(edges, nodes, path);
//         }

//         // S -> E turn
//         {
//             List<Integer> path = new ArrayList<>();
//             path.add(result.southAnchor);
//             path.addAll(result.southToEastArc);
//             path.add(result.eastAnchor);
//             addPathEdges(edges, nodes, path);
//         }

//         // W -> E straight
//         {
//             List<Integer> path = new ArrayList<>();
//             path.add(result.westAnchor);
//             path.addAll(result.straightWestToEast);
//             path.add(result.eastAnchor);
//             addPathEdges(edges, nodes, path);
//         }

//         // S -> N straight
//         {
//             List<Integer> path = new ArrayList<>();
//             path.add(result.southAnchor);
//             path.addAll(result.straightSouthToNorth);
//             path.add(result.northAnchor);
//             addPathEdges(edges, nodes, path);
//         }

//         return result;
//     }

//     private void assignPathFromGraph(Goal goal, Graph graph, int src, int dst) {
//         if (src == dst) {
//             return;
//         }

//         LinkedList<Vertex> path = getShortestPath(
//                 dijkstra,
//                 graph.getVertexes().get(src),
//                 graph.getVertexes().get(dst)
//         );

//         if (path == null || path.isEmpty()) {
//             return;
//         }

//         for (int i = 1; i < path.size(); i++) {
//             goal.add(translateCoordinateVector(path.get(i).getVertex1()));
//         }
//     }
//     //adding function to record spawn time and interval for chart
//     // private void recordSpawnEvent(long nowMs) {
//     //     synchronized (spawnChartLock) {
//     //         spawnTimesMs.add(nowMs);

//     //         if (previousSpawnMs < 0) {
//     //             spawnIntervalsMs.add(0L);   // first point has no previous interval
//     //         } else {
//     //             spawnIntervalsMs.add(nowMs - previousSpawnMs);
//     //         }
//     //         previousSpawnMs = nowMs;
//     //     }
//     // }
//     private void recordSpawnEvent(double simTimeSec, double usedIntervalSec) {
//         synchronized (spawnChartLock) {
//             spawnTimesSec.add(simTimeSec);
//             spawnIntervalsSec.add(usedIntervalSec);
//         }
//     }

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

//     /**
//      * Convert a source/destination pair into the internal flow id.
//      */
//     private int getFlowId(int src, int dst) {
//         if (src == 0 && dst == 4) {
//             return 0; // A-B-E
//         }
//         if (src == 0 && dst == 8) {
//             return 1; // A-B-F-I
//         }
//         if (src == 0 && dst == 6) {
//             return 2; // A-B-F-G
//         }
//         if (src == 3 && dst == 4) {
//             return 3; // D-B-E
//         }
//         if (src == 3 && dst == 8) {
//             return 4; // D-B-F-I
//         }
//         if (src == 3 && dst == 6) {
//             return 5; // D-B-F-G
//         }
//         if (src == 7 && dst == 8) {
//             return 6; // H-F-I
//         }
//         if (src == 7 && dst == 6) {
//             return 7; // H-F-G
//         }
//         return 0;
//     }

//     /**
//      * Convert internal flow id to a readable route label for the dashboard.
//      */
//     private String getFlowName(int flow) {
//         switch (flow) {
//             case 0:
//                 return "A-B-E";
//             case 1:
//                 return "A-F-I";
//             case 2:
//                 return "A-F-G";
//             case 3:
//                 return "D-B-E";
//             case 4:
//                 return "D-F-I";
//             case 5:
//                 return "D-F-G";
//             case 6:
//                 return "H-F-I";
//             case 7:
//                 return "H-F-G";
//             default:
//                 return "Unknown";
//         }
//     }

//     //This functions will return the src/dst base on Normal Distribution
//     public static int chooseByNormal(int[] options, double mean, double std) {
//         double arr[] = {0, 0, 0};
//         double gaussian, value;
//         for (int i = 0; i < 3; i++) {
//             gaussian = rand.nextGaussian();
//             value = mean + std * gaussian;
//             value = Math.max(0, Math.min(1, value));
//             arr[i] = value;
//         }
//         double max = -1;
//         int index = 0;
//         for (int i = 0; i < 3; i++) {
//             if (max < arr[i]) {
//                 max = arr[i];
//                 index = i;
//             }
//         }
//         return options[index];
//     }

//     /**
//      * Centralized spawn-interval policy.
//      *
//      * The selected distribution controls only the time between spawns. The
//      * source/destination selection is handled inside spawnOneAgent().
//      */
//     private double getNextSpawnInterval() {
//         double next;

//         switch (distributionType) {
//             case Normal:
//                 disType = "Normal Distribution";
//                 next = meanSpawnInterval + stdSpawnInterval * rand.nextGaussian();
//                 break;

//             case Randomed:
//                 disType = "Random Distribution";
//                 double r = rand.nextDouble();
//                 // sqrt(r) gives a non-uniform random interval biased toward larger values.
//                 next = randomMinSpawnInterval + (randomMaxSpawnInterval - randomMinSpawnInterval) * Math.sqrt(r);
//                 break;

//             case Weibull:
//                 disType = "Weibull Distribution";
//                 // The staged Weibull-like profile is refined in setPreferredVelocities().
//                 next = 75.0;
//                 break;

//             case Fixed:
//                 disType = "Fixed Distribution";
//                 next = meanSpawnInterval;
//                 break;

//             case Uniformed:
//             default:
//                 disType = "Uniform Distribution";
//                 next = minSpawnInterval + rand.nextDouble() * (maxSpawnInterval - minSpawnInterval);
//                 break;
//         }

//         return Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
//     }

//     //This function helps create the 2 edges A-->B-->C 
//     private void createEdge(int indexA, int indexB, int indexC, java.util.List<algorithms.dijkstra.model.Vertex> nodes, java.util.List<algorithms.dijkstra.model.Edge> edges) {
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

//     //This functions shuffleflows for UniformDistribution
//     private void shuffleFlows(Random rand) {

//         flowOrder.clear();

//         flowOrder.add(new int[]{0, 4});
//         flowOrder.add(new int[]{0, 8});
//         flowOrder.add(new int[]{0, 6});
//         flowOrder.add(new int[]{3, 4});
//         flowOrder.add(new int[]{3, 8});
//         flowOrder.add(new int[]{3, 6});
//         flowOrder.add(new int[]{7, 6});
//         flowOrder.add(new int[]{7, 8});

//         Collections.shuffle(flowOrder, rand);
//     }
//     //This is agents defined color
//     private static final Color[] FLOW_COLORS = new Color[]{
//         Color.BLACK, // flow 0
//         Color.RED, // flow 1
//         Color.ORANGE, // flow 2
//         Color.YELLOW, // flow 3
//         Color.GREEN, // flow 4
//         Color.CYAN, // flow 5
//         Color.BLUE, // flow 6
//         new Color(128, 0, 128) // flow 7 (PURPLE)
//     };
//     //This function will spawn one agent with defined Dst and Src

//     private void spawnOneAgent(
//             Graph graph,
//             double[][] behavior,
//             Random rand,
//             double gap
//     ) {
//         int k = Simulator.instance.getNumAgents();

//         // //adding x for chart
//         // long nowMs = System.currentTimeMillis();
//         // // record chart data
//         // recordSpawnEvent(nowMs);
//         // Update agents information
//         Agent agent = agents.get(k);
//         if (agent == null) {
//             agent = new Agent(k, 0, 0, 0, 0);
//             agents.put(k, agent);
//         }
//         agent.startTime = System.currentTimeMillis();
//         // This is Spawn Location Distribution
//         //--------------------------------
//         //        disType = "Normal Distribution";
//         int src, dst;
//         int logicalSrc, logicalDst;
//         if (distributionType == DISTRIBUTION_TYPE.Normal) {
// //                     src = chooseByNormal(sources, 0.5, 1);
// //                    dst = chooseByNormal(destinations, 0.5, 1);
// //                    if(src==7 && dst==4) {
// //                    	double gaussian = rand.nextGaussian();
// //                        double value = 0.5 + 1 * gaussian;
// //                    	int id = ((Math.max(0, Math.min(1, value)))>0.5 ?1:2);
// //                    	dst = destinations[id];
// //                    }

//             logicalSrc = chooseByNormal(new int[]{0, 3, 7}, 0.5, 1);
//             logicalDst = chooseByNormal(new int[]{4, 6, 8}, 0.5, 1);

//             if (logicalSrc == 7 && logicalDst == 4) {
//                 double gaussian = rand.nextGaussian();
//                 double value = 0.5 + gaussian;
//                 int id = ((Math.max(0, Math.min(1, value))) > 0.5 ? 1 : 2);
//                 logicalDst = new int[]{4, 6, 8}[id];
//             }

//             src = logicalSrc;
//             dst = logicalDst;
//         } //        disType = "Random Distribution";
//         else if (distributionType == DISTRIBUTION_TYPE.Randomed || distributionType == DISTRIBUTION_TYPE.Weibull) {
// //            	  src = sources[rand.nextInt(3)];
// //            	  dst = destinations[rand.nextInt(3)];
// //                if(src==7 && dst==4) {
// //            dst = destinations[rand.nextInt(2)+1];
// //              }
//             int[] logicalSources = {0, 3, 7};
//             int[] logicalDestinations = {4, 6, 8};

//             logicalSrc = logicalSources[rand.nextInt(3)];
//             logicalDst = logicalDestinations[rand.nextInt(3)];

//             if (logicalSrc == 7 && logicalDst == 4) {
//                 logicalDst = logicalDestinations[rand.nextInt(2) + 1];
//             }

//             src = logicalSrc;
//             dst = logicalDst;
//         } //        disType = "Uniform Distribution";
//         else {
//             if (spawnIndex % 8 == 0) {
//                 shuffleFlows(rand);
//             }
// //            int[] flow = flowOrder.get(spawnIndex % 8);
// //
// //            src = flow[0];
// //            dst = flow[1];
// //
//             int[] flow = flowOrder.get(spawnIndex % 8);

//             logicalSrc = flow[0];
//             logicalDst = flow[1];

//             src = logicalSrc;
//             dst = logicalDst;
//         }

//         //--------------------------------
//         disType = distributionType.name() + " Distribution";

//         spawnIndex++;
// //NEW
// //        int graphSrc = src;
// //        int graphDst = dst;
// //
// //        for (Vertex v : graph.getVertexes()) {
// //            if (v.getName().equals("A") && src == 0) graphSrc = v.getId();
// //            if (v.getName().equals("D") && src == 3) graphSrc = v.getId();
// //            if (v.getName().equals("H") && src == 7) graphSrc = v.getId();
// //
// //            if (v.getName().equals("E") && dst == 4) graphDst = v.getId();
// //            if (v.getName().equals("G") && dst == 6) graphDst = v.getId();
// //            if (v.getName().equals("I") && dst == 8) graphDst = v.getId();
// //        }
//         int graphSrc = logicalSrc;
//         int graphDst = logicalDst;

//         if (logicalSrc == 0) {
//             graphSrc = graphA; 
//         }else if (logicalSrc == 3) {
//             graphSrc = graphD; 
//         }else if (logicalSrc == 7) {
//             graphSrc = graphH;
//         }

//         if (logicalDst == 4) {
//             graphDst = graphE; 
//         }else if (logicalDst == 6) {
//             graphDst = graphG; 
//         }else if (logicalDst == 8) {
//             graphDst = graphI;
//         }

//         // Update metadata for agents
// //        agent.flow = getFlowId(src, dst);
//         agent.flow = getFlowId(logicalSrc, logicalDst);
//         if (logicalSrc == 0 && logicalDst == 4) {
//             leftToTop1++;
//             agent.flow = 0;
//         }
//         if (logicalSrc == 0 && logicalDst == 8) {
//             leftToTop2++;
//             agent.flow = 1;
//         }
//         if (logicalSrc == 0 && logicalDst == 6) {
//             leftToRight++;
//             agent.flow = 2;
//         }
//         if (logicalSrc == 3 && logicalDst == 4) {
//             bottom1ToTop1++;
//             agent.flow = 3;
//         }
//         if (logicalSrc == 3 && logicalDst == 6) {
//             bottom1ToRight++;
//             agent.flow = 5;
//         }
//         if (logicalSrc == 3 && logicalDst == 8) {
//             bottom1ToTop2++;
//             agent.flow = 4;
//         }
//         if (logicalSrc == 7 && logicalDst == 8) {
//             agent.flow = 6;
//             bottom2ToTop2++;
//         }
//         if (logicalSrc == 7 && logicalDst == 6) {
//             bottom2ToRight++;
//             agent.flow = 7;
//         }
//         int behaviorIndex = rand.nextInt(7);
//         System.out.println("\n=== K: " + k + " ===");
// //
// //        Simulator.instance.addAgent(
// //                translateCoordinateVector(graph.getVertexes().get(src).getVertex1())
// //        );
//         Simulator.instance.addAgent(
//                 translateCoordinateVector(graph.getVertexes().get(graphSrc).getVertex1())
//         );
//         Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
//         Simulator.instance.setAgentMaxNeighbors(
//                 k,
//                 (int) behavior[behaviorIndex][1]
//         );
//         Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);

//         double radius = gap * 0.21;
//         Simulator.instance.setAgentRadius(k, radius);
//         Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);
// //
// //        Goal goal = new Goal(
// //                translateCoordinateVector(graph.getVertexes().get(dst).getVertex1())
// //        );
//         Goal goal = new Goal(
//                 translateCoordinateVector(graph.getVertexes().get(graphDst).getVertex1())
//         );

//         goal.setColor(getColor());
//         goal.setFlag(1);
// //        goal.setSource(src);
// //        goal.setDestination(dst);

//         goal.setSource(logicalSrc);
//         goal.setDestination(logicalDst);
// //        Vector2D Global_Goal = translateCoordinateVector(
// //                graph.getVertexes().get(dst).getVertex1()
// //        );
//         Vector2D Global_Goal = translateCoordinateVector(
//                 graph.getVertexes().get(graphDst).getVertex1()
//         );
//         goal.setGlobalGoal(Global_Goal);
//         assignPathFromGraph(goal, graph, graphSrc, graphDst);

//         goalsList.add(goal);
//     }

//     //This is a setup function which only run once to define the environment
//     @Override
//     protected void setupScenario() {

//         startSearchTime = System.currentTimeMillis();
//         intialMillis = System.currentTimeMillis();
//         startTime = Simulator.instance.getGlobalTime();

//         // Reset runtime state so rerunning the scenario does not keep old data.
//         nodePoints.clear();
//         timeHistory.clear();
//         agentHistory.clear();
//         agents.clear();
//         isBusy = false;
//         nextSwitchWallTimeMs = 0L;
//         throughExitCount = 0;
//         slowExitCount = 0;
//         simulationPaused = false;

//         leftToTop1 = 0;
//         leftToTop2 = 0;
//         leftToRight = 0;
//         bottom1ToTop1 = 0;
//         bottom1ToTop2 = 0;
//         bottom1ToRight = 0;
//         bottom2ToTop2 = 0;
//         bottom2ToRight = 0;

//         synchronized (spawnChartLock) {
//             spawnTimesSec.clear();
//             spawnIntervalsSec.clear();
//         }

//         //adding chart data reset
//         // synchronized (spawnChartLock) {
//         //     spawnTimesMs.clear();
//         //     spawnIntervalsMs.clear();
//         //     previousSpawnMs = -1L;
//         // }
//         Simulator.instance.setTimeStep(1);
//         Simulator.instance.setAgentDefaults(80, 40, 38, 2.5, 3.55, 2.0,
//                 org.apache.commons.math3.geometry.euclidean.twod.Vector2D.ZERO);

//         double[][] behavior = createBehaviorMatrix();

//         //=========OBSTACLES CREATION===============
//         obsList1 = new java.util.ArrayList<Obs>();
//         int rowsObs = 2;
//         int colsObs = 3; // Changed from 2 to 3 for two intersections

//         int decreScale = 1;

//         double blockWidth = 450 / decreScale;  // pixels
//         double blockHeight = 375 / decreScale; // pixels
//         double gap = 50 / decreScale;          // pixels between blocks
//         roadGap = gap;
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

//         //=============NODE CREATION===============
// //        java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> nodePoints = new java.util.ArrayList<>();
//         //=====Coordinates for Nodes Creation=======
//         double yMid = startY + blockHeight + gap / 2.0;
//         double xLeft = startX;
//         double xBetween = startX + blockWidth + gap / 2.0;
//         double xRight = startX + blockWidth * 3 + gap * 2; // Updated for two intersections
//         double xBetween2 = startX + blockWidth * 2 + gap * 1.5; // Corrected position for second intersection
//         double yTop = startY;
//         double yBottom = yTop + blockHeight * 2 + gap;
//         //     4     8
//         // 0   1  2  5   6
//         //     3     7     
//         //	  xLeft 
//         //  	-------------------->    
//         //yTop  |    4     8	
//         //yMid  |0   1  2  5   6(xRight)          
//         //yBot  |    3     7     
//         //		v   xB    xB2
//         //      
//         // for visualization the node location
//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft, yMid)); //0

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yMid)); //1

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(((xBetween2 + xBetween) / 2), yMid)); //2

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yBottom)); //3

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yTop)); //4

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yMid)); //5

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight, yMid)); //6

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yBottom)); //7

//         nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yTop)); //8

// //        java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
// //        java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();
// //        for (int i = 0; i < nodePoints.size(); i++) {
// //            org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
// //            algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex("node_" + i, i, 0, "node_" + i, p, p, 0);
// //            nodes.add(v);
// //        }
// //
// //        createEdge(0, 1, 2, nodes, edges);
// //        createEdge(2, 5, 6, nodes, edges);
// //        createEdge(3, 1, 4, nodes, edges);
// //        createEdge(7, 5, 8, nodes, edges);
// //
// //        graph = new Graph(nodes, edges);
//         List<Vertex> nodes = new ArrayList<>();
//         List<Edge> edges = new ArrayList<>();

//         // External endpoints only.
//         // Keep original IDs for actual source/destination nodes:
//         // 0=A, 3=D, 4=E, 6=G, 7=H, 8=I
//         Map<Integer, Integer> logicalToGraph = new HashMap<>();

//         logicalToGraph.put(0, addVertex(nodes, "A", nodePoints.get(0)));
//         logicalToGraph.put(3, addVertex(nodes, "D", nodePoints.get(3)));
//         logicalToGraph.put(4, addVertex(nodes, "E", nodePoints.get(4)));
//         logicalToGraph.put(6, addVertex(nodes, "G", nodePoints.get(6)));
//         logicalToGraph.put(7, addVertex(nodes, "H", nodePoints.get(7)));
//         logicalToGraph.put(8, addVertex(nodes, "I", nodePoints.get(8)));

//         // Build local subgraph around old node 1 and old node 5.
//         IntersectionNodes inter1 = buildIntersectionSubgraph("B", nodePoints.get(1), nodes, edges);
//         IntersectionNodes inter5 = buildIntersectionSubgraph("F", nodePoints.get(5), nodes, edges);

//         // Connect external roads to local graph of intersection 1.
//         addDirectedEdge(edges, nodes, logicalToGraph.get(0), inter1.westAnchor);   // A -> B_W
//         addDirectedEdge(edges, nodes, logicalToGraph.get(3), inter1.southAnchor);  // D -> B_S

//         addDirectedEdge(edges, nodes, inter1.northAnchor, logicalToGraph.get(4));  // B_N -> E

//         // Road between intersection 1 and intersection 5:
//         addDirectedEdge(edges, nodes, inter1.eastAnchor, inter5.westAnchor);        // B_E -> F_W

//         // Connect external roads to local graph of intersection 5.
//         addDirectedEdge(edges, nodes, logicalToGraph.get(7), inter5.southAnchor);   // H -> F_S
//         addDirectedEdge(edges, nodes, inter5.northAnchor, logicalToGraph.get(8));   // F_N -> I
//         addDirectedEdge(edges, nodes, inter5.eastAnchor, logicalToGraph.get(6));    // F_E -> G

//         graph = new Graph(nodes, edges);
//         dijkstra = new DijkstraAlgorithm(graph);

//         // remap logical source/destination ids so spawn code still uses old labels
//         Map<Integer, Integer> remap = new HashMap<>();
//         remap.put(0, logicalToGraph.get(0));
//         remap.put(3, logicalToGraph.get(3));
//         remap.put(4, logicalToGraph.get(4));
//         remap.put(6, logicalToGraph.get(6));
//         remap.put(7, logicalToGraph.get(7));
//         remap.put(8, logicalToGraph.get(8));

//         // store graph-ready ids back into arrays used by spawning
//         sources = new int[]{remap.get(0), remap.get(3), remap.get(7)};
//         destinations = new int[]{remap.get(4), remap.get(6), remap.get(8)};

//         System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());
//         System.out.println("Auto-generated Graph edges: " + graph.getEdges().size());

//         graphA = logicalToGraph.get(0);
//         graphD = logicalToGraph.get(3);
//         graphE = logicalToGraph.get(4);
//         graphG = logicalToGraph.get(6);
//         graphH = logicalToGraph.get(7);
//         graphI = logicalToGraph.get(8);

//         // Added: Store positions for painting
//         node1Pos = translateCoordinateVector(nodePoints.get(1));
//         node5Pos = translateCoordinateVector(nodePoints.get(5));

//         zoneB = new ReservationZone("B");
//         zoneF = new ReservationZone("F");
//         zoneB.reset(node1Pos);
//         zoneF.reset(node5Pos);

//         for (int i = 0; i < totalAgents; i++) {
//             double start = rand.nextDouble() * 10;
//             double end = start + rand.nextDouble() * 10;
//             int flow = rand.nextInt(8);
//             double prob = rand.nextDouble();

//             Agent agent = new Agent(i, start, end, flow, prob);
//             agents.put(i, agent);
//         }
//         spawnOneAgent(graph, behavior, rand, gap);
//         spawnedAgents++;

//         // initialize dynamic interval after first spawn
//         // spawnInterval = getNextSpawnInterval();
//         recordSpawnEvent(Simulator.instance.getGlobalTime(), 0.0); // first spawn has no previous interval
//         spawnInterval = getNextSpawnInterval();
//         lastSpawnTime = Simulator.instance.getGlobalTime();

//         System.out.println("Scenario initialized: " + scenarioName);
//     }

//     @Override
//     protected void paintComponent(Graphics g) {
//         super.paintComponent(g);

//         Graphics2D g2 = (Graphics2D) g;
//         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//         // Draw exit-status bars. Green = FREE, red = BUSY.
//         if (nodePoints.size() >= 9) {
//             double barGap = 50.0;
//             double barThickness = 10.0;
//             int[] exitNodes = {4, 6, 8};

//             g2.setColor(isBusy ? Color.RED : Color.GREEN);
//             for (int idx : exitNodes) {
//                 Vector2D p = nodePoints.get(idx);
//                 if (idx == 6) {
//                     // Right exit G: vertical gate bar.
//                     g2.fillRect((int) (p.getX() - 5), (int) (p.getY() - barGap / 2), (int) barThickness, (int) barGap);
//                 } else {
//                     // Top exits E and I: horizontal gate bars.
//                     g2.fillRect((int) (p.getX() - barGap / 2), (int) (p.getY() - 5), (int) barGap, (int) barThickness);
//                 }
//             }
//         }

//         //	 Agent agent = agents.get(agentNo);
//         if (Simulator.instance != null) {
//             int numAgents = Math.min(Simulator.instance.getNumAgents(), goalsList.size());
//             for (int agentNo = 0; agentNo < numAgents; agentNo++) {
//                 Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//                 if (agentPos == null) {
//                     continue;
//                 }

//                 Vector2D agentPos_cood = translateCoordinateBackVector(agentPos);
//                 int radius = (int) Simulator.instance.getAgentRadius(agentNo);

//                 if (goalsList.get(agentNo).getFlag() == 1) {
//                     Agent agent = agents.get(agentNo);
//                     int flow = agent.flow; // phải có hàm này
//                     Color agentColor = FLOW_COLORS[flow % 8];
//                     g2.setColor(agentColor);
//                     g2.fillOval((int) agentPos_cood.getX() - 2, (int) agentPos_cood.getY() - 2, 4, 4);
//                     // Draw oval instead of fill the Oval 
// //                 g2.drawOval((int) agentPos_cood.getX() - radius, (int) agentPos_cood.getY() - radius,
// //                         radius * 2, radius * 2);
//                     g2.fillOval(
//                             (int) agentPos_cood.getX() - radius,
//                             (int) agentPos_cood.getY() - radius,
//                             radius * 2,
//                             radius * 2
//                     );
//                 }

//                 // Draw trajectory while the toggle is ON, or always after the run ends.
//                 if ((ended || showPathsDuringSimulation) && totalSteps < 10000) {
//                     int pointSize = goalsList.get(agentNo).getaddMovingStepSize();
//                     if (pointSize > 1) {
//                         g2.setColor(goalsList.get(agentNo).getColor());
//                         for (int i = 0; i < pointSize - 1; i += Math.max(1, pointSize / 50)) {
//                             Vector2D curr = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(i));
//                             Vector2D next = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(Math.min(i + 1, pointSize - 1)));
//                             g2.drawLine((int) curr.getX(), (int) curr.getY(), (int) next.getX(), (int) next.getY());
//                         }
//                     }
//                 }

//             }
//         }

//         // Draw the circle at each intersection where priority flow algorithm will work  
// //     int radius = 37;
// //     g2.setColor(Color.GREEN);
// //
// //    //  Draw circle at node 1
// //     if (node1Pos != null) {
// //         g2.drawOval(
// //            //  (int) node1Pos.getX() - radius,
// //            //  (int) node1Pos.getY() - radius,
// //             710 - radius,
// //             540 - radius,
// //             radius * 2,
// //             radius * 2
// //         );
// //     }
// //
// //    //  Draw circle at node 5
// //     if (node5Pos != null) {
// //         g2.drawOval(
// //             //  (int) node5Pos.getX() - radius,
// //             //  (int) node5Pos.getY() - radius,
// //             1210 - radius,
// //             540 - radius,
// //             radius * 2,
// //             radius * 2
// //         );
// //     }
//         // ===== Draw information on screen ==
//         int startX = 20;
//         int startY = 150;
//         int circleSize = 12;
//         int spacing = 20;
//         g2.setColor(Color.BLACK);
//         g2.drawString(disType, startX, startY - 50);

//         g2.drawString("Simulation Time(s)", startX, startY - 30);

//         g2.drawString("Agent's Color | Count | Time(s) " + String.format("%.2f", spawnInterval), startX, startY - 10);

//         // Display dynamic exit status and exit counters.
//         g2.setColor(isBusy ? Color.RED : new Color(0, 150, 0));
//         g2.drawString("EXIT STATUS: " + (isBusy ? "BUSY" : "FREE"), startX + 170, startY - 70);
//         g2.setColor(new Color(0, 120, 0));
//         g2.drawString("Normal Exits: " + throughExitCount, startX + 170, startY - 50);
//         g2.setColor(Color.RED);
//         g2.drawString("Slow Exits: " + slowExitCount, startX + 170, startY - 35);
//         g2.setColor(Color.BLACK);

//         int[] flowCount = {
//             leftToTop1,
//             leftToTop2,
//             leftToRight,
//             bottom1ToTop1,
//             bottom1ToTop2,
//             bottom1ToRight,
//             bottom2ToTop2,
//             bottom2ToRight
//         };
//         int[] src_dst_count = {
//             leftToTop1 + leftToTop2 + leftToRight,
//             bottom1ToTop1 + bottom1ToTop2 + bottom1ToRight,
//             bottom2ToTop2 + bottom2ToRight,
//             leftToTop1 + bottom1ToTop1,
//             leftToTop2 + bottom1ToTop2 + bottom2ToTop2,
//             leftToRight + bottom1ToRight + bottom2ToRight
//         };

//         double[] avgTime = new double[8];
//         int[] countFlow = new int[8];
//         if (ended) {
//             for (int i = 0; i < totalAgents; i++) {
//                 Agent agent = agents.get(i);
//                 if (agent.endTime > 0) {
//                     int f = agent.flow;
//                     avgTime[f] += agent.getDuration();
//                     countFlow[f]++;
//                 }
//             }

//             for (int i = 0; i < 8; i++) {
//                 if (countFlow[i] > 0) {
//                     avgTime[i] /= countFlow[i];
//                 }
//             }
//             long endTime = System.currentTimeMillis();
//             double time = (endTime - startSearchTime) * 0.001;

//             g2.drawString(String.format("%.2f", time), startX + 110, startY - 30);

//         }

//         for (int i = 0; i < 8; i++) {
//             int y = startY + i * spacing;

//             g2.setColor(FLOW_COLORS[i]);
//             g2.fillOval(startX, y - circleSize + 9, circleSize, circleSize);

//             g2.setColor(Color.BLACK);
//             String flowName = getFlowName(i);

//             int col1X = startX + 20;
//             int col2X = startX + 100;
//             int col3X = startX + 140;
//             if (ended && countFlow[i] > 0) {
//                 g2.drawString(String.format("%.2f", avgTime[i] * 0.001), col3X, y + 9);
//             } else {
//                 g2.drawString("-", col3X, y + 9);
//             }
//             g2.drawString(flowName, col1X, y + 9);

//             g2.drawString(String.valueOf(flowCount[i]), col2X, y + 9);

//         }
//         // ===== SOURCE / DESTINATION COUNT =====

//         int startY2 = startY + 8 * spacing + 30;

//         int colLeftX = startX;        // Source
//         int colRightX = startX + 90; // Destination

//         g2.setColor(Color.BLACK);
//         g2.drawString("Source Count", colLeftX, startY2 - 20);
//         g2.drawString("Destination Count", colRightX, startY2 - 20);

//         // Source labels
//         String[] sources = {"A", "D", "H"};
//         // Destination labels
//         String[] dests = {"E", "I", "G"};

//         for (int i = 0; i < 3; i++) {
//             int y = startY2 + i * spacing;

//             // SOURCE
//             g2.drawString(
//                     sources[i] + ": " + src_dst_count[i],
//                     colLeftX,
//                     y
//             );

//             // DESTINATION
//             g2.drawString(
//                     dests[i] + ": " + src_dst_count[i + 3],
//                     colRightX,
//                     y
//             );
//         }

//         //===== REALTIME GRAPH (FIXED SCALE) =====
//         int graphX = startX + 15;
//         int graphY = startY2 + 3 * spacing + 400;

//         int graphWidth = 150;
//         int graphHeight = 80;

// //fixed range
//         double maxTimeRange = 80.0;
//         int maxAgentRange = 40;

// //khung
//         g2.setColor(Color.BLACK);
//         g2.drawRect(graphX, graphY, graphWidth, graphHeight);

// //label
//         g2.drawString("Agents", graphX, graphY - 5);

//         g2.drawString("0", graphX - 10, graphY + graphHeight);
//         g2.drawString("40", graphX - 25, graphY + 10);

//         g2.drawString("0", graphX, graphY + graphHeight + 15);
//         g2.drawString("80", graphX + graphWidth - 20, graphY + graphHeight + 15);
//         g2.drawString("Time(s)", graphX + graphWidth + 5, graphY + graphHeight + 7);

// //vẽ line
//         if (timeHistory.size() > 1) {
//             g2.setColor(Color.BLUE);

//             for (int i = 1; i < timeHistory.size(); i++) {
//                 double t1 = timeHistory.get(i - 1);
//                 double t2 = timeHistory.get(i);

//                 int a1 = agentHistory.get(i - 1);
//                 int a2 = agentHistory.get(i);

//                 t1 = Math.min(t1, maxTimeRange);
//                 t2 = Math.min(t2, maxTimeRange);

//                 a1 = Math.min(a1, maxAgentRange);
//                 a2 = Math.min(a2, maxAgentRange);

//                 int x1 = graphX + (int) (t1 / maxTimeRange * graphWidth);
//                 int x2 = graphX + (int) (t2 / maxTimeRange * graphWidth);

//                 int y1 = graphY + graphHeight - (int) ((double) a1 / maxAgentRange * graphHeight);
//                 int y2 = graphY + graphHeight - (int) ((double) a2 / maxAgentRange * graphHeight);

//                 g2.drawLine(x1, y1, x2, y2);
//             }
//         }

//         // ===== DRAW NODE LABELS =====
//         if (nodePoints != null) {

//             Graphics2D gText = (Graphics2D) g2.create();

//             gText.setFont(new Font("Lexend", Font.BOLD, 24));
//             gText.setColor(Color.BLACK);

//             Random rand = new Random(42);

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

//         //adding code to draw the chart
//         int chartWidth = 210;
//         int chartHeight = 320;
//         int chartX = 10;
//         int chartY = 425;
//         // drawSpawnIntervalChart(g2, chartX, chartY, chartWidth, chartHeight);

//         drawSpawnHistogram(g2, chartX, chartY, chartWidth, chartHeight);
//     }

// //adding method to draw the chart for spawn interval
//     private void drawSpawnIntervalChart(Graphics2D g2, int x, int y, int width, int height) {
//         List<Double> times;
//         List<Double> intervals;

//         synchronized (spawnChartLock) {
//             times = new ArrayList<>(spawnTimesSec);
//             intervals = new ArrayList<>(spawnIntervalsSec);
//         }

//         Graphics2D g = (Graphics2D) g2.create();
//         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

//         // ===== outer panel =====
//         g.setColor(Color.WHITE);
//         g.fillRoundRect(x, y, width, height, 18, 18);
//         g.setColor(Color.BLACK);
//         g.setStroke(new BasicStroke(2f));
//         g.drawRoundRect(x, y, width, height, 18, 18);

//         // ===== title =====
//         g.setFont(new Font("Arial", Font.BOLD, 15));
//         String title = "Spawn Interval (Second)";
//         FontMetrics titleFm = g.getFontMetrics();
//         int titleX = x + (width - titleFm.stringWidth(title)) / 2;
//         g.drawString(title, titleX, y + 38);

//         // no data
//         if (times.size() < 2) {
//             g.setFont(new Font("Arial", Font.PLAIN, 12));
//             g.drawString("Chưa đủ dữ liệu spawn", x + 20, y + 70);
//             g.dispose();
//             return;
//         }

//         // ===== plot area =====
//         int leftPad = 44;
//         int rightPad = 26;
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

//         double firstTime = times.get(0);
//         double lastTime = times.get(times.size() - 1);

//         double[] xSecs = new double[times.size()];
//         double[] ySecs = new double[intervals.size()];

// //        for (int i = 0; i < times.size(); i++) {
// //            xSecs[i] = times.get(i) - firstTime;   // simulation elapsed time
// //            ySecs[i] = intervals.get(i);  // planned/used spawn interval in seconds
//         ////            if (xSecs[i] > maxX) {
// ////                maxX = xSecs[i];
// ////            }
// ////            if (ySecs[i] > maxY) {
// ////                maxY = ySecs[i];
// ////            }
// //        }

// //        if (maxX <= 0) {
// //            maxX = 1.0;
// //        }
// //        if (maxY <= 0) {
// //            maxY = 1.0;
// //        }

//       double yTop = totalAgents / 8 + 5;
// //        double yTop = Math.ceil(maxY / 5.0) * 5.0;
// //        if (yTop < 5.0) {
// //            yTop = 5.0;
// //        }

//         double yMid = yTop * 0.6;   // giống style mẫu: 1 đường giữa
//         if (yMid < 1.0) {
//             yMid = yTop / 2.0;
//         }
//         double minX = 15.0;
//         double maxX = 120.0;

//         double minY = 0.0;
//         double maxY = yTop; // giữ nguyên logic của bạn
//         // ===== dashed guide lines =====
//         Stroke oldStroke = g.getStroke();
//         Stroke dashed = new BasicStroke(
//                 1.4f,
//                 BasicStroke.CAP_BUTT,
//                 BasicStroke.JOIN_BEVEL,
//                 0,
//                 new float[]{8f, 8f},
//                 0
//         );
//         g.setStroke(dashed);
//         g.setColor(Color.DARK_GRAY);

//         int yTopPix = py + (int) ((1.0 - yTop / yTop) * ph);
//         int yMidPix = py + (int) ((1.0 - yMid / yTop) * ph);

//         g.drawLine(px, yTopPix, px + pw, yTopPix);
//         g.drawLine(px, yMidPix, px + pw, yMidPix);

//         g.setStroke(oldStroke);
//         g.setColor(Color.BLACK);

//         // ===== y labels =====
//         g.setFont(new Font("Arial", Font.PLAIN, 13));
//         g.drawString(String.format("%.0fs", yTop), x + 8, yTopPix + 5);
//         g.drawString(String.format("%.0fs", yMid), x + 8, yMidPix + 5);
//         g.drawString("0", x + 18, py + ph + 5);

//         // ===== line + markers =====
//         g.setColor(new Color(30, 110, 200));
//         g.setStroke(new BasicStroke(2.5f));

//         int prevX = -1;
//         int prevY = -1;

//         for (int i = 0; i < xSecs.length; i++) {
//             int cx = px + (int) (((xSecs[i] - minX) / (maxX - minX)) * pw);
//             int cy = py + ph - (int) ((ySecs[i] / yTop) * ph);

//             if (prevX != -1) {
//                 g.drawLine(prevX, prevY, cx, cy);
//             }

//             prevX = cx;
//             prevY = cy;
//         }

//         // small round markers
//         for (int i = 0; i < xSecs.length; i++) {
//             int cx = px + (int) ((xSecs[i] / maxX) * pw);
//             int cy = py + ph - (int) ((ySecs[i] / yTop) * ph);

//             int r = 2;

//             g.setColor(Color.RED);
//             g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
//         }

//         // ===== current status text =====
//         double currentElapsed = lastTime - firstTime;
//         double currentInterval = intervals.get(intervals.size() - 1);

//         g.setFont(new Font("Arial", Font.PLAIN, 12));
//         g.setColor(Color.BLACK);
//         String status = String.format("Current: %.1fs (Interval: %.1fs)", currentElapsed, currentInterval);
//         g.drawString(status, x + 20, y + height - 22);

//         g.dispose();
//     }

//     private void drawSpawnHistogram(Graphics2D g2, int x, int y, int width, int height) {
//         List<Double> intervals;

//         synchronized (spawnChartLock) {
//             intervals = new ArrayList<>(spawnIntervalsSec);
//         }

//         Graphics2D g = (Graphics2D) g2.create();
//         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

//         // ===== outer panel =====
//         g.setColor(Color.WHITE);
//         g.fillRoundRect(x, y, width, height, 18, 18);
//         g.setColor(Color.BLACK);
//         g.setStroke(new BasicStroke(2f));
//         g.drawRoundRect(x, y, width, height, 18, 18);

//         // ===== title =====
//         g.setFont(new Font("Arial", Font.BOLD, 13));
//         String title = "Spawn Interval Histogram";
//         FontMetrics titleFm = g.getFontMetrics();
//         int titleX = x + (width - titleFm.stringWidth(title)) / 2;
//         g.drawString(title, titleX, y + 38);

//         // bỏ điểm đầu tiên = 0.0 nếu có
//         List<Double> values = new ArrayList<>();
//         for (double v : intervals) {
//             if (v > 0.0) {
//                 values.add(v);
//             }
//         }

//         if (values.size() < 2) {
//             g.setFont(new Font("Arial", Font.PLAIN, 12));
//             g.drawString("In Proccess", x + 20, y + 70);
//             g.dispose();
//             return;
//         }

//         // ===== plot area =====
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

//         // ===== histogram bins (FIXED RANGE) =====
//         double minX = 15.0;
//         double maxX = 120.0;
//         int binCount = 8;

//         double binSize = (maxX - minX) / binCount;
//         int[] counts = new int[binCount];

//         for (double v : values) {
//             double clamped = Math.max(minX, Math.min(v, maxX));

//             int bin = (int) ((clamped - minX) / binSize);
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

//         // ===== dashed middle guide =====
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

//         // ===== draw bars =====
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

//         // ===== x labels (FIXED) =====
//         g.setFont(new Font("Arial", Font.PLAIN, 12));

//         // min = 15
//         g.drawString("15", px - 5, y + height - 40);

//         // max = 120
//         g.drawString("120", px + pw - 25, y + height - 40);

//         // mid
//         double midX = (minX + maxX) / 2.0;
//         int midXPix = px + (int) (((midX - minX) / (maxX - minX)) * pw);
//         g.drawString(String.valueOf((int) midX), midXPix - 10, y + height - 40);

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

//                     // Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
//                     // Simulator.instance.setAgentRadius(agentNo, 0);
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

//         // giới hạn size để không lag
//         if (timeHistory.size() > 5000) {
//             timeHistory.remove(0);
//             agentHistory.remove(0);
//         }
//         repaint();
//     }

//     double smooth(double x) {
//         return x * x * (3 - 2 * x);
//     }


//     private boolean isActiveAgent(int agentNo) {
//         return agentNo >= 0
//                 && agentNo < goalsList.size()
//                 && goalsList.get(agentNo) != null
//                 && goalsList.get(agentNo).getFlag() == 1;
//     }

//     private boolean routeUsesB(Goal goal) {
//         int src = goal.getSource();
//         return src == 0 || src == 3;
//     }

//     private boolean routeUsesF(Goal goal) {
//         int src = goal.getSource();
//         int dst = goal.getDestination();
//         return src == 7 || dst == 6 || dst == 8;
//     }

//     private boolean routeUsesConnector(Goal goal) {
//         int src = goal.getSource();
//         int dst = goal.getDestination();
//         return (src == 0 || src == 3) && (dst == 6 || dst == 8);
//     }

//     private boolean isInConnector(Vector2D p) {
//         if (node1Pos == null || node5Pos == null || p == null) {
//             return false;
//         }

//         double minX = Math.min(node1Pos.getX(), node5Pos.getX()) + RESERVATION_RELEASE_RADIUS * 0.45;
//         double maxX = Math.max(node1Pos.getX(), node5Pos.getX()) - RESERVATION_RELEASE_RADIUS * 0.45;
//         double midY = (node1Pos.getY() + node5Pos.getY()) * 0.5;

//         return p.getX() >= minX
//                 && p.getX() <= maxX
//                 && Math.abs(p.getY() - midY) <= CONNECTOR_HALF_WIDTH;
//     }

//     private int countConnectorAgents(int excludeAgent) {
//         int count = 0;
//         int numAgents = Simulator.instance.getNumAgents();
//         for (int i = 0; i < numAgents; i++) {
//             if (i == excludeAgent || !isActiveAgent(i)) {
//                 continue;
//             }
//             Vector2D pos = Simulator.instance.getAgentPosition(i);
//             if (isInConnector(pos)) {
//                 count++;
//             }
//         }
//         return count;
//     }

//     private boolean downstreamClearFor(ReservationZone zone, int agentNo, Goal goal) {
//         if (zone == zoneB && routeUsesConnector(goal)) {
//             return countConnectorAgents(agentNo) < MAX_CONNECTOR_AGENTS;
//         }
//         return true;
//     }

//     private boolean shouldControlZone(ReservationZone zone, int agentNo, Goal goal, Vector2D pos) {
//         if (zone == null || zone.center == null || pos == null || zone.cleared.contains(agentNo)) {
//             return false;
//         }
//         if (zone == zoneB && !routeUsesB(goal)) {
//             return false;
//         }
//         if (zone == zoneF && !routeUsesF(goal)) {
//             return false;
//         }
//         return pos.distance(zone.center) <= RESERVATION_REQUEST_RADIUS;
//     }

//     private void removeInactiveFromQueue(ReservationZone zone) {
//         zone.queue.removeIf(agent -> !isActiveAgent(agent) || zone.cleared.contains(agent));
//     }

//     private void refreshReservationZone(ReservationZone zone) {
//         if (zone == null || zone.center == null) {
//             return;
//         }

//         removeInactiveFromQueue(zone);

//         if (zone.owner >= 0) {
//             if (!isActiveAgent(zone.owner)) {
//                 zone.owner = -1;
//             } else {
//                 Vector2D ownerPos = Simulator.instance.getAgentPosition(zone.owner);
//                 if (ownerPos == null) {
//                     zone.owner = -1;
//                 } else {
//                     double dist = ownerPos.distance(zone.center);
//                     if (dist <= RESERVATION_ENTER_RADIUS) {
//                         zone.entered.add(zone.owner);
//                     }

//                     // Release only after the owner has actually entered and then left.
//                     if (zone.entered.contains(zone.owner) && dist > RESERVATION_RELEASE_RADIUS) {
//                         zone.cleared.add(zone.owner);
//                         zone.entered.remove(zone.owner);
//                         zone.owner = -1;
//                     }
//                 }
//             }
//         }
//     }

//     private boolean ownsOrCanReserve(ReservationZone zone, int agentNo, Goal goal) {
//         refreshReservationZone(zone);

//         if (zone.owner == agentNo) {
//             return true;
//         }

//         if (!zone.queue.contains(agentNo)) {
//             zone.queue.offer(agentNo);
//         }

//         removeInactiveFromQueue(zone);

//         Integer first = zone.queue.peek();
//         if (zone.owner < 0 && first != null && first == agentNo && downstreamClearFor(zone, agentNo, goal)) {
//             zone.owner = agentNo;
//             zone.queue.poll();
//             return true;
//         }

//         return false;
//     }

//     private ReservationZone requiredReservationZone(int agentNo, Goal goal, Vector2D pos) {
//         if (shouldControlZone(zoneB, agentNo, goal, pos)) {
//             return zoneB;
//         }
//         if (shouldControlZone(zoneF, agentNo, goal, pos)) {
//             return zoneF;
//         }
//         return null;
//     }

//     private Vector2D computeWaypointPreferredVelocity(int agentNo) {
//         Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//         Goal goal = goalsList.get(agentNo);
//         Vector2D goalVector;
//         int next = goal.getNext();

//         if (next < goal.getSubGoalSize()) {
//             goalVector = goal.getSubGoal().get(next).subtract(agentPos);
//         } else {
//             goalVector = goal.getGlobalGoal().subtract(agentPos);
//         }

//         double lengthSq = goalVector.getNormSq();
//         if (lengthSq > 1.0) {
//             goalVector = goalVector.scalarMultiply(1.0 / FastMath.sqrt(lengthSq));
//         }

//         return goalVector;
//     }

//     private void applySmallRandomPerturbation(int agentNo) {
//         final double angle = random1.nextDouble() * 2.0 * FastMath.PI;
//         final double distance = random1.nextDouble() * 0.00005;
//         Simulator.instance.setAgentPreferredVelocity(agentNo,
//                 Simulator.instance.getAgentPreferredVelocity(agentNo)
//                         .add(new Vector2D(FastMath.cos(angle), FastMath.sin(angle)).scalarMultiply(distance)));
//     }

//     private void applyReservationControlledVelocity(int agentNo) {
//         if (!isActiveAgent(agentNo)) {
//             Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
//             return;
//         }

//         Goal goal = goalsList.get(agentNo);
//         Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//         ReservationZone neededZone = requiredReservationZone(agentNo, goal, agentPos);

//         if (neededZone != null && !ownsOrCanReserve(neededZone, agentNo, goal)) {
//             Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
//             return;
//         }

//         Simulator.instance.setAgentPreferredVelocity(agentNo, computeWaypointPreferredVelocity(agentNo));
//         applySmallRandomPerturbation(agentNo);
//     }

//     @Override
//     protected void setPreferredVelocities() {
//         // Do not call super.setPreferredVelocities() here. The base method contains
//         // a local pairwise yield heuristic that can create circular waits. This
//         // method replaces it with a FIFO reservation controller for B and F.
//         refreshReservationZone(zoneB);
//         refreshReservationZone(zoneF);

//         double[][] behavior = createBehaviorMatrix();
//         double currentTime = Simulator.instance.getGlobalTime();
//         int gap = 50;
//         time = (System.currentTimeMillis() - intialMillis) * 0.001;

//         // Keep the dynamic BUSY/FREE exit state synchronized with simulator time.
//         updateTrafficState();

//         // Spawn new agents according to the selected interval distribution.
//         if (spawnedAgents < totalAgents && currentTime - lastSpawnTime >= spawnInterval) {
//             double usedInterval = spawnInterval;

//             spawnOneAgent(graph, behavior, rand, gap);
//             spawnedAgents++;
//             lastSpawnTime = currentTime;

//             // Log the interval that was actually used for the histogram.
//             recordSpawnEvent(currentTime, usedInterval);

//             // Generate the interval for the next spawn.
//             spawnInterval = getNextSpawnInterval();

//             /*
//              * Weibull-like staged arrival profile:
//              * - early period: sparse arrivals,
//              * - middle period: dense arrivals,
//              * - final period: sparse arrivals again.
//              */
//             if (distributionType == DISTRIBUTION_TYPE.Weibull) {
//                 time = (System.currentTimeMillis() - intialMillis) * 0.001;

//                 if (time < 15) {
//                     spawnInterval = 75;
//                 } else if (time < 25) {
//                     double t = (time - 20) / 15.0;
//                     double s = smooth(t);
//                     spawnInterval = 80 - 60 * s;
//                 } else if (time < 50) {
//                     double t = (time - 35) / 15.0;
//                     double s = smooth(t);
//                     spawnInterval = 20 + 60 * s;
//                 } else {
//                     spawnInterval = 100;
//                 }

//                 spawnInterval = Math.max(minSpawnInterval, Math.min(maxSpawnInterval, spawnInterval));
//             }
//         }

//         int numAgents = Simulator.instance.getNumAgents();

//         // First compute movement using the reservation controller.
//         for (int agentNo = 0; agentNo < numAgents; agentNo++) {
//             applyReservationControlledVelocity(agentNo);
//         }

//         // Then keep the existing exit BUSY/FREE slowdown and forward-neighbor braking.
//         for (int agentNo = 0; agentNo < numAgents; agentNo++) {
//             Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
//             if (pref == null || pref.getNorm() < 1e-6) {
//                 continue;
//             }

//             Goal goal = goalsList.get(agentNo);
//             if (goal == null || goal.getFlag() != 1) {
//                 continue;
//             }

//             /*
//              * BUSY-gate slow down.
//              * Final exits in TC8 are E(4), G(6), and I(8). When the exit status
//              * is BUSY, agents inside DETECTION_RADIUS smoothly reduce speed.
//              */
//             if (isBusy && (goal.getDestination() == 4 || goal.getDestination() == 6 || goal.getDestination() == 8)) {
//                 Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//                 Vector2D targetPos = goal.getGlobalGoal();
//                 double distToGoal = targetPos.subtract(agentPos).getNorm();

//                 if (distToGoal < DETECTION_RADIUS) {
//                     double speedFactor = distToGoal / DETECTION_RADIUS;
//                     speedFactor = Math.max(MIN_SPEED_FACTOR, speedFactor);
//                     pref = pref.scalarMultiply(speedFactor);
//                 }
//             }

//             /*
//              * Local forward-neighbor braking.
//              * If another active agent is almost directly ahead and inside the
//              * neighbor range, reduce preferred velocity to reduce pushing.
//              */
//             double prefNorm = pref.getNorm();
//             if (prefNorm < 1e-6) {
//                 continue;
//             }

//             Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
//             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//             double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
//             double minScale = 1.0;

//             for (int other = 0; other < numAgents; other++) {
//                 if (other == agentNo || !isActiveAgent(other)) {
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

//                 double safety = Simulator.instance.getAgentRadius(agentNo)
//                         + Simulator.instance.getAgentRadius(other) + 1.0;
//                 double desiredSpacing = Math.max(neighborDistance, safety + 1.0);

//                 double scale = (dist - safety) / (desiredSpacing - safety);
//                 scale = Math.max(0.0, Math.min(1.0, scale));

//                 if (scale < minScale) {
//                     minScale = scale;
//                 }
//             }

//             Vector2D finalPref = pref.scalarMultiply(minScale);
//             Simulator.instance.setAgentPreferredVelocity(agentNo, finalPref);
//         }
//     }


//     public static void main(String[] args) {
//         TestCase08_setting2_TESTING testCase = new TestCase08_setting2_TESTING();
//         testCase.runSimulation();
//     }

//     /**
//      * Custom simulation loop copied/adapted from Setting 1.
//      *
//      * The loop adds a Swing button that toggles trajectory drawing during the
//      * simulation. The step order is kept consistent with the base scenario:
//      * update visualization, compute preferred velocities, update sub-goals, and
//      * advance the RVO2 simulator.
//      */
//     @Override
//     protected void runSimulation() {
//         setupScenario();

//         JFrame frame = new JFrame(scenarioName);
//         frame.setLayout(new BorderLayout());
//         frame.setSize(w, h);

//         // Reset buttons for new simulation
//         simulationPaused = false;

//         togglePathButton = new JButton("Show Paths: OFF");
//         togglePathButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 showPathsDuringSimulation = !showPathsDuringSimulation;
//                 togglePathButton.setText("Show Paths: " + (showPathsDuringSimulation ? "ON" : "OFF"));
//             }
//         });

//         pauseResumeButton = new JButton("Pause");
//         pauseResumeButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 simulationPaused = !simulationPaused;
//                 pauseResumeButton.setText(simulationPaused ? "Resume" : "Pause");
//             }
//         });

//         javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
//         buttonPanel.add(togglePathButton);
//         buttonPanel.add(pauseResumeButton);
//         frame.add(buttonPanel, BorderLayout.NORTH);

//         frame.add(this, BorderLayout.CENTER);
//         frame.pack();
//         frame.setVisible(true);
//         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//         final int TARGET_FPS = 60;
//         final long FRAME_TIME = 1000 / TARGET_FPS;

//         do {
//             long frameStart = System.currentTimeMillis();

//             updateVisualization();

//             // Only step simulation if not paused
//             if (!simulationPaused) {
//                 setPreferredVelocities();
//                 updateSubGoal();
//                 Simulator.instance.doStep();
//                 totalSteps++;
//             } else {
//                 // Still repaint while paused
//                 repaint();
//             }

//             long frameElapsed = System.currentTimeMillis() - frameStart;
//             if (frameElapsed < FRAME_TIME) {
//                 try {
//                     Thread.sleep(FRAME_TIME - frameElapsed);
//                 } catch (InterruptedException e) {
//                     e.printStackTrace();
//                     Thread.currentThread().interrupt();
//                 }
//             }

//             if (agentCount == totalAgents) {
//                 break;
//             }

//             repaint();
//         } while (!reachedGoal());

//         ended = true;
//         updateVisualization();
//         printResults();
//     }
// }
package rvo2example;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import algorithms.dijkstra.model.Edge;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import rvo2.Goal;
import rvo2.Simulator;

//adding library for chart
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * TC8: Two intersections with eight possible flows.
 *
 * Layout mapping: A=0, B=1, C=2, D=3, E=4, F=5, G=6, H=7, I=8. B and F are
 * intersections. Agents can spawn from A, D, or H and exit at E, G, or I.
 *
 * This version includes the two missing Setting 1 features: 1) dynamic
 * BUSY/FREE exit-state logic, and 2) custom runSimulation() with a
 * trajectory-display toggle button.
 */
public class TestCase08_setting2_TESTING extends BaseTrafficScenario2_TESTING {

    private static final long serialVersionUID = 1L;

    //======================PARAMETERS==========================
    public TestCase08_setting2_TESTING() {
        scenarioName = "TC8: Two Intersections - 6 Rules";
        trafficType = "2-intersection-6-rules";
        totalAgents = 64;
    }

    // Counts spawned agents and helps rotate through shuffled uniform flows.
    private int spawnIndex = 0;

    // Temporary source/destination sequence used by the uniform flow scheduler.
    private List<int[]> flowOrder = new ArrayList<>();

    // Dynamic exit state copied from Setting 1. BUSY means agents slow down near exits.
    private boolean isBusy = false;

    // Use real wall-clock time for BUSY/FREE switching.
    // Do NOT use Simulator.instance.getGlobalTime() here because the simulation
    // advances once per frame, so 4-7 simulation steps can pass in less than
    // one real second when the UI runs at 60 FPS.
    //FOR CURVE CODE
    private static final int TURN_SEGMENTS = 6;
    private static final double INTERSECTION_OFFSET_RATIO = 0.10;
    private static final double ARC_RADIUS_RATIO = 1.0;
    private static final double CONNECTOR_STEP_RATIO = 0.45;

    private double roadGap = 50.0;

    private DijkstraAlgorithm dijkstra;
    private int graphA;
    private int graphD;
    private int graphE;
    private int graphG;
    private int graphH;
    private int graphI;

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

    private long nextSwitchWallTimeMs = 0L;
    private static final long MIN_STATUS_DURATION_MS = 4000L;
    private static final long MAX_STATUS_DURATION_MS = 7000L;

    private static final double DETECTION_RADIUS = 60.0;
    private static final double MIN_SPEED_FACTOR = 0.15;
    private int throughExitCount = 0;
    private int slowExitCount = 0;

    // Runtime trajectory display controlled by the button in runSimulation().
    private boolean showPathsDuringSimulation = false;
    private JButton togglePathButton;

    // Pause/Resume simulation flag
    private boolean simulationPaused = false;
    private JButton pauseResumeButton;
    int leftToTop1 = 0;
    int leftToTop2 = 0;
    int leftToRight = 0;
    int bottom1ToTop1 = 0;
    int bottom1ToTop2 = 0;
    int bottom1ToRight = 0;
    int bottom2ToTop2 = 0;
    int bottom2ToRight = 0;
    double time;
    double startTime = 0;
    double endTime = 0;
    static Random rand = new Random();
    String disType = "Weibull Distribution";
    int[] sources = {0, 3, 7}; // Added 7 for second bottom
    int[] destinations = {4, 6, 8}; // Added 6 and 8 for second intersection
    private List<Vector2D> nodePoints = new ArrayList<>();
    int spawnedAgents = 0;
    // spawn interval settings
    private DISTRIBUTION_TYPE distributionType = DISTRIBUTION_TYPE.Weibull;
    private double meanSpawnInterval = 60.0;
    private double stdSpawnInterval = 15.0;
    private double minSpawnInterval = 15.0;
    private double maxSpawnInterval = 120.0;
    private double randomMinSpawnInterval = 20.0;
    private double randomMaxSpawnInterval = 100.0;
    double spawnInterval = meanSpawnInterval;
    double lastSpawnTime = 0;
    private Graph graph;
    private java.util.List<Double> timeHistory = new java.util.ArrayList<>();
    private java.util.List<Integer> agentHistory = new java.util.ArrayList<>();
    double intialMillis = System.currentTimeMillis();

    // Added positions for nodes 1 and 5
    private Vector2D node1Pos;
    private Vector2D node5Pos;

    /*
     * Plan B reservation controller.
     *
     * These zones are simulation-level resources, not Java locks. An agent must
     * reserve the intersection before it can enter the conflict area. The
     * intersection chooses the next owner using route priority:
     *   1) A-B-F-G,
     *   2) D-B-E and H-F-I,
     *   3) right/turning movements,
     * with FIFO order inside each group.
     */
    private static final double RESERVATION_REQUEST_RADIUS = 70.0;
    private static final double RESERVATION_ENTER_RADIUS = 21.0;
    private static final double RESERVATION_RELEASE_RADIUS = 36.0;
    private static final double CONNECTOR_HALF_WIDTH = 32.0;
    private static final int MAX_CONNECTOR_AGENTS = 8;
    private static final double MAX_TURN_WAIT_TIME = 5.0;

    private enum MovementType {
        STRAIGHT, TURN
    }

    private enum RoutePriority {
        ABFG_HIGH,
        DBE_HFI_MEDIUM,
        TURN_LOW
    }

    private ReservationZone zoneB;
    private ReservationZone zoneF;

    private static class ReservationZone {

        final String name;
        Vector2D center;
        int owner = -1;
        final Queue<Integer> queue = new LinkedList<>();
        final Map<Integer, Double> queuedSince = new HashMap<>();
        final Set<Integer> cleared = new HashSet<>();
        final Set<Integer> entered = new HashSet<>();

        ReservationZone(String name) {
            this.name = name;
        }

        void reset(Vector2D center) {
            this.center = center;
            this.owner = -1;
            this.queue.clear();
            this.queuedSince.clear();
            this.cleared.clear();
            this.entered.clear();
        }
    }

    Map<Integer, Agent> agents = new HashMap<>();

    //adding fields for chart
    // private final List<Long> spawnTimesMs = new ArrayList<>();
    // private final List<Long> spawnIntervalsMs = new ArrayList<>();
    // private final Object spawnChartLock = new Object();
    // private long previousSpawnMs = -1L;
    private final List<Double> spawnTimesSec = new ArrayList<>();
    private final List<Double> spawnIntervalsSec = new ArrayList<>();
    private final Object spawnChartLock = new Object();

    //adding field for spawn rules
//   private final double[] spawnCycle = {10, 30, 50, 30};
    //adding type for distribution
    enum DISTRIBUTION_TYPE {
        Uniformed, Normal, Randomed, Weibull, Fixed
    }

// This class saves metadata of Agents
    class Agent {

        int id;
        double startTime;
        double endTime;
        int flow;
        double spawnProb;

        public Agent(
                int id,
                double startTime,
                double endTime,
                int flow,
                double spawnProb
        ) {
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
            double y = center.getY() - radius * Math.sin(angle); // đổi dấu ở đây

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
                startAngleDeg = 180;
                endAngleDeg = 90;
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
                startAngleDeg = 0;
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

    private IntersectionNodes buildIntersectionSubgraph(
            String prefix,
            Vector2D center,
            List<Vertex> nodes,
            List<Edge> edges
    ) {
        IntersectionNodes result = new IntersectionNodes();

        double laneOffset = roadGap * INTERSECTION_OFFSET_RATIO;
        double connectorStep = roadGap * CONNECTOR_STEP_RATIO;

        Vector2D westAnchorPos = new Vector2D(center.getX() - connectorStep, center.getY());
        Vector2D southAnchorPos = new Vector2D(center.getX(), center.getY() + connectorStep);
        Vector2D northAnchorPos = new Vector2D(center.getX(), center.getY() - connectorStep);
        Vector2D eastAnchorPos = new Vector2D(center.getX() + connectorStep, center.getY());

        result.westAnchor = addVertex(nodes, prefix + "_W", westAnchorPos);
        result.southAnchor = addVertex(nodes, prefix + "_S", southAnchorPos);
        result.northAnchor = addVertex(nodes, prefix + "_N", northAnchorPos);
        result.eastAnchor = addVertex(nodes, prefix + "_E", eastAnchorPos);

        // turning arcs actually used in your scenario
        result.westToNorthArc = createArcVertexChain(prefix + "_WN", center, CornerType.NW, nodes);
        result.southToEastArc = createArcVertexChain(prefix + "_SE", center, CornerType.SE, nodes);

        // straight connectors
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

        // W -> N turn
        {
            List<Integer> path = new ArrayList<>();
            path.add(result.westAnchor);
            path.addAll(result.westToNorthArc);
            path.add(result.northAnchor);
            addPathEdges(edges, nodes, path);
        }

        // S -> E turn
        {
            List<Integer> path = new ArrayList<>();
            path.add(result.southAnchor);
            path.addAll(result.southToEastArc);
            path.add(result.eastAnchor);
            addPathEdges(edges, nodes, path);
        }

        // W -> E straight
        {
            List<Integer> path = new ArrayList<>();
            path.add(result.westAnchor);
            path.addAll(result.straightWestToEast);
            path.add(result.eastAnchor);
            addPathEdges(edges, nodes, path);
        }

        // S -> N straight
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
    //adding function to record spawn time and interval for chart
    // private void recordSpawnEvent(long nowMs) {
    //     synchronized (spawnChartLock) {
    //         spawnTimesMs.add(nowMs);

    //         if (previousSpawnMs < 0) {
    //             spawnIntervalsMs.add(0L);   // first point has no previous interval
    //         } else {
    //             spawnIntervalsMs.add(nowMs - previousSpawnMs);
    //         }
    //         previousSpawnMs = nowMs;
    //     }
    // }
    private void recordSpawnEvent(double simTimeSec, double usedIntervalSec) {
        synchronized (spawnChartLock) {
            spawnTimesSec.add(simTimeSec);
            spawnIntervalsSec.add(usedIntervalSec);
        }
    }

    /**
     * Toggle the exit state between BUSY and FREE using real wall-clock time.
     *
     * This prevents BUSY/FREE from flashing too quickly. The simulator global
     * time advances every doStep(), and runSimulation() calls doStep() near 60
     * times per real second. Therefore, a 4-7 simulation-step interval would
     * only last around 0.07-0.12 real seconds.
     */
    private void updateTrafficState() {
        long currentWallTimeMs = System.currentTimeMillis();

        // First call: schedule the first status change but keep the initial state.
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

    /**
     * Convert a source/destination pair into the internal flow id.
     */
    private int getFlowId(int src, int dst) {
        if (src == 0 && dst == 4) {
            return 0; // A-B-E
        }
        if (src == 0 && dst == 8) {
            return 1; // A-B-F-I
        }
        if (src == 0 && dst == 6) {
            return 2; // A-B-F-G
        }
        if (src == 3 && dst == 4) {
            return 3; // D-B-E
        }
        if (src == 3 && dst == 8) {
            return 4; // D-B-F-I
        }
        if (src == 3 && dst == 6) {
            return 5; // D-B-F-G
        }
        if (src == 7 && dst == 8) {
            return 6; // H-F-I
        }
        if (src == 7 && dst == 6) {
            return 7; // H-F-G
        }
        return 0;
    }

    /**
     * Convert internal flow id to a readable route label for the dashboard.
     */
    private String getFlowName(int flow) {
        switch (flow) {
            case 0:
                return "A-B-E";
            case 1:
                return "A-F-I";
            case 2:
                return "A-F-G";
            case 3:
                return "D-B-E";
            case 4:
                return "D-F-I";
            case 5:
                return "D-F-G";
            case 6:
                return "H-F-I";
            case 7:
                return "H-F-G";
            default:
                return "Unknown";
        }
    }

    //This functions will return the src/dst base on Normal Distribution
    public static int chooseByNormal(int[] options, double mean, double std) {
        double arr[] = {0, 0, 0};
        double gaussian, value;
        for (int i = 0; i < 3; i++) {
            gaussian = rand.nextGaussian();
            value = mean + std * gaussian;
            value = Math.max(0, Math.min(1, value));
            arr[i] = value;
        }
        double max = -1;
        int index = 0;
        for (int i = 0; i < 3; i++) {
            if (max < arr[i]) {
                max = arr[i];
                index = i;
            }
        }
        return options[index];
    }

    /**
     * Centralized spawn-interval policy.
     *
     * The selected distribution controls only the time between spawns. The
     * source/destination selection is handled inside spawnOneAgent().
     */
    private double getNextSpawnInterval() {
        double next;

        switch (distributionType) {
            case Normal:
                disType = "Normal Distribution";
                next = meanSpawnInterval + stdSpawnInterval * rand.nextGaussian();
                break;

            case Randomed:
                disType = "Random Distribution";
                double r = rand.nextDouble();
                // sqrt(r) gives a non-uniform random interval biased toward larger values.
                next = randomMinSpawnInterval + (randomMaxSpawnInterval - randomMinSpawnInterval) * Math.sqrt(r);
                break;

            case Weibull:
                disType = "Weibull Distribution";
                // The staged Weibull-like profile is refined in setPreferredVelocities().
                next = 75.0;
                break;

            case Fixed:
                disType = "Fixed Distribution";
                next = meanSpawnInterval;
                break;

            case Uniformed:
            default:
                disType = "Uniform Distribution";
                next = minSpawnInterval + rand.nextDouble() * (maxSpawnInterval - minSpawnInterval);
                break;
        }

        return Math.max(minSpawnInterval, Math.min(maxSpawnInterval, next));
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

    //This functions shuffleflows for UniformDistribution
    private void shuffleFlows(Random rand) {

        flowOrder.clear();

        flowOrder.add(new int[]{0, 4});
        flowOrder.add(new int[]{0, 8});
        flowOrder.add(new int[]{0, 6});
        flowOrder.add(new int[]{3, 4});
        flowOrder.add(new int[]{3, 8});
        flowOrder.add(new int[]{3, 6});
        flowOrder.add(new int[]{7, 6});
        flowOrder.add(new int[]{7, 8});

        Collections.shuffle(flowOrder, rand);
    }
    //This is agents defined color
    private static final Color[] FLOW_COLORS = new Color[]{
        Color.BLACK, // flow 0
        Color.RED, // flow 1
        Color.ORANGE, // flow 2
        Color.YELLOW, // flow 3
        Color.GREEN, // flow 4
        Color.CYAN, // flow 5
        Color.BLUE, // flow 6
        new Color(128, 0, 128) // flow 7 (PURPLE)
    };
    //This function will spawn one agent with defined Dst and Src

    private void spawnOneAgent(
            Graph graph,
            double[][] behavior,
            Random rand,
            double gap
    ) {
        int k = Simulator.instance.getNumAgents();

        // //adding x for chart
        // long nowMs = System.currentTimeMillis();
        // // record chart data
        // recordSpawnEvent(nowMs);
        // Update agents information
        Agent agent = agents.get(k);
        if (agent == null) {
            agent = new Agent(k, 0, 0, 0, 0);
            agents.put(k, agent);
        }
        agent.startTime = System.currentTimeMillis();
        // This is Spawn Location Distribution
        //--------------------------------
        //        disType = "Normal Distribution";
        int src, dst;
        int logicalSrc, logicalDst;
        if (distributionType == DISTRIBUTION_TYPE.Normal) {
//                     src = chooseByNormal(sources, 0.5, 1);
//                    dst = chooseByNormal(destinations, 0.5, 1);
//                    if(src==7 && dst==4) {
//                    	double gaussian = rand.nextGaussian();
//                        double value = 0.5 + 1 * gaussian;
//                    	int id = ((Math.max(0, Math.min(1, value)))>0.5 ?1:2);
//                    	dst = destinations[id];
//                    }

            logicalSrc = chooseByNormal(new int[]{0, 3, 7}, 0.5, 1);
            logicalDst = chooseByNormal(new int[]{4, 6, 8}, 0.5, 1);

            if (logicalSrc == 7 && logicalDst == 4) {
                double gaussian = rand.nextGaussian();
                double value = 0.5 + gaussian;
                int id = ((Math.max(0, Math.min(1, value))) > 0.5 ? 1 : 2);
                logicalDst = new int[]{4, 6, 8}[id];
            }

            src = logicalSrc;
            dst = logicalDst;
        } //        disType = "Random Distribution";
        else if (distributionType == DISTRIBUTION_TYPE.Randomed || distributionType == DISTRIBUTION_TYPE.Weibull) {
//            	  src = sources[rand.nextInt(3)];
//            	  dst = destinations[rand.nextInt(3)];
//                if(src==7 && dst==4) {
//            dst = destinations[rand.nextInt(2)+1];
//              }
            int[] logicalSources = {0, 3, 7};
            int[] logicalDestinations = {4, 6, 8};

            logicalSrc = logicalSources[rand.nextInt(3)];
            logicalDst = logicalDestinations[rand.nextInt(3)];

            if (logicalSrc == 7 && logicalDst == 4) {
                logicalDst = logicalDestinations[rand.nextInt(2) + 1];
            }

            src = logicalSrc;
            dst = logicalDst;
        } //        disType = "Uniform Distribution";
        else {
            if (spawnIndex % 8 == 0) {
                shuffleFlows(rand);
            }
//            int[] flow = flowOrder.get(spawnIndex % 8);
//
//            src = flow[0];
//            dst = flow[1];
//
            int[] flow = flowOrder.get(spawnIndex % 8);

            logicalSrc = flow[0];
            logicalDst = flow[1];

            src = logicalSrc;
            dst = logicalDst;
        }

        //--------------------------------
        disType = distributionType.name() + " Distribution";

        spawnIndex++;
//NEW
//        int graphSrc = src;
//        int graphDst = dst;
//
//        for (Vertex v : graph.getVertexes()) {
//            if (v.getName().equals("A") && src == 0) graphSrc = v.getId();
//            if (v.getName().equals("D") && src == 3) graphSrc = v.getId();
//            if (v.getName().equals("H") && src == 7) graphSrc = v.getId();
//
//            if (v.getName().equals("E") && dst == 4) graphDst = v.getId();
//            if (v.getName().equals("G") && dst == 6) graphDst = v.getId();
//            if (v.getName().equals("I") && dst == 8) graphDst = v.getId();
//        }
        int graphSrc = logicalSrc;
        int graphDst = logicalDst;

        if (logicalSrc == 0) {
            graphSrc = graphA; 
        }else if (logicalSrc == 3) {
            graphSrc = graphD; 
        }else if (logicalSrc == 7) {
            graphSrc = graphH;
        }

        if (logicalDst == 4) {
            graphDst = graphE; 
        }else if (logicalDst == 6) {
            graphDst = graphG; 
        }else if (logicalDst == 8) {
            graphDst = graphI;
        }

        // Update metadata for agents
//        agent.flow = getFlowId(src, dst);
        agent.flow = getFlowId(logicalSrc, logicalDst);
        if (logicalSrc == 0 && logicalDst == 4) {
            leftToTop1++;
            agent.flow = 0;
        }
        if (logicalSrc == 0 && logicalDst == 8) {
            leftToTop2++;
            agent.flow = 1;
        }
        if (logicalSrc == 0 && logicalDst == 6) {
            leftToRight++;
            agent.flow = 2;
        }
        if (logicalSrc == 3 && logicalDst == 4) {
            bottom1ToTop1++;
            agent.flow = 3;
        }
        if (logicalSrc == 3 && logicalDst == 6) {
            bottom1ToRight++;
            agent.flow = 5;
        }
        if (logicalSrc == 3 && logicalDst == 8) {
            bottom1ToTop2++;
            agent.flow = 4;
        }
        if (logicalSrc == 7 && logicalDst == 8) {
            agent.flow = 6;
            bottom2ToTop2++;
        }
        if (logicalSrc == 7 && logicalDst == 6) {
            bottom2ToRight++;
            agent.flow = 7;
        }
        int behaviorIndex = rand.nextInt(7);
        System.out.println("\n=== K: " + k + " ===");
//
//        Simulator.instance.addAgent(
//                translateCoordinateVector(graph.getVertexes().get(src).getVertex1())
//        );
        Simulator.instance.addAgent(
                translateCoordinateVector(graph.getVertexes().get(graphSrc).getVertex1())
        );
        Simulator.instance.setAgentNeighborDistance(k, behavior[behaviorIndex][0]);
        Simulator.instance.setAgentMaxNeighbors(
                k,
                (int) behavior[behaviorIndex][1]
        );
        Simulator.instance.setAgentTimeHorizonAgents(k, behavior[behaviorIndex][2]);

        double radius = gap * 0.21;
        Simulator.instance.setAgentRadius(k, radius);
        Simulator.instance.setAgentMaxSpeed(k, behavior[behaviorIndex][4]);
//
//        Goal goal = new Goal(
//                translateCoordinateVector(graph.getVertexes().get(dst).getVertex1())
//        );
        Goal goal = new Goal(
                translateCoordinateVector(graph.getVertexes().get(graphDst).getVertex1())
        );

        goal.setColor(getColor());
        goal.setFlag(1);
//        goal.setSource(src);
//        goal.setDestination(dst);

        goal.setSource(logicalSrc);
        goal.setDestination(logicalDst);
//        Vector2D Global_Goal = translateCoordinateVector(
//                graph.getVertexes().get(dst).getVertex1()
//        );
        Vector2D Global_Goal = translateCoordinateVector(
                graph.getVertexes().get(graphDst).getVertex1()
        );
        goal.setGlobalGoal(Global_Goal);
        assignPathFromGraph(goal, graph, graphSrc, graphDst);

        goalsList.add(goal);
    }

    //This is a setup function which only run once to define the environment
    @Override
    protected void setupScenario() {

        startSearchTime = System.currentTimeMillis();
        intialMillis = System.currentTimeMillis();
        startTime = Simulator.instance.getGlobalTime();

        // Reset runtime state so rerunning the scenario does not keep old data.
        nodePoints.clear();
        timeHistory.clear();
        agentHistory.clear();
        agents.clear();
        isBusy = false;
        nextSwitchWallTimeMs = 0L;
        throughExitCount = 0;
        slowExitCount = 0;
        simulationPaused = false;

        leftToTop1 = 0;
        leftToTop2 = 0;
        leftToRight = 0;
        bottom1ToTop1 = 0;
        bottom1ToTop2 = 0;
        bottom1ToRight = 0;
        bottom2ToTop2 = 0;
        bottom2ToRight = 0;

        synchronized (spawnChartLock) {
            spawnTimesSec.clear();
            spawnIntervalsSec.clear();
        }

        //adding chart data reset
        // synchronized (spawnChartLock) {
        //     spawnTimesMs.clear();
        //     spawnIntervalsMs.clear();
        //     previousSpawnMs = -1L;
        // }
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

        //=============NODE CREATION===============
//        java.util.List<org.apache.commons.math3.geometry.euclidean.twod.Vector2D> nodePoints = new java.util.ArrayList<>();
        //=====Coordinates for Nodes Creation=======
        double yMid = startY + blockHeight + gap / 2.0;
        double xLeft = startX;
        double xBetween = startX + blockWidth + gap / 2.0;
        double xRight = startX + blockWidth * 3 + gap * 2; // Updated for two intersections
        double xBetween2 = startX + blockWidth * 2 + gap * 1.5; // Corrected position for second intersection
        double yTop = startY;
        double yBottom = yTop + blockHeight * 2 + gap;
        //     4     8
        // 0   1  2  5   6
        //     3     7     
        //	  xLeft 
        //  	-------------------->    
        //yTop  |    4     8	
        //yMid  |0   1  2  5   6(xRight)          
        //yBot  |    3     7     
        //		v   xB    xB2
        //      
        // for visualization the node location
        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xLeft, yMid)); //0

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yMid)); //1

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(((xBetween2 + xBetween) / 2), yMid)); //2

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yBottom)); //3

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween, yTop)); //4

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yMid)); //5

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xRight, yMid)); //6

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yBottom)); //7

        nodePoints.add(new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(xBetween2, yTop)); //8

//        java.util.List<algorithms.dijkstra.model.Vertex> nodes = new java.util.ArrayList<>();
//        java.util.List<algorithms.dijkstra.model.Edge> edges = new java.util.ArrayList<>();
//        for (int i = 0; i < nodePoints.size(); i++) {
//            org.apache.commons.math3.geometry.euclidean.twod.Vector2D p = nodePoints.get(i);
//            algorithms.dijkstra.model.Vertex v = new algorithms.dijkstra.model.Vertex("node_" + i, i, 0, "node_" + i, p, p, 0);
//            nodes.add(v);
//        }
//
//        createEdge(0, 1, 2, nodes, edges);
//        createEdge(2, 5, 6, nodes, edges);
//        createEdge(3, 1, 4, nodes, edges);
//        createEdge(7, 5, 8, nodes, edges);
//
//        graph = new Graph(nodes, edges);
        List<Vertex> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        // External endpoints only.
        // Keep original IDs for actual source/destination nodes:
        // 0=A, 3=D, 4=E, 6=G, 7=H, 8=I
        Map<Integer, Integer> logicalToGraph = new HashMap<>();

        logicalToGraph.put(0, addVertex(nodes, "A", nodePoints.get(0)));
        logicalToGraph.put(3, addVertex(nodes, "D", nodePoints.get(3)));
        logicalToGraph.put(4, addVertex(nodes, "E", nodePoints.get(4)));
        logicalToGraph.put(6, addVertex(nodes, "G", nodePoints.get(6)));
        logicalToGraph.put(7, addVertex(nodes, "H", nodePoints.get(7)));
        logicalToGraph.put(8, addVertex(nodes, "I", nodePoints.get(8)));

        // Build local subgraph around old node 1 and old node 5.
        IntersectionNodes inter1 = buildIntersectionSubgraph("B", nodePoints.get(1), nodes, edges);
        IntersectionNodes inter5 = buildIntersectionSubgraph("F", nodePoints.get(5), nodes, edges);

        // Connect external roads to local graph of intersection 1.
        addDirectedEdge(edges, nodes, logicalToGraph.get(0), inter1.westAnchor);   // A -> B_W
        addDirectedEdge(edges, nodes, logicalToGraph.get(3), inter1.southAnchor);  // D -> B_S

        addDirectedEdge(edges, nodes, inter1.northAnchor, logicalToGraph.get(4));  // B_N -> E

        // Road between intersection 1 and intersection 5:
        addDirectedEdge(edges, nodes, inter1.eastAnchor, inter5.westAnchor);        // B_E -> F_W

        // Connect external roads to local graph of intersection 5.
        addDirectedEdge(edges, nodes, logicalToGraph.get(7), inter5.southAnchor);   // H -> F_S
        addDirectedEdge(edges, nodes, inter5.northAnchor, logicalToGraph.get(8));   // F_N -> I
        addDirectedEdge(edges, nodes, inter5.eastAnchor, logicalToGraph.get(6));    // F_E -> G

        graph = new Graph(nodes, edges);
        dijkstra = new DijkstraAlgorithm(graph);

        // remap logical source/destination ids so spawn code still uses old labels
        Map<Integer, Integer> remap = new HashMap<>();
        remap.put(0, logicalToGraph.get(0));
        remap.put(3, logicalToGraph.get(3));
        remap.put(4, logicalToGraph.get(4));
        remap.put(6, logicalToGraph.get(6));
        remap.put(7, logicalToGraph.get(7));
        remap.put(8, logicalToGraph.get(8));

        // store graph-ready ids back into arrays used by spawning
        sources = new int[]{remap.get(0), remap.get(3), remap.get(7)};
        destinations = new int[]{remap.get(4), remap.get(6), remap.get(8)};

        System.out.println("Auto-generated Graph vertices: " + graph.getVertexes().size());
        System.out.println("Auto-generated Graph edges: " + graph.getEdges().size());

        graphA = logicalToGraph.get(0);
        graphD = logicalToGraph.get(3);
        graphE = logicalToGraph.get(4);
        graphG = logicalToGraph.get(6);
        graphH = logicalToGraph.get(7);
        graphI = logicalToGraph.get(8);

        // Added: Store positions for painting
        node1Pos = translateCoordinateVector(nodePoints.get(1));
        node5Pos = translateCoordinateVector(nodePoints.get(5));

        zoneB = new ReservationZone("B");
        zoneF = new ReservationZone("F");
        zoneB.reset(node1Pos);
        zoneF.reset(node5Pos);

        for (int i = 0; i < totalAgents; i++) {
            double start = rand.nextDouble() * 10;
            double end = start + rand.nextDouble() * 10;
            int flow = rand.nextInt(8);
            double prob = rand.nextDouble();

            Agent agent = new Agent(i, start, end, flow, prob);
            agents.put(i, agent);
        }
        spawnOneAgent(graph, behavior, rand, gap);
        spawnedAgents++;

        // initialize dynamic interval after first spawn
        // spawnInterval = getNextSpawnInterval();
        recordSpawnEvent(Simulator.instance.getGlobalTime(), 0.0); // first spawn has no previous interval
        spawnInterval = getNextSpawnInterval();
        lastSpawnTime = Simulator.instance.getGlobalTime();

        System.out.println("Scenario initialized: " + scenarioName);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw exit-status bars. Green = FREE, red = BUSY.
        if (nodePoints.size() >= 9) {
            double barGap = 50.0;
            double barThickness = 10.0;
            int[] exitNodes = {4, 6, 8};

            g2.setColor(isBusy ? Color.RED : Color.GREEN);
            for (int idx : exitNodes) {
                Vector2D p = nodePoints.get(idx);
                if (idx == 6) {
                    // Right exit G: vertical gate bar.
                    g2.fillRect((int) (p.getX() - 5), (int) (p.getY() - barGap / 2), (int) barThickness, (int) barGap);
                } else {
                    // Top exits E and I: horizontal gate bars.
                    g2.fillRect((int) (p.getX() - barGap / 2), (int) (p.getY() - 5), (int) barGap, (int) barThickness);
                }
            }
        }

        //	 Agent agent = agents.get(agentNo);
        if (Simulator.instance != null) {
            int numAgents = Math.min(Simulator.instance.getNumAgents(), goalsList.size());
            for (int agentNo = 0; agentNo < numAgents; agentNo++) {
                Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
                if (agentPos == null) {
                    continue;
                }

                Vector2D agentPos_cood = translateCoordinateBackVector(agentPos);
                int radius = (int) Simulator.instance.getAgentRadius(agentNo);

                if (goalsList.get(agentNo).getFlag() == 1) {
                    Agent agent = agents.get(agentNo);
                    int flow = agent.flow; // phải có hàm này
                    Color agentColor = FLOW_COLORS[flow % 8];
                    g2.setColor(agentColor);
                    g2.fillOval((int) agentPos_cood.getX() - 2, (int) agentPos_cood.getY() - 2, 4, 4);
                    // Draw oval instead of fill the Oval 
//                 g2.drawOval((int) agentPos_cood.getX() - radius, (int) agentPos_cood.getY() - radius,
//                         radius * 2, radius * 2);
                    g2.fillOval(
                            (int) agentPos_cood.getX() - radius,
                            (int) agentPos_cood.getY() - radius,
                            radius * 2,
                            radius * 2
                    );
                }

                // Draw trajectory while the toggle is ON, or always after the run ends.
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

        // Draw the circle at each intersection where priority flow algorithm will work  
//     int radius = 37;
//     g2.setColor(Color.GREEN);
//
//    //  Draw circle at node 1
//     if (node1Pos != null) {
//         g2.drawOval(
//            //  (int) node1Pos.getX() - radius,
//            //  (int) node1Pos.getY() - radius,
//             710 - radius,
//             540 - radius,
//             radius * 2,
//             radius * 2
//         );
//     }
//
//    //  Draw circle at node 5
//     if (node5Pos != null) {
//         g2.drawOval(
//             //  (int) node5Pos.getX() - radius,
//             //  (int) node5Pos.getY() - radius,
//             1210 - radius,
//             540 - radius,
//             radius * 2,
//             radius * 2
//         );
//     }
        // ===== Draw information on screen ==
        int startX = 20;
        int startY = 150;
        int circleSize = 12;
        int spacing = 20;
        g2.setColor(Color.BLACK);
        g2.drawString(disType, startX, startY - 50);

        g2.drawString("Simulation Time(s)", startX, startY - 30);

        g2.drawString("Agent's Color | Count | Time(s) " + String.format("%.2f", spawnInterval), startX, startY - 10);

        // Display dynamic exit status and exit counters.
        g2.setColor(isBusy ? Color.RED : new Color(0, 150, 0));
        g2.drawString("EXIT STATUS: " + (isBusy ? "BUSY" : "FREE"), startX + 170, startY - 70);
        g2.setColor(new Color(0, 120, 0));
        g2.drawString("Normal Exits: " + throughExitCount, startX + 170, startY - 50);
        g2.setColor(Color.RED);
        g2.drawString("Slow Exits: " + slowExitCount, startX + 170, startY - 35);
        g2.setColor(Color.BLACK);

        int[] flowCount = {
            leftToTop1,
            leftToTop2,
            leftToRight,
            bottom1ToTop1,
            bottom1ToTop2,
            bottom1ToRight,
            bottom2ToTop2,
            bottom2ToRight
        };
        int[] src_dst_count = {
            leftToTop1 + leftToTop2 + leftToRight,
            bottom1ToTop1 + bottom1ToTop2 + bottom1ToRight,
            bottom2ToTop2 + bottom2ToRight,
            leftToTop1 + bottom1ToTop1,
            leftToTop2 + bottom1ToTop2 + bottom2ToTop2,
            leftToRight + bottom1ToRight + bottom2ToRight
        };

        double[] avgTime = new double[8];
        int[] countFlow = new int[8];
        if (ended) {
            for (int i = 0; i < totalAgents; i++) {
                Agent agent = agents.get(i);
                if (agent.endTime > 0) {
                    int f = agent.flow;
                    avgTime[f] += agent.getDuration();
                    countFlow[f]++;
                }
            }

            for (int i = 0; i < 8; i++) {
                if (countFlow[i] > 0) {
                    avgTime[i] /= countFlow[i];
                }
            }
            long endTime = System.currentTimeMillis();
            double time = (endTime - startSearchTime) * 0.001;

            g2.drawString(String.format("%.2f", time), startX + 110, startY - 30);

        }

        for (int i = 0; i < 8; i++) {
            int y = startY + i * spacing;

            g2.setColor(FLOW_COLORS[i]);
            g2.fillOval(startX, y - circleSize + 9, circleSize, circleSize);

            g2.setColor(Color.BLACK);
            String flowName = getFlowName(i);

            int col1X = startX + 20;
            int col2X = startX + 100;
            int col3X = startX + 140;
            if (ended && countFlow[i] > 0) {
                g2.drawString(String.format("%.2f", avgTime[i] * 0.001), col3X, y + 9);
            } else {
                g2.drawString("-", col3X, y + 9);
            }
            g2.drawString(flowName, col1X, y + 9);

            g2.drawString(String.valueOf(flowCount[i]), col2X, y + 9);

        }
        // ===== SOURCE / DESTINATION COUNT =====

        int startY2 = startY + 8 * spacing + 30;

        int colLeftX = startX;        // Source
        int colRightX = startX + 90; // Destination

        g2.setColor(Color.BLACK);
        g2.drawString("Source Count", colLeftX, startY2 - 20);
        g2.drawString("Destination Count", colRightX, startY2 - 20);

        // Source labels
        String[] sources = {"A", "D", "H"};
        // Destination labels
        String[] dests = {"E", "I", "G"};

        for (int i = 0; i < 3; i++) {
            int y = startY2 + i * spacing;

            // SOURCE
            g2.drawString(
                    sources[i] + ": " + src_dst_count[i],
                    colLeftX,
                    y
            );

            // DESTINATION
            g2.drawString(
                    dests[i] + ": " + src_dst_count[i + 3],
                    colRightX,
                    y
            );
        }

        //===== REALTIME GRAPH (FIXED SCALE) =====
        int graphX = startX + 15;
        int graphY = startY2 + 3 * spacing + 400;

        int graphWidth = 150;
        int graphHeight = 80;

//fixed range
        double maxTimeRange = 80.0;
        int maxAgentRange = 40;

//khung
        g2.setColor(Color.BLACK);
        g2.drawRect(graphX, graphY, graphWidth, graphHeight);

//label
        g2.drawString("Agents", graphX, graphY - 5);

        g2.drawString("0", graphX - 10, graphY + graphHeight);
        g2.drawString("40", graphX - 25, graphY + 10);

        g2.drawString("0", graphX, graphY + graphHeight + 15);
        g2.drawString("80", graphX + graphWidth - 20, graphY + graphHeight + 15);
        g2.drawString("Time(s)", graphX + graphWidth + 5, graphY + graphHeight + 7);

//vẽ line
        if (timeHistory.size() > 1) {
            g2.setColor(Color.BLUE);

            for (int i = 1; i < timeHistory.size(); i++) {
                double t1 = timeHistory.get(i - 1);
                double t2 = timeHistory.get(i);

                int a1 = agentHistory.get(i - 1);
                int a2 = agentHistory.get(i);

                t1 = Math.min(t1, maxTimeRange);
                t2 = Math.min(t2, maxTimeRange);

                a1 = Math.min(a1, maxAgentRange);
                a2 = Math.min(a2, maxAgentRange);

                int x1 = graphX + (int) (t1 / maxTimeRange * graphWidth);
                int x2 = graphX + (int) (t2 / maxTimeRange * graphWidth);

                int y1 = graphY + graphHeight - (int) ((double) a1 / maxAgentRange * graphHeight);
                int y2 = graphY + graphHeight - (int) ((double) a2 / maxAgentRange * graphHeight);

                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // ===== DRAW NODE LABELS =====
        if (nodePoints != null) {

            Graphics2D gText = (Graphics2D) g2.create();

            gText.setFont(new Font("Lexend", Font.BOLD, 24));
            gText.setColor(Color.BLACK);

            Random rand = new Random(42);

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

        //adding code to draw the chart
        int chartWidth = 210;
        int chartHeight = 320;
        int chartX = 10;
        int chartY = 425;
        // drawSpawnIntervalChart(g2, chartX, chartY, chartWidth, chartHeight);

        drawSpawnHistogram(g2, chartX, chartY, chartWidth, chartHeight);
    }

//adding method to draw the chart for spawn interval
    private void drawSpawnIntervalChart(Graphics2D g2, int x, int y, int width, int height) {
        List<Double> times;
        List<Double> intervals;

        synchronized (spawnChartLock) {
            times = new ArrayList<>(spawnTimesSec);
            intervals = new ArrayList<>(spawnIntervalsSec);
        }

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ===== outer panel =====
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, width, height, 18, 18);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, width, height, 18, 18);

        // ===== title =====
        g.setFont(new Font("Arial", Font.BOLD, 15));
        String title = "Spawn Interval (Second)";
        FontMetrics titleFm = g.getFontMetrics();
        int titleX = x + (width - titleFm.stringWidth(title)) / 2;
        g.drawString(title, titleX, y + 38);

        // no data
        if (times.size() < 2) {
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Chưa đủ dữ liệu spawn", x + 20, y + 70);
            g.dispose();
            return;
        }

        // ===== plot area =====
        int leftPad = 44;
        int rightPad = 26;
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

        double firstTime = times.get(0);
        double lastTime = times.get(times.size() - 1);

        double[] xSecs = new double[times.size()];
        double[] ySecs = new double[intervals.size()];

//        for (int i = 0; i < times.size(); i++) {
//            xSecs[i] = times.get(i) - firstTime;   // simulation elapsed time
//            ySecs[i] = intervals.get(i);  // planned/used spawn interval in seconds
        ////            if (xSecs[i] > maxX) {
////                maxX = xSecs[i];
////            }
////            if (ySecs[i] > maxY) {
////                maxY = ySecs[i];
////            }
//        }

//        if (maxX <= 0) {
//            maxX = 1.0;
//        }
//        if (maxY <= 0) {
//            maxY = 1.0;
//        }

      double yTop = totalAgents / 8 + 5;
//        double yTop = Math.ceil(maxY / 5.0) * 5.0;
//        if (yTop < 5.0) {
//            yTop = 5.0;
//        }

        double yMid = yTop * 0.6;   // giống style mẫu: 1 đường giữa
        if (yMid < 1.0) {
            yMid = yTop / 2.0;
        }
        double minX = 15.0;
        double maxX = 120.0;

        double minY = 0.0;
        double maxY = yTop; // giữ nguyên logic của bạn
        // ===== dashed guide lines =====
        Stroke oldStroke = g.getStroke();
        Stroke dashed = new BasicStroke(
                1.4f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0,
                new float[]{8f, 8f},
                0
        );
        g.setStroke(dashed);
        g.setColor(Color.DARK_GRAY);

        int yTopPix = py + (int) ((1.0 - yTop / yTop) * ph);
        int yMidPix = py + (int) ((1.0 - yMid / yTop) * ph);

        g.drawLine(px, yTopPix, px + pw, yTopPix);
        g.drawLine(px, yMidPix, px + pw, yMidPix);

        g.setStroke(oldStroke);
        g.setColor(Color.BLACK);

        // ===== y labels =====
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.drawString(String.format("%.0fs", yTop), x + 8, yTopPix + 5);
        g.drawString(String.format("%.0fs", yMid), x + 8, yMidPix + 5);
        g.drawString("0", x + 18, py + ph + 5);

        // ===== line + markers =====
        g.setColor(new Color(30, 110, 200));
        g.setStroke(new BasicStroke(2.5f));

        int prevX = -1;
        int prevY = -1;

        for (int i = 0; i < xSecs.length; i++) {
            int cx = px + (int) (((xSecs[i] - minX) / (maxX - minX)) * pw);
            int cy = py + ph - (int) ((ySecs[i] / yTop) * ph);

            if (prevX != -1) {
                g.drawLine(prevX, prevY, cx, cy);
            }

            prevX = cx;
            prevY = cy;
        }

        // small round markers
        for (int i = 0; i < xSecs.length; i++) {
            int cx = px + (int) ((xSecs[i] / maxX) * pw);
            int cy = py + ph - (int) ((ySecs[i] / yTop) * ph);

            int r = 2;

            g.setColor(Color.RED);
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        }

        // ===== current status text =====
        double currentElapsed = lastTime - firstTime;
        double currentInterval = intervals.get(intervals.size() - 1);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.BLACK);
        String status = String.format("Current: %.1fs (Interval: %.1fs)", currentElapsed, currentInterval);
        g.drawString(status, x + 20, y + height - 22);

        g.dispose();
    }

    private void drawSpawnHistogram(Graphics2D g2, int x, int y, int width, int height) {
        List<Double> intervals;

        synchronized (spawnChartLock) {
            intervals = new ArrayList<>(spawnIntervalsSec);
        }

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ===== outer panel =====
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, width, height, 18, 18);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, width, height, 18, 18);

        // ===== title =====
        g.setFont(new Font("Arial", Font.BOLD, 13));
        String title = "Spawn Interval Histogram";
        FontMetrics titleFm = g.getFontMetrics();
        int titleX = x + (width - titleFm.stringWidth(title)) / 2;
        g.drawString(title, titleX, y + 38);

        // bỏ điểm đầu tiên = 0.0 nếu có
        List<Double> values = new ArrayList<>();
        for (double v : intervals) {
            if (v > 0.0) {
                values.add(v);
            }
        }

        if (values.size() < 2) {
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("In Proccess", x + 20, y + 70);
            g.dispose();
            return;
        }

        // ===== plot area =====
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

        // ===== histogram bins (FIXED RANGE) =====
        double minX = 15.0;
        double maxX = 120.0;
        int binCount = 8;

        double binSize = (maxX - minX) / binCount;
        int[] counts = new int[binCount];

        for (double v : values) {
            double clamped = Math.max(minX, Math.min(v, maxX));

            int bin = (int) ((clamped - minX) / binSize);
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

        // ===== dashed middle guide =====
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

        // ===== draw bars =====
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

        // ===== x labels (FIXED) =====
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        // min = 15
        g.drawString("15", px - 5, y + height - 40);

        // max = 120
        g.drawString("120", px + pw - 25, y + height - 40);

        // mid
        double midX = (minX + maxX) / 2.0;
        int midXPix = px + (int) (((midX - minX) / (maxX - minX)) * pw);
        g.drawString(String.valueOf((int) midX), midXPix - 10, y + height - 40);

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

                    // Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
                    // Simulator.instance.setAgentRadius(agentNo, 0);
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

        // giới hạn size để không lag
        if (timeHistory.size() > 5000) {
            timeHistory.remove(0);
            agentHistory.remove(0);
        }
        repaint();
    }

    double smooth(double x) {
        return x * x * (3 - 2 * x);
    }


    private boolean isActiveAgent(int agentNo) {
        return agentNo >= 0
                && agentNo < goalsList.size()
                && goalsList.get(agentNo) != null
                && goalsList.get(agentNo).getFlag() == 1;
    }

    private boolean routeUsesB(Goal goal) {
        int src = goal.getSource();
        return src == 0 || src == 3;
    }

    private boolean routeUsesF(Goal goal) {
        int src = goal.getSource();
        int dst = goal.getDestination();
        return src == 7 || dst == 6 || dst == 8;
    }

    private boolean routeUsesConnector(Goal goal) {
        int src = goal.getSource();
        int dst = goal.getDestination();
        return (src == 0 || src == 3) && (dst == 6 || dst == 8);
    }

    private boolean isInConnector(Vector2D p) {
        if (node1Pos == null || node5Pos == null || p == null) {
            return false;
        }

        double minX = Math.min(node1Pos.getX(), node5Pos.getX()) + RESERVATION_RELEASE_RADIUS * 0.45;
        double maxX = Math.max(node1Pos.getX(), node5Pos.getX()) - RESERVATION_RELEASE_RADIUS * 0.45;
        double midY = (node1Pos.getY() + node5Pos.getY()) * 0.5;

        return p.getX() >= minX
                && p.getX() <= maxX
                && Math.abs(p.getY() - midY) <= CONNECTOR_HALF_WIDTH;
    }

    private int countConnectorAgents(int excludeAgent) {
        int count = 0;
        int numAgents = Simulator.instance.getNumAgents();
        for (int i = 0; i < numAgents; i++) {
            if (i == excludeAgent || !isActiveAgent(i)) {
                continue;
            }
            Vector2D pos = Simulator.instance.getAgentPosition(i);
            if (isInConnector(pos)) {
                count++;
            }
        }
        return count;
    }

    private boolean downstreamClearFor(ReservationZone zone, int agentNo, Goal goal) {
        if (zone == zoneB && routeUsesConnector(goal)) {
            return countConnectorAgents(agentNo) < MAX_CONNECTOR_AGENTS;
        }
        return true;
    }

    private boolean shouldControlZone(ReservationZone zone, int agentNo, Goal goal, Vector2D pos) {
        if (zone == null || zone.center == null || pos == null || zone.cleared.contains(agentNo)) {
            return false;
        }
        if (zone == zoneB && !routeUsesB(goal)) {
            return false;
        }
        if (zone == zoneF && !routeUsesF(goal)) {
            return false;
        }
        return pos.distance(zone.center) <= RESERVATION_REQUEST_RADIUS;
    }

    private void removeInactiveFromQueue(ReservationZone zone) {
        zone.queue.removeIf(agent -> !isActiveAgent(agent) || zone.cleared.contains(agent));
        zone.queuedSince.keySet().removeIf(agent -> !zone.queue.contains(agent));
    }

    private MovementType getMovementTypeAtZone(int agentNo, ReservationZone zone) {
        if (!isActiveAgent(agentNo)) {
            return MovementType.TURN;
        }

        RoutePriority priority = getRoutePriorityAtZone(agentNo, zone);
        return priority == RoutePriority.TURN_LOW ? MovementType.TURN : MovementType.STRAIGHT;
    }

    private RoutePriority getRoutePriorityAtZone(int agentNo, ReservationZone zone) {
        if (!isActiveAgent(agentNo)) {
            return RoutePriority.TURN_LOW;
        }

        Goal goal = goalsList.get(agentNo);
        int src = goal.getSource();
        int dst = goal.getDestination();

        // Highest priority: A-B-F-G. This route goes straight through B, then
        // straight through F to exit G. It is the main through-flow.
        if (src == 0 && dst == 6 && (zone == zoneB || zone == zoneF)) {
            return RoutePriority.ABFG_HIGH;
        }

        // Second priority: D-B-E at B and H-F-I at F. These are straight
        // vertical movements through their local intersection.
        if (zone == zoneB && src == 3 && dst == 4) {
            return RoutePriority.DBE_HFI_MEDIUM;
        }
        if (zone == zoneF && src == 7 && dst == 8) {
            return RoutePriority.DBE_HFI_MEDIUM;
        }

        // All other controlled movements are treated as lower-priority turns.
        return RoutePriority.TURN_LOW;
    }

    private int priorityRank(RoutePriority priority) {
        switch (priority) {
            case ABFG_HIGH:
                return 0;
            case DBE_HFI_MEDIUM:
                return 1;
            case TURN_LOW:
            default:
                return 2;
        }
    }

    private Integer peekNextAgentByPriority(ReservationZone zone) {
        removeInactiveFromQueue(zone);

        Integer bestAgent = null;
        int bestRank = Integer.MAX_VALUE;

        for (Integer candidate : zone.queue) {
            if (!isActiveAgent(candidate)) {
                continue;
            }

            Goal candidateGoal = goalsList.get(candidate);
            if (!downstreamClearFor(zone, candidate, candidateGoal)) {
                continue;
            }

            int candidateRank = priorityRank(getRoutePriorityAtZone(candidate, zone));

            // Lower rank means higher priority. Because we only replace when the
            // candidate has a strictly better rank, FIFO is preserved inside the
            // same priority group.
            if (bestAgent == null || candidateRank < bestRank) {
                bestAgent = candidate;
                bestRank = candidateRank;
            }
        }

        return bestAgent;
    }

    private void removeFromReservationQueue(ReservationZone zone, int agentNo) {
        zone.queue.remove(agentNo);
        zone.queuedSince.remove(agentNo);
    }

    private void refreshReservationZone(ReservationZone zone) {
        if (zone == null || zone.center == null) {
            return;
        }

        removeInactiveFromQueue(zone);

        if (zone.owner >= 0) {
            if (!isActiveAgent(zone.owner)) {
                zone.owner = -1;
            } else {
                Vector2D ownerPos = Simulator.instance.getAgentPosition(zone.owner);
                if (ownerPos == null) {
                    zone.owner = -1;
                } else {
                    double dist = ownerPos.distance(zone.center);
                    if (dist <= RESERVATION_ENTER_RADIUS) {
                        zone.entered.add(zone.owner);
                    }

                    // Release only after the owner has actually entered and then left.
                    if (zone.entered.contains(zone.owner) && dist > RESERVATION_RELEASE_RADIUS) {
                        zone.cleared.add(zone.owner);
                        zone.entered.remove(zone.owner);
                        zone.owner = -1;
                    }
                }
            }
        }
    }

    private boolean ownsOrCanReserve(ReservationZone zone, int agentNo, Goal goal) {
        refreshReservationZone(zone);

        if (zone.owner == agentNo) {
            return true;
        }

        if (!zone.queue.contains(agentNo)) {
            zone.queue.offer(agentNo);
            zone.queuedSince.put(agentNo, Simulator.instance.getGlobalTime());
        }

        removeInactiveFromQueue(zone);

        Integer nextOwner = peekNextAgentByPriority(zone);
        if (zone.owner < 0 && nextOwner != null && nextOwner == agentNo) {
            zone.owner = agentNo;
            removeFromReservationQueue(zone, agentNo);
            return true;
        }

        return false;
    }

    private ReservationZone requiredReservationZone(int agentNo, Goal goal, Vector2D pos) {
        if (shouldControlZone(zoneB, agentNo, goal, pos)) {
            return zoneB;
        }
        if (shouldControlZone(zoneF, agentNo, goal, pos)) {
            return zoneF;
        }
        return null;
    }

    private Vector2D computeWaypointPreferredVelocity(int agentNo) {
        Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
        Goal goal = goalsList.get(agentNo);
        Vector2D goalVector;
        int next = goal.getNext();

        if (next < goal.getSubGoalSize()) {
            goalVector = goal.getSubGoal().get(next).subtract(agentPos);
        } else {
            goalVector = goal.getGlobalGoal().subtract(agentPos);
        }

        double lengthSq = goalVector.getNormSq();
        if (lengthSq > 1.0) {
            goalVector = goalVector.scalarMultiply(1.0 / FastMath.sqrt(lengthSq));
        }

        return goalVector;
    }

    private void applySmallRandomPerturbation(int agentNo) {
        final double angle = random1.nextDouble() * 2.0 * FastMath.PI;
        final double distance = random1.nextDouble() * 0.00005;
        Simulator.instance.setAgentPreferredVelocity(agentNo,
                Simulator.instance.getAgentPreferredVelocity(agentNo)
                        .add(new Vector2D(FastMath.cos(angle), FastMath.sin(angle)).scalarMultiply(distance)));
    }

    private void applyReservationControlledVelocity(int agentNo) {
        if (!isActiveAgent(agentNo)) {
            Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
            return;
        }

        Goal goal = goalsList.get(agentNo);
        Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
        ReservationZone neededZone = requiredReservationZone(agentNo, goal, agentPos);

        if (neededZone != null && !ownsOrCanReserve(neededZone, agentNo, goal)) {
            Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
            return;
        }

        Simulator.instance.setAgentPreferredVelocity(agentNo, computeWaypointPreferredVelocity(agentNo));
        applySmallRandomPerturbation(agentNo);
    }

    @Override
    protected void setPreferredVelocities() {
        // Do not call super.setPreferredVelocities() here. The base method contains
        // a local pairwise yield heuristic that can create circular waits. This
        // method replaces it with a reservation controller for B and F. The
        // controller gives straight movements higher priority than turns, with
        // timeout protection so turning agents do not starve.
        refreshReservationZone(zoneB);
        refreshReservationZone(zoneF);

        double[][] behavior = createBehaviorMatrix();
        double currentTime = Simulator.instance.getGlobalTime();
        int gap = 50;
        time = (System.currentTimeMillis() - intialMillis) * 0.001;

        // Keep the dynamic BUSY/FREE exit state synchronized with simulator time.
        updateTrafficState();

        // Spawn new agents according to the selected interval distribution.
        if (spawnedAgents < totalAgents && currentTime - lastSpawnTime >= spawnInterval) {
            double usedInterval = spawnInterval;

            spawnOneAgent(graph, behavior, rand, gap);
            spawnedAgents++;
            lastSpawnTime = currentTime;

            // Log the interval that was actually used for the histogram.
            recordSpawnEvent(currentTime, usedInterval);

            // Generate the interval for the next spawn.
            spawnInterval = getNextSpawnInterval();

            /*
             * Weibull-like staged arrival profile:
             * - early period: sparse arrivals,
             * - middle period: dense arrivals,
             * - final period: sparse arrivals again.
             */
            if (distributionType == DISTRIBUTION_TYPE.Weibull) {
                time = (System.currentTimeMillis() - intialMillis) * 0.001;

                if (time < 15) {
                    spawnInterval = 75;
                } else if (time < 25) {
                    double t = (time - 20) / 15.0;
                    double s = smooth(t);
                    spawnInterval = 80 - 60 * s;
                } else if (time < 50) {
                    double t = (time - 35) / 15.0;
                    double s = smooth(t);
                    spawnInterval = 20 + 60 * s;
                } else {
                    spawnInterval = 100;
                }

                spawnInterval = Math.max(minSpawnInterval, Math.min(maxSpawnInterval, spawnInterval));
            }
        }

        int numAgents = Simulator.instance.getNumAgents();

        // First compute movement using the reservation controller.
        for (int agentNo = 0; agentNo < numAgents; agentNo++) {
            applyReservationControlledVelocity(agentNo);
        }

        // Then keep the existing exit BUSY/FREE slowdown and forward-neighbor braking.
        for (int agentNo = 0; agentNo < numAgents; agentNo++) {
            Vector2D pref = Simulator.instance.getAgentPreferredVelocity(agentNo);
            if (pref == null || pref.getNorm() < 1e-6) {
                continue;
            }

            Goal goal = goalsList.get(agentNo);
            if (goal == null || goal.getFlag() != 1) {
                continue;
            }

            /*
             * BUSY-gate slow down.
             * Final exits in TC8 are E(4), G(6), and I(8). When the exit status
             * is BUSY, agents inside DETECTION_RADIUS smoothly reduce speed.
             */
            if (isBusy && (goal.getDestination() == 4 || goal.getDestination() == 6 || goal.getDestination() == 8)) {
                Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
                Vector2D targetPos = goal.getGlobalGoal();
                double distToGoal = targetPos.subtract(agentPos).getNorm();

                if (distToGoal < DETECTION_RADIUS) {
                    double speedFactor = distToGoal / DETECTION_RADIUS;
                    speedFactor = Math.max(MIN_SPEED_FACTOR, speedFactor);
                    pref = pref.scalarMultiply(speedFactor);
                }
            }

            /*
             * Local forward-neighbor braking.
             * If another active agent is almost directly ahead and inside the
             * neighbor range, reduce preferred velocity to reduce pushing.
             */
            double prefNorm = pref.getNorm();
            if (prefNorm < 1e-6) {
                continue;
            }

            Vector2D prefDir = pref.scalarMultiply(1.0 / prefNorm);
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
            double neighborDistance = Simulator.instance.getAgentNeighborDistance(agentNo);
            double minScale = 1.0;

            for (int other = 0; other < numAgents; other++) {
                if (other == agentNo || !isActiveAgent(other)) {
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
        TestCase08_setting2_TESTING testCase = new TestCase08_setting2_TESTING();
        testCase.runSimulation();
    }

    /**
     * Custom simulation loop copied/adapted from Setting 1.
     *
     * The loop adds a Swing button that toggles trajectory drawing during the
     * simulation. The step order is kept consistent with the base scenario:
     * update visualization, compute preferred velocities, update sub-goals, and
     * advance the RVO2 simulator.
     */
    @Override
    protected void runSimulation() {
        setupScenario();

        JFrame frame = new JFrame(scenarioName);
        frame.setLayout(new BorderLayout());
        frame.setSize(w, h);

        // Reset buttons for new simulation
        simulationPaused = false;

        togglePathButton = new JButton("Show Paths: OFF");
        togglePathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPathsDuringSimulation = !showPathsDuringSimulation;
                togglePathButton.setText("Show Paths: " + (showPathsDuringSimulation ? "ON" : "OFF"));
            }
        });

        pauseResumeButton = new JButton("Pause");
        pauseResumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulationPaused = !simulationPaused;
                pauseResumeButton.setText(simulationPaused ? "Resume" : "Pause");
            }
        });

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.add(togglePathButton);
        buttonPanel.add(pauseResumeButton);
        frame.add(buttonPanel, BorderLayout.NORTH);

        frame.add(this, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final int TARGET_FPS = 60;
        final long FRAME_TIME = 1000 / TARGET_FPS;

        do {
            long frameStart = System.currentTimeMillis();

            updateVisualization();

            // Only step simulation if not paused
            if (!simulationPaused) {
                setPreferredVelocities();
                updateSubGoal();
                Simulator.instance.doStep();
                totalSteps++;
            } else {
                // Still repaint while paused
                repaint();
            }

            long frameElapsed = System.currentTimeMillis() - frameStart;
            if (frameElapsed < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - frameElapsed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }

            if (agentCount == totalAgents) {
                break;
            }

            repaint();
        } while (!reachedGoal());

        ended = true;
        updateVisualization();
        printResults();
    }
}
