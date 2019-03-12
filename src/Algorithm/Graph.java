package Algorithm;

/**

 * This program creates an undirected graph G from an input file 
 * where the edges are represented as pair of integers 
 * and integers represent vertices of the graph. 
 * It is assumed that there is no duplicate edge and self loop in the graph.
 * */
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import utils.SetOperations;
import utils.Vscore;

public class Graph {

	private Scanner scanner;

	public HashMap<Integer, HashSet<Integer>> AdjList;

	private Map<Integer, Integer> degenscores; // (key, value) pair; key = vertex, value = degeneracy score
	
	private Vscore[] S;
	
	private int degeneracy; // contains degeneracy value

	private Map<Integer, Integer> trianglecount; // (key, value) pair; key = vertex, value = triangle count
	

	public Graph() {

		this.AdjList = new HashMap<>();
	}

	public Graph(Graph G) {

		AdjList = new HashMap<>(G.AdjList);

	}

	// Read input graph when the input graph is in the form of adjacency list
	// instead of edge list in a file
	// args is referred to that file
	public Graph(String args, int val) {

		AdjList = new HashMap<>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(args));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splits = line.split("\\s+");
				if (splits.length == 1) {
					AdjList.put(Integer.parseInt(splits[0]), new HashSet<>());
				} else {
					int u = Integer.parseInt(splits[0]);
					HashSet<Integer> S = new HashSet<>();
					// HashSet<Integer> S = new HashSet<Integer>();
					for (int i = 1; i < splits.length; i++) {
						S.add(Integer.parseInt(splits[i]));
					}
					AdjList.put(u, S);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Graph(String args) {

		AdjList = new HashMap<>();
		

		trianglecount = new HashMap<>();

		try {
			//File file = new File(args);
			//scanner = new Scanner(file);
			BufferedReader br = new BufferedReader(new FileReader(args));
			String line;

			while ((line = br.readLine()) != null) {
				String[] splits = line.split("\\s+");
				int u = Integer.parseInt(splits[0]);
				int v = Integer.parseInt(splits[1]);

				if (u != v)
					addEdge(u, v);
				// System.out.println(i + " " + j);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Graph(int n) {
		this.AdjList = new HashMap<>();

		for (int i = 0; i < n; i++) {
			this.AdjList.put(i, new HashSet<>());
		}
	}

	public void addEdge(int u, int v) {

		if (!containsEdge(u, v) && !containsEdge(v, u)) {
			
/*
			if (!trianglecount.keySet().contains(u))
				trianglecount.put(u, 0);
			if (!trianglecount.keySet().contains(v))
				trianglecount.put(v, 0);
*/
			if (AdjList.get(u) == null) {

				HashSet<Integer> S = new HashSet<>();
				S.add(v);
				AdjList.put(u, S);
			} else {

				HashSet<Integer> S = AdjList.get(u);
				S.add(v);
				AdjList.put(u, S);
			}

			if (AdjList.get(v) == null) {

				HashSet<Integer> S = new HashSet<>();
				S.add(u);
				AdjList.put(v, S);
			} else {

				HashSet<Integer> S = AdjList.get(v);
				S.add(u);
				AdjList.put(v, S);
			}

			// update the triangles count
/*			
			HashSet<Integer> commonNeighborhood = SetOperations.intersect(AdjList.get(u), AdjList.get(v));

			for (int w : commonNeighborhood) {

				int tcount_u = 0;
				int tcount_v = 0;
				int tcount_w = 0;

				tcount_u = trianglecount.get(u);
				tcount_v = trianglecount.get(v);
				tcount_w = trianglecount.get(w);

				tcount_u++;
				tcount_v++;
				tcount_w++;

				trianglecount.put(u, tcount_u);
				trianglecount.put(v, tcount_v);
				trianglecount.put(w, tcount_w);
			}
*/
			// AdjList.get(u).add(v);
			// AdjList.get(v).add(u);
		}

	}

	public boolean containsEdge(int u, int v) {
		if (AdjList.get(u) != null) {
			if (AdjList.get(u).contains(v))
				return true;
			else
				return false;
		}
		return false;
	}

	public void removeEdge(int u, int v) {

		if (AdjList.get(u) != null) {
			if (AdjList.get(u).contains(v)) {
				AdjList.get(u).remove(v);
				AdjList.get(v).remove(u);
			}
		}

	}

	public void print() {
		for (int v : AdjList.keySet()) {
			System.out.print(v + ":");
			for (int u : AdjList.get(v)) {
				System.out.print(u + ";");
			}
			System.out.println();
		}
	}

	public void print(FileWriter fw) {
		try {
			for (int v : AdjList.keySet()) {
				fw.write(v + " ");
				for (int u : AdjList.get(v)) {
					fw.write(u + " ");
				}
				fw.write("\n");
			}
		} catch (IOException e) {
		}
	}

	public int MaxDegree() {
		int max = Integer.MIN_VALUE;
		for (int u : AdjList.keySet()) {
			int size = AdjList.get(u).size();
			if (max < size) {
				max = size;
			}
		}
		return max;
	}

	public int MinDegree() {
		int min = Integer.MAX_VALUE;
		for (int u : AdjList.keySet()) {
			int size = AdjList.get(u).size();
			if (min > size) {
				min = size;
			}
		}
		return min;
	}

	public int AvgDegree() {
		int sum = 0;
		for (int u : AdjList.keySet()) {
			sum += AdjList.get(u).size();
		}

		return (sum / (2 * AdjList.size()));
	}

	public int numE() {
		int size = 0;
		for (int u : AdjList.keySet()) {
			size += AdjList.get(u).size();
		}
		return size / 2;
	}
	
	public int numV() {
		return AdjList.keySet().size();
	}

	public double density(){
		
		int edges = this.numE();
		int vertices = this.numV();

		return (double)edges/((double)vertices*(vertices-1));
	
	}

	public int getSize() {
		int size = 0;
		for (int u : AdjList.keySet()) {
			size++;
			size += AdjList.get(u).size();
		}
		return size;
	}
	
	public Vscore[] getDegenOrdering() {
		return S;
	}

	public int degreeOf(int vertex) {
		return this.AdjList.get(vertex).size();
	}

	public int degeneracyOf(int vertex) {
		return degenscores.get(vertex);
	}

	public int tcountAt(int vertex) {
		return trianglecount.get(vertex);
	}

	public Collection<Integer> neighborsOf(int u) {
		return this.AdjList.get(u);
	}

	public void clear() {
		AdjList.clear();
	}

	// Adopted this code from
	// https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/scoring/Coreness.java
	public void computeDegeneracy() {

		if (degenscores != null) {
			return;
		}

		degenscores = new HashMap<>();
		
		S = new Vscore[AdjList.size()];
		
		int vcount = 0;

		degeneracy = 0;

		/* initialize buckets */
		int n = AdjList.size();
		int maxDegree = n - 1;
		Set<Integer>[] buckets = (Set<Integer>[]) Array.newInstance(Set.class, maxDegree + 1);

		for (int i = 0; i < buckets.length; i++) {
			buckets[i] = new HashSet<>();
		}

		int minDegree = n;
		Map<Integer, Integer> degrees = new HashMap<>();

		for (int v : AdjList.keySet()) {
			int d = degreeOf(v);
			buckets[d].add(v);
			degrees.put(v, d);
			minDegree = Math.min(minDegree, d);
		}

		/* Extract from bucket */
		while (minDegree < n) {
			Set<Integer> b = buckets[minDegree];
			if (b.isEmpty()) {
				minDegree++;
				continue;
			}

			int v = b.iterator().next();
			b.remove(v);
			
			degenscores.put(v, minDegree);
			S[vcount] = new Vscore(v, minDegree);
			vcount++;

			degeneracy = Math.max(degeneracy, minDegree);

			for (int u : AdjList.get(v)) {
				int uDegree = degrees.get(u);
				if (uDegree > minDegree && !degenscores.containsKey(u)) {
					buckets[uDegree].remove(u);
					uDegree--;
					degrees.put(u, uDegree);
					buckets[uDegree].add(u);
					minDegree = Math.min(minDegree, uDegree);
				}
			}
		}
		
		Arrays.parallelSort(S);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Graph g = new Graph("C:/Users/apurd/Documents/data/sx_stackoverflow_a2q_edges");

		int maxdegree = g.MaxDegree();

		System.out.println("max degree of sx_stackoverflow: " + maxdegree);

		// g = new Graph("/home/apurba/data/ER-2M-15M-edges");

		// maxdegree = g.MaxDegree();

		// System.out.println("max degree of 2M-15M: " + maxdegree);

		// g.removeEdge(1,2);
		// g.removeEdge(2,3);
		// System.out.println("After removal of (1,2) and (2,3)");
		// g.print();

		// new Tomita(g);

	}

	public void printedges(FileWriter fw) throws Exception {
		String[] edges = new String[numE()];

		int count_edges = 0;
		for (int v : AdjList.keySet()) {
			for (int u : AdjList.get(v)) {
				if (u < v) {
					edges[count_edges] = u + " " + v;
					count_edges++;
				}
			}
		}

		System.out.println(count_edges);

		// randomly shuffling the edges
		for (int i = 0; i < edges.length; i++) {
			int r = i + (int) (Math.random() * (edges.length - i));
			String temp = edges[r];
			edges[r] = edges[i];
			edges[i] = temp;
		}

		for (int i = 0; i < edges.length; i++) {
			fw.write(edges[i] + "\n");
		}
		fw.close();

	}

	public void printVertices(FileWriter fw) throws Exception {
		for (int i : this.AdjList.keySet()) {
			fw.write(i + "\n");
		}

	}

}
