/*
 * Created on Feb 15, 2013 by Spyros Voulgaris
 *
 */
package powerlaw;



public class FitnessLowerUpperBounded extends Fitness
{
  private double lambda;
  private int nodes;
  private double lower, upper;

  public FitnessLowerUpperBounded(int nodes, double lambda, double lower, double upper)
  {
    this.nodes = nodes;
    this.lambda = lambda;
    this.lower = lower;
    this.upper = upper;
  }



  @Override
  public double generateFitnessValue()
  {
    double expr1 = Math.pow(lower, 1-lambda);
    double expr2 = Math.pow(upper, 1-lambda)-expr1;
    double fitness = Math.pow(expr2*rngFitness.nextDouble()+expr1, 1/(1-lambda));

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
    return Math.pow(fitA*fitB/((fitA+1)*(fitB+1)), (double) 2/3) / Math.sqrt(nodes);
  }
}
