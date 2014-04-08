/**
 * 
 */
package vitis.types;

import peernet.core.Descriptor;

/**
 * @author anca
 *
 */
public class DescriptorUtility implements Comparable<DescriptorUtility>
{ 
  private Descriptor descr;
  private double utility;  
  /**
   * 
   */
  public DescriptorUtility(Descriptor descr, double utility)
  {
    this.descr = descr;
    this.utility = utility;
  }
  
//------------------------------------------------------------------------
  public void setUtility(double utility) {
    this.utility = utility;
  }

//------------------------------------------------------------------------
  public double getUtility() {
    return utility;
  }
  
//------------------------------------------------------------------------
  public Descriptor getDescriptor() {
    return descr;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime*result+((descr==null) ? 0 : descr.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this==obj)
      return true;
    if (obj==null)
      return false;
    if (getClass()!=obj.getClass())
      return false;
    DescriptorUtility other = (DescriptorUtility) obj;
    if (descr==null)
    {
      if (other.descr!=null)
        return false;
    }
    else if (!descr.equals(other.descr))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(DescriptorUtility arg0)
  {
    return (utility > arg0.utility ? -1 : 1);
  }

  public String toString() {
    return utility + " ";
  }
}
