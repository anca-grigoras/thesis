/*
 * Created on Jun 20, 2011 by Spyros Voulgaris
 *
 */
package netsize;

import java.util.BitSet;

import peernet.core.CommonState;

public class PC extends Algorithm
{
  // Parameters specific to a node
  BitSet bitset;



  public PC(String prefix)
  {
  }



  public void init()
  {
    // Pick a bit b with probability 1/2^b
    int b = 1;
    while (CommonState.r.nextBoolean())
      b++;

    // Mark it in my BitSet
    bitset = new BitSet();
    bitset.set(b-1);
  }

  
  
  public void deliverMessage(Object message)
  {
    bitset.or((BitSet)message);
  }



  public Object prepareMessage()
  {
    return bitset.clone();
  }



  public double getEstimate()
  {
    int trailOne = bitset.nextClearBit(0);
    return 1.29281 * Math.pow(2, trailOne + 1);
  }



  @Override
  public int getMessageSize()
  {
    return (int) Math.ceil(bitset.size()/8.0);
  }
}
