package peersim.gossipsub;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeerTable {

  private HashMap<String, List<BigInteger>> peerMap; // , samplesIndexed;

  public PeerTable() {

    peerMap = new HashMap<>();
  }

  public void addPeer(String topic, BigInteger peer) {
    if (peerMap.get(topic) != null) {
      List<BigInteger> nodes = peerMap.get(topic);
      nodes.add(peer);
    } else {
      List<BigInteger> nodes = new ArrayList<>();
      nodes.add(peer);
      peerMap.put(topic, nodes);
    }
  }

  public List<BigInteger> getPeers(String topic) {
    return peerMap.get(topic);
  }

  public List<BigInteger> getNPeers(String topic, int n, List<BigInteger> peers) {
    List<BigInteger> nodes = new ArrayList<>();
    if (peerMap.get(topic) != null) {
      List<BigInteger> topicPeers = peerMap.get(topic);
      for (BigInteger id : topicPeers) {
        if (!peers.contains(id)) nodes.add(id);
      }
    }

    return nodes;
  }
}
