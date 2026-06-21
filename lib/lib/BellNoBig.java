/*
 * BellNoBig.java
 *
 * Created on 24 March 2006, 19:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package lib;

import java.math.BigInteger;
/**
 *
 * @author cs50
 */
public class BellNoBig {
    static boolean verbose = false;
    /** Creates a new instance of BellNoBig */
    public BellNoBig() {
    }

    public static void setVerbose(boolean v) {
    	verbose = v;
    }
    /**
     * Get number of coalition structures.
     */
    public static BigInteger getBellNo(int n) {
        BigInteger bellNo = new BigInteger("0");
        for(int i=1; i<=n; i++) {
            BigInteger sn = SN(n, i);
            bellNo = bellNo.add(sn);
            if(verbose)
            	System.out.println("k="+i+", bSN:"+sn.toString());
        }
        return bellNo;
    }
    /**
     * Stirling Number of the Second Kind
     * S(n,k) = S(n-1,k-1) + k*S(n-1,k) where S(n,n)=1.
     */
    static BigInteger SN(int n, int k) {
        if(n==k)
            return new BigInteger("1");        
        else if(k==0)
            return new BigInteger("0");

        BigInteger sn = null;
        try {
            sn = new BigInteger(String.valueOf(k));
            sn = sn.multiply(SN(n-1, k));
            sn = sn.add(SN(n-1, k-1));
        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        return sn;
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
        System.out.println("No of Coal Structures:"+ BellNoBig.getBellNo(n));
    }
}
