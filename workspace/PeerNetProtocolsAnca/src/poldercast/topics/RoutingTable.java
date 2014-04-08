/**
 * 
 */
package poldercast.topics;

import java.util.HashMap;
import java.util.Vector;

import peernet.core.Descriptor;
import poldercast.descriptor.DescriptorTopics;

/**
 * @author anca
 *
 */
public class RoutingTable
{
  Vector<Descriptor> pred;
  Vector<Descriptor> succ;
  /**
   * @return the pred
   */
  public Vector<Descriptor> getPred()
  {
    if (pred == null)
      pred = new Vector<Descriptor>();
    return pred;
  }
  /**
   * @param pred the pred to set
   */
  public void setPred(Vector<Descriptor> pred)
  {
    this.pred = pred;
  }
  /**
   * @return the succ
   */
  public Vector<Descriptor> getSucc()
  {
    if (succ == null)
      succ = new Vector<Descriptor>();
    return succ;
  }
  /**
   * @param succ the succ to set
   */
  public void setSucc(Vector<Descriptor> succ)
  {
    this.succ = succ;
  }
  /**
   * @param pred
   * @param succ
   */
  public RoutingTable(Vector<Descriptor> pred, Vector<Descriptor> succ)
  {
    super();
    this.pred = pred;
    this.succ = succ;
  }
  
//  public RoutingTable(int index, int limit)
//  {
////    HashMap<Integer,>
////    
////    Vector<Integer> myTopics = ((DescriptorProfile)d).getTopics();
////    
////      pred = new Vector<Descriptor>(limit/2);
////      succ = new Vector<Descriptor>(limit/2);
////    
//  
//  }
  
}
