/**
 * 
 */
package vitis.types;

/**
 * @author anca
 *
 */
public class Statistics
{
  private float relayTraffic;
  private float avgHopCounts;
  private float coverage;
  private int numReceivedEvents;
  /**
   * 
   */
  public Statistics()
  {
    relayTraffic = 0;
    avgHopCounts = 0;
    coverage = 1;
    numReceivedEvents = 0;
  }
//------------------------------------------------------------------------    
  public void setRelayTraffic(float relayTraffic) {
    this.relayTraffic = relayTraffic;
  }
  public float getRelayTraffic() {
    return relayTraffic;
  }
  
  //------------------------------------------------------------------------    
  public void setAvgHopCounts(float avgHopCounts) {
    this.avgHopCounts = avgHopCounts;
  }
  public float getAvgHopCounts() {
    return avgHopCounts;
  }
  
  //------------------------------------------------------------------------    
  public void setCoverage(float coverage) {
    this.coverage = coverage;
  }
  public float getCoverage() {
    return coverage;
  }
  
  //------------------------------------------------------------------------  
  public void setNumReceivedEvents(int numReceivedEvents) {
    this.numReceivedEvents = numReceivedEvents;
  }
  public int getNumReceivedEvents() {
    return numReceivedEvents;
  }
  
}
