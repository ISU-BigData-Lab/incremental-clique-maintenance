package Algorithm.MCE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import Algorithm.Graph;
import utils.SetOperations;

public class CN {
	
	private int N;
	private Graph g;
	private int[] V;
	private int[] S;
	private int[] T;
	private HashMap<Integer, Integer> VtoI;	//which vertex at which index in the sorted (based on degree) array 
	
	private static Collection<TreeSet<Integer>> C;
	
	//private FileWriter cw;
	
	static int count;

	public CN(Graph g) {
		// TODO Auto-generated constructor stub
		this.g = g;
		count = 1;
		VtoI = new HashMap<Integer, Integer>();
		//try {
		//	cw = new FileWriter(new File(ofname));
		//} catch (IOException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		LinkedHashMap<Integer, Integer> D = new LinkedHashMap<Integer, Integer>();
		
		for(int u : g.AdjList.keySet()){
			D.put(u, g.AdjList.get(u).size());
		}
		
		V = sortVertices(D);	//sorted vertices based on degree. i.e, d(V[1]) <= d(V[2]) <= d(V[3]) <= ...
		
		N = g.AdjList.size();	//number of vertices in the graph
		//System.out.println(N);
		
		C = new HashSet<TreeSet<Integer>>();
		
		S = new int[N+1];
		T = new int[N+1];
		Arrays.fill(S, 0);
		Arrays.fill(T, 0);
		HashSet<Integer> c = new HashSet<Integer>(); //represents a clique
		
		c.add(1);
		
		//Update(V[2],C);
		Update(2,c);
		
		
	}

	private void Update(int i, HashSet<Integer> c) {
		// TODO Auto-generated method stub
		//System.out.println(i + "!");
		//System.out.println(c);
		if(i == N+1){
			//print out a new clique C
			//System.out.println("final: ");
			TreeSet<Integer> T = new TreeSet<Integer>();
			for(int idx : c){
				T.add(V[idx]);
				//System.out.print(V[idx] + " ");
			}
			C.add(T);
			if(C.size()%100000 == 0)
				System.out.println(C.size());
			//System.out.println(count + ":" + T);
			count++;
			//System.out.println();
			/*
			for(int u : c){
				try {
					cw.write(u + " ");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				cw.write("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		else{
//========================================================================================================================			
			HashSet<Integer> C_minus_Ni = new HashSet<Integer>(c);	//beginning of (1)
			HashSet<Integer> Ni = new HashSet<Integer>();
			//HashSet<Integer> NN = new HashSet<Integer>(g.AdjList.get(V[i]));	//changed
			HashSet<Integer> NN = g.AdjList.get(V[i]);
			for(int u : NN){
				Ni.add(VtoI.get(u));	//Computing C - N(i)
			}
			C_minus_Ni.removeAll(Ni);
			if(C_minus_Ni.size() > 0){	//checking if C-N(i) is not null
				//System.out.println((i+1) + "*");
				Update(i+1, c);
			}		//end of (1)
//=========================================================================================================================			
			HashSet<Integer> C_intersect_Ni = new HashSet<Integer>(c);	//beginning of (2)
			HashSet<Integer> X = new HashSet<Integer>();
			for(int u : g.AdjList.get(V[i])){
				X.add(VtoI.get(u));
			}
			//C_intersect_Ni.retainAll(X);	//Computing C intersection N(i)	//CHANGED
			C_intersect_Ni = SetOperations.intersect(C_intersect_Ni, X);
			int size_c_int_ni = C_intersect_Ni.size();	//size of this intersection
			HashSet<Integer> yset1 = new HashSet<Integer>();
			HashSet<Integer> yset2 = new HashSet<Integer>();
			//System.out.println(c + " intersect N(" + i + "): " + C_intersect_Ni);
			for(int x : C_intersect_Ni){
				HashSet<Integer> Nx = new HashSet<Integer>(g.AdjList.get(V[x]));
				//System.out.println("x: " + x);
				//System.out.println("V[x]: " + V[x]);
				//System.out.println(Nx);
				for(int id : c){
					Nx.remove(V[id]);
				}
				Nx.remove(V[i]);
				//System.out.println("N(" + x + "): " + Nx);
				//System.out.println("Computation of T[y]:Start");
				for(int u : Nx){
					int y = VtoI.get(u);
					
					T[y] = T[y]+1;
					yset1.add(y);
				}
				
				//for(int y : yset1)
					//System.out.println("T[" + y + "]: " + T[y]);
				//System.out.println("Computation of T[y]:End");
			}		//end of (2)
//==========================================================================================================================			
			for(int x:C_minus_Ni){	//beginning of (3)
				HashSet<Integer> Nx = new HashSet<Integer>(g.AdjList.get(V[x]));
				for(int id : c){
					Nx.remove(V[id]);
				}
				for(int u:Nx){
					int y = VtoI.get(u);
					S[y] = S[y]+1;
					yset2.add(y);
				}
			}
			boolean FLAG = true;	//end of (3)
//==========================================================================================================================			
			//maximality test
			//System.out.println("current index: " + i);
			//System.out.println("check for maximality: " + c);
			HashSet<Integer> Ni_minus_C = new HashSet<Integer>(Ni);	// beginning of (4)
			Ni_minus_C.removeAll(c);	//N(i)-C  
			//System.out.println("Ni-C: " + Ni_minus_C);
			
			//System.out.println("size of C intersection Ni: " + size_c_int_ni);
			for(int y : Ni_minus_C){
				//System.out.println("T[" + y + "]: " + T[y]);
				if((y < i) && (T[y] == size_c_int_ni)){	// end of (4)
					FLAG = false;
					//System.out.println(c + " intersection " + " N(" + i + ") union " + i + " is not clique of G" + i );
				}
				if(FLAG == false)
					break;
			}	//end of (4)
			//if(FLAG == true)
				//System.out.println(c + " intersection " + " N(" + i + ") union " + i + " is clique of G" + i );
//============================================================================================================================			
			//===============LEXICO TEST SEEMS CORRECT============================
			//lexico test	
			int p = C_minus_Ni.size();	//beginning of (5)
			//System.out.println(p);
			int[] j = new int[p+1];
			int id = 1;
			j[0] = 0;
			for(int y: C_minus_Ni){
				j[id++] = y;
			}
			if(p > 0)	//fix
				Arrays.sort(j);	//j[1] < j[2] < ... < j[p]; end of (5)
			//System.out.println("C: " + c);
			//System.out.println("j array:");
			//for(int l=0; l<=p; l++)
			//	System.out.print(" " + j[l]);
			//System.out.println();
			//System.out.println("i: " + i);
			//System.out.println("C intersect N(" + i + "): " + C_intersect_Ni + "in G"+i);
			//System.out.println("C intersect Ni: " + size_c_int_ni);
//=============================================================================================================================
			for(int k=1; k<= p; k++){	//beginning of (6)
				//System.out.println("k: " + k);
				HashSet<Integer> t = new HashSet<Integer>(g.AdjList.get(V[j[k]]));
				for(int idx : c){
					t.remove(V[idx]);
				}
				for(int u : t){
					int y = VtoI.get(u);
					//System.out.println("y: " + y);
					//System.out.println("T[y]: " + T[y]);
					//System.out.println("S[y]: " + S[y]);
					if((y < i) && (T[y] == size_c_int_ni)){
						if(y >= j[k]){
							//System.out.println("Line 213");
							S[y] = S[y]-1;
						}
						else{
							//check that jk is the first vertex which satisfies y < jk : start
							boolean jk_isfirst = false;
							if(j[k] > y)
								jk_isfirst = true;
							for(int m = 1; m < k; m++){
								if(j[m] > y)
									jk_isfirst = false;
							}
							//check that jk is the first vertex which satisfies y < jk : end
							if(jk_isfirst){
								if((S[y]+k-1 == p) && (y >= j[k-1])){
									//System.out.println("Line 220");
									FLAG = false;
								}
							}
						}
					}
				}
			}//end of (6)
			
//================================================================================================================================			
			//case S(y) = 0
			if(C_intersect_Ni.size() > 0){	//beginning of (7)
				// for each vertex y not in the intersection of (C and N(i)) such that y < i and 
				//T[y] = Size of (C intersection N(i)) and S[y] = 0.
				//Instead of trying with all y in V - C - i, it is sufficient to trying with all y in the neighborhood
				//of all the vertices in the intersection of C and N(i).
				for(int x : C_intersect_Ni){
					HashSet<Integer> Nx = new HashSet<Integer>(g.AdjList.get(V[x]));
					for(int idx : c){
						Nx.remove(V[idx]);
					}
					Nx.remove(V[i]);
					for(int u : Nx){
						int y = VtoI.get(u);
						//System.out.println("LINE 242: y: " + y);
						//System.out.println("LINE 243: T[y]: " + T[y]);
						//System.out.println("LINE 244: S[y]: " + S[y]);
						if((y < i) && (T[y] == size_c_int_ni) && (S[y] == 0)){
							//System.out.println("LINE 246");
							if(j[p] < y)
								FLAG = false;	// c is not lexico. largest.
						}
				
					}
				}
				
			}
			else if(j[p] < i-1)
				FLAG = false;	// c is not lexico. largest.//end of (7)
//===================================================================================================================================			
			
			//reinitialize S and T
			/*
			for(int x : C_intersect_Ni){	//beginning of (8)
				HashSet<Integer> Nx = new HashSet<Integer>(g.AdjList.get(V[x]));
				for(int idx : c){
					Nx.remove(V[idx]);
				}
				Nx.remove(V[i]);
				for(int u : Nx){
					int y = VtoI.get(u);
					T[y] = 0;
				}
			}	//end of (8)
			for(int x:C_minus_Ni){	//beginning of (9)
				HashSet<Integer> Nx = new HashSet<Integer>(g.AdjList.get(V[x]));
				for(int idx : c){
					Nx.remove(V[idx]);
				}
				for(int u:Nx){
					int y = VtoI.get(u);
					S[y] = 0;
				}
			}	//end of (9)
			*/
			for(int y : yset1)
				T[y] = 0;
			for(int y : yset2)
				S[y] = 0;
//====================================================================================================================================			
			if(FLAG){	//beginning of (10)
				HashSet<Integer> SAVE = new HashSet<Integer>(c);
				SAVE.removeAll(Ni);
				//System.out.println(c + "c**");
				//c.retainAll(Ni);	//changed
				c = SetOperations.intersect(c, Ni); 
				c.add(i);
				//System.out.println(c + " maximal and lex passed");
				/*check if the c after adding the current vertex contains an already processed edge. In that case, don't call update*/
				//System.out.println(i + "**");
				//System.out.println(CC + "**");
				//System.out.println(c + "c**");
				Update(i+1,c);
				c.remove(i);
				c.addAll(SAVE);
			}	//end of (10)
//=====================================================================================================================================			
		}
	}
	
	public Collection<TreeSet<Integer>> collect(){
		return C;
	}

	private int[] sortVertices(LinkedHashMap<Integer, Integer> d) {
		// TODO Auto-generated method stub
		List<Map.Entry<Integer, Integer>> entries = new ArrayList<Map.Entry<Integer, Integer>>(d.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<Integer, Integer>>(){
			public int compare(Map.Entry<Integer, Integer> a, Map.Entry<Integer, Integer> b) {
				return a.getValue().compareTo(b.getValue());
			}
		});
		int[] T = new int[d.size()+1];
		int i = 1;
		for(Map.Entry<Integer, Integer> entry : entries){
			int u = entry.getKey();
			T[i] = u;
			VtoI.put(u, i);
			i++;
		}
		//System.out.println(VtoI);
		return T;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long t1 = System.currentTimeMillis();		
		new CN(new Graph(args[0]));
		long elapsed = System.currentTimeMillis();
		System.out.println("Number of Maximal Cliques: " + C.size());
		System.out.println("Sequential Time: " + elapsed/1000 + " sec.");

	}

}
