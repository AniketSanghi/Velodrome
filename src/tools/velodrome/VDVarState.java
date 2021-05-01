package tools.velodrome;

import rr.state.ShadowThread;
import rr.state.ShadowVar;

public class VDVarState implements ShadowVar {

  private VDTransactionNode lastTxnToWrite;
  private VDTransactionNode[] lastTxnPerThreadToRead;

  public VDVarState() {
    lastTxnToWrite = null;
    lastTxnPerThreadToRead = new VDTransactionNode[100];
  }

  public void setLastTxnToReadForThread(
    ShadowThread st,
    VDTransactionNode txn
  ) {
    lastTxnPerThreadToRead[st.getTid()] = txn;
  }

  public VDTransactionNode[] getLastTxnPerThreadToReadAll() {
    for(int i=0; i < 100; ++i){
      if(lastTxnPerThreadToRead[i] != null){
        if(lastTxnPerThreadToRead[i].isDeleted())
          lastTxnPerThreadToRead[i] = null;
      } 
    }
    return lastTxnPerThreadToRead;
  }

  public synchronized VDTransactionNode getLastTxnToWrite() {
    if(lastTxnToWrite != null && lastTxnToWrite.isDeleted())
        lastTxnToWrite = null;
    return lastTxnToWrite;
  }

  public synchronized void setLastTxnToWrite(VDTransactionNode txn) {
    lastTxnToWrite = txn;
  }
}
