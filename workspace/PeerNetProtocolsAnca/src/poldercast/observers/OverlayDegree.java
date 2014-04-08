package poldercast.observers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Descriptor;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Rings;
import topics.Topic;
import topics.TopicsRepository;

public class OverlayDegree implements Control
{
  int pid;
  int cycle = 0;
  int size = 0;
  
  public OverlayDegree(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    size = Network.size();
  }
  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    System.out.println("Time: "+CommonState.getTime());
    cycle++;
    computeOverlayDegree();
    System.out.println();
    return false;
  }
  /**
   * 
   */
  private void computeOverlayDegree()
  {
    HashMap<Long,Integer> degree = new HashMap<Long, Integer>();
    for(int i = 0; i< size; i++)
    {
      Node node = Network.get(i);
      Rings rings = ((Rings)node.getProtocol(pid));
      Vector<Topic> topics = TopicsRepository.getTopics(node.getIndex());
      Vector<Descriptor> localView = new Vector<Descriptor>();
      int indegree = 0;
      for (Topic topic : topics)
        if (rings.getRT(topic) != null)
        {
          for (Descriptor d : rings.getRT(topic).getPred())
          {
            if (!localView.contains(d))
            {
              if(degree.get(d.getID()) == null)
                degree.put(d.getID(), 1);
              else
                degree.put(d.getID(), degree.get(d.getID())+1);
              indegree++;
              localView.add(d);
            }
          }
          for (Descriptor d : rings.getRT(topic).getSucc())
          {
            if(!localView.contains(d))
            {
              if(degree.get(d.getID()) == null)
                degree.put(d.getID(), 1);
              else
                degree.put(d.getID(), degree.get(d.getID())+1);
              indegree++;
              localView.add(d);
            }
          }
        }
      if(degree.get(node.getID()) == null)
        degree.put(node.getID(), indegree);
      else
        degree.put(node.getID(), degree.get(node.getID())+indegree);
    }
    
    Set<Entry<Long,Integer>> set = degree.entrySet();
    Iterator<Entry<Long,Integer>> it = set.iterator();
    while (it.hasNext())
    {
      Entry<Long,Integer> entry = it.next();
      System.out.println(entry.getValue() + "\t" + entry.getKey() + "\t" + TopicsRepository.getTopics(entry.getKey()).size());
    }
  }
}
