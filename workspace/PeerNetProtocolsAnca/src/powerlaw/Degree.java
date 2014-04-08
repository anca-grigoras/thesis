package powerlaw;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Network;





/**
 * 
 */
public class Degree implements Control
{
  /**
   * The protocol to operate on.
   * 
   * A parameter with the name 'protocol' is expected to be found in the
   * configuration file, in the block defining this Control.
   */
  private static final String PAR_PROT = "protocol";
  protected final int pid;



  /**
   * Standard constructor that reads the configuration parameters. Invoked by
   * the simulation engine only once in the beginning.
   * 
   * @param name the configuration prefix for this class
   */
  public Degree(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROT);

    // Print the fitnesses of the whole network, in descending order
    Vector<Double> fitnesses = new Vector<Double>(Network.size());
    for (int i = 0; i<Network.size(); i++)
    {
      FitnessNode node = (FitnessNode) Network.get(i);
      fitnesses.add(node.fitness);
    }
    Collections.sort(fitnesses);
    for (int i = fitnesses.size()-1; i>=0; i--)
      System.out.println(fitnesses.elementAt(i));
    System.out.println();
    System.out.println();
  }



  /**
   */
  public boolean execute()
  {
//    Vector<Integer> degrees = new Vector<Integer>(Network.size());
//    for (int i = 0; i<Network.size(); i++)
//    {
//      Node node = Network.get(i);
//      PowerlawOverlay prot = (PowerlawOverlay) node.getProtocol(pid);
//      degrees.add(prot.degree());
//    }


    Vector<FitnessNode> nodes = new Vector<FitnessNode>();

    for (int i=0; i<Network.size(); i++)
      nodes.add((FitnessNode)Network.get(i));

    Collections.sort(nodes,
        new Comparator<FitnessNode>()
        {
          public int compare(FitnessNode a, FitnessNode b)
          {
            int expDegA = a.expectedDegree;
            int expDegB = b.expectedDegree;
            if (expDegA==expDegB)
              return 0;
            else
              return expDegA-expDegB<0 ? 1 : -1;
          }
        });

    int missingLinks = 0;
    int extraLinks = 0;
    int incompleteNodes = 0;
    for (FitnessNode n: nodes)
    {
      int deg = ((PowerlawOverlay) n.getProtocol(pid)).degree();
      //System.out.println(n.fitness+"\t"+n.expectedDegree+"\t"+deg);
      missingLinks += Math.max(n.expectedDegree-deg, 0);
      extraLinks += Math.max(deg-n.expectedDegree, 0);
      if (n.expectedDegree != deg)
        incompleteNodes++;
    }

    System.out.println("missing "+missingLinks+"\textra "+extraLinks+"\tincomplete "+incompleteNodes);
    //System.out.println();
    //System.out.println();


//    for (int i = 0; i<Network.size(); i++)
//    {
//      FitnessNode node = (FitnessNode) Network.get(i);
//      PowerlawOverlay prot = (PowerlawOverlay) node.getProtocol(pid);
//      System.out.println(node.fitness+"\t"+node.expectedDegree+"\t"+
//          prot.degree());
//    }
//    System.out.println();
//    System.out.println();
    return false;
  }
}
