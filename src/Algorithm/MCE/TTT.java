package Algorithm.MCE;
/**This Algorithm generates all maximal cliques of an undirected graph G
 * based on Tomita et al. with title "The worst-case time complexity for generating all maximal cliques and computational experiments"
 * */

import java.util.*;

import Algorithm.Graph;
import Algorithm.MurmurHash3;
import utils.SetOperations;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TTT {

	private Set<Integer> CAND;
	private Set<Integer> FINI;
	private TreeSet<Integer> K;
	public HashSet<BitSet> CLQ;
	public int clqcnt;
	private FileWriter cw;

	private static Graph G;
	
	static long loopoverhead;

	static long count;

	static long cumulative_pivot_computation_time;

	private static HashMap<Integer, HashSet<Integer>> MapVE;
	
	private static HashSet<TreeSet<Integer>> cliques;
	
	private static FileWriter cliquewriter;

	public TTT(Graph g) throws IOException {

		this.G = g;

		count = 0;
		
		loopoverhead = 0;

		cumulative_pivot_computation_time = 0;

		CAND = new HashSet<Integer>();
		FINI = new HashSet<Integer>();
		K = new TreeSet<Integer>();
		CLQ = new HashSet<BitSet>();
		cliques = new HashSet<TreeSet<Integer>>();
		clqcnt = 1;
		for (int v : G.AdjList.keySet()) {
			CAND.add(v);
		}
		expand(K, CAND, FINI);
	}


	public void expand(TreeSet<Integer> K, Set<Integer> CAND, Set<Integer> FINI) throws IOException{
		if (CAND.isEmpty() && FINI.isEmpty()) {
			// System.out.println(count + ":" + K);
			StringBuilder sb = new StringBuilder();
			
			//for(int v : K)
			//	sb.append(v + " ");
			
			//cliquewriter.write(sb.toString() + "\n");
			
			count++;
			//System.out.println(K);
			//TreeSet<Integer> clique = new TreeSet<>(K);
			//cliques.add(clique);
			//System.out.println(cliques);
			if(count%1000000 == 0)
				System.out.println(count);
			return;
		}
		if(CAND.isEmpty() && !FINI.isEmpty())
			return;

		long start = System.currentTimeMillis();		

		int u = find_u(CAND, FINI);

		cumulative_pivot_computation_time += (System.currentTimeMillis() - start);

		Collection<Integer> NghOfu = Ngh(G, u);

		Iterator<Integer> candit = CAND.iterator();

		while (candit.hasNext()) {

			int q = candit.next();
			if (!NghOfu.contains(q)) {

				K.add(q);
				
				start = System.currentTimeMillis();

				Collection<Integer> NghOfq = Ngh(G, q);

				HashSet<Integer> CANDq = SetOperations.intersect(CAND, NghOfq);

				HashSet<Integer> FINIq = SetOperations.intersect(FINI, NghOfq);
				
				loopoverhead += (System.currentTimeMillis() - start);

				expand(K, CANDq, FINIq);

				candit.remove(); // CAND - {q}
				
				K.remove(q);

				FINI.add(q); // FINI union {q}
			}
		}
	}

	/*
	 * public Set<Integer> Ngh(int u) { return G.AdjList.get(u); }
	 */

	public Collection<Integer> Ngh(Graph G, int u) {
		return G.AdjList.get(u);
	}
	
	public Collection<TreeSet<Integer>> collect(){
		//for(TreeSet<Integer> c : cliques)
		//	System.out.println(c);
		return cliques;
	}

	public int find_u(Set<Integer> CAND, Set<Integer> FINI /*
															 * Set<String> SUBG
															 */) {

		int size = -1;
		int v = 0;

		for (int u : CAND) {
			HashSet<Integer> Q = SetOperations.intersect(Ngh(G,u), CAND);
			int tmp = Q.size();
			if (size <= tmp) {
				size = tmp;
				v = u;
			}
		}

		for (int u : FINI) {
			HashSet<Integer> Q = SetOperations.intersect(Ngh(G,u), CAND);
			int tmp = Q.size();
			if (size <= tmp) {
				size = tmp;
				v = u;
			}
		}
		return v;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		System.out.println(args[0]);

		System.out.println("Sequential TTT Algorithm");
		System.out.println("Input Graph: " + args[0]);
		
			
		//Graph G = new Graph(args[0], 1);	//reading adjacency list
		Graph G = new Graph(args[0]);	//edge list
		
		System.out.println("Graph Reading Complete");
		
		System.out.println("Number of Vertices: " + G.numV());
		System.out.println("Number of Edges: " + G.numE());

		try {
			long t1 = System.currentTimeMillis();
			
			//cliquewriter = new FileWriter(args[0] + "_cliques");
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println(dateFormat.format(date));
			
			new TTT(G);
			
			//cliquewriter.close();
			long elapsed = System.currentTimeMillis() - t1;
			System.out.println("number of maximal cliques: " + count);
			System.out.println("Sequential time taken to compute maximal cliques in" + args[0] + ": " + elapsed/1000 + " sec.");
			System.out.println("Total loop overhead : " + loopoverhead/1000 + " sec.");
			System.out.println("Total pivot computation time : " + cumulative_pivot_computation_time/1000 + " sec.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// new Tomita(G);
	}

}
