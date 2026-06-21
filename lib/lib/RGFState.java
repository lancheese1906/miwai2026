package lib;

public class RGFState {
	int [] fmax0; // Initial state of fmax
	int [] f0; // Initial state of f
	int [] fmax; // Last state of fmax
	int [] f; // Last stat of f
	long stateNo = 0; // Number of states
	public RGFState(int [] fx, int [] ff) {
		fmax0 = new int [fx.length];
		fmax = new int [fx.length];
		f0 = new int [fx.length];
		f = new int [fx.length];
		for(int i=0; i<fx.length; i++) {
			fmax0[i]=fmax[i]=fx[i];
			f0[i]=f[i]=ff[i];
		}
		stateNo = 1;
	}
	public void setFinalState(int [] fx, int [] ff) {
		for(int i=0; i<fx.length; i++) {
			fmax[i] = fx[i];
			f[i] = ff[i];
		}
	}

}
