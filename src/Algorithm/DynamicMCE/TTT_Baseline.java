package Algorithm.DynamicMCE;
/**
 * This Algorithm generates all maximal cliques of an undirected graph G based on Tomita et al. with
 * title "The worst-case time complexity for generating all maximal cliques and computational
 * experiments"
 */

import java.util.*;

import Algorithm.Graph;
import Algorithm.MurmurHash3;
import utils.SetOperations;

import java.io.*;

public class TTT_Baseline
{

    private Set<Integer> CAND;
    private Set<Integer> FINI;
    private TreeSet<Integer> K;
    // private ArrayList<Integer> K;
    public HashSet<HashSet<Integer>> CLQ;
    private static HashSet<String> CliqueSet[];
    public int clqcnt;
    private static Graph G;

    static int count;

    public TTT_Baseline(Graph g, int id) throws IOException
    {

        TTT_Baseline.G = g;

        // FileWriter fw = new FileWriter(new File(ofname));

        // this.cw = fw;

        count = 1;

        CAND = new HashSet<Integer>();
        FINI = new HashSet<Integer>();
        K = new TreeSet<Integer>();
        // K = new ArrayList<Integer>();
        CLQ = new HashSet<HashSet<Integer>>();
        clqcnt = 1;
        for (int v : G.AdjList.keySet())
        {
            CAND.add(v);
        }
        // System.out.println("Generating all Maximal Cliques");
        /* 1: *//* expand(SUBG, CAND); */

        CliqueSet[id] = new HashSet<>();

        expand(K, CAND, FINI, id);
    }

    public void expand(TreeSet<Integer> K, Set<Integer> CAND, Set<Integer> FINI, int id) throws IOException
    {
        if (CAND.isEmpty() && FINI.isEmpty())
        {

//            StringBuilder sb = new StringBuilder();
//
//            for (int v : K)
//            {
//                sb.append(v + " ");
//            }
            
            String cliquestring = K.toString().replace("[", "").replace("]", "").replace(",", "");
            /* Computing signature of the clique */
            //byte[] key = sb.toString().getBytes();
            //long[] hash =
            //{ MurmurHash3.MurmurHash3_x64_64(key, 0), 0 };
            //BitSet b = BitSet.valueOf(hash);

            CliqueSet[id].add(cliquestring);

            // System.out.println(count + ":" + K);
            count++;
            //if (count % 1000 == 0)
            //    System.out.println(count);
            return;
        }
        int u = find_u(CAND, FINI);
        
        Set<Integer> NghOfu = Ngh(u);

        Iterator<Integer> candit = CAND.iterator();

        while(candit.hasNext())
        {
            int q = candit.next();
            if (!NghOfu.contains(q)){
            
            K.add(q);
            
            Set<Integer> NghOfq = Ngh(q);
            HashSet<Integer> CANDq = new HashSet<Integer>();
            HashSet<Integer> FINIq = new HashSet<Integer>();

            CANDq = SetOperations.intersect(CAND, NghOfq);
            FINIq = SetOperations.intersect(FINI, NghOfq);


           // HashSet<Integer> CANDq = new HashSet<Integer>(CAND);
            //CANDq.retainAll(Ngh(q)); // Intersection of CAND and Ngh(q)

            //HashSet<Integer> FINIq = new HashSet<Integer>(FINI);
            //FINIq.retainAll(Ngh(q)); // Intersection of FINI and Ngh(q)

            expand(K, CANDq, FINIq, id);

            candit.remove(); // CAND - {q}
            
            K.remove(q);

            FINI.add(q); // FINI union {q}
        }
        }
    }

    public Set<Integer> Ngh(int u)
    {
        return G.AdjList.get(u);
    }

    public int find_u(Set<Integer> CAND, Set<Integer> FINI /*
                                                            * Set<String> SUBG
                                                            */)
    {

        int size = -1;
        int v = 0;

        // HashSet<Integer> SUBG = new HashSet<Integer>(CAND);
        // SUBG.addAll(FINI);
        // if(CAND.size() > 0)
        // return CAND.iterator().next();
        // else
        // return FINI.iterator().next();

        for (int u : CAND)
        {
            HashSet<Integer> Q = new HashSet<Integer>();
            Q = SetOperations.intersect(CAND, Ngh(u));
            int tmp = Q.size();
            if (size <= tmp)
            {
                size = tmp;
                v = u;
            }
        }

        for (int u : FINI)
        {
            HashSet<Integer> Q = new HashSet<Integer>();
            Q = SetOperations.intersect(CAND, Ngh(u));
            int tmp = Q.size();
            if (size <= tmp)
            {
                size = tmp;
                v = u;
            }
        }

        // v = SUBG.iterator().next();
        // for (int u : SUBG) {
        // HashSet<Integer> Q = new HashSet<Integer>();
        // Q.addAll(Ngh(u));
        // Q.retainAll(CAND);
        // int tmp = Q.size();
        // if (size <= tmp) {
        // size = tmp;
        // v = u;
        // }
        // }

        return v;

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws NumberFormatException, IOException
    {
        // TODO Auto-generated method stub

        String graph = args[0];
        String cliqueset = args[1];
        String edge_set = args[2];
        int batch_size = Integer.parseInt(args[3]); // batch count in
                                                    // incremental computation
        String out_file = args[4];

        

        Graph G = new Graph(graph, 1); // for adjacency list format


        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(edge_set)));
        lnr.skip(Long.MAX_VALUE);
        int lines = lnr.getLineNumber() + 1;
        lnr.close();

        CliqueSet = new HashSet[2];

        BufferedReader cbr = new BufferedReader(new FileReader(cliqueset));
        String line;

        CliqueSet[0] = new HashSet<>();
        CliqueSet[1] = new HashSet<>();

        // adding the existing cliqueset to a container. Each clique is stored
        // as a string of vertex ids sorted
        while ((line = cbr.readLine()) != null)
        {

            TreeSet<Integer> T = new TreeSet<Integer>();
            StringBuilder sb = new StringBuilder();
            for (String s : line.split("\\s+"))
            {
                T.add(Integer.parseInt(s));
            }
            //for (int v : T)
            //{
            //   sb.append(v + " ");
            //}
            
            String cliquestring = T.toString().replace("[", "").replace("]", "").replace(",", "");
            /* Computing signature of the clique */
            CliqueSet[0].add(cliquestring);

        }
        System.out.println("Intial CliqueSet Reading Complete!!");
        cbr.close();

        int id = 0;
        int index = 0;
        boolean eof_flag = false;

        BufferedReader ebr = new BufferedReader(new FileReader(edge_set));

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));

        out.println();
        out.println("Algorithm-Baseline-TTT");
        out.println("Total Number - (new + subsumed)\t Computation time(ms)");
        out.println();
        out.close();
        
        int turn = 1;

        while (true)
        {
            index = 0;
            id++;

            while (index < batch_size)
            {
                if ((line = ebr.readLine()) != null)
                {
                    int u = Integer.parseInt(line.split(" ")[0]);
                    int v = Integer.parseInt(line.split(" ")[1]);
                    G.addEdge(u, v);
                    index++;
                }
                else
                {
                    eof_flag = true;
                    break;
                }
            }

            System.out.println("Updating graph complete!!");

            System.out.println("Maximal Clique Computation start!!");
            long t1 = System.currentTimeMillis();
            new TTT_Baseline(G, turn);
            long compute_time = System.currentTimeMillis() - t1;
            System.out.println("Maximal Clique Computation end!!");

            System.out.println("Symmetric Difference Computation start!!");
            long t2 = System.currentTimeMillis();
            /* computing the symmetric difference */
            Set<String> symmetricDiff = new HashSet<>(CliqueSet[1-turn]);
            symmetricDiff.addAll(CliqueSet[turn]);
            //Set<String> tmp = new HashSet<>(CliqueSet[id - 1]);
            HashSet<String> tmp1 = SetOperations.intersect(CliqueSet[1-turn], CliqueSet[turn]);
            //tmp.retainAll(CliqueSet[turn];
            symmetricDiff.removeAll(tmp1);
            System.out.println("Symmetric Difference Computation end!!");
            turn = 1 - turn;
            CliqueSet[turn].clear();

            long symdiff_time = System.currentTimeMillis() - t2;

            long total_time = compute_time + symdiff_time;
            
            out = new PrintWriter(new BufferedWriter(new FileWriter(out_file, true)));

            out.println(symmetricDiff.size() + "\t" + total_time);
            out.close();
            System.out.println(id + ": " + symmetricDiff.size() + "\t" + total_time);
            if (eof_flag)
                break;

        }

        ebr.close();

    }

}
