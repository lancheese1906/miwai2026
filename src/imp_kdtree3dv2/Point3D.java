package imp_kdtree3dv2;

public class Point3D implements Comparable<Point3D> {
	private double dist;
	private double x;
	private double y;

	public Point3D(double dist, double x, double y) {
		this.dist = dist;
		this.x = x;
		this.y = y;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
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

	public double distanceSquaredTo(Point3D p) {
		double dx = this.x - p.x;
		double dy = this.y - p.y;
		return dx * dx + dy * dy;
	}

	@Override
	public String toString() {
		return "(dist=" + dist + ", x=" + x + ", y=" + y + ")";
	}

	@Override
	public int compareTo(Point3D o) {
		if (this.dist < o.dist) {
			return -1;
		}
		if (this.dist > o.dist) {
			return +1;
		}
		if (this.y < o.y) {
			return -1;
		}
		if (this.y > o.y) {
			return +1;
		}
		if (this.x < o.x) {
			return -1;
		}
		if (this.x > o.x) {
			return +1;
		}
		return 0;
	}
}
