package Algorithm.DynamicMCE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import Algorithm.Graph;
import utils.SetOperations;

/***
 * This is an implementation from the paper titled "Mining Maximal Cliques on
 * Dynamic Graphs Efficiently by Local Strategies" Note : This algorithm only
 * handles one edge insertion at a time. Support for batch insertion is not
 * there in the algorithm Drawbacks : 1. Iterate over the entire maximal clique
 * set for the maintenance of the changes 2. Does not maintain new maximal
 * cliques and subsumed cliques seperately
 * 
 * @author Apurba Das
 * @Date 11/1/2017
 *
 */

public class MCMEI {

	// private static Map<Integer, HashSet<HashSet<Integer>>> C;
	private static HashSet<HashSet<Integer>>[] C;
	private static Graph G;
	private static int maxCliqueCount;
	private static int largeCliqueCount;

	public static void main(String[] args) throws IOException {
		if (args.length < 4) {

			System.out.println("usage : java program graph clique_set edge_set out_file");
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
		int batch_size = Integer.parseInt(args[3]);
		String out_file = args[4];

		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(edge_set)));

		lnr.skip(Long.MAX_VALUE);

		int lines = lnr.getLineNumber() + 1;
		lnr.close();

		maxCliqueCount = 0;
		// C = new HashMap<>();

		C = new HashSet[lines + 10];

		C = new HashSet[2];

		G = new Graph(graph, 1);

		// reading the initial clique set
		BufferedReader br = new BufferedReader(new FileReader(clique_set));

		String line = "";
		// HashSet<HashSet<Integer>> cliqueset = new HashSet<>();

		C[0] = new HashSet<>();
		C[1] = new HashSet<>();

		while ((line = br.readLine()) != null) {
			HashSet<Integer> T = new HashSet<Integer>();
			for (String s : line.split("\\s+")) {
				T.add(Integer.parseInt(s));
			}
			maxCliqueCount++;
			C[0].add(T);
		}
		// C.put(0, cliqueset); //populating initial set of Maximal Cliques

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
		out.println();
		out.println("MCMEI");

		out.println("#iteration, time in ms, #maximal-cliques");
		out.close();

		BufferedReader erdr = new BufferedReader(new FileReader(edge_set));

		int iter = 0;
		int index = 0;
		boolean eof_flag = false;
		int ecount = 0;
		int count = 0;

		largeCliqueCount = 0;

		int turn = 1;

		long cumulative_time = 0;

		while (true) {

			index = 0;
			count++;
			Set<int[]> batch = new LinkedHashSet<>();
			
			if(count%1000 == 0)
				System.out.println("Currently at iteration: " + count);

			while (index < batch_size) {
				if ((line = erdr.readLine()) != null) {
					batch.add(new int[] { Integer.parseInt(line.split("\\s+")[0]),
							Integer.parseInt(line.split("\\s+")[1]) });
					index++;
					if (count <= 50000) {
						int u = Integer.parseInt(line.split("\\s+")[0]);
						int v = Integer.parseInt(line.split("\\s+")[1]);
						G.addEdge(u, v);
					}
				} else {
					eof_flag = true;
					break;
				}
			}

			long start = System.currentTimeMillis();
			Iterator<int[]> it = batch.iterator();
			if (count == 50001) {
				System.out.println("Number of vertices of initial graph: " + G.numV());
				System.out.println("Number of edges of initial graph: " + G.numE());
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println(dateFormat.format(date));
				for (int i = 0; i < index; i++) {
					if(i%10 == 0)
						System.out.println(i);
					ecount++;
					MCMEI algo = new MCMEI();

					int[] e = it.next();
					// System.out.println("Iteration " + ecount + " edge: " + e[0] + " " + e[1]);

					algo.run(e, turn);

					turn = 1 - turn;

				}
				long duration = System.currentTimeMillis() - start;
				
				cumulative_time += duration;
				out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
				out.println(count + "," + C[1 - turn].size() + "," + duration);
				out.close();
				break;
			}

			
			// System.out.println(ecount + "," + C[1-turn].size() + "," + duration + "," +
			// cumulative_time);
			

			if (cumulative_time >= 7200000)
				break;

		}

	}

	private void run(int[] e, int turn) {
		// TODO Auto-generated method stub
		int u = e[0];
		int v = e[1];
		G.addEdge(u, v);
		HashSet<HashSet<Integer>> tempSet = new HashSet<>();
		int prev_turn = 1 - turn;
		;
		// C[ecount] = new HashSet<>();
		int iteration_no = 0;
		Iterator<HashSet<Integer>> it = C[prev_turn].iterator();
		while (it.hasNext()) {
			HashSet<Integer> c = it.next();
			it.remove();
			// iteration_no++;
			// if(iteration_no%100000 == 0)
			// System.out.println(iteration_no);
			if (!c.contains(u) && !c.contains(v)) {
				C[turn].add(c);
			}
			if (c.contains(u)) {
				Collection<Integer> neighbors_of_v = G.neighborsOf(v);
				HashSet<Integer> C_cand = SetOperations.intersect(c, neighbors_of_v);

				if (C_cand.equals(c)) {
					C_cand.add(v);

					C[turn].add(C_cand);

					/*
					 * if(C_cand.size() >= 20){ largeCliqueCount++; DateFormat dateFormat = new
					 * SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); Date date = new Date();
					 * System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
					 * System.out.println(largeCliqueCount);
					 * System.out.println("new maximal clique found of size: " + C_cand.size());
					 * System.out.println(C_cand); }
					 */
				} else {
					C[turn].add(c);
					C_cand.add(v);
					if (MaxEval(C_cand)) {
						C[turn].add(C_cand);
						/*
						 * if(C_cand.size() >= 20){ largeCliqueCount++; DateFormat dateFormat = new
						 * SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); Date date = new Date();
						 * System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
						 * System.out.println(largeCliqueCount);
						 * System.out.println("new maximal clique found of size: " + C_cand.size());
						 * System.out.println(C_cand); }
						 */
					}
				}

			}
			if (c.contains(v)) {
				Collection<Integer> neighbors_of_u = G.neighborsOf(u);
				HashSet<Integer> C_cand = SetOperations.intersect(c, neighbors_of_u);
				if (!C_cand.equals(c)) {
					C[turn].add(c);
				}

			}

		}
		C[prev_turn].clear();

	}

	private boolean MaxEval(HashSet<Integer> C_cand) {
		Iterator<Integer> it = C_cand.iterator();
		Collection<Integer> S = G.neighborsOf(it.next());
		while (it.hasNext()) {
			int w = it.next();
			S = SetOperations.intersect(S, G.neighborsOf(w));
		}

		if (S.size() > 0)
			return false;
		return true;
	}

}
