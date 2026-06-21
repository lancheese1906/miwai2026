// // package rvo2example;

// // import java.awt.Color;
// // import java.awt.Dimension;
// // import java.awt.Graphics;
// // import java.awt.Graphics2D;
// // import java.awt.Polygon;
// // import java.util.ArrayList;
// // import java.util.LinkedList;
// // import java.util.List;
// // import java.util.Random;

// // import javax.swing.JFrame;
// // import javax.swing.JPanel;
// // import javax.swing.WindowConstants;

// // import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
// // import org.apache.commons.math3.util.FastMath;

// // import algorithms.dijkstra.engine.DijkstraAlgorithm;
// // import algorithms.dijkstra.model.Graph;
// // import algorithms.dijkstra.model.Vertex;
// // import rvo2.Goal;
// // import rvo2.Simulator;

// // public abstract class BaseTrafficScenario2_TESTING extends JPanel {

// //     private static final long serialVersionUID = 1L;

// //     protected List<Goal> goalsList = new ArrayList<Goal>();
// //     protected Random random1 = new Random();
// //     protected Random random2 = new Random(100);
// //     protected Random random3 = new Random(200);

// //     protected List<Obs> obsList1;
// //     protected boolean ended = false;
// //     protected int agentCount = 0;
// //     protected long startSearchTime = 0;
// //     protected long endSearchTime = 0;
// //     protected int sumTime = 0;

// //     public static int w = 1920;
// //     public static int h = 1080;
// //     public boolean BDI_ENABLE = true;

// //     // Metrics tracking
// //     protected long totalSimulationTime = 0;
// //     protected double avgAgentTime = 0;
// //     protected int totalSteps = 0;

// //     protected String scenarioName = "Default Scenario";
// //     protected int totalAgents = 0;
// //     protected String trafficType = "unknown"; //

// //     public BaseTrafficScenario2_TESTING() {
// //         setPreferredSize(new Dimension(w, h));
// //         setOpaque(true);
// //         setBackground(Color.WHITE);
// //         setForeground(Color.RED);
// //     }

// //     @Override
// //     protected void paintComponent(Graphics g) {
// //         super.paintComponent(g);
// //         Graphics2D g2 = (Graphics2D) g;
// //         int point_radius = 1;
// //         // Draw Obstacles
// //         if (obsList1 != null) {
// //             for (int i = 0; i < obsList1.size(); i++) {
// //                 Obs polygonPoint = obsList1.get(i);
// //                 Polygon p = new Polygon();
// //                 for (int j = 0; j < polygonPoint.coord2.size(); j++) {
// //                     Vector2D v = translateCoordinateBackVector(polygonPoint.coord2.get(j));
// //                     p.addPoint((int) v.getX(), (int) v.getY());
// //                 }
// //                 g2.setColor(Color.RED);
// //                 g2.drawPolygon(p);
// //             }
// //         }




// //         // Show some simulation information
// //         g2.setColor(Color.BLACK);
// //         g2.setFont(g2.getFont().deriveFont(12f));
// //         g2.drawString(scenarioName + " - " + trafficType, 10, 20);
// //         g2.drawString("Agents: " + agentCount + "/" + totalAgents, 10, 40);
// //         g2.drawString("Step: " + (int) Simulator.instance.getGlobalTime(), 10, 60);
// //         g2.drawString("FPS: " + String.format("%.1f", getFPS()), 10, 80);

// //     }

// //     private double lastFPS = 0;
// //     private long lastFrameTime = System.currentTimeMillis();

// //     private double getFPS() {
// //         long now = System.currentTimeMillis();
// //         long delta = now - lastFrameTime;
// //         if (delta > 0) {
// //             lastFPS = 1000.0 / delta;
// //         }
// //         lastFrameTime = now;
// //         return lastFPS;
// //     }

// //     /**
// //      * This func will be overridden
// //      */
// //     protected abstract void setupScenario();

// //     /**
// //      * Behaviours Matrix
// //      */
// //     protected double[][] createBehaviorMatrix() {
// //         double[][] behavior = new double[7][5];

// //         int increDist = 3;

// //         //P
// //         behavior[0][0] = 15*increDist;
// //         behavior[0][1] = 40;
// //         behavior[0][2] = 38;
// //         behavior[0][3] = 2.4;
// //         behavior[0][4] = 3.55;
// //         //E
// //         behavior[1][0] = 15*increDist;
// //         behavior[1][1] = 23;
// //         behavior[1][2] = 32;
// //         behavior[1][3] = 3.4;
// //         behavior[1][4] = 3.55;
// //         //N
// //         behavior[2][0] = 15*increDist;
// //         behavior[2][1] = 9;
// //         behavior[2][2] = 29;
// //         behavior[2][3] = 2.6;
// //         behavior[2][4] = 3.25;
// //         //A
// //         behavior[3][0] = 15*increDist;
// //         behavior[3][1] = 20;
// //         behavior[3][2] = 31;
// //         behavior[3][3] = 2.6;
// //         behavior[3][4] = 3.55;
// //         //AT
// //         behavior[4][0] = 17*increDist;
// //         behavior[4][1] = 17;
// //         behavior[4][2] = 40;
// //         behavior[4][3] = 2.4;
// //         behavior[4][4] = 3.55;
// //         //H
// //         behavior[5][0] = 15*increDist;
// //         behavior[5][1] = 7;
// //         behavior[5][2] = 30;
// //         behavior[5][3] = 2.1;
// //         behavior[5][4] = 3.25;
// //         //Default
// //         behavior[6][0] = 15*increDist;
// //         behavior[6][1] = 5;
// //         behavior[6][2] = 5;
// //         behavior[6][3] = 3;
// //         behavior[6][4] = 3.25;
// //         return behavior;
// //     }

// // //         Add agent
// //      protected void addAgent(Graph graph, int behaviorIndex, double[][] behavior, int agentNo) {
// //         int rand = random2.nextInt(graph.getVertexes().size() - 1);
// //         int rand_source = random3.nextInt(graph.getVertexes().size() - 1);

// //         Simulator.instance.addAgent(translateCoordinateVector(graph.getVertexes().get(rand_source).getVertex1()));
// //         Simulator.instance.setAgentNeighborDistance(agentNo, behavior[behaviorIndex][0]);
// //         Simulator.instance.setAgentMaxNeighbors(agentNo, (int) behavior[behaviorIndex][1]);
// //         Simulator.instance.setAgentTimeHorizonAgents(agentNo, behavior[behaviorIndex][2]);
// //         Simulator.instance.setAgentRadius(agentNo, behavior[behaviorIndex][3]);
// //         Simulator.instance.setAgentMaxSpeed(agentNo, behavior[behaviorIndex][4]);

// //         goalsList.add(new Goal(translateCoordinateVector(graph.getVertexes().get(rand).getVertex1())));
// //         goalsList.get(agentNo).setColor(getColor());
// //         goalsList.get(agentNo).setFlag(1);
// //         goalsList.get(agentNo).setSource(rand_source);
// //         goalsList.get(agentNo).setDestination(rand);
// //     }

// //     protected void setupObstacles() {
// //         ObsGenerator obs = new ObsGenerator(1, 1);
// //         obsList1 = obs.genSquareBlocksList5();
// //         System.out.println("Obstacles: " + obsList1.size());

// //         for (int k = 0; k < obsList1.size(); k++) {
// //             Obs o = obsList1.get(k);
// //             Simulator.instance.addObstacle(o.coord2);
// //         }


// //         Simulator.instance.processObstacles();
// //     }

// //     protected LinkedList<Vertex> getShortestPath(DijkstraAlgorithm dijkstra, Vertex source, Vertex destination) {
// //         dijkstra.execute(source);
// //         LinkedList<Vertex> path = dijkstra.getPath(destination);
// //         if (path == null || path.isEmpty()) {
// //             return null;
// //         }
// //         return simplifyPath(path);
// //     }

// //     protected LinkedList<Vertex> simplifyPath(LinkedList<Vertex> path) {
// //         if (path.size() <= 2) return path;

// //         LinkedList<Vertex> simplified = new LinkedList<>();
// //         simplified.add(path.get(0));

// //         for (int i = 1; i < path.size() - 1; i += 2) {
// //             simplified.add(path.get(i));
// //         }

// //         simplified.add(path.get(path.size() - 1));
// //         return simplified;
// //     }

// //     protected Color getColor() {
// //         int r = random2.nextInt(255);
// //         int g = random2.nextInt(255);
// //         int b = random2.nextInt(255);
// //         return new Color(r, g, b);
// //     }

// //     protected Vector2D translateCoordinateVector(Vector2D v) {
// //         double xp = v.getX() - (w / 2);
// //         double yp = ((v.getY() - (h / 2)) * (-1));
// //         return new Vector2D(xp, yp);
// //     }

// //     protected Vector2D translateCoordinateBackVector(Vector2D v) {
// //         int xp = (int) v.getX() + (w / 2);
// //         int yp = (((int) v.getY() - (h / 2)) * (-1));
// //         return new Vector2D(xp, yp);
// //     }

// //     protected void updateVisualization() {

// //     }
// //     boolean isStraight(int src, int dst,Vector2D point ){
// //     	if((src==0 && dst == 8)) {
// //     		if(point.getX() == -250.0 && point.getY() == -0.0) {
// //     			return true;
// //     		}
// //     		else
// //     			return false;
// //     	}
// //      	if((src==3 && dst == 6)) {
// //     		if(point.getX() == -250.0 && point.getY() == -0.0) {
// //                 System.out.println("Bị kẹt" + point.getY());

// //     			return false;
// //     		}
// //     		else
// //     			return true;
// //     	}

// //     	if((src==0 && dst == 4) ||(src==7 && dst == 6) ||(src==3 && dst == 8)   ) {
// //     		return false;
// //     	}

// //         return (src==0 && dst==6) ||
// //                (src==3 && dst==4) ||
// //                (src==7 && dst==8);
// //     }

// //     protected void setPreferredVelocities() {
// //         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
// //             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
// //             Vector2D goalVector;
// //             int next = goalsList.get(agentNo).getNext();
// //             boolean shouldWait = false;
// //             if (next < goalsList.get(agentNo).getSubGoalSize() ) {

// //                 Vector2D currentWaypoint = goalsList.get(agentNo).getSubGoal().get(next);

// //                 double distToNext = agentPos.distance(currentWaypoint);
// //                 //     4     8
// //                 // 0   1  2  5   6
// //                 //     3     7

// //                 if(distToNext < 32){

// //                     int srcA = goalsList.get(agentNo).getSource();
// //                     int dstA = goalsList.get(agentNo).getDestination();
// //                     boolean straightA = isStraight(srcA, dstA,currentWaypoint);

// //                     for(int other = 0; other < Simulator.instance.getNumAgents(); other++){

// //                         if(other == agentNo) continue;

// //                         Vector2D otherPos = Simulator.instance.getAgentPosition(other);

// //                         int nextOther = goalsList.get(other).getNext();

// //                         if(nextOther >= goalsList.get(other).getSubGoalSize()) continue;

// //                         Vector2D otherWaypoint = goalsList.get(other).getSubGoal().get(nextOther);

// //                         double otherDist = otherPos.distance(otherWaypoint);
// //                         double twoAgentDist = otherPos.distance(agentPos);
// //                         if(otherDist < 32 && twoAgentDist<60){

// //                             int srcB = goalsList.get(other).getSource();
// //                             int dstB = goalsList.get(other).getDestination();
// //                             boolean straightB = isStraight(srcB, dstB,otherWaypoint);

// //                             if(!straightA && straightB){
// //                                 shouldWait = true;
// //                             }

// //                             if(straightA && straightB){
// //                                 if(otherDist < distToNext){
// //                                     shouldWait = true;
// //                                 }
// //                             }
// //                         }
// //                     }
// //                 }
// // //REMOVED FOR NEW  TURNING CODE
// // //                if (distToNext < 14.0) {
// // //                	goalsList.get(agentNo).setNext(next+ 1);
// // //
// // //                }
// //                   goalVector = currentWaypoint.subtract(agentPos);

// //             }
// //             else {
// //                 goalVector = goalsList.get(agentNo).getGlobalGoal().subtract(agentPos);
// //             }

// //             final double lengthSq = goalVector.getNormSq();
// //             if (lengthSq > 1.0) {
// //                 goalVector = goalVector.scalarMultiply(1 / FastMath.sqrt(lengthSq));
// //             }
// //             if(shouldWait){
// //                 goalVector = Vector2D.ZERO;
// //             }
// //             Simulator.instance.setAgentPreferredVelocity(agentNo, goalVector);

// //             final double angle = random1.nextDouble() * 2.0 * FastMath.PI;
// //             final double distance = random1.nextDouble() * 0.00005;
// //             Simulator.instance.setAgentPreferredVelocity(agentNo,
// //                     Simulator.instance.getAgentPreferredVelocity(agentNo)
// //                             .add(new Vector2D(FastMath.cos(angle), FastMath.sin(angle)).scalarMultiply(distance)));
// //         }

// //     }

// //     protected boolean reachedGoal() {
// //         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
// //             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
// //             if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) > 100.0) {
// //                 return false;
// //             }
// //         }
// //         return true;
// //     }

// //     protected void updateSubGoal() {
// //         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
// //             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
// //             int next = goalsList.get(agentNo).getNext();

// //             if (next < goalsList.get(agentNo).getSubGoalSize() - 1) {
// //                 double distSq = agentPos.distanceSq(goalsList.get(agentNo).getSubGoal().get(next));
// //                 if(distSq < 14.0*14.0) { // sqrt(900) = 30 units
// //                     goalsList.get(agentNo).setNext(next + 1);
// //                 }
// //             }
// //         }
// //     }

// //     protected void printResults() {
// //         long endTime = System.currentTimeMillis();
// //         totalSimulationTime = endTime - startSearchTime;
// //         totalSteps = (int) Simulator.instance.getGlobalTime();
// //         avgAgentTime = (totalAgents > 0) ? (double) sumTime / totalAgents : 0;

// //         System.out.println("\n=== RESULTS: " + scenarioName + " ===");
// //         System.out.println("Traffic Type: " + trafficType);
// //         System.out.println("Total Agents: " + totalAgents);
// //         System.out.println("Completed: " + agentCount);
// //         System.out.println("Total Simulation Time: " + totalSimulationTime + "ms");
// //         System.out.println("Total Steps: " + totalSteps);
// //         System.out.println("Avg Time per Agent: " + String.format("%.2f", avgAgentTime) + "ms");
// //         System.out.println("FPS: " + String.format("%.2f", totalSteps / (totalSimulationTime / 1000.0)));

// //         long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
// //         System.out.println("Memory Used: " + (memory / 1024 / 1024) + "MB");
// //         System.out.println("================================\n");
// //     }

// //     protected void runSimulation() {
// //         setupScenario();

// //         JFrame frame = new JFrame(scenarioName);
// //         frame.setSize(w, h);
// //         frame.add(this);
// //         frame.pack();
// //         frame.setVisible(true);
// //         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

// //         final int TARGET_FPS = 60;
// //         final long FRAME_TIME = 1000 / TARGET_FPS;

// //         long lastFrameTime = System.currentTimeMillis();
// //         do {
// //             long frameStart = System.currentTimeMillis();

// //             updateVisualization();
// //             setPreferredVelocities();
// //             updateSubGoal();
// //             Simulator.instance.doStep();

// //             totalSteps++;

// //             long frameElapsed = System.currentTimeMillis() - frameStart;
// //             if (frameElapsed < FRAME_TIME) {
// //                 try {
// //                     Thread.sleep(FRAME_TIME - frameElapsed);
// //                 } catch (InterruptedException e) {
// //                     e.printStackTrace();
// //                 }
// //             }

// //             lastFrameTime = System.currentTimeMillis();
// //             if(agentCount==totalAgents) {
// //             	break;
// //             }
// //         } while (!reachedGoal());

// //         ended = true;
// //         updateVisualization();
// //         printResults();
// //     }
// // }
// package rvo2example;

// import java.awt.Color;
// import java.awt.Dimension;
// import java.awt.Graphics;
// import java.awt.Graphics2D;
// import java.awt.Polygon;
// import java.util.ArrayList;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Random;

// import javax.swing.JFrame;
// import javax.swing.JPanel;
// import javax.swing.WindowConstants;

// import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
// import org.apache.commons.math3.util.FastMath;

// import algorithms.dijkstra.engine.DijkstraAlgorithm;
// import algorithms.dijkstra.model.Graph;
// import algorithms.dijkstra.model.Vertex;
// import rvo2.Goal;
// import rvo2.Simulator;

// public abstract class BaseTrafficScenario2_TESTING extends JPanel {

//     private static final long serialVersionUID = 1L;

//     protected List<Goal> goalsList = new ArrayList<Goal>();
//     protected Random random1 = new Random();
//     protected Random random2 = new Random(100);
//     protected Random random3 = new Random(200);

//     protected List<Obs> obsList1;
//     protected boolean ended = false;
//     protected int agentCount = 0;
//     protected long startSearchTime = 0;
//     protected long endSearchTime = 0;
//     protected int sumTime = 0;

//     public static int w = 1920;
//     public static int h = 1080;
//     public boolean BDI_ENABLE = true;

//     // Metrics tracking
//     protected long totalSimulationTime = 0;
//     protected double avgAgentTime = 0;
//     protected int totalSteps = 0;

//     protected String scenarioName = "Default Scenario";
//     protected int totalAgents = 0;
//     protected String trafficType = "unknown"; //

//     public BaseTrafficScenario2_TESTING() {
//         setPreferredSize(new Dimension(w, h));
//         setOpaque(true);
//         setBackground(Color.WHITE);
//         setForeground(Color.RED);
//     }

//     @Override
//     protected void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         Graphics2D g2 = (Graphics2D) g;
//         int point_radius = 1;
//         // Draw Obstacles
//         if (obsList1 != null) {
//             for (int i = 0; i < obsList1.size(); i++) {
//                 Obs polygonPoint = obsList1.get(i);
//                 Polygon p = new Polygon();
//                 for (int j = 0; j < polygonPoint.coord2.size(); j++) {
//                     Vector2D v = translateCoordinateBackVector(polygonPoint.coord2.get(j));
//                     p.addPoint((int) v.getX(), (int) v.getY());
//                 }
//                 g2.setColor(Color.RED);
//                 g2.drawPolygon(p);
//             }
//         }




//         // Show some simulation information
//         g2.setColor(Color.BLACK);
//         g2.setFont(g2.getFont().deriveFont(12f));
//         g2.drawString(scenarioName + " - " + trafficType, 10, 20);
//         g2.drawString("Agents: " + agentCount + "/" + totalAgents, 10, 40);
//         g2.drawString("Step: " + (int) Simulator.instance.getGlobalTime(), 10, 60);
//         g2.drawString("FPS: " + String.format("%.1f", getFPS()), 10, 80);

//     }

//     private double lastFPS = 0;
//     private long lastFrameTime = System.currentTimeMillis();

//     private double getFPS() {
//         long now = System.currentTimeMillis();
//         long delta = now - lastFrameTime;
//         if (delta > 0) {
//             lastFPS = 1000.0 / delta;
//         }
//         lastFrameTime = now;
//         return lastFPS;
//     }

//     /**
//      * This func will be overridden
//      */
//     protected abstract void setupScenario();

//     /**
//      * Behaviours Matrix
//      */
//     protected double[][] createBehaviorMatrix() {
//         double[][] behavior = new double[7][5];

//         int increDist = 3;

//         //P
//         behavior[0][0] = 15*increDist;
//         behavior[0][1] = 40;
//         behavior[0][2] = 38;
//         behavior[0][3] = 2.4;
//         behavior[0][4] = 3.55;
//         //E
//         behavior[1][0] = 15*increDist;
//         behavior[1][1] = 23;
//         behavior[1][2] = 32;
//         behavior[1][3] = 3.4;
//         behavior[1][4] = 3.55;
//         //N
//         behavior[2][0] = 15*increDist;
//         behavior[2][1] = 9;
//         behavior[2][2] = 29;
//         behavior[2][3] = 2.6;
//         behavior[2][4] = 3.25;
//         //A
//         behavior[3][0] = 15*increDist;
//         behavior[3][1] = 20;
//         behavior[3][2] = 31;
//         behavior[3][3] = 2.6;
//         behavior[3][4] = 3.55;
//         //AT
//         behavior[4][0] = 17*increDist;
//         behavior[4][1] = 17;
//         behavior[4][2] = 40;
//         behavior[4][3] = 2.4;
//         behavior[4][4] = 3.55;
//         //H
//         behavior[5][0] = 15*increDist;
//         behavior[5][1] = 7;
//         behavior[5][2] = 30;
//         behavior[5][3] = 2.1;
//         behavior[5][4] = 3.25;
//         //Default
//         behavior[6][0] = 15*increDist;
//         behavior[6][1] = 5;
//         behavior[6][2] = 5;
//         behavior[6][3] = 3;
//         behavior[6][4] = 3.25;
//         return behavior;
//     }

// //         Add agent
//      protected void addAgent(Graph graph, int behaviorIndex, double[][] behavior, int agentNo) {
//         int rand = random2.nextInt(graph.getVertexes().size() - 1);
//         int rand_source = random3.nextInt(graph.getVertexes().size() - 1);

//         Simulator.instance.addAgent(translateCoordinateVector(graph.getVertexes().get(rand_source).getVertex1()));
//         Simulator.instance.setAgentNeighborDistance(agentNo, behavior[behaviorIndex][0]);
//         Simulator.instance.setAgentMaxNeighbors(agentNo, (int) behavior[behaviorIndex][1]);
//         Simulator.instance.setAgentTimeHorizonAgents(agentNo, behavior[behaviorIndex][2]);
//         Simulator.instance.setAgentRadius(agentNo, behavior[behaviorIndex][3]);
//         Simulator.instance.setAgentMaxSpeed(agentNo, behavior[behaviorIndex][4]);

//         goalsList.add(new Goal(translateCoordinateVector(graph.getVertexes().get(rand).getVertex1())));
//         goalsList.get(agentNo).setColor(getColor());
//         goalsList.get(agentNo).setFlag(1);
//         goalsList.get(agentNo).setSource(rand_source);
//         goalsList.get(agentNo).setDestination(rand);
//     }

//     protected void setupObstacles() {
//         ObsGenerator obs = new ObsGenerator(1, 1);
//         obsList1 = obs.genSquareBlocksList5();
//         System.out.println("Obstacles: " + obsList1.size());

//         for (int k = 0; k < obsList1.size(); k++) {
//             Obs o = obsList1.get(k);
//             Simulator.instance.addObstacle(o.coord2);
//         }


//         Simulator.instance.processObstacles();
//     }

//     protected LinkedList<Vertex> getShortestPath(DijkstraAlgorithm dijkstra, Vertex source, Vertex destination) {
//         dijkstra.execute(source);
//         LinkedList<Vertex> path = dijkstra.getPath(destination);
//         if (path == null || path.isEmpty()) {
//             return null;
//         }
//         return simplifyPath(path);
//     }

//     protected LinkedList<Vertex> simplifyPath(LinkedList<Vertex> path) {
//         if (path.size() <= 2) return path;

//         LinkedList<Vertex> simplified = new LinkedList<>();
//         simplified.add(path.get(0));

//         for (int i = 1; i < path.size() - 1; i += 2) {
//             simplified.add(path.get(i));
//         }

//         simplified.add(path.get(path.size() - 1));
//         return simplified;
//     }

//     protected Color getColor() {
//         int r = random2.nextInt(255);
//         int g = random2.nextInt(255);
//         int b = random2.nextInt(255);
//         return new Color(r, g, b);
//     }

//     protected Vector2D translateCoordinateVector(Vector2D v) {
//         double xp = v.getX() - (w / 2);
//         double yp = ((v.getY() - (h / 2)) * (-1));
//         return new Vector2D(xp, yp);
//     }

//     protected Vector2D translateCoordinateBackVector(Vector2D v) {
//         int xp = (int) v.getX() + (w / 2);
//         int yp = (((int) v.getY() - (h / 2)) * (-1));
//         return new Vector2D(xp, yp);
//     }

//     protected void updateVisualization() {

//     }
//     boolean isStraight(int src, int dst,Vector2D point ){
//     	if((src==0 && dst == 8)) {
//     		if(point.getX() == -250.0 && point.getY() == -0.0) {
//     			return true;
//     		}
//     		else
//     			return false;
//     	}
//      	if((src==3 && dst == 6)) {
//     		if(point.getX() == -250.0 && point.getY() == -0.0) {
//                 System.out.println("Bị kẹt" + point.getY());

//     			return false;
//     		}
//     		else
//     			return true;
//     	}

//     	if((src==0 && dst == 4) ||(src==7 && dst == 6) ||(src==3 && dst == 8)   ) {
//     		return false;
//     	}

//         return (src==0 && dst==6) ||
//                (src==3 && dst==4) ||
//                (src==7 && dst==8);
//     }

//     protected void setPreferredVelocities() {
//         /*
//          * Default movement policy: follow the current sub-goal/global goal.
//          *
//          * The old implementation contained a local pairwise priority rule
//          * (turning yields to straight, straight-vs-straight yields by distance).
//          * That rule can create circular waits in dense traffic. Scenario-specific
//          * controllers, such as TC8's reservation controller, should implement
//          * intersection access explicitly instead of relying on local pairwise waits.
//          */
//         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
//             if (agentNo >= goalsList.size() || goalsList.get(agentNo).getFlag() != 1) {
//                 Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
//                 continue;
//             }

//             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//             Vector2D goalVector;
//             int next = goalsList.get(agentNo).getNext();

//             if (next < goalsList.get(agentNo).getSubGoalSize()) {
//                 Vector2D currentWaypoint = goalsList.get(agentNo).getSubGoal().get(next);
//                 goalVector = currentWaypoint.subtract(agentPos);
//             } else {
//                 goalVector = goalsList.get(agentNo).getGlobalGoal().subtract(agentPos);
//             }

//             final double lengthSq = goalVector.getNormSq();
//             if (lengthSq > 1.0) {
//                 goalVector = goalVector.scalarMultiply(1 / FastMath.sqrt(lengthSq));
//             }

//             Simulator.instance.setAgentPreferredVelocity(agentNo, goalVector);

//             final double angle = random1.nextDouble() * 2.0 * FastMath.PI;
//             final double distance = random1.nextDouble() * 0.00005;
//             Simulator.instance.setAgentPreferredVelocity(agentNo,
//                     Simulator.instance.getAgentPreferredVelocity(agentNo)
//                             .add(new Vector2D(FastMath.cos(angle), FastMath.sin(angle)).scalarMultiply(distance)));
//         }
//     }


//     protected boolean reachedGoal() {
//         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
//             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//             if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) > 100.0) {
//                 return false;
//             }
//         }
//         return true;
//     }

//     protected void updateSubGoal() {
//         for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
//             Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
//             int next = goalsList.get(agentNo).getNext();

//             if (next < goalsList.get(agentNo).getSubGoalSize() - 1) {
//                 double distSq = agentPos.distanceSq(goalsList.get(agentNo).getSubGoal().get(next));
//                 if(distSq < 14.0*14.0) { // sqrt(900) = 30 units
//                     goalsList.get(agentNo).setNext(next + 1);
//                 }
//             }
//         }
//     }

//     protected void printResults() {
//         long endTime = System.currentTimeMillis();
//         totalSimulationTime = endTime - startSearchTime;
//         totalSteps = (int) Simulator.instance.getGlobalTime();
//         avgAgentTime = (totalAgents > 0) ? (double) sumTime / totalAgents : 0;

//         System.out.println("\n=== RESULTS: " + scenarioName + " ===");
//         System.out.println("Traffic Type: " + trafficType);
//         System.out.println("Total Agents: " + totalAgents);
//         System.out.println("Completed: " + agentCount);
//         System.out.println("Total Simulation Time: " + totalSimulationTime + "ms");
//         System.out.println("Total Steps: " + totalSteps);
//         System.out.println("Avg Time per Agent: " + String.format("%.2f", avgAgentTime) + "ms");
//         System.out.println("FPS: " + String.format("%.2f", totalSteps / (totalSimulationTime / 1000.0)));

//         long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//         System.out.println("Memory Used: " + (memory / 1024 / 1024) + "MB");
//         System.out.println("================================\n");
//     }

//     protected void runSimulation() {
//         setupScenario();

//         JFrame frame = new JFrame(scenarioName);
//         frame.setSize(w, h);
//         frame.add(this);
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
//             if(agentCount==totalAgents) {
//             	break;
//             }
//         } while (!reachedGoal());

//         ended = true;
//         updateVisualization();
//         printResults();
//     }
// }
package rvo2example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import algorithms.dijkstra.engine.DijkstraAlgorithm;
import algorithms.dijkstra.model.Graph;
import algorithms.dijkstra.model.Vertex;
import rvo2.Goal;
import rvo2.Simulator;

public abstract class BaseTrafficScenario2_TESTING extends JPanel {

    private static final long serialVersionUID = 1L;

    protected List<Goal> goalsList = new ArrayList<Goal>();
    protected Random random1 = new Random();
    protected Random random2 = new Random(100);
    protected Random random3 = new Random(200);

    protected List<Obs> obsList1;
    protected boolean ended = false;
    protected int agentCount = 0;
    protected long startSearchTime = 0;
    protected long endSearchTime = 0;
    protected int sumTime = 0;

    public static int w = 1920;
    public static int h = 1080;
    public boolean BDI_ENABLE = true;

    // Metrics tracking
    protected long totalSimulationTime = 0;
    protected double avgAgentTime = 0;
    protected int totalSteps = 0;

    protected String scenarioName = "Default Scenario";
    protected int totalAgents = 0;
    protected String trafficType = "unknown"; //

    public BaseTrafficScenario2_TESTING() {
        setPreferredSize(new Dimension(w, h));
        setOpaque(true);
        setBackground(Color.WHITE);
        setForeground(Color.RED);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int point_radius = 1;
        // Draw Obstacles
        if (obsList1 != null) {
            for (int i = 0; i < obsList1.size(); i++) {
                Obs polygonPoint = obsList1.get(i);
                Polygon p = new Polygon();
                for (int j = 0; j < polygonPoint.coord2.size(); j++) {
                    Vector2D v = translateCoordinateBackVector(polygonPoint.coord2.get(j));
                    p.addPoint((int) v.getX(), (int) v.getY());
                }
                g2.setColor(Color.RED);
                g2.drawPolygon(p);
            }
        }




        // Show some simulation information
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(12f));
        g2.drawString(scenarioName + " - " + trafficType, 10, 20);
        g2.drawString("Agents: " + agentCount + "/" + totalAgents, 10, 40);
        g2.drawString("Step: " + (int) Simulator.instance.getGlobalTime(), 10, 60);
        g2.drawString("FPS: " + String.format("%.1f", getFPS()), 10, 80);

    }

    private double lastFPS = 0;
    private long lastFrameTime = System.currentTimeMillis();

    private double getFPS() {
        long now = System.currentTimeMillis();
        long delta = now - lastFrameTime;
        if (delta > 0) {
            lastFPS = 1000.0 / delta;
        }
        lastFrameTime = now;
        return lastFPS;
    }

    /**
     * This func will be overridden
     */
    protected abstract void setupScenario();

    /**
     * Behaviours Matrix
     */
    protected double[][] createBehaviorMatrix() {
        double[][] behavior = new double[7][5];

        int increDist = 3;

        //P
        behavior[0][0] = 15*increDist;
        behavior[0][1] = 40;
        behavior[0][2] = 38;
        behavior[0][3] = 2.4;
        behavior[0][4] = 3.55;
        //E
        behavior[1][0] = 15*increDist;
        behavior[1][1] = 23;
        behavior[1][2] = 32;
        behavior[1][3] = 3.4;
        behavior[1][4] = 3.55;
        //N
        behavior[2][0] = 15*increDist;
        behavior[2][1] = 9;
        behavior[2][2] = 29;
        behavior[2][3] = 2.6;
        behavior[2][4] = 3.25;
        //A
        behavior[3][0] = 15*increDist;
        behavior[3][1] = 20;
        behavior[3][2] = 31;
        behavior[3][3] = 2.6;
        behavior[3][4] = 3.55;
        //AT
        behavior[4][0] = 17*increDist;
        behavior[4][1] = 17;
        behavior[4][2] = 40;
        behavior[4][3] = 2.4;
        behavior[4][4] = 3.55;
        //H
        behavior[5][0] = 15*increDist;
        behavior[5][1] = 7;
        behavior[5][2] = 30;
        behavior[5][3] = 2.1;
        behavior[5][4] = 3.25;
        //Default
        behavior[6][0] = 15*increDist;
        behavior[6][1] = 5;
        behavior[6][2] = 5;
        behavior[6][3] = 3;
        behavior[6][4] = 3.25;
        return behavior;
    }

//         Add agent
     protected void addAgent(Graph graph, int behaviorIndex, double[][] behavior, int agentNo) {
        int rand = random2.nextInt(graph.getVertexes().size() - 1);
        int rand_source = random3.nextInt(graph.getVertexes().size() - 1);

        Simulator.instance.addAgent(translateCoordinateVector(graph.getVertexes().get(rand_source).getVertex1()));
        Simulator.instance.setAgentNeighborDistance(agentNo, behavior[behaviorIndex][0]);
        Simulator.instance.setAgentMaxNeighbors(agentNo, (int) behavior[behaviorIndex][1]);
        Simulator.instance.setAgentTimeHorizonAgents(agentNo, behavior[behaviorIndex][2]);
        Simulator.instance.setAgentRadius(agentNo, behavior[behaviorIndex][3]);
        Simulator.instance.setAgentMaxSpeed(agentNo, behavior[behaviorIndex][4]);

        goalsList.add(new Goal(translateCoordinateVector(graph.getVertexes().get(rand).getVertex1())));
        goalsList.get(agentNo).setColor(getColor());
        goalsList.get(agentNo).setFlag(1);
        goalsList.get(agentNo).setSource(rand_source);
        goalsList.get(agentNo).setDestination(rand);
    }

    protected void setupObstacles() {
        ObsGenerator obs = new ObsGenerator(1, 1);
        obsList1 = obs.genSquareBlocksList5();
        System.out.println("Obstacles: " + obsList1.size());

        for (int k = 0; k < obsList1.size(); k++) {
            Obs o = obsList1.get(k);
            Simulator.instance.addObstacle(o.coord2);
        }


        Simulator.instance.processObstacles();
    }

    protected LinkedList<Vertex> getShortestPath(DijkstraAlgorithm dijkstra, Vertex source, Vertex destination) {
        dijkstra.execute(source);
        LinkedList<Vertex> path = dijkstra.getPath(destination);
        if (path == null || path.isEmpty()) {
            return null;
        }
        return simplifyPath(path);
    }

    protected LinkedList<Vertex> simplifyPath(LinkedList<Vertex> path) {
        if (path.size() <= 2) return path;

        LinkedList<Vertex> simplified = new LinkedList<>();
        simplified.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i += 2) {
            simplified.add(path.get(i));
        }

        simplified.add(path.get(path.size() - 1));
        return simplified;
    }

    protected Color getColor() {
        int r = random2.nextInt(255);
        int g = random2.nextInt(255);
        int b = random2.nextInt(255);
        return new Color(r, g, b);
    }

    protected Vector2D translateCoordinateVector(Vector2D v) {
        double xp = v.getX() - (w / 2);
        double yp = ((v.getY() - (h / 2)) * (-1));
        return new Vector2D(xp, yp);
    }

    protected Vector2D translateCoordinateBackVector(Vector2D v) {
        int xp = (int) v.getX() + (w / 2);
        int yp = (((int) v.getY() - (h / 2)) * (-1));
        return new Vector2D(xp, yp);
    }

    protected void updateVisualization() {

    }
    boolean isStraight(int src, int dst,Vector2D point ){
    	if((src==0 && dst == 8)) {
    		if(point.getX() == -250.0 && point.getY() == -0.0) {
    			return true;
    		}
    		else
    			return false;
    	}
     	if((src==3 && dst == 6)) {
    		if(point.getX() == -250.0 && point.getY() == -0.0) {
                System.out.println("Bị kẹt" + point.getY());

    			return false;
    		}
    		else
    			return true;
    	}

    	if((src==0 && dst == 4) ||(src==7 && dst == 6) ||(src==3 && dst == 8)   ) {
    		return false;
    	}

        return (src==0 && dst==6) ||
               (src==3 && dst==4) ||
               (src==7 && dst==8);
    }

    protected void setPreferredVelocities() {
        /*
         * Default movement policy: follow the current sub-goal/global goal.
         *
         * The old implementation contained a local pairwise priority rule
         * (turning yields to straight, straight-vs-straight yields by distance).
         * That rule can create circular waits in dense traffic. Scenario-specific
         * controllers, such as TC8's reservation controller, should implement
         * intersection access explicitly instead of relying on local pairwise waits.
         */
        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            if (agentNo >= goalsList.size() || goalsList.get(agentNo).getFlag() != 1) {
                Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
                continue;
            }

            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
            Vector2D goalVector;
            int next = goalsList.get(agentNo).getNext();

            if (next < goalsList.get(agentNo).getSubGoalSize()) {
                Vector2D currentWaypoint = goalsList.get(agentNo).getSubGoal().get(next);
                goalVector = currentWaypoint.subtract(agentPos);
            } else {
                goalVector = goalsList.get(agentNo).getGlobalGoal().subtract(agentPos);
            }

            final double lengthSq = goalVector.getNormSq();
            if (lengthSq > 1.0) {
                goalVector = goalVector.scalarMultiply(1 / FastMath.sqrt(lengthSq));
            }

            Simulator.instance.setAgentPreferredVelocity(agentNo, goalVector);

            final double angle = random1.nextDouble() * 2.0 * FastMath.PI;
            final double distance = random1.nextDouble() * 0.00005;
            Simulator.instance.setAgentPreferredVelocity(agentNo,
                    Simulator.instance.getAgentPreferredVelocity(agentNo)
                            .add(new Vector2D(FastMath.cos(angle), FastMath.sin(angle)).scalarMultiply(distance)));
        }
    }


    protected boolean reachedGoal() {
        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
            if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) > 100.0) {
                return false;
            }
        }
        return true;
    }

    protected void updateSubGoal() {
        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
            int next = goalsList.get(agentNo).getNext();

            if (next < goalsList.get(agentNo).getSubGoalSize() - 1) {
                double distSq = agentPos.distanceSq(goalsList.get(agentNo).getSubGoal().get(next));
                if(distSq < 14.0*14.0) { // sqrt(900) = 30 units
                    goalsList.get(agentNo).setNext(next + 1);
                }
            }
        }
    }

    protected void printResults() {
        long endTime = System.currentTimeMillis();
        totalSimulationTime = endTime - startSearchTime;
        totalSteps = (int) Simulator.instance.getGlobalTime();
        avgAgentTime = (totalAgents > 0) ? (double) sumTime / totalAgents : 0;

        System.out.println("\n=== RESULTS: " + scenarioName + " ===");
        System.out.println("Traffic Type: " + trafficType);
        System.out.println("Total Agents: " + totalAgents);
        System.out.println("Completed: " + agentCount);
        System.out.println("Total Simulation Time: " + totalSimulationTime + "ms");
        System.out.println("Total Steps: " + totalSteps);
        System.out.println("Avg Time per Agent: " + String.format("%.2f", avgAgentTime) + "ms");
        System.out.println("FPS: " + String.format("%.2f", totalSteps / (totalSimulationTime / 1000.0)));

        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Memory Used: " + (memory / 1024 / 1024) + "MB");
        System.out.println("================================\n");
    }

    protected void runSimulation() {
        setupScenario();

        JFrame frame = new JFrame(scenarioName);
        frame.setSize(w, h);
        frame.add(this);
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
            if(agentCount==totalAgents) {
            	break;
            }
        } while (!reachedGoal());

        ended = true;
        updateVisualization();
        printResults();
    }
}
