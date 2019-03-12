package Algorithm.DynamicMCE;
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
 
import Algorithm.Graph;
import utils.SetOperations;
 
public class StixDecremental {
 
    private static Graph g;
 
    private static HashSet<HashSet<Integer>> cliques;
 
    private static long sizeOfChange;
 
    public StixDecremental() {
        sizeOfChange = 0;
    }
 
    public static void main(String[] args) throws IOException {
 
        String graph_file = args[0];
        String clique_file = args[1];
        String edge_file = args[2];
        int batch_size = Integer.parseInt(args[3]);
        String output_file = args[4];
 
        g = new Graph(args[0]); // edge file is presented to construct initial the graph
         
        System.out.println("Graph reading complete");
 
        cliques = new HashSet<>();
 
        String line = "";
 
        BufferedReader clique_reader = new BufferedReader(new FileReader(clique_file));
 
        while ((line = clique_reader.readLine()) != null) {
            HashSet<Integer> S = new HashSet<Integer>();
            for (String s : line.split("\\s+")) {
                S.add(Integer.parseInt(s));
            }
            cliques.add(S);
        }
 
        clique_reader.close();
         
        System.out.println("Clique reading complete");
         
        BufferedReader edge_reader = new BufferedReader(new FileReader(edge_file));
         
        FileWriter output_writer = new FileWriter(output_file, true);   //for appending the contents
         
        output_writer.write("Stix-Decremental\n");
         
        output_writer.close();
         
        int num_iterations = 0;
        int lineindex = 0;
        boolean eof_indicator = false;
         
        long start, elapsed;
         
        long cumulative_time = 0;
         
        while(true) {
            num_iterations++;
            lineindex = 0;
             
            Set<int[]> batch = new HashSet<>();
            
            if(num_iterations%1000 == 0)
            	System.out.println("Currently at iteration: " + num_iterations);
             
            while (lineindex < batch_size) {
                if ((line = edge_reader.readLine()) != null) {
                    batch.add(new int[] {Integer.parseInt(line.split("\\s+")[0]), Integer.parseInt(line.split("\\s+")[1])});
                    lineindex++;
                    if(num_iterations <= 50000) {
                    	int u = Integer.parseInt(line.split("\\s+")[0]);
                    	int v = Integer.parseInt(line.split("\\s+")[1]);
                    	g.removeEdge(u, v);
                    }
                } else {
                    eof_indicator = true;
                    break;
                }
            }
             
            StixDecremental sd = new StixDecremental();
             
            
            if(num_iterations == 50001) {
            	start = System.currentTimeMillis();
            	System.out.println("no. of vertex of initial graph: " + g.numV());
				System.out.println("no. of edges of initial graph: " + g.numE());
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println(dateFormat.format(date));
            	sd.run(batch);
            	
            	elapsed = System.currentTimeMillis() - start;
                
                cumulative_time += elapsed;
                 
                output_writer = new FileWriter(output_file, true);
                 
                output_writer.write(num_iterations + "," + sizeOfChange + "," + elapsed + "," + cumulative_time + "\n");
                 
                output_writer.close();
                
                break;
            }
            
            
            
            
            if(cumulative_time >= 7200000 || eof_indicator)
            	break;
             
            //if(num_iterations == 1)
            //  break;
        }
 
    }
 
    private void run(Set<int[]> batch) {
         
        HashSet<HashSet<Integer>> cliques_copy = new HashSet<>();
         
        cliques_copy.addAll(cliques);
        int counter = 0;
        for (int[] edge : batch) {
        	counter++;
        	if(counter%10 == 0)
        		System.out.println(counter);
            int u = edge[0];
            int v = edge[1];
 
            List<HashSet<Integer>> cliques_containing_uv = findCliquesContainingEdge(u, v);
 
            for (HashSet<Integer> A : cliques_containing_uv) {
                HashSet<Integer> C1 = new HashSet<>(A);
                HashSet<Integer> C2 = new HashSet<>(A);
 
                C1.remove(u);
                C2.remove(v);
 
                g.removeEdge(u, v);
 
                if (MaxEval(C1)) {
                    cliques.add(C1);
                    sizeOfChange++;
                }
                if (MaxEval(C2)) {
                    cliques.add(C2);
                    sizeOfChange++;
                }
                cliques.remove(A);
                sizeOfChange++;
            }
 
        }
         
        HashSet<HashSet<Integer>> cliques_new = new HashSet<>();
        HashSet<HashSet<Integer>> cliques_deleted = new HashSet<>();
         
        cliques_new.addAll(cliques);
        cliques_new.removeAll(cliques_copy);
         
        cliques_deleted.addAll(cliques_copy);
        cliques_deleted.removeAll(cliques);
         
        sizeOfChange = cliques_new.size() + cliques_deleted.size();
         
         
 
    }
 
    private boolean MaxEval(HashSet<Integer> c) {
        Iterator<Integer> it = c.iterator();
        Collection<Integer> S = g.neighborsOf(it.next());
        while (it.hasNext()) {
            int w = it.next();
            S = SetOperations.intersect(S, g.neighborsOf(w));
        }
 
        if (S.size() > 0)
            return false;
        return true;
    }
 
    private List<HashSet<Integer>> findCliquesContainingEdge(int u, int v) {
 
        List<HashSet<Integer>> cliqueset = new ArrayList<>();
 
        for (HashSet<Integer> clique : cliques) {
            if (clique.contains(u) && clique.contains(v)) {
                cliqueset.add(clique);
            }
        }
 
        return cliqueset;
 
    }
 
}
