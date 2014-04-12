package poldercast.protocols;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Network;
import peernet.core.Node;
import peernet.transport.Address;
import poldercast.descriptor.DescriptorTopics;
import poldercast.topics.RoutingTable;
import topics.Topic;

public class CopyOfRings extends Gossip
{
  RingsSettings ringsSettings;
  
  public HashMap<Topic,RoutingTable> rtMap = new HashMap<Topic, RoutingTable>();
  
  public CopyOfRings(String prefix)
  {
    super(prefix);
    ringsSettings = (RingsSettings)settings;
    
    view = new Vector<Descriptor>();
    controlMessages = 0;
  }

  @Override 
  public void nextCycle(Node node, int protocolID)
  {
    Descriptor selfDescr = createDescriptor();
    //System.out.println("i am in next cycle in rings "+node.getID());
    
    Vector<Descriptor> neighborsFromAllProtocols;
    
    if (rtMap.isEmpty())
    {
      neighborsFromAllProtocols = collectAllNeighbors(node, protocolID);
      selectToKeep(ringsSettings.viewLen/2, selfDescr, neighborsFromAllProtocols); 
      insertToView();
    }  
    
    //COLLECT ALL NEIGHBORS (CYCLON, VICINITY, RINGS)
    neighborsFromAllProtocols = collectAllNeighbors(node, protocolID);
    
    for (Descriptor d: view)
      ((DescriptorTopics)d).incAge();
     
    Descriptor selectToGossip = selectGossip();
   
    if(selectToGossip == null)
      return;   
   
    Vector<Topic> commontop = ((DescriptorTopics)selfDescr).getCommonTopics(selectToGossip);
    int index = CommonState.r.nextInt(commontop.size());
    Topic gossipTopic = commontop.get(index);
    
    Vector<Descriptor> subscribers = getSubscribersCommonTopic(neighborsFromAllProtocols, gossipTopic);
    subscribers.add(selfDescr);subscribers.remove(selectToGossip);
    Vector<Descriptor> neighborsToSend = selectToSend(ringsSettings.gossipLen/2, selectToGossip, subscribers, gossipTopic,  null);
    
    //UPDATE VIEW
    selectToKeep(ringsSettings.viewLen/2, selfDescr, neighborsFromAllProtocols); 
    insertToView();
    
    //System.out.println("i am sending a message to " + selectToGossip.getID());
    //System.out.println("my pid is " + protocolID);
    controlMessages++;
    Message msg = new Message();
    msg.type = Message.Type.GOSSIP_REQUEST;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    msg.topic = gossipTopic;
    send(selectToGossip.address, protocolID, msg);
    
  }
  
  /**
   * @param myTopics
   * @return
   */
  private Topic selectTopic(Vector<Topic> myTopics)
  {
    int min = Integer.MAX_VALUE;
    Topic selectedTopic = null;
    for (Topic topic : myTopics)
      if (topic.getAge() < min)
      {
        min = topic.getAge();
        selectedTopic = topic;
      }
    return selectedTopic;
  }

  /**
   * 
   */
  private void insertToView()
  {
    view.clear();
    Set<Entry<Topic,RoutingTable>> set = rtMap.entrySet();
    Iterator<Entry<Topic,RoutingTable>> it = set.iterator();
    while(it.hasNext())
    {
      Entry<Topic,RoutingTable> entry = it.next();
      RoutingTable rt = entry.getValue();
      for (int i = 0; i< rt.getPred().size(); i++)
        if (!view.contains(rt.getPred().get(i)))
          view.add(rt.getPred().get(i));
      for (int i = 0; i< rt.getSucc().size(); i++)
        if (!view.contains(rt.getSucc().get(i)))
          view.add(rt.getSucc().get(i));
    }
  }

  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {
    //System.out.println("rings received from " + src);
        
    Message msg = (Message) event;
    controlMessages++;
    msg.sender.address = src; // Set the sender's address
    switch (msg.type)
    {
      case GOSSIP_REQUEST:
        processGossipRequest(msg.sender, node, pid, msg.descriptors, msg.topic);
        break;
      case GOSSIP_RESPONSE:
        processResponse(node, pid, msg.descriptors, msg.topic);
    }    
  }
  
  private void processGossipRequest(Descriptor sender, Node node, int pid, Vector<Descriptor> received, Topic topic)
  {
    Descriptor selfDescr = createDescriptor();
    ((DescriptorTopics)selfDescr).resetAge();
    
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, pid);    
    
   // neighborsFromAllProtocols.remove(sender);//remove is not working. the descriptors are diff. use index

    Vector<Descriptor> subscribers = getSubscribersCommonTopic(neighborsFromAllProtocols, topic);
    subscribers.add(selfDescr);subscribers.remove(sender);
    Vector<Descriptor> neighborsToSend = selectToSend(ringsSettings.gossipLen/2, sender, subscribers, topic, received);
    
    neighborsFromAllProtocols.addAll(received);
    eliminateDuplicates(neighborsFromAllProtocols);
    
    selectToKeep(ringsSettings.viewLen/2, selfDescr, neighborsFromAllProtocols);
    insertToView();
    
    controlMessages++;
    Message msg = new Message();
    msg.type = Message.Type.GOSSIP_RESPONSE;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    msg.topic = topic;
    send(sender.address, pid, msg);
    
  }  
  
  private void processResponse(Node node, int pid, Vector<Descriptor> received, Topic topic)
  {
    Vector<Descriptor> neighborsFromAllProtocols;
    Descriptor selfDescr = createDescriptor();
    
    neighborsFromAllProtocols = collectAllNeighbors(node, pid);
    neighborsFromAllProtocols.addAll(received);
    eliminateDuplicates(neighborsFromAllProtocols);
   
    selectToKeep(ringsSettings.viewLen/2, selfDescr, neighborsFromAllProtocols);
    insertToView();
  } 
  
  public Descriptor selectGossip()
  {
    DescriptorTopics maxAgeDescr = null;

    if (maxAgeDescr == null)
      for (Descriptor d: view)
        if (maxAgeDescr==null || ((DescriptorTopics) d).getAge() > maxAgeDescr.getAge())
          maxAgeDescr = (DescriptorTopics) d;
    
    return maxAgeDescr;
  }
  
  /**
   * Puts together all neighbors of this protocol and all linked protocols.
   * Descriptors are NOT necessarily cloned. The idea is that preparing a
   * collection of all neighbors should be fast, and cloning should be mandatory
   * when selecting out of these neighbors either to feed my view, or to send
   * to my peer.
   * 
   * @param selfNode
   * @param selfDescr
   * @param myView 
   * @param pid
   * @return Returned descriptors are NOT guaranteed to be cloned.
   */
  private Vector<Descriptor> collectAllNeighbors(Node selfNode, int pid)
  {
    // If no protocols are linked, return the view, as is.
    if (!settings.hasLinkable())
      return view;

    Vector<Descriptor> neighborsFromAllProtocols = new Vector<Descriptor>();

    if (view != null)
      neighborsFromAllProtocols.addAll(view);

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
        Descriptor d = null;
        d = (Descriptor) descr.clone();

        ((DescriptorTopics) d).resetAge(); // Since Vicinity uses hops in a different context, reset it.
        neighborsFromAllProtocols.add(d);
      }
    }
    eliminateDuplicates(neighborsFromAllProtocols);
    return neighborsFromAllProtocols;
  }
  
  private void eliminateDuplicates(Vector<Descriptor> descriptors)
  {
    int i = 0, j = 0;
    boolean isRemoved = false;
    while (i < descriptors.size()-1)
    {
     j = i+1;
      isRemoved = false;
      while (j < descriptors.size())
      {
        if (((DescriptorTopics)descriptors.get(i)).equals((DescriptorTopics)descriptors.get(j)))
          if (resolveDuplicate(descriptors.get(i), descriptors.get(j)) < 0)
          {
            descriptors.remove(j);
            j--;
          }else{
            descriptors.remove(i);
            isRemoved = true;
            continue;
          }
        j++;
      }
      if (!isRemoved)
        i++;
    }
  }    
  
  public static int resolveDuplicate(Descriptor a, Descriptor b)
  {  
    return ((DescriptorTopics)a).getAge()-((DescriptorTopics)b).getAge();
  }
    
  /**
   * Returns all the subscribers that are subscribed to the common topics between the two descriptors
   * @param selfDescr
   * @param selectToGossip
   * @param subscribers
   * @return
   */
  private Vector<Descriptor> getSubscribersCommonTopic(Vector<Descriptor> subscribers, Topic commonTopic)
  {    
    Vector<Descriptor> bestSubscribers = new Vector<Descriptor>();
    for (Descriptor d : subscribers)
    {
      if (((DescriptorTopics)d).isSubscribedTo(commonTopic))
        bestSubscribers.add(d);
    }
    
    Collections.sort(bestSubscribers, new Comparator<Descriptor>()
    {

      @Override
      public int compare(Descriptor o1, Descriptor o2)
      {
        return (int) (o1.getID()-o2.getID());
      }
    });
    return bestSubscribers;
  }
  
  /**
   * 
   * @param ringLinks
   * @param descr
   * @param subscribers
   * @param topic
   * @return
   */
  private void selectToKeep(int ringLinks, Descriptor descr, Vector<Descriptor> subscribers)
  {    
   
   Vector<Topic> topics = ((DescriptorTopics)descr).getTopics();
    
   for (Topic t : topics)
   {
     Vector<Descriptor> pred = new Vector<Descriptor>();
     Vector<Descriptor> succ = new Vector<Descriptor>();
     Vector<Descriptor> candidates = new Vector<Descriptor>();
     
     RoutingTable rt = getRT(t);
     if (rt != null)
     {
       candidates.addAll(rt.getPred());
       candidates.addAll(rt.getSucc());
     }
     for (Descriptor d : subscribers)
     {
       if (((DescriptorTopics)d).isSubscribedTo(t) && !candidates.contains(d))
         candidates.add(d);
     }
     if (candidates.isEmpty())
       continue;
     
    // eliminateDuplicates(candidates);
     candidates.add(descr);
     Collections.sort(candidates, new Comparator<Descriptor>()
    {

      @Override
      public int compare(Descriptor o1, Descriptor o2)
      {
        return (int) (o1.getID()-o2.getID());
      }
    });
     
   //get the succ
     succ = getRingLinks(candidates, ringLinks, descr);
      
     Collections.reverse(candidates);
     pred = getRingLinks(candidates, ringLinks, descr);
     //Collections.reverse(pred); //so that the order is the one from the link
     
     setOptimalLinks(pred, succ, descr, ringLinks);
     
     rtMap.put(t, new RoutingTable(pred, succ));
     
     candidates.clear();
     /*pred.clear();
     succ.clear();*/
   }
 
  }
  
  private Vector<Descriptor> getRingLinks(Vector<Descriptor> candidates, int ringLinks, Descriptor descr)
  {
    Vector<Descriptor> links = new Vector<Descriptor>();
    int index = candidates.indexOf(descr);
    int size = candidates.size();
    for (int i = index +1; i<= index+ ringLinks; i++)
      links.add(candidates.get(i%size));
    eliminateDuplicates(candidates);
    links.remove(descr);
    return links;
  }
  
  private void setOptimalLinks (Vector<Descriptor> pred, Vector<Descriptor> succ, Descriptor descr, int size)
  {
    
    int i = 0;
    while (i < pred.size())
    {
      if (succ.contains(pred.get(i)))
        if (belongsToPred(pred.get(i),descr))
        {
          succ.remove(pred.get(i));
          i++;
        }
        else
          pred.remove(i);
      else i++;
    }
    
    if (pred.size() == size && succ.size() == 0)
    {
      succ.add(pred.get(0));
      pred.remove(0);
      return;
    }
    if (succ.size() == size && pred.size() == 0)
    {
      pred.add(succ.get(succ.size()-1));
      succ.remove(succ.size()-1);
      return;
    }
  }
  
  /**
   * @param descriptor
   * @param descr 
   * @return
   */
  private boolean belongsToPred(Descriptor neighbor, Descriptor descr)
  {
    int diagonal = (int) ((Network.size()/2+descr.getID()))%Network.size();
    if (neighbor.getID() < descr.getID() || neighbor.getID() >= diagonal)
      return true;
    return false;
  }

  /**
   * 
   * @param gossipLen
   * @param gossipChoice
   * @param neighbors
   * @param exclude
   * @return
   */
  private Vector<Descriptor> selectToSend(int gossipLen, Descriptor gossipChoice, Vector<Descriptor> subscribers, Topic commonTopic, Vector<Descriptor> exclude)
   {    
     Vector<Descriptor> toReturn = new Vector<Descriptor>();
     Vector<Descriptor> candidates = new Vector<Descriptor>();
     if (exclude != null)
       for (Descriptor d : subscribers)
         if (!exclude.contains(d))
           candidates.add(d);
     if (candidates.isEmpty()) return toReturn;
     int index = candidates.indexOf(gossipChoice);
     
     for (int i = index+1 ; i<= index + gossipLen; i++)
       toReturn.add(candidates.get(i%candidates.size()));
     
     Collections.sort(candidates, new Comparator<Descriptor>()
    {

      @Override
      public int compare(Descriptor o1, Descriptor o2)
      {
        // TODO Auto-generated method stub
        return (int) (o2.getID()-o1.getID());
      }
    });
     
     index = candidates.indexOf(gossipChoice);
     for (int i = index+1; i<= index+gossipLen; i++)
       toReturn.add(candidates.get(i%candidates.size()));
     toReturn.remove(gossipChoice);
     eliminateDuplicates(toReturn);
     /*if (toReturn.size() > gossipLen)
     {
       Collections.shuffle(toReturn);
       for (int i = toReturn.size()-1; i>= gossipLen; i--)
         toReturn.remove(i);
     }*/
     return toReturn;
   }
  
  public int degreePerTopic(Topic topic)
  {
    int degree = 0;
    RoutingTable rt = rtMap.get(topic);
    if(rt != null)
    {
      if(rt.getPred() != null)
        degree += rt.getPred().size();
      if(rt.getSucc() != null)
        degree += rt.getSucc().size();  
    }
    
    return degree; 
  } 
  
 
  /**
   * @param topic
   * @return
   */
  public RoutingTable getRT(Topic topic)
  {
    return rtMap.get(topic);
  }

  /**
   * @param topic
   * @return
   */
  public RoutingTable getRingsNeighbors(Topic topic)
  {
    return rtMap.get(topic);
  }

  /**
   * @param node
   * @param pid
   * @param topic
   * @return
   */
  public Vector<Descriptor> getRandomNeighbors(Node node, int pid, Topic topic)
  {
    
 // If no protocols are linked, return the view, as is.
    if (!settings.hasLinkable())
      return null;

    Vector<Descriptor> randomNeighbors = new Vector<Descriptor>();

    // Then collect neighbors from linked protocols
    for (int i=0; i<settings.numLinkables(); i++)
    {
      int linkableID = settings.getLinkable(i);
      Linkable linkable = (Linkable) node.getProtocol(linkableID);
      // Add linked protocol's neighbors
      for (int j = 0; j<linkable.degree(); j++)
      {
        // We have to clone it, to change its hops without affecting Cyclon.
        Descriptor descr = (Descriptor) linkable.getNeighbor(j);
        if (((DescriptorTopics)descr).isSubscribedTo(topic) && !randomNeighbors.contains(descr))
        {
          Descriptor d = null;
          d = (Descriptor) descr.clone();

          ((DescriptorTopics) d).resetAge(); // Since Vicinity uses hops in a different context, reset it.
          randomNeighbors.add(d);
        }
      }
    }
    Collections.shuffle(randomNeighbors);
    
    return randomNeighbors;
  }
  
  @Override
  public Object clone()
  {
    CopyOfRings rings = (CopyOfRings)super.clone();
    rings.rtMap = (HashMap<Topic,RoutingTable>)rtMap.clone();
    rings.view = (Vector<Descriptor>)view.clone();
    return rings;
  }

  /* (non-Javadoc)
   * @see peernet.core.Linkable#addNeighbor(peernet.core.Descriptor)
   */
  @Override
  public boolean addNeighbor(Descriptor neighbour)
  {
    if (contains(neighbour))
      return false;

    view.add(neighbour);
    return true;
  }
}