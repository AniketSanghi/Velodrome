package tools.velodrome;

import java.util.Set;
import java.util.HashSet;

public class VDTransactionNode {

  private int label;
  private int tid;
  private String methodName;
  private String txnId;
  private VDThreadState txnThread;

  private volatile int numberOfInEdges = 0;
  private volatile boolean txnFinishedFlag = false;
  private Set<VDLockState> locksInTxn = new HashSet<VDLockState>();
  private Set<VDVarState> varsInTxn = new HashSet<VDVarState>();

  public VDTransactionNode(int myLabel, String inpmethodName, int inpTid, VDThreadState inpThreadState) {
    label = myLabel;
    tid = inpTid;
    methodName = inpmethodName;
    txnId = methodName + "__" + inpTid + "__" + myLabel;
    txnThread = inpThreadState;
  }

  public int getLabel(){ return label; }
  public int getTid() { return tid; }
  public String getMethodName(){ return methodName; }
  public String getId(){ return txnId; }
  
  public void incNumberOfInEdges() { ++numberOfInEdges; }
  public void decNumberOfInEdges() { --numberOfInEdges; }
  public int getNumberOfInEdges() { return numberOfInEdges; }

  public void setFinished() { txnFinishedFlag = true; }
  public boolean isFinished() { return txnFinishedFlag; }

  public void addToLockList(VDLockState lockSt) { locksInTxn.add(lockSt); }
  public void addToVarList(VDVarState varSt){ varsInTxn.add(varSt); }

  public boolean lockPresentInList(VDLockState lockSt) { return locksInTxn.contains(lockSt); }
  public boolean varPresentInList(VDVarState varSt) { return varsInTxn.contains(varSt); }

  public void setRefrences2Null(){
    for( VDVarState tempVar: varsInTxn)
      tempVar.set2NullForGarbageCollection(this);
    for( VDLockState tempLock: locksInTxn)
      tempLock.set2NullForGarbageCollection(this);
    txnThread.set2NullForGarbageCollection(this);
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
