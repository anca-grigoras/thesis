/**
 * 
 */
package tera.messages;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Vector;

import peernet.core.Descriptor;
import tera.messages.Message.Type;
import topics.Topic;

/**
 * @author anca
 *
 */
public class Advertisment implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public enum Type implements Serializable {
    ADV_REQUEST, ADV_RESPONSE;
  }

  public Type type;
  public Descriptor sender;
  public Entry<Topic, Integer> entry;
  public double sizeEstimation;
}
