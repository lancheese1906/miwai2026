package rvo2example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import rvo2.Goal;
import rvo2.Line;
import rvo2.Simulator;

public class LargeSpace17 extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Store the goals of the agents.
	//private final List<Vector2D> goals = new ArrayList<>();
	private List<Goal> goalsList = new ArrayList<Goal>();

	// Random number generator.
	private final Random random = new Random();
	private final Random random2 = new Random(1);
	
	List<List<Vector2D>> obsList;
	List<Obs> obsList1;
	static boolean ended = false;
	static int agentTest = 0;
	static long startSearchTime = 0;
	static long endSearchTime = 0;
	static int sumTime = 0;
	static double avgTime = 0;
	
	public static int w = 1920;
	public static int h = 1080;

	public LargeSpace17() {
		setPreferredSize(new Dimension(w, h));
		setOpaque(true);
		setBackground(Color.WHITE);
		setForeground(Color.RED);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		for (int i = 0; i < obsList1.size(); i++) {
        	Obs polygonPoint = obsList1.get(i);
        	Polygon p = new Polygon();
        	for (int j = 0; j < polygonPoint.coord2.size(); j++) {
        		Vector2D v = translateCoordinateBackVector(polygonPoint.coord2.get(j));
        		p.addPoint((int)v.getX(), (int)v.getY());
			}
        	g2.setColor(Color.RED);
        	g2.drawPolygon(p);
		}
		

		for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
			Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
			//Vector2D velocity = Simulator.instance.getAgentVelocity(agentNo);
			//List<Line> agentLine = Simulator.instance.getAgentLines(agentNo);
			
			Vector2D agentPos_cood = translateCoordinateBackVector(agentPos);
			//Vector2D velocity_cood = translateCoordinateBackVector(velocity);

			
			int subGoalSize = goalsList.get(agentNo).getSubGoalSize(); 
			
			int sgx = 0;
			int sgy = 0;
			
			if(subGoalSize > 0) {
				sgx = (int)goalsList.get(agentNo).getSubGoal().get(0).getX();
				sgy = (int)goalsList.get(agentNo).getSubGoal().get(0).getY();
				Vector2D vector4 = translateCoordinateBackVector(sgx, sgy);
				g2.setColor(Color.RED);
				g2.drawLine((int) agentPos_cood.getX(), (int) agentPos_cood.getY(), (int) vector4.getX(), (int) vector4.getY());
			}

			
			int redius = (int)Simulator.instance.getAgentRadius(agentNo);
			
			//if(goalsList.get(agentNo).getFlag() == 1) {
				//redius+=4;
			//}
			
			g2.setColor(goalsList.get(agentNo).getColor());
			g2.drawOval((int) agentPos_cood.getX() - (2), (int) agentPos_cood.getY() - (2), (2) * 2, (2) * 2);

			g2.setColor(goalsList.get(agentNo).getColor());
			g2.drawOval((int) agentPos_cood.getX() - (redius), (int) agentPos_cood.getY() - (redius), (redius) * 2, (redius) * 2);
			
			if(ended) {
				int point_size = goalsList.get(agentNo).getaddMovingStepSize();
				for (int i = 0; i < point_size; i++) {
					

					g2.setColor(goalsList.get(agentNo).getColor());
					if(i < point_size - 1) {
						Vector2D curr_moving = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(i));
						Vector2D next_moving = translateCoordinateBackVector(goalsList.get(agentNo).getMovingStep().get(i + 1));
						
						g2.drawLine((int) next_moving.getX(), (int) next_moving.getY(), (int) curr_moving.getX(), (int) curr_moving.getY());
						
					}
					
					
				}
			}
		}
	}

	/**
	 * 
	 */
	private void setupScenario() {
		startSearchTime = System.currentTimeMillis();
		// Specify the global time step of the simulation.
		Simulator.instance.setTimeStep(0.25);

		// Specify the default parameters for agents that are subsequently
		// added.
		Simulator.instance.setAgentDefaults(15.0, 10, 5.0, 5.0, 2.0, 2.0, Vector2D.ZERO);

		double[][] behavior = new double[7][5];
		//P
		behavior[0][0] = 15;
		behavior[0][1] = 40;
		behavior[0][2] = 38;
		behavior[0][3] = 0.4;
		behavior[0][4] = 1.55;
		//E
		behavior[1][0] = 15;
		behavior[1][1] = 23;
		behavior[1][2] = 32;
		behavior[1][3] = 0.4;
		behavior[1][4] = 1.55;
		//N
		behavior[2][0] = 15;
		behavior[2][1] = 9;
		behavior[2][2] = 29;
		behavior[2][3] = 1.6;
		behavior[2][4] = 1.25;
		//A
		behavior[3][0] = 15;
		behavior[3][1] = 20;
		behavior[3][2] = 31;
		behavior[3][3] = 0.6;
		behavior[3][4] = 1.55;
		//AT
		behavior[4][0] = 13;
		behavior[4][1] = 17;
		behavior[4][2] = 40;
		behavior[4][3] = 0.4;
		behavior[4][4] = 1.55;
		//H
		behavior[5][0] = 15;
		behavior[5][1] = 7;
		behavior[5][2] = 30;
		behavior[5][3] = 1.1;
		behavior[5][4] = 1.25;
		
		//Default
		behavior[6][0] = 15;
		behavior[6][1] = 5;
		behavior[6][2] = 5;
		behavior[6][3] = 2;
		behavior[6][4] = 1.35;
		
		// Add agents, specifying their start position, and store their goals on
		// the opposite side of the environment.
		int behaviorIdex1 = 6; //+,+  ---      -,+      |      +,+
		int behaviorIdex2 = 6; //-,+           ---------+---------
		int behaviorIdex3 = 6; //+,-  ---	   -,-      |      +,-
		int behaviorIdex4 = 6; //-,-
		
		int behaviorIdex5 = 5;
		int behaviorIdex6 = 6;
		
		int agentNo = 0;
		int agent = 40;
		for (int i = 0; i < agent; i++) {
            for (int j = 0; j < agent; j++) {
   	
            	Simulator.instance.addAgent(new Vector2D(950 + i * 10.0, 530 + j * 10.0));
            	Simulator.instance.setAgentNeighborDistance(agentNo, behavior[behaviorIdex1][0]);
            	Simulator.instance.setAgentMaxNeighbors(agentNo, (int)behavior[behaviorIdex1][1]);
            	Simulator.instance.setAgentTimeHorizonAgents(agentNo, behavior[behaviorIdex1][2]);
            	Simulator.instance.setAgentRadius(agentNo, behavior[behaviorIdex1][3]);
            	Simulator.instance.setAgentMaxSpeed(agentNo, behavior[behaviorIdex1][4]);
				goalsList.add(new Goal(Simulator.instance.getAgentPosition(agentNo).negate()));
				goalsList.get(agentNo).setColor(getColor());
				goalsList.get(agentNo).setFlag(1);
				agentTest+=1;
				agentNo++;

                Simulator.instance.addAgent(new Vector2D(-950 - i * 10.0, 530 + j * 10.0));
            	Simulator.instance.setAgentNeighborDistance(agentNo, behavior[behaviorIdex2][0]);
            	Simulator.instance.setAgentMaxNeighbors(agentNo, (int)behavior[behaviorIdex2][1]);
            	Simulator.instance.setAgentTimeHorizonAgents(agentNo, behavior[behaviorIdex2][2]);
            	Simulator.instance.setAgentRadius(agentNo, behavior[behaviorIdex2][3]);
            	Simulator.instance.setAgentMaxSpeed(agentNo, behavior[behaviorIdex2][4]);
				goalsList.add(new Goal(Simulator.instance.getAgentPosition(agentNo).negate()));
				goalsList.get(agentNo).setColor(getColor());
				goalsList.get(agentNo).setFlag(1);
				agentTest+=1;
				agentNo++;

                Simulator.instance.addAgent(new Vector2D(950 + i * 10.0, -530 - j * 10.0));
            	Simulator.instance.setAgentNeighborDistance(agentNo, behavior[behaviorIdex3][0]);
            	Simulator.instance.setAgentMaxNeighbors(agentNo, (int)behavior[behaviorIdex3][1]);
            	Simulator.instance.setAgentTimeHorizonAgents(agentNo, behavior[behaviorIdex3][2]);
            	Simulator.instance.setAgentRadius(agentNo, behavior[behaviorIdex3][3]);
            	Simulator.instance.setAgentMaxSpeed(agentNo, behavior[behaviorIdex3][4]);
				goalsList.add(new Goal(Simulator.instance.getAgentPosition(agentNo).negate()));
				goalsList.get(agentNo).setColor(getColor());
				goalsList.get(agentNo).setFlag(1);
				agentTest+=1;
				agentNo++;

                Simulator.instance.addAgent(new Vector2D(-950 - i * 10.0, -530 - j * 10.0));
            	Simulator.instance.setAgentNeighborDistance(agentNo, behavior[behaviorIdex4][0]);
            	Simulator.instance.setAgentMaxNeighbors(agentNo, (int)behavior[behaviorIdex4][1]);
            	Simulator.instance.setAgentTimeHorizonAgents(agentNo, behavior[behaviorIdex4][2]);
            	Simulator.instance.setAgentRadius(agentNo, behavior[behaviorIdex4][3]);
            	Simulator.instance.setAgentMaxSpeed(agentNo, behavior[behaviorIdex4][4]);
				goalsList.add(new Goal(Simulator.instance.getAgentPosition(agentNo).negate()));
				goalsList.get(agentNo).setColor(getColor());
				goalsList.get(agentNo).setFlag(1);
				agentTest+=1;
				agentNo++;
            }
        }

		//ObsGenerator obs = new ObsGenerator(48, 85);
		
		/*ObsGenerator obs = new ObsGenerator(48, 85);
		obsList1 = obs.genSquareBlocksList2(1, 1);*/
		
		ObsGenerator obs = new ObsGenerator(52, 93);
		obsList1 = obs.genSquareBlocksList4();
		System.out.println("obs: " + obsList1.size());
		
		for (int k = 0; k < obsList1.size(); k++) {
			Obs o = obsList1.get(k);
			
			int id = Simulator.instance.addObstacle(o.coord2);
        	//System.out.println("id: " + id);
			
		}
				
		// Process the obstacles so that they are accounted for in the
		// simulation.
		Simulator.instance.processObstacles();
	}
	
	public Color getColor() {
		int r = random2.nextInt(255);
		int g = random2.nextInt(255);
		int b = random2.nextInt(255);
		Color col = new Color(r, g, b);
		return col;
	}

	public Vector2D translateCoordinateVector(int x, int y) {
		int xp = x - (w / 2);
		int yp = ((y - (h / 2)) * (-1));
		Vector2D vector = new Vector2D(xp, yp);
		// System.out.println(x+" -- "+y+" translate to " + xp + ", " + yp);

		return vector;
	}

	public Vector2D translateCoordinateBackVector(int x, int y) {
		int xp = x + (w / 2);
		int yp = ((y - (h / 2)) * (-1));
		Vector2D vector = new Vector2D(xp, yp);
		// System.out.println(x+" -- "+y+" translate to " + xp + ", " + yp);

		return vector;
	}
	
	public Vector2D translateCoordinateBackVector(Vector2D v) {
		int xp = (int)v.getX() + (w / 2);
		int yp = (((int)v.getY() - (h / 2)) * (-1));
		Vector2D vector = new Vector2D(xp, yp);
		// System.out.println(x+" -- "+y+" translate to " + xp + ", " + yp);

		return vector;
	}
	
	public static float getAngle(Vector2D p1, Vector2D p2) {
	    float angle = (float) Math.toDegrees(Math.atan2(p1.getY() - p2.getY(), p1.getY() - p2.getX()));
	    
	    if(angle < 0){
	        angle += 360;
	    }

	    return angle;
	}
	
	public static double angleBetween2CartesianPoints(Vector2D p1, Vector2D p2) {
	    double angle = Math.atan2((p2.getX() - p1.getX()), (p2.getY() - p1.getY())) * 180 / Math.PI;
	    if (angle < 0) {
	        return (360 + angle);
	    } else {
	        return (angle);
	    }
	}
	
	public static double bearing(Vector2D p1, Vector2D p2) {
	    double TWOPI = FastMath.PI * 2; //6.283185307179586
	    double RAD2DEG = 180 / FastMath.PI; // (180 / FastMath.PI) - 57.2957795130823209
	    // if (a1 = b1 and a2 = b2) throw an error 
	    double theta = FastMath.atan2(p2.getX() - p1.getX(), p1.getY() - p2.getY());
	    if (theta < 0.0) {
	        theta += TWOPI;
	    }
	    return RAD2DEG * theta;
	}

	private void updateVisualization() {
		// Output the current global time.
	//	System.out.print(Simulator.instance.getGlobalTime());
		for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
			Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
			if(goalsList.get(agentNo).getFlag() == 1) {
				//goalsList.get(agentNo).addMovingStep(Simulator.instance.getAgentPosition(agentNo));
				if(agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) < 10.0) {
					
					endSearchTime = System.currentTimeMillis();
					long elapsed = endSearchTime-startSearchTime;
					System.out.println("agentNo: " + agentNo + " -> " + (elapsed));
					goalsList.get(agentNo).setReachGoalms(elapsed);
					goalsList.get(agentNo).setFlag(2);
					sumTime+=elapsed;
					Simulator.instance.setAgentPreferredVelocity(agentNo, Vector2D.ZERO);
				}
			}
		}
		
	//	System.out.println();
		repaint();
	}

	private void setPreferredVelocities() {
		// Set the preferred velocity to be a vector of unit magnitude (speed)
		// in the direction of the goal.
		for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
			//List<Line> agentLine = Simulator.instance.getAgentLines(agentNo);
			Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);

			// this command return vector: (x - p.x, y - p.y)
			Vector2D goalVector;
			if(goalsList.get(agentNo).getSubGoalSize() > 0) {
				goalVector = goalsList.get(agentNo).getSubGoal().get(0).subtract(agentPos);
			} else {
				goalVector = goalsList.get(agentNo).getGlobalGoal().subtract(agentPos);
			}

			// this command return double: (x * x + y * y)
			final double lengthSq = goalVector.getNormSq();

			//System.out.println(agentNo+") lengthSq: " + lengthSq + ", goalVector: " + goalVector + ", Global Goal: " + goalsList.get(agentNo).getGlobalGoal() + ", Agent Position: " + agentPos);

			if (lengthSq > 1.0) {
				// this command return vector: (scalar * x, scalar * y)
				goalVector = goalVector.scalarMultiply(1 / FastMath.sqrt(lengthSq));
			}

			//System.out.println(agentNo+") lengthSq: " + lengthSq + ", goalVector: " + goalVector + ", Global Goal: " + goalsList.get(agentNo).getGlobalGoal() + ", Agent Position: " + agentPos);

			Simulator.instance.setAgentPreferredVelocity(agentNo, goalVector);
		
			
			
	/*		int nearAgent = Simulator.instance.getAgentNumAgentNeighbors(agentNo);
			if(nearAgent > 10) {
				
				double sAngle = bearing(agentPos, goalsList.get(agentNo).getGlobalGoal()) - (random.nextInt(20 - (10)) + (10));
				double sDistance = (random.nextInt(40 - (20)) + (20));
				
				double goalX = agentPos.getX() + sDistance * FastMath.cos(sAngle * FastMath.PI / 180);
				double goalY = agentPos.getY() + sDistance * FastMath.sin(sAngle * FastMath.PI / 180);
				
				double goalNX = agentPos.getX() + (-sDistance) * FastMath.cos(sAngle * FastMath.PI / 180);
				double goalNY = agentPos.getY() + (-sDistance) * FastMath.sin(sAngle * FastMath.PI / 180);
				Vector2D subGoal = null;
				
				if(goalsList.get(agentNo).getSubGoalSize() <= 0 || nearAgent > 10 ) {
					if(agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) > 500.0) {
						subGoal = new Vector2D(goalX, goalY);
					}
				} else {
					subGoal = new Vector2D(goalNX, goalNY);
				}
				
				goalsList.get(agentNo).add(subGoal);
			} */
			
		/*	int nearAgent = Simulator.instance.getAgentNumAgentNeighbors(agentNo);
			if(nearAgent > 4) {
				double sAngle = bearing(agentPos, goalsList.get(agentNo).getGlobalGoal()) - (random.nextInt(20 - (10)) + (10));
				double sDistance = (random.nextInt(5 - (1)) + (1));
				if(goalsList.get(agentNo).getSubGoalSize() <= 0) {
			
					
					if(agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) > 500.0) {
						double goalX = agentPos.getX() + sDistance * FastMath.cos(sAngle * FastMath.PI / 180);
						double goalY = agentPos.getY() + sDistance * FastMath.sin(sAngle * FastMath.PI / 180);
						
						Vector2D subGoal = new Vector2D(goalX, goalY);
						goalsList.get(agentNo).add(subGoal);
					}
				} 
			}*/
			
		/*	boolean vi = Simulator.instance.queryVisibility(agentPos, goalsList.get(agentNo).globalGoal, 50);
			if(vi) {
				System.out.println(vi);
			}*/
			
/*			int nearAgent = Simulator.instance.getAgentNumAgentNeighbors(agentNo);
			if(nearAgent > 4) {
			
				if(goalsList.get(agentNo).getSubGoalSize() <= 0) {
					
					
					if(agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) > 500.0) {
						int neighborNo = Simulator.instance.getAgentNumObstacleNeighbors(agentNo);
						if(neighborNo > 0) {
							int vertexNo = Simulator.instance.getAgentObstacleNeighbor(agentNo, 0);
							Vector2D v = Simulator.instance.getObstacleVertex(vertexNo);
							//System.out.println(v);
							goalsList.get(agentNo).add(v);
						}
						
					}
				} 
			}*/
			
			
			
			//
			//System.out.println("nearAgent: " + nearAgent + ", subGoal: " + goalsList.get(agentNo).getSubGoalSize());*/
			
			// Perturb a little to avoid deadlocks due to perfect symmetry.
			final double angle = random.nextDouble() * 2.0 * FastMath.PI;
			//final double distance = random.nextDouble() * 0.0001;
			final double distance = random.nextDouble() * 0.0001;
			//System.out.println("angle: " + angle + ", distance: " + distance);
			
            Simulator.instance.setAgentPreferredVelocity(agentNo, Simulator.instance.getAgentPreferredVelocity(agentNo).add(new Vector2D(FastMath.cos(angle), FastMath.sin(angle)).scalarMultiply(distance)));
			
			
		}
		//System.out.println();
	}

	private boolean reachedGoal() {
		// Check if all agents have reached their goals.
		for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
			Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);	
			if (agentPos.distanceSq(goalsList.get(agentNo).getGlobalGoal()) > 100.0) {
				return false;
			}
		}
		return true;
	}
	
	private void updateSubGoal() {
		// Check if all agents have reached their subgoals.
		for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
			Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
			
			if (goalsList.get(agentNo).getSubGoalSize() > 0) {
				double distSq = agentPos.distanceSq(goalsList.get(agentNo).getSubGoal().get(0));
				
				if((distSq < 10.0)) {
					goalsList.get(agentNo).remove();
					//break;
				}
			}	
		}
	}
	
	private void updateSubGoal1() {
		// Check if all agents have reached their subgoals.
		for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
			Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
			
			if (goalsList.get(agentNo).getSubGoalSize() > 0) {
				
				
				double distSq = agentPos.distanceSq(goalsList.get(agentNo).getSubGoal().get(0));
				
				if((distSq < 10.0) || (distSq > 10 && distSq < 50) || (Simulator.instance.getAgentNumAgentNeighbors(agentNo) <= 4)) {
					goalsList.get(agentNo).remove();
					break;
				}
			}	
		}
	}

	public static void main(String[] args) {
		// Get the Java runtime
		Runtime runtime = Runtime.getRuntime();
		
		long startSearchTime = System.currentTimeMillis();
        System.out.println("start: " + startSearchTime); 
        
		final LargeSpace17 blocks = new LargeSpace17();
		
		// Set up the scenario.
		blocks.setupScenario();
		
		System.out.println("testAgent: " + agentTest);

		JFrame frame = new JFrame();
		frame.setSize(w, h);
		frame.add(blocks);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Perform (and manipulate) the simulation.
		do {
			blocks.updateVisualization();
			blocks.setPreferredVelocities();
			blocks.updateSubGoal1();
			Simulator.instance.doStep();
		} while (!blocks.reachedGoal());
		ended = true;
		blocks.updateVisualization();
		
		long endSearchTime = System.currentTimeMillis();
		System.out.println("end: " + endSearchTime);
        long elapsedTime = endSearchTime - startSearchTime;
        System.out.println("elapsedTime: " + elapsedTime);
        System.out.println("step: " + Simulator.instance.getGlobalTime());
        System.out.println("avg: " + (sumTime / agentTest));
        
        System.out.println("fps: " + Simulator.instance.getGlobalTime() / (elapsedTime / 60));
        
        // Run the garbage collector
     	runtime.gc();
     	// Calculate the used memory
     	long memory = runtime.totalMemory() - runtime.freeMemory();
     	System.out.println("Used memory is bytes: " + memory);
     	System.out.println("Used memory is megabytes: " + bytesToMegabytes(memory));
	}
	
	private static final long MEGABYTE = 1024L * 1024L;

	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}
}
