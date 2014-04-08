/*
 * Created on Aug 25, 2007 by Spyros Voulgaris
 *
 */
package gossip.comparator;

import gossip.descriptor.DescriptorAge;
import peernet.core.Descriptor;


public class AgeAscending implements DescriptorComparator
{
  /**
   * Default constructor.
   */
  public AgeAscending(String prefix) {}


  /**
   *  Do nothing. Sorting independent of reference item.
   */
  public void setReference(Descriptor refItem)
  {
    assert false: "Is this supposed to be used?";
  }


  /**
   * Sorts based on hops, in ascending order,
   * so that the freshest items end up in the vector's head.
   */
  public int compare(Descriptor a, Descriptor b)
  {
    return ((DescriptorAge)a).getAge()-((DescriptorAge)b).getAge();
  }
}
