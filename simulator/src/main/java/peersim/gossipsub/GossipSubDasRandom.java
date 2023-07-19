package peersim.gossipsub;

import java.util.List;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.kademlia.SimpleEvent;
import peersim.kademlia.das.Block;
import peersim.kademlia.das.Sample;
import peersim.kademlia.das.operations.SamplingOperation;
import peersim.kademlia.das.operations.ValidatorSamplingOperation;

public class GossipSubDasRandom extends GossipSubDas {

  // protected long time;
  private boolean started;

  public GossipSubDasRandom(String prefix) {
    super(prefix);
    // TODO Auto-generated constructor stub
    started = false;
  }
  /**
   * Replicate this object by returning an identical copy. It is called by the initializer and do
   * not fill any particular field.
   *
   * @return Object
   */
  public Object clone() {
    GossipSubDasRandom dolly = new GossipSubDasRandom(GossipSubDasStable.prefix);
    return dolly;
  }

  /**
   * Start a topic query opearation.<br>
   *
   * @param m Message received (contains the node to find)
   * @param myPid the sender Pid
   */
  protected void handleInitNewBlock(Message m, int myPid) {

    currentBlock = (Block) m.body;
    if (!started) {
      started = true;
      return;
    }
    logger.warning("Init block");
    for (SamplingOperation sop : samplingOp.values()) {
      logger.warning(
          "Reporting sampling "
              + ((ValidatorSamplingOperation) sop).getRow()
              + " "
              + ((ValidatorSamplingOperation) sop).getColumn());
      GossipObserver.reportOperation(sop);
    }
    samplingOp.clear();
    if (isValidator) {
      logger.warning("Select rows/columns");
      int row1 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Row" + row1), getNode(), myPid);
      int row2 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Row" + row2), getNode(), myPid);
      int col1 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Column" + col1), getNode(), myPid);
      int col2 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Column" + col2), getNode(), myPid);

      for (int i = 1; i < Network.size(); i++) {
        Node n = Network.get(i);
        GossipSubProtocol prot = (GossipSubProtocol) n.getProtocol(myPid);
        prot.getTable().addPeer("Row" + row1, this.getGossipNode().getId());
        prot.getTable().addPeer("Row" + row2, this.getGossipNode().getId());
        prot.getTable().addPeer("Column" + col1, this.getGossipNode().getId());
        prot.getTable().addPeer("Column" + col2, this.getGossipNode().getId());
      }
      startValidatorSampling(row1, 0, "Row" + row1);
      startValidatorSampling(row2, 0, "Row" + row2);
      startValidatorSampling(0, col1, "Column" + col1);
      startValidatorSampling(0, col2, "Column" + col2);
    }

    startRandomSampling(myPid);
  }

  protected void handleMessage(Message m, int myPid) {

    super.handleMessage(m, myPid);
    Sample s = (Sample) m.value;

    logger.warning(
        "dasrows handleMessage received "
            + m.body
            + " "
            + s.getId()
            + " "
            + m.id
            + " "
            + m.src.getId());

    Sample[] samples = new Sample[] {s};

    String topic = (String) m.body;

    logger.warning("Received message sample " + s.getRow() + " " + s.getColumn() + " " + topic);

    for (SamplingOperation sop : samplingOp.values()) {
      List<String> topics = samplingTopics.get(sop.getId());
      if (topics.contains(topic)) {
        sop.addMessage(m.id);
        sop.elaborateResponse(samples);
        sop.increaseHops();
        if (sop.completed()) {
          sop.setStopTime(CommonState.getTime() - sop.getTimestamp());
        }
        logger.warning("Sop " + sop.getSamples().length + " " + topic + " " + sop.getHops());
      }
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
      default:
        super.processEvent(node, pid, event);
        break;
    }
  }

  public void setValidator(boolean isValidator) {
    this.isValidator = isValidator;
  }

  /*private int getRow(String topic) {
    if (topic.length() > 4) {
      if (topic.substring(0, 4).equals("Row")) {
        return Integer.valueOf(topic.substring(4, topic.length()));
      }
    }
    return 0;
  }

  private int getColumn(String topic) {
    if (topic.length() > 6) {
      if (topic.substring(0, 7).equals("Column")) {
        return Integer.valueOf(topic.substring(7, topic.length()));
      }
    }
    return 0;
  }*/
}
