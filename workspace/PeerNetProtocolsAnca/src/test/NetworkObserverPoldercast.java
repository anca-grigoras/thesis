/**
 * 
 */
package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Descriptor;
import peernet.core.Network;
import peernet.core.Node;
import poldercast.protocols.Cyclon;
import poldercast.protocols.Dissemination;
import poldercast.protocols.Message;
import poldercast.protocols.Rings;
import poldercast.protocols.Vicinity;
import poldercast.topics.RoutingTable;
import poldercast.topics.TopicEvent;
import topics.Topic;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class NetworkObserverPoldercast implements Control
{
  private static final String PAR_LOG_PATH = "log";
  private static final String PAR_STATISTICS_PATH  = "statistics";
  private static final String PAR_SUBSCRIPTION_SIZE_PATH = "subscriptionsize";
  private static final String PAR_TOPIC_POPULARITY_PATH = "topicpopularity";

  private static final String PAR_PROT_CYCLON = "cyclon";
  private static final String PAR_PROT_VICINITY = "vicinity";
  private static final String PAR_PROT_RINGS = "rings";

  int pid, cyclonPid, vicinityPid, ringsPid;
  int cycle = 0;
  BufferedWriter log, statistics, subscriptions, topicpop;

  int netSize, topicSize;
  Vector<Boolean> convergedNode = null;
  Vector<Boolean> converged = null;
  Vector<Topic> allTopics = null;

  /**
   * 
   */
  public NetworkObserverPoldercast(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    cyclonPid = Configuration.getPid(name+"."+PAR_PROT_CYCLON);
    vicinityPid = Configuration.getPid(name+"."+PAR_PROT_VICINITY);
    ringsPid = Configuration.getPid(name+"."+PAR_PROT_RINGS);
    
    createGeneralLogPathFile(name);
    //createStatisticsPathFile(name);
    //createSubscriptionsPath(name);
    //createTopicPopPath(name);

    
    netSize = Network.size();
    convergedNode = new Vector<Boolean>(netSize);
    for (int i = 0; i< netSize; i++)
      convergedNode.add(false);

    allTopics = TopicsRepository.getAllTopics();
    topicSize = allTopics.size();
    converged = new Vector<Boolean>(topicSize);
    for (int i = 0; i< topicSize; i++)
      converged.add(false);      
  }

  /**
   * @param name
   */
  private void createGeneralLogPathFile(String name)
  {
    File generalLogPath = new File(Configuration.getString(name + "." + PAR_LOG_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");
    
    String firstLine;

    //create the log file
    try 
    {
      firstLine = "# nodeId\tmsgId\ttopicId\thops\ttime";  
      log = new BufferedWriter(new FileWriter(generalLogPath));
      log.write(firstLine);
      log.newLine();
      log.flush();
      System.out.println("Created file " + generalLogPath.getName());
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }
  }

  /**
   * @param name
   */
  private void createStatisticsPathFile(String name)
  {
    File statisticsPath = new File(Configuration.getString(name + "." + PAR_STATISTICS_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");
    
    String firstLine;
    
    //create statistics file
    try 
    {
      firstLine = "# nodeId numMsgReceived numMsgFwd numDupMsg numControlMsg subscriptionSize unwantedtraffic degree"; 
      statistics = new BufferedWriter(new FileWriter(statisticsPath));
      statistics.write(firstLine);
      statistics.newLine();
      statistics.flush();
      System.out.println("Created file " + statisticsPath.getName());
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }
 
  }

  /**
   * @param name
   */
  private void createSubscriptionsPath(String name)
  {
    File subscriptionsPath = new File(Configuration.getString(name + "." + PAR_SUBSCRIPTION_SIZE_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");
    
    String firstLine;
    
    //create subscription size file
    try 
    {
      firstLine = "# nodeId numOfTopics cyclesToConverge"; 
      subscriptions = new BufferedWriter(new FileWriter(subscriptionsPath));
      subscriptions.write(firstLine);
      subscriptions.newLine();
      subscriptions.flush();
      System.out.println("Created file " + subscriptionsPath.getName());
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }

  }

  /**
   * @param name
   */
  private void createTopicPopPath(String name)
  {
    File topicPopPath = new File(Configuration.getString(name + "." + PAR_TOPIC_POPULARITY_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");

    String firstLine;   
    
    //create topic popularity file
    try 
    {
      firstLine = "# topicId numOfSubscribers cyclesToConverge"; 
      topicpop = new BufferedWriter(new FileWriter(topicPopPath));
      topicpop.write(firstLine);
      topicpop.newLine();
      topicpop.flush();
      System.out.println("Created file " + topicPopPath.getName());
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }

  }

  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    cycle++;
    String s = "cycle: " + cycle;
    System.out.println(s);
    //checkLostMessages();
    //logPublishedEvents();
    //doStatistics();
    //writeSubscriptionSize();
    //writeTopicPopularity();
    System.out.println("Done");
    return false;
  }

  /**
   * 
   */
  private void checkLostMessages()
  {
    for (int i = 0; i< netSize; i++)
    {
      Node n = Network.get(i);
      //Cyclon cyclon = (Cyclon)n.getProtocol(cyclonPid);
      Rings cyclon = (Rings)n.getProtocol(ringsPid);
      HashMap<Message,Long> s = cyclon.sentMessages;
      HashMap<Message,Long> r = cyclon.receivedMessages;
      try{
        Set<Entry<Message,Long>> set = s.entrySet();
        Iterator<Entry<Message,Long>> it = set.iterator();
        while (it.hasNext())
        {
          Entry<Message,Long> entry = it.next();
          String line = n.getID() + "\t"
                      + entry.getKey().id + "\t"
                      + entry.getValue() + "\t"
                      + "s";
          log.write(line);
          log.newLine();
          log.flush();
        }
        
        Set<Entry<Message,Long>> set2 = r.entrySet();
        Iterator<Entry<Message,Long>> it2 = set2.iterator();
        while(it2.hasNext())
        {
          Entry<Message,Long> entry = it2.next();
          String line = n.getID() + "\t"
                      + entry.getKey().id + "\t"
                      + entry.getValue() + "\t"
                      + "r";
          log.write(line);
          log.newLine();
          log.flush();
        }
      }catch (IOException e)
      {
        e.printStackTrace();
      }        
      
      cyclon.resetReceivedMessages();cyclon.resetSentMessages();
    }
    
  }

  /**
   * 
   */
 /* private void logPublishedEvents()
  {
    for (int i = 0; i< netSize; i++)
    {  
      Node n = Network.get(i);
      //System.out.println("i am node " + n.getID());
      Dissemination dissem = (Dissemination)n.getProtocol(pid);

      HashMap<Message,Long> myMsg = dissem.getReceivedMessages();
      Set<Entry<Message,Long>> set = Collections.synchronizedSet(myMsg.entrySet());
      Iterator<Entry<Message,Long>> it = set.iterator();

      while(it.hasNext())
      {
        Entry<Message,Long> entry = it.next();
        String path="<"; 
        for (Descriptor d : entry.getKey().getPath())
          path += d.getID() + ", ";
        path +=">";
        String line = n.getID() + "\t" + 
            entry.getKey().getId() + "\t" +
            entry.getKey().getTopicOfInterest().getId() + "\t" +
            entry.getKey().getHop() + "\t" + 
            entry.getValue() + "\t" +
            "r" + "\t" +
            path;      
        
        String line = n.getID() + "\t" + 
            entry.getKey().id + "\t" +
            entry.getKey().topic.getId() + "\t" +
            entry.getKey().hops + "\t" + 
            entry.getValue() + "\t" +
            "r";
        
        try
        {
          //System.out.println(line);
          log.write(line);
          log.newLine();
          log.flush();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
      
      HashMap<AbstractMap.SimpleEntry<Message,Long>,Vector<Descriptor>> mySentMsg = dissem.getSentMessages();
      Set<Entry<SimpleEntry<Message, Long>, Vector<Descriptor>>> sentSet = mySentMsg.entrySet();
      Iterator<Entry<SimpleEntry<Message, Long>, Vector<Descriptor>>> sentIt = sentSet.iterator();
      while (sentIt.hasNext())
      {
        Entry<SimpleEntry<Message, Long>, Vector<Descriptor>> sentEntry = sentIt.next();
        SimpleEntry<Message, Long> key = sentEntry.getKey();
        String path="<";
        for (Descriptor d : sentEntry.getValue())
          path += d.getID() + ", ";
        path +=">";
        String line = n.getID() + "\t" + 
                      key.getKey().id + "\t" +
                      key.getKey().topic.getId() + "\t" +
                      key.getKey().hops + "\t" + 
                      key.getValue() + "\t" + 
                      "s" + "\t" + 
                      sentEntry.getValue().size();

        
        try
        {
          //System.out.println(line);
          log.write(line);
          log.newLine();
          log.flush();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
      dissem.resetSentMessages();
      dissem.resetReceivedMessages();
    }

  }*/

  /**
   * 
   */
  private void doStatistics()
  {
    int nummsgreceived = 0, nummsgfwd = 0, numdupmsg = 0, numcontrolmsg = 0, subscriptionsize = 0;
    Vector<String> lines = new Vector<String>();
    HashMap<Long,Integer> degree = new HashMap<Long, Integer>();

    for (int i = 0; i< netSize; i++)
    {  
      Node n = Network.get(i);
      Dissemination dissem = (Dissemination)n.getProtocol(pid);

      /*nummsgreceived = dissem.getReceived(); dissem.resetReceived();
      nummsgfwd = dissem.getForwarded(); dissem.resetForwarded();
      numdupmsg = dissem.getDuplicates(); dissem.resetDuplicates();
*/
      Cyclon cyclon = (Cyclon)n.getProtocol(cyclonPid);
      Vicinity vic = (Vicinity)n.getProtocol(vicinityPid);
      Rings rings = (Rings)n.getProtocol(ringsPid);
      subscriptionsize = TopicsRepository.getTopics(n.getID()).size();
      numcontrolmsg = cyclon.controlMessages+vic.controlMessages+rings.controlMessages; //bandwidth consumption
      cyclon.resetControlMessages(); vic.resetControlMessages(); rings.resetControlMessages();

      String line = n.getID() + "\t" + nummsgreceived + "\t" + nummsgfwd + "\t" + numdupmsg + "\t" + numcontrolmsg + "\t" + subscriptionsize + "\t 0";//unwanted traffic
      lines.add(line);      

      //compute the degree
      Vector<Topic> topics = TopicsRepository.getTopics(n.getID());
      Vector<Descriptor> localView = new Vector<Descriptor>();
      int indegree = 0;
      for (Topic topic : topics)
        if (rings.getRT(topic) != null)
        {
          for (Descriptor d : rings.getRT(topic).getPred())
          {
            if (!localView.contains(d))
            {
              if(degree.get(d.getID()) == null)
                degree.put(d.getID(), 1);
              else
                degree.put(d.getID(), degree.get(d.getID())+1);
              indegree++;
              localView.add(d);
            }
          }
          for (Descriptor d : rings.getRT(topic).getSucc())
          {
            if(!localView.contains(d))
            {
              if(degree.get(d.getID()) == null)
                degree.put(d.getID(), 1);
              else
                degree.put(d.getID(), degree.get(d.getID())+1);
              indegree++;
              localView.add(d);
            }
          }
        }
      if(degree.get(n.getID()) == null)
        degree.put(n.getID(), indegree);
      else
        degree.put(n.getID(), degree.get(n.getID())+indegree);  
    }

    try
    {

      Set<Entry<Long,Integer>> set = degree.entrySet();
      Iterator<Entry<Long,Integer>> it = set.iterator();
      int index = 0; 
      while (it.hasNext())
      {
        Entry<Long,Integer> entry = it.next();
        String line = lines.get(index) + "\t" + entry.getValue();
        index++;

        statistics.write(line);
        statistics.newLine();
        statistics.flush();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * 
   */
  private void writeSubscriptionSize()
  {
    int convergedNodes = 0;
    HashMap<Long, Integer> nodeSubscrMap = new HashMap<Long, Integer>();
    for (int i = 0; i< Network.size(); i++)
    {
      if (!convergedNode.get(i))
      {
        int convergedRings = 0;
        Node n = Network.get(i);
        Rings rings = (Rings)n.getProtocol(ringsPid);
        Vector<Topic> myTopics = TopicsRepository.getTopics(n.getID());
        int subscriptionSize = myTopics.size();
        for (Topic topic : myTopics)
        {
          RoutingTable rt = rings.getRT(topic);
          if (isConverged(rt,n.getID(), topic))
            convergedRings++;
        }
        if (convergedRings == subscriptionSize)
        {
          convergedNodes++;
          nodeSubscrMap.put(n.getID(), subscriptionSize);
          convergedNode.remove((int)n.getID());convergedNode.add((int) n.getID(), true);
        }
      }
    }
    try
    {
      subscriptions.write(convergedNodes + " \t");
      subscriptions.flush();

      //System.out.print(convergedNodes + " \t");
      Set<Entry<Long,Integer>> set = nodeSubscrMap.entrySet();
      Iterator<Entry<Long,Integer>> it = set.iterator();
      while (it.hasNext())
      {
        Entry<Long,Integer> entry = it.next();
        System.out.print(entry.getValue() + " ");
        subscriptions.write(entry.getValue() + " \t");
      }
      subscriptions.newLine();
      subscriptions.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }


  }

  /**
   * @param rt
   * @param id
   * @param topic
   * @return
   */
  private boolean isConverged(RoutingTable rt, long id, Topic topic)
  {
    Vector<Long> bestNeighbors = getBestNeighbors(topic, id);
    Vector<Long> realNeighbors = getRealNeighbors(rt);
    for(long neighbor : bestNeighbors)
      if(!realNeighbors.contains(neighbor))
        return false;

    return true;
  }

  /**
   * @param rt
   * @return
   */
  private Vector<Long> getRealNeighbors(RoutingTable rt)
  {
    Vector<Long> neighbors = new Vector<Long>();
    if (rt!= null)
    {
      for (int j = 0; j< rt.getPred().size(); j++)
        neighbors.add(rt.getPred().get(j).getID());
      for(int j = 0; j <rt.getSucc().size(); j++)
        neighbors.add(rt.getSucc().get(j).getID());
    }
    return neighbors;
  }

  private Vector<Long> getBestNeighbors(Topic topic, long id)
  {
    Vector<Long> subscribers = TopicsRepository.getSubscribers(topic);
    Vector<Long> bestNeighbors = new Vector<Long>();

    Collections.sort(subscribers);
    bestNeighbors.addAll(getBestHalfNeighbors(subscribers, id));

    Collections.reverse(subscribers);
    bestNeighbors.addAll(getBestHalfNeighbors(subscribers, id));

    Collection<Long> noDup = new LinkedHashSet<Long>(bestNeighbors);
    bestNeighbors.clear();
    bestNeighbors.addAll(noDup);    

    return bestNeighbors;
  }

  /**
   * The method returns the successors if subscribers are in ascendent order and predecessors otherwise
   * The method is customized for 2 succ and 2 pred
   * @param subscribers
   * @param id
   * @return
   */
  private Vector<Long> getBestHalfNeighbors(Vector<Long> subscribers, long id)
  {
    int index = subscribers.indexOf(id);
    Vector<Long> neighbors = new Vector<Long>();
    for (int i = index+1; i<= index+2; i++)
      neighbors.add(subscribers.get(i%subscribers.size()));
    return neighbors;
  }

  /**
   * 
   */
  private void writeTopicPopularity()
  {
    int convergedRings = 0;
    Vector<Integer> convTopics = new Vector<Integer>();
    for (Topic topic : allTopics)
    {
      if (!converged.get((int) topic.getId()) && isConverged(topic))
      {
        convergedRings++;
        converged.remove(topic);converged.add((int) topic.getId(), true);
        convTopics.add(TopicsRepository.getSubscribers(topic).size());
      }
    }

    try
    {
      topicpop.write(convergedRings + " \t");
      topicpop.flush();
      for (int i = 0; i< convTopics.size(); i++)
        topicpop.write(convTopics.get(i) + " \t");
      topicpop.newLine();
      topicpop.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }  

  /**
   * @param topic
   * @return
   */
  private boolean isConverged(Topic topic)
  {
    Vector<Long> subscribers = TopicsRepository.getSubscribers(topic);
    Vector<Long> ringNodes = new Vector<Long>();
    checkRing(Network.getByID(subscribers.get(0).intValue()),ringNodes, topic);
    for (long l : subscribers)
      if(!ringNodes.contains(l))
        return false;
    return true;
  }

  /**
   * @param node
   * @param ringNodes
   * @param topic 
   */
  private void checkRing(Node node, Vector<Long> ringNodes, Topic topic)
  {
    Rings rings = (Rings)node.getProtocol(ringsPid);
    RoutingTable rt = rings.getRT(topic);
    if(rt != null)
    {
      for(Descriptor d : rt.getPred())
        if(!ringNodes.contains(d.getID()))
        {
          ringNodes.add(d.getID());
          checkRing(Network.getByID((int)d.getID()), ringNodes, topic);
        }
      for(Descriptor d : rt.getSucc())
        if(!ringNodes.contains(d.getID()))
        {
          ringNodes.add(d.getID());
          checkRing(Network.getByID((int)d.getID()), ringNodes, topic);
        }
    }    
  }
}
