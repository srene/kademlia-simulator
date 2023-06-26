package peersim.gossipsub;

import java.math.BigInteger;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.kademlia.UniformRandomGenerator;

/**
 * This control initializes the whole network (that was already created by peersim) assigning a
 * unique NodeId, randomly generated, to every node.
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class CustomDistribution implements peersim.core.Control {

  private static final String PAR_PROT = "protocol";

  private int protocolID;
  private UniformRandomGenerator urg;

  public CustomDistribution(String prefix) {
    protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
    urg = new UniformRandomGenerator(GossipCommonConfig.BITS, CommonState.r);
  }

  /**
   * Scan over the nodes in the network and assign a randomly generated NodeId in the space
   * 0..2^BITS, where BITS is a parameter from the kademlia protocol (usually 160)
   *
   * @return boolean always false
   */
  public boolean execute() {
    // BigInteger tmp;
    for (int i = 0; i < Network.size(); ++i) {
      Node generalNode = Network.get(i);
      BigInteger id;
      // BigInteger attackerID = null;
      GossipNode node;
      // String ip_address;
      id = urg.generate();
      // node = new KademliaNode(id, randomIpAddress(r), 0);
      node = new GossipNode(id);

      GossipSubProtocol gossipProt = ((GossipSubProtocol) (Network.get(i).getProtocol(protocolID)));

      // generalNode.setKademliaProtocol(gossipProt);
      gossipProt.setNode(node);
      gossipProt.setProtocolID(protocolID);
    }

    return false;
  }
}
