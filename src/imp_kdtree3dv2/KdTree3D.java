package imp_kdtree3dv2;

/****************************************************************************
 * Author: Alberto Gutiérrez Jácome <agjacome@gmail.com>
 * Date: 16/09/2012
 * 
 * Compilation: javac KdTree.java
 * Execution: not applicable
 * Dependencies: Point2D.java RectHV.java StdDraw.java Queue.java
 * 
 * Description: A mutable data type that uses a 2d-tree to represent a set of
 * points in the unit square. A 2d-tree is a generalization of a BST to
 * two-dimensional keys. The idea is to build a BST with points in the nodes,
 * using the x and y coordinates of the points as keys in strictly alternating
 * sequence. The prime advantage of a 2d-tree over a BST is that it supports
 * efficient implementation of range search and nearest neighbor search. Each
 * node corresponds to an axis-aligned rectangle in the unit square, which
 * encloses all of the points in its subtree. The root corresponds to the unit
 * square; the left and right children of the root corresponds to the two
 * rectangles split by the x-coordinate of the point at the root; and so
 * forth.
 * 
 ***************************************************************************/

public class KdTree3D
{

    // helper data type representing a node of a kd-tree
    private static class KdNode
    {
        private KdNode        left;
        private KdNode        right;
        private int dim;
        private double dist;
        private double  x;
        private double  y;

        public KdNode(double dist, double x, double y, KdNode l, KdNode r, int d)
        {
        	this.dist = dist;
        	this.x = x;
            this.y = y;
            left  = l;
            right = r;
            dim = d;
        }
    }

    private static final Rect3D CONTAINER = new Rect3D(0, 0, 0, 0, 4000, 4000);
    private KdNode root;
    private int    size;

    // construct an empty tree of points
    public KdTree3D()
    {
        size = 0;
        root = null;
    }



    // helper: add point p to subtree rooted at node
    private KdNode insert(final KdNode node, final Point3D p, int dim)
    {
        // if new node, create it
        if (node == null) {
            size++;
            System.out.printf("create node: dist: %f, x: %f, y: %f \n", p.getDist(), p.getX(), p.getY());
            return new KdNode(p.getDist() ,p.getX(), p.getY(), null, null, dim);
        }

        // if already in, return it
        if (node.dist == p.getDist() && node.x == p.getX() && node.y == p.getY()) {
        	System.out.printf("duplicate: x: %f, y: %f \n", p.getX(), p.getY());
        	return node;
        }

        // else, insert it where corresponds (left - right recursive call)
        //if ((node.dim == 1) && node.x >= p.x() || (node.dim == 0)&& node.y >= p.y()) {
        if ((node.dim == 1) && node.dist >= p.getDist() || (node.dim == 2)&& node.x >= p.getX() || (node.dim == 3)&& node.y >= p.getY()) {
        	System.out.printf("left-node: dist: %f, x: %f, y: %f,  dim: %d\n", p.getDist(), p.getX(), p.getY(), node.dim);
            //node.left = insert(node.left, p, node.dim == 1? 0:1);
        	if(node.dim == 1) {
        		dim = 2;
        	} else if (node.dim == 2){
        		dim = 3;
        	} else {
        		dim = 1;
        	}

        	node.left = insert(node.left, p, dim);
       // } else if(node) {
        	
        } else {
        	System.out.printf("right-node: dist: %f, x: %f, y: %f,  dim: %d\n", p.getDist(), p.getX(), p.getY(), node.dim);
            //node.right = insert(node.right, p, node.dim == 1? 0:1);
        	if(node.dim == 1) {
        		dim = 2;
        	} else if (node.dim == 2){
        		dim = 3;
        	} else {
        		dim = 1;
        	}
        	node.right = insert(node.right, p, dim);
        }

        return node;
    }

    // add the point p to the tree (if it is not already in the tree)
    public void insert(final Point3D p)
    {
        root = insert(root, p, 1);
    }

    // is the tree empty?
    public boolean isEmpty()
    {
        return size == 0;
    }

    // number of points in the tree
    public int size()
    {
        return size;
    }
    
 // helper: nearest neighbor of (x,y) in subtree rooted at node
    private Point3D nearest(final KdNode node, final Rect3D rect,  
          final double dist,  final double x, final double y, final Point3D candidate)
    {
        if (node == null) return candidate;

        double dqn = 0.0;
        double drq = 0.0;
        Rect3D left = null;
        Rect3D rigt = null;
        final Point3D query = new Point3D(dist, x, y);
        Point3D nearest = candidate;

        if (nearest != null) {
            dqn = query.distanceSquaredTo(nearest);
            drq = rect.distanceSquaredTo(query);
        }

        if (nearest == null || dqn > drq) {
            final Point3D point = new Point3D(node.dist, node.x, node.y);
            if (nearest == null || dqn > query.distanceSquaredTo(point))
                nearest = point;

            if (node.dim == 1) { //here to check 
                System.out.println(">>"+node.dim);
                //impl distance dimension
                
                
            } else if(node.dim == 2) {
            	System.out.println(">>"+node.dim);
            	left = new Rect3D(0 ,rect.xmin(), rect.ymin(), 0, node.x, rect.ymax());
                rigt = new Rect3D(0, node.x, rect.ymin(), 0, rect.xmax(), rect.ymax());

                if (x < node.x) {
                    nearest = nearest(node.left, left, dist, x, y, nearest);
                    nearest = nearest(node.right, rigt, dist, x, y, nearest);
                } else {
                    nearest = nearest(node.right, rigt, dist, x, y, nearest);
                    nearest = nearest(node.left, left, dist, x, y, nearest);
                }
            } else {
            	System.out.println(">>"+node.dim);
                left = new Rect3D(0, rect.xmin(), rect.ymin(), 0, rect.xmax(), node.y);
                rigt = new Rect3D(0, rect.xmin(), node.y, 0, rect.xmax(), rect.ymax());

                if (y < node.y) {
                    nearest = nearest(node.left, left, dist, x, y, nearest);
                    nearest = nearest(node.right, rigt, dist, x, y, nearest);
                } else {
                    nearest = nearest(node.right, rigt, dist, x, y, nearest);
                    nearest = nearest(node.left, left, dist, x, y, nearest);
                }
            }
        }

        return nearest;
    }
    
    // a nearest neighbor in the set to p; null if set is empty
    public Point3D nearest(final Point3D p)
    {
        return nearest(root, CONTAINER, p.getDist(), p.getX(), p.getY(), null);
    }

}
