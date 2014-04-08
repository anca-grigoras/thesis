/*
 * Created on Aug 25, 2007 by Spyros Voulgaris
 *
 */
package gossip.descriptor;

import peernet.core.Descriptor;
import peernet.core.Node;

public class DescriptorAge extends Descriptor
{
  private static final long serialVersionUID = 2418672101965696827L;

  protected int age;



  public DescriptorAge(Node node, int pid)
  {
    super(node ,pid);
  }

  public int getAge()
  {
    return age;
  }

  public void incAge()
  {
    age++;
  }

  public void resetAge()
  {
    age = 0;
  }
  
  public String toString()
  {
    return ""+address+"-"+age;
  }
}