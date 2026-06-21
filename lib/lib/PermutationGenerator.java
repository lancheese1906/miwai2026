/*
 * PermutationGenerator.java
 *
 * Created on 26 December 2004, 09:43
 * 
 * Output is of form:
 * 0,1,2; 1,0,2; 1,2,0;...
 */

package lib;

import java.math.BigInteger;
public class PermutationGenerator {
    private short[] a;
    private BigInteger numLeft;
    private BigInteger total;
    
    /** Creates a new instance of PermutationGenerator */
    public PermutationGenerator(int n) {
        if(n<1)
            throw new IllegalArgumentException("Min 1");
        a = new short[n];
        total = getFactorial(n);
        reset();
    }
    public void reset() {
        for(int i=0; i<a.length; i++) {
            a[i]=(short)i;
        }
        numLeft = new BigInteger(total.toString());
    }
    public BigInteger getNumLeft() {
        return numLeft;
    }
    public BigInteger getTotal() {
        return total;
    }
    public boolean hasMore() {
        return numLeft.compareTo(BigInteger.ZERO) == 1;
    }
    private static BigInteger getFactorial(int n) {
        BigInteger fact = BigInteger.ONE;
        for(int i=n; i>1; i--) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }
    public short[] getNext() {
        if(numLeft.equals(total)) {
            numLeft = numLeft.subtract(BigInteger.ONE);
            return a;
        }
        short temp;
        int j = a.length - 2;
        while(a[j]>a[j+1]) {
            j--;
        }
        int k = a.length - 1;
        while(a[j] > a[k]) {
            k--;
        }
        temp = a[k];
        a[k] = a[j];
        a[j] = temp;
        
        int r = a.length - 1;
        int s = j + 1;
        while(r > s) {
            temp = a[s];
            a[s] = a[r];
            a[r] = temp;
            r--;
            s++;
        }
        numLeft = numLeft.subtract(BigInteger.ONE);
        return a;
    }
    public static void main(String [] args) {
    	if(args.length!=1) {
    		Logger.logErr("Usage: java lib.PermutationGenerator n");
    	}
    	int n=0;
    	try {
    		n = Integer.parseInt(args[0]);
    		PermutationGenerator p = new PermutationGenerator(n);
    		while(p.hasMore()) {
    			short [] t = p.getNext();
    			ArrayViewer.view1DShortArray(t);
    		}
    	} catch(Exception ex) {
    		ex.printStackTrace();    		
    	}
    }
}