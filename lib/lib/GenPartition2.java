package lib;

/**
 * Kreher's algorithm3.3
 * 
 * @author cs50
 *
 */
public class GenPartition2 {
	int [] a;
	int m;
	private void recPartition(int m, int b, int n) {
		if(m==0) {
			ArrayViewer.view1DIntArray(a);
			a[n]=0;
		} else {
			for(int i=1; i<=Math.min(b, m); i++) {
				a[n+1] = i;
				recPartition(m-i, i, n+1);
			}
		}
	}
	public GenPartition2(int mm) {
		a = new int[mm+1];
		m = mm;
	}
	private void run() {
		for(int i=m; i>=1; i--) {
			a[1]=i;
			recPartition(m-i, i, 1);
		}
	}
	public static void main(String [] args) {
		try {
			int m = Integer.parseInt(args[0]);
			GenPartition2 g = new GenPartition2(m);
			g.run();
		} catch(Exception ex) {
			Logger.logErr("Usage: lib.GenPartition2 m\n");
			ex.printStackTrace();
		}
	}
}
