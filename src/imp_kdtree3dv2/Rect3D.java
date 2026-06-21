package imp_kdtree3dv2;


/*************************************************************************
 *  Compilation:  javac RectHV.java
 *  Execution:    java RectHV
 *  Dependencies: Point2D.java
 *
 *  Implementation of 2D axis-aligned rectangle.
 *
 *************************************************************************/

public class Rect3D {
	private final double dmin, dmax;
    private final double xmin, ymin;   // minimum x- and y-coordinates
    private final double xmax, ymax;   // maximum x- and y-coordinates

    // construct the axis-aligned rectangle [xmin, xmax] x [ymin, ymax]
    public Rect3D(double dmin, double xmin, double ymin, double dmax, double xmax, double ymax) {
        if (xmax < xmin || ymax < ymin) {
            throw new IllegalArgumentException("Invalid rectangle");
        }
        this.dmin = dmin;
        this.xmin = xmin;
        this.ymin = ymin;
        this.dmax = dmax;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    // accessor methods for 4 coordinates
    public double dmin() { return dmin; }
    public double dmax() { return dmax; }
    public double xmin() { return xmin; }
    public double ymin() { return ymin; }
    public double xmax() { return xmax; }
    public double ymax() { return ymax; }

    // width and height of rectangle
    public double width()  { return xmax - xmin; }
    public double height() { return ymax - ymin; }

    // does this axis-aligned rectangle intersect that one?
    public boolean intersects(Rect3D that) {
        return this.xmax >= that.xmin && this.ymax >= that.ymin
            && that.xmax >= this.xmin && that.ymax >= this.ymin;
    }

    // distance from p to closest point on this axis-aligned rectangle
    public double distanceTo(Point3D p) {
        return Math.sqrt(this.distanceSquaredTo(p));
    }

    // distance squared from p to closest point on this axis-aligned rectangle
    public double distanceSquaredTo(Point3D p) {
        double dx = 0.0, dy = 0.0;
        if (p.getX() < xmin) { 
        	dx = p.getX() - xmin;
        } else if (p.getX() > xmax) { 
        	dx = p.getX() - xmax;
        }
        
        if (p.getY() < ymin) { 
        	dy = p.getY() - ymin;
        } else if (p.getY() > ymax) { 
        	dy = p.getY() - ymax;
        }
        return dx*dx + dy*dy;
    }

    // does this axis-aligned rectangle contain p?
    public boolean contains(Point3D p) {
        return (p.getX() >= xmin) && (p.getX() <= xmax)
            && (p.getY() >= ymin) && (p.getY() <= ymax);
    }

    // are the two axis-aligned rectangles equal?
    public boolean equals(Object y) {
        if (y == this) return true;
        if (y == null) return false;
        if (y.getClass() != this.getClass()) return false;
        Rect3D that = (Rect3D) y;
        if (this.xmin != that.xmin) return false;
        if (this.ymin != that.ymin) return false;
        if (this.xmax != that.xmax) return false;
        if (this.ymax != that.ymax) return false;
        return true;
    }

    // return a string representation of this axis-aligned rectangle
    public String toString() {
        return "[" + xmin + ", " + xmax + "] x [" + ymin + ", " + ymax + "]";
    }

}