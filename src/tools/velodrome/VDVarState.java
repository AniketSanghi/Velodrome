package tools.velodrome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import rr.state.ShadowThread;
import rr.state.ShadowVar;

public class VDVarState implements ShadowVar {

  private VDTransactionNode lastTxnToWrite;
  private VDTransactionNode[] lastTxnPerThreadToRead;;

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

  public VDTransactionNode getLastTxnToWrite() {
    if(lastTxnToWrite != null){
      if(lastTxnToWrite.isDeleted())
        lastTxnToWrite = null;
    }
    return lastTxnToWrite;
  }

  public void setLastTxnToWrite(VDTransactionNode txn) {
    lastTxnToWrite = txn;
  }
}
