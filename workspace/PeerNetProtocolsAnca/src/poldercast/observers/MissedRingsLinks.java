/**
 * It checks how many nodes have the best ring links
 */
package poldercast.observers;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Rings;
import poldercast.topics.RoutingTable;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 * It measures the percentage of target ring links that are not yet in place (missed links)
 */
public class MissedRingsLinks implements Control
{
  int pid;
  int cycle = 0;
  Vector<Double> missedLinks = new Vector<Double>(Network.size());
  Vector<Double> comN = new Vector<Double>(Network.size());
  
  public MissedRingsLinks(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
  }
  
  private void computeMissedLinks()
  {
    int missedLinksPerNode, totalLinks;
    int size = Network.size();
    
    for (int i = 0; i< size; i++)
    {      
      Node n = Network.get(i);
      long id = n.getID();
      
      Vector<Topic> topics = TopicsRepository.getTopics(id);//get the topic subscriptions for node n
      
      missedLinksPerNode = 0;      
      totalLinks = 0;
      for (Topic topic : topics)
      {
        boolean isComplete = true;
        Vector<Long> realNeighbors = getRealNeighbors(n, topic);
        Vector<Long> bestNeighbors = getBestNeighbors(topic, id);

        totalLinks += bestNeighbors.size();
        
        for (int j = 0; j< bestNeighbors.size(); j++)
          if (!realNeighbors.contains(bestNeighbors.get(j)))
          {
            missedLinksPerNode++;
            isComplete = false;
          }
        if (!isComplete && cycle > 90)
          printState(bestNeighbors, realNeighbors, topic, id);
      }
      missedLinks.add((double)missedLinksPerNode/totalLinks*100);    
     /* if (missedRatio.get(i) > comN.get(i))
        System.out.println("this node is not ok " + id + " <" + comN.get(i) + " , " + missedRatio.get(i) + " >");
      comN.remove(i); comN.add(i, missedRatio.get(i));*/
    }
    double totalPercentage = 0;
    for (int i = 0; i< missedLinks.size(); i++)
      totalPercentage+= missedLinks.get(i);
    totalPercentage = totalPercentage/size;
    System.out.println(cycle + " " + totalPercentage);
    missedLinks.clear();
  }
  
  private void printState(Vector<Long> bestNeighbors, Vector<Long> realNeighbors, Topic topic, long nodeId)
  {
    System.out.print(nodeId + " " + topic.getId() + " real: ");
    for (int r = 0; r<realNeighbors.size(); r++)
      System.out.print(realNeighbors.get(r) + " ");
    System.out.print("  // best : ");
    for (int b = 0; b< bestNeighbors.size(); b++)
      System.out.print(bestNeighbors.get(b) + " ");
    System.out.println();
  }
  /**
   * @param topic
   * @param id TODO
   * @return
   */
  private Vector<Long> getBestNeighbors(Topic topic, long id)
  {
    Vector<Long> subscribers = TopicsRepository.getSubscribers(topic);
    Vector<Long> bestNeighbors = new Vector<Long>();
    
    Collections.sort(subscribers);
    bestNeighbors.addAll(getBestHalfNeighbors(subscribers, id));
    
    Collections.reverse(subscribers);
    bestNeighbors.addAll(getBestHalfNeighbors(subscribers, id));
    
    Collection<Long> noDup = new LinkedHashSet<Long>(bestNeighbors);
    bestNeighbors.clear();
    bestNeighbors.addAll(noDup);    
    
    return bestNeighbors;
  }

  /**
   * @param n
   * @param topic
   * @return
   */
  private Vector<Long> getRealNeighbors(Node n, Topic topic)
  {
    RoutingTable rt = ((Rings)n.getProtocol(pid)).getRT(topic);
    Vector<Long> neighbors = new Vector<Long>();
    if (rt!= null)
    {
      for (int j = 0; j< rt.getPred().size(); j++)
        neighbors.add(rt.getPred().get(j).getID());
      for(int j = 0; j <rt.getSucc().size(); j++)
        neighbors.add(rt.getSucc().get(j).getID());
    }
    return neighbors;
  }

  /**
   * The method returns the successors if subscribers are in ascendent order and predecessors otherwise
   * The method is customized for 2 succ and 2 pred
   * @param subscribers
   * @param id
   * @return
   */
  private Vector<Long> getBestHalfNeighbors(Vector<Long> subscribers, long id)
  {
    int index = subscribers.indexOf(id);
    Vector<Long> neighbors = new Vector<Long>();
    for (int i = index+1; i<= index+2; i++)
      neighbors.add(subscribers.get(i%subscribers.size()));
    neighbors.remove(id);
    return neighbors;
  }

  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    cycle++;
    computeMissedLinks();
    return false;
  }
}
