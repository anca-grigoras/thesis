package vitis.descriptor;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;
import peernet.core.Descriptor;
import peernet.core.Node;
import topics.Topic;
import topics.TopicsRepository;
import vitis.types.LeaderInfo;
import gossip.descriptor.DescriptorAge;

public class DescriptorProfile extends DescriptorAge implements Comparable<Descriptor>
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  protected long id;
  private int numOfSmallWorldLinks = 1;
  private HashMap<Topic,Vector<LeaderInfo>> topicInfo = new HashMap<Topic,Vector<LeaderInfo>>();
  private HashMap<Topic, Descriptor> interests = new HashMap<Topic, Descriptor>(); //<topic, leader>
  private Vector<Topic> asArelay = new Vector<Topic>();
  
  public DescriptorProfile(Node node, int pid)
  {
    super(node ,pid);
    id = getID();
  }
  
 
  /**
   * 
   * @return
   */
  public Vector<Topic> getTopics()
  {
    if (id != -1)
      return TopicsRepository.getTopics(id);
    return null;
  }
  
  /**
   * 
   * @param descr
   * @return
   */
  public Vector<Topic> getCommonTopics(Descriptor descr)
  {
    Vector<Topic> commonTopics = new Vector<Topic>();
    Vector<Topic> descrTopics = ((DescriptorProfile)descr).getTopics();

    for (Topic topic : getTopics())
      if(descrTopics.contains(topic))
        commonTopics.add(topic);
    return commonTopics;
  }
  
  /**
   * The method returns the number of common topics between two nodes
   * @param descr
   * @return
   */
  public int getNbCommonTopics(Descriptor descr)
  {
    return getCommonTopics(descr).size();
  }
    
  /**
   * Verifies if the node is subscribed to topic
   * @param topic
   * @return
   */
  public boolean isSubscribedTo(Topic topic)
  {
    Vector<Topic> myTopics = getTopics();
    if(myTopics.contains(topic))
      return true;
    return false;
  }
  
  /**
   * Maybe make it void, not boolean
   * @param topic
   * @return
   */
  public boolean subscribeToTopic(Topic topic)
  {
    topicInfo.put(topic, new Vector<LeaderInfo>());
    return true;
  }
  
  /**
   * Maybe make it void, not boolean
   * @param topic
   * @return
   */
  public boolean unsubscribeFromTopic(Topic topic)
  {
    topicInfo.remove(topic);
    return true;
  }
  
  public void setLeaders(Descriptor self, TreeSet<Descriptor> neighbors) { //the gateways
    Descriptor leader;
    Vector<Descriptor> interestedNeighbors = new Vector<Descriptor>();
    for (Topic topic : getTopics())
    {
      for (Descriptor d : neighbors)
        if (((DescriptorProfile)d).isSubscribedTo(topic))
          interestedNeighbors.add(d);
      
      leader = findLeaderParent(self, topic, interestedNeighbors);
      interests.put(topic, leader);
    }
  }
  
  /**
   * @param self
   * @param topic
   * @param neighbors
   * @return
   */
  private Descriptor findLeaderParent(Descriptor self, Topic topic, Vector<Descriptor> neighbors)
  {
    Descriptor leader = self;
    Vector<LeaderInfo> invalidInfo = new Vector<LeaderInfo>();
    Vector<LeaderInfo> candidates = new Vector<LeaderInfo>();
    Vector<LeaderInfo> myFinalInfo = new Vector<LeaderInfo>();
    Vector<LeaderInfo> neighborInfo;
    Vector<LeaderInfo> connectedInfo;
    
    candidates.add(new LeaderInfo(self,self,self,0));
    
    for (Descriptor d : neighbors) {
      if (((DescriptorProfile)d).isSubscribedTo(topic) && betterThan(d.getID(), leader.getID(), topic.getId())) {
        candidates.add(new LeaderInfo(d, self, self, 0));
        leader = d;
      }
    }
    
    for (Descriptor d : neighbors) {
      neighborInfo = ((DescriptorProfile)d).getLeaderInfo(topic);
      if (neighborInfo != null) 
        for (LeaderInfo l : neighborInfo) {
          if (!l.parent.equals(self) && !candidates.contains(new LeaderInfo(l.leader, l.directlyConnected, d, l.hops)) &&
              !candidates.contains(new LeaderInfo(l.leader, self, d, l.hops))) {
            LeaderInfo ll = new LeaderInfo(l.leader, l.directlyConnected, d, l.hops+1);
            candidates.add(ll);
          }
        }
    }
    
    //find the invalid entries
    for (LeaderInfo l : candidates) {
      if (l.hops > 5)
        invalidInfo.add(l);
      // if it is directly connected, but not connected anymore
      else if (!l.leader.equals(self) && l.directlyConnected.equals(self) && !neighbors.contains(l.leader) && !invalidInfo.contains(l))
        invalidInfo.add(l);
      else if (neighbors.contains(l.directlyConnected) && !invalidInfo.contains(l)) {            // if the node who was directly connected, says it's not connected any more
          connectedInfo = ((DescriptorProfile)l.directlyConnected).getLeaderInfo(topic);
          if (connectedInfo!= null && !connectedInfo.contains(l))
            invalidInfo.add(l);
          else if (betterThan(l.leader.getID(), leader.getID(), topic.getId()))
            leader = l.leader;
        } else if (betterThan(l.leader.getID(), leader.getID(), topic.getId()))
          leader = l.leader;
        
    }
    
    for (LeaderInfo l : invalidInfo)
      candidates.remove(l);

    for (LeaderInfo l : candidates) {
      if (l.leader.equals(leader) && !myFinalInfo.contains(l))
        myFinalInfo.add(l);
    }

    topicInfo.put(topic, myFinalInfo);
    
    return leader;
  }

  public Descriptor getLeader(Topic topic)
  {
    return interests.get(topic);
  }
  
  public Vector<LeaderInfo> getLeaderInfo(Topic topicId) {
    return topicInfo.get(topicId);
  }
  
  public void setNumOfSmallWorldLinks(int numOfSmallWorldLinks) {
    this.numOfSmallWorldLinks = numOfSmallWorldLinks;
  }

  public int getNumOfSmallWorldLinks() {
    return numOfSmallWorldLinks;
  }  
  
  public void clearRelayInterests() {
    asArelay.clear();
  }

//------------------------------------------------------------------------  
  public void addRelayInterest(Topic topic) {
    if (!asArelay.contains(topic))
      asArelay.add(topic);
  }

//------------------------------------------------------------------------  
  public Vector<Topic> getRelayInterests() {
    return asArelay;
  }
  
  /**
   * no no no no!!!
   * @param peer1
   * @param peer2
   * @param topicId
   * @return
   */
  public boolean betterThan(long peer1, long peer2, long topicId) {
    boolean result = false;
    if ((peer1 < peer2 && peer1 >= topicId) ||
      (peer1 < peer2 && peer1 < topicId && peer2 < topicId) ||
      (peer1 > peer2 && peer2 < topicId && peer1 >= topicId))
      result = true;
        
    return result;
  }


  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Descriptor d)
  {
    return (int)(this.getID()-d.getID());
  }
}
