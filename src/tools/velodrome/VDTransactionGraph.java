package tools.velodrome;

import java.util.HashMap;
import java.util.HashSet;

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

  public boolean isAcyclic() {

    return true;
  }
}
