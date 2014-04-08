/**
 * 
 */
package vitis.messages;

import java.io.Serializable;
import java.util.Vector;

import peernet.core.Descriptor;

import vitis.messages.CyclonMessage.Type;
import vitis.types.Buffer;

/**
 * @author anca
 *
 */
public class TManMessage extends Message
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public enum Type implements Serializable {
    RT_REQUEST, RT_RESPONSE;
  }
  
  public Type type;
  public Buffer buffer;
}
