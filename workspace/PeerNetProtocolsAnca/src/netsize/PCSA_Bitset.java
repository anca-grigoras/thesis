/*
 * Created on Jun 20, 2011 by Spyros Voulgaris
 *
 */
package netsize;

import java.util.BitSet;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class PCSA_Bitset extends Algorithm
{
  // Parameters specific to a node
  private static int I;
  
  private static final String PAR_I = "I";
  
  private BitSet[] bitsets;


  public PCSA_Bitset(String prefix)
  {
    I = Configuration.getInt(prefix+"."+PAR_I);
  }



  public void init()
  {
    // Pick a bit b with probability 1/2^b
    int b = 1;
    while (CommonState.r.nextBoolean())
      b++;
    
    // pick a bucket
    int bucket;
    bucket = CommonState.r.nextInt(I);
    
    // Mark it in my BitSet
    bitsets = new BitSet[I];
    for (int i=0; i<I; i++)
      bitsets[i] = new BitSet();
    bitsets[bucket].set(b-1);
  }

  
  
  public void deliverMessage(Object message)
  {
    BitSet[] receivedBitsets = (BitSet[])message;
    
    for (int i=0; i<I; i++)
      bitsets[i].or(receivedBitsets[i]);
  }



  public Object prepareMessage()
  {
    return bitsets.clone();
  }



  @Override
  public int getMessageSize()
  {
    int messageSize = 0;
    for (int i=0; i<I; i++)
    {
      messageSize += bitsets[i].length()/8 + 1;
      messageSize += 1;   // assuming the bitset's length takes one byte too
    }
    messageSize += 1;   // adding yet another byte for the number of bitsets
    return messageSize;
  }



  public double getEstimate()
  {
    double totalTrail = 0;

    for (int i=0; i<I; i++)
    {
      int trailOne = bitsets[i].nextClearBit(0);
      totalTrail += trailOne;
    }

    return 1.29281 * I * Math.pow(2, totalTrail/I);
  }
}
