/*
 * PC Created on Jun 20, 2011 by Spyros Voulgaris, modified to PCSA on June 27, 2011 by Demeter Kiss

 * Demeter: I used arrays of booleans, since since java does not support variable length arrays, and array of Bitsets produced errors
 */
package netsize;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class PCSA extends Algorithm
{
  private static final String PAR_I = "I";
  private static final String PAR_M = "m";
  
  // Parameter for all nodes
  private static int I;
  private static int m;
  
  // Parameters specific to a node
  private boolean[][] bitarrays;
  
  
  public PCSA(String prefix)
  {
    I = Configuration.getInt(prefix+"."+PAR_I);
    m = Configuration.getInt(prefix+"."+PAR_M);
  }



  public void init()
  {
    // Create my bit
    bitarrays = new boolean[I][m];
    
    // distribute the nodes to k buckets
    int bucket = CommonState.r.nextInt(I);
    
    // Pick a bit b with probability 1/2^b
    int b = 1;
    while (CommonState.r.nextBoolean())
      b++;

    //update bitarrays with own values
    bitarrays[bucket][Math.min(b-1,m-1)] = true; 
  }

  
  
  public void deliverMessage(Object message)
  {
    // receive message
    boolean[][] received_bitarrays = (boolean[][])(message);
    
    //update bitarrays using the message
    for (int i = 0; i < I; i++) {
      for (int j = 0; j < m; j++) {
        bitarrays[i][j] = bitarrays[i][j] | received_bitarrays[i][j];
      }
    }    
  }



  public Object prepareMessage()
  {
    // send all the bitarrays
    return bitarrays.clone();
  }



  public double getEstimate()
  {
    // compute the estimate for PCSA from the paper
    double  avgtrailOne = 0;
    int trailOne = 0;
    for (int i = 0; i < I; i++) {
      trailOne = 0;
      for (int j = 0; j < m; j++) {
        if (bitarrays[i][j])
          trailOne++;
        else
          break;
      }        
      avgtrailOne = avgtrailOne + trailOne;
    }
    avgtrailOne = avgtrailOne / I;
    return 1.29281 * I * Math.pow(2, avgtrailOne);
       
  }



  @Override
  public int getMessageSize()
  {
    return (int) Math.ceil(I*m/8.0);
  }
}
