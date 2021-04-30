package tools.velodrome;

public class VDThreadState {

  private VDTransactionNode currentTxnNode;
  private VDTransactionNode lastTxnNode;

  public VDThreadState() {
    currentTxnNode = null;
    lastTxnNode = null;
  }

  public VDTransactionNode getCurrentTxnNode() {
    return currentTxnNode;
  }

  public void setCurrentTxnNode(VDTransactionNode node) {
    currentTxnNode = node;
  }

  public VDTransactionNode getLastTxnNode() {
    if(lastTxnNode != null){
      if(lastTxnNode.isDeleted())
        lastTxnNode = null;
    }
    return lastTxnNode;
  }

  public void setLastTxnNode(VDTransactionNode node) {
    lastTxnNode = node;
  }

}
