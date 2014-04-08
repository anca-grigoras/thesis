/*
 * Created on Jun 20, 2011 by Spyros Voulgaris
 *
 */
package netsize;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import peernet.util.IncrementalStats;

public class SizeEstimation implements Control
{
  private int pid;

  public SizeEstimation(String prefix)
  {
    pid = Configuration.getPid(prefix+"."+PAR_PROTOCOL);
  }


  @Override
  public boolean execute()
  {
    IncrementalStats stats = new IncrementalStats();
    IncrementalStats upload = new IncrementalStats();
    IncrementalStats download = new IncrementalStats();

    for (int i=0; i<Network.size(); i++)
    {
      Node node = Network.get(i);
      NetSize ns = (NetSize) node.getProtocol(pid);

      // Upload and download stats
      upload.add(ns.uploadBytes);
      download.add(ns.downloadBytes);

      // Size estimation stats
      Algorithm alg = ns.getAlgorithm();
      stats.add(alg.getEstimate());
    }

    double relativeError = (stats.getAverage()-stats.getN())/stats.getN();
    System.out.println("Estimation: "+stats+" "+relativeError);
    System.out.println("Upload:     "+upload);
    System.out.println("Download:   "+download);
    
//    System.out.println("Estimation: "+stats.getAverage()+" "+relativeError);
//    System.out.println("Upload:     "+upload.getAverage());
//    System.out.println("Download:   "+download.getAverage());

    System.out.println();
    return false;
  }
}
