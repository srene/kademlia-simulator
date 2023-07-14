package peersim.gossipsub;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

public class GossipSubDasRows extends GossipSubProtocol {

  private LinkedHashMap<Long, SamplingOperation> samplingOp;

  protected Block currentBlock;

  // protected long time;
  private boolean started;
  private int row1;
  private int row2;
  private int col1;
  private int col2;

  public GossipSubDasRows(String prefix) {
    super(prefix);
    samplingOp = new LinkedHashMap<>();
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
    GossipSubDasRows dolly = new GossipSubDasRows(GossipSubDasRows.prefix);
    return dolly;
  }

  private void startSampling(int row, int column) {
    logger.warning("StartSampling " + row + " " + column);

    ValidatorSamplingOperation op =
        new ValidatorSamplingOperation(
            this.getGossipNode().getId(),
            CommonState.getTime(),
            currentBlock,
            null,
            row,
            column,
            true,
            null);
    samplingOp.put(op.getId(), op);
  }

  /**
   * Start a topic query opearation.<br>
   *
   * @param m Message received (contains the node to find)
   * @param myPid the sender Pid
   */
  protected void handleInitNewBlock(Message m, int myPid) {

    currentBlock = (Block) m.body;
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
    if (!started) {
      logger.warning("Assing rows/columns");
      started = true;
      row1 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Row" + row1), getNode(), myPid);
      row2 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Row" + row2), getNode(), myPid);
      col1 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Column" + col1), getNode(), myPid);
      col2 = CommonState.r.nextInt(currentBlock.getSize()) + 1;
      EDSimulator.add(0, Message.makeInitJoinMessage("Column" + col2), getNode(), myPid);

      for (int i = 1; i < Network.size(); i++) {
        Node n = Network.get(i);
        GossipSubProtocol prot = (GossipSubProtocol) n.getProtocol(myPid);
        prot.getTable().addPeer("Row" + row1, this.getGossipNode().getId());
        prot.getTable().addPeer("Row" + row2, this.getGossipNode().getId());
        prot.getTable().addPeer("Column" + col1, this.getGossipNode().getId());
        prot.getTable().addPeer("Column" + col2, this.getGossipNode().getId());
      }
    } else {
      startSampling(row1, 0);
      startSampling(row2, 0);
      startSampling(0, col1);
      startSampling(0, col2);
    }
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

    List<Long> toRemove = new ArrayList<>();

    String topic = (String) m.body;

    logger.warning("Received message sample " + s.getRow() + " " + s.getColumn() + " " + topic);

    for (SamplingOperation sop : samplingOp.values()) {
      ValidatorSamplingOperation vsop = (ValidatorSamplingOperation) sop;

      if (s.getRow() == vsop.getRow() || s.getColumn() == vsop.getColumn()) {
        sop.addMessage(m.id);
        sop.elaborateResponse(samples);
        sop.increaseHops();

        // sop.increaseHops();
        if (sop.completed()) {
          // GossipObserver.reportOperation(sop);
          // toRemove.add(sop.getId());
          sop.setStopTime(CommonState.getTime());
        }
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
