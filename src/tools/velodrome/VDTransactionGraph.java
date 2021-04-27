package tools.velodrome;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class VDTransactionGraph {

  private HashMap<VDTransactionNode, HashSet<VDTransactionNode> > graph;
  
  private HashMap<VDTransactionNode, Boolean> visited;

  public VDTransactionGraph() {
    graph = new HashMap<>();
  }

  /**
   * Add an edge to the transaction graph.
   * Also taking care of null boundary cases.
   * @param src Source Node
   * @param dest Destination Node
   */
  public synchronized void addEdge(
    VDTransactionNode src,
    VDTransactionNode dest
  ) {
    if(src == dest || src == null || dest == null)
      return;

    HashSet<VDTransactionNode> neighbours = graph.get(src);

    if(neighbours == null) neighbours = new HashSet<VDTransactionNode>();

    neighbours.add(dest);
    graph.put(src, neighbours);
  }

  public synchronized void addNode(VDTransactionNode node){
    graph.put(node, null);
    visited.put(node, false);
  }


  private synchronized void resetVisited(){

    for(VDTransactionNode node : visited.keySet())
      visited.replace(node, false);
      
  }

  /**
   * Check for a cycle in the graph
   */
  public boolean isAcyclic() {
    
    for(VDTransactionNode node : graph.keySet()){
      if(!visited.get(node) && dfsUtil(node)){
        resetVisited();
        return true;
      }
    }

    resetVisited();
    return false;
  }

  /**
   * Utility function for DFS traversal
   * @param node
   * @return
   */
  private boolean dfsUtil(VDTransactionNode node){

    HashSet<VDTransactionNode> neighbours = graph.get(node);

    if(neighbours == null || neighbours.isEmpty())
      return false;
    
    visited.replace(node, true);

    for(VDTransactionNode neighbour : neighbours ){
      if(visited.get(neighbour) || dfsUtil(neighbour))
        return true;
    }

    return false;
  }

  /**
   * Dump the whole graph into a file in DOT format
   */
  public void dump(){
    
    try {
      File outfile = new File("TXgraph.dot");
      
      if(outfile.createNewFile()){
        System.err.println("File created " + outfile.getName());
      }else{
        System.err.println("Erorr in file creation");
      }
      
      FileWriter fout = new FileWriter(outfile);
      fout.write("digraph G { \n");

      for (Map.Entry<VDTransactionNode, HashSet<VDTransactionNode>> entry : graph.entrySet()) {
        VDTransactionNode node1 = (VDTransactionNode)entry.getKey();
        HashSet<VDTransactionNode> edges = (HashSet<VDTransactionNode>)entry.getValue();

        fout.write("  " + node1.getLabel() + " [ label = \"" + node1.getMethodName() + "\" ];");
        
        if(edges == null)
          continue;

        for(VDTransactionNode node2 : edges ){
          fout.write("  " + node1.getLabel() + " -> " + node2.getLabel() + ";\n");
        }
      }

      fout.write("} \n");

      fout.close();
      
    } catch (IOException e) {
      System.out.println("An error occurred while dumping transaction graph.");
      e.printStackTrace();
    }
  }

}
