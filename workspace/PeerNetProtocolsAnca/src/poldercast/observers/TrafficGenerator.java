/**
 * 
 */
package poldercast.observers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import poldercast.protocols.Message;
import poldercast.topics.TopicEvent;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Descriptor;
import peernet.core.Network;
import peernet.core.Node;
import peernet.core.Protocol;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class TrafficGenerator implements Control
{
  int pid;
  int cycle = 0;
  public static int serialNumCounter = 0;

  public TrafficGenerator(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);

  }
  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    cycle++;
    for (Topic t : TopicsRepository.getAllTopics())
    {
      Vector<Long> subscribers = TopicsRepository.getSubscribers(t);
      int index = CommonState.r.nextInt(subscribers.size());
      int id = subscribers.get(index).intValue();
      Node sender = Network.getByID(id);
      Descriptor descr = sender.getProtocol(pid).createDescriptor();
      Message te = new Message();
      te.id = serialNumCounter++;
      te.sender = descr;
      te.topic = t;
      te.hops = 0;
      //TopicEvent te = new TopicEvent(msgId++, descr, t);
      sender.getTransportByPid(pid).send(sender, descr.address, pid, te);
    }
    return false;
  }
}
