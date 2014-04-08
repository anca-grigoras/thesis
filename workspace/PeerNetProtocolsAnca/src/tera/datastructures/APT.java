/**
 * 
 */
package tera.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import peernet.core.Descriptor;

import topics.Topic;

/**
 * @author anca
 *
 */
public class APT implements Cloneable
{
  public Hashtable<Topic,Descriptor> elements;
  private int maxsize = 0;
  /**
   * 
   */
  public APT(int maxsize)
  {
    elements = new Hashtable<Topic, Descriptor>();
    this.maxsize = maxsize;
  }
  
  public void put (Topic t, Descriptor d) {
    elements.put(t,d);
  }
  
  public boolean containsTopic(Topic t) {
    return elements.containsKey(t);
  }
  
  public Descriptor getAccessPoint(Topic t) {
    return elements.get(t);
  }
  
  public void truncate() {
    if (maxsize > 0 && elements.size() > maxsize) {
      ArrayList<Topic> topics = new ArrayList<Topic>();
      topics.addAll((Set<Topic>) elements.keySet());
      Collections.shuffle(topics);
      while (topics.size() > maxsize)
        elements.remove(topics.remove(0));
    }
  }
  
  public int size() {
    return elements.size();
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

}
