package Algorithm.DynamicMCE;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
//import org.mapdb.*;

import Algorithm.Graph;
import utils.MathOperations;
import utils.SetOperations;

public class Ottosen {

	private static Set<String> CliqueSet;
	private static Set<String> newCliques; // store all the new cliques
	private static Set<String>OverCountedCliques;   //to track the existing cliques counted as new
	private Set<Integer> U;
	private Set<Integer> FamilyOfU;
	private Set<String> Cnew;
	private static Set<String> Cdel;

	public static long startTime;
	public static long newtime;
	public static long subtime;
	public static long endTime;

	private static long recompute_time;

	private static Graph G;


	private static int cliqueCount;
	private static int cliqueSizes;
	private static int spaceOverhead;
	private static int spaceOverhead1;
	private static int spaceOverhead2;
	private static int spaceOverhead3;
	private static int spaceOverhead4;
	private static int spaceOverhead5;

	private static String newDir;
	private static String MaximalSet;
	private static String SubsumedSet;
	private static String TotalSet;

	private static File ottosen;
	private static File cnew;
	private static File cdel;
	private static File All;
	
	private static int maxDegree;
	
	private static int largeCliqueCount;
	
	//private static DB db;

	public Ottosen(Set<int[]> batch) throws IOException {

		newCliques = new HashSet<String>();
		//newCliques = db.getHashSet("newCliques");
		Cdel = new HashSet<String>();
		//Cdel = db.getHashSet("Cdel");
		U = new HashSet<Integer>();
		//U = db.getHashSet("U");
		FamilyOfU = new HashSet<Integer>();
		//FamilyOfU = db.getHashSet("FamilyOfU");
		OverCountedCliques = new HashSet<String>();

		startTime = System.currentTimeMillis();

		spaceOverhead1 = 0;	//for maintaining Cnew
		spaceOverhead2 = 0;	//for maintaining U
		spaceOverhead3 = 0;	//for maintaining newCliques
		spaceOverhead4 = 0;	//for maintaining Cdel

		/**Computes all new cliques here*/
		SetBasedUpdate(batch);

		//System.out.println(CliqueSet);
		
		newtime = System.currentTimeMillis() - startTime;

		long t1 = System.currentTimeMillis();

		/** Compute Subsumed Cliques : START */
		Iterator<String> citr = CliqueSet.iterator();
		while (citr.hasNext()) {
			String c = citr.next();
			//System.out.println(c);
			String[] ids = c.split("\\s+");
			HashSet<Integer> temp = new HashSet<Integer>();
			for(String id : ids){
				temp.add(Integer.parseInt(id));
			}
			temp.retainAll(U);
			

			if (!temp.isEmpty()) {
				//System.out.println("subsumed: " + c);
			        if(!OverCountedCliques.contains(c))
			            Cdel.add(c);
			}

		}
		/** Compute Subsumed Cliques : END */
		
		subtime = System.currentTimeMillis() - t1;

		
		/**Removes already existing cliques which are generatd along
		 * Along with the new maximal cliques; These cliques are actually 
		 * gets deleted while computing subsumed cliques; But we manually
		 * deletes them from the Cdel set to maintain exact set of subsumed cliques*/
		/*
		Iterator<String> itr = newCliques.iterator();

		while (itr.hasNext()) {
			String entry = itr.next();
			if (CliqueSet.contains(entry)){
				Cdel.remove(entry);
				itr.remove();
			}
		}
		*/


		/**Update the clique set, for incremental computation : START*/
		CliqueSet.addAll(newCliques);
		CliqueSet.removeAll(Cdel);
		//space cost should be adjusted accordingly
		/**Update the clique set, for incremental computation : END*/
		
		endTime = System.currentTimeMillis();

		spaceOverhead2 = U.size()*4;

		for(String s : Cnew){
			spaceOverhead1 += s.length();	//considering each integer of size 4 byte
		}

		//for(String s : CliqueSet){
		//	spaceOverhead5 += s.length();	//considering each integer of size 4 byte
		//}


		//System.out.println("List of all new cliques:");
		//print(newCliques);
		//System.out.println("List of all subsumed cliques:");
		//print(Cdel);

	}

	public void print(Set<String> C) {
		// System.out.println("All new cliques:");
		int i = 0;
		for (String s : C) {
			i++;
			System.out.println("Clique" + " " + i + " : ");
			System.out.println(s);
		}
	}

	public void SetBasedUpdate(Set<int[]> batch) throws IOException {

		//File f = new File(edge_set);
		//Scanner scanner = new Scanner(f);
		
		//BufferedReader br = new BufferedReader(new FileReader(f));
		//String line = "";

		/* =====Line 5 & 6 : START====== */
		/**Creates set U and update the original graph by adding the set of edges*/
		//while (scanner.hasNextInt()) {
		int iteration_count = 0;
		for(int[] e : batch){
			//String[] splits = line.split("\\s+");
			iteration_count++;
			if(iteration_count%10 == 0)
				System.out.println(iteration_count);
			int u = e[0];
			int v = e[1];
			//int u = scanner.nextInt();
			//int v = scanner.nextInt();
			U.add(u);
			U.add(v);
			G.addEdge(u, v); // This G is the latest G after adding all the edges
			spaceOverhead += (2*4);
			
			int degree_of_u = G.degreeOf(u);
			int degree_of_v = G.degreeOf(v);
			
			if(degree_of_u > maxDegree)
				maxDegree = degree_of_u;
			
			if(degree_of_v > maxDegree)
				maxDegree = degree_of_v;
		}
		/* =====Line 5 & 6 : END======= */


		/* ========Line 7 : START====== */
		for (int u : U) {
			FamilyOfU.addAll(G.AdjList.get(u));
		}
		FamilyOfU.addAll(U);

		
		/*
		 * System.out.println("The Graph : "); G.print();
		 * System.out.println("FamilyOfU : "); for(int u : FamilyOfU)
		 * System.out.print(u + " "); System.out.println();
		 */
		TreeSet<Integer> R = new TreeSet<Integer>();
		HashSet<Integer> X = new HashSet<Integer>();
		Cnew = new HashSet<String>();
		//Cnew = db.getHashSet("Cnew");
		cliqueCount = 0;
		cliqueSizes = 0;
		//Cnew = BKWithPivot(G, R, FamilyOfU, X); // compute new cliques using
		BKWithPivot(G, R, FamilyOfU, X); // compute new cliques using
												// Bron-Kerbosch algorithm with
												// pivoting

		/*
		 * System.out.println("All new cliques"); for(HashSet<Integer> c :
		 * Cnew){ for(int u : c) System.out.print(u + ";");
		 * System.out.println(); }
		 */

		/* ========Line 7 : END======== */

		// Add new cliques
		//for(File fileEntry : cnew.listFiles()){
		//	if(!fileEntry.isDirectory()){
		//		String[] ids = fileEntry.getName().split("\\s+");
		//		HashSet<Integer> ctemp = new HashSet<Integer>();
		//		for(String id : ids){
		//			if(U.contains(Integer.parseInt(id)))
		//				ctemp.add(Integer.parseInt(id));
		//		}
		//		if(ctemp.isEmpty()) {	//not new maximal clique
		//			fileEntry.delete();
		//		}
		//	}
		//}
		//for (File fileEntry : cnew.listFiles()) {
		for (String c : Cnew) {
			//String c = fileEntry.getName();
			String[] ids = c.split("\\s+");
			HashSet<Integer> ctemp = new HashSet<Integer>();
			for(String id : ids){
				ctemp.add(Integer.parseInt(id));
			}
			ctemp.retainAll(U);
			if (!ctemp.isEmpty()) {
				//File file = new File(MaximalSet + File.separator + c);
				//file.createNewFile();
			        if(!CliqueSet.contains(c))
			            newCliques.add(c);
			        else
			            OverCountedCliques.add(c);
			}
		}

		//scanner.close();

	}

	// Bron-Kerbosch algorithm with pivot
	//private HashSet<String> BKWithPivot(Graph G, TreeSet<Integer> R,
	private void BKWithPivot(Graph G, TreeSet<Integer> R,
			Set<Integer> P, Set<Integer> X) throws IOException{

		//HashSet<String> C = new HashSet<String>();
		if (P.isEmpty() && X.isEmpty()) {
			/*
			 * System.out.println(); System.out.println("New cliques"); for(int
			 * u : R) System.out.print(u + ";"); System.out.println();
			 */
			StringBuilder sb = new StringBuilder();
			for(int u : R){
				sb.append(u + " ");
			}
			//C.add(sb.toString());
			//File file = new File(MaximalSet + File.separator + sb.toString());
			//file.createNewFile();
			
			/*if(R.size() >= 30){
				largeCliqueCount++;
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
				System.out.println(largeCliqueCount);
				System.out.println("new maximal clique found of size: " + R.size());
				System.out.println(R);
			}*/

			Cnew.add(sb.toString());
			//C.add(R);
			//return C;

		} else {

			//HashSet<Integer> PuX = new HashSet<Integer>(P);
			//PuX.addAll(X);
			int u;
			if(!P.isEmpty())
				u = P.iterator().next();
			else
				u = X.iterator().next();
			HashSet<Integer> P1 = new HashSet<Integer>(P);
			P1.removeAll(G.AdjList.get(u));
			for (int v : P1) {
				//P.remove(v);
				TreeSet<Integer> Rnew = new TreeSet<Integer>(R);
				Rnew.add(v);
				
				//HashSet<Integer> Pnew = new HashSet<Integer>(P);
				//Pnew.retainAll(G.AdjList.get(v));
				//HashSet<Integer> Xnew = new HashSet<Integer>(X);
				//Xnew.retainAll(G.AdjList.get(v));
				//HashSet<String> K = BKWithPivot(G, Rnew, Pnew, Xnew);
				
				
				HashSet<Integer> NghOfv = G.AdjList.get(v);
				HashSet<Integer> Pnew = new HashSet<Integer>();
				HashSet<Integer> Xnew = new HashSet<Integer>();
				
				Pnew = SetOperations.intersect(P, NghOfv);
				Xnew = SetOperations.intersect(X, NghOfv);
				
				
				BKWithPivot(G, Rnew, Pnew, Xnew);
				P.remove(v);
				X.add(v);
				//C.addAll(K);
			}
			//return C;
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		if (args.length < 4) {

			System.out.println("usage : java program graph clique_set edge_set batch_size out_file");
			System.out
					.println("graph : represented as set of edges in a text file");
			System.out
					.println("clique_set: set of all maximal cliques of graph in a text file");
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
			
		spaceOverhead = 0;
		spaceOverhead5 = 0;
		
		maxDegree = 0;

		int total_number = 0;

		G = new Graph(graph, 1); //input file is in adjacency format
		System.out.println("Graph Reading completed");

		spaceOverhead += (G.getSize()*4);

		BufferedReader br = new BufferedReader(new FileReader(clique_set));
		//PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfile,true)));

		String line;

		CliqueSet = new HashSet<String>();
		
		largeCliqueCount = 0;


		while ((line = br.readLine()) != null) {
			TreeSet<Integer> S = new TreeSet<Integer>();
			StringBuilder sb = new StringBuilder();
			for (String s : line.split("\\s+")) {
				//sb.append(s + " ");
				S.add(Integer.parseInt(s));
				spaceOverhead += line.length();
			}
			for(int u : S)
				sb.append(u + " ");
			CliqueSet.add(sb.toString());
		}

		br.close();
		System.out.println("loading maximal cliques done!");

		br = new BufferedReader(new FileReader(edge_set));

		int count = 0;
		boolean eof_flag = false;
		int index = 0;
			
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_file,true)));
		out.println("OV");
		out.println();
		out.close();

		long cumulative_time = 0;

		while(true) {
	
			index = 0;
			count++;
			
			Set<int[]> batch = new HashSet<>();

			//File f = new File("batch");
			//FileWriter fw = new FileWriter(f);
			
			//batch_size = 3 * MathOperations.log2(maxDegree);
			
			//if(batch_size == 0)
			//	batch_size += 1;
			
			if(count%1000 == 0)
				System.out.println("Currently at iteration: " + count);

			while(index < batch_size){
				if((line = br.readLine()) != null){
					batch.add(new int[] {Integer.parseInt(line.split("\\s+")[0]), Integer.parseInt(line.split("\\s+")[1])});
					index++;
					if (count <= 50000) {
						int u = Integer.parseInt(line.split("\\s+")[0]);
						int v = Integer.parseInt(line.split("\\s+")[1]);
						G.addEdge(u, v);
					}
				}
				else{
					eof_flag = true;
					break;
				}
			}
			//fw.close();

			


			//new Ottosen("_e"+x);

			if(count == 50001) {
				System.out.println("Number of vertices of initial graph: " + G.numV());
				System.out.println("Number of edges of initial graph: " + G.numE());
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println(dateFormat.format(date));
				
				new Ottosen(batch);
				recompute_time = endTime - startTime;

				cumulative_time += recompute_time;
				
				int subSize = 0;
				int newSize = 0;
				total_number = newCliques.size() + Cdel.size();
				int total_size = newSize + subSize;
				out = new PrintWriter(new BufferedWriter(new FileWriter(out_file,true)));
				out.println(count + "," + batch_size + "," + total_number + "," + total_size +  "," + newtime + "," + subtime +  "," + recompute_time + "," + cumulative_time);
				out.close();
				break;

			}

			

			
			int n;
			//for (String s : Cdel) {
			//	String[] setofv = s.split("\\s+");
			//	n = setofv.length;
			//	subSize += (n * (n - 1) / 2);
			//	spaceOverhead4 += s.length();
			//	spaceOverhead -= s.length();	//as we need to remove the cliques from CliqueSet
			//}

			//for(String s : newCliques){
			//	String[] setofv = s.split("\\s+");
			//	n = setofv.length;
			//	newSize += (n*(n-1)/2);
			//	spaceOverhead3 += s.length();
			//	spaceOverhead += s.length();	//as we need to include the cliques in CliqueSet
			//}

			//System.out.println(count + ": # new cliques : " + newCliques.size() + "# subsumed cliques : " + Cdel.size() + " time: " + recompute_time);

			
			//total_number += newCliques.size();
			
			//spaceOverhead += spaceOverhead1 + spaceOverhead2 + spaceOverhead3 + spaceOverhead4;
			//spaceOverhead += spaceOverhead2;
			//System.out.println("new: " + newCliques.size());
			//System.out.println("subsumed: " + Cdel.size());

			long sc = (spaceOverhead + spaceOverhead1 + spaceOverhead2 + spaceOverhead3 + spaceOverhead4)/(1024*1024);
			
			if(cumulative_time >= 7200000)
				break;

			if(eof_flag)
				break;
			//if(20 == count)
			//	break;
		}

	}

	/**
	 * This procedure checkes whether the set A is maximal clique in G by seeing
	 * whether there exists any vertex v in V\A so that v is connected to all
	 * the vertices in A. If true for some v in V\A then A is not maximal.
	 * Otherwise, A is maximal
	 */
	public boolean maximal(Set<Integer> C, Graph G) {

		Set<Integer> N = nb(C, G);
		

		for (int v : N) {
			if (G.AdjList.get(v).containsAll(C)) {
				return false;
			}
		}

		return true;
	}

	public Set<Integer> nb(Set<Integer> C, Graph G) {
		Set<Integer> N = new HashSet<Integer>();

		//for (int u : C) {
		//	N.addAll(G.AdjList.get(u));
		//}
		//N.removeAll(C);
		
		for(int u : C){
		    HashSet<Integer> NghOfu = G.AdjList.get(u);
		    for(int v : NghOfu){
		        if(!C.contains(v)){
		            N.add(v);
		        }
		    }
		}

		return N;
	}
}
