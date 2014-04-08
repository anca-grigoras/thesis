package powerlaw;

import java.util.Random;





public class PowerLawGeneric
{
  Fitness fitness = null;

  /** Array containing node fitness values */
  double[] nodesFitness;

  /** Array of node degrees*/
  int[] nodesDegree;

  /** λ */
  double lambda;

  /** Number of nodes */
  int nodes;

  /** Random Number Generator for the vicinity function */
  Random rngVicinity = new Random();

  /** Stores the maximum degree of all nodes */
  int maxDegree = 0;

  /* Other parameters */
  double minFit, maxFit;



  PowerLawGeneric(double lambda, int numNodes, double low, double high)
  {
    // Construct PowerLaw with arbitrary seeders, different in each execution.
    this(lambda, numNodes, low, high, System.currentTimeMillis(), System.currentTimeMillis());
  }



  PowerLawGeneric(double lambda, int numNodes, double low, double high, long fitnessSeed, long vicinitySeed)
  {
    nodesFitness = new double[numNodes];
    nodesDegree = new int[numNodes];
    this.lambda = lambda;

    minFit = low;
    maxFit = high;
    if (maxFit==0)
      maxFit = Double.POSITIVE_INFINITY;

    nodes = numNodes;

    //fitness = new FitnessLowerBounded(nodes, lambda);
    fitness = new FitnessLowerUpperBounded(nodes, lambda, low, high);

    fitness.rngFitness.setSeed(fitnessSeed);
    rngVicinity.setSeed(vicinitySeed);

    System.out.println("fitnessSeeder="+fitnessSeed+" ; vicinitySeeder="+vicinitySeed);
  }



  void generateFitnesses()
  {
    for (int i = 0; i<nodes; i++)
      nodesFitness[i] = fitness.generateFitnessValue();

    System.out.println("minFit: "+minFit+"; maxFit: "+maxFit+"; xM: "+fitness.maxFitness()+"; xm: "+fitness.minFitness());
  }



  void checkLambda()
  {
    double sum = 0.0;
    for (int i = 0; i<nodes; i++)
    {
      sum += Math.log(nodesFitness[i]);
    }
    sum = 1+nodes/sum;
    System.out.println("theoretical lambda is: "+lambda+" ; lambda from generated values is: "+sum);
  }



  void generateEdges()
  {
    for (int i = 0; i<nodes; i++)
    {
      for (int j = i+1; j<nodes; j++)
      {
        double fij = fitness.vicinityFunction(nodesFitness[i], nodesFitness[j]);

        // System.out.println("fij[" + i +"]["+j+"]=" + fij);
        if (rngVicinity.nextDouble() < fij)
        {
          nodesDegree[i]++;
          nodesDegree[j]++;

          maxDegree = Math.max(maxDegree, nodesDegree[i]);
          maxDegree = Math.max(maxDegree, nodesDegree[j]);
        }
      }
    }
    System.out.println("maximum degree is: "+maxDegree);
  }



  void sortDegrees()
  {
    int[] degreeHisto = new int[maxDegree+1];
    for (int i = 0; i<nodes; i++)
    {
      degreeHisto[nodesDegree[i]]++;
    }
    for (int j=1; j<=maxDegree; j++)
    {
      System.out.println(j+"\t"+degreeHisto[j]);
    }
    double sum = 0.0;
    int nonzeroDegreeNodes = 0;
    for (int i = 0; i<nodes; i++)
    {
      if (nodesDegree[i]>0)
      {
        sum += Math.log(nodesDegree[i]);
        nonzeroDegreeNodes++;
      }
    }
    sum = 1+nonzeroDegreeNodes/sum;
    System.out.println("lambda for degree distribution is: "+sum+" ; zeroDegreeNodes="+(nodes-nonzeroDegreeNodes));
  }



  public static void main(String[] args)
  {
    double lambda = Double.parseDouble(args[0]);
    int numNodes = Integer.parseInt(args[1]);
    double low = Double.parseDouble(args[2]);
    double high = Double.parseDouble(args[3]);
    long fitnessSeed = Long.parseLong(args[4]);
    long vicinitySeed = Long.parseLong(args[5]);

    PowerLawGeneric graph = new PowerLawGeneric(lambda, numNodes, low, high, fitnessSeed, vicinitySeed);

    graph.generateFitnesses();
    graph.checkLambda();
    graph.generateEdges();
    graph.sortDegrees();
  }
}
