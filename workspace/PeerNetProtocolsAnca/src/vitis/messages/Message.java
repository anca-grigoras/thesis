/*
 * Created on Jul 23, 2010 by Spyros Voulgaris
 */
package vitis.messages;

import java.io.Serializable;
import java.util.Vector;

import peernet.core.Descriptor;

public class Message implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1797845507069160137L;
  
  public Descriptor sender;
}