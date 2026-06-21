/*
 * BellNoLong.java
 *
 * Created on 24 March 2006, 19:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package lib;

import java.text.DecimalFormat;
/**
 *
 * @author cs50
 */
public class BellNoLong {
    static boolean verbose = false;
    
    /** Creates a new instance of BellNoLong */
    public BellNoLong() {
    }
    public static void setVerbose(boolean v) {
    	verbose = v;
    }
    /**
     * Get sum of lSN
     */
    static long getSumlSN(int n) {
        long sumVCS = 0;
        for(int i=1; i<=n; i++) {
            long lsn = lSN(n, i);
            if(verbose)
            	System.out.println("k="+i+", lSN="+lsn);
            sumVCS += lsn;
        }
        return sumVCS;
    }
    
    
    /**
     * Computes SN in long.
     * 
     * @param   n   agentNo
     * @param   k   No of coalitions in this partition
     *
     * @return  No of different partitions.
     */
    static long lSN(int n, int k) {
        if(n==k) {
            return (long)1;        
        } else if(k==0) {
            return (long)0;
        }
        long sn = k*(lSN(n-1,k)) + lSN(n-1, k-1);
        return sn;
    }
    public static long getBellNo(int n) {
    	long b = 0;
    	b = BellNoLong.getSumlSN(n);
    	return b;
    }
    public static String getStringBellNo(int n) {
        DecimalFormat f = new DecimalFormat("#,###");
        String s = f.format(BellNoLong.getSumlSN(n));
    	return s;
    }
    /**
     * The main method.
     */
    public static void main(String [] args) {
        if(args.length<1) {
            System.out.println("Usage: java CoalStrucGen agentNo");
            throw new IllegalArgumentException();
        }
        int n = Integer.parseInt(args[0]);
        DecimalFormat f = new DecimalFormat("#,###");
        System.out.println("No of Coal Structures:"+ f.format(BellNoLong.getSumlSN(n)));
    }
}
