/*
 * Created on Aug 25, 2007 by Spyros Voulgaris
 *
 */
package gossip.comparator;

import gossip.descriptor.DescriptorAge;
import peernet.core.Descriptor;


public class AgeDescending implements DescriptorComparator
{
  /**
   * Default constructor.
   */
  public AgeDescending(String prefix) {}


  /**
   *  Do nothing. Sorting independent of reference item.
   */
  public void setReference(Descriptor refDesc)
  {
    assert false: "Is this supposed to be used?";
  }


  /**
   * Sorts based on hops, in descending order,
   * so that the oldest items end up in the vector's head.
   */
  public int compare(Descriptor a, Descriptor b)
  {
    return -(((DescriptorAge)a).getAge()-((DescriptorAge)b).getAge());
  }


}
