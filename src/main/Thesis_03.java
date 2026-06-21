package main;

import java.util.ArrayList;

public class Thesis_03 {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
/*		ArrayList<Point> pointList = new ArrayList<Point>();
		pointList.add(new Point(1, 1));
		pointList.add(new Point(2, 8));
		pointList.add(new Point(4, 9));
		pointList.add(new Point(7, 7));
		pointList.add(new Point(7, 2));*/
		
		ArrayList<Vertice> pointList = new ArrayList<Vertice>();
		pointList.add(new Vertice(2, 3));
		pointList.add(new Vertice(-2, 4));
		pointList.add(new Vertice(-3, -2));
		pointList.add(new Vertice(1, -2));
		
		System.out.println(pointList.size());
		
		ArrayList<Vertice> nodesList1 = new ArrayList<Vertice>();
		ArrayList<Vertice> nodesList2 = new ArrayList<Vertice>();
		
		for (int i = 0; i < pointList.size(); i++) {
			
			if(i < pointList.size()) {
				Vertice curPoint = pointList.get(i);
				Vertice curpreviousPoint = new Vertice();
				Vertice previousPoint = new Vertice();
				
				if(i == 0) {
					curpreviousPoint = pointList.get(i);
					previousPoint = pointList.get(pointList.size() - 1);
				} else {
					curpreviousPoint = pointList.get(pointList.size() - i);
					previousPoint = pointList.get((pointList.size() - i) - 1);
				}
				
				
				Vertice nodeNPoint = new Vertice();
				Vertice nodeBPoint = new Vertice();
				
				
				
				if(i < (pointList.size() - 1)) {
					System.out.println("----------------");
					System.out.println("<");
					Vertice nextPoint = pointList.get(i + 1);
					
					
					System.out.println("curPoint.x = " + curPoint.x);
					System.out.println("curPoint.y = " + curPoint.y);
					
					System.out.println("nextPoint.x = " + nextPoint.x);
					System.out.println("nextPoint.y = " + nextPoint.y);
					
					double dx = nextPoint.x - curPoint.x;
					double dy = nextPoint.y - curPoint.y;
					
					System.out.println("DX: " + dx + ", DY: " + dy);
					System.out.println("PowDX: " + Math.pow(dx, 2) + ", PowDY: " + Math.pow(dy, 2));
					
					double dist = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
					
					System.out.println("DIST: " + dist);
					
					double ndx = ((3 * dx) / dist);
					double ndy = ((3 * dy) / dist);
					
					System.out.println("ndx: " + ndx + ", ndy: " + ndy);
					
					double nx = nextPoint.x + ndx;
					double ny = nextPoint.y + ndy;
					
					System.out.println("nx: " + nx + ", ny: " + ny);
					
					nodeNPoint.x = nx;
					nodeNPoint.y = ny;
					
					System.out.println("----------------");
					
					// back
					

					System.out.println("curpreviousPoint.x = " + curpreviousPoint.x);
					System.out.println("curpreviousPoint.y = " + curpreviousPoint.y);
					
					System.out.println("previousPoint.x = " + previousPoint.x);
					System.out.println("previousPoint.y = " + previousPoint.y);
					
					dx = previousPoint.x - curpreviousPoint.x;
					dy = previousPoint.y - curpreviousPoint.y;
					
					System.out.println("BDX: " + dx + ", BDY: " + dy);
					System.out.println("BPowDX: " + Math.pow(dx, 2) + ", BPowDY: " + Math.pow(dy, 2));
					
					dist = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
					
					System.out.println("BDIST: " + dist);
					
					ndx = ((3 * dx) / dist);
					ndy = ((3 * dy) / dist);
					
					System.out.println("bndx: " + ndx + ", bndy: " + ndy);
					
					nx = previousPoint.x + ndx;
					ny = previousPoint.y + ndy;
					
					System.out.println("bnx: " + nx + ", bny: " + ny);
					
					nodeBPoint.x = nx;
					nodeBPoint.y = ny;
					System.out.println("----------------");
					
				} else {
					System.out.println("----------------");
					System.out.println(">");
					Vertice nextPoint = pointList.get(0);
					
					System.out.println("curPoint.x = " + curPoint.x);
					System.out.println("curPoint.y = " + curPoint.y);
					
					System.out.println("nextPoint.x = " + nextPoint.x);
					System.out.println("nextPoint.y = " + nextPoint.y);
					
					double dx = nextPoint.x - curPoint.x;
					double dy = nextPoint.y - curPoint.y;
					
					System.out.println("DX: " + dx + ", DY: " + dy);
					System.out.println("PowDX: " + Math.pow(dx, 2) + ", PowDY: " + Math.pow(dy, 2));
					
					double dist = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
					
					System.out.println("DIST: " + dist);
					
					double ndx = ((3 * dx) / dist);
					double ndy = ((3 * dy) / dist);
					
					System.out.println("ndx: " + ndx + ", ndy: " + ndy);
					
					double nx = nextPoint.x + ndx;
					double ny = nextPoint.y + ndy;
					
					System.out.println("nx: " + nx + ", ny: " + ny);
					
					nodeNPoint.x = nx;
					nodeNPoint.y = ny;
					
					System.out.println("----------------");
					
					// back
					
					System.out.println("curpreviousPoint.x = " + curpreviousPoint.x);
					System.out.println("curpreviousPoint.y = " + curpreviousPoint.y);
					
					System.out.println("previousPoint.x = " + previousPoint.x);
					System.out.println("previousPoint.y = " + previousPoint.y);
					
					dx = previousPoint.x - curpreviousPoint.x;
					dy = previousPoint.y - curpreviousPoint.y;
					
					System.out.println("BDX: " + dx + ", BDY: " + dy);
					System.out.println("BPowDX: " + Math.pow(dx, 2) + ", BPowDY: " + Math.pow(dy, 2));
					
					dist = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
					
					System.out.println("BDIST: " + dist);
					
					ndx = ((3 * dx) / dist);
					ndy = ((3 * dy) / dist);
					
					System.out.println("bndx: " + ndx + ", bndy: " + ndy);
					
					nx = previousPoint.x + ndx;
					ny = previousPoint.y + ndy;
					
					System.out.println("bnx: " + nx + ", bny: " + ny);
					
					nodeBPoint.x = nx;
					nodeBPoint.y = ny;
					System.out.println("----------------");
				}
				
				nodesList1.add(nodeNPoint);
				nodesList2.add(nodeBPoint);
				
				System.out.println("[" + i + "]nodeNPoint.x = " + nodeNPoint.x + ", nodeNPoint.y = " + nodeNPoint.y);
				System.out.println("[" + i + "]nodeBPoint.x = " + nodeBPoint.x + ", nodeBPoint.y = " + nodeBPoint.y);
				
				/*System.out.println("==============================");
				
				double x = (nodeNPoint.x + nodeBPoint.x) / 2;
				double y = (nodeNPoint.y + nodeBPoint.y) / 2;
				
				System.out.println("nodeNPoint.x: " + nodeNPoint.x + ", nodeBPoint.x: " + nodeBPoint.x);
				System.out.println("nodeNPoint.y: " + nodeNPoint.y + ", nodeBPoint.y: " + nodeBPoint.y);
				System.out.println("nodeNPoint.x + nodeBPoint.x: " + (nodeNPoint.x + nodeBPoint.x));
				System.out.println("nodeNPoint.y + nodeBPoint.y: " + (nodeNPoint.y + nodeBPoint.y));
				System.out.println("(nodeNPoint.x + nodeBPoint.x) / 2: " + (nodeNPoint.x + nodeBPoint.x) / 2);
				System.out.println("(nodeNPoint.y + nodeBPoint.y) / 2: " + (nodeNPoint.y + nodeBPoint.y) / 2);
				
				System.out.println("x: " + x + ", y: " + y);*/
				
			} else {
				System.out.println(">");
			}

		}
		
		System.out.println("================================================================");
		
		//Collections.reverse(nodesList2);
		//Collections.reverse(nodesList1);
		
		for (int j = 0; j < pointList.size(); j++) {
			Vertice p1 = new Vertice();
			Vertice p2 = new Vertice();
			
			
			if (j < pointList.size()) {
				if (j == 0) {
					p1 = nodesList1.get(nodesList1.size() - 1);
					p2 = nodesList2.get(nodesList2.size() - 1);
				} else {
					p1 = nodesList1.get(j - 1);
					p2 = nodesList2.get(nodesList2.size() - (j + 1));
				}
				
				System.out.println("p1: " + p1.x + ", " + p1.y);
				System.out.println("p2: " + p2.x + ", " + p2.y);
				
				double x = (p1.x + p2.x) / 2;
				double y = (p1.y + p2.y) / 2;
				
				System.out.println(x + ", " + y);
			} else {
				
			}
			
			//Point p1 = nodesList1.get(j);
			//Point p2 = nodesList2.get(j);
			
			
			
			//System.out.println("p1: " + p1.x + ", " + p1.y);
			//System.out.println("p2: " + p2.x + ", " + p2.y);
			
			//System.out.println("=============>");
			
			/*if(j == 0) {
				p1 = nodesList1.get(1);
				p2 = nodesList2.get(nodesList2.size() - 1);
			} else if((nodesList1.size()) > j) {
				p1 = nodesList1.get(j + 1);
				p2 = nodesList2.get((nodesList2.size() - j) - 1);
			} else {
				
			}*/
			
			
			System.out.println("=========================>");
			
		}
	}
}
