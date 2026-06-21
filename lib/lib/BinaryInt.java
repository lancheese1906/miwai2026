package lib;

import java.util.Hashtable;

public class BinaryInt {
	public static int [] v = {
		0,
		(int)Math.pow(2, 1-1),
		(int)Math.pow(2, 2-1),
		(int)Math.pow(2, 3-1),
		(int)Math.pow(2, 4-1),
		(int)Math.pow(2, 5-1),
		(int)Math.pow(2, 6-1),
		(int)Math.pow(2, 7-1),
		(int)Math.pow(2, 8-1),
		(int)Math.pow(2, 9-1),
		(int)Math.pow(2, 10-1),
		(int)Math.pow(2, 11-1),
		(int)Math.pow(2, 12-1),
		(int)Math.pow(2, 13-1),
		(int)Math.pow(2, 14-1),
		(int)Math.pow(2, 15-1),
		(int)Math.pow(2, 16-1),
		(int)Math.pow(2, 17-1),
		(int)Math.pow(2, 18-1),
		(int)Math.pow(2, 19-1),
		(int)Math.pow(2, 20-1),
		(int)Math.pow(2, 21-1),
		(int)Math.pow(2, 22-1),
		(int)Math.pow(2, 23-1),
		(int)Math.pow(2, 24-1),
		(int)Math.pow(2, 25-1),
		(int)Math.pow(2, 26-1),
		(int)Math.pow(2, 27-1),
		(int)Math.pow(2, 28-1),
		(int)Math.pow(2, 29-1),
		(int)Math.pow(2, 30-1),
		(int)Math.pow(2, 31-1)
	};
	
//	public static int value(int f) {
//		return (int)Math.pow(2, f-1);
//	}
	/*
	 * Return the i-th position from right to left of the j-th one
	 * pos 987654321
	 * R = 110110101
	 * iThOne(3) is the 3rd 1 is at pos 5. 
	 */
	public static int iThOne(int r, int j) {
		int p = 0;
		for(int i=1; i<=32; i++) {
			if((v[i]|r)==r) {
				p++;
				if(p==j)
					return i;
			}
		}
		return 0;
	}
	/*
	 * For n=9, returns 1,4
	 */
	public static String toString(int n) {
		StringBuffer bf = new StringBuffer();
		if(n>0) {
			int f = Integer.numberOfTrailingZeros(n)+1;
			int l = Integer.SIZE-Integer.numberOfLeadingZeros(n);
			for(int i=f; i<=l; i++) {
				if((v[i]|n)==n) {
					bf.append(i>f?",":"").append(i);
				}
			}
		}
		return bf.toString();
	}
	public static boolean isAinB(int a, int b) {
		for(int i=Integer.numberOfTrailingZeros(a)+1; i<=31; i++) {
			if((a&v[i])==v[i]) {
				//Logger.outl(toString(a)+"&v["+i+"]:"+toString(v[i])+
				//		"==v["+i+"]:"+toString(v[i]));
				if((b&v[i])==0) {
					//Logger.outl(toString(b)+"&v["+i+"]:"+toString(v[i])+
					//		"==0"+toString(v[i]));
					return false;
				}
			}
		}
		return true;
	}
	/* 
	 * Generates coalitions in binary form as per Rahwan's
	 */
	public static void genNCR(int n) {
		Hashtable [] H = new Hashtable[n+1];
		Logger.outl("n:"+n);
		// generate nCr
		for(int i=1; i<=n; i++) {
			H[i] = new Hashtable();
			int c = 0;
			int m = 0;
			for(int k=1; k<=i; k++)
				m+=BinaryInt.v[k];
			int j = n;
			H[i].put(new Integer(m), new Integer(m));
			Logger.outl("H["+i+"]:"+H[i].size()+", "+
					BinaryInt.toString(m));
			while(j>0) {
				if((BinaryInt.v[j]|m)==m) {
					c++;
					if(j<n&&(BinaryInt.v[j+1]&m)==0) {
						m-=BinaryInt.v[j];
						for(int k=j+1; k<=j+c; k++)
							m=m|BinaryInt.v[k];
						for(int k=j+c+1; k<=n; k++)
							m=m&(Integer.MAX_VALUE-BinaryInt.v[k]);
						c=0;
						j=n;
						H[i].put(new Integer(m), new Integer(m));
						Logger.outl("H["+i+"]:"+H[i].size()+", "+
								BinaryInt.toString(m));
					} else {
						j--;
					}
				} else {
					j--;
				}
			}
		}
		for(int i=1; i<=(int)Math.pow(2, n)-1; i++) {
			int s=Integer.bitCount(i);
			int v=((Integer)(H[s].get(new Integer(i)))).intValue();
			Logger.outl("key:"+i+", value:"+v);
		}
	}
}