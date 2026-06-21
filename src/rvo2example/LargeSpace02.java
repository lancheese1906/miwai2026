package rvo2example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;

import rvo2.Simulator;

public class LargeSpace02 {
	
	// Store the goals of the agents.
    private final List<Vector2D> goals = new ArrayList<>();

    // Random number generator.
    private final Random random = new Random();

    
    public int w;
    public int h;
    public int agentNo;
    List<Obs> obsList;
    
    
    public LargeSpace02(int w, int h) {
    	this.w = w;
    	this.h = h;
	}
    
    
    
    private void setupScenario(int agent) {
        // Specify the global time step of the simulation.
        Simulator.instance.setTimeStep(0.25);

        // Specify the default parameters for agents that are subsequently
        // added.
        Simulator.instance.setAgentDefaults(15.0, 10, 5.0, 5.0, 2.0, 2.0, Vector2D.ZERO);

        // Add agents, specifying their start position, and store their goals on
        // the opposite side of the environment.
        System.out.println("agents: " + (agent*10*4));
        for (int i = 0; i < agent; i++) {
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
        ObsGenerator obs = new ObsGenerator(48, 85);
        obsList = obs.genSquareBlocksList();
        
        for (int i = 0; i < obsList.size(); i++) {
        	Obs polygonPoint = obsList.get(i);
        	Simulator.instance.addObstacle(polygonPoint.coord2);
		}
        
        // Process the obstacles so that they are accounted for in the
        // simulation.
        Simulator.instance.processObstacles();
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

    
    private void updateVisualization(long startMilli) {
        

        // Output the current position of all the agents.
 /*       for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            System.out.print(" " + Simulator.instance.getAgentPosition(agentNo));
        }*/

        if((Simulator.instance.getGlobalTime() % 1000) == 0) {
        	long endThousandStep = System.currentTimeMillis();
        	// Output the current global time.
            System.out.println(Simulator.instance.getGlobalTime() + " --> " +(endThousandStep - startMilli));
            
            
        	/*System.out.println("==========>");
        	Random rand = new Random();
        	for (int i = 0; i < goals.size(); i++) {
				int x = rand.nextInt(w);
				int y = rand.nextInt(h);
				
				Vector2D g = translateCoordinateVector(x, y);
				
				goals.set(i, g);
			}*/
        	
        	
        }
        
        //System.out.println();
    }
    

    private void setPreferredVelocities() {
        // Set the preferred velocity to be a vector of unit magnitude (speed)
        // in the direction of the goal.
        for (int agentNo = 0; agentNo < Simulator.instance.getNumAgents(); agentNo++) {
            Vector2D goalVector = goals.get(agentNo).subtract(Simulator.instance.getAgentPosition(agentNo));
            final double lengthSq = goalVector.getNormSq();

            if (lengthSq > 1.0) {
                goalVector = goalVector.scalarMultiply(150.0 / FastMath.sqrt(lengthSq));
            }

            Simulator.instance.setAgentPreferredVelocity(agentNo, goalVector);

            // Perturb a little to avoid deadlocks due to perfect symmetry.
            final double angle = random.nextDouble() * 2.0 * FastMath.PI;
            final double distance = random.nextDouble() * 0.0001;

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
    	int agents = Integer.parseInt(args[0]);
    	//int agents = 20;
    	int w = 1920;
    	int h = 1080;
    	
    	
    	final LargeSpace02 blocks = new LargeSpace02(w, h);

    	long startSetupTime = System.currentTimeMillis();
        System.out.println("start setup: " + startSetupTime); 
        // Set up the scenario.
        blocks.setupScenario(agents);
        long endSetupTime = System.currentTimeMillis();
        System.out.println("end setup: " + endSetupTime);
        long elapsedTime = endSetupTime - startSetupTime;
        System.out.println("setup scenario elapsed: " + elapsedTime);
        
        
        startSetupTime = System.currentTimeMillis();
        System.out.println("start simulate: " + startSetupTime); 
        // Perform (and manipulate) the simulation.
        do {
            blocks.updateVisualization(startSetupTime);
            blocks.setPreferredVelocities();
            Simulator.instance.doStep();
        }
        while (!blocks.reachedGoal());
        endSetupTime = System.currentTimeMillis();
        System.out.println("end simulate: " + endSetupTime);
        elapsedTime = endSetupTime - startSetupTime;
        System.out.println("simulate elapsed: " + elapsedTime);
        
        System.out.println("simulation step: " +Simulator.instance.getGlobalTime());
    }

}
