package peersim.gossipsub;

import java.math.BigInteger;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.kademlia.das.Block;
import peersim.kademlia.das.Sample;

/**
 * This control generates samples every 5 min that are stored in a single node (builder) and starts
 * random sampling from the rest of the nodes In parallel, random lookups are started to start
 * discovering nodes
 *
 * @author Sergi Rene
 * @version 1.0
 */

// ______________________________________________________________________________________________
public class TrafficGeneratorRowColumn implements Control {

  // ______________________________________________________________________________________________
  /** MSPastry Protocol to act */
  private static final String PAR_PROT = "protocol";

  /** Mapping function for samples */
  final String PAR_MAP_FN = "mapping_fn";

  /** Number of sample copies stored per node */
  final String PAR_NUM_COPIES = "sample_copy_per_node";

  final String PAR_BLK_DIM_SIZE = "block_dim_size";

  int mapfn;

  private int protocol;

  private boolean first = true, second = false;

  private long ID_GENERATOR = 0;

  // ______________________________________________________________________________________________
  public TrafficGeneratorRowColumn(String prefix) {

    GossipCommonConfig.BLOCK_DIM_SIZE =
        Configuration.getInt(prefix + "." + PAR_BLK_DIM_SIZE, GossipCommonConfig.BLOCK_DIM_SIZE);

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
      Block b = new Block(GossipCommonConfig.BLOCK_DIM_SIZE, ID_GENERATOR);

      for (int i = 0; i < Network.size(); i++) {
        Node n = Network.get(i);
        GossipSubProtocol prot = (GossipSubProtocol) n.getProtocol(protocol);
        BigInteger id = prot.getGossipNode().getId();
        if (i == 0) {
          System.out.println("Builder " + id);
          for (int l = 1; l < Network.size(); l++) {
            Node n2 = Network.get(l);
            GossipSubProtocol prot2 = (GossipSubProtocol) n2.getProtocol(protocol);
            for (int j = 1; j <= GossipCommonConfig.BLOCK_DIM_SIZE; j++) {
              String topic = "Row" + j;
              prot2.getTable().addPeer(topic, id);
              topic = "Column" + j;
              prot2.getTable().addPeer(topic, id);
            }
            EDSimulator.add(0, generateNewBlockMessage(b), n2, protocol);
          }

          for (int j = 1; j <= GossipCommonConfig.BLOCK_DIM_SIZE; j++) {
            String topic = "Row" + j;
            EDSimulator.add(0, Message.makeInitJoinMessage(topic), n, protocol);
            topic = "Column" + j;
            EDSimulator.add(0, Message.makeInitJoinMessage(topic), n, protocol);
          }
        }
      }

    } else /*if (second)*/ {
      Block b = new Block(GossipCommonConfig.BLOCK_DIM_SIZE, ID_GENERATOR);

      Node n = Network.get(0);

      for (int i = 0; i < GossipCommonConfig.BLOCK_DIM_SIZE; i++) {
        for (int j = 0; j < GossipCommonConfig.BLOCK_DIM_SIZE; j++) {
          Sample s = b.getSample(i, j);
          String topic = "Row" + (s.getRow());
          EDSimulator.add(0, Message.makePublishMessage(topic, s), n, protocol);
          topic = "Column" + (s.getColumn());
          EDSimulator.add(0, Message.makePublishMessage(topic, s), n, protocol);
        }
      }
      for (int i = 1; i < Network.size(); i++) {
        Node n2 = Network.get(i);
        EDSimulator.add(0, generateNewBlockMessage(b), n2, protocol);
      }
      second = false;
      ID_GENERATOR++;
    }
    return false;
  }

  // ______________________________________________________________________________________________

} // End of class
// ______________________________________________________________________________________________
