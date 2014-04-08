/**
 * 
 */
package vitis.protocols;

import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.transport.Address;
import vitis.descriptor.DescriptorProfile;
import vitis.messages.TManMessage;
import vitis.types.Buffer;

/**
 * @author anca
 *
 */
public class TMan extends Gossip
{
  public TManSettings tmanSettings;
  private int controlMsg;
  
  public int getControlMsg()
  {
    return controlMsg;
  }
  public void resetControlMsg()
  {
    controlMsg = 0;
  }
  /**
   * @param prefix
   */
  public TMan(String prefix)
  {
    super(prefix);
    tmanSettings = (TManSettings)settings;
    view = new Vector<Descriptor>(tmanSettings.viewSize);
    controlMsg = 0;
  }



  /* (non-Javadoc)
   * @see peernet.core.Linkable#addNeighbor(peernet.core.Descriptor)
   */
  @Override
  public boolean addNeighbor(Descriptor neighbour)
  {
    if (contains(neighbour))
      return false;

    if (view.size() >= tmanSettings.viewSize)
      throw new IndexOutOfBoundsException();

    view.add(neighbour);
    return true;
  }



  /* (non-Javadoc)
   * @see peernet.core.Protocol#processEvent(peernet.transport.Address, peernet.core.Node, int, java.lang.Object)
   */
  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {
    TManMessage msg = (TManMessage) event;
    controlMsg++;
    msg.sender.address = src; // Set the sender's address
    switch (msg.type)
    {
      case RT_REQUEST:
        processRTRequest(msg.sender, node, pid, msg.buffer);
        break;
      case RT_RESPONSE:
        processRTResponse(node, pid, msg.buffer);
    }
  }

  /**
   * 
   * @param sender
   * @param node
   * @param pid
   * @param received
   */
  private void processRTRequest(Descriptor sender, Node node, int pid, Buffer received)
  {
    Descriptor selfDescr = createDescriptor();    
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, selfDescr, pid);
    
    Buffer toSendBuffer;    
    if (view.isEmpty()) {
      initView(neighborsFromAllProtocols);
      toSendBuffer = new Buffer(view, selfDescr, tmanSettings.maxN);
    }
    else {
      toSendBuffer = new Buffer(view, selfDescr, tmanSettings.maxN);
      toSendBuffer.merge(neighborsFromAllProtocols);
    }
    
    Buffer buffer = new Buffer(toSendBuffer.getPeers(), selfDescr, tmanSettings.maxN);
    buffer.merge(received.getPeers());
    
    while(buffer.getPeers().contains(selfDescr))
      buffer.getPeers().remove(selfDescr);
    
    Vector<Descriptor> selectedNeighbors = buffer.selectNeighbors(selfDescr, tmanSettings.viewSize, tmanSettings.numOfSmallWorldLinks, tmanSettings.fingerDistanceSeed);
    refreshView(selectedNeighbors);
    
    // replace that selected peer's entry with your own
    toSendBuffer.getPeers().remove(sender);
    toSendBuffer.getPeers().add(selfDescr);
    
    
    TManMessage msg = new TManMessage();
    msg.type = TManMessage.Type.RT_RESPONSE;
    msg.sender = selfDescr;
    msg.buffer = toSendBuffer;
    send(sender.address, pid, msg);
    
  }

  
/**
 * 
 * @param node
 * @param pid
 * @param received
 */
  private void processRTResponse(Node node, int pid, Buffer received)
  {
    
    Descriptor selfDescr = createDescriptor();
    
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, selfDescr, pid);
    Buffer buffer = new Buffer(view, selfDescr, tmanSettings.maxN);
    buffer.merge(neighborsFromAllProtocols);
    buffer.merge(received.getPeers());
    
    while (buffer.getPeers().contains(selfDescr))
      buffer.getPeers().remove(selfDescr);
    
    Vector<Descriptor> selectedNeighbors = buffer.selectNeighbors(selfDescr, tmanSettings.viewSize, tmanSettings.numOfSmallWorldLinks, tmanSettings.fingerDistanceSeed);
    refreshView(selectedNeighbors);
  }

  /* (non-Javadoc)
   * @see peernet.core.Protocol#nextCycle(peernet.core.Node, int)
   */
  @Override
  public void nextCycle(Node node, int protocolID)
  {
    Descriptor selfDescr = createDescriptor();
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(node, selfDescr, protocolID);
    
    if (view.isEmpty())
    {
      initView(neighborsFromAllProtocols);
      //return;
    }
    
    Descriptor selectToGossip = selectRandomPeer();
    
    //System.out.println("I am node " +node.getID() + " and i chose " +selectToGossip.ID);
    
    Buffer buffer = new Buffer(view, selfDescr, tmanSettings.maxN);
    buffer.merge(neighborsFromAllProtocols);
    
    // replace that selected peer's entry with your own
    /*buffer.getPeers().remove(selectToGossip);
    buffer.getPeers().add(selfDescr);*/
    
    Vector<Descriptor> selectedNeighbors = buffer.selectNeighbors(selfDescr, tmanSettings.viewSize, tmanSettings.numOfSmallWorldLinks, tmanSettings.fingerDistanceSeed);
    refreshView(selectedNeighbors);
    
    //print("I am node " + node.getID(),selectedNeighbors);
    
    //send the neighbors to gossipChoice 
    controlMsg++;
    TManMessage msg = new TManMessage();
    msg.type = TManMessage.Type.RT_REQUEST;
    msg.sender = selfDescr;
    msg.buffer = buffer;
    send(selectToGossip.address, protocolID, msg);
  
  } 

  /**
   * 
   * @return
   */
  private Descriptor selectRandomPeer()
  {
    int index = CommonState.r.nextInt(view.size());
    Descriptor randomPeer = view.get(index);
    return randomPeer;
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
    if (!tmanSettings.hasLinkable())
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
        if(!neighborsFromAllProtocols.contains(descr))
        {
          Descriptor d = null;
          d = (Descriptor) descr.clone();

          ((DescriptorProfile) d).resetAge(); // Since Vicinity uses hops in a different context, reset it.
          neighborsFromAllProtocols.add(d);
        }
      }
    }    
    return neighborsFromAllProtocols;
  }

  /**
   * @param neighborsFromAllProtocols
   */
  private void initView(Vector<Descriptor> neighborsFromAllProtocols)
  {
    int howMany = tmanSettings.viewSize;
    for (Descriptor d : neighborsFromAllProtocols)
      if(view.size() < howMany) //why <=??
        view.add(d);
      else
        break;    
  }

  private void refreshView(Vector<Descriptor> selectedNeighbors)
  {
    view.clear();
    for (Descriptor d : selectedNeighbors) {
      if (d != null && !view.contains(d))
        view.add(d);
    }
  }
  
  private void print(String s, Vector<Descriptor> peers) {
    System.out.print(s + " : ");
    for (Descriptor d : peers) 
      System.out.print(d.getID() + " ");
    System.out.println();
  }
  
//------------------------------------------------------------------------    
  public synchronized Descriptor getSucc() {
    Descriptor succ;
    Descriptor self = createDescriptor();
    
    if (view.size() > 1)
      succ = view.elementAt(1);
    else if (view.size() > 0)
      succ = self;
    else
      succ = self;
    
    return succ;  
  }

//------------------------------------------------------------------------    
  public synchronized Descriptor getPred() {
    Descriptor pred;     
    Descriptor self = createDescriptor();
    
    if (view.size() > 1)
      pred = view.elementAt(0);
    else if (view.size() > 0)
      pred = view.elementAt(0);
    else
      pred = self;      
    
    return pred;  
  }

//------------------------------------------------------------------------    
  public synchronized Vector<Descriptor> getCluster() {
    Vector<Descriptor> cluster = new Vector<Descriptor>();
    
    if (view.size() > 3) {
      for (Descriptor profile : view.subList(3, view.size()))
        cluster.addElement(profile);
    }
    
    return cluster; 
  }
  
  public int getNumOfSmallWorldLinks() {
    Descriptor self = createDescriptor();
    return ((DescriptorProfile)self).getNumOfSmallWorldLinks();
  }
}

