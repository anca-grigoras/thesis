/**
 * 
 */
package poldercast.observers;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Dissemination;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class DisseminationProgress implements Control
{
  int pid;
  int cycle = 0;
  int size = 0;
  Vector<Topic> topics = null;  
  /**
   * 
   */
  public DisseminationProgress(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    
    topics = TopicsRepository.getAllTopics(); 
    size = topics.size();
  }



  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    /*System.out.println();
    System.out.println("Time: "+CommonState.getTime());
*/    cycle++;
    computeDisseminationProgress();
    return false;
  }



  /**
   * 
   */
  private void computeDisseminationProgress()
  {
    HashMap<Integer,Double> notReached = new HashMap<Integer,Double>();
    for (Topic topic : topics)
    {
      HashMap<Integer,Integer> hops = new HashMap<Integer,Integer>();
      Vector<Long> subscribers = TopicsRepository.getSubscribers(topic);
      int interested = subscribers.size();
      for (long subscr : subscribers)
      {
        Node n = Network.getByID((int) subscr);
        Dissemination dissem = (Dissemination)n.getProtocol(pid);
        int myHops = -1;
        if (dissem.getMsgHopCounter().get(topic) != null)
          myHops = dissem.getMsgHopCounter().get(topic);
        if (myHops > 0)
        {
              dissem.resetHopCounter(topic);
              if (hops.get(myHops) == null)
                hops.put(myHops, 1);
              else
                hops.put(myHops, hops.get(myHops)+1);
           
        }
        dissem.resetHopCounter(topic);
      }
      Set<Entry<Integer,Integer>> set = hops.entrySet();
      Iterator<Entry<Integer,Integer>> it = set.iterator();
      int reachedNodes = 1; //because of the node that started the dissemination has hop 0
      while (it.hasNext())
      {
        Entry<Integer,Integer> entry = it.next();
        reachedNodes += entry.getValue();
        double percentNotReached = (double)(interested-reachedNodes)/interested*100;
        if (notReached.get(entry.getKey()) == null)
          notReached.put(entry.getKey(), percentNotReached);
        else
          notReached.put(entry.getKey(), notReached.get(entry.getKey())+percentNotReached);
      }
    }
   
    Set<Entry<Integer,Double>> set = notReached.entrySet();
    Iterator<Entry<Integer,Double>> it = set.iterator();
    while (it.hasNext())
    {
      Entry<Integer,Double> entry = it.next();
      double percentNotReached = entry.getValue()/size;
      System.out.println(entry.getKey() + " \t" + percentNotReached );
    }
   
  }
}
