/*
 * Created on Dec 9, 2004 by Spyros Voulgaris
 *
 */
package gossip.comparator;

import peernet.core.CommonState;
import peernet.core.Descriptor;


public class Random implements DescriptorComparator
{
  /**
   * Default constructor.
   */
  public Random(String prefix) {}


  /**
   *  Do nothing. Sorting independent of reference descriptor.
   */
  public void setReference(Descriptor refDesc)
  {
    //assert false: "Is this supposed to be used?";
  }


  /**
   * Return a random ordering of the two objects.
   */
  public int compare(Descriptor a, Descriptor b)
  {
    return (CommonState.r.nextBoolean() ? -1 : 1);
  }


}
