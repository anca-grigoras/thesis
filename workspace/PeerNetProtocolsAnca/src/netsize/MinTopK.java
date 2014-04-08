/*
 * Created on Jun 20, 2011 by Spyros Voulgaris
 *
 */
package netsize;

import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class MinTopK extends Algorithm
{
  private static final String PAR_K = "k";
  private static final String PAR_SPACE = "space";

  // Parameters specific to a node
  private Vector<Coin> list;
  private int myNumber;

  // Parameters for all nodes
  private static int k;
  private static int space;



  public MinTopK(String prefix)
  {
    
    k = Configuration.getInt(prefix+"."+PAR_K);
    space = Configuration.getInt(prefix+"."+PAR_SPACE);
  }



  public void init()
  {
    myNumber = CommonState.r.nextInt(space);
    list = new Vector<Coin>();
    Coin coin = new Coin(myNumber, CommonState.getNode().getID());
    list.add(coin);
  }



  public void deliverMessage(Object message)
  {
    list.addAll((Vector<Coin>)message);

    // Remove duplicates
    HashSet hs = new HashSet();
    hs.addAll(list);
    list.clear();
    list.addAll(hs);
    
    // Sort list
    Collections.sort(list);

    // Reduce it to k items
    while (list.size() > k)
      list.remove(list.size()-1);
  }



  public Object prepareMessage()
  {
    return list.clone();
  }



  public double getEstimate()
  {
    assert list.size() > 0: "The list cannot be empty";

    // We assume the list is already sorted.
    return ((double)space)*list.size() / list.lastElement().value;
  }



  private class Coin implements Comparable<Coin>
  {
    public int value;
    public long nodeId;

    public Coin(int value, long nodeId)
    {
      super();
      this.value = value;
      this.nodeId = nodeId;
    }


    public int compareTo(Coin other)
    {
      return value - other.value;
    }
    
    public String toString()
    {
      return "("+value+","+nodeId+")";
    }
  }



  @Override
  public int getMessageSize()
  {
    // TODO Auto-generated method stub
    return 0;
  }

}
