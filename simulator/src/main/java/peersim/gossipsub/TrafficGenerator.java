package peersim.gossipsub;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * This control generates random search traffic from nodes to random destination node.
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class TrafficGenerator implements Control {

  /** MSPastry Protocol to act. */
  private static final String PAR_PROT = "protocol";

  /** MSPastry Protocol ID to act */
  protected final int pid;

  private boolean first = true;

  /**
   * Constructs a TrafficGenerator object.
   *
   * @param prefix the prefix string
   */
  public TrafficGenerator(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
  }

  /**
   * Generates a random region-based find node message, by selecting randomly the destination.
   *
   * @return Message
   */
  private Message generateJoinMessage(String topic) {
    // existing active destination node
    // UniformRandomGenerator urg =
    //    new UniformRandomGenerator(KademliaCommonConfig.BITS, CommonState.r);
    // BigInteger id = urg.generate();

    Message m = Message.makeInitJoinMessage(topic);
    m.timestamp = CommonState.getTime();

    return m;
  }

  // ______________________________________________________________________________________________
  /**
   * every call of this control generates and send a random find node message
   *
   * @return boolean
   */
  public boolean execute() {

    Node start;
    do {
      start = Network.get(CommonState.r.nextInt(Network.size()));
    } while ((start == null) || (!start.isUp()));

    // send message
    String topic = "topic1";
    EDSimulator.add(0, generateJoinMessage(topic), start, pid);
    // EDSimulator.add(0, generateRegionBasedFindNodeMessage(), start, pid);

    return false;
  }

  // ______________________________________________________________________________________________

} // End of class
// ______________________________________________________________________________________________
