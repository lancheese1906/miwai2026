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

import imp_kdtree3dv2.Point3D;
import rvo2.Simulator;

public class LargeSpace03 extends JPanel {
	  /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// Store the goals of the agents.
	    private final List<Vector2D> goals = new ArrayList<>();

	    // Random number generator.
	    private final Random random = new Random();

	    
	    public static int w = 1920;
	    public static int h = 1080;
	    List<Obs> obsList;
	 
	    
	    public LargeSpace03() {
	    	setPreferredSize(new Dimension(w, h));
			setOpaque(true);
			setBackground(Color.CYAN);
			setForeground(Color.RED);
		}
	    
	    @Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			
			for (int i = 0; i < obsList.size(); i++) {
	        	Obs polygonPoint = obsList.get(i);
	        	Polygon p = new Polygon();
	        	for (int j = 0; j < polygonPoint.coord1.size(); j++) {
	        		Vector2D v = polygonPoint.coord1.get(j);
	        		p.addPoint((int)v.getX(), (int)v.getY());
				}
	        	g2.drawPolygon(p);
			}
			
			for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
	           // System.out.print(" " + Simulator.instance.getAgentPosition(agentNo));
	            Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
	           /* List<Line> lines = Simulator.instance.getAgentLines(agentNo);
	            for (Line line : lines) {
					System.out.println(line.direction.toString() + "-->" + line.point.toString());
				}*/
	            
	            int ax = (int) agentPos.getX();
	            int ay = (int) agentPos.getY();
	            
	    		Vector2D vector1 = translateCoordinateBackVector(ax, ay);

	    		
	    		if(agentNo == 0) {
	    			Vector2D goalPos = goals.get(0);
		            Vector2D goalVector = goals.get(0).subtract(Simulator.instance.getAgentPosition(0));
		            final double lengthSq = goalVector.getNormSq();
		            
		            int gx = (int) goalPos.getX();
		            int gy = (int) goalPos.getY();
		            
		            goalVector = goalVector.scalarMultiply(1 / FastMath.sqrt(lengthSq));
		            
		            int gvx = (int) goalVector.getX();
		            int gvy = (int) goalVector.getY();
		            
		            
		            
		    		Vector2D vector2 = translateCoordinateBackVector(gx, gy);
		    		Vector2D vector3 = translateCoordinateBackVector(gvx, gvy);
		            
	    			g2.setColor(Color.BLUE);
	    			g2.drawLine((int)vector1.getX(), (int)vector1.getY(), (int)vector2.getX(), (int)vector2.getY());
	    			g2.setColor(Color.ORANGE);
	    			g2.drawLine((int)vector1.getX(), (int)vector1.getY(), (int)vector3.getX(), (int)vector3.getY());
	    			g2.setColor(Color.BLACK);
	    			g2.drawString(goalVector.toString() +" " + goalPos.toString(), (int)vector3.getX(), (int)vector3.getY());
	    			
	    		} else {
	    			g2.setColor(Color.RED);
	    		}
	    		//agentPos.scalarMultiply(a);
	            g2.drawOval((int)vector1.getX() - (4/2), (int)vector1.getY() - (4/2), 4, 4);
	            
	        }
	    }
	    
	    private void setupScenario() {
	        // Specify the global time step of the simulation.
	        Simulator.instance.setTimeStep(0.25);

	        // Specify the default parameters for agents that are subsequently
	        // added.
	        Simulator.instance.setAgentDefaults(15.0, 10, 5.0, 5.0, 2.0, 2.0, Vector2D.ZERO);

	        // Add agents, specifying their start position, and store their goals on
	        // the opposite side of the environment.
	        for (int i = 0; i < 10; i++) {
	            for (int j = 0; j < 10; j++) {
	                //Simulator.instance.addAgent(new Vector2D(55.0 + i * 10.0, 55.0 + j * 10.0));
	            	Simulator.instance.addAgent(new Vector2D(950 + i * 10.0, 530 + j * 10.0));
	                //goals.add(new Vector2D(-75.0, -75.0));
	                goals.add(new Vector2D(-950, -530));

	                Simulator.instance.addAgent(new Vector2D(-950 - i * 10.0, 530 + j * 10.0));
	                //goals.add(new Vector2D(75.0, -75.0));
	                goals.add(new Vector2D(950, -530));

	                Simulator.instance.addAgent(new Vector2D(950 + i * 10.0, -530 - j * 10.0));
	                //goals.add(new Vector2D(-75.0, 75.0));
	                goals.add(new Vector2D(-950, 530));

	                Simulator.instance.addAgent(new Vector2D(-950 - i * 10.0, -530 - j * 10.0));
	                //goals.add(new Vector2D(75.0, 75.0));
	                goals.add(new Vector2D(950, 530));
	            }
	        }

	        // Add (polygonal) obstacles, specifying their vertices in
	        // counterclockwise order.
	        ObsGenerator obs = new ObsGenerator(500, 500);
	        obsList = obs.genSquareBlocksList();
	        
	        long startSearchTime = System.currentTimeMillis();
	        System.out.println("start: " + startSearchTime); 

	        
	        
	        for (int i = 0; i < obsList.size(); i++) {
	        	Obs polygonPoint = obsList.get(i);
	        	
	        	int id = Simulator.instance.addObstacle(polygonPoint.coord2);
	        	System.out.println("id: " + id);
			}
	        
	        
	        //System.out.println(Simulator.instance.getObstacleSize());

	        // Process the obstacles so that they are accounted for in the
	        // simulation.
	        Simulator.instance.processObstacles();
	        long endSearchTime = System.currentTimeMillis();
	        System.out.println("end: " + endSearchTime);
	        
	        long elapsedTime = endSearchTime - startSearchTime;
	        System.out.println("obstacle-add: " + elapsedTime);
	        System.exit(0);
	    }

	    
	    public Vector2D translateCoordinateVector(int x, int y) {
			int xp = x - (w/2);
			int yp = ((y - (h/2)) * (-1));
			Vector2D vector = new Vector2D(xp, yp);
			//System.out.println(x+" -- "+y+" translate to " + xp + ", " + yp);
			
			return vector;
		}
	    
	    public Vector2D translateCoordinateBackVector(int x, int y) {
			int xp = x + (w/2);
			int yp = ((y - (h/2)) * (-1));
			Vector2D vector = new Vector2D(xp, yp);
			//System.out.println(x+" -- "+y+" translate to " + xp + ", " + yp);
			
			return vector;
		}
	    
	    private void updateVisualization() {
	        // Output the current global time.
	        System.out.println(Simulator.instance.getGlobalTime());

	        // Output the current position of all the agents.
	 /*       for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
	            System.out.print(" " + Simulator.instance.getAgentPosition(agentNo));
	        }*/

	      /*  if((Simulator.instance.getGlobalTime() % 1000) == 0) {
	        	System.out.println("==========>");
	        	Random rand = new Random();
	        	for (int i = 0; i < goals.size(); i++) {
					int x = rand.nextInt(w);
					int y = rand.nextInt(h);
					
					Vector2D g = translateCoordinateVector(x, y);
					
					goals.set(i, g);
				}
	        	
	        	
	        }*/
	        
	        
	        repaint();
	    }

	    private void setPreferredVelocities() {
	        // Set the preferred velocity to be a vector of unit magnitude (speed)
	        // in the direction of the goal.
	        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
	        	
	        	Vector2D agentPos = Simulator.instance.getAgentPosition(agentNo);
	        	
	        	//this command return vector: (x - p.x, y - p.y)
	            Vector2D goalVector = goals.get(agentNo).subtract(agentPos);
	            
	            //this command return double: (x * x + y * y)
	            final double lengthSq = goalVector.getNormSq();
	        
	            if(agentNo == 0) {
	            	System.out.println("1) lengthSq: " + lengthSq + ", goalVector: " + goalVector + ", Global Goal: " + goals.get(0) + ", Agent Position: " + agentPos);
	            }
	            
	            if (lengthSq > 1.0) {
	            	//this command return vector: (scalar * x, scalar * y)
	                goalVector = goalVector.scalarMultiply(1 / FastMath.sqrt(lengthSq));
	            }

	            if(agentNo == 0) {
	            	System.out.println("2) lengthSq: " + lengthSq + ", goalVector: " + goalVector + ", Global Goal: " + goals.get(0) + ", Agent Position: " + agentPos);
	            }
	            
	            Simulator.instance.setAgentPreferredVelocity(agentNo, goalVector);

	            // Perturb a little to avoid deadlocks due to perfect symmetry.
	            final double angle = random.nextDouble() * 2.0 * FastMath.PI;
	            final double distance = random.nextDouble() * 0.0001;
	            if(agentNo == 0) {
	            	System.out.println("angle: " + angle + ", " + distance); 
	            }

	            Simulator.instance.setAgentPreferredVelocity(agentNo, Simulator.instance.getAgentPreferredVelocity(agentNo).add(new Vector2D(FastMath.cos(angle), FastMath.sin(angle)).scalarMultiply(distance)));
	            
	        }
	    }

	    private boolean reachedGoal() {
	        // Check if all agents have reached their goals.
	        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
	        	
	            if (Simulator.instance.getAgentPosition(agentNo).distanceSq(goals.get(agentNo)) > 80000.0) {
	                return false;
	            }
	        }

	        return true;
	    }

	    public static void main(String[] args) {
	        final LargeSpace03 blocks = new LargeSpace03();

	        // Set up the scenario.
	        blocks.setupScenario();
	        
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
	            Simulator.instance.doStep();
	        }
	        while (!blocks.reachedGoal());
	    }
}
