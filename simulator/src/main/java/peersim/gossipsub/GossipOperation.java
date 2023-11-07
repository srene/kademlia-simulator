package peersim.gossipsub;

import java.math.BigInteger;
import peersim.kademlia.operations.FindOperation;

public class GossipOperation extends FindOperation {

  private Block currentBlock;
  private boolean completed;

  public GossipOperation(BigInteger srcNode, BigInteger destNode, long timestamp, Block block) {
    super(srcNode, destNode, timestamp);
    completed = false;
    currentBlock = block;
  }
}
