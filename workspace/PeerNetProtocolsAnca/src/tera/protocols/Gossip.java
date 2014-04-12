/**
 * 
 */
package tera.protocols;

import java.util.Vector;

import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.core.Protocol;
import peernet.transport.Address;

/**
 * @author anca
 *
 */
public abstract class Gossip extends Protocol implements Linkable
{
  /**
   * This is the network view of gossiping protocols.
   * It is a Vector of 'Descriptor' objects, or of its subclasses.
   */
  public Vector<Descriptor> view;

  /**
   * Name
   */
  public String name = "anonymous node";
  /**
   * @param prefix
   */
  public Gossip(String prefix)
  {
    super(prefix);
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see peernet.core.Cleanable#onKill()
   */
  @Override
  public void onKill()
  {
    view = null;
    
  }



  /* (non-Javadoc)
   * @see peernet.core.Linkable#degree()
   */
  @Override
  public int degree()
  {
    return view.size();
  }



  /* (non-Javadoc)
   * @see peernet.core.Linkable#getNeighbor(int)
   */
  @Override
  public Descriptor getNeighbor(int i)
  {
    return view.elementAt(i);
  }



  /* (non-Javadoc)
   * @see peernet.core.Linkable#addNeighbor(peernet.core.Descriptor)
   */
  @Override
  public boolean addNeighbor(Descriptor neighbour)
  {
    // TODO Auto-generated method stub
    return false;
  }



  /* (non-Javadoc)
   * @see peernet.core.Linkable#contains(peernet.core.Descriptor)
   */
  @Override
  public boolean contains(Descriptor neighbor)
  {
    return view.contains(neighbor);
  }
  
  /**
   * Individual views are instantiated by means of the clone function.
   */
  public Object clone()
  {
    Gossip gossip = (Gossip)super.clone();

    // No need for deep cloning of 'view'.
    gossip.view = (Vector<Descriptor>)view.clone();
    return gossip;
  }
}