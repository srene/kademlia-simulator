package peersim.blockpropagation;

/**
 * Fixed Parameters of a kademlia network. They have a default value and can be configured at
 * startup of the network, once only.
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class BlockPropagationConfig {

  public static int BLOCK_SIZE = 512 * 1024;
  public static int SAMPLE_SIZE = 512;

  /**
   * Provides short information about current Kademlia configuration
   *
   * @return a string containing the current configuration
   */
  public static String info() {
    return String.format("[BITS=%d]", BLOCK_SIZE);
  }
}
