/**
 * 
 */
package test;

import peernet.config.Configuration;
import peernet.core.Control;

/**
 * @author anca
 *
 */
public class NetworkObserverTera implements Control
{
  int pid;
  int cycle = 0;
  /**
   * 
   */
  public NetworkObserverTera(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
  }



  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    cycle++;
    System.out.println("cycle : " + cycle);
    return false;
  }
}
