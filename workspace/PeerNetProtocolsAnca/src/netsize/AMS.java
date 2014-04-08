/*
 * Created on Jun 27, 2011 by Demeter Kiss
 *
 */
package netsize;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class AMS extends Algorithm
{
  private static final String PAR_K = "k";
  
  // Parameters specific to a node
  private int[] list;

  // Parameters for all nodes
  private static int k;
  

  public AMS(String prefix)
  {
    k = Configuration.getInt(prefix+"."+PAR_K);
  }

 

  public void init()
  {
    // Declare my list of integers
    list = new int[k];

    // Initialize my list of integers
    int b = 1;
    for (int i = 0; i < k; i++) {
      b = 1;
      while (CommonState.r.nextBoolean())
        b++;
      list[i] = b;
    }
  }



  public void deliverMessage(Object message)
  {
    // get the message
    int[] received_message = (int[])(message);
    
    // update list
    for (int i = 0; i < k; i++) {
      list[i] = Math.max(received_message[i],list[i]);
    }
  }



  public Object prepareMessage()
  {
    // broadcast list
    return list.clone();
  }



  public double getEstimate()
  {
    // use the estimate from the Why go logarithmic article
    assert list.length > 0: "The list cannot be empty";
    double avg = 0;
    for (int i = 0; i < k; i++) {
      avg = avg + list[i];
    }
    avg = avg / k;
    
    return 0.39701 * Math.pow(2., avg);
  }



  @Override
  public int getMessageSize()
  {
    // TODO Auto-generated method stub
    return 0;
  }



  

}
