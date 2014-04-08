/**
 * 
 */
package vitis.initializers;


import peernet.config.Configuration;
import peernet.core.Control;
import topics.TopicsRepository;

/**
 * 
 * @author anca
 *
 */
public class NodeTopicsInitializer implements Control
{

  private static final String PAR_DATA = "data";
  
 // boolean isChurnEnabled = false;
  int pid = 0;
  String confProperty = null;
  int limit = 0;
  
  public NodeTopicsInitializer(String prefix)
  {
    pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
    confProperty = prefix + "." + PAR_DATA;
    
  }



  /* (non-Javadoc)
   * @see peeremu.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    if (Configuration.contains(confProperty))
      TopicsRepository.initTopics(confProperty);
    return false;
  }
}
