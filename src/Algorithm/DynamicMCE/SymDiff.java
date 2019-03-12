package Algorithm.DynamicMCE;

//use containment check for computing subsumed cliques
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import Algorithm.Graph;
import Algorithm.MurmurHash3;
import utils.SetOperations;
import utils.MathOperations;

public class SymDiff {

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

	private static int redundant_subsumed_cliques;
	private static long redundant_subsumed_cliques_size;
	
	private static double size_ratio_subsumed_cliques;
	private static long candidate_enumeration_cost;
	private static long subsumed_decision_cost;

	private static long candidate_subsumed_cliques;

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
	public SymDiff(Set<int[]> batch) throws IOException {

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
		redundant_subsumed_cliques = 0;
		redundant_subsumed_cliques_size = 0;
		size_ratio_subsumed_cliques = 0.0;
		candidate_subsumed_cliques = 0;

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

	/**
	 * test doc
	 * @param K tree set of vertex ids
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void computeSubsumedCliques(TreeSet<Integer> K) throws IOException {
		// DeletedEdges = new HashSet<>();

		// Graph IndG = CreateInducedSubgraph_minusH(K);

		// g_density += IndG.numEdges()/IndG.AdjList.keySet().size();
		// g_count++;
		// g_size += K.size();

		HashSet<TreeSet<Integer>>[] S = new HashSet[(int)Math.pow((double)K.size(), 2.0)];

		// Iterator<int[]> itr = DeletedEdges.iterator();

		// int[] e;
		
		S[0] = new HashSet<TreeSet<Integer>>();

		S[0].add(K);
		
		int idx = 0;

			long start = System.currentTimeMillis();
			for (int u : K) {
				for (int v : K) {
					if ((u < v) && (H.containsEdge(u, v))) {
						candidate_subsumed_cliques++;
						//HashSet<TreeSet<Integer>> A = new HashSet<>();
						
						//System.out.println(idx);
						Iterator<TreeSet<Integer>> itr = S[idx].iterator();
						idx = 1 - idx;
						S[idx] = new HashSet<>();
						while(itr.hasNext()) {
							TreeSet<Integer> T = itr.next();
							if (T.contains(u) && T.contains(v)) {
								TreeSet<Integer> X = new TreeSet<>(T);
								TreeSet<Integer> Y = new TreeSet<>(T);
								X.remove(u);
								Y.remove(v);
								S[idx].add(X);
								S[idx].add(Y);
							}
							else
								S[idx].add(T);
						}
						//S[idx-1].clear();
						//S.addAll(A);
					}
				}
			}
			
			candidate_enumeration_cost += (System.currentTimeMillis() - start);

		//candidate_subsumed_cliques += S[idx].size();		

		start = System.currentTimeMillis();	
		for (TreeSet<Integer> KK : S[idx]) {
				

			if (isContained(KK)) {
				
				byte[] key = KK.toString().getBytes();
				// long[] hash = MurmurHash3.MurmurHash3_x64_128(key, 0);
				long[] hash = { MurmurHash3.MurmurHash3_x64_64(key, 0), 0 };
				BitSet b = BitSet.valueOf(hash);

				if (!Cdel.contains(b)) {
					int n = KK.size();
					subSize += (n * (n - 1) / 2);
					Cdel.add(b); // there may be duplicates, so store them in
									// seperate set
					subCliqueSizeInNodes += n;
					subCliqueCount++;
					size_ratio_subsumed_cliques += (double)K.size()/(double)KK.size();
					CliqueSet.remove(b); // only during incremental computation
					bitsetsize -= b.size();
				}
			}
			else {
				redundant_subsumed_cliques ++;
				long size = KK.size();
				redundant_subsumed_cliques_size += size*(size-1)/2;
			}
		}
		subsumed_decision_cost += (System.currentTimeMillis() - start);
		
		S[idx].clear();
		//CliqueSet.removeAll(Cdel);
		/*
		 * HashSet<Integer> CAND = new HashSet<Integer>(); HashSet<Integer> FINI
		 * = new HashSet<Integer>(); TreeSet<Integer> K1 = new
		 * TreeSet<Integer>(); for (int w : IndG.AdjList.keySet()) {
		 * CAND.add(w); }
		 * 
		 * // System.out.println("Computing subsumed cliques from new maximal //
		 * clique: " + K);
		 * 
		 * expand_for_subsumed(IndG, K1, CAND, FINI); // expand for computing //
		 * subsumed cliques only
		 */
	}

	public boolean isContained(TreeSet<Integer> K) {

		if (K.size() == 0)
			return false;

		byte[] key = K.toString().getBytes();
		// long[] hash = MurmurHash3.MurmurHash3_x64_128(key, 0);
		long[] hash = { MurmurHash3.MurmurHash3_x64_64(key, 0), 0 };

		BitSet b = BitSet.valueOf(hash);

		return CliqueSet.contains(b);

	}

	/*
	public void expand_for_subsumed(Graph G, TreeSet<Integer> K, Set<Integer> CAND, Set<Integer> FINI)
			throws IOException {
		if (CAND.isEmpty() && FINI.isEmpty()) { // a new maxial clique found
			if (isContained(K)) {

				byte[] key = K.toString().getBytes();
				// long[] hash = MurmurHash3.MurmurHash3_x64_128(key, 0);
				long[] hash = { MurmurHash3.MurmurHash3_x64_64(key, 0), 0 };
				BitSet b = BitSet.valueOf(hash);
				if (!Cdel.contains(b)) {
					int n = K.size();
					subSize += (n * (n - 1) / 2);
					Cdel.add(b); // there may be duplicates, so store them in
									// seperate set
					CliqueSet.remove(b); // only during incremental computation
					bitsetsize -= b.size();
				}

			}
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

				HashSet<Integer> NghOfq = Ngh(G, q);
				HashSet<Integer> CANDq = new HashSet<Integer>();
				HashSet<Integer> FINIq = new HashSet<Integer>();

				CANDq = SetOperations.intersect(CAND, NghOfq);
				FINIq = SetOperations.intersect(FINI, NghOfq);

				expand_for_subsumed(G, K, CANDq, FINIq);

				candit.remove(); // CAND - {q}

				K.remove(q);

				FINI.add(q); // FINI union {q}
			}
		}
	}*/

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
			//newCliqueSizeInNode += s;
			long time = System.currentTimeMillis();
			computeSubsumedCliques(K);
			sub_time += System.currentTimeMillis() - time;
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
		String clique_set = args[1];
		String edge_set = args[2];
		int batch_size = Integer.parseInt(args[3]); // batch count in
													// incremental computation
		//int edge_count = Integer.parseInt(args[4]);
		String out_file = args[4];

		spacecost = 0;
		
		maxDegree = 0;

		G = new Graph(graph, 1); // input file is in adjacency list format
		// G = new Graph(graph); //input file is list of edges
		System.out.println(graph + " Graph reading done");

		spacecost += G.getSize();

		BufferedReader br = new BufferedReader(new FileReader(clique_set));

		String line;

		bitsetsize = 0;

		CliqueSet = new HashSet<BitSet>();

		// adding the existing cliqueset to a container. Each clique is stored
		// as a string of vertex ids sorted
		while ((line = br.readLine()) != null) {

			TreeSet<Integer> T = new TreeSet<Integer>();
			for (String s : line.split("\\s+")) {
				T.add(Integer.parseInt(s));
			}
			// for (int v : T)
			// {
			// sb.append(v + " ");
			// }
			/* Computing signature of the clique */
			// byte[] key = sb.toString().getBytes();
			byte[] key = T.toString().getBytes();
			// long[] hash = MurmurHash3.MurmurHash3_x64_128(key, 0);
			long[] hash = { MurmurHash3.MurmurHash3_x64_64(key, 0), 0 };
			// String signature = String.valueOf(hash[0]) +
			// String.valueOf(hash[1]);
			BitSet b = BitSet.valueOf(hash);

			bitsetsize += b.size();
			CliqueSet.add(b);
			// CliqueSet.add(sb.toString().trim());

		}

		System.out.println("cliques reading complete!!");
		br.close();
		// System.out.println("Reading cliques done");
		// System.out.println("size of the cliqueset : " + CliqueSet.size());
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
		out.println();
		out.println("SymDiff-2");

		out.println("#iteration,batch_size,#new,#sub,total_num,size_new,size_sub,total_size-edges,size-new-nodes,size-new-edges,total-size-nodes,new_time,sub_time,total_time,other_time,space in mb, # of max cliques");
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
		
		int total_edge_size = 0;
		
		int cumulative_redundant_subsumed_cliques = 0;
		long cumulative_redundant_subsumed_cliques_size = 0;
		long total_size_edges = 0;
		double cumulative_size_ratio_subsumed_cliques = 0;
		
		candidate_enumeration_cost = 0;
		subsumed_decision_cost = 0;
		
		//System.out.println("Targeted number of edges - " + edge_count);

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
			
			total_edge_size += batch_size;

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
			new SymDiff(batch);


			long total_number = ncliqueCount + subCliqueCount;
			long total_size = ncliqueSizes + subSize;
			long total_size_in_nodes = newCliqueSizeInNodes + subCliqueSizeInNodes;
			
			cumulative_redundant_subsumed_cliques += redundant_subsumed_cliques;
			cumulative_redundant_subsumed_cliques_size += redundant_subsumed_cliques_size;
			
			cumulative_size_ratio_subsumed_cliques += size_ratio_subsumed_cliques;

			//if(count%1000000 == 0)
				System.out.println(count + ": batch-size: " + batch_size + " new: " + ncliqueCount + " sub: " + subCliqueCount + " time: " + recompute_time + "other_time: " + other_time + " redundant subsumed cliques: " + redundant_subsumed_cliques);

			System.out.println("Total candidate subsumed cliques: " + candidate_subsumed_cliques);

			long sc = ((spacecost + spacecost2 + spacecost3) * 4 / mb) + (bitsetsize / (8 * mb))
					+ (Cdel.size() * 8 / mb);

			long for_storing_cliques = (bitsetsize / (8 * mb));

			long for_cdel = (Cdel.size() * 8 / mb);

			// spacecost = (instance.totalMemory() - instance.freeMemory())/mb;

			long new_time = recompute_time - sub_time;
			
			cumulative_time += recompute_time;
			
			new_cumulative_time += new_time;
			sub_cumulative_time += sub_time;
			
			total_size_edges += total_size;

			out.println(count + "," + batch_size + "," + ncliqueCount + "," + subCliqueCount + "," + total_number + "," + ncliqueSizes + "," + subSize + "," + total_size + "," + newCliqueSizeInNodes + "," + subCliqueSizeInNodes + ","
					+ total_size_in_nodes + "," + new_time + "," + sub_time + "," + recompute_time + "," + other_time + "," + cumulative_time  + "," + sc + ","
					+ for_storing_cliques + "," + for_cdel + "," + CliqueSet.size() + "," + redundant_subsumed_cliques);
			out.close();
			
			//if((batch_count == count) || eof_flag){
			if(eof_flag || cumulative_time >= 7200000){
			//if((total_edge_size >= edge_count) || eof_flag){
				System.out.println("number of edges inserted: " + total_edge_size);
				System.out.println("cumulative redundant subsumed cliques: " + cumulative_redundant_subsumed_cliques);
				System.out.println("cumulative redundant subsumed cliques size in edge: " + cumulative_redundant_subsumed_cliques_size);
				System.out.println("Cumulative total size of change in edges: " + total_size_edges);
				System.out.println("Cumulative sum of ratio of new cliques size to the subsumed clique size: " + cumulative_size_ratio_subsumed_cliques);
				System.out.println("total cumulative time: " + cumulative_time);
				System.out.println("new cumulative time: " + new_cumulative_time);
				System.out.println("sub cumulative time: " + sub_cumulative_time);
				System.out.println("Cumulative candidate enumeration cost: " + candidate_enumeration_cost);
				System.out.println("Cumulative subsumed decision cost: " + subsumed_decision_cost);
				break;
			} 

			if (eof_flag)
				break;
			

			 if (2 == count)
				break;
		}
		br1.close();

	}
}
