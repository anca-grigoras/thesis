/**
 * 
 */
package poldercast.observers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Descriptor;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Message;
import poldercast.topics.TopicEvent;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class TrafficGeneratorNet implements Control
{
  int pid;
  int cycle = 0;
  HashMap<Topic, Vector<Long>> topicSet = new HashMap<Topic,Vector<Long>>();

  /**
   * 
   */
  public TrafficGeneratorNet(String name)
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
    if (cycle == 1)
    {
      for (int i = 0; i< Network.size(); i++) {
        long id = Network.get(i).getID();
        Vector<Topic> topics = TopicsRepository.getTopics(id);
        for (Topic topic : topics)
        {
          Vector<Long> subscribers = new Vector<Long>();

          if (topicSet.get(topic) != null) {
            if (!topicSet.get(topic).contains(id)) {
              subscribers.addAll(topicSet.get(topic));
              subscribers.add(id);
              topicSet.put(topic, subscribers);
            }
          }
          else {
            subscribers.add(id);
            topicSet.put(topic, subscribers);
          }
        }
      }
    }
    
   /* int topicIndex = CommonState.r.nextInt(topicSet.keySet().size());
    Iterator<Topic> it = topicSet.keySet().iterator();
    Topic t = null;
    int i = 0;
    while(i<topicIndex)
    {
      i++;
      t = it.next();
    }*/
    //Topic t = new Topic(971);
    
    int events = 0;
    for (Topic t : topicSet.keySet())
    {
      Vector<Long> subscribers = topicSet.get(t);
      int index = CommonState.r.nextInt(subscribers.size());
      int id = subscribers.get(index).intValue();
      Node sender = Network.getByID(id);
      Descriptor descr = sender.getProtocol(pid).createDescriptor();
      Message te = new Message();
      te.id = Long.parseLong(""+id+CommonState.getTime()+t.getId());
      te.sender = descr;
      te.topic = t;
      te.hops = 0;
      sender.getTransportByPid(pid).send(sender, descr.address, pid, te);
      events++;
    }
    //System.out.println(events + " events were published");
    return false;
  }
}
