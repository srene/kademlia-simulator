package peersim.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.gossipsub.Message;

public final class PairwiseFixedLatencyTransport extends UniformRandomTransport {

  /** Size of the network. */
  private int size;

  /** Latencies between peers (symmetric). */
  private static long[][] pairwise_lat;

  private long[] uploadInterfaceBusyUntil;

  /**
   * String name of the parameter used to configure the minimum latency.
   *
   * @config
   */
  private static final String PAR_SIZE = "size";

  /** Reads configuration parameter. */
  public PairwiseFixedLatencyTransport(String prefix) {
    super(prefix);
    // generate the delays or just the array
    size = Configuration.getInt(prefix + "." + PAR_SIZE);
    pairwise_lat = new long[size][];
    // symmetric latency assumption so only need to allocate half of the matrix
    for (int i = 0; i < size; i++) {
      pairwise_lat[i] = new long[i + 1];
    }
    uploadInterfaceBusyUntil = new long[size];
    System.out.println("New PairwiseFixedLatencyTransport " + prefix);
    for (int i = 0; i < size; i++) {
      uploadInterfaceBusyUntil[i] = 0;
    }
  }

  /**
   * Delivers the message with a pairwise (pre-)generated latency for the given pair of src, dest
   * nodes.
   */
  public void send(Node src, Node dest, Object msg, int pid) {

    // avoid calling nextLong if possible
    long latencydelay = getLatency(src, dest);

    double transDelay = 0;

    if (msg instanceof Message) {
      Message message = (Message) msg;
      if (src.getBandwidth() > 0 && message.getSize() > 0) {
        transDelay += (double) message.getSize() * 8 * 1.03 / src.getBandwidth() * 1000;
        System.out.println(
            CommonState.getTime()
                + " Msg size "
                + message.getSize()
                + " "
                + src.getBandwidth()
                + " "
                + transDelay
                + " "
                + getBusyUntilTime(src));
      }
    }

    long timeNow = CommonState.getTime();

    if (transDelay > 0) {
      if (getBusyUntilTime(src) > timeNow) {
        /// this.uploadInterfaceBusyUntil += (long) transDelay; // truncated value
        updateBusyUntilTime(src, getBusyUntilTime(src) + (long) transDelay);

      } else {
        updateBusyUntilTime(src, timeNow + (long) transDelay);
      }
    }

    if (msg instanceof Message && src.getBandwidth() > 0) {
      System.out.println(
          CommonState.getTime() + " Adding latency " + transDelay + " " + latencydelay);
      System.out.println(CommonState.getTime() + " interface busy " + getBusyUntilTime(src));
    }

    long delay = (long) transDelay + latencydelay;
    EDSimulator.add(delay, msg, dest, pid);

    // avoid calling nextLong if possible
    // long delay = getLatency(src, dest);

    // EDSimulator.add(delay, msg, dest, pid);
  }

  /**
   * Returns the assigned delay to the specific src and dest peers that was previously generated.
   */
  public long getLatency(Node src, Node dest) {
    int sender = ((int) src.getID()) % size;
    int receiver = ((int) dest.getID()) % size;
    if (sender < receiver) {
      int tmp = sender;
      sender = receiver;
      receiver = tmp;
    }
    if (pairwise_lat[sender][receiver] == 0) {
      // compute the latency on-demand
      pairwise_lat[sender][receiver] = (range == 1 ? min : min + CommonState.r.nextLong(range));
    }
    return pairwise_lat[sender][receiver];
  }

  public long getBusyUntilTime(Node src) {
    int sender = ((int) src.getID()) % size;
    return this.uploadInterfaceBusyUntil[sender];
  }

  public void updateBusyUntilTime(Node src, long time) {
    int sender = ((int) src.getID()) % size;
    this.uploadInterfaceBusyUntil[sender] = time;
  }
}
