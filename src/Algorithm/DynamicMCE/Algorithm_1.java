package Algorithm.DynamicMCE;

//Algorithm-1 in paper
import java.io.*;
import java.util.*;

import Algorithm.Graph;
import Algorithm.MurmurHash3;
import Algorithm.MCE.CN;
import utils.MathOperations;

import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import org.mapdb.*;

public class Algorithm_1 {

	/* Global variables */
	/*
	 * =========================================================================
	 * =================================================
	 */
	private static HashSet<String> CliqueSet; // stores the set of all maximal
												// cliques of the input graph
	// private static HashSet<TreeSet<Integer>> Cdel; // This is to store all
	// the cliques which are subsumed by new maximal cliques
	// private static Set<String> Cdel; // This is to store all the cliques
	// which are subsumed by new maximal cliques
	private static Set<BitSet> Cdel; // This is to store all the cliques which
										// are subsumed by new maximal cliques

	private static long recompute_time, newtime, subtime, nstime, nftime; // for
																			// computing
																			// time
																			// taken
																			// for
																			// computing
																			// new
																			// and
																			// subsumed
																			// cliques

	private static Graph G; // The original graph
	private static Graph H; // The edge set (represented in the form of
							// adjacency list)

	private static long ncliqueCount; // number of new cliques
	private static long ncliqueSizes; // total size of the new cliques
	private static long scliqueCount; // number of subsumed cliques
	private static long scliqueSizes; // total size of the subsumed cliques

	private static long spacecost, spacecost2, spacecost3; // for computing
															// total space cost
															// of the algorithm

	private static HashMap<Integer, TreeSet<Integer>> Ei;

	private static int maxDegree;
	private static int largeCliqueCount;
	private static int total_size_cumulative;

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
	// public NewAlgorithm_inc_4(String edge_set) throws IOException {
	public Algorithm_1(Set<int[]> batch) throws IOException {

		/* Initializations */
		/* ============================================================= */

		ncliqueCount = 0;
		ncliqueSizes = 0;
		scliqueCount = 0;
		scliqueSizes = 0;
		int ecount = 0;
		spacecost2 = 0;
		spacecost3 = 0;
		total_size_cumulative = 0;
		long startTime = System.currentTimeMillis();

		/* Updating the graph by adding new edges */
		/* ============================================================= */
		// File f = new File(edge_set);
		// Scanner scanner = new Scanner(f);

		// BufferedReader br = new BufferedReader(new FileReader(f));
		// String line = "";

		for (int[] e : batch) {
			int u = e[0];
			int v = e[1];
			H.addEdge(u, v);
			spacecost2 += 2;
			G.addEdge(u, v);
			spacecost += 2;

			int degree_of_u = G.degreeOf(u);
			int degree_of_v = G.degreeOf(v);

			if (degree_of_u > maxDegree)
				maxDegree = degree_of_u;

			if (degree_of_v > maxDegree)
				maxDegree = degree_of_v;
		}

		// br.close();
		int size_of_cand = 0;
		int size_of_fini = 0;

		/*
		 * =====================================================================
		 * =============================
		 */
		nstime = System.currentTimeMillis();
		/*
		 * scanner = new Scanner(f); while (scanner.hasNextInt()) { int u =
		 * scanner.nextInt(); int v = scanner.nextInt(); G.addEdge(u, v);
		 * spacecost += 2; // H.addEdge(u,v); //was here // spacecost += 2;
		 * //was here }
		 */
		/* ============================================================= */

		/* Computation of new maximal cliques */
		/*
		 * =====================================================================
		 * =============================
		 */
		size_of_cand = 0;

		// br = new BufferedReader(new FileReader(f));
		// scanner = new Scanner(f);

		for (int[] e : batch) {

			Graph IndG = new Graph();

			int u = e[0];
			int v = e[1];

			HashSet<Integer> Nu = new HashSet<Integer>(G.AdjList.get(u)); // Nu
																			// =
																			// Neighbours
																			// of
																			// u
			HashSet<Integer> Nv = new HashSet<Integer>(G.AdjList.get(v)); // Nv
																			// =
																			// Neighbours
																			// of
																			// v

			if (Nu.size() < Nv.size()) {
				Nu.retainAll(Nv);
				Nu.add(u);
				Nu.add(v);
				// IndG = CreateInducedSubgraph(Nu);
				IndG = CreateInducedSubgraph(Nu);
				size_of_cand += Nu.size();
			} else {
				Nv.retainAll(Nu);
				Nv.add(u);
				Nv.add(v);
				// IndG = CreateInducedSubgraph(Nv);
				IndG = CreateInducedSubgraph(Nv);
				size_of_cand += Nv.size();
			}

			CN cn = new CN(IndG);

			Collection<TreeSet<Integer>> maxcliques = cn.collect();
			
			total_size_cumulative += maxcliques.size();
			
			// check if each clique thus generated does not contain and edge
			// already considered
			for (TreeSet<Integer> c : maxcliques) {
				if(c.contains(1104811) && c.contains(1188777) && c.contains(1188782))
					System.out.println(u + " " + v + ": " + c);
				boolean newFlag = true;
				for (int x : Ei.keySet()) {
					for (int y : Ei.get(x)) {
						if (c.contains(x) && c.contains(y)) {
							//System.out.println(x + " " +  y + " : " + c);
							newFlag = false;
						}
					}
				}
				if (newFlag) {
					ncliqueCount++;
					/*if (c.size() >= 30) {
						largeCliqueCount++;
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date date = new Date();
						System.out.println(dateFormat.format(date));
						System.out.println(largeCliqueCount);
						System.out.println("new maximal clique found of size: " + c.size());
						System.out.println(c);
					}*/
				}
			}

			if (u < v) {

				// adding edge (u,v)
				if (Ei.get(u) == null) {
					TreeSet<Integer> L = new TreeSet<Integer>();
					L.add(v);
					Ei.put(u, L);
					spacecost3 += 2;
				} else {
					TreeSet<Integer> L = Ei.get(u);
					L.add(v);
					Ei.put(u, L);
					spacecost3 += 1;
				}
			}

			if (v < u) {

				// adding edge (v,u)
				if (Ei.get(v) == null) {
					TreeSet<Integer> L = new TreeSet<Integer>();
					L.add(u);
					Ei.put(v, L);
					spacecost3 += 2;
				} else {
					TreeSet<Integer> L = Ei.get(v);
					L.add(u);
					Ei.put(v, L);
					spacecost3 += 1;
				}
			}

			// ecount += 1;
			/* Clearing the data structures: Start */
			Nu.clear();
			Nv.clear();
			IndG.clear();
			/* Clearing the data structures: End */
		}
		// System.out.println("size of cand in new call:" + size_of_cand);
		/* Clearing the data structures: Start */
		Ei.clear();
		H.clear();
		// br.close();
		/* Clearing the data structures: End */
		/*
		 * =====================================================================
		 * =============================
		 */

		nftime = System.currentTimeMillis() - nstime;

		recompute_time = System.currentTimeMillis() - startTime; // end of
																	// computing
																	// new
																	// cliques
		// after adding a set of edges
		// System.out.println("The Graph");
		// G.print();
		// System.out.println("List of subsumed cliques");
		// print(Cdel);

	}

	/**
	 * This procedure cretes an induced subgraph with vertex set V from the
	 * input graph G.
	 */
	public static Graph CreateInducedSubgraph(Set<Integer> V) throws IOException {

		// System.out.println("Inside CreateInducedSubgraph");
		// G.print();

		Graph H = new Graph();

		for (int u : V) {
			// Set<Integer> N = new HashSet<Integer>(G.AdjList.get(u));
			for (int v : G.AdjList.get(u)) {
				if (V.contains(v)) {
					H.addEdge(u, v);
				}
			}
		}

		return H;

	}

	public static void print(Set<String> S) {
		int i = 0;
		// System.out.println("All new cliques: ");
		for (String s : S) {
			i++;
			// System.out.println("Clique" + " " + i + " : ");
			System.out.println(s);
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		if (args.length < 4) {

			System.out.println("usage : java program graph edge_set batchsize out_file");
			System.out.println("graph: represented as set of edges in a txt file");
			System.out.println("out_file: the file to store the output data");
			System.out.println("edge_set: edges to be added: text file");

			System.exit(0);
		}

		String graph = args[0];
		String edge_set = args[1];
		int batch_size = Integer.parseInt(args[2]); // input Set of edges to be
													// added
		String out_file = args[3];

		// Initializations
		Cdel = new HashSet<BitSet>();
		H = new Graph();
		Ei = new HashMap<Integer, TreeSet<Integer>>();

		spacecost = 0;

		maxDegree = 0;
		
		largeCliqueCount = 0;

		G = new Graph(graph, 1); // input file is in adjacency list format

		spacecost += G.getSize();

		BufferedReader br = new BufferedReader(new FileReader(edge_set));

		int count = 0;
		boolean eof_flag = false;
		int index = 0;
		String line;

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
		out.println();
		out.println("Algorithm-1");
		out.println();
		out.close();

		long cumulative_time = 0;

		while (true) {

			index = 0;
			count++;

			Set<int[]> batch = new HashSet<>();

			// File f = new File("batch");
			// FileWriter fw = new FileWriter(f);

			// we consider batch size to be the 3*log_2(maxDegree)

			// batch_size = 3 * MathOperations.log2(maxDegree);

			// if(batch_size == 0)
			// batch_size += 1;

			while (index < batch_size) {
				if ((line = br.readLine()) != null) {
					// fw.write(line);
					// fw.write("\n");
					batch.add(new int[] { Integer.parseInt(line.split("\\s+")[0]),
							Integer.parseInt(line.split("\\s+")[1]) });
					index++;
				} else {
					eof_flag = true;
					break;
				}
			}
			// fw.close();

			out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));

			// new NewAlgorithm_inc_4("batch");
			new Algorithm_1(batch);
			// f.delete();

			long subSize = 0;
			int subcount = 0;
			int n;

			long total_number = ncliqueCount + scliqueCount;
			// long total_number = ncliqueCount + subcount;
			long total_size = ncliqueSizes + scliqueSizes;
			// long total_number = ncliqueCount + cdel_count; //for dummy
			// long total_size = ncliqueSizes + cdel_size; //for dummy

			long sc = (long) ((spacecost + spacecost2 + spacecost3) * 4 / (1024 * 1024))
					+ (long) (scliqueCount * 8 / (1024 * 1024));

			// long for_clique = scliqueCount*8/(1024*1024);

			// newtime = recompute_time - subtime;

			cumulative_time += recompute_time;
			
			System.out.println(total_size_cumulative);

			out.println(count + "," + batch_size + "," + ncliqueCount + "," + recompute_time + "," + cumulative_time);
			// out.println(total_number + "," + total_size + "," + nftime + ","
			// + subtime + "," + recompute_time + "," + sc + "," +
			// g_density/g_count + "," + g_size + "," + g_edges);
			out.close();

			// System.gc();

			long total_memory_in_mb = Runtime.getRuntime().totalMemory() / (1024 * 1024);
			//if (count % 1000 == 0) {
				//System.out.println(count + ": new: " + ncliqueCount);
				//System.out.println("Total memory currently allocated : " + total_memory_in_mb);
			//}
			
			if(count == 1)
				break;

			if(cumulative_time >= 7200000)
				break;

			if (eof_flag)
				break;
			// if (20 == count)
			// break;
		}

	}
}
