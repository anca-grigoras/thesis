package powerlaw;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Network;





public class PowerlawFitness extends Fitness
{
  private final static String PAR_LOW = "lowest_fitness";
  private final static String PAR_HIGH = "highest_fitness";
  private final static String PAR_ALPHA = "alpha";

  public static double low;
  public static double high;
  public static double alpha;
  public static double denominator;



  public PowerlawFitness(String prefix)
  {
    low = Configuration.getDouble(prefix+"."+PAR_LOW);
    high = Configuration.getDouble(prefix+"."+PAR_HIGH);
    alpha = Configuration.getDouble(prefix+"."+PAR_ALPHA);
    denominator = (high-low)*(high-low);
    //denominator = 42*42;
  }



  @Override
  public double generateFitnessValue()
  {
    // Initializing fitness with a random value in [L,H)
    double uniform = CommonState.r.nextDouble();  // get random number in [0,1)
    double lowToAlpha = Math.pow(low, alpha);
    double highToAlpha = Math.pow(high, alpha);
    return Math.pow(-(uniform*(highToAlpha-lowToAlpha)-highToAlpha)/
        (lowToAlpha*highToAlpha), -1/alpha);
  }



  @Override
  public int expectedDegree(double fitness)
  {
    return (int) Math.rint(100*Network.size()*fitness/(high*high)/(alpha-1));
  }



  @Override
  public double vicinityFunction(double fitNode, double fitPeer)
  {
    return fitNode*fitPeer/denominator;
  }
}
