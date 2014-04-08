/*
 * Created on May 14, 2010 by Spyros Voulgaris
 */
package var;

import peeremu.config.Configuration;
import peeremu.core.CommonState;
import peeremu.core.Control;
import peeremu.core.Linkable;
import peeremu.core.Network;
import peeremu.graph.GraphAlgorithms;
import peeremu.graph.GraphFactory;
import peeremu.graph.NeighborListGraph;





public class TargetOverlay implements Control
{
  /**
   * Outdegree of each node
   */
  private static final String PAR_K = "k";

  /**
   * How many hops away a node can "see" the proximity of other nodes.
   * E.g., if sight is set to 2, a node knows that peers at 1 hop distance are
   * "closer" than peers at 2 hops, while it treats any peer of 3, 4, or more
   * hops as equally far (infinite distance).
   */
  private static final String PAR_SIGHT = "sight";


  /**
   * Rewiring probability
   */
  private static final String PAR_P = "p";

  private static int[][] distanceMatrix = null;
  private static NeighborListGraph graph = null;
  public static int k;
  public static double p;
  public static int sight;



  public TargetOverlay(String prefix)
  {
    k = Configuration.getInt(prefix+"."+PAR_K);
    p = Configuration.getDouble(prefix+"."+PAR_P);
    sight = Configuration.getInt(prefix+"."+PAR_SIGHT, Integer.MAX_VALUE);
  }



  @Override
  public boolean execute()
  {
    graph = new NeighborListGraph(Network.size(), false);
    GraphFactory.wireWS(graph, k, p, CommonState.r);

//    IncrementalStats stats = new IncrementalStats();
//    for (int i=0; i<graph.size(); i++)
//      stats.add(GraphAlgorithms.clustering(graph, i));
//    System.out.println("#Clustering: "+stats);

    // Compute the distance between any two nodes in the target overlay.
    distanceMatrix = new int[graph.size()][graph.size()];
    GraphAlgorithms ga = new GraphAlgorithms();
    for (int i=0; i<graph.size(); i++)
    {
      ga.dist(graph, i);
      for (int j=0; j<graph.size(); j++)
      {
        int dist = ga.d[j] <= sight ? ga.d[j] : sight+1;
        distanceMatrix[i][j] = dist;
      }
    }
    
//    for (int i=0; i<graph.size(); i++)
//      System.out.println(distanceMatrix[0][i]);

    return false;
  }


  
  public static int distance(int id1, int id2)
  {
    return distanceMatrix[id1][id2];
  }
  
  public static boolean isEdge(int id1, int id2)
  {
    return distanceMatrix[id1][id2] == 1;
  }



  public static class TargetOverlayObserver implements Control
  {
    private String PAR_PROTOCOL = "protocol";

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
        int goodLinks = 0;
        int badLinks = 0;
        Linkable prot = (Linkable) Network.get(i).getProtocol(pid);
        for (int j=0; j<prot.degree(); j++)
        {
          int peerId = (int)prot.getNeighbor(j).getID();
          if (TargetOverlay.isEdge(i, peerId))
            goodLinks++;
          else
            badLinks++;
          if (goodLinks == 10) //XXX UGLY!
            completeNodes++;
        }
        totalGoodLinks += goodLinks;
        totalBadLinks += badLinks;
      }

      System.out.println(CommonState.getTime()+"\t"+completeNodes+"\t"+((double)totalGoodLinks)/Network.size());
      return false;
    }
  }
}
