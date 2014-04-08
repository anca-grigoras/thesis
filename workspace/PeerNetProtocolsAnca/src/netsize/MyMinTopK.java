/*
 * Created on July 06, 2011 by Wojtek Kowalczyk
 * Another version of MinTopK: the "list of coins" is replaced by an vector of integers
 */
package netsize;

//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.CommonState;

public class MyMinTopK extends Algorithm
{
  private static final String PAR_K = "k";
  private static final String PAR_SPACE = "space";

  // Parameters specific to a node
  private int[] list;
  private int myNumber;
  private int smallest;
  private int pos_smallest; //position of the smallest on the list
  // Parameters for all nodes
  private static int k;
  private static int space;
  private int messageLength;


  public MyMinTopK(String prefix)
  {
    k = Configuration.getInt(prefix+"."+PAR_K);
    space = Configuration.getInt(prefix+"."+PAR_SPACE);
  }



  public void init()
  {
    myNumber = CommonState.r.nextInt(space);
    list = new int[k];
    list[0]=myNumber;
    smallest=0;
    pos_smallest=1;
    //is it needed?
    for (int i=1; i<k; i++)
      list[i]=0; //actually, it should be -1!
  }



  public void deliverMessage(Object message)
  { 
    int[] received_vector = (int[])(message);
    int x;
    for (int i=0; i<received_vector.length;i++)
    {
      x=received_vector[i];
      if (x > smallest)
      {
        list[pos_smallest]=x;
        if (pos_smallest<k-1)
          //there are still 0s available so we have to advance the position
          pos_smallest++;
        else
          smallest=x;
      }
    }
  }



  public Object prepareMessage()
  {
    int[] myMessage;
    myMessage=new int[pos_smallest];
    for (int i=0;i<pos_smallest;i++) myMessage[i]=list[i];
    //return list.clone();
    messageLength=myMessage.length*4; //int takes 4 bytes
    return myMessage;
  }

                                                                                                                                                                                                                                                       
  @Override
  public int getMessageSize()
  {
    return messageLength;
  }


  public double getEstimate()
  {
    if (pos_smallest<k)
      return (double)pos_smallest;
    else
    return (double)(k)/ ((double)(smallest))/((double)(space));
  }



/*  private class Coin implements Comparable<Coin>
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
  }*/

}
