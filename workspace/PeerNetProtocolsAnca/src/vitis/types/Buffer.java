/**
 * 
 */
package vitis.types;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import peernet.core.Descriptor;
import vitis.descriptor.DescriptorProfile;

/**
 * @author anca
 *
 */
public class Buffer implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final BigInteger N;
  private final Descriptor selfDescr;
  private Vector<Descriptor> peers = new Vector<Descriptor>();
  /**
   * 
   */
  public Buffer(Vector<Descriptor> peers, Descriptor selfDescr, BigInteger maxN)
  {
    this.peers = new Vector<Descriptor>(peers);
    this.selfDescr = selfDescr;
    this.N = maxN;
  }
  
  public void merge (Vector<Descriptor> newPeers) {
    if (newPeers == null)
      return;
    
    for (Descriptor d : newPeers)
    {
      if (d.equals(this.selfDescr))
        continue;
      if (!this.peers.contains(d))
        this.peers.add(d);
      else
        this.peers.set(this.peers.indexOf(d), d);
    }
  }
  
  public Vector<Descriptor> selectNeighbors(Descriptor selfDescr, int size, int numOfSmallWorldLinks, int fingerDistanceSeed)
  {
    if (size < 2) return null;
    
    if (this.peers.size() <= size) return this.peers;
    
    Vector<Descriptor> selectedNeighbors = new Vector<Descriptor>();
    
    Descriptor pred = this.peers.get(0); 
    Descriptor succ = this.peers.get(1); 
    Descriptor finger;
    
    long k = selfDescr.getID();
    long p = pred.getID();
    long s = succ.getID();
    long i;
    
    for (Descriptor d : this.peers)
    {
      i = d.getID();
      if (((i > k) && (p > k) && (i > p)) || ((i < k) && (p > k)) || ((i < k) && (p < k) && (i > p))) {
        pred = d;
        p = pred.getID();
      }
      
      if (((i > k) && (s > k) && (i < s)) || ((i > k) && (s < k)) || ((i < k) && (s < k) && (i < s))) {
        succ = d;
        s = succ.getID();
      } 
    }
    
    selectedNeighbors.add(pred);
    selectedNeighbors.add(succ);
    
    this.peers.remove(pred);
    this.peers.remove(succ);
    
    numOfSmallWorldLinks = numOfSmallWorldLinks -2;
    
    int z = size -2;
    if (this.peers.size() > z) {
      int numOfNeededFriends = z - numOfSmallWorldLinks;
      
      if (numOfNeededFriends > 0) {
        Vector<Descriptor> sortedPeers = sortPeers(selfDescr, this.peers, numOfNeededFriends);
        
        int index;
        for (index = 0; index < numOfNeededFriends; index++)
          selectedNeighbors.add(sortedPeers.get(index));        
      }
      
      int longRangeLinks = size - selectedNeighbors.size();
      if (longRangeLinks != numOfSmallWorldLinks)
        System.err.println("problem in setting the number of small world links"); 
      
      ((DescriptorProfile)selfDescr).setNumOfSmallWorldLinks(longRangeLinks);
      
      double fingerDistance;
      Random rnd = new Random(fingerDistanceSeed);
      for (int c = 0 ; c < longRangeLinks && c < this.peers.size(); c++) {
        fingerDistance = Math.exp(Math.log(N.doubleValue() / 2) * (rnd.nextDouble() - 1)) * N.doubleValue() / 2;
        finger = findPeerAtDistance(selfDescr, fingerDistance, selectedNeighbors);
        if (finger != null)
          selectedNeighbors.add(finger);
      }
    }
    else
      selectedNeighbors.addAll(this.peers);   
   
    return selectedNeighbors;    
  }
  
  
 /**
   * @param descr 
   * @param neighbors
   * @param numOfNeededFriends
   * @return
   */
  private Vector<Descriptor> sortPeers(Descriptor descr, Vector<Descriptor> peersToSort, int numOfNeededFriends)
  {
    DescriptorUtility nodeUtility;
    TreeSet<DescriptorUtility> list = new TreeSet<DescriptorUtility>();
    double utility;
    
    for (Descriptor d: peersToSort) {
      utility = getUtility(descr, d);
      nodeUtility = new DescriptorUtility(d, utility);
      list.add(nodeUtility);  
    }

    Vector<Descriptor> sortedList = new Vector<Descriptor>();
    for (DescriptorUtility du: list) {
      sortedList.add(du.getDescriptor());
    }

    return sortedList;
  }
  
  /** For now, I consider that the distribution is uniform of the topic publication rates.
   * @param node
   * @param descr
   * @param d 
   * @return
   */
  private double getUtility(Descriptor descr, Descriptor d)
  {
    int intersectionValue = ((DescriptorProfile)descr).getNbCommonTopics(d);
    int unionValue = ((DescriptorProfile)descr).getTopics().size() + 
                     ((DescriptorProfile)d).getTopics().size() -
                     intersectionValue;
    
    double utility = (unionValue > 0 ? (double) intersectionValue  / (double) unionValue: 0);
    return utility;
  }
   
   /**
    * @param descr 
   * @param N 
   * @param selectedNeighbors 
   * @param fingerDistance
    * @param selectedNeighbors
    * @return
    */
   private Descriptor findPeerAtDistance(Descriptor descr, double distance, Vector<Descriptor> alreadySelected)
   {
     long f;
     long temp;
     long dist, tempd;
     Descriptor finger = null;
     long self = descr.getID();
     Vector<Descriptor> notSelected = new Vector<Descriptor>();
     
     for (Descriptor d: this.peers)
       if (!alreadySelected.contains(d))
         notSelected.add(d);
     
     if (notSelected.isEmpty())
       return null;
     
     finger = notSelected.get(0);
     f = finger.getID();
     dist = Math.min(Math.abs(self - f), N.intValue() - Math.abs(self-f));
     
     for (int i = 0; i< notSelected.size(); i++)
     {
       temp = notSelected.get(i).getID();
       tempd = Math.min(Math.abs(self - temp), N.intValue() - Math.abs(self - f));
       if (Math.abs(tempd - distance) < Math.abs(dist - distance)) {
         finger = notSelected.get(i);
         f = finger.getID();
         dist = Math.min(Math.abs(self - f), N.intValue() - Math.abs(self - f));
       }
     }
     
     return finger;
   }
   
  
  public Vector<Descriptor> getPeers() {
    return this.peers;
  }
  
  public void print()
  {
    for (Descriptor d : this.peers)
      System.out.print(d + " ");
    System.out.println();
  }
}
