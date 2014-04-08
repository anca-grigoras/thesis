/**
 * 
 */
package poldercast.observers;

import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Dissemination;
import poldercast.protocols.Rings;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class HitRatio implements Control
{
  int pid;
  int cycle = 0;
  int size = 0;
  Vector<Topic> topics = null;  
  Vector<Double> hits = null;
  
  public HitRatio(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    
    topics = TopicsRepository.getAllTopics();
    size = topics.size();
    hits = new Vector<Double>(size);
    for (int i = 0; i<= size; i++)
      hits.add((double)0);
  }
  
  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    cycle++;
    disseminateEvents();
    
    return false;
  }
  
  private void disseminateEvents()
  {
    double hitRatio = 0;
    int missratio = 0;
    double totalHitRatio = 0;
    int completeDissemination = 0;
    for (Topic topic : topics)
    {
      hitRatio = 0;
      Vector<Long> subscribers = TopicsRepository.getSubscribers(topic);
      int interested = subscribers.size();
      for (long subscr : subscribers)
      {
        Node n = Network.getByID((int) subscr);
        //System.out.println(n.getID());
        Dissemination dissem = (Dissemination)n.getProtocol(pid);
        Rings rings = (Rings)n.getProtocol(2);
        if (!dissem.getTopicMessageFwd().isEmpty())
            if (dissem.getTopicMessageFwd().get(topic))
            {
              hitRatio++;
              dissem.setEventState(topic);
            }
            else {
              missratio++;
             /* if (cycle >= 90)
                System.out.println(n.getID() + " -> " + topic.getId());*/
            }
      }
      /*if (hitRatio < hits.get(topic.getId()))
        System.out.println("topic: "+topic.getId() + "  " + hits.get(topic.getId()) + " " + hitRatio);*/
      //hits.remove(topic.getId());
      //hits.add(topic.getId(), hitRatio);
      
      //System.out.println("topic " + topic + " : " + hitRatio);
      hitRatio = hitRatio/interested*100;
      totalHitRatio += hitRatio;
      if(hitRatio == 100)
        completeDissemination++;
    }
   
    double avgHitRatio = (double)totalHitRatio/size; //hit ratio
    double avgmissratio = 100-avgHitRatio;
    double msgDiss = 100-(double)completeDissemination/size*100; //complete dissemination
    System.out.println(cycle + "\t" + avgmissratio + "\t" + msgDiss);
  }
}
