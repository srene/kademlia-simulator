package peersim.blockpropagation;

public class Sample {

  int size;
  long id;
  Block block;

  public Sample(Block block, long id) {

    this.block = block;
    this.id = id;
    this.size = BlockPropagationConfig.SAMPLE_SIZE;
  }

  public Block getBlock() {
    return block;
  }

  public int getSize() {
    return size;
  }

  public long getId() {
    return id;
  }
}
