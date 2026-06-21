/*
 * GenerateRGF.java
 *
 * Created on 27 March 2006, 14:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package lib;

/**
 * This class generates coalition structures in lexicographic order
 * Note: RGF is of form: 0,0,0,1
 *
 * @author cs50
 */
public class RGFGenerator0 {
    int agentNo;
    int [] f;
    int [] fmax;
    boolean done;
    
    /**
     * Contructure with no parameters
     */
    public RGFGenerator0() { }
    /**
     * Constructor with one parameter
     */
    public RGFGenerator0(int m) {
        agentNo = m;
        f = new int [agentNo];
        fmax = new int [agentNo];
        for(int i=0; i<agentNo; i++) {
            f[i] = 0;
            fmax[i] = 1;
        }
        done = false;
    }
    /**
     * Check if there is more CS
     */
    public boolean hasNext() {
        return !done;
    }
    
    /**
     * Get the f array.
     */
    public int [] getF() { return f; }
    /**
     * Get the fmax array.
     */
    public int [] getFmax() { return fmax; }
    
    /**
     * Set f to the given array.
     */
    public void setF(int [] f) {
    	for(int i=0; i<f.length; i++)
    		this.f[i] = f[i];
    }
    /**
     * Set fmax to the given array.
     */
    public void setFmax(int [] fx) {
    	for(int i=0; i<fx.length; i++)
    		this.fmax[i] = fx[i];
    }
    /**
     * Get the next states of fmax and f after generating the next n CSs
     * given the present states. If there are fewer CSs than n, return 
     * null.
     * @param	fm	states of fmax 
     * @param	f	states of f
     * @param	n	the n-th CSs from this present state. 
     * 
     * @return	null	if there are no more CSs, or
     * 			fmax and f otherwise.
     * 		
     */
    public int [][] getFmaxFStatesN(int [] fm, int [] f, int n) {
    	RGFGenerator0 tmp = new RGFGenerator0(fm.length);
    	int [][] states = new int [2][fm.length];
    	tmp.setFmax(fm);
    	tmp.setF(f);
    	int cnt = 0;
    	while(cnt++<n&&tmp.hasNext())
    		tmp.getNext();
    	if(!tmp.hasNext())
    		return null;
    	else
    		return states;
    }
    
    /**
     * Get the next CS.
     */
    public int [] getNext() {
        // Get the next to be returned for this call.
        int [] r = new int[f.length];
        for(int i=0; i<r.length; i++)
            r[i]=f[i];
        
        // Prepares the next for next call
        int j = agentNo;
        do{
            j--;
        } while(f[j]==fmax[j]);
        if(j>0) {
            f[j] = f[j]+1;
            for(int i=j+1; i<agentNo; i++) {
                f[i] = 0;
                if(f[j]==fmax[j])
                    fmax[i] = fmax[j]+1;
                else
                    fmax[i] = fmax[j];
            }
        } else {
            done = true;
        }
        return r;
    }
    
    /** Generates RGF for m agents
     */
    public void generateRGF(int m) {
        int [] f = new int [m];
        int [] fmax = new int [m];
        for(int i=0; i<m; i++) {
            f[i] = 0;
            fmax[i] = 1;
        }
        boolean done = false;
        while(!done) {
            viewRGF(f);
            viewRGF(fmax);
            System.out.println("\n");
            int j = m;
            do{
                j--;
            } while(f[j]==fmax[j]);
            if(j>0) {
                f[j] = f[j]+1;
                for(int i=j+1; i<m; i++) {
                    f[i] = 0;
                    if(f[j]==fmax[j])
                        fmax[i] = fmax[j]+1;
                    else
                        fmax[i] = fmax[j];
                }
            } else {
                done = true;
            }
        }
    }
    
    /**
     * Views RGF
     */
    private void viewRGF(int [] rgf) {
        for(int i=0; i<rgf.length; i++)
            System.out.print(rgf[i]+(i<rgf.length-1?",":"\n"));
    }
    
    /**
     * Main method.
     */
    public static void main(String [] args) {
        try {
            int m = Integer.parseInt(args[0]);
            new RGFGenerator0().generateRGF(m);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Usage: java GenerateRGF agentNo");
        }
    }
    
}
