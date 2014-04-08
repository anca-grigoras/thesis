package poldercast.descriptor;

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
      
  public DescriptorTopics(Node node, int pid)
  {
    super(node ,pid);

  }
  
  /**
   * The method returns a vector with the topics the node is subscribed to
   * @return
   */
  public Vector<Topic> getTopics()
  { 
    if (getID() != -1)
      return TopicsRepository.getTopics(getID() );
    return null;
  }
  
  /**
   * The method returns the number of common topics between this and another descriptor
   * @param descr
   * @return
   */
  public int getNbCommonTopics(Descriptor descr)
  {
    long index = descr.getID();
    
    if(getID()  == -1 || index == -1)
      return -1;
    return TopicsRepository.getCommonTopics(getID() , index);
  }
  
  /**
   * The method returns a vector of topics that are common between this and another descriptor
   * @param descr
   * @return
   */
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
  public boolean isSubscribedTo(Topic topic)
  {
    Vector<Topic> myTopics = getTopics();
    if (myTopics.contains(topic))
      return true;
    return false;
  }
  
  public void printId()
  {
    System.out.println("My id is " + getID());
  }
}
