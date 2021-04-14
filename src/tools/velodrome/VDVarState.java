package tools.velodrome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import rr.state.ShadowThread;
import rr.state.ShadowVar;

public class VDVarState implements ShadowVar {

  private VDTransactionNode lastTxnToWrite;
  private Map<ShadowThread, VDTransactionNode> lastTxnPerThreadToRead;

  public VDVarState() {
    lastTxnToWrite = null;
    lastTxnPerThreadToRead = new HashMap<ShadowThread, VDTransactionNode>();
  }

  public VDTransactionNode getLastTxnToReadForThread(ShadowThread st) {
    return lastTxnPerThreadToRead.get(st);
  }

  public void setLastTxnToReadForThread(
    ShadowThread st,
    VDTransactionNode txn
  ) {
    lastTxnPerThreadToRead.put(st, txn);
  }

  public Collection<VDTransactionNode> getLastTxnPerThreadToReadAll() {
    return lastTxnPerThreadToRead.values();
  }

  public VDTransactionNode getLastTxnToWrite() {
    return lastTxnToWrite;
  }

  public void setLastTxnToWrite(VDTransactionNode txn) {
    lastTxnToWrite = txn;
  }
}
