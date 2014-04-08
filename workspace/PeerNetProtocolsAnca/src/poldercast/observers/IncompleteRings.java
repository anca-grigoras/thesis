/**
 * 
 */
package poldercast.observers;

import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Rings;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 * It measures the percentage of topics for which the ring has not convergedNode yet (incomplete rings)
 */
public class IncompleteRings implements Control
{
  int pid;
  int cycle = 0;
  
  public IncompleteRings(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
  }


  private void getRingsRT()
  {
   for (int i = 0; i<Network.size(); i++)
    {      
      Node node = Network.get(i);
      Vector<Topic> t = TopicsRepository.getTopics(node.getIndex());
      System.out.println(node.getID());
      for (int j = 0; j< t.size(); j++)
        System.out.print("("+t.get(j)+ ":" + ((Rings)node.getProtocol(pid)).degreePerTopic(t.get(j)) +") ");
      System.out.println();  
    }
  }
  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    getRingsRT();
    return false;
  }
}
