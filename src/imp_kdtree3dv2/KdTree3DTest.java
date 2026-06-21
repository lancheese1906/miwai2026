package imp_kdtree3dv2;

import java.util.Random;


public class KdTree3DTest {
	public static long seed = 6;
	public static void main(String[] args) {
		// initialize the two data structures with point from standard input
        System.out.println("Loading....");
		KdTree3D kdtree = new KdTree3D();

		Random rand = new Random(seed);
		for (int i = 0; i < 25; i++) {
			double x = rand.nextInt(100);
			double y = rand.nextInt(100);

			double dist = distance(0, 0, x, y);
			Point3D p = new Point3D(dist, x, y);
			System.out.println(String.format("%.2f", dist) + ", " + x + ", " + y);
			kdtree.insert(p);
			//System.out.println("-------------------------------------------------------");
			//brute.insert(p);
		}
		
		System.out.println("size: "+kdtree.size());
        
		System.out.println("End loading");
		Point3D query = new Point3D(3, 3, 0);
		
        long startSearchTime = System.currentTimeMillis();
        System.out.println("start: " + startSearchTime); 
        Point3D p = kdtree.nearest(query);
        System.out.println("nearest query "+query.toString()+" is: " + p.toString());
        long endSearchTime = System.currentTimeMillis();
        System.out.println("end: " + endSearchTime);
        long elapsedTime = endSearchTime - startSearchTime;
        System.out.println("elapsedTime_kdtree: " + elapsedTime);
	}
	
	public static double distance(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return Math.sqrt(dx * dx + dy * dy);
	}
}
