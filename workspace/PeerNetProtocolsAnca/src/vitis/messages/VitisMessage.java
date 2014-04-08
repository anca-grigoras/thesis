/**
 * 
 */
package vitis.messages;

import java.io.Serializable;
import java.util.Vector;

import peernet.core.Descriptor;

import topics.Topic;

/**
 * @author anca
 *
 */
public class VitisMessage extends Message
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public enum Type implements Serializable {
    REFRESH_FRIENDSHIP, CANCEL_FRIENDSHIP, SUBSCRIBE_ON_RELAY_PATH, UNSUBSCRIBE_FROM_RELAY_PATH;
  }
  
  public Type type;
  public Topic topic;
  public Descriptor profile;
  public Vector<Descriptor> path;
}
