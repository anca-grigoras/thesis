/*
 * Created on Jul 1, 2007 by Spyros Voulgaris
 *
 */
package vitis.protocols;

import vitis.descriptor.DescriptorProfile;
import vitis.messages.CyclonMessage;
import vitis.messages.Message;

import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Node;
import peernet.transport.Address;

public class Cyclon extends Gossip
{
  /**
   * This variable holds all protocol-instance related variable,
   * that have a fixed value for all view participating in an
   * instance of a protocol, such as the view length or gossip length.
   */
  public CyclonSettings cyclonSettings;

  /*
   * When a node initiates a gossip request, it sends some part of its view to
   * the other peer. This part of the view is moved from 'view' to 'shippedView'
   * and when a response is received, it is merged back to 'view', if there are
   * available slots (e.g., in case of duplicates, or an initially non-full
   * view).
   * 
   * So, this container is to be filled in nextCycle(), and "emptied" in
   * processResponse().
   */
  Vector<Descriptor> shippedDescriptors= null;
  private int controlMsg = 0;
  
  public int getControlMsg()
  {
    return controlMsg;
  }
  
  public void resetControlMsg()
  {
    controlMsg = 0;
  }

  public Cyclon(String prefix)
  {
    super(prefix);
    cyclonSettings = (CyclonSettings)settings;
    view = new Vector<Descriptor>(cyclonSettings.viewLen);
    controlMsg = 0;
    //System.out.println("The protocol is " + view.capacity());
  }

  public String toString()
  {
    return name;
  }
  /**
   * Initiates gossip to a peer. Sends the initial message.
   */
  @SuppressWarnings("unchecked")
  public void nextCycle(Node node, int pid)
  {
    //System.out.println("Node "+node.getID()+" degree "+degree());
    Descriptor selfDescr = createDescriptor();
   //if (node.getID() == 0)
    //  System.out.println(""+node.getID()+" "+view);

    // If there is free space in my view, fill it in with items
    // that I shipped last time. This will be the case when I shipped
    // some descriptors to a neighbor, but received no response,
    // therefore I assume the node I contacted is dead, and I reinsert
    // to my view the descriptors I had shipped to him.
    insertToView(shippedDescriptors, selfDescr);


    // Increase the hops of each item in my view by 1.
    for (Descriptor d: view)
      ((DescriptorProfile)d).incAge();

    // If a node's view is empty, don't proceed
    if ( (view.size() == 0) ||
         (cyclonSettings.gossipLen == 0) )
      return;

    // First, select a peer to gossip with, and remove him from my view.
    Descriptor peerDescr = selectToGossip();
    view.remove(peerDescr);

    // Select 'gossipLen'-1 neighbors to send to the other peer.
    // Of course, exclude the peer itself.
    Vector<Descriptor> descriptorsToSend = selectToSend(peerDescr, cyclonSettings.gossipLen-1);

    // Store the list of sent descriptors to fill in empty slots when I receive
    // a response, or to 
    shippedDescriptors = (Vector<Descriptor>) descriptorsToSend.clone();

    // Add my own descriptor to the list to send to the peer.
    descriptorsToSend.add(selfDescr);

    controlMsg++;
    // Send the selected neighbors to this peer.
    CyclonMessage msg = new CyclonMessage();
    msg.type = CyclonMessage.Type.GOSSIP_REQUEST;
    msg.sender = selfDescr;
    msg.descriptors = descriptorsToSend;
//    System.err.println("Sending to "+peerDescr.address);
    send(peerDescr.address, pid, msg);
  }

  private Descriptor selectToGossip()
  {
    DescriptorProfile maxAgeDescr = null;

    for (Descriptor d: view)
      if (maxAgeDescr==null || ((DescriptorProfile) d).getAge() > maxAgeDescr.getAge())
        maxAgeDescr = (DescriptorProfile) d;

    return maxAgeDescr;
  }



  public void processEvent(Address srcAddr, Node node, int pid, Object event)
  {
    controlMsg++;
//    System.err.println("Received from "+srcAddr);
    CyclonMessage msg = (CyclonMessage) event;
    msg.sender.address = srcAddr; // Set the sender's address
    switch (msg.type)
    {
      case GOSSIP_REQUEST:
        processGossipRequest(msg.sender, node, pid, msg.descriptors);
        break;
      case GOSSIP_RESPONSE:
        processResponse(node, pid, msg.descriptors);
    }
  }




  protected void processGossipRequest(Descriptor sender, Node node, int pid, Vector<Descriptor> received)
  {
    // Select 'gossipLen' neighbors to send back.
    // Of course, exclude that peer.
    Vector<Descriptor> descriptorsToSend = selectToSend(sender, cyclonSettings.gossipLen);

    // Insert the received descriptors to own view
    insertToView(received, null);
    insertToView(descriptorsToSend, sender);

    Descriptor selfDescr = createDescriptor();

    // Send the selected neighbors to this peer.
    CyclonMessage msg = new CyclonMessage();
    msg.type = CyclonMessage.Type.GOSSIP_RESPONSE;
    msg.sender = selfDescr;
    msg.descriptors = descriptorsToSend;
    send(sender.address, pid, msg);
  }





  protected void processResponse(Node node, int pid, Vector<Descriptor> received)
  {
    Descriptor myDescr = createDescriptor();
    insertToView(received, null);
    insertToView(shippedDescriptors, myDescr);
    shippedDescriptors = null;
  }



  /**
   * Selects (and removes!) k descriptors from the view, to be sent to
   * another peer.
   * 
   * @param dest
   * @param k
   * @return
   */
  protected Vector<Descriptor> selectToSend(Descriptor dest, int k)
  {
    // if asking for 0 items, or my view has 0 items, return ...0 items!
    if (k==0 || view.isEmpty())
      return new Vector<Descriptor>(0); // empty vector

    Vector<Descriptor> selected = new Vector<Descriptor>(cyclonSettings.gossipLen);
    Vector<Descriptor> inappropriate = new Vector<Descriptor>(0);

    int viewSize = view.size();
    while (k > 0)
    {
      // grab a random element
      int r = CommonState.r.nextInt(viewSize);
      Descriptor d = view.elementAt(r);

      // move the last element to view[r], and decrease the view size
      view.set(r, view.elementAt(--viewSize));

      // store the chosen one in 'selected' (except if it's not appropriate)
      if (!((DescriptorProfile)d).equals((DescriptorProfile)dest))
      {
        selected.add(d);
        k--;  // one off
      }
      else
        inappropriate.add(d);

      if (viewSize == 0)
        break;
    }

    // Decrease the actual view size to the estimated one
    view.setSize(viewSize);
    
    // and move back to the view all elements 'inappropriately' removed
    view.addAll(inappropriate);


    return selected;
  }



  /**
   * Inserts the descriptors received by the peer in my own view.
   * 
   * @param received
   * @param descriptors
   * @param exclude
   */
  protected void insertToView(Vector<Descriptor> descriptors, Descriptor exclude)
  {
    // Insert descriptors to own view.
    if (descriptors != null)
      for (Descriptor d: descriptors)
      {
        if (exclude!=null && ((DescriptorProfile)d).equals((DescriptorProfile)exclude))
          continue; // Bypass it if it's my own descriptor
        if (view.size() == cyclonSettings.viewLen)
          break;
        insert(d);
      }
  }



  protected void insert(Descriptor d)
  {
    int foundAt = view.indexOf(d);

    if (foundAt==-1) // 'd' is not in my view ==> put it!
      view.add(d);
    else // we have a duplicate ==> Keep the preferred one
    {
      Descriptor existingItem = view.elementAt(foundAt);

      if (resolveDuplicate(d,existingItem) < 0)
        view.setElementAt(d, foundAt);
    }
  } 
  
  public static int resolveDuplicate(Descriptor a, Descriptor b)
  {  
    return ((DescriptorProfile)a).getAge()-((DescriptorProfile)b).getAge();
  }
  
  @Override
  public int degree()
  {
    return view.size() + (shippedDescriptors==null ? 0 : shippedDescriptors.size());
  }



  @Override
  public boolean addNeighbor(Descriptor neighbor)
  {
    if (contains(neighbor))
      return false;

    if (view.size() >= cyclonSettings.viewLen)
      throw new IndexOutOfBoundsException();

    view.add(neighbor);
    return true;
  }



  @Override
  public Descriptor getNeighbor(int i)
  {
    int vs = view.size();
    return i<vs ? view.elementAt(i) : shippedDescriptors.elementAt(i-vs);
  }



  @Override
  public boolean contains(Descriptor d)
  {
    return view.contains(d) ||
           (shippedDescriptors!=null && shippedDescriptors.contains(d));
  }
}
