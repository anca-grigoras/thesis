/**
 * 
 */
package vitis.types;

import peernet.core.Descriptor;

/**
 * @author anca
 *
 */
public class LeaderInfo
{
  public Descriptor leader;
  public Descriptor directlyConnected;
  public Descriptor parent;
  public int hops;
  /** 
   * 
   */
  public LeaderInfo(Descriptor leader, Descriptor directlyConnected, Descriptor parent, int age)
  {
    this.leader = leader;
    this.parent = parent;
    this.directlyConnected = directlyConnected;
  }
  
  public String toString() {
    return leader.getID() + ":" + directlyConnected + ":" + parent.getID() + "  hops:" + hops;
  }
  
//------------------------------------------------------------------------
  public Descriptor getLeader() {
    return leader;
  }
  
//------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((directlyConnected == null) ? 0 : directlyConnected
            .hashCode());
    result = prime * result + ((leader == null) ? 0 : leader.hashCode());
    return result;
  }

//------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LeaderInfo other = (LeaderInfo) obj;
    if (directlyConnected == null) {
      if (other.directlyConnected != null)
        return false;
    } else if (!directlyConnected.equals(other.directlyConnected))
      return false;
    if (leader == null) {
      if (other.leader != null)
        return false;
    } else if (!leader.equals(other.leader))
      return false;
    return true;
  }
  
}
