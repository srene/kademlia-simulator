package peersim.blockpropagation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.gossipsub.GossipSubProtocol;
import peersim.gossipsub.Message;
import peersim.kademlia.SimpleEvent;

public class GossipSubBlock extends GossipSubProtocol {

  protected LinkedHashMap<Long, GossipBlockOperation> samplingOp;

  protected HashMap<Long, List<String>> samplingTopics;

  // protected Block currentBlock;

  protected boolean isValidator;

  public GossipSubBlock(String prefix) {
    super(prefix);
    samplingOp = new LinkedHashMap<>();
    // samplingTopics = new HashMap<>();
    // TODO Auto-generated constructor stub
    isValidator = false;
  }
  /**
   * Replicate this object by returning an identical copy. It is called by the initializer and do
   * not fill any particular field.
   *
   * @return Object
   */
  public Object clone() {
    GossipSubBlock dolly = new GossipSubBlock(GossipSubBlock.prefix);
    return dolly;
  }

  protected void startGossipBlockOperation(Block b, boolean old) {

    GossipBlockOperation op =
        new GossipBlockOperation(this.getGossipNode().getId(), CommonState.getTime(), b, old);
    samplingOp.put(op.getId(), op);
  }

  /**
   * Start a topic query opearation.<br>
   *
   * @param m Message received (contains the node to find)
   * @param myPid the sender Pid
   */
  protected void handleInitNewBlock(Message m, int myPid) {
    logger.warning("Init block received");
    List<Long> toRemove = new ArrayList<>();
    for (GossipBlockOperation sop : samplingOp.values()) {
      if (sop.isCompleted()) {
        GossipObserver.reportOperation(sop);
        toRemove.add(sop.getId());
      }
    }
    for (Long id : toRemove) samplingOp.remove(id);
    // samplingOp.clear();

    startGossipBlockOperation((Block) m.body, false);
  }

  protected void handleInitOldBlock(Message m, int myPid) {
    logger.warning("Init old block received");
    // samplingOp.clear();

    startGossipBlockOperation((Block) m.body, true);
  }

  protected void handleMessage(Message m, int myPid) {

    Sample s = (Sample) m.value;

    // Sample[] samples = new Sample[] {s};

    String topic = (String) m.body;

    for (GossipBlockOperation sop : samplingOp.values()) {
      if (sop.getBlock().getId() == s.getBlock().getId()) {
        sop.addMessage(m.id);
        if (!sop.isCompleted()) {
          sop.addHops(m.getHops());
          sop.elaborateResponse(s);
          logger.warning(
              "Received message block "
                  + s.getId()
                  + " "
                  + s.getBlock().getId()
                  + " "
                  + topic
                  + " "
                  + sop.getHops()
                  + " "
                  + sop.getStopTime()
                  + " "
                  + sop.isCompleted());
          if (sop.isCompleted()) {
            sop.setStopTime(CommonState.getTime() - sop.getTimestamp());
            logger.warning(
                "Completed operation "
                    + s.getBlock().getId()
                    + " "
                    + sop.getStopTime()
                    + " "
                    + sop.getTimestamp());
          }
        } else {
          logger.warning(
              "Received extra copy " + s.getId() + " " + s.getBlock().getId() + " " + m.getHops());
        }
      }
    }

    super.handleMessage(m, myPid);
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
      case Message.MSG_INIT_OLD_BLOCK:
        m = (Message) event;
        handleInitOldBlock(m, pid);
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

  public boolean isValidator() {
    return isValidator;
  }
}
