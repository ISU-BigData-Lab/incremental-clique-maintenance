package Algorithm.MCE;


/**
 * ELS: Sequential Algorithm due to Eppstein, Loffler, and Strash
 * "Listing All Maximal Cliques in Sparse Graphs in Near-optimal Time"
 */

import java.util.*;

import Algorithm.Graph;
import Algorithm.MurmurHash3;
import utils.SetOperations;
import utils.Vscore;

import java.io.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ELS {


	private static Collection<BitSet> CLQ;

	private static ConcurrentMap<Long, Integer> timeToFrequency;
	public int clqcnt;
	private FileWriter cw;

	private static Graph G;

	static Long count;

	private static HashMap<Integer, HashSet<Integer>> MapVE;

	public ELS(Graph g, String ofname) throws IOException {

		this.G = g;

		count = 0L;

		CLQ = ConcurrentHashMap.newKeySet();

		timeToFrequency = new ConcurrentHashMap<>();
		
		Vscore[] S = G.getDegenOrdering();
		
		for(Vscore s : S) {
			
			int v = s.getVertex();
			
			Set<Integer> CAND = new HashSet<Integer>();
			Set<Integer> FINI = new HashSet<Integer>();
			TreeSet<Integer> K = new TreeSet<Integer>();
			
			K.add(v);
			for(int w : G.AdjList.get(v)){
				if (G.degeneracyOf(w) > G.degeneracyOf(v))
					CAND.add(w);
				else if(G.degeneracyOf(w) < G.degeneracyOf(v))
					FINI.add(w);
				else {
					if(w > v)
						CAND.add(w);
					else
						FINI.add(w);
				}
			}
			long t1 = System.currentTimeMillis();
			expand(K, CAND, FINI);
			long elapsed = System.currentTimeMillis() - t1;
			System.out.println(v + "\t" + elapsed + "\t" + G.AdjList.get(v).size());
		}
	}


	public void expand(TreeSet<Integer> K, Collection<Integer> CAND, Collection<Integer> FINI)
			throws IOException {
		if (CAND.isEmpty() && FINI.isEmpty()) {
			// System.out.println(count + ":" + K);
			count++;
			//count.incrementAndGet();
			if (count % 1000000000 == 0)
				System.out.println(count);
			return;
		}
		int u = find_u(CAND, FINI);

		Collection<Integer> NghOfu = Ngh(G, u);

		Iterator<Integer> candit = CAND.iterator();


		while (candit.hasNext()) {

			int q = candit.next();
			if (!NghOfu.contains(q)) {

				K.add(q);

				Collection<Integer> NghOfq = Ngh(G, q);

				HashSet<Integer> CANDq = SetOperations.intersect(CAND, NghOfq);
				//Collection<Integer> CANDq = SetOperations.parIntersect(CAND, NghOfq);

				HashSet<Integer> FINIq = SetOperations.intersect(FINI, NghOfq);
				//Collection<Integer> FINIq = SetOperations.parIntersect(FINI, NghOfq);

				expand(K, CANDq, FINIq);

				candit.remove(); // CAND - {q}
				
				K.remove(q);

				FINI.add(q); // FINI union {q}
			}
		}
	}



	public Collection<Integer> Ngh(Graph G, int u) {
		if (G.AdjList.get(u) == null)
			System.out.println("No neighbors");
		return G.AdjList.get(u);
	}

	public int find_u(Collection<Integer> CAND, Collection<Integer> FINI /*
															 * Set<String> SUBG
															 */) {


		int size = -1;
		int v = 0;
		for(int u : CAND){
			HashSet<Integer> Q = SetOperations.intersect(Ngh(G,u), CAND);
			if(size < Q.size()){
				size = Q.size();
				v = u;
			}
		}

		for(int u : FINI){
			HashSet<Integer> Q = SetOperations.intersect(Ngh(G,u), CAND);
			if(size < Q.size()){
				size = Q.size();
				v = u;
			}
		}
		return v; 
		

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		Graph G = new Graph(args[0]);
		
		System.out.println("ELS: Sequential Algorithm due to Eppstein, Loffler, and Strash");
		System.out.println("Input Graph: " + args[0]);
		
		System.out.println("Graph Reading Complete");
		//Graph G = new Graph("reduced_web_google_001_adj", 1);
		
		long time_start = System.currentTimeMillis();
		
		G.computeDegeneracy();
		
		long elapsed_time = System.currentTimeMillis() - time_start;
		
		System.out.println("Computing degeneracy takes time: " + elapsed_time/1000 + " sec.");

		try {
			long t1 = System.currentTimeMillis();
			new ELS(G, "output_TTT");
			long elapsed = System.currentTimeMillis() - t1;
			System.out.println("number of maximal cliques: " + count);
			System.out.println("Time taken to compute maximal cliques in" +  args[0] + ": " + elapsed/1000 + " sec.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// new Tomita(G);
	}

}
