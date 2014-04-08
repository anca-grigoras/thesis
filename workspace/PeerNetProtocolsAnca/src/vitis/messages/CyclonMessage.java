/**
 * 
 */
package vitis.messages;

import java.io.Serializable;
import java.util.Vector;

import peernet.core.Descriptor;

/**
 * @author anca
 *
 */
public class CyclonMessage extends Message
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public enum Type implements Serializable {
    GOSSIP_REQUEST, GOSSIP_RESPONSE;
  }
  
  public Type type;
  public Vector<Descriptor> descriptors;
}
