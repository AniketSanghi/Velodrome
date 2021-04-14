package tools.velodrome;

public class VDTransactionNode {

  private int label;
  private int numberOfInEdges;

  public VDTransactionNode(int myLabel) {
    label = myLabel;
    numberOfInEdges = 0;
  }

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
