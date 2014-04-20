///**
// * 
// */
//package test;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.TreeMap;
//
//import peernet.config.Configuration;
//import peernet.core.CommonState;
//import peernet.core.Control;
//import peernet.core.Descriptor;
//import peernet.core.Network;
//import peernet.core.Node;
//import vitis.protocols.TMan;
//import vitis.types.DateUtils;
//
///**
// * @author anca
// *
// */
//public class NetworkObserverVitis implements Control
//{
//  private static final String PAR_LOG_PATH = "log";
//  private static final String PAR_STATISTICS_PATH  = "statistics";
//  private static final String PAR_SUBSCRIPTION_SIZE_PATH = "subscriptionsize";
//  private static final String PAR_TOPIC_POPULARITY_PATH = "topicpopularity";
//  
//  private static final String PAR_PROT_CYCLON = "cyclon";
//  private static final String PAR_PROT_TMAN = "tman";
//  private static final String PAR_PROT_VITIS = "vitis";
//  
//  int pid, cyclonPid, tmanPid, vitisPid;
//  int cycle = 0;
//  BufferedWriter log, statistics, subscriptions, topicpop;
//  /**
//   * 
//   */
//  public NetworkObserverVitis(String name)
//  {
//    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
//    cyclonPid = Configuration.getPid(name+"."+PAR_PROT_CYCLON);
//    tmanPid = Configuration.getPid(name+"."+PAR_PROT_TMAN);
//    vitisPid = Configuration.getPid(name+"."+PAR_PROT_VITIS);
//
//    long time = CommonState.getTime();
//
//    Path generalLogPath = Paths.get(Configuration.getString(name + "." + PAR_LOG_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");
//    Path statisticsPath = Paths.get(Configuration.getString(name + "." + PAR_STATISTICS_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");
//    Path subscriptionsPath = Paths.get(Configuration.getString(name + "." + PAR_SUBSCRIPTION_SIZE_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");
//    Path topicPopPath = Paths.get(Configuration.getString(name + "." + PAR_TOPIC_POPULARITY_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");
//
//    Charset charset = Charset.forName("US-ASCII");
//    String firstLine;
//    
//    //create the log file
//    try 
//    {
//      firstLine = "# nodeId msgId topicId hops time";  
//      log = Files.newBufferedWriter(generalLogPath, charset);
//      log.write(firstLine);
//      log.newLine();
//      log.flush();
//    } catch (IOException x) {
//      System.err.format("IOException: %s%n", x);
//    }
//    
//    /*//create statistics file
//    try 
//    {
//      firstLine = "# nodeId numMsgReceived numMsgFwd numDupMsg numControlMsg subscriptionSize degree"; 
//      statistics = Files.newBufferedWriter(statisticsPath, charset);
//      statistics.write(firstLine);
//      statistics.newLine();
//      statistics.flush();
//    } catch (IOException x) {
//      System.err.format("IOException: %s%n", x);
//    }
//    
//    //create subscription size file
//    try 
//    {
//      firstLine = "# nodeId numOfTopics cyclesToConverge"; 
//      subscriptions = Files.newBufferedWriter(subscriptionsPath, charset);
//      subscriptions.write(firstLine);
//      subscriptions.newLine();
//      subscriptions.flush();
//    } catch (IOException x) {
//      System.err.format("IOException: %s%n", x);
//    }
//    
//    //create topic popularity file
//    try 
//    {
//      firstLine = "# topicId numOfSubscribers cyclesToConverge"; 
//      topicpop = Files.newBufferedWriter(topicPopPath, charset);
//      topicpop.write(firstLine);
//      topicpop.newLine();
//      topicpop.flush();
//    } catch (IOException x) {
//      System.err.format("IOException: %s%n", x);
//    }*/
//  }
//  /* (non-Javadoc)
//   * @see peernet.core.Control#execute()
//   */
//  @Override
//  public boolean execute()
//  {
//    cycle++;
//    String s = "cycle: " + cycle;
//    System.out.println(s);
//    
//    tmanObserver();
//    
//    //logPublishedEvents();
//    //doStatistics();
//    //writeSubscriptionSize();
//    //writeTopicPopularity();
//    System.out.println("Done");
//    return false;
//  }
//  /**
//   * 
//   */
//  private void tmanObserver()
//  {
//    verifyRing();
//    plotTman();
//    howManySmallWorldLinks();
//    
//  }
//  /**
//   * 
//   */
//  private void verifyRing()
//  {
//    Node node;
//    TMan tman;
//    int wrongLinks = 0;
//    Descriptor pred, succ;
//    int size = Network.size();
//    Node[] sortedNodes = new Node[size];
//    TreeMap<Long, Node> nodesCollection = new TreeMap<Long, Node>();
//  
//        for (int i = 0; i < Network.size(); i++) {
//          node = Network.get(i);
//          nodesCollection.put(node.getID(), node);
//        }
//
//        nodesCollection.values().toArray(sortedNodes);
//        
//        for (int i = 0; i < size; i++) {
//          tman = (TMan)sortedNodes[i].getProtocol(tmanPid);
//          
//      pred = tman.getPred();
//      succ = tman.getSucc();
//
//      if (!(sortedNodes[(size + i - 1) % size].getID() == pred.getID()))
//        wrongLinks++;
//
//      if (!(sortedNodes[(i + 1) % size].getID() == succ.getID()))
//        wrongLinks++;
//    }
//        
//    System.out.println("Wrong ring links: " + wrongLinks);
//    
//  }
//  /**
//   * 
//   */
//  private void plotTman()
//  {
//    // TODO Auto-generated method stub
//    
//  }
//  /**
//   * 
//   */
//  private void howManySmallWorldLinks()
//  {
//    // TODO Auto-generated method stub
//    
//  }
//  /**
//   * 
//   */
//  private void logPublishedEvents()
//  {
//    // TODO Auto-generated method stub
//    
//  }
//}
