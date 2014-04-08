/*
 * Created on Jun 27, 2011 by Demeter Kiss
 * 
 */
package netsize;

import java.util.LinkedList;
import peernet.config.Configuration;
import peernet.core.CommonState;

public class MRBlinkedlist extends Algorithm
{
  private static final String PAR_M = "m";
  
  // Parameters specific to a node
  private LinkedList<boolean[]> list;

  // Parameters for all nodes
  private static int m;
  

  public MRBlinkedlist(String prefix)
  {
    m = Configuration.getInt(prefix+"."+PAR_M);
  }



  public void init()
  {
    // Declare the table of hashed ids 
    list = new LinkedList<boolean[]>();

    // Initialise by the own hashed entry
    int b = 0;
    while (CommonState.r.nextBoolean())
      b++;
    int c = CommonState.r.nextInt(m);
    
    //fill up the list with empty linear hash tables
    for (int i = 0; i < b; i++) {
      list.add(new boolean[m]);
    }

    boolean[] dummy = new boolean[m];
    dummy[c] = true;
    list.add(dummy);
    
  }



  public void deliverMessage(Object message)
  {
    // get the message

    LinkedList<boolean[]> received_message = (LinkedList<boolean[]>)message;
    
    // update list
    int i,j;
    boolean[] dummy = new boolean[m];
    boolean[] message_dummy = new boolean[m];
    
    for (i = 0; i < received_message.size(); i++) {
      // if own linked list is small, then add received boolean arrays
      if (i + 1 > list.size())
        list.add(received_message.get(i));
      else {
        // otherwise update them
        dummy = list.get(i);
        message_dummy = received_message.get(i);
        for (j = 0; j < m; j++) {
          dummy[j] = dummy[j] | message_dummy[j];          
        }
        list.set(i, dummy);
      }        
      
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
    assert list.size() > 0: "The list cannot be empty";
    
    // counting the number of true entries by level
    int base = 0;
    double estim = 0;
    int i, j, d;
    double threshold = (1 - Math.exp(-1.94)) * m;
    boolean[] dummy;
    
    for (i = 0; i < list.size(); i++) {
      // count the number of hashed values at level i
      d = 0;
      dummy = list.get(i);
      for (j = 0; j < m; j++) {
        if (dummy[j])
          d++;
      }
      // if there are too many hashed values, then increase the base
      if (d > threshold) {
        base = i + 1;
        estim = 0;
      }
      // otherwise count the hashed values
      else {
        estim = estim - m * Math.log(1 - (double)d / (double)m);
      }      
    }
    
    return estim * Math.pow(2, base);
  }



  @Override
  public int getMessageSize()
  {
    // TODO Auto-generated method stub
    return 0;
  }



  

}
