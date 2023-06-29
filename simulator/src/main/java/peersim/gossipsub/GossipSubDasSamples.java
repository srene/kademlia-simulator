package peersim.gossipsub;

public class GossipSubDasSamples extends GossipSubProtocol {

  /**
   * Replicate this object by returning an identical copy. It is called by the initializer and do
   * not fill any particular field.
   *
   * @return Object
   */
  public Object clone() {
    GossipSubDasSamples dolly = new GossipSubDasSamples(GossipSubDasSamples.prefix);
    return dolly;
  }

  GossipSubDasSamples(String prefix) {
    super(prefix);
  }
}
