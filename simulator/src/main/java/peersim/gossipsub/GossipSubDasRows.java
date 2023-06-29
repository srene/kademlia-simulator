package peersim.gossipsub;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.kademlia.SimpleEvent;
import peersim.kademlia.das.Block;
import peersim.kademlia.das.Sample;
import peersim.kademlia.das.operations.SamplingOperation;
import peersim.kademlia.das.operations.ValidatorSamplingOperation;

public class GossipSubDasRows extends GossipSubProtocol {

  private LinkedHashMap<Long, SamplingOperation> samplingOp;

  protected Block currentBlock;

  protected long time;

  /**
   * Replicate this object by returning an identical copy. It is called by the initializer and do
   * not fill any particular field.
   *
   * @return Object
   */
  public Object clone() {
    GossipSubDasRows dolly = new GossipSubDasRows(GossipSubDasRows.prefix);
    return dolly;
  }

  GossipSubDasRows(String prefix) {
    super(prefix);
    samplingOp = new LinkedHashMap<Long, SamplingOperation>();
  }

  private void startSampling(int row, int column) {
    logger.warning("StartSampling " + row + " " + column);

    ValidatorSamplingOperation op =
        new ValidatorSamplingOperation(
            this.getGossipNode().getId(), time, currentBlock, null, row, 0, true, null);
    samplingOp.put(op.getId(), op);
  }

  /**
   * Start a topic query opearation.<br>
   *
   * @param m Message received (contains the node to find)
   * @param myPid the sender Pid
   */
  protected void handleInitNewBlock(Message m, int myPid) {
    time = CommonState.getTime();
    currentBlock = (Block) m.body;

    int row = CommonState.r.nextInt(currentBlock.getSize());
    startSampling(row, 0);
    EDSimulator.add(0, Message.makeInitJoinMessage("Row" + row), getNode(), myPid);
    row = CommonState.r.nextInt(currentBlock.getSize());
    startSampling(row, 0);
    EDSimulator.add(0, Message.makeInitJoinMessage("Row" + row), getNode(), myPid);
    int column = CommonState.r.nextInt(currentBlock.getSize());
    EDSimulator.add(0, Message.makeInitJoinMessage("Column" + column), getNode(), myPid);
    startSampling(0, column);
    column = CommonState.r.nextInt(currentBlock.getSize());
    EDSimulator.add(0, Message.makeInitJoinMessage("Column" + column), getNode(), myPid);
    startSampling(0, column);
  }

  private void handleMessage(Message m, int myPid) {
    String topic = (String) m.body;

    Sample s = (Sample) m.value;
    mCache.put(s.getId(), s);
    if (cache.get(topic) == null) cache.put(topic, new ArrayList<BigInteger>());
    if (m.src == this.node || cache.get(topic).contains(s.getId())) return;

    cache.get(topic).add(s.getId());
    if (mesh.get(topic) != null) {
      for (BigInteger id : mesh.get(topic)) {
        m.dst = ((GossipSubProtocol) nodeIdtoNode(id).getProtocol(myPid)).getGossipNode();
        sendMessage(m, id, myPid);
      }
    }
    Sample[] samples = new Sample[] {s};

    logger.warning("Received message sample " + s.getRow() + " " + s.getColumn());

    for (SamplingOperation sop : samplingOp.values()) {
      sop.elaborateResponse(samples);
      sop.increaseHops();
      logger.warning(
          "Sop "
              + sop.getSamples().length
              + " "
              + ((ValidatorSamplingOperation) sop).getRow()
              + " "
              + ((ValidatorSamplingOperation) sop).getColumn()
              + " "
              + sop.getHops());
    }
  }

  @Override
  public void processEvent(Node node, int pid, Object event) {
    // Set the Kademlia ID as the current process ID - assuming Pid stands for process ID.
    this.gossipid = pid;
    Message m;

    // If the event is a message, report the message to the Kademlia observer.
    if (event instanceof Message) {
      m = (Message) event;
      // KademliaObserver.reportMsg(m, false);
    }

    // Handle the event based on its type.
    switch (((SimpleEvent) event).getType()) {
      case Message.MSG_INIT_NEW_BLOCK:
        m = (Message) event;
        handleInitNewBlock(m, pid);
        break;
      case Message.MSG_MESSAGE:
        m = (Message) event;
        handleMessage(m, pid);
        break;
    }

    super.processEvent(node, pid, event);
  }
}
