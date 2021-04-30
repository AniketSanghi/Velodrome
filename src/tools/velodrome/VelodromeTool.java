package tools.velodrome;

import java.lang.*;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import acme.util.Assert;
import acme.util.Util;
import acme.util.decorations.Decoration;
import acme.util.decorations.DecorationFactory;
import acme.util.decorations.DefaultValue;
import acme.util.decorations.NullDefault;
import acme.util.option.CommandLine;
import java.util.ArrayList;
import rr.annotations.Abbrev;
import rr.barrier.BarrierMonitor;
import rr.event.AccessEvent;
import rr.event.AcquireEvent;
import rr.event.FieldAccessEvent;
import rr.event.MethodEvent;
import rr.event.NewThreadEvent;
import rr.event.ReleaseEvent;
import rr.event.VolatileAccessEvent;
import rr.meta.ClassInfo;
import rr.state.ShadowLock;
import rr.state.ShadowThread;
import rr.state.ShadowVar;
import rr.tool.Tool;
import tools.util.Epoch;
import tools.util.VectorClock;

@Abbrev("VD")
public class VelodromeTool extends Tool {

  private Integer label;
  private VDTransactionGraph graph;
  Set<String> exclusionList = new HashSet<String>();

  private final Decoration<ShadowThread, VDThreadState> threadState =
    ShadowThread.makeDecoration(
      "VD::ThreadState",
      DecorationFactory.Type.MULTIPLE,
      new DefaultValue<ShadowThread, VDThreadState>() {
        @Override
        public VDThreadState get(ShadowThread shadowThread) {
          return new VDThreadState();
        }
      });

  private final Decoration<ShadowLock, VDLockState> lockState =
    ShadowLock.makeDecoration(
      "VD::LockState",
      DecorationFactory.Type.MULTIPLE,
      new DefaultValue<ShadowLock, VDLockState>() {
        @Override
        public VDLockState get(ShadowLock shadowLock) {
          return new VDLockState();
        }
      });

  public VelodromeTool(
    final String name,
    final Tool next,
    CommandLine commandLine
  ) {
    super(name, next, commandLine);
  }

  @Override
  public void init() {
    label  = 0;
    graph = new VDTransactionGraph();
    /* reading the exclusion list */
    Scanner inpFile;
    try{
      inpFile = new Scanner(new File("exclusionList"));
      while (inpFile.hasNext()) 
        exclusionList.add(inpFile.nextLine());
    }catch (FileNotFoundException ex) {
      System.out.println("There is a problem in File reading");
    }

  }

  @Override
  public void fini() {
    System.out.println("#---#" + graph.isCyclic());
    graph.dump();
  }

  @Override
  public void enter(MethodEvent me) {
    ShadowThread st = me.getThread();
    String methodName = me.getInfo().getName();
    VDThreadState currThreadState = threadState.get(st);
    
    if(
      !exclusionList.contains(methodName) &&
        currThreadState.getCurrentTxnNode() == null
    )
      enterTxn(st, methodName);

    super.enter(me);
  }

  @Override
  public void exit(MethodEvent me) {
    ShadowThread st = me.getThread();
    String methodName = me.getInfo().getName();
    VDThreadState currThreadState = threadState.get(st);

    if(
      !exclusionList.contains(methodName) &&
      currThreadState
        .getCurrentTxnNode()
        .getMethodName()
        .equals(me.getInfo().getName())
    )
      exitTxn(st);

    super.exit(me);
  }

  @Override
  public ShadowVar makeShadowVar(final AccessEvent event) {
    return new VDVarState();
  }

  @Override
  public void create(NewThreadEvent event) {
    final ShadowThread st = event.getThread();

    // TODO: If anything to be done

    super.create(event);
  }

  @Override
  public void acquire(final AcquireEvent event) {
    final ShadowThread st = event.getThread();
    final ShadowLock sl = event.getLock();

    VDThreadState currThreadState = threadState.get(st);
    VDTransactionNode currTxnNode = currThreadState.getCurrentTxnNode();

    if(currTxnNode == null) enterTxn(st, "UnaryAcquire");

    VDLockState currLockState = lockState.get(sl);
    graph.addEdge(currLockState.getLastTxnThatReleasedLock(), currTxnNode);

    if(currTxnNode == null) exitTxn(st);

    super.acquire(event);
  }

  @Override
  public void release(final ReleaseEvent event) {
    final ShadowThread st = event.getThread();
    final ShadowLock sl = event.getLock();

    VDThreadState currThreadState = threadState.get(st);
    VDTransactionNode currTxnNode = currThreadState.getCurrentTxnNode();

    if(currTxnNode == null) enterTxn(st, "UnaryRelease");

    VDLockState currLockState = lockState.get(sl);
    currLockState.setLastTxnThatReleasedLock(currTxnNode);
    lockState.set(sl, currLockState);

    if(currTxnNode == null) exitTxn(st);

    super.release(event);
  }

  static VDVarState ts_get_badVarState(ShadowThread st) {
    Assert.panic("Bad");
    return null;
  }

  static void ts_set_badVarState(
    ShadowThread st,
    VDVarState v
  ) {
    Assert.panic("Bad");
  }

  protected static ShadowVar getOriginalOrBad(
    ShadowVar original,
    ShadowThread st
  ) {
    final VDVarState savedState = ts_get_badVarState(st);
    if (savedState != null) {
      ts_set_badVarState(st, null);
      return savedState;
    } else {
      return original;
    }
  }

  @Override
  public void access(final AccessEvent event) {
    final ShadowThread st = event.getThread();
    final ShadowVar var = getOriginalOrBad(event.getOriginalShadow(), st);

    if (var instanceof VDVarState) { // shadow instanceof FTVarState
      VDThreadState currThreadState = threadState.get(st);
      VDTransactionNode currTxnNode = currThreadState.getCurrentTxnNode();
      VDVarState currVar = (VDVarState) var;

      if(currTxnNode == null) enterTxn(st, "UnaryAccess");

      if(event.isWrite()) {
        write(st, currTxnNode, currVar);
      } else {
        read(st, currTxnNode, currVar);
      }

      if(currTxnNode == null) exitTxn(st);

    } else {
      super.access(event);
    }
  }

  @Override
  public void volatileAccess(final VolatileAccessEvent event) {
    final ShadowThread st = event.getThread();

    // Extra work

    super.volatileAccess(event);
  }

  private void enterTxn(ShadowThread st, String methodName) {
    VDTransactionNode currTxnNode;

    synchronized (label) {
      currTxnNode = new VDTransactionNode(label, methodName, st.getTid() );
      label += 1;
    }

    VDThreadState currThreadState = threadState.get(st);

    graph.addEdge(currThreadState.getLastTxnNode(), currTxnNode);
    currThreadState.setCurrentTxnNode(currTxnNode);

    threadState.set(st, currThreadState);
  }

  private void exitTxn(ShadowThread st) {
    VDTransactionNode currTxnNode;

    VDThreadState currThreadState = threadState.get(st);

    currTxnNode = currThreadState.getCurrentTxnNode();
    currThreadState.setCurrentTxnNode(null);
    currThreadState.setLastTxnNode(currTxnNode);

    threadState.set(st, currThreadState);

    currTxnNode.txnFinished();
    graph.GarbageCollection(currTxnNode);
  }

  private void read(
    ShadowThread st,
    VDTransactionNode currTxnNode,
    VDVarState var
  ) {

    var.setLastTxnToReadForThread(st, currTxnNode);
    graph.addEdge(var.getLastTxnToWrite(), currTxnNode);
  }

  private void write(
    ShadowThread st,
    VDTransactionNode currTxnNode,
    VDVarState var
  ) {

    var.setLastTxnToWrite(currTxnNode);
    graph.addEdge(var.getLastTxnToWrite(), currTxnNode);

    VDTransactionNode[] values = var.getLastTxnPerThreadToReadAll();
    
    for(VDTransactionNode val: values) {
      graph.addEdge(val, currTxnNode);
    }
  }
}
