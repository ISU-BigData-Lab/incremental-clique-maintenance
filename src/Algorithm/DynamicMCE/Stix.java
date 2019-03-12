package Algorithm.DynamicMCE;
/**

	This program implements stix algorithm for computing all new maximal
	cliques when a single edge is added at a time in G. Though the original 
	algorithm automates the process of maintaining all new maximal cliques 
	in G due to subsequent additon of edges once at a time, this program 
	evaluates the running time of the algorithm for maintaing 
	new maximal cliques when there is a lot more number of new cliques 
	may form (may even be exponential in worst case) due to addition of
	a single edge in the undirected graph G. 
 */

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import Algorithm.Graph;
import utils.SetOperations;

public class Stix {

	// private static Map<Integer, HashSet<Integer>> CliqueSet;
	private static HashSet<HashSet<Integer>> CliqueSet;
	private static HashSet<HashSet<Integer>> AuxCliqueSet;
	// private static Map<Integer, HashSet<Integer>> CliqueSet1;
	private Map<Integer, HashSet<Integer>> Cliques_u; // set of all cliques
														// containing vertex u
	private Map<Integer, HashSet<Integer>> Cliques_v; // set of all cliques
														// containing vertex v
	// private static Map<Integer, HashSet<Integer>> newCliques; // store all
	// the new cliques
	private static HashSet<HashSet<Integer>> newCliques; // store all the new
															// cliques

	// private static Map<Integer, HashSet<Integer>> subsumedCliques; //store
	// all subsumed cliques
	private static HashSet<HashSet<Integer>> subsumedCliques; // store all
																// subsumed
																// cliques

	private static Graph G;

	public static long startTime;
	public static long endTime;

	private static long recompute_time;

	private static long spacecost, spacecost1, spacecost2;
	private static int largeCliqueCount;

	public Stix(String edge_set) throws IOException {

		newCliques = new HashSet<HashSet<Integer>>();
		subsumedCliques = new HashSet<HashSet<Integer>>();

		spacecost1 = 0;
		spacecost2 = 0;

		File f = new File(edge_set);

		// Scanner scanner = new Scanner(f);

		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = "";
		// Tomita clique = new Tomita(G);
		// CliqueSet = new HashMap<Integer, HashSet<Integer>>(clique.CLQ);

		startTime = System.currentTimeMillis();

		int count = 0;
		while ((line = br.readLine()) != null) {
			
			count++;
			if(count%10 == 0)
				System.out.println(count);
			String[] splits = line.split("\\s+");
			int u = Integer.parseInt(splits[0]);
			int v = Integer.parseInt(splits[1]);
			// Pair p = new Pair(u,v);
			// System.out.println("Edge : " + u + ", " + v);
			G.addEdge(u, v);
			// Cliques_u = new HashMap<Integer,
			// HashSet<Integer>>(ComputeCliquesContaining(u)); // A:
			// set
			// of
			// all
			// cliques
			// containing
			// vertex
			// u
			// Cliques_v = new HashMap<Integer,
			// HashSet<Integer>>(ComputeCliquesContaining(v)); // B:
			// set
			// of
			// all
			// cliques
			// containing
			// vertex
			// v
			// System.out.println("Cliques at " + u + " " + Cliques_u);
			// System.out.println("Cliques at " + v + " " + Cliques_v);

			Cliques_u = ComputeCliquesContaining(u);
			Cliques_v = ComputeCliquesContaining(v);

			for (int i : Cliques_u.keySet()) {
				for (int j : Cliques_v.keySet()) {
					HashSet<Integer> A = Cliques_u.get(i);
					HashSet<Integer> B = Cliques_v.get(j);

					HashSet<Integer> C = new HashSet<Integer>();

					C = SetOperations.intersect(A, B);
					C.add(u);
					C.add(v);

					if (maximal(C, G)) {
						CliqueSet.add(C);
						spacecost += C.size();
						newCliques.add(C);
						/*
						 * if(C.size() >= 20){ largeCliqueCount++; DateFormat dateFormat = new
						 * SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); Date date = new Date();
						 * System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
						 * System.out.println(largeCliqueCount);
						 * System.out.println("new maximal clique found of size: " + C.size());
						 * System.out.println(C); }
						 */
					}

					if (!maximal(A, G)) {
						CliqueSet.remove(A);
						spacecost -= A.size();
						newCliques.remove(A);
						subsumedCliques.add(A);
					}

					if (!maximal(B, G)) {
						CliqueSet.remove(B);
						spacecost -= B.size();
						newCliques.remove(B);
						subsumedCliques.add(B);
					}
				}
			}
		}

		br.close();

		/** For computation of subsumed cliques Only : Start */
		Iterator<HashSet<Integer>> itr = subsumedCliques.iterator();
		while (itr.hasNext()) {
			HashSet<Integer> clq = itr.next();
			if (!AuxCliqueSet.contains(clq)) {
				itr.remove();
			}
		}

		/** For computation of subsumed cliques Only : End */

		/** For incremental computation : Start */
		AuxCliqueSet = new HashSet<HashSet<Integer>>(CliqueSet);
		/** For incremental computation : Stop */

		endTime = System.currentTimeMillis();

		/* Now the new cliques are without duplicates */
		// System.out.println("Set of all new cliques");
		// print(newCliques);
		// System.out.println("Set of all cliques");
		// print(CliqueSet);

	}

	public void print(HashSet<HashSet<Integer>> C) {
		// System.out.println("All new cliques: ");
		for (HashSet<Integer> S : C) {
			System.out.println("clique : " + S);
		}
		// for (int i : C.keySet()) {
		// System.out.println("Clique" + " " + i + " : ");
		// for (int u : C.get(i)) {
		// System.out.print(u + ";");
		// }
		// System.out.println();
		// }
	}

	/**
	 * This procedure checkes whether the set A is maximal clique in G by seeing
	 * whether there exists any vertex v in V\A so that v is connected to all the
	 * vertices in A. If true for some v in V\A then A is not maximal. Otherwise, A
	 * is maximal
	 */
	public boolean maximal(Set<Integer> C, Graph G) {

		Set<Integer> N = nb(C, G);

		for (int v : N) {
			if (G.AdjList.get(v).containsAll(C)) {
				return false;
			}
		}

		return true;
		/*
		 * HashSet<Integer> result = new HashSet<Integer>();
		 * 
		 * if (C.isEmpty()) return false;
		 * 
		 * int v = C.iterator().next();
		 * 
		 * HashSet<Integer> ngh = new HashSet<Integer>(G.AdjList.get(v));
		 * 
		 * result.addAll(ngh);
		 * 
		 * for (int u : C) {
		 * 
		 * ngh = new HashSet<Integer>(G.AdjList.get(u));
		 * 
		 * result.retainAll(ngh); }
		 * 
		 * if (result.isEmpty()) return true; else return false;
		 */

		// Set<Integer> N = nb(C, G);

		// for (int v : N) {
		// if (G.AdjList.get(v).containsAll(C)) {
		// return false;
		// }
		// }

		// return true;
	}

	public Set<Integer> nb(Set<Integer> C, Graph G) {
		Set<Integer> N = new HashSet<Integer>();

		// for (int u : C) {
		// N.addAll(G.AdjList.get(u));
		// }
		// N.removeAll(C);

		for (int u : C) {
			HashSet<Integer> NghOfu = G.AdjList.get(u);
			for (int v : NghOfu) {
				if (!C.contains(v)) {
					N.add(v);
				}
			}
		}

		return N;
	}

	public Map<Integer, HashSet<Integer>> ComputeCliquesContaining(int v) {

		int index = 1;
		Map<Integer, HashSet<Integer>> clique = new HashMap<Integer, HashSet<Integer>>();

		for (HashSet<Integer> s : CliqueSet) {
			if (s.contains(v)) {
				clique.put(index, s);
				index++;
			}
		}

		return clique;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		if (args.length < 3) {

			System.out.println("usage : java program graph clique_set edge_set batch_size out_file");
			System.out.println("graph : represented as set of edges in a text file");
			System.out.println("clique_set: set of all maximal cliques of graph in a text file");
			System.out.println("edge_set : edges to be added : text file");
			System.out.println("batch_size: number of edges in a batch");
			System.out.println("out_file: the file to store the output data");

			System.exit(0);
		}

		String graph = args[0]; // input graph
		String clique_set = args[1];
		String edge_set = args[2]; // input Set of edges to be added
		int batch_size = Integer.parseInt(args[3]);
		String out_file = args[4];

		BufferedReader br = new BufferedReader(new FileReader(clique_set));

		String line;

		spacecost = 0;

		largeCliqueCount = 0;

		CliqueSet = new HashSet<HashSet<Integer>>();
		AuxCliqueSet = new HashSet<HashSet<Integer>>();
		// CliqueSet1 = new HashMap<Integer, HashSet<Integer>>();

		while ((line = br.readLine()) != null) {
			HashSet<Integer> S = new HashSet<Integer>();
			for (String s : line.split("\\s+")) {
				S.add(Integer.parseInt(s));
			}
			// CliqueSet.put(CliqueSet.keySet().size()+1, S);
			CliqueSet.add(S);
			spacecost += S.size();
			AuxCliqueSet.add(S);
			spacecost += S.size();
		}

		br.close();
		
		System.out.println("Reading cliques complete");

		G = new Graph(graph, 1); // input file is in adjacency list format
		
		System.out.println("Reading empty graph complete");

		spacecost += G.getSize();

		br = new BufferedReader(new FileReader(edge_set));

		int count = 0;
		boolean eof_flag = false;
		int index = 0;

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
		out.println("STIX");
		out.println();
		out.close();

		long cumulative_time = 0;

		while (true) {

			index = 0;
			count++;

			File f = new File("batch");
			FileWriter fw = new FileWriter(f);
			
			if(count%100 == 0)
				System.out.println("Currently at count: " + count);

			while (index < batch_size) {
				if ((line = br.readLine()) != null) {
					fw.write(line);
					fw.write("\n");
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
			fw.close();

			// new Stix("_e"+x);
			if (count == 50001) {
				System.out.println("Number of vertices of initial graph: " + G.numV());
				System.out.println("Number of edges of initial graph: " + G.numE());
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println(dateFormat.format(date));
				new Stix("batch");

				recompute_time = endTime - startTime;

				cumulative_time += recompute_time;

				int subSize = 0;
				int newSize = 0;
				int n;

				int total_number = newCliques.size() + subsumedCliques.size();
				int total_size = newSize + subSize;

				long sc = (spacecost + spacecost1 + spacecost2) * 4 / (1024 * 1024);

				out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
				out.println(count + "," + total_number + "," + total_size + "," + recompute_time + "," + cumulative_time + "," + sc);
				out.close();

				break;

			}

			// for (HashSet<Integer> s : subsumedCliques)
			// {
			// n = s.size();

			// spacecost1 += n;

			// subSize += (n * (n - 1) / 2);
			// }

			// for (HashSet<Integer> s : newCliques)
			// {
			// n = s.size();
			// newSize += (n * (n - 1) / 2);

			// spacecost2 += n;
			// }

			// System.out.println(count + ": # new cliques : " + newCliques.size() + "#
			// subsumed cliques : " + subsumedCliques.size());

			/*
			 * int total_number = newCliques.size() + subsumedCliques.size(); int total_size
			 * = newSize + subSize;
			 * 
			 * long sc = (spacecost + spacecost1 + spacecost2) * 4 / (1024 * 1024);
			 * 
			 * out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));
			 * out.println(total_number + "," + total_size + "," + recompute_time + "," +
			 * cumulative_time + "," + sc); out.close();
			 */

			if (cumulative_time >= 7200000)
				break;

			if (eof_flag)
				break;
			// if (20 == count)
			// break;
		}

	}
}
