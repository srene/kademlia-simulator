package peersim.gossipsub;

public class Block {

  private long id;
  private int size;

  public Block(long id, int size) {
    this.id = id;
    this.size = size;
  }

  public long getId() {
    return id;
  }

  public int getSize() {
    return size;
  }
}
