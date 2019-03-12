package Algorithm.MCE;

import java.util.HashSet;
import java.util.TreeSet;

import Algorithm.Graph;

/*Bron-Kerbosch algorithm with pivoting. Here we selected first vertex in P U X as pivot. In this algorithm there is no pivot selection strategy.
 * pivot selecting strategy gives better runtime as in Tomita.
 * 
 * https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm
 * */

/*
 * Actual implementation of Bron-Kerbosch assumes the graph to be input as Adjacency matrix, but we consider graphs in either edge list or adjacency list format. 
 * So we do not adopt that implementation as in http://dl.acm.org/citation.cfm?doid=362342.362367
 * */

public class BKS {
	
		private Graph G;
		static int count;

		public BKS(Graph g){
			this.G = g;
			TreeSet<Integer> R = new TreeSet<Integer>();
			HashSet<Integer> X = new HashSet<Integer>();
			HashSet<Integer> P = new HashSet<Integer>(G.AdjList.keySet());
			
			count = 1;
			
			extend(R,P,X);
			
		}
		
		private void extend(TreeSet<Integer> R, HashSet<Integer> P, HashSet<Integer> X){
			if (P.isEmpty() && X.isEmpty()) {
				/*new maximal clique found*/
				System.out.println(count + ": " + R);
				count++;

			} else {

				int u;	//pivot
				if(!P.isEmpty())
					u = P.iterator().next();
				else
					u = X.iterator().next();
				HashSet<Integer> P1 = new HashSet<Integer>(P);
				P1.removeAll(G.AdjList.get(u));
				for (int v : P1) {
					TreeSet<Integer> Rnew = new TreeSet<Integer>(R);
					Rnew.add(v);
					HashSet<Integer> Pnew = new HashSet<Integer>(P);
					Pnew.retainAll(G.AdjList.get(v));
					HashSet<Integer> Xnew = new HashSet<Integer>(X);
					Xnew.retainAll(G.AdjList.get(v));
					extend(Rnew, Pnew, Xnew);
					P.remove(v);
					X.add(v);
				}
			}
		}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new BKS(new Graph("Input/g_70_08"));

	}

}
