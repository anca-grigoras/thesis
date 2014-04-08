/*
 * Created on Jun 27, 2011 by Demeter Kiss
 *
 * Note that the use of levels parameter can be omitted by using linked lists
 *
 */
package netsize;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class MRB extends Algorithm
{
  private static final String PAR_M = "m";
  private static final String PAR_LEVELS = "levels";
  
  // Parameters specific to a node
  private boolean[][] list;

  // Parameters for all nodes
  private static int m;
  private static int levels;
  

  public MRB(String prefix)
  {
    m = Configuration.getInt(prefix+"."+PAR_M);
    levels = Configuration.getInt(prefix+"."+PAR_LEVELS);
  }



  public void init()
  {
    // Declare the table of hashed ids 
    list = new boolean[levels][m];

    // Initialise by the own hashed entry
    int b = 0;
    while (CommonState.r.nextBoolean())
      b++;
    int c = CommonState.r.nextInt(m);
    
    list[Math.min(b, (levels - 1))][c] = true;   
    
  }



  public void deliverMessage(Object message)
  {
    // get the message
    boolean[][] received_message = (boolean[][])message;
    
    // update list
    int i, j;
    for (i = 0; i < levels; i++) {
      for (j = 0; j < m; j++) {
        list[i][j] = received_message[i][j] | list[i][j];        
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
    assert list.length > 0: "The list cannot be empty";
    
    // counting the number of true entries by level
    int base = 0;
    double estim = 0;
    int i , j, d;
    double threshold = (1 - Math.exp(-1.94)) * m;
    
    for (i = 0; i < levels; i++) {
      // count the number of hashed values at level i
      d = 0;      
      for (j = 0; j < m; j++) {
        if (list[i][j])
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
