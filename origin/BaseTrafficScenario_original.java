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

/**
 * Base class cho các scenario giao thông thực tế Hỗ trợ: 1-way, 2-way, 4-way,
 * tắc đường
 */
public abstract class BaseTrafficScenario extends JPanel {

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
    protected String trafficType = "unknown"; // "1-way", "2-way", "4-way", "roundabout", "rush-hour"

    public BaseTrafficScenario() {
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

        // Vẽ chướng ngại vật
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

        // Vẽ agents - Tối ưu: bỏ những agent không cần vẽ
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
                    g2.setColor(goalsList.get(agentNo).getColor());
                    g2.fillOval((int) agentPos_cood.getX() - 2, (int) agentPos_cood.getY() - 2, 4, 4);
                    g2.drawOval((int) agentPos_cood.getX() - radius, (int) agentPos_cood.getY() - radius,
                            radius * 2, radius * 2);
                }

                // Vẽ đường đi nếu kết thúc - Chỉ vẽ nếu cần thiết
                if (ended && totalSteps < 10000) { // Tránh vẽ quá nhiều
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

        // Vẽ thông tin scenario
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

    protected void addMiddleLaneDivider(double midX, double blockWidth, double blockHeight, double spacing) {
    /**
     * Tạo dòng chướng ngại vật ở giữa để chia làn
     * @param midX - tọa độ X giữa bản đồ
     * @param blockWidth - chiều rộng mỗi block (20 = ~20 pixels)
     * @param blockHeight - chiều cao mỗi block
     * @param spacing - khoảng cách giữa các block (50 = gap nhỏ)
     */
        for (double y = 0; y <= h; y += spacing + blockHeight) {
            List<Vector2D> blockCoords = new ArrayList<>();
            List<Vector2D> emptyList = new ArrayList<>();
        
            // Tạo hình chữ nhật (4 góc)
            blockCoords.add(new Vector2D(midX - blockWidth/2, y));
            blockCoords.add(new Vector2D(midX + blockWidth/2, y));
            blockCoords.add(new Vector2D(midX + blockWidth/2, y + blockHeight));
            blockCoords.add(new Vector2D(midX - blockWidth/2, y + blockHeight));
            
            Obs obstacle = new Obs(blockCoords, emptyList);
            obsList1.add(obstacle);
        
            Simulator.instance.addObstacle(blockCoords);
        }
    }
    /**
     * Setup scenario - phải override trong subclass
     */
    protected abstract void setupScenario();

    /**
     * Tạo behavior matrix cho các loại agent khác nhau
     */
    protected double[][] createBehaviorMatrix() {
        double[][] behavior = new double[7][5];
        //P - Normal person (người bình thường)
        behavior[0][0] = 15;
        behavior[0][1] = 40;
        behavior[0][2] = 38;
        behavior[0][3] = 0.4;
        behavior[0][4] = 1.55;
        //E - Careful person (người cẩn thận)
        behavior[1][0] = 15;
        behavior[1][1] = 23;
        behavior[1][2] = 32;
        behavior[1][3] = 0.4;
        behavior[1][4] = 1.55;
        //N - Elder/Large (người lớn/béo)
        behavior[2][0] = 15;
        behavior[2][1] = 9;
        behavior[2][2] = 29;
        behavior[2][3] = 1.6;
        behavior[2][4] = 1.25;
        //A - Aggressive (người nóng tính)
        behavior[3][0] = 15;
        behavior[3][1] = 20;
        behavior[3][2] = 31;
        behavior[3][3] = 0.6;
        behavior[3][4] = 1.55;
        //AT - Very careful (rất cẩn thận)
        behavior[4][0] = 13;
        behavior[4][1] = 17;
        behavior[4][2] = 40;
        behavior[4][3] = 0.4;
        behavior[4][4] = 1.55;
        //H - Heavy (hành lý/xe)
        behavior[5][0] = 15;
        behavior[5][1] = 7;
        behavior[5][2] = 30;
        behavior[5][3] = 1.1;
        behavior[5][4] = 1.25;
        //Default - Children
        behavior[6][0] = 15;
        behavior[6][1] = 5;
        behavior[6][2] = 5;
        behavior[6][3] = 2;
        behavior[6][4] = 1.25;
        return behavior;
    }

    /**
     * Thêm agent vào scenario
     */
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

        // addMiddleLaneDivider(960, 20, 40, 50);

        Simulator.instance.processObstacles();
    }

    protected LinkedList<Vertex> getShortestPath(DijkstraAlgorithm dijkstra, Vertex source, Vertex destination) {
        dijkstra.execute(source);
        LinkedList<Vertex> path = dijkstra.getPath(destination);
        // Simplified path: giảm waypoint không cần thiết
        return simplifyPath(path);
    }

    /**
     * Rút gọn path bằng cách loại bỏ waypoint trung gian
     * Chỉ giữ lại những waypoint quan trọng
     */
    protected LinkedList<Vertex> simplifyPath(LinkedList<Vertex> path) {
        if (path.size() <= 2) return path;
        
        LinkedList<Vertex> simplified = new LinkedList<>();
        simplified.add(path.get(0)); // Điểm đầu
        
        // Giữ lại mỗi waypoint thứ 2 (giảm 50%)
        for (int i = 1; i < path.size() - 1; i += 2) {
            simplified.add(path.get(i));
        }
        
        simplified.add(path.get(path.size() - 1)); // Điểm cuối
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
        if ((Simulator.instance.getGlobalTime() % 100) == 0) {
            System.out.println("Step: " + Simulator.instance.getGlobalTime()
                    + " -> Completed: " + agentCount + "/" + totalAgents);
        }

        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
            if (goalsList.get(agentNo).getFlag() == 1) {
                goalsList.get(agentNo).addMovingStep(agentPos);

                if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) < 10.0) {
                    agentCount++;
                    long elapsed = System.currentTimeMillis() - startSearchTime;
                    goalsList.get(agentNo).setReachGoalms(elapsed);
                    goalsList.get(agentNo).setFlag(2);
                    sumTime += elapsed;

                    Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
                    Simulator.instance.setAgentRadius(agentNo, 0);
                }
            }
        }
        repaint();
    }

    protected void setPreferredVelocities() {
        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
            Vector2D goalVector;
            int next = goalsList.get(agentNo).getNext();

            if (next < goalsList.get(agentNo).getSubGoalSize() - 1) {
                // Lấy waypoint hiện tại
                Vector2D currentWaypoint = goalsList.get(agentNo).getSubGoal().get(next);
                // Kiểm tra nếu gần waypoint, hãy nhìn xa hơn (look-ahead)
                Vector2D nextWaypoint = currentWaypoint;
                if (next + 1 < goalsList.get(agentNo).getSubGoalSize() - 1) {
                    // Smooth: mix giữa waypoint hiện tại và kế tiếp
                    double distToNext = agentPos.distance(currentWaypoint);
                    if (distToNext < 15.0) {
                        nextWaypoint = goalsList.get(agentNo).getSubGoal().get(next + 1);
                        goalVector = nextWaypoint.subtract(agentPos);
                    } else {
                        goalVector = currentWaypoint.subtract(agentPos);
                    }
                } else {
                    goalVector = currentWaypoint.subtract(agentPos);
                }
            } else {
                goalVector = goalsList.get(agentNo).getGlobalGoal().subtract(agentPos);
            }

            final double lengthSq = goalVector.getNormSq();
            if (lengthSq > 1.0) {
                goalVector = goalVector.scalarMultiply(1 / FastMath.sqrt(lengthSq));
            }

            Simulator.instance.setAgentPreferredVelocity(agentNo, goalVector);

            // Giảm perturb để tránh quá nhiều dao động
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
                // Tăng threshold từ 1.0 lên 30 để agent không chuyển waypoint quá nhanh
                // Điều này giúp agent di chuyển mượt mà hơn thay vì giựt qua giựt lại
                if(distSq < 900.0) { // sqrt(900) = 30 units
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

        // Chạy mô phỏng với FPS cap
        final int TARGET_FPS = 60;
        final long FRAME_TIME = 1000 / TARGET_FPS; // ~16ms per frame

        long lastFrameTime = System.currentTimeMillis();
        do {
            long frameStart = System.currentTimeMillis();

            updateVisualization();
            setPreferredVelocities();
            updateSubGoal();
            Simulator.instance.doStep();

            totalSteps++;

            // FPS limiter - tránh giựt
            long frameElapsed = System.currentTimeMillis() - frameStart;
            if (frameElapsed < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - frameElapsed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            lastFrameTime = System.currentTimeMillis();
        } while (!reachedGoal());

        ended = true;
        updateVisualization();
        printResults();
    }
}
