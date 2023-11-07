package peersim.gossipsub;

public class Block {

  private long id;
  private int size;
  private GossipNode publisher;

  public Block(long id, int size, GossipNode publisher) {
    this.id = id;
    this.size = size;
    this.publisher = publisher;
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
}
