
package powerlaw;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;




/**
 * Prints the whole graph in a given format.
 */
public class Observer implements Control
{
  /**
   * The protocol to operate on.
   * 
   * A parameter with the name 'protocol' is expected to be found in the
   * configuration file, in the block defining this Control.
   */
  private static final String PAR_PROT = "protocol";


  protected final int pid;



  /**
   * Standard constructor that reads the configuration parameters. Invoked by
   * the simulation engine only once in the beginning.
   * 
   * @param name
   *          the configuration prefix for this class
   */
  public Observer(String name)
  {
    pid = Configuration.getPid(name + "." + PAR_PROT);
  }



  /**
   * Very basic Control that prints the list of neighbors.
   * Used only as an example of how to write a control.
   */
  public boolean execute()
  {
    double sum = 0.0;
    
    for (int i=0; i<Network.size(); i++)
    {
      Node node = Network.get(i);
      PowerlawOverlay prot = (PowerlawOverlay) node.getProtocol(pid);
            
//      System.out.println("Node "+node.getID()+" has "+ prot.view.size()+" neighbors:");
      
      if (prot.view.size() > 0)
        sum += Math.log(prot.view.size());
    }

    double myAlpha = Network.size()/sum;
    System.out.println("alpha = " + myAlpha + "\t config alpha = " + 
    		((PowerlawFitness)FitnessNode.fitnessFunction).alpha);
    return false;
  }
}

