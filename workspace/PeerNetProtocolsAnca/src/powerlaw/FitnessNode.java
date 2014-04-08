/*
 * Created on May 13, 2010 by Spyros Voulgaris
 */
package powerlaw;

import peernet.core.Node;




public class FitnessNode extends Node
{
  public double fitness;
  public int expectedDegree;
  public static Fitness fitnessFunction;



  public FitnessNode(String prefix)
  {
    super(prefix);

    /*
     * this should be changed to load fitness function class specified in config
     * file
     */
    fitnessFunction = new PowerlawFitness(prefix);
  }



  public Object clone()
  {
    FitnessNode newNode = (FitnessNode) super.clone();

    newNode.fitness = fitnessFunction.generateFitnessValue();
    newNode.expectedDegree = fitnessFunction.expectedDegree(newNode.fitness);
    //System.out.println("Fitness: "+newNode.fitness+" expected degree: "+newNode.expectedDegree);

    return newNode;
  }
}
