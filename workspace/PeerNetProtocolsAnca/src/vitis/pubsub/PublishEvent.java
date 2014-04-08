/**
 * 
 */
package vitis.pubsub;

import peernet.core.Descriptor;
import topics.Topic;

/**
 * @author anca
 *
 */
public class PublishEvent
{
  private final Descriptor publisher;
  private final Topic topic;
  /**
   * 
   */
  public PublishEvent(Descriptor publisher, Topic topic)
  {
    this.publisher = publisher;
    this.topic = topic;
  }
  /**
   * @return the nodeId
   */
  public Descriptor getPublisher()
  {
    return publisher;
  }
  /**
   * @return the topicId
   */
  public Topic getTopic()
  {
    return topic;
  }
  
}
