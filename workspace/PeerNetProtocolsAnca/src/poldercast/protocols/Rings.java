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

public class Rings extends Gossip
{
  RingsSettings ringsSettings;
  
  public HashMap<Topic,RoutingTable> rtMap = new HashMap<Topic, RoutingTable>();
  
  public Rings(String prefix)
  {
    super(prefix);
    ringsSettings = (RingsSettings)settings;
    
    view = new Vector<Descriptor>();
    controlMessages = 0;
    sentMessages = new HashMap<Message, Long>();
    receivedMessages = new HashMap<Message, Long>();
  }

  @Override 
  public void nextCycle(Node node, int protocolID)
  {
    Descriptor selfDescr = createDescriptor();
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, protocolID);
    
   //System.out.println(node.getID() + "----------");
   /* for (Descriptor d : neighborsFromAllProtocols)
    {
     System.out.print(d.getID() + " ");
     ((DescriptorTopics)d).printId();
    }
    System.out.println("----------");*/
   
    selectToKeep(ringsSettings.viewLen/2, selfDescr, neighborsFromAllProtocols); 
    insertToView();
      
    for (Descriptor d: view)
      ((DescriptorTopics)d).incAge();
     
    Descriptor selectToGossip = selectToGossip();
   
    if(selectToGossip == null)
      return;   
   
    Vector<Topic> commontop = ((DescriptorTopics)selfDescr).getCommonTopics(selectToGossip);
    
    if (commontop.size() == 0)
    {
      System.out.println("This is not ok");
      return;
    }
    
    int index = CommonState.r.nextInt(commontop.size());
    Topic gossipTopic = commontop.get(index);
    
    neighborsFromAllProtocols.add(selfDescr); neighborsFromAllProtocols.remove(selectToGossip);
    Vector<Descriptor> subscribersToCommonTopic = getSubscribersCommonTopic(neighborsFromAllProtocols, gossipTopic);
    Vector<Descriptor> neighborsToSend = selectToSend(ringsSettings.gossipLen, ringsSettings.viewLen/2, selfDescr, selectToGossip, neighborsFromAllProtocols, subscribersToCommonTopic, gossipTopic,  null);
    
    controlMessages++;
    
    Message msg = new Message();
    msg.id = Long.parseLong(selfDescr.getID()+""+CommonState.getTime()+selectToGossip.getID());
    msg.type = Message.Type.GOSSIP_REQUEST;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    msg.topic = gossipTopic;
    //sentMessages.put(msg,CommonState.getTime());
    send(selectToGossip.address, protocolID, msg);
    
  }
  
  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {    
    Message msg = (Message) event;
    controlMessages++;
    //receivedMessages.put(msg,CommonState.getTime());
    msg.sender.address = src; // Set the sender's address
    switch (msg.type)
    {
      case GOSSIP_REQUEST:
        processGossipRequest(msg.sender, node, pid, msg.descriptors, msg.topic);
        break;
      case GOSSIP_RESPONSE:
        processResponse(msg.sender, node, pid, msg.descriptors, msg.topic);
    }    
  }
  
  private void processGossipRequest(Descriptor sender, Node node, int pid, Vector<Descriptor> received, Topic topic)
  {
    Descriptor selfDescr = createDescriptor();
    //if (received.contains(selfDescr)) System.out.println("received contains self descr and it shouldn't");
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, pid);    
    neighborsFromAllProtocols.addAll(received);
    eliminateDuplicates(neighborsFromAllProtocols);
    selectToKeep(ringsSettings.viewLen/2, selfDescr, neighborsFromAllProtocols);
    insertToView();
    
    neighborsFromAllProtocols.add(selfDescr);neighborsFromAllProtocols.remove(sender);
    Vector<Descriptor> subscribersToReceivedTopic = getSubscribersCommonTopic(neighborsFromAllProtocols, topic);
    
    Vector<Descriptor> neighborsToSend = selectToSend(ringsSettings.gossipLen, ringsSettings.viewLen/2, selfDescr, sender, neighborsFromAllProtocols, subscribersToReceivedTopic, topic, received);
        
    controlMessages++;
    
    Message msg = new Message();
    msg.id = Long.parseLong(selfDescr.getID()+""+CommonState.getTime()+sender.getID());
    msg.type = Message.Type.GOSSIP_RESPONSE;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    msg.topic = topic;
    //sentMessages.put(msg,CommonState.getTime());
    send(sender.address, pid, msg);
    
  }  
  
  private void processResponse(Descriptor sender, Node node, int pid, Vector<Descriptor> received, Topic topic)
  {
    Descriptor selfDescr = createDescriptor();
    //if (received.contains(selfDescr)) System.out.println("received contains self descr and it shouldn't");
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, pid);
    neighborsFromAllProtocols.addAll(received);
    if (view.contains(sender)) neighborsFromAllProtocols.add(sender);
    eliminateDuplicates(neighborsFromAllProtocols);
   
    selectToKeep(ringsSettings.viewLen/2, selfDescr, neighborsFromAllProtocols);
    insertToView();
  } 
  
  public Descriptor selectToGossip()
  {
    DescriptorTopics maxAgeDescr = null;

    if (maxAgeDescr == null)
      for (Descriptor d: view)
        if (maxAgeDescr==null || ((DescriptorTopics) d).getAge() > maxAgeDescr.getAge())
          maxAgeDescr = (DescriptorTopics) d;
    
    return maxAgeDescr;
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
     //System.out.println(descr.getID() + " " + t.getId());
     for (Descriptor d : subscribers)
     {
      /* System.out.print("\t"+d.getID()+" -> ");
       for (Topic to : ((DescriptorTopics)d).getTopics())
         System.out.print(to.getId() + " ");
       System.out.println();*/
       if (((DescriptorTopics)d).isSubscribedTo(t)) 
       {
         //System.out.println("is good " + d.getID());
           if (candidates.contains(d))
           {
             Descriptor tmp = candidates.get(candidates.indexOf(d));
             if (resolveDuplicate(tmp, d) > 0)
             {
               candidates.remove(tmp);
               candidates.add(d);
             }
           }
           else candidates.add(d);
       }
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
   * @param howMany : how many nodes can be sent for gossiping
   * @param k : how many neighbors a node can have for a topic 
   * @param source
   * @param destination 
   * @param neighbors : the neighbors from all protocols
   * @param subscribersToCommonTopic 
   * @param commonTopic
   * @param exclude
   * @return
   */
  private Vector<Descriptor> selectToSend(int howMany, int k, Descriptor source, Descriptor destination, Vector<Descriptor> neighbors, 
      Vector<Descriptor> subscribersToCommonTopic, Topic commonTopic, Vector<Descriptor> exclude)
   {    
    
     Vector<Descriptor> toReturn = new Vector<Descriptor>();
     Vector<Descriptor> candidates = new Vector<Descriptor>();
           
     if (rtMap.get(commonTopic) != null)
     {
       for (Descriptor d : rtMap.get(commonTopic).getPred())
       {
         if (d.equals(destination))
           continue;
         candidates.add(d);
       }
       for (Descriptor d : rtMap.get(commonTopic).getSucc())
       {
         if (d.equals(destination))
           continue;
         candidates.add(d);
       }
     }
     
     for (Descriptor d : subscribersToCommonTopic)
     {
       if (candidates.contains(d))
         continue;
       candidates.add(d);
     }     
      
     if (candidates.size() > 2*k)
       toReturn.addAll(selectBestKNeighbors(candidates, destination, k));
     else 
       toReturn.addAll(candidates);
     
     getCandidatesFromOtherTopics(toReturn, exclude, source, destination, neighbors, commonTopic, howMany, k);
     
     
    /* toReturn.addAll(selectBestKNeighbors(candidates, destination, k));
     getCandidatesFromOtherTopics(toReturn, exclude, source, destination, neighbors, commonTopic, howMany, k);
     for (Descriptor d : candidates)
     {
       if (!toReturn.contains(d) && toReturn.size() < howMany)
         toReturn.add(d);
       if (toReturn.size() == howMany)
         break;
     }*/
     
     //if (neighbors.contains(destination)) System.out.println("toreturn contains destination and it shouldn't");
     return toReturn;
   }
  
  /**
   * @param candidates
   * @param gossipChoice
   * @param k 
   */
  private Vector<Descriptor> selectBestKNeighbors(Vector<Descriptor> candidates, Descriptor gossipChoice, int k)
  {
    if (candidates.isEmpty()) return null;

    //if (candidates.contains(gossipChoice)) System.out.println("Candidates contains gossip choice and it shouldn't");
    candidates.add(gossipChoice);
    Collections.sort(candidates, new Comparator<Descriptor>()
        {

      @Override
      public int compare(Descriptor o1, Descriptor o2)
      {
        // TODO Auto-generated method stub
        return (int) (o1.getID()-o2.getID());
      }
        });

    int index = candidates.indexOf(gossipChoice);

    Vector<Descriptor> bestKNeighbors = new Vector<Descriptor>();

    for (int i = index+1 ; i<= index + k; i++)
      bestKNeighbors.add(candidates.get(i%candidates.size()));

    Collections.reverse(candidates);    
    index = candidates.indexOf(gossipChoice);
    for (int i = index+1; i<= index+k; i++)
      bestKNeighbors.add(candidates.get(i%candidates.size()));

    while (bestKNeighbors.contains(gossipChoice))
      bestKNeighbors.remove(gossipChoice);

    if (bestKNeighbors.contains(gossipChoice)) System.out.println("best neighbors contains gossip choice and it shouldn't");
    eliminateDuplicates(bestKNeighbors);

    return bestKNeighbors;
  }
  
  /**
   * @param toReturn 
   * @param exclude 
   * @param source
   * @param destination 
   * @param subscribers : neighbors from all protocols
   * @param commonTopic
   * @param gossipLen
   * @param k 
   */
  private void getCandidatesFromOtherTopics(Vector<Descriptor> toReturn, Vector<Descriptor> exclude, Descriptor source, Descriptor destination, 
      Vector<Descriptor> subscribers, Topic commonTopic, int gossipLen, int k)
  {
    //Vector<Topic> commonTopics = ((DescriptorTopics)source).getCommonTopics(destination);
    Vector<Topic> commonTopics = ((DescriptorTopics)destination).getTopics();
    for (Topic t : commonTopics)
    {
      if (t.equals(commonTopic))
        continue;
      Vector<Descriptor> topicNeighbors = new Vector<Descriptor>();
      if (rtMap.get(t) != null)
      {
        for (Descriptor d : rtMap.get(t).getPred())
        {
          if (d.equals(destination))
            continue;
          topicNeighbors.add(d);
        }
        for (Descriptor d : rtMap.get(t).getSucc())
        {
          if (d.equals(destination))
            continue;
          topicNeighbors.add(d);
        }
      }
      
      for (Descriptor d : subscribers)
        if (((DescriptorTopics)d).isSubscribedTo(t))
        {
          if (!topicNeighbors.contains(d))
            topicNeighbors.add(d);
        }     
      
      if (topicNeighbors.isEmpty())
        continue;
      Vector<Descriptor> bestk = selectBestKNeighbors(topicNeighbors, destination, k);
      
      for (Descriptor d : bestk)
      {
        if ((!toReturn.contains(d) && toReturn.size() < gossipLen))
          toReturn.add(d);
        
        if (toReturn.size() == gossipLen)
          return;
      }
    }
    
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
  
  @Override
  public Object clone()
  {
    Rings rings = (Rings)super.clone();
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
