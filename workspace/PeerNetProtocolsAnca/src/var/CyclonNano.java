/*
 * Created on Jul 1, 2007 by Spyros Voulgaris
 *
 */
package var;

import gossip.comparator.Random;
import gossip.descriptor.DescriptorAge;

import java.util.Collections;
import java.util.Vector;


import peeremu.cdsim.CDProtocol;
import peeremu.config.FastConfig;
import peeremu.core.CommonState;
import peeremu.core.Descriptor;
import peeremu.core.Node;
import peeremu.edsim.EDProtocol;
import peeremu.transport.Transport;

public class CyclonNano extends Gossip implements EDProtocol, CDProtocol
{
  public CyclonNano(String name)
  {
    super(name);
  }

  protected Vector<Descriptor> neighborsToSend = null;


  /**
   * Initiates gossip to a peer. Sends the initial message.
   */
  public void nextCycle(Node node, int pid)
  {
    if ( (view.size() == 0) ||
         (gossipSettings.gossipLen == 0) )
      return;

    Descriptor selfDescr = node.getDescriptor(pid);

    // Sort based on the selectComparator...
    Collections.shuffle(view, CommonState.r);
    Collections.sort(view,
        Collections.reverseOrder(gossipSettings.selectComparator));

    // ...and select from the end.
    int peerIndex = view.size()-1;
    Descriptor peerDescr = view.remove(peerIndex);
    
    // Select 'gossipLen'-1 neighbors to send to other peer.
    // Of course, exclude the peer itself.
    if (neighborsToSend!=null)
      insertReceivedItems(neighborsToSend, null, selfDescr);
    neighborsToSend = selectItemsToSend(peerDescr, null, gossipSettings.gossipLen-1);

    // Add my own item to the list to send to the peer.
    neighborsToSend.add(selfDescr);

    // Send the selected neighbors to this peer.
    int tid = FastConfig.getTransport(pid);
    Transport tr = (Transport) node.getProtocol(tid);
    Message msg = new Message();
    msg.type = Type.GOSSIP_REQUEST;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    tr.send(selfDescr, peerDescr, pid, msg);
  }


  static int counter = 0;
  public void processEvent(Node node, int pid, Object event)
  {
    Message msg = (Message) event;
//    System.out.println(counter+")\t"+msg.sender.getID()+"\t"+node.getID()+"\t"+msg.type+"\t("+CommonState.getIntTime()+")");
//    counter++;

    System.out.print("("+CommonState.getTime()+") - "+(msg.type==Type.GOSSIP_REQUEST ? "REQUEST  " : "RESPONSE "));
    System.out.print((char)(msg.sender.getID()+'A')+"->"+node+"  : ");
    for (Descriptor d: msg.descriptors)
      System.out.print(" "+d);
    System.out.println();

    switch (msg.type)
    {
      case GOSSIP_REQUEST:
        processGossipRequest(msg.sender, node, pid, msg.descriptors);
        break;
      case GOSSIP_RESPONSE:
        processResponse(node, pid, msg.descriptors);
        break;
    }
  }




  protected void processGossipRequest(Descriptor sender, Node node, int pid, Vector<Descriptor> received)
  {
//    Descriptor myDescr = node.getDescriptor(pid);
//    if (neighborsToSend!=null)
//      insertReceivedItems(null, neighborsToSend, myDescr);

    // Select 'gossipLen' neighbors to send back.
    // Of course, exclude that peer.
    Vector<Descriptor> neighborsToSend = selectItemsToSend(sender, received, gossipSettings.gossipLen);

    // Insert the received view to my cache
    insertReceivedItems(received, neighborsToSend, sender);

    // If using ages, increase the age of each item in my cache by 1.
    if (view.elementAt(0) instanceof DescriptorAge)
      for (Descriptor d: view)
        ((DescriptorAge)d).incAge();

    Descriptor selfDescr = node.getDescriptor(pid);

    // Send the selected neighbors to this peer.
    int tid = FastConfig.getTransport(pid);
    Transport tr = (Transport) node.getProtocol(tid);
    Message msg = new Message();
    msg.type = Type.GOSSIP_RESPONSE;
    msg.sender = selfDescr;
    msg.descriptors = neighborsToSend;
    tr.send(selfDescr, sender, pid, msg);
  }





  protected void processResponse(Node node, int pid, Vector<Descriptor> received)
  {
    Descriptor myDescr = node.getDescriptor(pid);
    insertReceivedItems(received, neighborsToSend, myDescr);
    neighborsToSend = null;
    
    ListTopology.instance.execute();
  }





  protected Vector<Descriptor> selectItemsToSend(Descriptor dest, Vector<Descriptor> received, int howmany)
  {
      if (howmany==0)
        return new Vector<Descriptor>(0); // empty vector

      Vector<Descriptor> itemsToSend = new Vector<Descriptor>(gossipSettings.gossipLen);

      /*
       * If I want to send ALL items, there's no need to sort them.
       * Otherwise shuffle them, to select random 'howmany' of them.
       */
      if (howmany < view.size())
      {
        // Sort based on the selectComparator...
        Collections.shuffle(view, CommonState.r);
        if (!(gossipSettings.selectComparator instanceof Random))
          Collections.sort(view, gossipSettings.selectComparator);
      }

      /*
       * And now select the 'howmany' first of my sorted view.
       * Any descriptor selected for sending is also removed from my view.
       */
      for (int i=0; i<view.size(); i++)
      {
        Descriptor item = view.elementAt(i);

        // Check if the selected descriptor is the destination node,
        // which should obviously be excluded.
        if (!item.equals(dest))
        {
          view.remove(i--);
          try {itemsToSend.add((Descriptor)item.clone());}
          catch (CloneNotSupportedException e) {e.printStackTrace();}
          if (--howmany==0)
            break;
        }
      }

    return itemsToSend;
  }




  /**
   * Inserts the descriptors received by the peer in my own view.
   * 
   * @param received
   * @param sent
   * @param self
   */
  protected void insertReceivedItems(Vector<Descriptor> received, Vector<Descriptor> sent, Descriptor self)
  {
    /*
     * Insert received descriptors to own view.
     */
    if (received != null)
      for (int i=0; i<received.size(); i++)
      {
        if (view.size() >= gossipSettings.viewLen)
          break;
        insertItem(received.elementAt(i));
      }

    assert view.size() <= gossipSettings.viewLen;

    /*
     * Now try filling up empty slots with neighbors I sent to the other peer.
     */
    if (sent != null)
    {
      int index=0;
      for (int i=gossipSettings.viewLen-view.size(); i>0; i--)
      {
        // If there are no more descriptors in the sent vector, break.
        if (index>=sent.size())
          break;

        /*
         * Add (sent) descriptor back to own view.
         * Just make sure it's not a descriptor of myself.
         */
        Descriptor sentItem = sent.elementAt(index);
        if (!sentItem.equals(self))
          insertItem(sentItem);

        index++;
      }
    }

    assert view.size() <= gossipSettings.viewLen;
  }





  protected void insertItem(Descriptor d)
  {
    int foundAt;

    // If 'd' is not in my view already, put it.
    if ( (foundAt=view.indexOf(d)) == -1 )
      view.add(d);

    // Else, we have a duplicate. Keep the preferred one.
    else
    {
      // contained already, at position 'foundAt'
      Descriptor existingItem = view.elementAt(foundAt);

      if (gossipSettings.duplComparator.compare(d,existingItem) < 0)
        view.setElementAt(d, foundAt);
    }
  } 

  
  public int degree()
  {
    return view.size() + (neighborsToSend==null ? 0 : neighborsToSend.size());
  }

  public Descriptor getNeighbor(int i)
  {
    int vs = view.size();
    return i<vs ? view.elementAt(i) : neighborsToSend.elementAt(i-vs);
  }

  public boolean contains(Descriptor d)
  {
    return view.contains(d) ||
           (neighborsToSend!=null && neighborsToSend.contains(d));
  }

  enum Type
  {
    GOSSIP_REQUEST,
    GOSSIP_RESPONSE;
  }
  
  public class Message
  {
    Type type;
    Descriptor sender;
    Vector<Descriptor> descriptors;
  }
}
