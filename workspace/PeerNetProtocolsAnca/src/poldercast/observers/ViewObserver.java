package poldercast.observers;

import peernet.config.Configuration;
import peernet.core.Linkable;
import peernet.core.Network;
import peernet.core.Node;
import peernet.core.Control;
import peernet.transport.AddressSim;
import topics.TopicsRepository;

/**
 * 
 * @author anca
 * Observer used for Vicinity: it counts how many nodes have a complete view. A complete view means that
 * the node has at least one topic in common with each of the neighbors from the view. If the number of
 * such neighbors is less than the view size, the percentage is calculated with respect to this number.
 */
public class ViewObserver implements Control 
{
  private static final String PAR_VIEWLEN = "viewlen";
  int pid;
  int cycle = 0;
  int viewLen;
  
  public ViewObserver(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    viewLen = Configuration.getInt(name + "." + PAR_VIEWLEN);
  }

  protected void getViewPercentace()
  {
    int count, maxSize;
    int completeView = 0;
    
    int zeroCount = 0;
    cycle ++;
    for (int i = 0; i<Network.size(); i++)
    {
      count = 0;
      Node node = Network.get(i);
      Linkable l = (Linkable) node.getProtocol(pid);
      for (int j = 0; j< l.degree(); j++)
      {
        Node n = ((AddressSim)l.getNeighbor(j).address).node;
        if (TopicsRepository.getCommonTopics(node.getIndex(), n.getIndex()) > 0)
          count++;        
      }
      maxSize = TopicsRepository.getMaximNeighbors(node.getIndex());
      
      if (maxSize > viewLen)
        maxSize = viewLen;
      if (count == maxSize)
        completeView++;
      if (count == 0)
        zeroCount++;
    }
    System.out.println(cycle + " " + (double)completeView/Network.size()*100);
  }
  @Override
  public boolean execute()
  {
    getViewPercentace();
    return false;
  }
}
