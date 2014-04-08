/**
 * 
 */
package tera.protocols;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.transport.Address;
import tera.datastructures.APT;
import tera.datastructures.Overlay;
import tera.descriptor.DescriptorTopics;
import tera.messages.FoundAccessPointMessage;
import tera.messages.JoinRequestMessage;
import tera.messages.JoinResponseMessage;
import tera.messages.Message;
import tera.messages.SearchAccessPointMessage;
import topics.Topic;

/**
 * @author anca
 *
 */
public class Tera extends Gossip
{
  TeraSettings teraSettings;
  HashMap<Topic, String> st;
  APT apt;
  HashMap<Topic, Vector<Descriptor>> topicView;

  /**
   * @param prefix
   */
  public Tera(String prefix)
  {
    super(prefix);
    teraSettings = (TeraSettings)settings;
    st = new HashMap<Topic, String>();
    apt = new APT(teraSettings.aptSize);
    topicView = new HashMap<Topic, Vector<Descriptor>>();
    view  = new Vector<Descriptor>();
  }

  /* (non-Javadoc)
   * @see peernet.core.Protocol#processEvent(peernet.transport.Address, peernet.core.Node, int, java.lang.Object)
   */
  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {
    Message msg = (Message) event;
    msg.sender.address = src; // Set the sender's address
    switch (msg.type)
    {
      case ACCESS_POINT_LOOKUP:
        accessPointLookup(msg.sender, node, pid,((SearchAccessPointMessage)msg).ttl, msg.topic);
        break;
      case ACCESS_POINT_FOUND:
        accessPointFound(msg.sender, node, pid, ((FoundAccessPointMessage)msg).accessPoint, msg.topic);
        break;
      case JOIN_REQUEST:
        joinRequest(msg.sender, node, pid, ((JoinRequestMessage)msg).requestor, msg.topic);
        break;
      case JOIN_RESPONSE:
        joinResponse(msg.sender, node, pid, ((JoinResponseMessage)msg).overlayId, msg.topic, msg.descriptors);
    }
  }


  /**
   * @param sender
   * @param node
   * @param pid
   * @param ttl
   * @param topic
   */
  private void accessPointLookup(Descriptor sender, Node node, int pid, int ttl, Topic topic)
  {
    //System.out.println("i am in access point lookup method");
    Descriptor selfDescriptor = createDescriptor();
    //This node is a subscriber for the requested topic, thus it can send back a reference to itself
    if (st.containsKey(topic))
    {
      System.out.println(" i am subscribed to the requested topic");
      FoundAccessPointMessage msg = new FoundAccessPointMessage();
      msg.type = Message.Type.ACCESS_POINT_FOUND;
      msg.sender = selfDescriptor;
      msg.accessPoint = selfDescriptor;
      msg.topic = topic;
      //send(sender.address, pid, msg);
    } else {
      if (apt.containsTopic(topic))//We have an access point for the requested topic. Send back a response to the original node
      {
        System.out.println("i am not subscribed but i have an access point");
        FoundAccessPointMessage msg = new FoundAccessPointMessage();
        msg.type = Message.Type.ACCESS_POINT_FOUND;
        msg.sender = selfDescriptor;
        msg.accessPoint = apt.getAccessPoint(topic);
        msg.topic = topic;
        //send(sender.address, pid, msg);
      }
      else {
        System.out.println(" no access point " + ttl);
        ttl--;
        if (ttl == 0 || view.isEmpty())
        {
          //End of life for the message. Return no node found
          FoundAccessPointMessage msg = new FoundAccessPointMessage();
          msg.type = Message.Type.ACCESS_POINT_FOUND;
          msg.sender = selfDescriptor;
          msg.accessPoint = null;
          msg.topic = topic;
          send(sender.address, pid, msg);
        } 
        else {
          int index = CommonState.r.nextInt(view.size());
          Descriptor gossip = view.elementAt(index);
          SearchAccessPointMessage msg = new SearchAccessPointMessage();
          msg.type = Message.Type.ACCESS_POINT_LOOKUP;
          msg.sender = sender;
          msg.topic = topic;
          msg.ttl = ttl;
          send(gossip.address, pid, msg);
        }
      }
    }

  }
  
  /**
   * @param topic 
   * @param pid 
   * @param node 
   * @param sender 
   * @param accessPoint 
   * 
   */
  private void accessPointFound(Descriptor sender, Node node, int pid, Descriptor accessPoint, Topic topic)
  {
    //if (st.containsKey(topic)) return;
    Descriptor selfDescr = createDescriptor();
    if (accessPoint == null) { //instantiate a new overlay
      String oid = node.getID() + "-" + topic.getId() + "-" + new Long(CommonState.getTime()).toString();
      ((DescriptorTopics)selfDescr).overlayId = oid;
      st.put(topic, ((DescriptorTopics)selfDescr).overlayId);
      System.out.println("     "+node.getID() + ": " + topic.getId() + " " + st.get(topic));
      Overlay.instance().inc(((DescriptorTopics)selfDescr).overlayId);      
    }
    else {
      //send a join request message to access point
      //System.out.println(selfDescr.getID() + " sends a join request msg for topic "+ topic.getId() + " to " + accessPoint.getID());
      JoinRequestMessage msg = new JoinRequestMessage();
      msg.type = Message.Type.JOIN_REQUEST;
      msg.topic = topic;
      msg.requestor = selfDescr;
      msg.sender = selfDescr;
      send(accessPoint.address, pid, msg);
    }
  }
  
  /**
   * @param sender
   * @param node
   * @param pid
   * @param requestor
   * @param topic
   */
  private void joinRequest(Descriptor sender, Node node, int pid, Descriptor requestor, Topic topic)
  {
    Descriptor selfDescr = createDescriptor();
    //System.out.println(selfDescr.getID() + " got the request from " + sender.getID() + " for topic " + topic.getId());
    Vector<Topic> myTopics = ((DescriptorTopics)selfDescr).getTopics();
    if (myTopics.contains(topic) && st.containsKey(topic))
    {
      //First we send a copy of our view to the requesting node. Note that we send the view pertaining to the topic overlay.
      JoinResponseMessage msg = new JoinResponseMessage();
      msg.type = Message.Type.JOIN_RESPONSE;
      msg.sender = selfDescr;
      msg.topic = topic;
      msg.descriptors = topicView.get(topic);
      msg.overlayId = st.get(topic); //not sure if it's correct
      
      if (topicView.get(topic) == null)
      {
        Vector<Descriptor> neighbors = new Vector<Descriptor>();
        neighbors.add(requestor);
        topicView.put(topic, neighbors);
      }
      else
      if (!topicView.get(topic).contains(requestor))
      {
        if (topicView.get(topic).size() == teraSettings.viewSize)
        {
          int index = CommonState.r.nextInt(topicView.get(topic).size());
          topicView.get(topic).remove(index);
        }
        topicView.get(topic).add(requestor);
      }
    }
    else {
      //This node is no more subscribed to the topic. Send a negative reply.
      //System.out.println("you should never be here");
    }
    
  }
  
  private void joinResponse(Descriptor sender, Node node, int pid, String overlayId, Topic topic, Vector<Descriptor> descriptors) {
    Descriptor selfDescr = createDescriptor();
    //if (st.containsKey(topic)) return;
    //if (view_x.containsKey(m.topic)) Overlay.instance().dec(view_x.get(m.topic).overlayID);
    if (descriptors != null && overlayId.isEmpty())
    {
      ((DescriptorTopics)selfDescr).overlayId = overlayId;
      if (descriptors.size() == teraSettings.viewSize)
      {
        int index = CommonState.r.nextInt(descriptors.size());
        descriptors.remove(index);
      }
      descriptors.add(sender);
      insertToView(descriptors);
    }
    else {
    //The access point we found is no more subscribed to that topic.
      //Instantiate a new overlay
      ((DescriptorTopics)selfDescr).overlayId = node.getID() + "-" + topic.getId() + "-" + new Long(CommonState.getTime()).toString(); 
      //System.out.println(selfDescr + " " + ((DescriptorTopics)selfDescr).overlayId);
    }
    //System.out.println("i am here");
    st.put(topic, ((DescriptorTopics)selfDescr).overlayId);
    Overlay.instance().inc(((DescriptorTopics)selfDescr).overlayId);
  }

  /* (non-Javadoc)
   * @see peernet.core.Protocol#nextCycle(peernet.core.Node, int)
   */
  @Override
  public void nextCycle(Node node, int protocolID)
  {
    Descriptor selfDescr = createDescriptor();

    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, selfDescr, protocolID);
    insertToView(neighborsFromAllProtocols);
    Vector<Topic> myTopics = ((DescriptorTopics)selfDescr).getTopics();
   
    //overlayId initialization
    for (Topic t : myTopics)
      if (!st.containsKey(t)) 
        lookup(t, selfDescr, protocolID);
//    for (Topic t : myTopics)
//      System.out.println(selfDescr.getID() + " " + t.getId() + " " + st.get(t));
//    
    //advertiseSubscriptions();
  }

  /**
   * 
   */
  private void advertiseSubscriptions()
  {
    int i = Math.min(teraSettings.nSubUpdate, view.size()-1);
    Vector<Descriptor> toSend = new Vector<Descriptor>();
    Collections.shuffle(view);
    while (i > 0) {
      toSend.add(view.elementAt(i));
      i--;
    }
    
    
  }

  /**
   * @param neighborsFromAllProtocols
   */
  private void insertToView(Vector<Descriptor> neighborsFromAllProtocols)
  {
    if (neighborsFromAllProtocols.isEmpty()) return;
    view.clear();
    view.addAll(neighborsFromAllProtocols);    
  }

  /**
   * @param topic
   * @param selfDescr 
   * @param protocolID 
   */
  private void lookup(Topic topic, Descriptor selfDescr, int protocolID)
  {
    if (topic == null) return;
    if (view.isEmpty()) return;
    if (apt.containsTopic(topic))
    {
      System.out.println("I have to send a cyclon request message");
    } 
    else 
    {
      int toSendSize = Math.min(teraSettings.searchNum, view.size());
      Collections.shuffle(view);
      for (int i = 0; i< toSendSize; i++)
      {
        Descriptor gossip = view.elementAt(i);
        //System.out.println(selfDescr.getID() + " sends an APL message for topic " + topic.getId() + " to " + gossip.getID());
        SearchAccessPointMessage msg = new SearchAccessPointMessage();
        msg.type = Message.Type.ACCESS_POINT_LOOKUP;
        msg.sender = selfDescr;
        msg.topic = topic;
        msg.ttl = teraSettings.ttl;
        send(gossip.address, protocolID, msg);
      }
    }
  }

  /**
   * @param neighborsFromAllProtocols
   * @param gossipLen
   * @return
   */
  private Vector<Descriptor> selectToSend(Vector<Descriptor> neighborsFromAllProtocols, int gossipLen)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param node
   * @param selfDescr
   * @param protocolID
   * @return
   */
  private Vector<Descriptor> collectAllNeighbors(Node selfNode, Descriptor selfDescr, int protocolID)
  {
    // If no protocols are linked, return the view, as is.
    if (!settings.hasLinkable())
      return view;

    Vector<Descriptor> neighborsFromAllProtocols = new Vector<Descriptor>();

    // First collect my own neighbors (not cloned)
    for (Descriptor d: view)
      neighborsFromAllProtocols.add(d);

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
    Tera tera = (Tera)super.clone();
    tera.st = (HashMap<Topic,String>)st.clone();
    tera.apt = (APT)apt.clone();
    tera.view = (Vector<Descriptor>)view.clone();
    tera.topicView = (HashMap<Topic, Vector<Descriptor>>)topicView.clone();
    return tera;
  }

}
