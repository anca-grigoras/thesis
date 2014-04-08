/*
 * Created on Jun 27, 2011 by Demeter Kiss
 *
 */
package netsize;

import java.util.BitSet;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class LinearCounting extends Algorithm
{
  private static final String PAR_SPACE = "space";
  
  // Parameter for all nodes
  private static int space;

  // Parameters specific to a node
  private BitSet recurrence;
  
  
  public LinearCounting(String prefix)
  {
    space = Configuration.getInt(prefix+"."+PAR_SPACE);
  }



  public void init()
  {
    //Create my recurrence vector
    recurrence = new BitSet();
    
    // initialise it with own number
    int myNumber = CommonState.r.nextInt(space);
    recurrence.set(myNumber);
  }

  
  
  public void deliverMessage(Object message)
  {
    // receive message
    BitSet received_recurrence = (BitSet)(message);
    
    // update the recurrence vector
    recurrence.or(received_recurrence);    
  }



  public Object prepareMessage()
  {
    // send all the recurrence vector
    return recurrence.clone();
  }



  public double getEstimate()
  {
    // use the estimator from the paper
    return -space * Math.log(1.0 - (double)recurrence.cardinality() / space);       
  }



  @Override
  public int getMessageSize()
  {
    return (int) Math.ceil(recurrence.size()/8.0);
  }
  
  
}
