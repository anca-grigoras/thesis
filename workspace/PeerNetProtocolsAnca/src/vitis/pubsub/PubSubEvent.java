/**
 * 
 */
package vitis.pubsub;

import peernet.core.Descriptor;

/**
 * @author anca
 *
 */
public class PubSubEvent
{
  private PubSubEventType type;
  private Descriptor src;
  private Object event;
  private static int serialNumCounter = 0;
  
  /**
   * 
   */
  public PubSubEvent(PubSubEventType type, Descriptor src, Object event) 
  {
    this.type = type;
    this.src = src;
    this.event = event;
  }
  
  
  /**
   * @return the type
   */
  public PubSubEventType getType()
  {
    return type;
  }
  /**
   * @return the src
   */
  public Descriptor getSrc()
  {
    return src;
  }
  /**
   * @return the event
   */
  public Object getEvent()
  {
    return event;
  }
  
}
