/**
 * 
 */
package poldercast.observers;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Dissemination;

/**
 * @author anca
 *
 */
public class MessageOverhead implements Control
{
  int pid;
  int cycle = 0;
  int size = 0;
  /**
   * 
   */
  public MessageOverhead(String name)
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
    cycle++;
    System.out.println("Cycle: "+ cycle);
    calculateMessageOverhead();
    return false;
  }

  /**
   * 
   */
  private void calculateMessageOverhead()
  {
    int distinct = 0;
    int duplicates = 0;
    for (int i = 0; i< size; i++)
    {
      Node n = Network.get(i);
      Dissemination diss = (Dissemination)n.getProtocol(pid);
      distinct += diss.getReceived();
      diss.resetReceived();
      
      duplicates += diss.getDuplicates();
      diss.resetDuplicates();
    }
    System.out.println(distinct + " \t" + duplicates);
  }
}
