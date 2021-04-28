package tools.velodrome;

import java.util.ArrayList;
import java.util.Arrays;
import rr.state.ShadowThread;
import rr.state.ShadowVar;

public class VDVarState implements ShadowVar {

  private VDTransactionNode lastTxnToWrite;
  private VDTransactionNode[] lastTxnPerThreadToRead;

  public VDVarState() {
    lastTxnToWrite = null;
    lastTxnPerThreadToRead = new VDTransactionNode[100];
  }

  public VDTransactionNode getLastTxnToReadForThread(ShadowThread st) {
    return lastTxnPerThreadToRead[st.getTid()];
  }

  public void setLastTxnToReadForThread(
    ShadowThread st,
    VDTransactionNode txn
  ) {
    lastTxnPerThreadToRead[st.getTid()]= txn;
  }

  public ArrayList<VDTransactionNode> getLastTxnPerThreadToReadAll() {
    return (ArrayList<VDTransactionNode>) Arrays.asList(lastTxnPerThreadToRead);
  }

  public VDTransactionNode getLastTxnToWrite() {
    return lastTxnToWrite;
  }

  public void setLastTxnToWrite(VDTransactionNode txn) {
    lastTxnToWrite = txn;
  }
}
