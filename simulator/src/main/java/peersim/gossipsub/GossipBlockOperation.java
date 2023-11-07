package peersim.gossipsub;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import peersim.kademlia.operations.FindOperation;

public class GossipBlockOperation extends FindOperation {

  private Block currentBlock;
  private boolean completed;

  public GossipBlockOperation(BigInteger srcNode, long timestamp, Block block) {
    super(srcNode, null, timestamp);
    completed = false;
    currentBlock = block;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void elaborateResponse(Block block) {
    if (block.getId() == currentBlock.getId()) completed = true;
  }

  public Map<String, Object> toMap() {
    // System.out.println("Mapping");
    Map<String, Object> result = new HashMap<String, Object>();

    result.put("id", this.operationId);
    result.put("src", this.srcNode);
    result.put("type", "ValidatorSamplingOperation");
    result.put("messages", getMessagesString());
    result.put("start", this.timestamp);
    result.put("completion_time", this.stopTime);
    result.put("hops", this.nrHops);
    result.put("num_messages", getMessages().size());
    result.put("block_id", this.currentBlock.getId());
    result.put("completed", isCompleted());
    return result;
  }
}
