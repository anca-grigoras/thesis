/**
 * 
 */
package poldercast.protocols;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Node;
import peernet.core.Protocol;
import peernet.transport.Address;
import peernet.transport.Packet;
import peernet.transport.TransportUDP;
import poldercast.topics.RoutingTable;
import topics.Topic;
import topics.TopicsRepository;

import java.util.Random;
/**
 * @author anca
 *
 */
public class Dissemination extends Protocol
{
  DisseminationSettings disSettings;
  Vector<Long> msgIdContainer;
  //HashMap<Message,Long> receivedMessages;
  //HashMap<AbstractMap.SimpleEntry<Message,Long>,Vector<Descriptor>> sentMessages;
  boolean isInitialized = false;
  int fanout;
  ReentrantLock lock = new ReentrantLock();
   
  /**
   * @param prefix
   */
  public Dissemination(String prefix)
  {
    super(prefix);
    disSettings = (DisseminationSettings)settings;
    msgIdContainer = new Vector<Long>();
    //receivedMessages = new HashMap<Message,Long>();
//    sentMessages = new HashMap<AbstractMap.SimpleEntry<Message,Long>,Vector<Descriptor>>();
  }


 


  /* (non-Javadoc)
   * @see peernet.core.Protocol#processEvent(peernet.transport.Address, peernet.core.Node, int, java.lang.Object)
   */
  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {
    Message te = (Message)event;
    Descriptor selfDescr = createDescriptor();
    long time = CommonState.getTime();
//    receivedMessages.put(te, time);
    
    writeToFile(selfDescr, te, "r");
    
    Topic topic = te.topic;
    long msgId = te.id;
    //System.out.println("i can be whatever");
    if (msgIdContainer.contains(msgId))
      return;
    msgIdContainer.add(msgId);    
    
    Descriptor sender = te.sender;
    Message newTe = new Message();
    newTe.id = te.id;
    newTe.sender = selfDescr;
    newTe.topic = te.topic;
    newTe.hops = te.hops+1;
   
    fanout = disSettings.fanout;
    Rings rings = (Rings)node.getProtocol(disSettings.rtPid);
    
    disseminateEvent(selfDescr, sender, rings, topic, newTe, pid);
    
  }

  /**
   * @param sender 
   * @param rings
   * @param topic
   * @param newTe
   * @param pid 
   * @param fanout 
   */
  private void disseminateEvent(Descriptor selfDescr, Descriptor sender, Rings rings, 
      Topic topic, Message newTe, int pid)
  {
    RoutingTable rt = rings.getRingsNeighbors(topic);
    if (rt == null)
      return; //do I return or send just to random neighbors?
    Vector<Descriptor> selectedIds = new Vector<Descriptor>();
    sendEvent(selfDescr, rt.getPred(), sender.getID(), pid, newTe, selectedIds);//sends at most one pred
    sendEvent(selfDescr, rt.getSucc(), sender.getID(), pid, newTe, selectedIds);//sends at most one succ
    Vector<Descriptor> randomNeighbors = rings.getRandomNeighbors(node, pid, topic);
    if (randomNeighbors.isEmpty())
    {
      AbstractMap.SimpleEntry<Message, Long> pair = new SimpleEntry<Message, Long>(newTe, CommonState.getTime());
//      sentMessages.put(pair, selectedIds);
      return;
    }
    int remaining = fanout;
    while (remaining > 0)
    {
      Descriptor randomDescr = chooseRandomNeighbor(randomNeighbors, selectedIds, sender.getID());
      if (randomDescr == null) 
      {
        AbstractMap.SimpleEntry<Message, Long> pair = new SimpleEntry<Message, Long>(newTe, CommonState.getTime());
//        sentMessages.put(pair, selectedIds);
        return;
      }
      selectedIds.add(randomDescr);
      remaining--;
      try{Thread.sleep(new Random().nextInt(100));}
      catch (InterruptedException e){e.printStackTrace();}
      send(randomDescr.address, pid, newTe);
      writeToFile(selfDescr, newTe, "s");
    }
    AbstractMap.SimpleEntry<Message, Long> pair = new SimpleEntry<Message, Long>(newTe, CommonState.getTime());
//    sentMessages.put(pair, selectedIds);
  }


  private void sendEvent(Descriptor selfDescr, Vector<Descriptor> neighbors, long senderId, int pid, 
      Message te, Vector<Descriptor> selectedIds)
  {
    boolean isSent = false;
    int i = 0;
    while (!isSent)
    {
      Descriptor descr = null;
      if (i < neighbors.size())
        descr = neighbors.get(i);
      i++;
      if (descr == null)
        return;
      if(descr.getID() == senderId)
        continue;
      

      selectedIds.add(descr);
      fanout--;
      isSent = true;
      //System.out.println("sender: " + senderId + ", current " +te.sender.getID() + "(hops: "+ te.hops + "), to " + descr.getID() + " -> " + descr.address);
      try{Thread.sleep(new Random().nextInt(100));}
      catch (InterruptedException e){e.printStackTrace();}
      
      send(descr.address, pid, te);      
      writeToFile(selfDescr, te, "s");
    }
  }
  
  private void writeToFile(Descriptor descr, Message te, String label)
  {
    lock.lock();
    try {
      String line = descr.getID() + "\t" + 
          te.id + "\t" +
          te.topic.getId() + "\t" +
          te.hops + "\t" + 
          CommonState.getTime() + "\t" + 
          label;

      try
      {
        //System.out.println(line);
        disSettings.log.write(line);
        disSettings.log.newLine();
        disSettings.log.flush();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    } finally{
      lock.unlock();
    }
  }

  private Descriptor chooseRandomNeighbor(Vector<Descriptor> randomNeighbors, Vector<Descriptor> selectedIds, long l)
  {
    Descriptor chosen = null;
    boolean isChosen = false;
    while (randomNeighbors.size() > 0 && !isChosen)
    {
      int index = CommonState.r.nextInt(randomNeighbors.size());
      chosen = randomNeighbors.get(index);
      if (!selectedIds.contains(chosen) && !(chosen.getID() == l))
        isChosen = true;
      else
      {
        randomNeighbors.remove(index);
        chosen = null;
      }
        
    }
    return chosen;
  }
  
  
  /*public HashMap<Message,Long> getReceivedMessages()
  {
    return receivedMessages;
  }
  
  public HashMap<AbstractMap.SimpleEntry<Message,Long>,Vector<Descriptor>> getSentMessages()
  {
    return sentMessages;
  }
  
  public void resetSentMessages()
  {
    sentMessages.clear();
  }
  
  public void resetReceivedMessages()
  {
    receivedMessages.clear();
  }*/

  /* (non-Javadoc)
   * @see peernet.core.Protocol#nextCycle(peernet.core.Node, int)
   */
  @Override
  public void nextCycle(Node node, int protocolID)
  {
    // TODO Auto-generated method stub
  }
  
  
  @Override
  public Object clone()
  {
    Dissemination diss = null;
    diss = (Dissemination) super.clone();
    diss.msgIdContainer = (Vector<Long>)msgIdContainer.clone();
//    diss.receivedMessages = (HashMap<Message,Long>)receivedMessages.clone();
//    diss.sentMessages = (HashMap<AbstractMap.SimpleEntry<Message,Long>,Vector<Descriptor>>) sentMessages.clone();
    return diss;
  }
}
