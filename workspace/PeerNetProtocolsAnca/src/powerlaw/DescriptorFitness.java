/*
 * Created on Jul 12, 2010 by Spyros Voulgaris
 *
 */
package powerlaw;

import gossip.descriptor.DescriptorAge;
import peernet.core.Node;
import peernet.transport.Address;

public class DescriptorFitness extends DescriptorAge
{
  public double fitness;



  public DescriptorFitness(Node node, int pid)
  {
    super(node, pid);
    fitness = ((FitnessNode)node).fitness;
  }



  public DescriptorFitness(Address address)
  {
    super(address);
  }



  public Object clone() throws CloneNotSupportedException
  {
    Object result = super.clone();

    return result;
  }
}
