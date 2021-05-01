package tools.velodrome;

import acme.util.option.CommandLineOption;
import java.lang.*;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;

import acme.util.Assert;
import acme.util.decorations.Decoration;
import acme.util.decorations.DecorationFactory;
import acme.util.decorations.DefaultValue;
import acme.util.option.CommandLine;
import acme.util.io.XMLWriter;
import rr.annotations.Abbrev;
import rr.event.AccessEvent;
import rr.event.AcquireEvent;
import rr.event.MethodEvent;
import rr.event.NewThreadEvent;
import rr.event.ReleaseEvent;
import rr.event.VolatileAccessEvent;
import rr.state.ShadowLock;
import rr.state.ShadowThread;
import rr.state.ShadowVar;
import rr.tool.Tool;

@Abbrev("VD")
public class VelodromeTool extends Tool {

  private VDTransactionGraph graph;
  Set<String> exclusionList = new HashSet<String>();
  Set<String> methodsViolatingAtomicity = new HashSet<>();
  Integer[] nestingLevelCounterPerThread = new Integer[100];

  private final CommandLineOption<String> exclusionListFileName =
    CommandLine.makeString(
      "exclusionList",
      "",
      CommandLineOption.Kind.EXPERIMENTAL,
      "Get the exclusion list"
    );

  private final CommandLineOption<String> outputExclusionListFileName =
    CommandLine.makeString(
      "outputExclusionList",
      "outExclusionList.txt",
      CommandLineOption.Kind.EXPERIMENTAL,
      "Where to print the current exclusion list"
    );

  private final CommandLineOption<Boolean> isMicroBenchMark =
    CommandLine.makeBoolean(
      "isMicroBenchMark",
      false,
      CommandLineOption.Kind.EXPERIMENTAL,
      "Dot files to be printed in micro-benchmarks only"
    );

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

    commandLine.add(exclusionListFileName);
    commandLine.add(outputExclusionListFileName);
    commandLine.add(isMicroBenchMark);
  }

  @Override
  public void init() {
    graph = new VDTransactionGraph(isMicroBenchMark.get());
    /* reading the exclusion list */
    Scanner inpFile;
    try {
      inpFile = new Scanner(new File(exclusionListFileName.get()));
      while (inpFile.hasNext()) 
        exclusionList.add(inpFile.nextLine());
    } catch (FileNotFoundException ex) {
      System.out.println("There is a problem in File reading");
    }
  }

  @Override
  public void printXML(XMLWriter xml) {
    try {
      FileWriter fw = new FileWriter(outputExclusionListFileName.get());
   
      for (String method : methodsViolatingAtomicity) {
        fw.write(method + "\n");
      }
    
      fw.close();
    }
    catch (IOException ioe) {
      System.out.println("There is a problem in File Writing");
    }
  }

  @Override
  public void enter(MethodEvent me) {
    ShadowThread st = me.getThread();
    String methodName = me.getInfo().getName();
    String methodInfo = me.getInfo().getKey();
    VDThreadState currThreadState = threadState.get(st);

    if(!exclusionList.contains(methodInfo)) {
      if(currThreadState.getCurrentTxnNode() == null) {
        nestingLevelCounterPerThread[st.getTid()] = 0;
        enterTxn(st, methodName, methodInfo);
      } else {
        nestingLevelCounterPerThread[st.getTid()] += 1;
      }
    }

    super.enter(me);
  }

  @Override
  public void exit(MethodEvent me) {
    ShadowThread st = me.getThread();
    String methodInfo = me.getInfo().getKey();

    if(!exclusionList.contains(methodInfo)) {
      if(nestingLevelCounterPerThread[st.getTid()] == 0) exitTxn(st);
      else nestingLevelCounterPerThread[st.getTid()] -= 1;
    }

    super.exit(me);
  }

  @Override
  public ShadowVar makeShadowVar(final AccessEvent event) {
    return new VDVarState();
  }

  @Override
  public void create(NewThreadEvent event) {
    super.create(event);
  }

  @Override
  public void acquire(final AcquireEvent event) {
    final ShadowThread st = event.getThread();
    final ShadowLock sl = event.getLock();

    VDThreadState currThreadState = threadState.get(st);
    VDTransactionNode currTxnNode = currThreadState.getCurrentTxnNode();
    VDLockState currLockState = lockState.get(sl);

    if(currTxnNode == null) {
      
      List<VDTransactionNode> mergeInputNodes = new ArrayList<VDTransactionNode>();
      mergeInputNodes.add(currThreadState.getLastTxnNode());
      mergeInputNodes.add(currLockState.getLastTxnThatReleasedLock());

      VDTransactionNode happensAfterNode =
        graph.merge(mergeInputNodes, "UnaryAcquire", st.getTid());
      
      currThreadState.setLastTxnNode(happensAfterNode);
      threadState.set(st, currThreadState);
    }
    else {
      graph
        .addEdge(
          currLockState.getLastTxnThatReleasedLock(),
          currTxnNode,
          methodsViolatingAtomicity
        );
    }

    super.acquire(event);
  }

  @Override
  public void release(final ReleaseEvent event) {
    final ShadowThread st = event.getThread();
    final ShadowLock sl = event.getLock();

    VDThreadState currThreadState = threadState.get(st);
    VDTransactionNode currTxnNode = currThreadState.getCurrentTxnNode();
    VDLockState currLockState = lockState.get(sl);

    if(currTxnNode == null) {
      currLockState.setLastTxnThatReleasedLock(currThreadState.getLastTxnNode());
    }
    else {
      currLockState.setLastTxnThatReleasedLock(currTxnNode);
    }
    lockState.set(sl, currLockState);

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

      if(event.isWrite()) {
        write(st, currTxnNode, currVar);
      } else {
        read(st, currTxnNode, currVar);
      }

    } else {
      super.access(event);
    }
  }

  @Override
  public void volatileAccess(final VolatileAccessEvent event) {
    final ShadowThread st = event.getThread();
    final ShadowVar var = getOriginalOrBad(event.getOriginalShadow(), st);

    if (var instanceof VDVarState) { // shadow instanceof FTVarState
      VDThreadState currThreadState = threadState.get(st);
      VDTransactionNode currTxnNode = currThreadState.getCurrentTxnNode();
      VDVarState currVar = (VDVarState) var;

      if(event.isWrite()) {
        write(st, currTxnNode, currVar);
      } else {
        read(st, currTxnNode, currVar);
      }

    } else {
      super.volatileAccess(event);
    }
  }

  private void enterTxn(ShadowThread st, String methodName, String methodInfo) {
    VDTransactionNode currTxnNode;

    currTxnNode =
      graph.IncLabelAndGetNewTxn(methodName, st.getTid(), methodInfo);

    VDThreadState currThreadState = threadState.get(st);

    graph.addEdge(currThreadState.getLastTxnNode(), currTxnNode, methodsViolatingAtomicity);
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

    currTxnNode.markTxnAsFinished();

    // Comment below line to not do GC
    graph.GarbageCollection(currTxnNode);
  }

  private void read(
    ShadowThread st,
    VDTransactionNode currTxnNode,
    VDVarState var
  ) {

    if (currTxnNode == null) {
      VDThreadState currThreadState = threadState.get(st);

      List<VDTransactionNode> mergeInputNodes = new ArrayList<VDTransactionNode>();
      mergeInputNodes.add(currThreadState.getLastTxnNode());
      mergeInputNodes.add(var.getLastTxnToWrite());

      VDTransactionNode happensAfterNode =
        graph.merge(mergeInputNodes, "UnaryRead", st.getTid());
      
      var.setLastTxnToReadForThread(st, happensAfterNode);

      currThreadState.setLastTxnNode(happensAfterNode);
      threadState.set(st, currThreadState);
    } else {
      var.setLastTxnToReadForThread(st, currTxnNode);
      graph
        .addEdge(var.getLastTxnToWrite(), currTxnNode, methodsViolatingAtomicity);
    }
  }

  private void write(
    ShadowThread st,
    VDTransactionNode currTxnNode,
    VDVarState var
  ) {

    if (currTxnNode == null) {
      VDThreadState currThreadState = threadState.get(st);

      List<VDTransactionNode> mergeInputNodes = new ArrayList<VDTransactionNode>();
      mergeInputNodes.add(currThreadState.getLastTxnNode());
      mergeInputNodes.add(var.getLastTxnToWrite());

      VDTransactionNode[] values = var.getLastTxnPerThreadToReadAll();

      mergeInputNodes.addAll(Arrays.asList(values));

      VDTransactionNode happensAfterNode =
        graph.merge(mergeInputNodes, "UnaryWrite", st.getTid());
      
      var.setLastTxnToWrite(happensAfterNode);

      currThreadState.setLastTxnNode(happensAfterNode);
      threadState.set(st, currThreadState);
    }
    else {
      graph.addEdge(var.getLastTxnToWrite(), currTxnNode, methodsViolatingAtomicity);
      var.setLastTxnToWrite(currTxnNode);

      for(VDTransactionNode val: var.getLastTxnPerThreadToReadAll()) {
        graph.addEdge(val, currTxnNode, methodsViolatingAtomicity);
      }
    }
  }
}
