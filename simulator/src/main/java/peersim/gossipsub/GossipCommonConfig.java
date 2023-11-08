package peersim.gossipsub;

/**
 * Fixed Parameters of a kademlia network. They have a default value and can be configured at
 * startup of the network, once only.
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class GossipCommonConfig {

  /** Length of Id */
  public static int BITS = 256;

  /** Dimension of k-buckets */
  public static int D_low = 5;

  public static int D = 6;

  public static int D_high = 12;

  public static int ttl = 36000;

  public static int BLOCK_SIZE = 500000;

  public static int BANDWIDTH = 100000000;
  /**
   * Provides short information about current Kademlia configuration
   *
   * @return a string containing the current configuration
   */
  public static String info() {
    return String.format("[D_low=%d][D_high=%d][BITS=%d]", D_low, D_high, BITS);
  }
}
