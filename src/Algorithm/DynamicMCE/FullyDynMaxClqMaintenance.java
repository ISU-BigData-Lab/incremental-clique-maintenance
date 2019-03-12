package Algorithm.DynamicMCE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import Algorithm.Graph;
import Algorithm.MurmurHash3;
import utils.SetOperations;

public class FullyDynMaxClqMaintenance {

	private static HashMap<String, Integer> _deletion_added_maximal_cliques;
	private static HashSet<String> _deletion_removed_maximal_cliques;
	private static HashSet<String> _addition_new_maximal_cliques;
	private static HashSet<String> _addition_subsumed_maximal_cliques;
	private static HashSet<String> _cliqueset_old_graph;
	private HashSet<int[]> _deleted_edges;
	private HashSet<int[]> _added_edges;
	private static long _deletion_number_added_cliques;
	private static long _deletion_size_in_edges_added_cliques;
	private static long _deletion_size_in_nodes_added_cliques;
	private static long _deletion_number_deleted_cliques;
	private static long _deletion_size_in_edges_deleted_cliques;
	private static long _deletion_size_in_nodes_deleted_cliques;
	private static long _addition_number_new_cliques;
	private static long _addition_size_in_edges_new_cliques;
	private static long _addition_size_in_nodes_new_cliques;
	private static long _addition_number_subsumed_cliques;
	private static long _addition_size_in_edges_subsumed_cliques;
	private static long _addition_size_in_nodes_subsumed_cliques;
	private static long _addition_update_time;
	private static long _deletion_update_time;
	private static long _number_not_in_output;
	private static long _size_in_nodes_not_in_output;
	private static long _size_in_edges_not_in_output;
	private static Graph g;
	private static Graph h;

	public FullyDynMaxClqMaintenance() {

		_deletion_added_maximal_cliques = new HashMap<>();
		_deletion_removed_maximal_cliques = new HashSet<>();
		_addition_new_maximal_cliques = new HashSet<>();
		_addition_subsumed_maximal_cliques = new HashSet<>();
		_deleted_edges = new HashSet<>();
		_added_edges = new HashSet<>();
		_deletion_number_added_cliques = 0;
		_deletion_size_in_edges_added_cliques = 0;
		_deletion_size_in_nodes_added_cliques = 0;
		_deletion_number_deleted_cliques = 0;
		_deletion_size_in_edges_deleted_cliques = 0;
		_deletion_size_in_nodes_deleted_cliques = 0;
		_addition_number_new_cliques = 0;
		_addition_size_in_edges_new_cliques = 0;
		_addition_size_in_nodes_new_cliques = 0;
		_addition_number_subsumed_cliques = 0;
		_addition_size_in_edges_subsumed_cliques = 0;
		_addition_size_in_nodes_subsumed_cliques = 0;
		_addition_update_time = 0;
		_deletion_update_time = 0;
		_number_not_in_output = 0;
		_size_in_nodes_not_in_output = 0;
		_size_in_edges_not_in_output = 0;

	}

	public static void main(String[] args) throws IOException {

		if (args.length < 4) {
			System.out.println("usage: java program graph edgeset batchsize outfile");
			System.exit(0);
		}

		String graph = args[0];
		String edge_set = args[1];
		int batch_size = Integer.parseInt(args[2]);
		String output_file = args[3];

		g = new Graph(graph, 1); // adjacency list format
		// g = new Graph(graph); //edge list format

		System.out.println("Graph Reading Complete");

		int index = 0;
		boolean eof_flag = false;
		int iteration_count = 0;
		String line = "";

		BufferedReader edge_reader = new BufferedReader(new FileReader(edge_set));

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output_file, true)));

		out.println("FullyDynamicMaintenance");
		
		out.println("We perform this experiment using a sliding window of fixed size. "
				+ "After initializing the graph with half of initial edges (according to the increasing timestamps), we start with a fixed size sliding window"
				+ "where every time window slides by the window_size, we delete the expired edges and insert the new edges in the current window");

		out.println(
				"count,batchsise,NumberNew,NumberDeleted,NumberNotOutput,SizeInNodeNew,SizeInNodeDeleted,SizeInNodeNotOutput,SizeInEdgeNew,SizeInEdgeDeleted,SizeInEdgeNotOutput,IncrementalTime,DecrementalTime,TotalTime");

		out.close();
		
		Path path = Paths.get(edge_set);
		
		long line_count = Files.lines(path).count();
		
		long edges_to_add_initially = line_count/2;
		
		long count = 0;

		long cumulative_time = 0;
		
		//HashSet<int[]> expired_edges = new HashSet<>();
		//HashSet<int[]> new_edges = new HashSet<>();
		
		/*while(count < edges_to_add_initially) {
			count++;
			line = edge_reader.readLine();
			String[] splits = line.split("\\s+");
			int u = Integer.parseInt(splits[0]);
			int v = Integer.parseInt(splits[1]);
			g.addEdge(u, v);
			
			if(edges_to_add_initially - count <= window_size) {
				expired_edges.add(new int[] {u,v});
			}
		}*/

		while (true) {
			Set<String> batch = new HashSet<>();
			index = 0;
			iteration_count++;

			//System.out.println("Iteration: " + iteration_count);

			while (index < batch_size) {
				if ((line = edge_reader.readLine()) != null) {
					batch.add(line);
					//String[] splits = line.split("\\s+");
					//int u = Integer.parseInt(splits[0]);
					//int v = Integer.parseInt(splits[1]);
					//new_edges.add(new int[] {u,v});
					index++;
				} else {
					eof_flag = true;
					break;
				}
			}

			if (eof_flag)
				break;

			FullyDynMaxClqMaintenance instance = new FullyDynMaxClqMaintenance();

			instance.run(batch);
			
			//expired_edges = new_edges;

			long number_new = _addition_number_new_cliques + _deletion_number_added_cliques;
			long number_deleted = _addition_number_subsumed_cliques + _deletion_number_deleted_cliques;

			long size_in_node_new = _addition_size_in_nodes_new_cliques + _deletion_size_in_nodes_added_cliques;
			long size_in_node_deleted = _addition_size_in_nodes_subsumed_cliques
					+ _deletion_size_in_nodes_deleted_cliques;
			long size_in_node_not_output = _size_in_nodes_not_in_output;

			long size_in_edge_new = _addition_size_in_edges_new_cliques + _deletion_size_in_edges_added_cliques;
			long size_in_edge_deleted = _addition_size_in_edges_subsumed_cliques
					+ _deletion_size_in_edges_deleted_cliques;
			long size_in_edge_not_output = _size_in_edges_not_in_output;

			long deletion_update_time_in_ms = _deletion_update_time;
			long addition_update_time_in_ms = _addition_update_time;
			long total_time_in_ms = (_deletion_update_time + _addition_update_time);

			cumulative_time += total_time_in_ms;
			
			out = new PrintWriter(new BufferedWriter(new FileWriter(output_file, true)));

			out.println(iteration_count + "," + batch_size + "," + number_new + "," + number_deleted + ","
					+ _number_not_in_output + "," + size_in_node_new + "," + size_in_node_deleted + ","
					+ size_in_node_not_output + "," + size_in_edge_new + "," + size_in_edge_deleted + ","
					+ size_in_edge_not_output + "," + deletion_update_time_in_ms + "," + addition_update_time_in_ms
					+ "," + total_time_in_ms);
			out.close();

			if(cumulative_time >= 7200000)
				break;

			if (iteration_count % 100 == 0) {
				System.out.println(iteration_count + "\t" + number_new + "\t" + number_deleted + "\t"
						+ _number_not_in_output + "\t" + total_time_in_ms);
			}

		}

		edge_reader.close();

	}

	private void run(Set<String> batch) {

		for (String signed_edge : batch) {
			String[] splits = signed_edge.split("\\s+");
			int u = Integer.parseInt(splits[0]);
			int v = Integer.parseInt(splits[1]);
			int sign = Integer.parseInt(splits[2]);

			if (0 == sign) {
				_deleted_edges.add(new int[] { u, v });
			}
			if (1 == sign) {
				_added_edges.add(new int[] { u, v });
			}

		}

		long start = System.currentTimeMillis();
		DecrementalMaintenance(_deleted_edges);
		_deletion_update_time = System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		IncrementalMaintenance(_added_edges);
		_addition_update_time = System.currentTimeMillis() - start;

	}

	private void IncrementalMaintenance(HashSet<int[]> _added_edges) {
		HashMap<Integer, HashSet<Integer>> considered_edges = new HashMap<>();

		h = new Graph();

		for (int[] e : _added_edges) {
			int u = e[0];
			int v = e[1];
			g.addEdge(u, v);
			h.addEdge(u, v);
		}

		for (int[] e : _added_edges) {

			Graph g_e = new Graph();
			HashSet<Integer> v_e = new HashSet<>();
			HashSet<Integer> neighbors_of_u = new HashSet<>();
			HashSet<Integer> neighbors_of_v = new HashSet<>();

			int u = e[0];
			int v = e[1];

			v_e = SetOperations.intersect(g.AdjList.get(u), g.AdjList.get(v));

			v_e.add(u);
			v_e.add(v);

			g_e = CreateInducedSubgraph(v_e);

			TTTExt(g_e, u, v, considered_edges, 1); // 1 indicates incremental case

			// adding edge (u,v)
			if (considered_edges.get(u) == null) {
				HashSet<Integer> L = new HashSet<Integer>();
				L.add(v);
				considered_edges.put(u, L);
			} else
				considered_edges.get(u).add(v);

			// adding edge (v,u)
			if (considered_edges.get(v) == null) {
				HashSet<Integer> L = new HashSet<Integer>();
				L.add(u);
				considered_edges.put(v, L);
			} else
				considered_edges.get(v).add(u);

		}

	}

	private void DecrementalMaintenance(HashSet<int[]> _deleted_edges) {

		HashMap<Integer, HashSet<Integer>> considered_edges = new HashMap<>();

		h = new Graph();

		for (int[] e : _deleted_edges) {
			int u = e[0];
			int v = e[1];
			g.removeEdge(u, v);
			h.addEdge(u, v);
		}

		for (int[] e : _deleted_edges) {

			Graph g_e = new Graph();
			HashSet<Integer> v_e = new HashSet<>();
			HashSet<Integer> neighbors_of_u = new HashSet<>();
			HashSet<Integer> neighbors_of_v = new HashSet<>();

			int u = e[0];
			int v = e[1];

			neighbors_of_u.addAll(g.AdjList.get(u));
			neighbors_of_u.addAll(h.AdjList.get(u));

			neighbors_of_v.addAll(g.AdjList.get(v));
			neighbors_of_v.addAll(h.AdjList.get(v));

			v_e = SetOperations.intersect(neighbors_of_u, neighbors_of_v);

			v_e.add(u);
			v_e.add(v);

			g_e = CreateInducedSubgraph(v_e, h);

			TTTExt(g_e, u, v, considered_edges, 0); // 0 indicates decremental case

			// adding edge (u,v)
			if (considered_edges.get(u) == null) {
				HashSet<Integer> L = new HashSet<Integer>();
				L.add(v);
				considered_edges.put(u, L);
			} else
				considered_edges.get(u).add(v);

			// adding edge (v,u)
			if (considered_edges.get(v) == null) {
				HashSet<Integer> L = new HashSet<Integer>();
				L.add(u);
				considered_edges.put(v, L);
			} else
				considered_edges.get(v).add(u);

		}

	}

	private void TTTExt(Graph g_e, int u, int v, HashMap<Integer, HashSet<Integer>> considered_edges, int flag) {
		HashSet<Integer> cand = new HashSet<>();
		HashSet<Integer> fini = new HashSet<>();
		TreeSet<Integer> k = new TreeSet<>();

		cand.addAll(g_e.AdjList.keySet());
		k.add(u);
		k.add(v);
		cand.remove(u);
		cand.remove(v);
		expand(g_e, k, cand, fini, considered_edges, flag);

	}

	private void expand(Graph g_e, TreeSet<Integer> k, HashSet<Integer> cand, HashSet<Integer> fini,
			HashMap<Integer, HashSet<Integer>> considered_edges, final int flag) {
		if (cand.isEmpty() && fini.isEmpty()) {
			if (0 == flag) {
				// decremental case
				// _deletion_removed_maximal_cliques.add(k.toString());
				_deletion_number_deleted_cliques++;
				int size_of_k = k.size();
				_deletion_size_in_nodes_deleted_cliques += size_of_k;
				_deletion_size_in_edges_deleted_cliques += size_of_k * (size_of_k - 1) / 2;
				ComputeAddedMaximalCliques(k);
			}
			if (1 == flag) {
				// incremental case
				_addition_number_new_cliques++;
				int size_of_k = k.size();
				_addition_size_in_nodes_new_cliques += size_of_k;
				_addition_size_in_edges_new_cliques += size_of_k * (size_of_k - 1) / 2;
				// _addition_new_maximal_cliques.add(k.toString());
				ComputeSubsumedMaximalCliques(k);
			}
		} else if (cand.isEmpty())
			return;

		int pivot = findPivot(g, cand, fini);

		Collection<Integer> neighbors_of_pivot = g.neighborsOf(pivot);

		Iterator<Integer> cand_iterator = cand.iterator();

		while (cand_iterator.hasNext()) {

			int q = cand_iterator.next();
			if (!neighbors_of_pivot.contains(q)) {
				k.add(q);

				// Line 9-13 of TomitaE : START
				boolean tflag = false;
				if (considered_edges.keySet().size() > 0 && (considered_edges.get(q) != null)) {
					// HashSet<Integer> S = Ei.get(q);
					for (int v : k) {
						if (considered_edges.get(q).contains(v)) {
							tflag = true;
							break;
						}
					}
				}

				if (tflag) { // Kq intersect Ei is not null
					cand_iterator.remove(); // CAND - {q}
					k.remove(q);
					fini.add(q);
					continue;
				}
				// Line 9-13 of TomitaE : END

				Collection<Integer> neighbors_of_q = g.neighborsOf(q);
				HashSet<Integer> cand_q = new HashSet<Integer>();
				HashSet<Integer> fini_q = new HashSet<Integer>();

				cand_q = SetOperations.intersect(cand, neighbors_of_q);
				fini_q = SetOperations.intersect(fini, neighbors_of_q);

				expand(g, k, cand_q, fini_q, considered_edges, flag);

				cand_iterator.remove(); // CAND - {q}
				k.remove(q);
				fini.add(q); // FINI union {q}
			}

		}

	}

	private void ComputeSubsumedMaximalCliques(TreeSet<Integer> new_clique) {
		HashSet<TreeSet<Integer>>[] s = new HashSet[(int) Math.pow((double) new_clique.size(), 2.0)];

		s[0] = new HashSet<TreeSet<Integer>>();

		s[0].add(new_clique);

		int idx = 0;

		for (int u : new_clique) {
			for (int v : new_clique) {
				if ((u < v) && (h.containsEdge(u, v))) {
					Iterator<TreeSet<Integer>> itr = s[idx].iterator();
					idx++;
					s[idx] = new HashSet<>();
					while (itr.hasNext()) {
						TreeSet<Integer> temp_set = itr.next();
						if (temp_set.contains(u) && temp_set.contains(v)) {
							TreeSet<Integer> clique_minus_u = new TreeSet<>(temp_set);
							TreeSet<Integer> clique_minus_v = new TreeSet<>(temp_set);
							clique_minus_u.remove(u);
							clique_minus_v.remove(v);
							s[idx].add(clique_minus_u);
							s[idx].add(clique_minus_v);
						} else
							s[idx].add(temp_set);
					}
				}
			}
		}

		for (TreeSet<Integer> candidate_clique : s[idx]) {
			if (isMaximalIncremental(candidate_clique)) {

				if (!_addition_subsumed_maximal_cliques.contains(candidate_clique.toString())) {

					if (!_deletion_added_maximal_cliques.containsKey(candidate_clique.toString())) {
						_addition_subsumed_maximal_cliques.add(candidate_clique.toString());
						_addition_number_subsumed_cliques++;
						int size_of_subclique = candidate_clique.size();
						_addition_size_in_nodes_subsumed_cliques += size_of_subclique;
						_addition_size_in_edges_subsumed_cliques += size_of_subclique * (size_of_subclique - 1) / 2;
					} else {
						if (_deletion_added_maximal_cliques.get(candidate_clique.toString()) != null) {
							int val = _deletion_added_maximal_cliques.get(candidate_clique.toString());
							if (1 == val) {
								_deletion_added_maximal_cliques.put(candidate_clique.toString(), 0);
								_deletion_number_added_cliques -= 1;
								_number_not_in_output++;
								int size_of_subclique = candidate_clique.size();
								_deletion_size_in_nodes_added_cliques -= size_of_subclique;
								_size_in_nodes_not_in_output += size_of_subclique;
								_deletion_size_in_edges_added_cliques -= size_of_subclique * (size_of_subclique - 1)
										/ 2;
								_size_in_edges_not_in_output += size_of_subclique * (size_of_subclique - 1) / 2;
							}
						}
					}
				}
			}
		}

	}

	private void ComputeAddedMaximalCliques(TreeSet<Integer> deleted_clique) {

		HashSet<TreeSet<Integer>>[] s = new HashSet[(int) Math.pow((double) deleted_clique.size(), 2.0)];

		s[0] = new HashSet<TreeSet<Integer>>();

		s[0].add(deleted_clique);

		int idx = 0;

		for (int u : deleted_clique) {
			for (int v : deleted_clique) {
				if ((u < v) && (h.containsEdge(u, v))) {
					Iterator<TreeSet<Integer>> itr = s[idx].iterator();
					idx++;
					s[idx] = new HashSet<>();
					while (itr.hasNext()) {
						TreeSet<Integer> temp_set = itr.next();
						if (temp_set.contains(u) && temp_set.contains(v)) {
							TreeSet<Integer> clique_minus_u = new TreeSet<>(temp_set);
							TreeSet<Integer> clique_minus_v = new TreeSet<>(temp_set);
							clique_minus_u.remove(u);
							clique_minus_v.remove(v);
							s[idx].add(clique_minus_u);
							s[idx].add(clique_minus_v);
						} else
							s[idx].add(temp_set);
					}
				}
			}
		}

		for (TreeSet<Integer> candidate_clique : s[idx]) {
			if (isMaximalDeletion(candidate_clique)) {

				if (!_deletion_added_maximal_cliques.containsKey(candidate_clique.toString())) {
					_deletion_added_maximal_cliques.put(candidate_clique.toString(), 1);
					_deletion_number_added_cliques++;
					int size_of_new = candidate_clique.size();
					_deletion_size_in_nodes_added_cliques += size_of_new;
					_deletion_size_in_edges_added_cliques += size_of_new * (size_of_new - 1) / 2;
				}
			}
		}
	}

	private boolean isMaximalDeletion(TreeSet<Integer> candidate_clique) {

		HashSet<Integer> result_set = new HashSet<>();

		Iterator it = candidate_clique.iterator();

		int u = (int) it.next();

		result_set.addAll(g.AdjList.get(u));

		while (it.hasNext()) {
			HashSet<Integer> temp = new HashSet<>();
			int v = (int) it.next();
			temp.addAll(g.AdjList.get(v));
			result_set = SetOperations.intersect(result_set, temp);
		}

		if (!result_set.isEmpty())
			return false;

		return true;
	}

	// Let H1 is the set of deleted edges and H2 is the set of added edges.
	// Here we are checking maximality of a candidate clique in G-H1. Is this
	// correct?
	// If it is a maximal in G-H1, then this will be removed from
	// _deletion_added_maximal_cliques
	// If it is maximal in G, it will be in _addition_subsumed_maximal_cliques.
	// How to check if the candidate_clique is maximal in G?
	private boolean isMaximalIncremental(TreeSet<Integer> candidate_clique) {

		HashSet<Integer> S = new HashSet<>();

		Iterator it = candidate_clique.iterator();

		int u = (int) it.next();

		S.addAll(g.AdjList.get(u));
		if (h.AdjList.get(u) != null)
			S.removeAll(h.AdjList.get(u));

		while (it.hasNext()) {
			HashSet<Integer> temp = new HashSet<>();
			int v = (int) it.next();
			temp.addAll(g.AdjList.get(v));
			if (h.AdjList.get(v) != null)
				temp.removeAll(h.AdjList.get(v));
			S = SetOperations.intersect(S, temp);
		}

		if (!S.isEmpty())
			return false;

		return true;
	}

	private Graph CreateInducedSubgraph(HashSet<Integer> v_e, Graph h) {
		// Decremental case
		Graph g_e = new Graph();

		for (int u : v_e) {
			for (int v : v_e) {
				if (g.containsEdge(u, v) || h.containsEdge(u, v)) {
					g_e.addEdge(u, v);

				}
			}
		}
		return g_e;
	}

	private Graph CreateInducedSubgraph(HashSet<Integer> v_e) {
		// Incremental case
		Graph g_e = new Graph();

		for (int u : v_e) {
			for (int v : v_e) {
				if (g.containsEdge(u, v)) {
					g_e.addEdge(u, v);

				}
			}
		}

		return g_e;

	}

	public int findPivot(Graph g, Set<Integer> cand, Set<Integer> fini) {

		int size = -1;
		int pivot = 0;

		for (int u : cand) {
			HashSet<Integer> Q = new HashSet<Integer>();
			Q = SetOperations.intersect(cand, g.neighborsOf(u));
			int tmp = Q.size();
			if (size <= tmp) {
				size = tmp;
				pivot = u;
			}
		}

		for (int u : fini) {
			HashSet<Integer> Q = new HashSet<Integer>();
			Q = SetOperations.intersect(cand, g.neighborsOf(u));
			int tmp = Q.size();
			if (size <= tmp) {
				size = tmp;
				pivot = u;
			}
		}

		return pivot;

	}

}
