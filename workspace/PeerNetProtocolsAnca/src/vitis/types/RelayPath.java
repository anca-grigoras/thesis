/**
 * 
 */
package vitis.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import peernet.core.Descriptor;
import topics.Topic;

/**
 * @author anca
 *
 */
public class RelayPath implements Serializable, Cloneable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private HashMap<Topic, Vector<Descriptor>> relayTo;
  private HashMap<Topic, Descriptor> receiveFrom;
  
  /**
   * 
   */
  public RelayPath()
  {
    relayTo = new HashMap<Topic, Vector<Descriptor>>();
    receiveFrom = new HashMap<Topic, Descriptor>();
  }
  
  /**
   * 
   * @param t
   * @param requester
   */
  public void addRelayTo(Topic t, Descriptor requester) {
    Vector<Descriptor> peers;
    
    if(!relayTo.containsKey(t)) {
      peers = new Vector<Descriptor>();
      peers.add(requester);
      relayTo.put(t, peers);
    }
    else {
      peers = relayTo.get(t);
      if (!peers.contains(requester)) {
        peers.add(requester);
        relayTo.put(t, peers);
      }
    }
  }
  
  /**
   * 
   * @param t
   * @param nextHop
   * @return
   */
  public Descriptor addRelayRequest(Topic t, Descriptor nextHop) {
    Descriptor toUnsubscribe = null;
    
    if (receiveFrom.containsKey(t) && ! receiveFrom.get(t).equals(nextHop))
      toUnsubscribe = receiveFrom.get(t);
    
    receiveFrom.put(t, nextHop);
    
    return toUnsubscribe;
  }
  
  public Descriptor removeRelayRequest(Topic topic) {
    Descriptor toUnsubscribe = receiveFrom.get(topic);
    receiveFrom.remove(topic);
    
    return toUnsubscribe;
  }
  
  public Descriptor removePath(Topic t, Descriptor requester, Descriptor self) {
    Descriptor toUnsubscribe = null;
    Vector<Descriptor> tempList;
    if(relayTo.get(t) != null && relayTo.get(t).contains(requester)) {
      tempList = relayTo.get(t);
      tempList.remove(requester);
      if (!tempList.isEmpty())
        relayTo.put(t, tempList);
      else {
        relayTo.remove(t);
        toUnsubscribe = receiveFrom.get(t);
        receiveFrom.remove(t);
      }
    } else if (relayTo.get(t) == null) {
      toUnsubscribe = receiveFrom.get(t);
      receiveFrom.remove(t);
    }
    
    return toUnsubscribe;
  }
  
  public Descriptor getReceiveFrom(Topic topic) {
    return receiveFrom.get(topic);
  }
  
  public Vector<Topic> getRelayTopics() {
    Vector<Topic> relayTopics = new Vector<Topic>();
    relayTopics.addAll(relayTo.keySet());
    return relayTopics;
  }
  
  public Vector<Descriptor> getRelayNeighbors(Topic t) {
    Vector<Descriptor> peers = new Vector<Descriptor>();
    
    if (relayTo.get(t) != null)
      peers.addAll(relayTo.get(t));
    
    if (receiveFrom.get(t) != null)
      peers.add(receiveFrom.get(t));
    
    return peers;
  }
  
  public Object clone()
  {
    Object obj = null;
    try
    {
      obj = super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
    }
    return obj;
  }
}
