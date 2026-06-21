package lib;

/**
 * Modified Kreher's algorithm 3.4
 * @author cs50
 *
 */

public class GenPartition4 {
	ConjPartition conj;
	int [] a;
	int m;
	private void recPartition3(int m, int b, int n) {
		if(m==0) {
			ArrayViewer.view1DIntArray(a);
			int [] bb = conj.conjPartition(a);
			ArrayViewer.view1DIntArray(bb);
			a[n]=0;
		} else {
			for(int i=1; i<=Math.min(b, m); i++) {
				a[n+1] = i;
				recPartition3(m-i, i, n+1);
			}
		}
	}
	public GenPartition4(int mm) {
		m = mm;
		a = new int[m+1];
		
	}
	private void run() {
		a[1]=1;
		recPartition3(5, 1, 1);
		Logger.logOut("---\n");
		a[1]=2;
		recPartition3(4, 2, 1);
		Logger.logOut("---\n");
		a[1]=3;
		recPartition3(3, 3, 1);
		Logger.logOut("---\n");
		a[1]=4;
		recPartition3(2, 4, 1);
		Logger.logOut("---\n");
		a[1]=5;
		recPartition3(1, 5, 1);
		Logger.logOut("---\n");
		a[1]=6;
		recPartition3(0, 6, 1);
/*		
		for(int i=1; i<=m; i++) {
			a[1]=i;
			recPartition3(m-i, i, 1);
		}
		*/
	}
	public static void main(String [] args) {
		int m = 0;
		GenPartition4 g;
		
		m=5;
		
		try {
			if(m==0) {
				m = Integer.parseInt(args[0]);
			}
		} catch(Exception ex) {
			Logger.logErr("Usage: lib.GenPartition4 m\n");
			ex.printStackTrace();
		}
		g = new GenPartition4(m);
		g.run();
	}
}

