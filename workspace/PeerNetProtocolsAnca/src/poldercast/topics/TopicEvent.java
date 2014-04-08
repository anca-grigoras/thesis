/**
 * 
 */
package poldercast.topics;

import java.io.Serializable;
import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import topics.Topic;

/**
 * @author anca
 *
 */
public class TopicEvent implements Serializable//, Cloneable 
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public Descriptor source;
  public long topicOfInterest;
  public int hop;
  public long id;
  
  /*public TopicEvent(long id, Descriptor source, Topic topic, String content, Vector<Descriptor> path)
  {
    this.source = source;
    topicOfInterest = topic;
    this.content = content;
    this.path = path;
    hop = 0;
    serialNumCounter++;
    String s = ""+source.getID()+serialNumCounter+CommonState.getTime();
    this.id = Long.parseLong(s);
    //this.id = id;
  }*/
  
 /* public TopicEvent(long id, Descriptor source, Topic topic)
  {
    this.source = source;
    topicOfInterest = topic;
    hop = 0;
    serialNumCounter++;
    String s = ""+source.getID()+serialNumCounter+CommonState.getTime();
    this.id = Long.parseLong(s);
    //this.id = id;
  }
  
  *//**
   * @return the sender
   *//*
  public Descriptor getSource()
  {
    return source;
  }

  *//**
   * @param sender the sender to set
   *//*
  public void setSource(Descriptor source)
  {
    this.source = source;
  }

  *//**
   * @return the topicOfInterest
   *//*
  public Topic getTopicOfInterest()
  {
    return topicOfInterest;
  }

  *//**
   * @return the id
   *//*
  public long getId()
  {
    return id;
  }

  *//**
   * @param id the id to set
   *//*
  public void setId(int id)
  {
    this.id = id;
  }

  *//**
   * @return the hop
   *//*
  public int getHop()
  {
    return hop;
  }
*/
  /*public void incHop()
  {
    hop++;
  }*/

  /*public Object clone()
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
  }*/
}
