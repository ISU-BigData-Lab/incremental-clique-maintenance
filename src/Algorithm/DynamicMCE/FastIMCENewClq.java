package Algorithm.DynamicMCE;

//use containment check for computing subsumed cliques
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import Algorithm.Graph;
import Algorithm.MurmurHash3;
import utils.SetOperations;

public class FastIMCENewClq {

	/* Global variables */
	/*
	 * =========================================================================
	 * =================================================
	 */
	// private static Set<String> CliqueSet; //stores the set of all maximal
	// cliques of the input graph
	private static Set<BitSet> CliqueSet; // stores the set of all maximal
											// cliques of the input graph
	private static Set<BitSet> Cdel; // This is to store all the cliques which
										// are subsumed by new maximal cliques
	// private static Set<String> Cdel; // This is to store all the cliques
	// which are subsumed by new maximal cliques

	private static long recompute_time, sub_time; // for computing time taken
													// for computing new and
													// subsumed cliques
	
	private static long other_time;	//time taken for task such as computing subgraph, updating graph etc

	private static Graph G; // The original graph
	private static Graph H; // The edge set (represented in the form of
							// adjacency list)

	private static long ncliqueCount; // number of new cliques
	private static long ncliqueSizes; // total size of the new cliques
	private static long newCliqueSizeInNodes;	//size of a clique as the number of nodes
	private static long subCliqueSizeInNodes;	//size of a clique as the number of edges
	private static long subCliqueCount;
	private static long spacecost, spacecost2, spacecost3; // for
															// computing
															// total
															// space
															// cost
															// of
															// the
															// algorithm

	private static long bitsetsize;
	private static long subSize;

	private static Set<int[]> DeletedEdges;
	
	private static int maxDegree;
	private static int largeCliqueCount;

	private static int duplicate_subsumed_cliques;

	/*
	 * =========================================================================
	 * =================================================
	 */

	/*
	 * Input G: original graph e[]: set of edges to be added num: size of the
	 * array e[]
	 */
	// public Maintain_MaxClique_Algorithm(Graph G, Edge e[], int num) throws
	// IOException{
	public FastIMCENewClq(Set<int[]> batch) throws IOException {

		/* Initializations */
		/* ============================================================= */
		Cdel = new HashSet<BitSet>();
		H = new Graph();

		// ArrayList<Pair> P = new ArrayList<Pair>();
		HashMap<Integer, HashSet<Integer>> Ei = new HashMap<Integer, HashSet<Integer>>();

		ncliqueCount = 0;
		ncliqueSizes = 0;
		newCliqueSizeInNodes = 0;
		subCliqueSizeInNodes = 0;
		int ecount = 0;
		spacecost2 = spacecost3 = 0;
		sub_time = 0;
		subCliqueCount = 0;
		other_time = 0;
		duplicate_subsumed_cliques = 0;
		long startTime = System.currentTimeMillis();

		/* Updating the graph by adding new edges */
		/* ============================================================= */

		long temp_stime = System.currentTimeMillis();
		
		for(int[] e : batch) {
			int u = e[0];
			int v = e[1];
			G.addEdge(u, v);
			int degree_of_u = G.degreeOf(u);
			int degree_of_v = G.degreeOf(v);
			
			if(degree_of_u > maxDegree)
				maxDegree = degree_of_u;
			
			if(degree_of_v > maxDegree)
				maxDegree = degree_of_v;
			
			spacecost += 2;
			H.addEdge(u, v);
		}
		
		other_time += (System.currentTimeMillis() - temp_stime);
		/* ============================================================= */
		
		for(int[] e : batch) {
			
			temp_stime = System.currentTimeMillis();

			Graph IndG = new Graph();

			int u = e[0];
			int v = e[1];

			HashSet<Integer> NuIntersectNv = new HashSet<Integer>();

			NuIntersectNv = SetOperations.intersect(G.AdjList.get(u), G.AdjList.get(v));
			NuIntersectNv.add(u);
			NuIntersectNv.add(v);
			IndG = CreateInducedSubgraph(ecount, NuIntersectNv);
			
			other_time += (System.currentTimeMillis() - temp_stime);

			tomita(IndG, u, v, Ei);

			// ading edge (u,v)
			if (Ei.get(u) == null) {
				HashSet<Integer> L = new HashSet<Integer>();
				L.add(v);
				Ei.put(u, L);
				spacecost3 += 2;
			} else {
				HashSet<Integer> L = Ei.get(u);
				L.add(v);
				Ei.put(u, L);
				spacecost3 += 1;
			}

			// adding edge (v,u)
			if (Ei.get(v) == null) {
				HashSet<Integer> L = new HashSet<Integer>();
				L.add(u);
				Ei.put(v, L);
				spacecost3 += 2;
			} else {
				HashSet<Integer> L = Ei.get(v);
				L.add(u);
				Ei.put(v, L);
				spacecost3 += 1;
			}

			ecount += 1;
		}
		// g_size += g_edges;
		/*
		 * =====================================================================
		 * =============================
		 */

		recompute_time = System.currentTimeMillis() - startTime; // end of
																	// computing
																	// new
																	// cliques
																	// after
																	// adding
																	// a set of
																	// edges

		for (int u : H.AdjList.keySet()) {
			spacecost2++;
			spacecost2 += H.AdjList.get(u).size();
		}

	}

	/**
	 * This procedure creates an induced subgraph with vertex set V from the
	 * input graph G.
	 */
	public static Graph CreateInducedSubgraph_minusH(Set<Integer> V) throws IOException {

		Graph C = new Graph();

		int[] e = new int[2];

		for (int u : V) {
			for (int v : V) {
				// if(!H.containsEdge(u, v) && (u != v))
				// C.addEdge(u, v);
				if (u < v) {
					// g_edges++;
					C.addEdge(u, v);
					if (H.AdjList.get(u) != null) {
						if (H.AdjList.get(u).contains(v)) {
							// g_edges--;
							C.removeEdge(u, v);
							//e[0] = u;
							//e[1] = v;
							//DeletedEdges.add(e);

						}
					}
				}
			}
		}

		return C;

	}

	/**
	 * This procedure cretes an induced subgraph with vertex set V from the
	 * input graph G.
	 */
	public static Graph CreateInducedSubgraph(int eindex, Set<Integer> V) throws IOException {

		// System.out.println("Inside CreateInducedSubgraph");
		// G.print();

		Graph H = new Graph();

		for (int u : V) {
			// Set<Integer> N = G.AdjList.get(u);
			for (int v : V) {
				if (G.containsEdge(u, v)) {
					H.addEdge(u, v);

				}
			}
		}

		return H;

	}

	public HashSet<Integer> Ngh(Graph G, int u) {
		if (G.AdjList.get(u) == null)
			System.out.println("No neighbors");
		return G.AdjList.get(u);
	}

	public int find_u(Graph G, Set<Integer> CAND,
			Set<Integer> FINI /*
								 * Set< String> SUBG
								 */) {

		int size = -1;
		int v = 0;

		for (int u : CAND) {
			HashSet<Integer> Q = new HashSet<Integer>();
			Q = SetOperations.intersect(CAND, Ngh(G, u));
			int tmp = Q.size();
			if (size <= tmp) {
				size = tmp;
				v = u;
			}
		}

		for (int u : FINI) {
			HashSet<Integer> Q = new HashSet<Integer>();
			Q = SetOperations.intersect(CAND, Ngh(G, u));
			int tmp = Q.size();
			if (size <= tmp) {
				size = tmp;
				v = u;
			}
		}

		return v;

	}

	/* input : Graph G and edge (u,v) */
	public void tomita(Graph G, int u, int v, HashMap<Integer, HashSet<Integer>> Ei) throws IOException {

		HashSet<Integer> CAND = new HashSet<Integer>();
		HashSet<Integer> FINI = new HashSet<Integer>();
		TreeSet<Integer> K = new TreeSet<Integer>();
		CAND.addAll(G.AdjList.keySet());
		// System.out.println("Generating all Maximal Cliques");
		/* 1: *//* expand(SUBG, CAND); */

		K.add(u);
		K.add(v);
		CAND.remove(u);
		CAND.remove(v);

		expand(G, K, CAND, FINI, Ei);
	}

	public void expand(Graph G, TreeSet<Integer> K, Set<Integer> CAND, Set<Integer> FINI,
			HashMap<Integer, HashSet<Integer>> Ei) throws IOException {
		if (CAND.isEmpty() && FINI.isEmpty()) { // a new maxial clique found

			//System.out.println("new " + K);
			byte[] key = K.toString().getBytes();
			// long[] hash = MurmurHash3.MurmurHash3_x64_128(key, 0);
			long[] hash = { MurmurHash3.MurmurHash3_x64_64(key, 0), 0 };

			BitSet b = BitSet.valueOf(hash);
			
			/*if(K.size() >= 20){
				largeCliqueCount++;
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
				System.out.println(largeCliqueCount);
				System.out.println("new maximal clique found of size: " + K.size());
				System.out.println(K);
			}*/

			CliqueSet.add(b); // for incremental comoutation we need to maintain
								// the cliqueset
			bitsetsize += b.size();
			ncliqueCount++;
			int s = K.size();
			ncliqueSizes += s * (s - 1) / 2;
			newCliqueSizeInNodes += s;
			return;
		} else if (CAND.isEmpty())
			return;
		int u = find_u(G, CAND, FINI);

		HashSet<Integer> NghOfu = Ngh(G, u);

		Iterator<Integer> candit = CAND.iterator();

		while (candit.hasNext()) {

			int q = candit.next();
			if (!NghOfu.contains(q)) {
				K.add(q);

				// Line 9-13 of TomitaE : START
				boolean tflag = false;
				if (Ei.keySet().size() > 0 && (Ei.get(q) != null)) {
					// HashSet<Integer> S = Ei.get(q);
					for (int v : K) {
						if (Ei.get(q).contains(v)) {
							tflag = true;
							break;
						}
					}
				}

				if (tflag) { // Kq intersect Ei is not null
					candit.remove(); // CAND - {q}
					K.remove(q);
					FINI.add(q);
					continue;
				}
				// Line 9-13 of TomitaE : END

				HashSet<Integer> NghOfq = Ngh(G, q);
				HashSet<Integer> CANDq = new HashSet<Integer>();
				HashSet<Integer> FINIq = new HashSet<Integer>();

				CANDq = SetOperations.intersect(CAND, NghOfq);
				FINIq = SetOperations.intersect(FINI, NghOfq);

				expand(G, K, CANDq, FINIq, Ei);

				candit.remove(); // CAND - {q}

				K.remove(q);

				FINI.add(q); // FINI union {q}
			}

		}
	}

	public static void print(Set<String> S) {
		// System.out.println("All new cliques: ");
		for (String s : S) {
			// System.out.println("Clique" + " " + i + " : ");
			System.out.println(s);
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		if (args.length < 4) {

			System.out.println("usage : java program graph clique_set edge_set batch_size out_file");
			System.out.println("graph: represented as set of edges in a txt file");
			System.out.println("clique_set: set of all maximal cliques of graph in a text file");
			System.out.println("edge_set: edges to be added: text file");
			System.out.println("batch_size: number of edges in a batch");
			System.out.println("out_file: the file to store the output data");

			System.exit(0);
		}

		String graph = args[0];
		String edge_set = args[1];
		int batch_size = Integer.parseInt(args[2]); // batch count in
													// incremental computation
		String out_file = args[3];

		spacecost = 0;
		
		maxDegree = 0;

		G = new Graph(graph, 1); // input file is in adjacency list format
		// G = new Graph(graph); //input file is list of edges
		System.out.println("Graph reading done");

		spacecost += G.getSize();;

		String line;

		bitsetsize = 0;

		CliqueSet = new HashSet<BitSet>();
		// System.out.println("Reading cliques done");
		// System.out.println("size of the cliqueset : " + CliqueSet.size());
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
		out.println();
		out.println("FastIMCENewClq");

		out.println("#iteration,batch_size,#new,new_time");
		out.close();
		int mb = 1024 * 1024;

		BufferedReader br1 = new BufferedReader(new FileReader(edge_set));
		int count = 0;
		boolean eof_flag = false;
		int index = 0;
		
		long cumulative_time = 0L;
		long new_cumulative_time = 0L;
		long sub_cumulative_time = 0L;
		
		largeCliqueCount = 0;
		
		int total_batch_size = 0;

		while (true) {

			index = 0;
			count++;
			
			Set<int[]> batch = new HashSet<>();
			
			//we consider batch size to be the 3*log_2(maxDegree)
			
			//batch_size = 3 * MathOperations.log2(maxDegree);
			
			//if(batch_size == 0)
			//	batch_size += 1;
			
			//for now set batch size 1
			//int batch_size = 1;
			
			//total_batch_size += batch_size;

			while (index < batch_size) {
				if ((line = br1.readLine()) != null) {
					batch.add(new int[] {Integer.parseInt(line.split("\\s+")[0]), Integer.parseInt(line.split("\\s+")[1])});
					index++;
				} else {
					eof_flag = true;
					break;
				}
			}

			out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));

			subSize = 0;
			new FastIMCENewClq(batch);


			long total_number = ncliqueCount + subCliqueCount;
			long total_size = ncliqueSizes + subSize;
			long total_size_in_nodes = newCliqueSizeInNodes + subCliqueSizeInNodes;

			if(count%10000 == 0)
				System.out.println(count + ": batch-size: " + batch_size + " new: " + ncliqueCount + " sub: " + subCliqueCount + " time: " + recompute_time + "other_time: " + other_time + " duplicate subsumed cliques: " + duplicate_subsumed_cliques);

			long sc = ((spacecost + spacecost2 + spacecost3) * 4 / mb) + (bitsetsize / (8 * mb))
					+ (Cdel.size() * 8 / mb);

			long for_storing_cliques = (bitsetsize / (8 * mb));

			long for_cdel = (Cdel.size() * 8 / mb);

			// spacecost = (instance.totalMemory() - instance.freeMemory())/mb;

			long new_time = recompute_time - sub_time;
			
			cumulative_time += recompute_time;
			
			new_cumulative_time += new_time;
			sub_cumulative_time += sub_time;
			
			out.println(count + "," + batch_size + "," + ncliqueCount + "," + new_time);

			//out.println(count + "," + batch_size + "," + ncliqueCount + "," + subCliqueCount + "," + total_number + "," + ncliqueSizes + "," + subSize + "," + total_size + "," + newCliqueSizeInNodes + "," + subCliqueSizeInNodes + ","
			//		+ total_size_in_nodes + "," + new_time + "," + sub_time + "," + recompute_time + "," + other_time + "," + cumulative_time  + "," + sc + ","
			//		+ for_storing_cliques + "," + for_cdel + "," + CliqueSet.size() + "," + duplicate_subsumed_cliques);
			out.close();
			
			//if((total_batch_size > 17000000) || eof_flag){
			//	System.out.println("number of edges inserted: " + total_batch_size);
			//	System.out.println("total cumulative time: " + cumulative_time);
			//	System.out.println("new cumulative time: " + new_cumulative_time);
			//	System.out.println("sub cumulative time: " + sub_cumulative_time);
			//	break;
			//}

			if (eof_flag)
				break;
			

			// if (20 == count)
			// break;
		}
		br1.close();

	}
}
