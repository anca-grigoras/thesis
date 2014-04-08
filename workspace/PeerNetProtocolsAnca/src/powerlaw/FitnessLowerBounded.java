/*
 * Created on Feb 15, 2013 by Spyros Voulgaris
 *
 */
package powerlaw;


public class FitnessLowerBounded extends Fitness
{
  private double lambda;
  private int nodes;

  public FitnessLowerBounded(int nodes, double lambda)
  {
    this.nodes = nodes;
    this.lambda = lambda;
  }



  @Override
  public double generateFitnessValue()
  {
    // fitness = μ + σ/ε * (Math.pow(r.nextDouble(),-ε) - 1);
    double fitness = Math.pow(rngFitness.nextDouble(), 1/(1-lambda));

    minFitness = Math.min(minFitness, fitness);
    maxFitness = Math.max(maxFitness, fitness);

    return fitness;
  }



  @Override
  public int expectedDegree(double fitness)
  {
    assert false: "expected degree not estimated for this fitness distribution";
    return 0;
  }



  @Override
  public double vicinityFunction(double fitA, double fitB)
  {
    return Math.pow((fitA*fitB)/((fitA+1)*(fitB+1)), (double) 2/3) / Math.sqrt(nodes);
  }
}
