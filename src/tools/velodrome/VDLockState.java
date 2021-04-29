package tools.velodrome;

public class VDLockState {

  private VDTransactionNode lockReleaseLastTxn;

  public VDLockState() {
    lockReleaseLastTxn = null;
  }

  public VDTransactionNode getLastTxnThatReleasedLock() {
    return lockReleaseLastTxn;
  }

  public void setLastTxnThatReleasedLock(VDTransactionNode txn) {
    if(txn != null){
      if( !txn.lockPresentInList(this) )
        txn.addToLockList(this);
    }
    lockReleaseLastTxn = txn;
  }

  public void set2NullForGarbageCollection(VDTransactionNode txn){
    if( lockReleaseLastTxn == txn)
      lockReleaseLastTxn = null;
  }
  
}
