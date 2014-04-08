/*
 * Created on Oct 11, 2009 by Spyros Voulgaris
 *
 */
package var;

import peeremu.core.GeneralNode;

public class NodeNano extends GeneralNode
{
  public NodeNano(String prefix)
  {
    super(prefix);
  }
  
  public String toString()
  {
    return ""+(char)(getID()+65);
  }
}
