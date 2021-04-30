package tools.velodrome;

public class VDLockState {

  private VDTransactionNode lockReleaseLastTxn;

  public VDLockState() {
    lockReleaseLastTxn = null;
  }

  public VDTransactionNode getLastTxnThatReleasedLock() {
    if(lockReleaseLastTxn != null){
      if(lockReleaseLastTxn.isDeleted())
        lockReleaseLastTxn = null;
    }
    return lockReleaseLastTxn;
  }

  public void setLastTxnThatReleasedLock(VDTransactionNode txn) {
    lockReleaseLastTxn = txn;
  }
}
