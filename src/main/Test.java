package main;

import java.util.ArrayList;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Vertice> pointList = new ArrayList<Vertice>();
		pointList.add(new Vertice(2, 3));
		pointList.add(new Vertice(-2, 4));
		pointList.add(new Vertice(-3, -2));
		pointList.add(new Vertice(1, -2));
		
		for (int i = 0; i < pointList.size(); i++) {
			int next = i;
			int back = 0;
			
			if(i == 0) {
				back = i;
			} else {
				back = (pointList.size() - i);
			}
			
			System.out.println("next: " + next + ", back: " + back);
		}
		
		
		System.out.println(Math.pow(2, 64));
	}

}
