/**
 * 
 */
package vitis.pubsub;

import java.io.Serializable;
import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import topics.Topic;

/**
 * @author anca
 *
 */
public class TopicEvent implements Serializable, Cloneable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private final Topic topic;
  private final Descriptor publisher;
  private final String content;
  private int hopCounts = 0;
  private final long time;
  
  private static long serialNumCounter = 0;
  private long id;
  private Vector<Descriptor> path;
  private Descriptor sender;
  /**
   * 
   */
  public TopicEvent(Topic topic, Descriptor publisher, String content, Vector<Descriptor> path)
  {
    this.topic = topic;
    serialNumCounter++;
    id = serialNumCounter;
    this.publisher = publisher;
    this.content = content;
    this.time = CommonState.getTime();
    this.path = path;
    this.sender = publisher;
  }

  public Vector<Descriptor> getPath()
  {
    return path;
  }
  
  public void setPath(Vector<Descriptor> path)
  {
    this.path = path;
  }
  /**
   * @return the serialversionuid
   */
  public static long getSerialversionuid()
  {
    return serialVersionUID;
  }

  /**
   * @return the topic
   */
  public Topic getTopic()
  {
    return topic;
  }

  /**
   * @return the publisher
   */
  public Descriptor getPublisher()
  {
    return publisher;
  }
  
  public Descriptor getSender()
  {
    return sender;
  }
  public void setSender(Descriptor sender)
  {
    this.sender = sender;
  }

  /**
   * @return the content
   */
  public String getContent()
  {
    return content;
  }

  /**
   * @return the hopCounts
   */
  public int getHopCounts()
  {
    return hopCounts;
  }
  
  public void incrementHopCounts() {
    this.hopCounts++;
  }

  /**
   * @return the time
   */
  public long getTime()
  {
    return time;
  }

  /**
   * @return the serialNumCounter
   */
  public static long getSerialNumCounter()
  {
    return serialNumCounter;
  }

  /**
   * @return the id
   */
  public long getId()
  {
    return id;
  }
  public Object clone()
  {
    Object obj = null;
    try
    {
      obj = super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
    }
    return obj;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime*result+(int) (id^(id>>>32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this==obj)
      return true;
    if (obj==null)
      return false;
    if (getClass()!=obj.getClass())
      return false;
    TopicEvent other = (TopicEvent) obj;
    if (id!=other.id)
      return false;
    if (topic == null) {
      if (other.topic != null)
        return false;
    } else if (!topic.equals(other.topic))
      return false;
    return true;
  }
  
  
  
}
