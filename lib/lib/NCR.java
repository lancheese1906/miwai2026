package lib;

import lib.*;

public class NCR {
	/*
	 * return -1 if error.
	 */
	public static long ncr(int n, int r) {
		long v = -1;
		if(n>=0&&r>=0&&r<=n) {
			v = 1;
			if(r>0) {
				// choose the higher of r and (n-r)
				int max = Math.max(r, n-r);
				int d = 1;
				for(int j=n; j>max; j--) {
					v*=j;
					v/=d++;
				}
			}
		}
		return v;
	}
	public static void main(String[] args) {
		int m=50;
		for(int i=0; i<=m; i++) {
			for(int j=0; j<=i; j++) {
				Logger.outl("n:"+i+", r:"+j+", "+i+"c"+j+":"+ncr(i, j));
			}
		}
	}
}
