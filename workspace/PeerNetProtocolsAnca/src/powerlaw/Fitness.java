package powerlaw;

import java.util.Random;

public abstract class Fitness
{
  protected double minFitness = Double.MAX_VALUE;
  protected double maxFitness = Double.MIN_VALUE;

  /** Random Number Generator for generating fitness values */
  public Random rngFitness = new Random();

  public abstract double generateFitnessValue();


  public abstract int expectedDegree(double fitness);


  public abstract double vicinityFunction(double fitNode, double fitPeer);


  public double minFitness() {return minFitness;}
  public double maxFitness() {return maxFitness;}
}
