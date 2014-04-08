/**
 * 
 */
package poldercast.observers;

import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Descriptor;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Rings;
import poldercast.topics.RoutingTable;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class TopicPopularity implements Control
{
  int pid;
  int cycle = 0;
  int size = 0;
  Vector<Topic> topics = null;
  Vector<Boolean> converged = null; //CHANGE TO A HASHMAP<tOPICS, BOOLEAN>
  /**
   * 
   */
  public TopicPopularity(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    topics = TopicsRepository.getAllTopics();
    size = topics.size();
    converged = new Vector<Boolean>(size);
    for (int i = 0; i< size; i++)
      converged.add(false);  
    }



  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    System.out.println();
    //System.out.println("Time: "+CommonState.getTime());
    cycle++;
    System.out.println(cycle);
    checkTopicConvergence();
    return false;
  }



  /**
   * 
   */
  private void checkTopicConvergence()
  {
    int convergedRings = 0;
    Vector<Integer> convTopics = new Vector<Integer>();
    for (Topic topic : topics)
    {
      if (!converged.get((int) topic.getId()) && isConverged(topic))
      {
        convergedRings++;
        converged.remove(topic);converged.add((int) topic.getId(), true);
        convTopics.add(TopicsRepository.getSubscribers(topic).size());
      }
    }
    System.out.print(convergedRings + " \t");
    for (int i = 0; i< convTopics.size(); i++)
      System.out.print(convTopics.get(i) + " ");
    System.out.println();
    
  }



  /**
   * @param topic
   * @return
   */
  private boolean isConverged(Topic topic)
  {
    Vector<Long> subscribers = TopicsRepository.getSubscribers(topic);
    Vector<Long> ringNodes = new Vector<Long>();
    checkRing(Network.getByID(subscribers.get(0).intValue()),ringNodes, topic);
    for (long l : subscribers)
      if(!ringNodes.contains(l))
        return false;
    return true;
  }



  /**
   * @param node
   * @param ringNodes
   * @param topic 
   */
  private void checkRing(Node node, Vector<Long> ringNodes, Topic topic)
  {
    Rings rings = (Rings)node.getProtocol(pid);
    RoutingTable rt = rings.getRT(topic);
    if(rt != null)
    {
      for(Descriptor d : rt.getPred())
        if(!ringNodes.contains(d.getID()))
        {
          ringNodes.add(d.getID());
          checkRing(Network.getByID((int)d.getID()), ringNodes, topic);
        }
      for(Descriptor d : rt.getSucc())
        if(!ringNodes.contains(d.getID()))
        {
          ringNodes.add(d.getID());
          checkRing(Network.getByID((int)d.getID()), ringNodes, topic);
        }
    }
    
  }
}
