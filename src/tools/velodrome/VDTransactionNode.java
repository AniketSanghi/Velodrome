package tools.velodrome;

public class VDTransactionNode {

  final private int label;
  final private String methodInfo;
  private int numberOfInEdges;
  final private String txnId;

  private boolean txnDeleted = false;
  private boolean txnFinished = false;

  public VDTransactionNode(int myLabel, String methodName, int tid, String methodInfo) {
    label = myLabel;
    this.methodInfo = methodInfo;
    this.txnId = methodName + "__" + tid + "__" + myLabel;
    numberOfInEdges = 0;
  }

  public int getLabel(){ return label; }
  public String getMethodInfo(){ return methodInfo; }
  public String getId(){ return txnId; }

  public synchronized void incNumberOfInEdges(){ ++numberOfInEdges; }
  public synchronized void decNumberOfInEdges(){ --numberOfInEdges; }
  public synchronized int getNumberOfInEdges(){ return numberOfInEdges; }

  public void markTxnAsDeleted(){ txnDeleted = true; }
  public boolean isDeleted(){ return txnDeleted; }

  public void markTxnAsFinished(){ txnFinished = true; }
  public boolean isFinished(){ return txnFinished; }

  @Override
  public int hashCode() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (this.getClass() != o.getClass()) {
      return false;
    }
    VDTransactionNode other = (VDTransactionNode) o;
    return this.label == other.label;
  }

}
