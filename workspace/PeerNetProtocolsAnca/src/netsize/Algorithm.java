/*
 * Created on Jun 24, 2011 by Spyros Voulgaris
 *
 */
package netsize;

public abstract class Algorithm implements Cloneable
{
  /**
   * Called when the simulation starts. Gives the algorithm implementation on each
   * node the opportunity to initialize internal data structures, variables, etc.
   */
  public abstract void init();

  /**
   * Called when a message from some other node is delivered to this node.
   * @param message
   */
  public abstract void deliverMessage(Object message);

  /**
   * This is called by the framework when the node needs to prepare a new message to
   * send to a random other peer.
   * @return
   */
  public abstract Object prepareMessage();

  /**
   * This is called always right after prepareMessage(), and should return the size
   * of the message jsut prepared in bytes.
   * @return
   */
  public abstract int getMessageSize();

  /**
   * Should return the current network size estimate, as considered by the node.
   * Called by observer for evaluation of the algorithm's accuracy.
   * @return
   */
  public abstract double getEstimate();
  

  public Object clone()
  {
    Algorithm newAlg = null;

    try {newAlg = (Algorithm) super.clone();}
    catch (CloneNotSupportedException e) {System.err.println(e);}
    newAlg.init();

    return newAlg;
  }
}
