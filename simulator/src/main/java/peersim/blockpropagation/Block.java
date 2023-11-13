package peersim.blockpropagation;

import java.util.ArrayList;
import java.util.List;
import peersim.gossipsub.GossipNode;

public class Block {

  private long id;
  private int size;
  private GossipNode publisher;
  private List<Sample> samples;

  public Block(long id, int size, GossipNode publisher) {
    this.id = id;
    this.size = size;
    this.publisher = publisher;
    samples = new ArrayList<>();
    int num_samples = size / BlockPropagationConfig.SAMPLE_SIZE;
    for (int i = 0; i < num_samples; i++) {
      samples.add(new Sample(this, id * num_samples + i));
    }
  }

  public long getId() {
    return id;
  }

  public int getSize() {
    return size;
  }

  public GossipNode getPublisher() {
    return publisher;
  }

  public List<Sample> getSamples() {
    return samples;
  }
}
