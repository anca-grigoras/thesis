/**
 * 
 */
package vitis.protocols;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.transport.Address;
import topics.Topic;
import vitis.descriptor.DescriptorProfile;
import vitis.types.RelayPath;
import vitis.messages.VitisMessage;
import gossip.descriptor.DescriptorAge;


/**
 * @author anca
 *
 */
public class Vitis extends Gossip
{  
  public VitisSettings vitisSettings;
  private RelayPath relayPath;
  Vector<Descriptor> oldFriends;
  Vector<Descriptor> fans;

  /**
   * @param prefix
   */
  public Vitis(String prefix)
  {
    super(prefix);
    vitisSettings = (VitisSettings)settings;
    view = new Vector<Descriptor>(vitisSettings.viewSize);
    relayPath = new RelayPath();
    oldFriends = new Vector<Descriptor>();
    fans = new Vector<Descriptor>();
  }

 
  /* (non-Javadoc)
   * @see peernet.core.Protocol#nextCycle(peernet.core.Node, int)
   */
  @Override
  public void nextCycle(Node node, int protocolID)
  {
    /*
    Descriptor selfDescr = createDescriptor();
    //System.out.println(selfDescr.getID());
    if (view.isEmpty())
    {
      Vector<Descriptor> neighborsFromTman = collectAllNeighbors(node, selfDescr, protocolID); //get the tman view -> RT; these are the friends
      insertToView(neighborsFromTman);
    //}
    if (view.isEmpty()) return;
    
    // make up a list of all your neighbors
    TreeSet<Descriptor> neighbors = new TreeSet<Descriptor>(view);
   
    if (!fans.isEmpty()) {
      for (Descriptor np : fans) {
        if (!neighbors.contains(np))
          neighbors.add(np);
      }
    } 
   
     exchangeProfile(selfDescr, neighbors, protocolID);    */     
  }  

  /* (non-Javadoc)
   * @see peernet.core.Protocol#processEvent(peernet.transport.Address, peernet.core.Node, int, java.lang.Object)
   */
  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {
    VitisMessage msg = (VitisMessage) event;
    msg.sender.address = src; // Set the sender's address
    switch (msg.type)
    {
      case REFRESH_FRIENDSHIP:
        refreshFriendship(msg.profile, node, pid);
        break;
      case CANCEL_FRIENDSHIP:
        cancelFriendship(msg.profile, node, pid);
        break;
      case SUBSCRIBE_ON_RELAY_PATH:
        subscribeOnRelayPath(msg.sender, msg.topic, msg.path, pid); //check if the sender is the requester
        break;
      case UNSUBSCRIBE_FROM_RELAY_PATH:
        unsubscribeFromRelayPath(msg.sender, msg.topic, pid);//same thing with the requester
    }   
  }  

  /**
   * @param sender
   * @param node
   */
  private void refreshFriendship(Descriptor requester, Node node, int pid)
  {
    /*//System.out.println(node.getID() + " refresh friendship with " + profile.getID());
    ((DescriptorAge)profile).resetAge();
    Descriptor selfDescr = createDescriptor();
    Vector<Descriptor> neighbors = collectAllNeighbors(node, selfDescr, pid); //get the tman view -> RT; these are the friends
    neighbors.add(profile);
    insertToView(neighbors);

    updateProfile(selfDescr, neighbors, pid);
    refreshRelayPath(selfDescr, neighbors, pid);*/
    
   if (!fans.contains(requester)) 
      fans.add(requester);
  }  
  
  private void cancelFriendship(Descriptor requester, Node node, int pid)
  {
    if (fans.contains(requester))
      fans.remove(requester); 
  }

  /**
   * @param requester
   * @param topic
   * @param path
   */
  private void subscribeOnRelayPath(Descriptor requester, Topic topic, Vector<Descriptor> path, int pid)
  {
    Descriptor selfDescr = createDescriptor();
    
    Descriptor toUnsubscribe;
    this.relayPath.addRelayTo(topic, requester);

    if (!((DescriptorProfile)selfDescr).isSubscribedTo(topic) && this.relayPath.getReceiveFrom(topic) == null) {
      //Vector<Descriptor> neighborsFromTman = collectAllNeighbors(node, selfDescr, pid);
      TreeSet<Descriptor> neighbors = new TreeSet<Descriptor>();
      
      neighbors.addAll(getFriendsAndFans());       
      neighbors.add(selfDescr);

      Descriptor nextHop = findNextHop(topic, neighbors);
      
      if(path.contains(nextHop) || nextHop.equals(selfDescr))
        return;
      else {
        path.add(selfDescr);
        //System.out.println(node.getID() + " recursion: " + path.size());
        toUnsubscribe = this.relayPath.addRelayRequest(topic, nextHop);
        
        //SEND TO NEXTHOP A REQUEST TO SUBSCRIBE ON RELAY PATH: (SELFDESCR, TOPIC, PATH) 
        sendRequest(selfDescr, nextHop, VitisMessage.Type.SUBSCRIBE_ON_RELAY_PATH, topic, path, pid);
        
        if (toUnsubscribe != null && !toUnsubscribe.equals(nextHop))
          sendRequest(selfDescr, nextHop, VitisMessage.Type.UNSUBSCRIBE_FROM_RELAY_PATH, topic, null, pid);
      }
    } 
  }

  /**
   * @param sender
   * @param topic
   */
  private void unsubscribeFromRelayPath(Descriptor requester, Topic topic, int pid)
  {
    Descriptor selfDescr = createDescriptor();
    Descriptor toUnsubscribe = this.relayPath.removePath(topic, requester, selfDescr);
    if (toUnsubscribe != null)
      sendRequest(selfDescr, toUnsubscribe, VitisMessage.Type.UNSUBSCRIBE_FROM_RELAY_PATH, topic, null, pid);
    //else System.out.println("Unsubscribed done");
  }

 
  private void exchangeProfile(Descriptor selfDescr, TreeSet<Descriptor> neighbors, int pid) {
    updateProfile(selfDescr, neighbors, pid);
    refreshRelayPath(selfDescr, neighbors, pid);

    //see what's the thing with the old friends. we just remove them or we also send a 'stop friendship' message

    //send the profile to the alive friends ????
    /*for (Descriptor d : neighbors)
    {
      if (d.equals(selfDescr))
        continue;
      if (((DescriptorAge)d).getAge() > vitisSettings.threshold)
        view.remove(d);
      else {
        ((DescriptorAge)d).incAge();
        VitisMessage msg = new VitisMessage();
        msg.sender = selfDescr;
        msg.profile = selfDescr;
        msg.type = VitisMessage.Type.REFRESH_FRIENDSHIP;
        send(d.address, pid, msg);
      }
    }*/
    
 // send a "stop friendship" message to the old friends, who are not a friend anymore
    for (Descriptor d : oldFriends) {
      if (!view.contains(d)) {
        VitisMessage msg = new VitisMessage();
        msg.sender = selfDescr;
        msg.profile = selfDescr;
        msg.type = VitisMessage.Type.CANCEL_FRIENDSHIP;
        send(d.address, pid, msg);
      }
    }
    
    oldFriends.clear();

    // send your profile to the alive friends
    for (Descriptor d : view) {
      //((DescriptorAge)d).incAge();
      VitisMessage msg = new VitisMessage();
      msg.sender = selfDescr;
      msg.profile = selfDescr;
      msg.type = VitisMessage.Type.REFRESH_FRIENDSHIP;
      send(d.address, pid, msg);
      oldFriends.add(d);
    }

  }

  private void updateProfile(Descriptor selfDescr, TreeSet<Descriptor> neighbors, int pid)
  {
    ((DescriptorProfile)selfDescr).setLeaders(selfDescr, neighbors); //update your own profile
    //printLeaders(selfDescr);


    //if you are a leader (gateway) subscribe on the replay path, otherwise make sure you are not subscribed on any path
    Vector<Descriptor> path;
    Descriptor nextHop, toUnsubscribe;

    neighbors.add(selfDescr);
    //Collections.sort(neighbors, new DescriptorComp());

    for (Topic topic : ((DescriptorProfile)selfDescr).getTopics())
    {
      Descriptor leader = ((DescriptorProfile)selfDescr).getLeader(topic);
      if (selfDescr.equals(leader)) //if it's the leader
      {
        //System.out.print(node.getID() + " " + topic.getId() + " I am the leader ");
        nextHop = findNextHop(topic, neighbors);
        if (!nextHop.equals(selfDescr)) //and it's not the rendezvous point
        {
          toUnsubscribe = this.relayPath.addRelayRequest(topic, nextHop);
          path = new Vector<Descriptor>();
          path.add(selfDescr);
          //SEND TO NEXTHOP A REQUEST TO SUBSCRIBE ON RELAY PATH: (SELFDESCR, TOPIC, PATH) 
          sendRequest(selfDescr, nextHop, VitisMessage.Type.SUBSCRIBE_ON_RELAY_PATH, topic, path, pid);

          if (toUnsubscribe != null && !toUnsubscribe.equals(nextHop))
          {
            //SEND TO TOUNSUBSCRIBE A REQUEST TO UNSUBSCRIBE FROM RELAY PATH: (SELFDESCR, TOPIC);
            sendRequest(selfDescr, toUnsubscribe, VitisMessage.Type.UNSUBSCRIBE_FROM_RELAY_PATH, topic, null, pid);
          }
          //System.out.println("but not the rendezvous point");
        } else
          if ((toUnsubscribe = this.relayPath.getReceiveFrom(topic)) != null) { //but if it is the rendezvous point
            //System.out.println("and also the rendezvous point");
            this.relayPath.removeRelayRequest(topic);
            //SEND TO TOUNSUBSCRIBE A REQUEST TO UNSUBSCRIBE FROM RELAY PATH: (SELFDESCR, TOPIC);
            sendRequest(selfDescr, toUnsubscribe, VitisMessage.Type.UNSUBSCRIBE_FROM_RELAY_PATH, topic, null, pid);
          }
      } else if ((toUnsubscribe = this.relayPath.getReceiveFrom(topic)) != null) { //but if it's not the leader
        this.relayPath.removeRelayRequest(topic);
        //SEND TO TOUNSUBSCRIBE A REQUEST TO UNSUBSCRIBE FROM RELAY PATH: (SELFDESCR, TOPIC);
        sendRequest(selfDescr, toUnsubscribe, VitisMessage.Type.UNSUBSCRIBE_FROM_RELAY_PATH, topic, null, pid);
      }
    }
  }

  private void refreshRelayPath(Descriptor selfDescr, TreeSet<Descriptor> neighbors, int pid) {
    //refresh the relay path
     Descriptor nextHop = null;
    Descriptor toUnsubscribe = null;
    Vector<Descriptor> path;

    ((DescriptorProfile)selfDescr).clearRelayInterests();

    for (Topic topic : this.relayPath.getRelayTopics()) {
      if (!((DescriptorProfile)selfDescr).getTopics().contains(topic)) {
        ((DescriptorProfile)selfDescr).addRelayInterest(topic);
        nextHop = findNextHop(topic, neighbors);

        //if it's not the rendezvous point and the next relay node is a new one
        if (!nextHop.equals(selfDescr) && (this.relayPath.getReceiveFrom(topic) == null ||!nextHop.equals(this.relayPath.getReceiveFrom(topic)))) {
          toUnsubscribe = this.relayPath.addRelayRequest(topic, nextHop);
          path = new Vector<Descriptor>();
          path.add(selfDescr);
          //SEND TO NEXTHOP A REQUEST TO SUBSCRIBE ON RELAY PATH: (SELFDESCR, TOPIC, PATH)
          sendRequest(selfDescr, nextHop, VitisMessage.Type.SUBSCRIBE_ON_RELAY_PATH, topic, path, pid);

          if (toUnsubscribe != null && !toUnsubscribe.equals(nextHop)) {
            //SEND TO TOUNSUBSCRIBE A REQUEST TO UNSUBSCRIBE FROM RELAY PATH: (SELFDESCR, TOPIC);
            sendRequest(selfDescr, toUnsubscribe, VitisMessage.Type.UNSUBSCRIBE_FROM_RELAY_PATH, topic, null, pid);
          }
        }
      }
    }
  }

  /**
   * @param topic
   * @param neighbors
   * @return
   */
  private Descriptor findNextHop(Topic topic, TreeSet<Descriptor> neighbors)
  {
    Descriptor nextHop = neighbors.first();

    for (Descriptor d : neighbors)
      if ((int)d.getID() > topic.getId())
      {
        nextHop = d;
        break;
      }

    return nextHop;
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
   * @param pid
   * @return Returned descriptors are NOT guaranteed to be cloned.
   */
  private Vector<Descriptor> collectAllNeighbors(Node selfNode, Descriptor selfDescr, int pid)
  {
    // If no protocols are linked, return the view, as is.
    if (!settings.hasLinkable())
      return view;

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

  private void sendRequest(Descriptor sender, Descriptor receiver, VitisMessage.Type type, Topic topic, Vector<Descriptor> path, int pid)
  {
    VitisMessage msg = new VitisMessage();
    msg.sender = sender;
    msg.type = type;
    msg.topic = topic;
    if (type.toString().equals("SUBSCRIBE_ON_RELAY_PATH"))
      msg.path = path;
    send(receiver.address, pid, msg);
  }
  
  private void insertToView(Vector<Descriptor> peers)
  {
    view.clear();
    for (Descriptor d : peers)
    {
      view.add(d);
    }
    if (view.size() > 15) System.out.println("view size is not correct");
  }
  
  public Vector<Descriptor> getRelayNeighbors(Topic topic)
  {
    return relayPath.getRelayNeighbors(topic);
  }
  
  private Vector<Descriptor> getFriendsAndFans() {
    Vector<Descriptor> neighbors = new Vector<Descriptor>();
    
    if (!view.isEmpty())
        neighbors.addAll(view);

    if (!fans.isEmpty()) {
      for (Descriptor np : fans) {
        if (!neighbors.contains(np))
          neighbors.add(np);
      }
    }
    
    return neighbors;
  }
  
  public Vector<Descriptor> findInterestedPeers(long src, Topic topic)
  {
    Descriptor self = createDescriptor();
    Vector<Descriptor> interestedPeers = new Vector<Descriptor>();
    Vector<Descriptor> tmp;
    // among your friends
    if (!view.isEmpty()) {
      tmp = getInterestedPeers(topic,view);
      if (!tmp.isEmpty()) 
        interestedPeers.addAll(tmp);
    
      tmp = getInterestedRelayPeers(topic, view);
      if (!tmp.isEmpty()) {
        for (Descriptor np : tmp) {
          if (!interestedPeers.contains(np))
            interestedPeers.add(np);
        }
      }
    }
    
    // or your fans
    if (!fans.isEmpty()) {
      tmp = getInterestedPeers(topic, fans);
      for (Descriptor np : tmp) {
        if (!interestedPeers.contains(np))
          interestedPeers.add(np);
      }
      
      tmp = getInterestedRelayPeers(topic, fans);
      for (Descriptor np : tmp) {
        if (!interestedPeers.contains(np))
        {
          //System.out.println(node.getID() + " interested realy peers : "+np.getID());
          interestedPeers.add(np);
        }
      }
    }
    
    // or on the relay path
    /*tmp = relayPath.getRelayNeighbors(topic);
    if (!tmp.isEmpty()) {
      for (Descriptor p : tmp) {
        if (p != null) {
          if (!interestedPeers.contains(p))
            interestedPeers.add(p);
        }
      }
    }*/
    
    
    interestedPeers.remove(self);
    Iterator<Descriptor> it = interestedPeers.iterator();
    while (it.hasNext())
    {
      Descriptor d = it.next();
      if (d.getID() == src)
        it.remove();
    }   
    eliminateDuplicates(interestedPeers);
    return interestedPeers;   
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
        if (((DescriptorProfile)descriptors.get(i)).equals((DescriptorProfile)descriptors.get(j)))
          {
            descriptors.remove(j);
            j--;
          }/*else{
            descriptors.remove(i);
            isRemoved = true;
            continue;
          }*/
        j++;
      }
      if (!isRemoved)
        i++;
    }
  }  
  
  private synchronized Vector<Descriptor> getInterestedPeers(Topic topic, Vector<Descriptor> peers)
  {
    Vector<Descriptor> interestedPeers = new Vector<Descriptor>();
    for (Descriptor d : peers)
      if (((DescriptorProfile)d).isSubscribedTo(topic))
        interestedPeers.add(d);    
    return interestedPeers;
  }

  private synchronized Vector<Descriptor> getInterestedRelayPeers(Topic topic, Vector<Descriptor> peers) 
  {
    Vector<Descriptor> interestedPeers = new Vector<Descriptor>();

    for (Descriptor d : peers) {
      if (((DescriptorProfile)d).getRelayInterests().contains(topic))
        interestedPeers.add(d);
    }
    return interestedPeers;
  }
  
  public Vector<Descriptor> getFriends()
  {
    return view;
  }
  
  public Vector<Descriptor> getFans()
  {
    return fans;
  }

  private void printLeaders(Descriptor self) {
    System.out.print(self.getID() + " : ");
    for (Topic t : ((DescriptorProfile)self).getTopics())
    {
      Descriptor leader = ((DescriptorProfile)self).getLeader(t);
      System.out.print("<"+t.getId()+","+leader.getID()+">   ");
    }
    System.out.println();
  }

  /* (non-Javadoc)
   * @see peernet.core.Linkable#addNeighbor(peernet.core.Descriptor)
   */
  @Override
  public boolean addNeighbor(Descriptor neighbour)
  {
    if (contains(neighbour))
      return false;

    if (view.size() >= vitisSettings.viewSize)
      throw new IndexOutOfBoundsException();

    view.add(neighbour);
    return true;
  }
  
  @Override
  public Object clone()
  {
    Vitis vitis = (Vitis)super.clone();
    vitis.view = (Vector<Descriptor>)view.clone();
    vitis.oldFriends = (Vector<Descriptor>)oldFriends.clone();
    vitis.fans = (Vector<Descriptor>)fans.clone();
    vitis.relayPath = (RelayPath)relayPath.clone();
    return vitis;
  }
}
