package powerlaw;

import java.util.Random;





public class PowerLaw
{
  double[] nodesFitness;
  int[] nodesDegree;
  double lambda;
  int nodes;
  Random fitnessRandom, vicinityRandom;
  long fitnessSeeder, vicinitySeeder;
  double denom;
  int[] degreeHisto;
  int maxDegree = 0;
  double miu, sigma, epsilon;
  double maxFit; // = Double.MIN_NORMAL;
  double minFit; // = Double.MAX_VALUE; //10000
  double xM = Double.MIN_VALUE;
  double xm = Double.MAX_VALUE;



  PowerLaw(double lambda, int noNodes, double low, double high)
  {
    nodesFitness = new double[noNodes];
    nodesDegree = new int[noNodes];
    this.lambda = lambda;
    minFit = low;
    maxFit = high;
    if (maxFit==0)
    {
      maxFit = Double.POSITIVE_INFINITY;
    }
    nodes = noNodes;
    fitnessSeeder = 0;
    fitnessRandom = new Random(fitnessSeeder);
    vicinitySeeder = 0;
    vicinityRandom = new Random(vicinitySeeder);
  }



  PowerLaw(double lambda, int noNodes, double low, double high, long fitnessSeeder, long vicinitySeeder)
  {
    this(lambda, noNodes, low, high);
    if (fitnessSeeder!=0)
    {
      this.fitnessSeeder = fitnessSeeder;
      fitnessRandom = new Random(fitnessSeeder);
    }
    if (vicinitySeeder!=0)
    {
      this.vicinitySeeder = vicinitySeeder;
      vicinityRandom = new Random(vicinitySeeder);
    }
    System.out.println("fitnessSeeder="+this.fitnessSeeder+" ; vicinitySeeder="+this.vicinitySeeder);
  }



  void generateFitnessLowerUpperBounded()
  {
    double expr1 = Math.pow(minFit, 1-lambda);
    double expr2 = Math.pow(maxFit, 1-lambda)-expr1;
    double xM = Double.MIN_VALUE;
    double xm = Double.MAX_VALUE;
    for (int i = 0; i<nodes; i++)
    {
      // nodesFitness[i] = miu + sigma/epsilon * (Math.pow(r.nextDouble(),
      // -epsilon) - 1);
      nodesFitness[i] = Math.pow(expr2*fitnessRandom.nextDouble()+expr1, 1/(1-lambda));
      if (xM<nodesFitness[i])
      {
        xM = nodesFitness[i];
      }
      if (xm>nodesFitness[i])
      {
        xm = nodesFitness[i];
      }
      System.out.println("fitness["+i+"]="+nodesFitness[i]);
    }
    System.out.println("minFit: "+minFit+"; maxFit: "+maxFit+"; xM: "+xM+"; xm: "+xm);
  }



  void generateFitnessLowerBounded()
  {
    for (int i = 0; i<nodes; i++)
    {
      // nodesFitness[i] = miu + sigma/epsilon * (Math.pow(r.nextDouble(),
      // -epsilon) - 1);
      nodesFitness[i] = Math.pow(1-fitnessRandom.nextDouble(), 1/(1-lambda));
      if (xM<nodesFitness[i])
      {
        xM = nodesFitness[i];
      }
      if (xm>nodesFitness[i])
      {
        xm = nodesFitness[i];
      }
      // System.out.println("fitness[" + i + "]=" + nodesFitness[i]);
    }
    System.out.println("minFit: "+minFit+"; maxFit: "+maxFit+"; xM: "+xM+"; xm: "+xm);
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



  void generateEdgesCaldarelli()
  {
    denom = xM*xM;
    for (int i = 0; i<nodes; i++)
    {
      for (int j = i+1; j<nodes; j++)
      {
        double fij = (nodesFitness[i]*nodesFitness[j])/denom;
        if (vicinityRandom.nextDouble()<fij)
        {
          nodesDegree[i]++;
          nodesDegree[j]++;
          if (maxDegree<nodesDegree[i])
            maxDegree = nodesDegree[i];
          if (maxDegree<nodesDegree[j])
            maxDegree = nodesDegree[j];
        }
      }
    }
    System.out.println("maximum degree is: "+maxDegree);
  }



  void generateEdgesBebe()
  {
    for (int i = 0; i<nodes; i++)
    {
      for (int j = i+1; j<nodes; j++)
      {
        double fij = Math.pow((nodesFitness[i]*nodesFitness[j])/((nodesFitness[i]+1)*(nodesFitness[j]+1)), (double) 2/3)/Math.sqrt(nodes);
        // System.out.println("fij[" + i +"]["+j+"]=" + fij);
        if (vicinityRandom.nextDouble()<fij)
        {
          nodesDegree[i]++;
          nodesDegree[j]++;
          if (maxDegree<nodesDegree[i])
            maxDegree = nodesDegree[i];
          if (maxDegree<nodesDegree[j])
            maxDegree = nodesDegree[j];
        }
      }
    }
    System.out.println("maximum degree is: "+maxDegree);
  }



  void sortDegrees()
  {
    degreeHisto = new int[maxDegree+1];
    for (int i = 0; i<nodes; i++)
    {
      degreeHisto[nodesDegree[i]]++;
    }
    for (int j = 1; j<=maxDegree; j++)
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
    PowerLaw graph = new PowerLaw(Double.parseDouble(args[0]), Integer.parseInt(args[1]),
        Double.parseDouble(args[2]), Double.parseDouble(args[3]),
        Long.parseLong(args[4]), Long.parseLong(args[5]));
    graph.generateFitnessLowerBounded();
    graph.checkLambda();
    graph.generateEdgesBebe();
    graph.sortDegrees();
  }
}
