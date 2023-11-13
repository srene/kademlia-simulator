package peersim.blockpropagation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import peersim.kademlia.operations.FindOperation;

public class GossipBlockOperation extends FindOperation {

  private Block currentBlock;
  private HashMap<Long, Boolean> samples;

  public GossipBlockOperation(BigInteger srcNode, long timestamp, Block block) {
    super(srcNode, null, timestamp);
    currentBlock = block;
    samples = new HashMap<>();
    for (Sample s : block.getSamples()) {
      samples.put(s.getId(), false);
    }
  }

  public Block getBlock() {
    return currentBlock;
  }

  public boolean isCompleted() {
    for (boolean s : samples.values()) {
      if (!s) return false;
    }
    return true;
  }

  public void elaborateResponse(Sample s) {
    samples.put(s.getId(), true);
  }

  public Map<String, Object> toMap() {
    // System.out.println("Mapping");
    Map<String, Object> result = new HashMap<String, Object>();

    result.put("id", this.operationId);
    result.put("src", this.srcNode);
    result.put("type", "BlockGossipOperation");
    // result.put("messages", getMessagesString());
    result.put("start", this.timestamp);
    result.put("completion_time", this.stopTime);
    result.put("hops", this.nrHops / samples.size());
    result.put("num_messages", getMessages().size() / samples.size());
    result.put("block_id", this.currentBlock.getId());
    return result;
  }
}
