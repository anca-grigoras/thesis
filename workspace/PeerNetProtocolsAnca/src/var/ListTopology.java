/*
 * Created on Oct 6, 2009 by Spyros Voulgaris
 *
 */
package var;

import gossip.descriptor.DescriptorSimAge;

import java.util.Collections;
import java.util.Vector;

import peeremu.config.Configuration;
import peeremu.core.Control;
import peeremu.core.Linkable;
import peeremu.core.Network;
import peeremu.core.Node;

public class ListTopology implements Control
{
  /**
   *  Protocol ID.
   */
  private static final String PAR_PROTOCOL = "protocol";
  

  private int pid;
  public static ListTopology instance = null;

  
  public ListTopology(String prefix)
  {
    pid = Configuration.getPid(prefix+"."+PAR_PROTOCOL);
    instance = this;
  }

  public boolean execute()
  {
    for (int i=0; i<Network.size(); i++)
    {
      Node node = Network.get(i);
      Linkable prot = (Linkable)node.getProtocol(pid);

      Vector<DescriptorSimAge> neighbors = new Vector<DescriptorSimAge>(); 
      for (int j=0; j<prot.degree(); j++)
        neighbors.add((DescriptorSimAge)prot.getNeighbor(j));
      Collections.sort(neighbors);
      System.out.print("Node "+node+"\t");

      long maxNeighborId = neighbors.elementAt(neighbors.size()-1).getID();
      for (long l=0; l<=maxNeighborId; l++)
      {
        boolean found = false;
        for (DescriptorSimAge n: neighbors)
          if (n.equals(l))
          {
            found = true;
            System.out.print(n);
          }
        if (!found)
          System.out.print("   ");
        System.out.print(" ");
      }
      System.out.println();
    }
    System.out.println("\n");
    return false;
  }
}
