package peersim.gossipsub;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PeerTable {

  private HashMap<String, HashSet<BigInteger>> peerMap; // , samplesIndexed;

  public PeerTable() {

    peerMap = new HashMap<>();
  }

  public void addPeer(String topic, BigInteger peer) {
    if (peerMap.get(topic) != null) {
      HashSet<BigInteger> nodes = peerMap.get(topic);
      nodes.add(peer);
    } else {
      HashSet<BigInteger> nodes = new HashSet<>();
      nodes.add(peer);
      peerMap.put(topic, nodes);
    }
  }

  public List<BigInteger> getPeers(String topic) {
    List<BigInteger> peers;
    if (peerMap.get(topic) != null) peers = new ArrayList<>(peerMap.get(topic));
    else peers = new ArrayList<>();
    Collections.shuffle(peers);
    // return peerMap.get(topic);
    return peers;
  }

  public List<BigInteger> getNPeers(String topic, int n, HashSet<BigInteger> initialPeers) {
    List<BigInteger> peers;
    if (peerMap.get(topic) != null) peers = new ArrayList<>(peerMap.get(topic));
    else peers = new ArrayList<>();
    peers.removeAll(initialPeers);
    Collections.shuffle(peers);

    List<BigInteger> peersResult = new ArrayList<>();
    for (BigInteger id : peers) {
      if (peersResult.size() < n) peersResult.add(id);
    }

    return peersResult;
  }
}
