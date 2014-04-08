/*
 * Created on May 14, 2010 by Spyros Voulgaris
 *
 */
package var;

import peeremu.config.Configuration;
import peeremu.core.CommonState;
import peeremu.core.Control;
import peeremu.core.Linkable;
import peeremu.core.Network;

public class TargetOverlayObserver implements Control
{
  private static String PAR_PROTOCOL = "protocol";

  private int pid;


  public TargetOverlayObserver(String prefix)
  {
    pid = Configuration.getPid(prefix+"."+PAR_PROTOCOL);
  }
  
  @Override
  public boolean execute()
  {
    int totalGoodLinks = 0;
    int totalBadLinks = 0;
    int completeNodes = 0;

    for (int i=0; i<Network.size(); i++)
    {
      int nodeId = (int)Network.get(i).getID();
      int goodLinks = 0;
      int badLinks = 0;
      Linkable prot = (Linkable) Network.get(i).getProtocol(pid);
      for (int j=0; j<prot.degree(); j++)
      {
        int peerId = (int)prot.getNeighbor(j).getID();
        if (TargetOverlay.isEdge(nodeId, peerId))
          goodLinks++;
        else
          badLinks++;
      }
      if (goodLinks >= TargetOverlay.k)   // ugly!
        completeNodes++;
      totalGoodLinks += goodLinks;
      totalBadLinks += badLinks;
    }

    System.out.println(CommonState.getTime()+"\t"+completeNodes+"\t"+((double)totalGoodLinks)/Network.size());
    
    if (completeNodes == Network.size())
      return true;
    else
      return false;
  }
}
