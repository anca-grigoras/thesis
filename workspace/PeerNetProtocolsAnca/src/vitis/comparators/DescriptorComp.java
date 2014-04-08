/**
 * 
 */
package vitis.comparators;

import java.util.Comparator;

import peernet.core.Descriptor;

/**
 * @author anca
 *
 */
public class DescriptorComp implements Comparator<Descriptor>
{

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Descriptor arg0, Descriptor arg1)
  {
    return (int) (arg0.getID() - arg1.getID());
  }
}
