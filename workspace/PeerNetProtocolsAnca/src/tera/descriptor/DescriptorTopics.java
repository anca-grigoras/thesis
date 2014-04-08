package tera.descriptor;

import java.util.Vector;

import peernet.core.Descriptor;
import peernet.core.Node;
import topics.Topic;
import topics.TopicsRepository;

public class DescriptorTopics extends DescriptorAge
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public String overlayId;
  public DescriptorTopics(Node node, int pid)
  {
    super(node ,pid);
  }

  /**
   * The method returns a vector with the index of topics the node is subcribed to
   * @param descr
   * @return
   */
  public Vector<Topic> getTopics()
  { 
    if (getID() != -1)
      return TopicsRepository.getTopics(getID());
    return null;
  }
  
  /**
   * The method returns the number of common topics between two nodes
   * @param descr
   * @return
   */
  public int getNbCommonTopics(Descriptor descr)
  {
    long index = descr.getID();
    
    if(getID() == -1 || index == -1)
      return -1;
    return TopicsRepository.getCommonTopics(getID(), index);
  }
  
  public Vector<Topic> getCommonTopics(Descriptor descr)
  {
    Vector<Topic> commonTopics = new Vector<Topic>();
    long index = descr.getID();
    Vector<Topic> topics = TopicsRepository.getTopics(index);
    
    for (int i = 0 ; i< getTopics().size(); i++)
    {
      if (topics.contains(getTopics().get(i)))
        commonTopics.add(getTopics().get(i));
    }
    
    return commonTopics;
  }
    
  /**
   * Verifies if the node is subscribed to topic
   * @param topic
   * @return
   */
  public boolean isSubscribedTo(int topic)
  {
    Vector<Topic> myTopics = getTopics();
    if (myTopics.contains(topic))
      return true;
    return false;
  }
  
  /**
   * The method establishes if two descriptors are equal based on the indexes of their nodes. It helps to eliminate the duplicates.
   * @param descr
   * @return
   */
 /* @Override
  public boolean equals(Object descr)
  {
    long index = ((Descriptor)descr).getID();
    
    if(id == -1 || index == -1)
      return false;
    
    return id == index;
  }*/
}
