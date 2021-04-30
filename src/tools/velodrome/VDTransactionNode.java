package tools.velodrome;

public class VDTransactionNode {

  private int label;
  private String methodName;
  private volatile int numberOfInEdges;
  private String txnId;

  private boolean txnDeleted = false;
  private boolean txnFinished = false;

  public VDTransactionNode(int myLabel, String methodName, int tid) {
    label = myLabel;
    this.methodName = methodName;
    this.txnId = methodName + "__" + tid + "__" + myLabel;
    numberOfInEdges = 0;
  }

  public int getLabel(){ return label; }
  public String getMethodName(){ return methodName; }
  public String getId(){ return txnId; }

  public void incNumberOfInEdges(){ ++numberOfInEdges; }
  public void decNumberOfInEdges(){ --numberOfInEdges; }
  public int getNumberOfInEdges(){ return numberOfInEdges; }

  public void txnDelete(){ txnDeleted = true; }
  public boolean isDeleted(){ return txnDeleted; }

  public void txnFinished(){ txnFinished = true; }
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
    if (this.label != other.label) {
      return false;
    }
    return true;
  }

}
