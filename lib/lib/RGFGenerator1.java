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
 * Note: RGF is of form: 1,1,1,2 not 0,0,0,1
 *
 * @author cs50
 */
public class RGFGenerator1 {
    int agentNo;
    int [] f;
    int [] fmax;
    boolean done;
    
    /**
     * Contructure with no parameters
     */
    public RGFGenerator1() { }
    /**
     * Constructor with one parameter
     */
    public RGFGenerator1(int m) {
        agentNo = m;
        f = new int [agentNo+1];
        fmax = new int [agentNo+1];
        for(int i=1; i<=agentNo; i++) {
            f[i] = 1;
            fmax[i] = 2;
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
     * Get the next CS. 0,1,1,1,2,2,2
     */
    public int [] getNext() {
        // Get the next to be returned for this call.
        int [] r = new int[f.length];
        for(int i=0; i<r.length; i++)
            r[i]=f[i];
        
        // Prepares the next for next call
        int j = agentNo+1;
        do{
            j--;
        } while(f[j]==fmax[j]);
        if(j>1) {
            f[j] = f[j]+1;
            for(int i=j+1; i<=agentNo; i++) {
                f[i] = 1;
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
        int [] f = new int [m+1];
        int [] fmax = new int [m+1];
        for(int i=1; i<=m; i++) {
            f[i] = 1;
            fmax[i] = 2;
        }
        boolean done = false;
        while(!done) {
            viewRGF(f);
            int j = m+1;
            do{
                j--;
            } while(f[j]==fmax[j]);
            if(j>1) {
                f[j] = f[j]+1;
                for(int i=j+1; i<=m; i++) {
                    f[i] = 1;
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
        for(int i=1; i<rgf.length; i++)
            System.out.print(rgf[i]+(i<rgf.length-1?",":"\n"));
    }
    
    /**
     * Main method.
     */
    public static void main(String [] args) {
        try {
            int m = Integer.parseInt(args[0]);
            new RGFGenerator1().generateRGF(m);
        } catch(Exception e) {
            System.out.println("Usage: java GenerateRGF agentNo");
            e.printStackTrace();
        }
    }
    
}
