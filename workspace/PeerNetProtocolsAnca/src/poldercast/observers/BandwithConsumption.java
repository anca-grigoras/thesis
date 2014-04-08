/**
 * 
 */
package poldercast.observers;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Cyclon;
import poldercast.protocols.Rings;
import poldercast.protocols.Vicinity;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class BandwithConsumption implements Control
{
  private static final String PAR_CYCLON = "cyclon";
  private static final String PAR_VICINITY = "vicinity";
  private static final String PAR_RINGS = "rings";
  
  int c, v, r;
  int cycle = 0;
  int size = 0;
  /**
   * 
   */
  public BandwithConsumption(String name)
  {
    c = Configuration.getPid(name+"."+PAR_CYCLON);
    v = Configuration.getPid(name+"."+PAR_VICINITY);
    r = Configuration.getPid(name+"."+PAR_RINGS);
    size = Network.size();
  }



  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    System.out.println("Time: "+CommonState.getTime());
    calculateBandwithConsumption();
    return false;
  }



  /**
   * 
   */
  private void calculateBandwithConsumption()
  {
    for (int i = 0; i< size; i++)
    {
      Node n = Network.get(i);
      Cyclon cyclon = (Cyclon)n.getProtocol(c);
      Vicinity vic = (Vicinity)n.getProtocol(v);
      Rings rings = (Rings)n.getProtocol(r);
      int subscriptionSize = TopicsRepository.getTopics(n.getID()).size();
      int bandwithConsumption = cyclon.controlMessages+vic.controlMessages+rings.controlMessages;
      cyclon.resetControlMessages(); vic.resetControlMessages(); rings.resetControlMessages();
      System.out.println(subscriptionSize + " \t" + bandwithConsumption);
    }
    
  }
}
