package tools.velodrome;

public class VDLockState {

  private VDTransactionNode lockReleaseLastTxn;

  public VDLockState() {
    lockReleaseLastTxn = null;
  }

  public synchronized VDTransactionNode getLastTxnThatReleasedLock() {
    if(lockReleaseLastTxn != null && lockReleaseLastTxn.isDeleted())
        lockReleaseLastTxn = null;
    return lockReleaseLastTxn;
  }

  public synchronized void setLastTxnThatReleasedLock(VDTransactionNode txn) {
    lockReleaseLastTxn = txn;
  }
}
