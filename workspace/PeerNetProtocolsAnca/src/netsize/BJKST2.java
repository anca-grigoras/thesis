/*
 * Created on Jul 1, 2011 by Demeter Kiss
 *
 */
package netsize;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class BJKST2 extends Algorithm
{
  private static final String PAR_K = "k";
  
  // Parameters specific to a node
  private int[] list;

  // Parameters for all nodes
  private static int k;
  

  public BJKST2(String prefix)
  {
    k = Configuration.getInt(prefix+"."+PAR_K);
  }



  public void init()
  {
    // Declare my list of integers
    list = new int[k];

    // Initialise my list of integers
    for (int i = 0; i < k; i++) {
      int b = 1;
      while (CommonState.r.nextBoolean())
        b++;
      list[i] = b;
    }
  }



  public void deliverMessage(Object message)
  {
    // get the message
    int[] recived_message = (int[])(message);
    
    // update list
    for (int i = 0; i < k; i++) {
      list[i] = Math.max(recived_message[i],list[i]);
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
    int R = list[0];
    
    //count how many are smaller or equal to R
    double counter = 0;
    for (int i = 1; i < k; i++) {
      if (list[i]<R)
        counter++;
    }
    return -Math.pow(2, R - 1) * Math.log(counter / (k-1)); }



  @Override
  public int getMessageSize()
  {
    // TODO Auto-generated method stub
    return 0;
  }

}
