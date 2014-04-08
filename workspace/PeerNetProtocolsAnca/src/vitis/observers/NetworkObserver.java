/**
 * 
 */
package vitis.observers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Descriptor;
import peernet.core.Network;
import peernet.core.Node;
import sun.security.krb5.internal.crypto.Des;
import topics.Topic;
import topics.TopicsRepository;
import vitis.protocols.Cyclon;
import vitis.protocols.Dissemination;
import vitis.protocols.TMan;
import vitis.protocols.Vitis;
import vitis.pubsub.TopicEvent;
import vitis.types.Statistics;

/**
 * @author anca
 *
 */
public class NetworkObserver implements Control
{
  private static final String PAR_PROT_CYCLON = "cyclon";
  private static final String PAR_PROT_TMAN = "tman";
  private static final String PAR_PROT_VITIS = "vitis";

  int pid, cyclonPid, tmanPid, vitisPid;
  private int cycle = 0;
  private static HashMap<Topic,Vector<TopicEvent>> publishedEvents = new HashMap<Topic, Vector<TopicEvent>>();
  BufferedWriter log;
  /**
   * 
   */
  public NetworkObserver(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    cyclonPid = Configuration.getPid(name+"."+PAR_PROT_CYCLON);
    tmanPid = Configuration.getPid(name+"."+PAR_PROT_TMAN);
    vitisPid = Configuration.getPid(name+"."+PAR_PROT_VITIS);
    
    File generalLogPath = new File("logvitis.txt");
    
    try {
      log = new BufferedWriter(new FileWriter(generalLogPath));
      log.write("#node_id msg_id topic hops ");
      log.newLine();
      log.flush();
    }catch (IOException x) {
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
    if (cycle == 70 || cycle ==71)
      control();
    //tmanObserver();
    //vitisObserver();
    //doMeasurements();

    return false;
  }

  
  private void control()
  {
    for (int i = 0; i< Network.size(); i++)
    {
      Node n = Network.get(i);
      Cyclon c = (Cyclon)n.getProtocol(cyclonPid);
      TMan t = (TMan)n.getProtocol(tmanPid);
      int msg = c.getControlMsg()+t.getControlMsg();
      c.resetControlMsg();t.resetControlMsg();
      System.out.println(TopicsRepository.getTopics(n.getID()).size() + "\t" + 10*msg);
    }
  }
  
  /**
   * 
   */
  private void tmanObserver()
  {
    verifyRing();
    plotTman();
    howManySmallWorldLinks();

  }
  /**
   * 
   */
  private void verifyRing()
  {
    Node node;
    TMan tman;
    int wrongLinks = 0;
    Descriptor pred, succ;
    int size = Network.size();
    Node[] sortedNodes = new Node[size];
    TreeMap<Long, Node> nodesCollection = new TreeMap<Long, Node>();

    for (int i = 0; i < Network.size(); i++) {
      node = Network.get(i);
      nodesCollection.put(node.getID(), node);
    }

    nodesCollection.values().toArray(sortedNodes);

    for (int i = 0; i < size; i++) {
      tman = (TMan)sortedNodes[i].getProtocol(tmanPid);

      pred = tman.getPred();
      succ = tman.getSucc();

      if (!(sortedNodes[(size + i - 1) % size].getID() == pred.getID()))
        wrongLinks++;

      if (!(sortedNodes[(i + 1) % size].getID() == succ.getID()))
        wrongLinks++;   
    }

    System.out.println("Wrong ring links: " + wrongLinks);

  }
  /**
   * 
   */
  private void plotTman()
  {
    TMan tman;
    long id;
    Descriptor succ;
    Descriptor pred;
    Vector<Descriptor> cluster; 


    String ringStr = "graph g {\n";
    String clusterStr = "digraph g {\n";

    for (int i = 0; i < Network.size(); i++) {
      Node n = Network.get(i);
      id = n.getID();
      tman = (TMan)n.getProtocol(tmanPid);

      // draw ring pointers
      pred = tman.getPred();
      succ = tman.getSucc();
      if (pred != null && succ == null)
        ringStr += id + "-- " + pred.getID() + ";\n";
      else if (pred == null && succ != null)
        ringStr += id + " -- " + succ.getID() + ";\n";
      else if (pred != null && succ != null) {
        ringStr += id + " -- " + succ.getID() + ";\n";
        ringStr += id + "-- " + pred.getID() + ";\n";
      }

      // draw cluster pointers
      cluster = tman.getCluster();
      if (cluster != null) {
        for (Descriptor neighbor : cluster)
          clusterStr += id + "->" + neighbor.getID() + ";\n";
      }
    }
    ringStr += "}";
    clusterStr += "}";
    System.out.println(ringStr);
    System.out.println(clusterStr);
  }
  /**
   * 
   */
  private void howManySmallWorldLinks()
  {
    TMan tman;
    long id;
    int numOfLinks;
    int sum = 0;
    double avgLinks = 0;
    int []histogram = new int[8];

    for (int i = 0; i < 8; i++)
      histogram[i] = 0;

    for (int i = 0; i < Network.size(); i++) {
      Node n = Network.get(i);
      id = n.getID();
      tman = (TMan)n.getProtocol(tmanPid);


      numOfLinks = tman.getNumOfSmallWorldLinks();
      histogram[numOfLinks]++;
      sum += numOfLinks;
    }

    avgLinks = sum / Network.size();

    String str = "";
    for (int i = 0; i < 8; i++)
      str +=  i + "\t" + histogram[i] + "\n";
    System.out.println(str);
  }

  private void vitisObserver()
  {
    HashMap<Topic, Vector<Long>> subscribersMap = new HashMap<Topic, Vector<Long>>();

    for (Topic t : TopicsRepository.getAllTopics())
      subscribersMap.put(t, TopicsRepository.getSubscribers(t));

    verifySubgraphs(subscribersMap);
  }
  
  /**
   * @param subscribersMap
   */
  private void verifySubgraphs(HashMap<Topic, Vector<Long>> subscribersMap)
  {
    String str = "";
    Vector<Long> interestedNodes;
    
    for (Topic topic : subscribersMap.keySet()) {
      interestedNodes = subscribersMap.get(topic);      
      Vector<Long> pathList = findPathList(interestedNodes.get(0), topic);
      Vector<Long> relayPeers = new Vector<Long>();
      relayPeers.addAll(pathList);
      relayPeers.removeAll(interestedNodes);
      Vector<Long> partitionedPeers = new Vector<Long>();
      partitionedPeers.addAll(interestedNodes);
      partitionedPeers.removeAll(pathList); 
      str += "topic: " + topic.getId() + "\t\trelay nodes: " + relayPeers.size() + "\t\tpath size: " + pathList.size() + "\t\tpartitioned peers: " + partitionedPeers.size() + "\t\tpopulation: " + interestedNodes.size() + "\n";
      
      Vitis vitis;
      if (partitionedPeers.size() > 0) {
        str += "path list: \n";
        for (long profile : pathList) {
          vitis = (Vitis)Network.getByID((int)profile).getProtocol(vitisPid);
          if (!vitis.getFriends().isEmpty())
            str += profile + "==> friends: " + vitis.getFriends();
          else
            str +=  profile + "==> No friends! "; 
          
          if (!vitis.getFans().isEmpty())
            str += ", fans: " + vitis.getFans() + "\n\n";
          else
            str += " No fans! ";
        }
        
        str += "-----------------------------\n\n";
        str += "partitioned peers: \n";
        for (long profile : partitionedPeers) {
          vitis = (Vitis)Network.getByID((int)profile).getProtocol(vitisPid);
          if (!vitis.getFriends().isEmpty())
            str += profile + "==> friends: " + vitis.getFriends();
          else
            str +=  profile + "==> No friends! "; 
          
          if (!vitis.getFans().isEmpty())
            str += ", fans: " + vitis.getFans() + "\n\n";
          else
            str += " No fans! ";
        }

        str += "-----------------------------\n\n";
        str += "population: " + interestedNodes.size() + "\n\n\n\n";
      }      
    }
    System.out.println(str);
  }

  /**
   * @param long1
   * @param topicID
   * @return
   */
  private Vector<Long> findPathList(Long interestedNode, Topic topic)
  {
    int i = 0;
    Vitis vitis;
    long node;
    Vector<Long> nodeInterestedNeighbors = new Vector<Long>();
    Vector<Long> pathList = new Vector<Long>();
    
    pathList.add(interestedNode);
    
    while (i < pathList.size()) {
      node = pathList.get(i);
      vitis = (Vitis)Network.getByID((int) node).getProtocol(vitisPid);
      for (Descriptor d : vitis.findInterestedPeers(node, topic))
        nodeInterestedNeighbors.add(d.getID());
      
      for (Long profile : nodeInterestedNeighbors) {
        if (!pathList.contains(profile))
          pathList.add(profile);
      }
      
      i++;
    }
      
    return pathList;
  }

  /**
   * 
   */
  private void doMeasurements()
  {
    Dissemination diss;

    Statistics nodeStatistics;
    float relayTraffic;
    float totalRelayTraffic = 0;
    float avgHopCounts;
    float totalHopCounts = 0;
    Vector<TopicEvent> relevantEvents = new Vector<TopicEvent>();
    int numRelevantEvents;
    float missingEventsPercentage;
    float hitRatio;
    float totalMissRatio = 0;
    //    int numberOfReceiverNodes = 0;
    int numberOfRelevantNodes = 0;
    int netSize = Network.size();

    for (int i = 0; i< netSize; i++)
    {
      Node n = Network.get(i);
      Vector<Topic> myTopics = TopicsRepository.getTopics(n.getID());
      int interested = myTopics.size();
      diss = (Dissemination)n.getProtocol(pid);
      nodeStatistics = diss.getStatistics();

      relayTraffic = nodeStatistics.getRelayTraffic();
      totalRelayTraffic += relayTraffic;


      avgHopCounts = nodeStatistics.getAvgHopCounts();
      totalHopCounts += avgHopCounts;
      if (avgHopCounts > 0) // we don't count the hops for relay nodes, so we should exclude them here too
        numberOfRelevantNodes++;

      /*relevantEvents.clear();
      for (Topic topic : myTopics)
        if (publishedEvents.get(topic) != null)
          relevantEvents.addAll(getPublishedEvents(topic));
      System.out.println("------------- relevant events --------------");
      for (TopicEvent te : relevantEvents)
      {
        String r = "node " + n.getID() + "\tmsgId: " + te.getId() + "\ttopic: " + te.getTopic().getId() + "\thops: " + te.getHopCounts() + "\tr"
            + "\ttime: " + te.getTime();
        System.out.println(r);
      }
      System.out.println("---------------------------------------------");
      numRelevantEvents = relevantEvents.size();     
      if (numRelevantEvents > 0) System.out.println("yeeeeeeeeeeeeyyyyyyyyyyyyyyyyyyyyyy");*/
      //relevantEvents.removeAll(diss.getReceivedEvents());
      int hits = 0;
      for (TopicEvent te : diss.getReceivedEvents())
        if (myTopics.contains(te.getTopic()))
          hits++;

      hitRatio = (float)hits/interested*100;
      missingEventsPercentage = 100 - hitRatio;
      totalMissRatio+=missingEventsPercentage;
     
      /*String str = "node: " + n.getID() + "\t%relay traffic: " + relayTraffic + "\tavg hop counts: " + avgHopCounts +
          "\t%hitRatio: " + hitRatio + "\t#received events: " +  diss.getReceivedEvents().size() + "\t#relevant events: " + numRelevantEvents +
          "\t topics: " + myTopics.size();*/

      for (TopicEvent te : diss.getReceivedEvents())
      {
        String path ="<";
        for (Descriptor d : te.getPath())
          path +=d.getID() + ", ";
        path +=">";
        String r = "node " + n.getID() + "\tmsgId: " + te.getId() + "\ttopic: " + te.getTopic().getId() + "\thops: " + te.getHopCounts() + "\tr"
            + "\ttime: " + te.getTime() + "\t" + path;
       // System.out.println(r);
        /*try{
          log.write(r);
          log.newLine();
          log.flush();
        }catch (IOException x) {
          System.err.format("IOException: %s%n", x);
        }*/
      }
      
      Set<Entry<TopicEvent,Vector<Descriptor>>> set = diss.getSentMessages().entrySet();
      Iterator<Entry<TopicEvent,Vector<Descriptor>>> it = set.iterator();
      
      while (it.hasNext())
      {
        Entry<TopicEvent,Vector<Descriptor>> entry = it.next();
        String path="<";
        for (Descriptor d : entry.getValue())
          path += d.getID() + ", ";
        path += ">";
        String s = "node " + n.getID() + "\tmsgId: " + entry.getKey().getId() + "\ttopic: " + entry.getKey().getTopic().getId() + "\thops: " + 
                entry.getKey().getHopCounts() + "\ts" + "\ttime: " + entry.getKey().getTime() + "\t" + path;
        //System.out.println(s);
        /*try {
          log.write(s);
          log.newLine();
          log.flush();
        }catch (IOException x) {
          System.err.format("IOException: %s%n", x);
        }*/
      }
      //System.out.println(str);
      diss.resetReceivedMessages();
      diss.resetSentMessages();
    }
    
    try {
      log.write(cycle + " " + totalMissRatio/netSize);
      log.newLine();
      log.flush();
    }catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }
    System.out.println(cycle + " " + totalMissRatio/netSize);
    
    //publishedEvents.clear();
  }

  public Vector<TopicEvent> getPublishedEvents(Topic topic) {
    long maxTime = 0;
    Vector<TopicEvent> eventsOnThisTopic = publishedEvents.get(topic);
    Vector<TopicEvent> relevantPublishedEvents = new Vector<TopicEvent>();
    if (eventsOnThisTopic != null) {
      for (TopicEvent event: eventsOnThisTopic)
        if (event.getTime() > maxTime)
        {
          relevantPublishedEvents.clear();
          maxTime = event.getTime();
          relevantPublishedEvents.add(event);
        }
        else if (event.getTime() == maxTime)       
          relevantPublishedEvents.add(event);
    }

    return relevantPublishedEvents;
  }

  /**
   * @param topicEvent
   */
  public static void addPublishedEvent(TopicEvent topicEvent)
  {
    Topic topic = topicEvent.getTopic();

    if (!publishedEvents.containsKey(topic))
      publishedEvents.put(topic, new Vector<TopicEvent>());

    publishedEvents.get(topic).add(topicEvent);

  }
}
