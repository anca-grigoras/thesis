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
public class DistributionLoad implements Control
{
  int pid;
  int cycle = 0;
  int size = 0;
  /**
   * 
   */
  public DistributionLoad(String name)
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
    System.out.println();
    System.out.println("Time: "+CommonState.getTime());
    calculateDistributionLoad();
    return false;
  }
  /**
   * 
   */
  private void calculateDistributionLoad()
  {
    int received = 0;
    int forwarded = 0;
    for (int i = 0; i< size; i++)
    {
      Node n = Network.get(i);
      Dissemination diss = (Dissemination)n.getProtocol(pid);
      received += diss.getReceived();
      diss.resetReceived();
      
      forwarded += diss.getForwarded();
      diss.resetForwarded();
      System.out.println(n.getID() + " \t" + (double)received/forwarded);
    }    
  }
}
