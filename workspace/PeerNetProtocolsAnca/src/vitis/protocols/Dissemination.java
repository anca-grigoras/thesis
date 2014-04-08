/**
 * 
 */
package vitis.protocols;

import java.util.HashMap;
import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.core.Protocol;
import peernet.transport.Address;
import topics.Topic;
import vitis.protocols.Dissemination;
import vitis.protocols.DisseminationSettings;
import vitis.pubsub.TopicEvent;
import vitis.types.Statistics;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class Dissemination extends Protocol
{
  public HashMap<Long, Integer> messageDigest;
  boolean isInitialized = false;
  DisseminationSettings disSettings;
  HashMap<Topic, Boolean> topicMessageFwd;
  private Vector<TopicEvent> receivedEvents;
  private HashMap<TopicEvent,Vector<Descriptor>> sentEvents;
  private Statistics statistics;
  private final long joiningTime;
  
  /**
   * @param prefix
   */
  public Dissemination(String prefix)
  {
    super(prefix);
    messageDigest = new HashMap<Long, Integer>();
    disSettings = (DisseminationSettings)settings;
    topicMessageFwd = new HashMap<Topic, Boolean>();
    receivedEvents = new Vector<TopicEvent>();
    sentEvents = new HashMap<TopicEvent,Vector<Descriptor>>();
    statistics = new Statistics();
    joiningTime = CommonState.getTime();
  }
 
  
  /* (non-Javadoc)
   * @see peernet.core.Protocol#processEvent(peernet.transport.Address, peernet.core.Node, int, java.lang.Object)
   */
  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {
    return;
    /*TopicEvent te = (TopicEvent)event;
    if (this.receivedEvents.contains(event))
    {
      //System.out.println("duplicate");
      //still store it for counting the duplicates
      return;
    }
    Descriptor selfDescr = createDescriptor();
    Descriptor source = te.getPublisher();
    Topic topic = te.getTopic();
    //System.out.println("   "+node.getID() + " received a message for topic " + topic.getId() + " with id " + event.getId());
    TopicEvent newTe = (TopicEvent) te.clone();
    newTe.incrementHopCounts();
    Vector<Descriptor> path = new Vector<Descriptor>();
    path.addAll(te.getPath());
    path.add(selfDescr);
    newTe.setPath(path);
    newTe.setSender(selfDescr);
    receivedEvents.add(te);
    
    Vitis vitis = (Vitis)node.getProtocol(disSettings.rtPid);
    Vector<Descriptor> interestedPeers = vitis.findInterestedPeers(source.getID(), topic);
    sentEvents.put(newTe, interestedPeers);
    //System.out.println(selfDescr.getID()+ " sends for topic " + topic.getId()+ " -> " + interestedPeers.size());
    int i = 0;
    for (Descriptor d : interestedPeers)    
      if (!d.equals(te.getSender()) ) { 
        send(d.address, pid, newTe);
        i++;
      }*/
  }
  
  public void resetReceivedMessages()
  {
    receivedEvents.clear();
  }
  
  public void resetSentMessages()
  {
    sentEvents.clear();
  }
  
  public HashMap<TopicEvent, Vector<Descriptor>> getSentMessages()
  {
    return sentEvents;
  }
  
  /*private Vector<Descriptor> findInterestedPeers(Node node, int pid, Descriptor self, Topic topic)
  {
    Vector<Descriptor> interestedPeers = new Vector<Descriptor>();
    
    Vector<Descriptor> friends = collectAllNeighbors(node, self, pid);
    
    if (friends != null)
      for (Descriptor d : friends)
      {
        if (((DescriptorProfile)d).isSubscribedTo(topic))
          interestedPeers.add(d);
        if (((DescriptorProfile)d).getRelayInterests().contains(topic))
          if (!interestedPeers.contains(d))
            interestedPeers.add(d);
      }
    
    Vitis vitis = (Vitis) node.getProtocol(disSettings.rtPid);
    if (vitis.getRelayNeighbors(topic) != null) {
      for (Descriptor d : vitis.getRelayNeighbors(topic)) {
        if (d != null) {
          if (!interestedPeers.contains(d))
            interestedPeers.add(d);
        }
      }
    }
    
    interestedPeers.remove(self);
    //interestedPeers.remove(src);
    
    return interestedPeers;
  }*/
  
  public Statistics getStatistics() {
    int relayEvents = 0;
    int relevantEvents = 0;
    int hopCounts = 0;
    float coverage = 1;
    
    int numOfPublishedEvents = 0;
    int size;
    
    Vector<Topic> myTopics = TopicsRepository.getTopics(node.getID());
    size = this.receivedEvents.size();
    
    for (TopicEvent event : this.receivedEvents) {
      if (!myTopics.contains(event.getTopic()))
        relayEvents++;
      else {
        hopCounts += event.getHopCounts();
        relevantEvents++;
      }
    }
    
    numOfPublishedEvents = myTopics.size(); //one event per topic
    
    coverage = (numOfPublishedEvents == 0 ? 1 : (float)size/(float)numOfPublishedEvents);
    
    this.statistics.setNumReceivedEvents(size);
    if (size > 0) {
      this.statistics.setRelayTraffic(relayEvents*100/size);
      this.statistics.setAvgHopCounts(relevantEvents > 0 ? hopCounts/relevantEvents : 0);
      this.statistics.setCoverage(coverage);
    }else {
      this.statistics.setRelayTraffic(0);
      this.statistics.setAvgHopCounts(0);
      this.statistics.setCoverage(1);
    }
    
    return this.statistics;
  }

  /* (non-Javadoc)
   * @see peernet.core.Protocol#nextCycle(peernet.core.Node, int)
   */
  @Override
  public void nextCycle(Node node, int protocolID)
  {
    // TODO Auto-generated method stub
  }
  
  /**
   * @return the messageDigest
   */
  public HashMap<Long, Integer> getMessageDigest()
  {
    return messageDigest;
  }

  /**
   * @return the isInitialized
   */
  public boolean isInitialized()
  {
    return isInitialized;
  }
  /**
   * @return the topicMessageFwd
   */
  public HashMap<Topic, Boolean> getTopicMessageFwd()
  {
    return topicMessageFwd;
  }
  
  public void setEventState(Topic topic)
  {
    topicMessageFwd.put(topic, false);
  }


  /**
   * @return the disSettings
   */
  public DisseminationSettings getDisSettings()
  {
    return disSettings;
  }
  
  private Vector<Descriptor> collectAllNeighbors(Node selfNode, Descriptor selfDescr, int pid)
  {
    // If no protocols are linked, return the view, as is.
    if (!settings.hasLinkable())
      return null;

    Vector<Descriptor> neighborsFromAllProtocols = new Vector<Descriptor>();

    // Then collect neighbors from linked protocols
    for (int i=0; i<settings.numLinkables(); i++)
    {
      int linkableID = settings.getLinkable(i);
      Linkable linkable = (Linkable) selfNode.getProtocol(linkableID);
      // Add linked protocol's neighbors
      for (int j = 0; j<linkable.degree(); j++)
      {
        // We have to clone it, to change its hops without affecting Cyclon.
        Descriptor descr = (Descriptor) linkable.getNeighbor(j);
        if(!neighborsFromAllProtocols.contains(descr))
        {
          Descriptor d = null;
          d = (Descriptor) descr.clone();
          neighborsFromAllProtocols.add(d);
        }
      }
    }    
    return neighborsFromAllProtocols;
  }

  public long getJoiningTime() {
    return this.joiningTime;
  }

  public Vector<TopicEvent> getReceivedEvents() {
    return this.receivedEvents;
  }
  
  @Override
  public Object clone()
  {
    Dissemination diss = null;
    diss = (Dissemination) super.clone();
    diss.messageDigest = (HashMap<Long, Integer>)messageDigest.clone();
    diss.isInitialized = false;
    diss.topicMessageFwd = (HashMap<Topic,Boolean>) topicMessageFwd.clone();
    diss.receivedEvents = (Vector<TopicEvent>)receivedEvents.clone();
    diss.sentEvents = (HashMap<TopicEvent,Vector<Descriptor>>)sentEvents.clone();
    return diss;
  }
}
