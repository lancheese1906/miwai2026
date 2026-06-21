package main;

/**
 * @author PoN
 *
 */
public class Vertice {
	double x;
	double y;

	/**
	 * 
	 */
	public Vertice() {
		;
	}

	public Vertice(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public void printPoint(Vertice vertice) {
		System.out.println(vertice.getX() + ", " + vertice.getY());
	}
}
