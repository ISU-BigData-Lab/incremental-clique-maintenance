package utils;


public class Vscore implements Comparable<Vscore>{
	
	private int vertex;
	private int score;
	
	public int getVertex() {
		return vertex;
	}

	public void setVertex(int vertex) {
		this.vertex = vertex;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	
	public Vscore(int v, int score) {
		this.vertex = v;
		this.score = score;
	}
	
	@Override
	public int compareTo(Vscore s) {
		
		return Integer.compare(score, s.score);
	}
	
	

}
