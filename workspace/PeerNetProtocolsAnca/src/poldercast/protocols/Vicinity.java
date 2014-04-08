package poldercast.protocols;

import poldercast.descriptor.DescriptorTopics;
import poldercast.protocols.VicinitySettings;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;
import java.util.AbstractMap.SimpleEntry;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.transport.Address;
import topics.Topic;

public class Vicinity extends Gossip
{

  VicinitySettings vicSettings;
  HashMap<Topic,Integer> topicOccurence;
   
  public Vicinity(String prefix)
  {
    super(prefix);
    vicSettings = (VicinitySettings)settings;

    view = new Vector<Descriptor>(vicSettings.viewLen);
    topicOccurence = new HashMap<Topic,Integer>();
    controlMessages = 0;
    sentMessages = new HashMap<Message, Long>();
    receivedMessages = new HashMap<Message, Long>();
  }

  @Override
  public void nextCycle(Node node, int protocolID)
  {
    Vector<Descriptor> neighborsFromAllProtocols;
    // Acquire fresh own descriptor
    // Need to call getOwnDescriptor, as it may produce gradually changing profiles. This is node P
    Descriptor selfDescr = createDescriptor();
   // ((AddressSim)selfDescr.address).node = node;
    
  //***************** STEP 3:   Merge the VICINITY, CYCLON (and RINGS-- later) views in one Vp = Vvic U Vcyclon (U Vrings) 
    neighborsFromAllProtocols = collectAllNeighbors(node, selfDescr, protocolID);
    
    if (view.isEmpty())
    {
    //update own view
      view = selectToKeep(vicSettings.viewLen, selfDescr, neighborsFromAllProtocols);
    }
    
     //***************** STEP 1:  Increase the hops field of every neighbor by one
    //assert (selfDescr instanceof DescriptorAge): "Vicinity needs DescriptorAge descriptors, or their descendants.";
    for (Descriptor d: view)
      ((DescriptorTopics)d).incAge();  
    
  //***************** STEP 2:   Select neighbor Q with the highest hops among all neighbors, and remove it from the VICINITY view: Vvic = Vvic -{Q} (after step 3). This is node Q
    Descriptor gossipChoice = selectPeer();
    if (gossipChoice == null)
      return;
    
    //***************** STEP 4: Add own descriptor with own profile and hops 0 to the merged view: Vp= Vp U {P} -- I added the own descriptor to neighborsToSend list
    ((DescriptorTopics)selfDescr).resetAge();
    neighborsFromAllProtocols.add(selfDescr);
    
    
    neighborsFromAllProtocols.remove(gossipChoice);
    
    //***************** STEP 5: Strip down Vp to its gvic best descriptors for Q, by applying the selection function from Q's perspective Vp = S(gvic, Q, Vp)
    Vector<Descriptor> neighborsToSend = selectToSend(vicSettings.gossipLen, gossipChoice, neighborsFromAllProtocols, null);
    neighborsFromAllProtocols.remove(selfDescr);
    neighborsFromAllProtocols.add(gossipChoice);
    
    Vector<Descriptor> chosen = keepCandidates(neighborsFromAllProtocols, protocolID, node, selfDescr);
    view = selectToKeep(vicSettings.viewLen, selfDescr, chosen);
    
    controlMessages++;
    //***************** STEP 6: Send Vp to peer Q
    Message msg = new Message();
    msg.id = Long.parseLong(selfDescr.getID()+""+CommonState.getTime()+gossipChoice.getID());
    msg.type = Message.Type.GOSSIP_REQUEST;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    //sentMessages.put(msg,CommonState.getTime());
    send(gossipChoice.address, protocolID, msg);
  }

  /**
   * @param neighborsFromAllProtocols
   * @param protocolID
   * @param node
   * @param selfDescr
   * @return
   */
  private Vector<Descriptor> keepCandidates(Vector<Descriptor> neighborsFromAllProtocols, int protocolID, Node node, Descriptor selfDescr)
  {
    Vector<Descriptor> candidates = new Vector<Descriptor>();
    Vector<Descriptor> rings = new Vector<Descriptor>();
    int ringlink = settings.numLinkables()-1;
    int linkableID = settings.getLinkable(ringlink);
    Linkable linkable = (Linkable) node.getProtocol(linkableID);
    // Add linked protocol's neighbors
    for (int j = 0; j<linkable.degree(); j++)
    {
      // We have to clone it, to change its hops without affecting Cyclon.
      Descriptor descr = (Descriptor) linkable.getNeighbor(j);
      rings.add(descr);
    }
    
    for (Descriptor d : neighborsFromAllProtocols)
      if (!rings.contains(d))
        candidates.add(d);
    
    return candidates;
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
        processGossipRequest(msg.sender, node, pid, msg.descriptors);
        break;
      case GOSSIP_RESPONSE:
        processResponse(msg.sender, node, pid, msg.descriptors);
    }    
  }

  /**
   * This is the passive thread. Executes steps 3-9
   * @param sender
   * @param node
   * @param pid
   * @param received
   */
  protected void processGossipRequest(Descriptor sender, Node node, int pid, Vector<Descriptor> received)
  {
    Descriptor selfDescr = createDescriptor();
    //((AddressSim)selfDescr.address).node = node;

    //***************** STEP 3:   Merge the VICINITY, CYCLON (and RINGS -- later) views in one Vp = Vvic U Vcyclon (U Vrings) 
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, selfDescr, pid);

    //***************** STEP 4: Add own descriptor with own profile and hops ) to the merged view: Vp= Vp U {P}
    ((DescriptorTopics)selfDescr).resetAge();
    neighborsFromAllProtocols.add(selfDescr);
    
    neighborsFromAllProtocols.remove(sender);

    //***************** STEP 5: Strip down Vp to its gvic best descriptors for P, by applying the selection function from P's perspective: Vq = S(gvic, P, Vq)
    Vector<Descriptor> neighborsToSend = selectToSend(vicSettings.gossipLen,sender,neighborsFromAllProtocols, received);
    
    neighborsFromAllProtocols.remove(selfDescr);//i should not be in my own view
    neighborsFromAllProtocols.add(sender);
    //should i increase the hops?????
    neighborsFromAllProtocols.addAll(received);
    eliminateDuplicates(neighborsFromAllProtocols);
    
    Vector<Descriptor> chosen = keepCandidates(neighborsFromAllProtocols, pid, node, selfDescr);
    view = selectToKeep(vicSettings.viewLen, selfDescr, chosen);

    controlMessages++;
    //***************** STEP 6: Send Vp to peer Q
    Message msg = new Message();
    msg.id = Long.parseLong(selfDescr.getID()+""+CommonState.getTime()+sender.getID());
    msg.type = Message.Type.GOSSIP_RESPONSE;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    //sentMessages.put(msg,CommonState.getTime());
    send(sender.address, pid, msg);
  }

  /**
   * This function represents
   * STEP 7: Similarly, receive Vq from peer Q, containing a set of (up to) gvic descriptors known by Q, optimally selected for P.
   * @param sender 
   * @param node
   * @param pid
   * @param received
   */
  protected void processResponse(Descriptor sender, Node node, int pid, Vector<Descriptor> received)
  {
    Vector<Descriptor> neighborsFromAllProtocols;
    Descriptor selfDescr = createDescriptor();
   // ((AddressSim)selfDescr.address).node = node;

    //***************** STEP 8: Merge the VICINITY, CYCLON (and RINGS -- later), and received views in one: V = Vvic U Vcyclon U Vrings U Vq
    neighborsFromAllProtocols = collectAllNeighbors(node, selfDescr, pid);
    neighborsFromAllProtocols.addAll(received);
    if (view.contains(sender)) neighborsFromAllProtocols.add(sender);
    eliminateDuplicates(neighborsFromAllProtocols);
   
    //***************** STEP 9: Rebuild the VICINITY view by selecting the best lvic neighbors from V: Vvic = S(lvic,P,V).
    Vector<Descriptor> chosen = keepCandidates(neighborsFromAllProtocols, pid, node, selfDescr);
    view = selectToKeep(vicSettings.viewLen, selfDescr, chosen);
  }
  
    
  /**
  * 
  * @param gossipLen
  * @param gossipChoice
  * @param neighbors
  * @param exclude
  * @return
  */
  private Vector<Descriptor> selectToSend(int gossipLen, Descriptor gossipChoice, Vector<Descriptor> neighbors, Vector<Descriptor> exclude)
  {
    //int index = ((AddressSim)gossipChoice.address).getNode().getIndex();
    return selectProximalNeighbors(gossipLen, gossipChoice, neighbors, exclude);
  }
  
  /**
   * 
   * @param viewLen
   * @param selfDescr
   * @param neighborsFromAllProtocols2
   * @return
   */
  private Vector<Descriptor> selectToKeep(int viewLen, Descriptor descr, Vector<Descriptor> neighbors)
  {
    //return selectProximalNeighbors(viewLen, descr, neighborsFromAllProtocols, null);
    
    Vector<Descriptor> toReturn = new Vector<Descriptor>();
    Vector<AbstractMap.SimpleEntry<Descriptor,Double>> map = new Vector<AbstractMap.SimpleEntry<Descriptor,Double>>();
    
    for (Descriptor d: neighbors)//neighbors does not have duplicates
    {
        if (!((DescriptorTopics)descr).equals(d))
        {
          //int common = ((DescriptorTopics)descr).getNbCommonTopics(d);
          double common = getDistanceMetric(descr,d);
          AbstractMap.SimpleEntry<Descriptor,Double> pair = new SimpleEntry<Descriptor, Double>(d, common);
          map.add(pair);
        }
    }
    
    Collections.sort(map, new Comparator<AbstractMap.SimpleEntry<Descriptor,Double>>() {
      public int compare(SimpleEntry<Descriptor, Double> a,
          SimpleEntry<Descriptor, Double> b) {
        return b.getValue().compareTo(a.getValue());
      }
    });
    
    int len = map.size()> viewLen ? viewLen : map.size();
    
    for (int i = 0; i < len; i++)
      toReturn.add(map.get(i).getKey());
  
    return toReturn;
  }
  
  private Vector<Descriptor> selectProximalNeighbors(int size, Descriptor descr, Vector<Descriptor> neighbors, Vector<Descriptor> exclude)
  {    
     boolean nodeExcluded = false;
    Vector<Descriptor> toReturn = new Vector<Descriptor>();
    Vector<AbstractMap.SimpleEntry<Descriptor,Double>> map = new Vector<AbstractMap.SimpleEntry<Descriptor,Double>>();
    
    for (Descriptor d: neighbors)//neighbors does not have duplicates
    {
        if (exclude != null)
          nodeExcluded = isExcluded(d, exclude);
        if (!nodeExcluded && !((DescriptorTopics)descr).equals(d))
        {
          //int common = ((DescriptorTopics)descr).getNbCommonTopics(d);
          double common = getDistanceMetric(descr,d);
          AbstractMap.SimpleEntry<Descriptor,Double> pair = new SimpleEntry<Descriptor, Double>(d, common);
          map.add(pair);
        }
    }
    
    Collections.sort(map, new Comparator<AbstractMap.SimpleEntry<Descriptor,Double>>() {
      @Override
      public int compare(SimpleEntry<Descriptor, Double> arg0, SimpleEntry<Descriptor, Double> arg1)
      {
        return arg0.getValue().compareTo(arg1.getValue());
      }
    });
    
    int len = map.size()> size ? size : map.size();
    
    for (int i = 0; i < len; i++)
    {
      toReturn.add(map.get(i).getKey());
      Vector<Topic> commonTop = ((DescriptorTopics)descr).getCommonTopics(toReturn.get(i));
      for (Topic t : commonTop)
      {
        int occurence = 1;
        if (topicOccurence.get(t) != null)
          occurence += topicOccurence.get(t);
        topicOccurence.put(t, occurence);
      }   
      
    }
  
    return toReturn;
  }
    
  /**
   * @param descr
   * @param d
   * @return
   */
  private double getDistanceMetric(Descriptor descr, Descriptor d)
  {
    Vector<Topic> commontop = ((DescriptorTopics)descr).getCommonTopics(d);
    int occ = 0;
    for (Topic t : commontop)
      if (topicOccurence.get(t) != null)
        occ += topicOccurence.get(t);
    
    //int size1 = ((DescriptorTopics)descr).getTopics().size();
    //int size2 = ((DescriptorTopics)d).getTopics().size();
    int common = ((DescriptorTopics)descr).getNbCommonTopics(d);
    //int total = size1+size2-common;
    
    if (occ == 0 && common != 0)
      return Double.MAX_VALUE; //maxim priority
    
    return (double)common/occ;
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
   
    // First collect my own neighbors (not cloned)
    for (Descriptor d: view)
      neighborsFromAllProtocols.add(d);    
    
    // Then collect neighbors from linked protocols
    for (int i=0; i<settings.numLinkables()-1; i++)
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
        
        if(!neighborsFromAllProtocols.contains(d))
        neighborsFromAllProtocols.add(d);
      }
    }
   // eliminateDuplicates(neighborsFromAllProtocols);
    return neighborsFromAllProtocols;
  }
  
  
  /**
   * 
   * @return
   */
  private Descriptor selectPeer()
  {
    DescriptorTopics maxAgeDescr = null;

    for (Descriptor d: view)
      if (maxAgeDescr==null || ((DescriptorTopics) d).getAge() > maxAgeDescr.getAge())
        maxAgeDescr = (DescriptorTopics) d;

    return maxAgeDescr;
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
  
  public static boolean isExcluded(Descriptor descr, Vector<Descriptor> descriptors)
  {
    for (int i = 0; i< descriptors.size(); i++)
    {
      if (((DescriptorTopics)descr).equals(descriptors.get(i)))
        return true;
    }
    return false;
  }

  @Override
  public boolean addNeighbor(Descriptor neighbour)
  {
    if (contains(neighbour))
      return false;

    if (view.size() >= vicSettings.viewLen)
      throw new IndexOutOfBoundsException();

    view.add(neighbour);
    return true;
  }
  
  @Override
  public Object clone()
  {
    Vicinity vic = (Vicinity)super.clone();
    vic.view = (Vector<Descriptor>)view.clone();
    vic.topicOccurence = (HashMap<Topic, Integer>)topicOccurence.clone();
    
    return vic;
  }
}