package tools.velodrome;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class VDTransactionGraph {

  private HashMap<VDTransactionNode, HashSet<VDTransactionNode> > graph;
  
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

/**
   * For garbage collection root is the given node
   * then the BFS traversal done to identify the
   * nodes with no parent these will be Garbage Collected
   * @param node
   * @return void (VDTransactionGraph will be modified)
   */
  public synchronized void GarbageCollection( VDTransactionNode root){

    Queue<VDTransactionNode> bfsQ = new LinkedList<VDTransactionNode>();
    bfsQ.add(root);

    while(!bfsQ.isEmpty()){
      
      VDTransactionNode parent = bfsQ.poll();                                   /** remove the parent and add the childern to the list */
      
      if( parent.isFinished() && parent.getNumberOfInEdges()==0 ){             /** enqueing the children */
        HashSet<VDTransactionNode> neighbours = graph.get(parent);
        
        if(neighbours != null){
          for(VDTransactionNode child: neighbours) {
            child.decNumberOfInEdges();
            bfsQ.add(child);
          }
        }

        graph.remove(parent);
        parent.setRefrences2Null();
      }
    }

  }

  /**
   * Check for a cycle in the graph
   */
  public synchronized boolean isCyclic() {
    
    HashSet<VDTransactionNode> visited = new HashSet<>();
    HashSet<VDTransactionNode> active = new HashSet<>();

    for(VDTransactionNode node : graph.keySet()){
      if(dfsUtil(node, visited, active)){
        return true;
      }
    }

    return false;
  }

  /**
   * Utility function for DFS traversal
   * @param node
   * @return
   */
  private boolean dfsUtil(VDTransactionNode node, 
    HashSet<VDTransactionNode> visited, 
    HashSet<VDTransactionNode> active){
    
    if(!visited.contains(node)){  
      HashSet<VDTransactionNode> neighbours = graph.get(node);

      visited.add(node);
      active.add(node);

      if(neighbours == null){
        // System.out.println("return false " + node.getLabel());
        active.remove(node);
        return false;
      }

      for(VDTransactionNode neighbour : neighbours ){
        if(!visited.contains(neighbour) && 
          dfsUtil(neighbour, visited, active)){
          return true;
        }else if(active.contains(neighbour)){
          return true;
        }
      }
    }
    active.remove(node);
    // System.out.println("return false " + node.getLabel());
    return false;
  }

  /**
   * Dump the whole graph into a file in DOT format
   */
  public synchronized void dump(){
    
    try {
      File outfile = new File("TXgraph.dot");
      
      if(outfile.exists())      
        outfile.delete();


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

        
        if(edges == null)
          continue;

        for(VDTransactionNode node2 : edges ){
          fout.write("  " + node1.getId() + " -> " + node2.getId() + ";\n");
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
