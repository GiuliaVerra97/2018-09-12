package it.polito.tdp.poweroutages.model;

public class NeighborNerc implements Comparable<NeighborNerc>{
	
	private Nerc nerc;
	private int correlation;		//peso
	
	public NeighborNerc(Nerc nerc, int correlation){
		this.nerc = nerc;
		this.correlation = correlation;
	}

	@Override
	public String toString() {
		return "NeighborNerc [nerc=" + nerc + ", correlation=" + correlation + "]";
	}

	@Override
	public int compareTo(NeighborNerc o) {
		return Integer.compare(o.correlation,this.correlation);
	}

}
