/*
 * Created on Nov 25, 2009 by Spyros Voulgaris
 */
package powerlaw;

import gossip.protocol.Cyclon;

import java.util.Vector;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.core.Protocol;
import peernet.transport.Address;





public class PowerlawOverlay extends Protocol implements Linkable
{
  public Vector<Descriptor> view = null;



  /**
   * Default constructor. Called only once for a new protocol class instance.
   */
  public PowerlawOverlay(String prefix)
  {
    super(prefix);
  }



  public Object clone()
  {
    PowerlawOverlay prot = null;
    prot = (PowerlawOverlay) super.clone();
    // No need for deep cloning of 'view'.
    prot.view = new Vector<Descriptor>();
    return prot;
  }



  public void nextCycle(Node node, int pid)
  {
//    if (node.getID()==0)
//      System.err.println("powerlaw: \t"+CommonState.getTime());
    for (Descriptor d: view)
    {
      if (d==null)
        System.out.println("null!!!");
    }

    int linkableID = settings.getLinkable();
    Linkable cyclon = (Linkable) node.getProtocol(linkableID);

    // Compute my own fitness
    double fitNode = ((FitnessNode) node).fitness;

    for (int i=0; i<cyclon.degree(); i++)
    {
      if (degree() >= ((FitnessNode)node).expectedDegree)
      {
        ((Cyclon)cyclon).pause(true);
        continue;
      }

      DescriptorFitness peerDescr = (DescriptorFitness) cyclon.getNeighbor(i);
      double fitPeer = peerDescr.fitness;
      double vic = FitnessNode.fitnessFunction.vicinityFunction(fitNode, fitPeer);
      if (CommonState.r.nextDouble() < vic)  // I want to add him as a neighbor
      {
        //DescriptorFitness nodeDescr = (DescriptorFitness)node.getDescriptor(pid);
        DescriptorFitness nodeDescr = (DescriptorFitness) getOwnDescriptor();
        send(peerDescr.address, pid, new Message(nodeDescr, Message.Type.INVITE));
      }
    }
  }



  @Override
  public void processEvent(Address src, Node node, int pid, Object payload)
  {
    Message msg = (Message)payload;
    switch (msg.type)
    {
      case INVITE:
        Message response = null;
        Descriptor nodeDescr = getOwnDescriptor();
        if (degree() < ((FitnessNode)node).expectedDegree)
        {
          addNeighbor(msg.sender);
          response = new Message(nodeDescr, Message.Type.ACCEPT);
        }
        else
        {
          response = new Message(nodeDescr, Message.Type.REJECT);
        }

        send(src, pid, response);
        break;
        
      case ACCEPT:
        addNeighbor(msg.sender);
        break;
//
//      case REJECT:
//        break;
    }
  }



  public boolean addNeighbor(Descriptor neighbor)
  {
    if (view.contains(neighbor))
      return false;
    view.add(neighbor);
    return true;
  }


  @Override
  public boolean contains(Descriptor neighbor)
  {
    return view.contains(neighbor);
  }



  @Override
  public int degree()
  {
    return view.size();
  }



  @Override
  public Descriptor getNeighbor(int i)
  {
    return view.elementAt(i);
  }



  @Override
  public void onKill()
  {
    assert false;
  }
}
