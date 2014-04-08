/**
 * 
 */
package poldercast.observers;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Dissemination;
import topics.Topic;

/**
 * @author anca
 *
 */
public class DuplicationFactor implements Control
{
  int pid;
  int cycle = 0;
  int size = 0;
  
  /**
   * 
   */
  public DuplicationFactor(String name)
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
   // System.out.println("Time: "+CommonState.getTime());
    cycle++;
    computeDuplicationFactor();
    return false;
  }

  /**
   * 
   */
  private void computeDuplicationFactor()
  {
    double finalDupFactor = 0;
    for(int i = 0; i< Network.size(); i++)
    {
      double duplicationFactor = 0;
      Node n = Network.get(i);
      Dissemination dissem = (Dissemination)n.getProtocol(pid);
      int duplicate = 0, distinct = 0;
      if (!dissem.getDuplicateMessages().isEmpty())
      {
        Set<Entry<Topic,Integer>> set = dissem.getDuplicateMessages().entrySet();
        Iterator<Entry<Topic, Integer>> it = set.iterator();
        while (it.hasNext())
        {
          Entry<Topic,Integer> entry = it.next();
          duplicate += entry.getValue();
        }
      }
      dissem.resetDuplicateMessages();
      
      if (!dissem.getMsgHopCounter().isEmpty())
      {
        Set<Entry<Topic,Integer>> set2 = dissem.getMsgHopCounter().entrySet();
        Iterator<Entry<Topic,Integer>> it2 = set2.iterator();
        while (it2.hasNext())
        {
          Entry<Topic,Integer> entry = it2.next();
          if(entry.getValue() > 0)
            distinct++;
          dissem.resetHopCounter(entry.getKey());
        }
      }
 
      int total = duplicate + distinct;
      if (distinct != 0)
        duplicationFactor = total/distinct;
      finalDupFactor += duplicationFactor;
    }
    finalDupFactor = finalDupFactor/size;
    System.out.println(cycle + "\t" + finalDupFactor);
    
  }
}
