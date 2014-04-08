package poldercast.observers;

import peernet.config.Configuration;
import peernet.core.Control;
import peernet.core.Linkable;
import peernet.core.Network;
import peernet.core.Node;
import peernet.transport.AddressSim;

public class NeighborsObserver implements Control
{
  int pid;
  int k = 0;
  
  public NeighborsObserver(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
  }
  
  protected void getNodeView()
  {
    for (int i = 0; i<Network.size(); i++)
      {
        Node node = Network.get(i);
        Linkable l = (Linkable) node.getProtocol(pid);
        System.out.print("the view for node " + node.getID()+ ": ");
        for (int j = 0; j< l.degree(); j++)
        {
          Node n = ((AddressSim)l.getNeighbor(j).address).node;
         System.out.print(n.getID() + " ");
        }
        System.out.println();;
      }   
  }

  @Override
  public boolean execute()
  {
    getNodeView();
    return false;
  }
}
