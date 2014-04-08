/*
 * Created on Jul 23, 2010 by Spyros Voulgaris
 */
package tera.messages;

import java.io.Serializable;
import java.util.Vector;

import peernet.core.Descriptor;
import topics.Topic;





public class Message implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1797845507069160137L;

  public enum Type implements Serializable {
    ACCESS_POINT_LOOKUP, ACCESS_POINT_FOUND, 
    JOIN_REQUEST, JOIN_RESPONSE, 
    GOSSIP_REQUEST, GOSSIP_RESPONSE;
  }

  public Type type;
  public Descriptor sender;
  public Vector<Descriptor> descriptors;
  public Topic topic;
}