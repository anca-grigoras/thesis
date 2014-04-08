package topics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import peernet.config.Configuration;
import peernet.core.Network;
import peernet.core.Node;

/**
 * 
 * @author anca
 *
 */

public class TopicsRepository //implements Topic
{
  private static String PAR_FILE = "file";
  
  private static HashMap<Long,Vector<Topic>> topicRepo = new HashMap<Long, Vector<Topic>>();//<nodeid, interests>
  private static HashMap<Topic,Vector<Long>> subscribers = new HashMap<Topic, Vector<Long>>(); //<TopicId,subscribers>
  
  
  /**
   * Read the file name 
   * @param prefix
   */
  public static void initTopics(String prefix)
  {
    String fileName = Configuration.getString(prefix + "." + PAR_FILE);
    mapFileToRep(fileName);    
  }
    
  /**
   * Reads the file line by line and it stores the node interests and the subscribers for topics
   * @param filename
   */
  private static void mapFileToRep(String filename) {
      
      BufferedReader bufferedReader = null;
      Vector<Topic> nodeSubscriptionList;
      
     
      try {
          //Construct the BufferedReader object
          System.out.println(filename);
          bufferedReader = new BufferedReader(new FileReader(filename));
          
          String line = null;
          long key= 0;
          while ((line = bufferedReader.readLine()) != null) {
              //Process the data, here we just print it out
            nodeSubscriptionList = new Vector<Topic>();
            String[] tokens = line.split(",");   
            for (int i = 0; i < tokens.length; i++)
            {
              Topic t =  new Topic(Long.parseLong(tokens[i]));
              nodeSubscriptionList.add(t);
              addSubscriber(t, key);
            }
            topicRepo.put(key, nodeSubscriptionList);
            key++;
          }
          
      } catch (FileNotFoundException ex) {
          ex.printStackTrace();
      } catch (IOException ex) {
          ex.printStackTrace();
      } finally {
          //Close the BufferedReader
          try {
              if (bufferedReader != null)
                  bufferedReader.close();
          } catch (IOException ex) {
              ex.printStackTrace();
          }
      }
     //printSubscribers();
  }

  /**
   * Return the vector of topics for a node id
   * @param nodeIndex
   * @return
   */
  public static Vector<Topic> getTopics(long nodeIndex)
  {
    return topicRepo.get(nodeIndex);
  }
  
  /**
   * Return the number of nodes with whom node id has at least one topic in common
   * @param id
   * @return
   */
  public static int getMaximNeighbors(int id)
  {
    int neighbbors = 0;
    for (int i = 0; i< topicRepo.size(); i++)
    {
      if (i != id)
      {
        if (getCommonTopics(id, i) > 0)
          neighbbors++;
      }
    }
    return neighbbors;
  }
  
  /**
   * Return the number of common topics between two node indexes
   * @param index1
   * @param index2
   * @return
   */
  public static int getCommonTopics (long index1, long index2)
  {
    int commonTopics = 0;
    for (int i = 0; i< topicRepo.get(index1).size(); i++)
      if (topicRepo.get(index2).contains(topicRepo.get(index1).get(i)))
        commonTopics++;
    return commonTopics;
  }
  
  
  /**
   * Add subscriber to a topic
   * @param topic
   * @param subscriber
   */
  private static void addSubscriber(Topic topic, long subscriber)
  {
    if (subscribers.containsKey(topic))
      subscribers.get(topic).add(subscriber);
    else
    {
      Vector<Long> v = new Vector<Long>();
      v.add(subscriber);
      subscribers.put(topic, v);
    }
  }  

  /**
   * @param topic
   * @return the ids of subscribers of topic
   */
  public static Vector<Long> getSubscribers(Topic topic)
  {
    return subscribers.get(topic);
  }
 
  /**
   * @return a vector with all the topics
   */
  public static Vector<Topic> getAllTopics()
  {
    Vector<Topic> allTopics = new Vector<Topic>();
    Set<Topic> set = subscribers.keySet();
    allTopics.addAll(set);
    return allTopics;
  }  
  
  /**
   * prints the subscribers for each topic
   */
  public static void printSubscribers()
  {
    Set<Entry<Topic,Vector<Long>>> set = subscribers.entrySet();
    Iterator<Entry<Topic,Vector<Long>>> it = set.iterator();
    while(it.hasNext())
    {
      Entry<Topic,Vector<Long>> entry = it.next();
      System.out.print("< " + entry.getKey().getId() + "> ");
      for (int i = 0; i< entry.getValue().size(); i++)
        System.out.print(entry.getValue().get(i) + " ");
      System.out.println();
    }
    
    for (int i = 0; i< Network.size();i++)
    {
      Node n = Network.get(i);
      System.out.println("I am node " + n.getID());
     /* for (Topic t : topicRepo.get(j))
        System.out.print(t.getId() + " ");
      System.out.println();*/
    }
  }
  
}
