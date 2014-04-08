/**
 * 
 */
package vitis.observers;

import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Descriptor;
import peernet.core.Network;
import peernet.core.Node;
import topics.Topic;
import vitis.pubsub.TopicEvent;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class TrafficGenerator implements Control
{
  int pid;
  int cycle  = 0;
  /**
   * 
   */
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
      
      /*PublishEvent pe = new PublishEvent(descr, t);
      PubSubEvent event = new PubSubEvent(PubSubEventType.PUBLISH, descr, pe);*/
      TopicEvent event = new TopicEvent(t, descr, "new event", new Vector<Descriptor>());
      //System.out.println(sender.getID() + " is sending an event for topic " + t.getId() + " in cycle " + cycle );
      //NetworkObserver.addPublishedEvent(event);
      sender.getTransportByPid(pid).send(sender, descr.address, pid, event);
    }
    return false;
  }
}
