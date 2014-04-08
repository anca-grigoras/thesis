/*
 * Created on Jun 18, 2010 by Spyros Voulgaris
 *
 */
package gossip.protocol;

import gossip.descriptor.DescriptorAge;

import java.util.Vector;

import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.transport.Address;



public class Vicinity extends Gossip
{
  VicinitySettings vicSettings;
  /**
   * Default constructor.
   * Called only once for a new protocol class instance.
   */
  public Vicinity(String prefix)
  {
    super(prefix);
    vicSettings = (VicinitySettings)settings;

    view = new Vector<Descriptor>(vicSettings.viewLen);
  }





  /**
   * Initiates gossip request to a peer. Sends the initial message (request).
   */
  public void nextCycle(Node selfNode, int pid)
  {
    // Acquire fresh own descriptor
    // Need to call getOwnDescriptor, as it may produce gradually changing profiles
    Descriptor selfDescr = createDescriptor();

    // Increase the hops field of every neighbor
    assert (selfDescr instanceof DescriptorAge): "Vicinity needs DescriptorAge descriptors, or their descendants.";
    for (Descriptor d: view)
      ((DescriptorAge)d).incAge();

    // Collect the neighbors from all protocols linked to Vicinity
    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(selfNode, selfDescr, pid);
    neighborsFromAllProtocols.add(selfDescr);
    eliminateDuplicates(neighborsFromAllProtocols, vicSettings.duplCmp);

    // Update own view
    if (settings.hasLinkable())
      view = vicSettings.selectProximal(selfDescr, neighborsFromAllProtocols, null, vicSettings.viewLen);

    /*
     * Select a peer to gossip with, and remove it from the view.
     * - If it responds, its fresh descriptor will be added.
     * - If it doesn't, it's explicitly good to forget him.
     */
    Descriptor peerDescr = vicSettings.selectToGossip(view, true);
    if (peerDescr==null)
      return;

    // Select 'gossipLen' neighbors to send to other peer.
    // Add fresh own descriptor to the collection from all protocol layers.
    Vector<Descriptor> candidatesToSend =
      vicSettings.sendLinkableItems ? neighborsFromAllProtocols : view;
    
    Vector<Descriptor> neighborsToSend = vicSettings.selectProximal(
        peerDescr, candidatesToSend, null, vicSettings.gossipLen);
//    for (Descriptor d: neighborsToSend)
//      ((DescriptorAge)d).resetAge();

    // Send the selected neighbors to this peer.
    Message msg = new Message();
    msg.type = Message.Type.GOSSIP_REQUEST;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    send(peerDescr.address, pid, msg);
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
  protected Vector<Descriptor> collectAllNeighbors(Node selfNode, Descriptor selfDescr, int pid)
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
        Descriptor d = null;
        d = (Descriptor) linkable.getNeighbor(j).clone();
        //XXX ((DescriptorAge) d).resetAge(); // Since Vicinity uses hops in a different context, reset it.
        neighborsFromAllProtocols.add(d);
      }
    }

    return neighborsFromAllProtocols;
  }





  @Override
  public void processEvent(Address from, Node node, int pid, Object event)
  {
    Message msg = (Message) event;
    msg.sender.address = from; // Set the sender's address
    switch (msg.type)
    {
      case GOSSIP_REQUEST:
        processGossipRequest(msg.sender, node, pid, msg.descriptors);
        break;
      case GOSSIP_RESPONSE:
        processResponse(node, pid, msg.descriptors);
    }
  }





  protected void processGossipRequest(Descriptor sender, Node selfNode, int pid, Vector<Descriptor> received)
  {
    Descriptor selfDescr = createDescriptor();

    Vector<Descriptor> neighborsFromAllProtocols = collectAllNeighbors(selfNode, selfDescr, pid);
    neighborsFromAllProtocols.add(selfDescr);
    eliminateDuplicates(neighborsFromAllProtocols, vicSettings.duplCmp);

    // Select 'gossipLen' neighbors to send back.
    // Exclude descriptors that I just received from that peer.
    Vector<Descriptor> neighborsToSendBack = vicSettings.selectProximal(
        sender /*this is now the target*/,
        neighborsFromAllProtocols,
        received,
        vicSettings.gossipLen);

    for (Descriptor d: neighborsToSendBack)
      ((DescriptorAge)d).incAge();

    // Update own view, to select best neighbors for myself.
    insertReceived(selfDescr, received);

    // Send the selected neighbors to this peer.
    Message msg = new Message();
    msg.type = Message.Type.GOSSIP_RESPONSE;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSendBack;
    send(sender.address, pid, msg);
  }
  
  protected void processResponse(Node node, int pid, Vector<Descriptor> received)
  {
    // Update own view, to select best neighbors for myself.
    Descriptor selfDescr = createDescriptor();
    insertReceived(selfDescr, received);
  }

  public boolean addNeighbor(Descriptor neighbor)
  {
    if (contains(neighbor))
      return false;

    if (view.size() >= vicSettings.viewLen)
      throw new IndexOutOfBoundsException();

    view.add(neighbor);
    return true;
  }

  protected void insertReceived(Descriptor selfDescr, Vector<Descriptor> received)
  {
    view.addAll(received);

    eliminateDuplicates(view, vicSettings.duplCmp);

    view = vicSettings.selectProximal(
        selfDescr,
        view, // original view + received
        null,
        vicSettings.viewLen);
  }





//  /**
//   * Inserts the descriptors received by the peer in my own view.
//   * 
//   * @param received
//   */
//  protected void insertReceived(Descriptor selfDescr, Vector<Descriptor> received)
//  {
//    // Select 'gossipLen' neighbors to send back.
//    // Exclude descriptors that I just received from that peer.
//    Vector<Descriptor> neighborsToSendBack = vicSettings.selectProximal(
//        sender /*this is now the target*/,
//        neighborsFromAllProtocols,
//        received,
//        vicSettings.gossipLen);
//
//
////    // Insert received descriptors to own view.
////    if (received != null)
////      for (Descriptor d: received)
////      {
////        if (view.size() >= vicSettings.viewLen)
////          break;
////        insert(d);
////      }
//
//    assert view.size() <= vicSettings.viewLen;
//  }
//
//
//
//
//
//  protected void insert(Descriptor d)
//  {
//    int foundAt = view.indexOf(d);
//
//    if (foundAt==-1) // 'd' is not in my view ==> put it!
//      view.add(d);
//    else // we have a duplicate ==> Keep the preferred one
//    {
//      Descriptor existingItem = view.elementAt(foundAt);
//      if (vicSettings.duplCmp.compare(d, existingItem) < 0)
//        view.setElementAt(d, foundAt);
//    }
//  } 
}
