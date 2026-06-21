package main;

public class Thesis_01 {
	public static void main(String[] args) {
		// sin = b/a
		// cos = c/a
		// tan = b/c

		float a = 5;
		float b = 3;
		float c = 4;

		System.out.println("a: " + a);
		System.out.println("b: " + b);
		System.out.println("c: " + c);

		// point 1 (x1, y1)
		Vertice point1 = new Vertice();
		point1.x = 2;
		point1.y = 3;

		System.out.println("point1.x = " + point1.x);
		System.out.println("point1.y = " + point1.y);

		System.out.println("Q1=========================");
		// point 2 (x2, y2)
		Vertice point2 = new Vertice();
		point2.x = 10;
		point2.y = 5;

		System.out.println("point2.x = " + point2.x);
		System.out.println("point2.y = " + point2.y);

		float aPrime = 1;
		float bPrime = (b * aPrime) / a; // ?
		float cPrime = (c * aPrime) / a; // ?
		System.out.println("aPrime: " + aPrime);
		System.out.println("bPrime: " + bPrime);
		System.out.println("cPrime: " + cPrime);

		// search
		// point 3 (x3, y3)
		Vertice point3 = new Vertice();
		point3.x = (point2.x + cPrime);
		point3.y = (point2.y + bPrime);

		System.out.println("point3.x = " + point3.x + ", point3.y = " + point3.y);

		System.out.println("Q2=========================");
		// point 2 (x2, y2)
		point2 = new Vertice();
		point2.x = -4;
		point2.y = 3;

		System.out.println("point2.x = " + point2.x);
		System.out.println("point2.y = " + point2.y);

		aPrime = 5;
		bPrime = (b * aPrime) / a; // ?
		cPrime = (c * aPrime) / a; // ?
		System.out.println("aPrime: " + aPrime);
		System.out.println("bPrime: " + bPrime);
		System.out.println("cPrime: " + cPrime);

		// search
		// point 3 (x3, y3)
		point3 = new Vertice();
		point3.x = (point2.x - cPrime);
		point3.y = (point2.y + bPrime);

		System.out.println("point3.x = " + point3.x + ", point3.y = " + point3.y);

		
		System.out.println("Q3=========================");
		// point 2 (x2, y2)
		point2 = new Vertice();
		point2.x = -4;
		point2.y = -3;

		System.out.println("point2.x = " + point2.x);
		System.out.println("point2.y = " + point2.y);

		aPrime = 5;
		bPrime = (b * aPrime) / a; // ?
		cPrime = (c * aPrime) / a; // ?
		System.out.println("aPrime: " + aPrime);
		System.out.println("bPrime: " + bPrime);
		System.out.println("cPrime: " + cPrime);

		// search
		// point 3 (x3, y3)
		point3 = new Vertice();
		point3.x = (point2.x - cPrime);
		point3.y = (point2.y - bPrime);

		System.out.println("point3.x = " + point3.x + ", point3.y = " + point3.y);

		System.out.println("Q4=========================");
		// point 2 (x2, y2)
		point2 = new Vertice();
		point2.x = 4;
		point2.y = -3;

		System.out.println("point2.x = " + point2.x);
		System.out.println("point2.y = " + point2.y);

		aPrime = 5;
		bPrime = (b * aPrime) / a; // ?
		cPrime = (c * aPrime) / a; // ?
		System.out.println("aPrime: " + aPrime);
		System.out.println("bPrime: " + bPrime);
		System.out.println("cPrime: " + cPrime);

		// search
		// point 3 (x3, y3)
		point3 = new Vertice();
		point3.x = (point2.x + cPrime);
		point3.y = (point2.y - bPrime);

		System.out.println("point3.x = " + point3.x + ", point3.y = " + point3.y);

		
		System.out.println("===========================");


		// search
		// point 4 (x4, y4)
		/*Point point4 = new Point();
		point4.x = (point1.x - cPrime);
		point4.y = (point1.y - bPrime);
		System.out.println("point4.x = " + (point1.x - cPrime));
		System.out.println("point4.y = " + (point1.y - bPrime));

		System.out.println(point4.x + ", " + point4.y);*/

	}
}
