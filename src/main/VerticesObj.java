package main;

import java.util.ArrayList;

public class VerticesObj {
	double x;
	double y;
	int obsIndex;
	Vertice enlargedVertice;
	Vertice originVertice;
	ArrayList<Vertice> originListVertics;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getObsIndex() {
		return obsIndex;
	}

	public void setObsIndex(int obsIndex) {
		this.obsIndex = obsIndex;
	}

	public Vertice getEnlargedVertice() {
		return enlargedVertice;
	}

	public void setEnlargedVertice(Vertice enlargedVertice) {
		this.enlargedVertice = enlargedVertice;
	}

	public Vertice getOriginVertice() {
		return originVertice;
	}

	public void setOriginVertice(Vertice originVertice) {
		this.originVertice = originVertice;
	}

	public ArrayList<Vertice> getOriginListVertics() {
		return originListVertics;
	}

	public void setOriginListVertics(ArrayList<Vertice> originListVertics) {
		this.originListVertics = originListVertics;
	}
}
