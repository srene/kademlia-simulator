package peersim.gossipsub;

import java.math.BigInteger;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * This control generates samples every 5 min that are stored in a single node (builder) and starts
 * random sampling from the rest of the nodes In parallel, random lookups are started to start
 * discovering nodes
 *
 * @author Sergi Rene
 * @version 1.0
 */

// ______________________________________________________________________________________________
public class TrafficGenerator implements Control {

  // ______________________________________________________________________________________________
  /** MSPastry Protocol to act */
  private static final String PAR_PROT = "protocol";

  final String PAR_BLK_DIM_SIZE = "block_size";

  int mapfn;

  private int protocol;

  private boolean first = true, second = false;

  private long ID_GENERATOR = 0;

  // ______________________________________________________________________________________________
  public TrafficGenerator(String prefix) {

    GossipCommonConfig.BLOCK_SIZE =
        Configuration.getInt(prefix + "." + PAR_BLK_DIM_SIZE, GossipCommonConfig.BLOCK_SIZE);

    protocol = Configuration.getPid(prefix + "." + PAR_PROT);
  }

  private Message generateNewBlockMessage(Block b) {

    Message m = Message.makeInitNewBlock(b);
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
    if (first) {
      first = false;
      second = true;
      // Block b = new Block(ID_GENERATOR, GossipCommonConfig.BLOCK_SIZE);

      for (int i = 0; i < Network.size(); i++) {
        Node n = Network.get(i);
        GossipSubProtocol prot = (GossipSubProtocol) n.getProtocol(protocol);
        BigInteger id = prot.getGossipNode().getId();
        // if (i == 0) {
        // System.out.println("Sequencer " + id);
        String topic = "blockChannel";
        for (int l = 1; l < Network.size(); l++) {
          Node n2 = Network.get(l);
          GossipSubProtocol prot2 = (GossipSubProtocol) n2.getProtocol(protocol);
          prot2.getTable().addPeer(topic, id);
        }
        EDSimulator.add(
            CommonState.r.nextLong(200), Message.makeInitJoinMessage(topic), n, protocol);
        // }
      }

    } else /*if (second)*/ {

      Node n = Network.get(0);

      Block b =
          new Block(
              ID_GENERATOR,
              GossipCommonConfig.BLOCK_SIZE,
              ((GossipSubBlock) n.getProtocol(protocol)).getGossipNode());

      for (int i = 1; i < Network.size(); i++) {
        Node n2 = Network.get(i);
        EDSimulator.add(0, generateNewBlockMessage(b), n2, protocol);
      }

      String topic = "blockChannel";
      EDSimulator.add(0, Message.makePublishMessage(topic, b), n, protocol);
      System.out.println(
          "Sending block "
              + b.getId()
              + " from "
              + ((GossipSubBlock) n.getProtocol(protocol)).getGossipNode().getId()
              + " at "
              + CommonState.getTime());
      /*for (int i = 0; i < GossipCommonConfig.BLOCK_DIM_SIZE; i++) {
        for (int j = 0; j < GossipCommonConfig.BLOCK_DIM_SIZE; j++) {
          Sample s = b.getSample(i, j);
          String topic = "Row" + (s.getRow());
          EDSimulator.add(10, Message.makePublishMessage(topic, s), n, protocol);
          topic = "Column" + (s.getColumn());
          EDSimulator.add(10, Message.makePublishMessage(topic, s), n, protocol);
        }
      }
      for (int i = 1; i < Network.size(); i++) {
        Node n2 = Network.get(i);
        EDSimulator.add(0, generateNewBlockMessage(b), n2, protocol);
      }*/
      second = false;
    }
    ID_GENERATOR++;
    return false;
  }

  // ______________________________________________________________________________________________

} // End of class
// ______________________________________________________________________________________________
