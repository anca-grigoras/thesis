/*
 * Created on Dec 9, 2004 by Spyros Voulgaris
 *
 */
package gossip.comparator;

import java.util.Comparator;

import peernet.core.Descriptor;


/**
 * @author Spyros Voulgaris
 *
 */
public interface DescriptorComparator extends Comparator<Descriptor>
{
  /**
   * Declare a reference item for comparators that compare
   * two items with respect to a reference item.
   * @param refItem The refItem to set.
   */
  public void setReference(Descriptor refDesc);
}
