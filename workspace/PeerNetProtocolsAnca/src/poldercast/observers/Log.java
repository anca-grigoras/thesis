/**
 * 
 */
package poldercast.observers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
import poldercast.topics.TopicEvent;
import topics.TopicsRepository;

/**
 * @author anca
 *
 */
public class Log implements Control
{
  private static final String PAR_PATH = "path";
  private static final String PAR_SEC_PATH  = "pathsec";
  
  private static final String PAR_PROT1 = "prot1";
  private static final String PAR_PROT2 = "prot2";
  private static final String PAR_PROT3 = "prot3";
  
 
  int pid, pid1, pid2, pid3;
  int cycle = 0;
  int size = 0;
  BufferedWriter writer, writersec;
  /**
   * 
   */
  public Log(String name)
  {
    pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
    pid1 = Configuration.getPid(name+"."+PAR_PROT1);
    pid2 = Configuration.getPid(name+"."+PAR_PROT2);
    pid3 = Configuration.getPid(name+"."+PAR_PROT3);
    
    long time = CommonState.getTime();
    Path file = Paths.get(Configuration.getString(name+"."+PAR_PATH)+time+".txt");
    Path second = Paths.get(Configuration.getString(name+"."+PAR_SEC_PATH)+time+".txt");
    size = Network.size();
    
    Charset charset = Charset.forName("US-ASCII");
    String s = "# nodeId msgId topicId hops time";    
    String s1 = "# nodeId nummsgreceived nummsgfwd numdupmsg numcontrolmsg subscriptionsize";
    try 
    {
        writer = Files.newBufferedWriter(file, charset);
        //writer.write(s, 0, s.length());
        writer.write(s);
        writer.newLine();
        writer.flush();
        
        writersec = Files.newBufferedWriter(second, charset);
        writersec.write(s1);
        writersec.newLine();
        writersec.flush();
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
    logInfo();
    System.out.println("Done");
    return false;
  }



  /**
   * @throws IOException 
   * 
   */
  private void logInfo()
  {
    int nummsgreceived = 0, nummsgfwd = 0, numdupmsg = 0, numcontrolmsg = 0, subscriptionsize = 0;
    try
    {
      String s = "cycle : " + cycle;      
      
      writer.newLine();
      writer.write(s);
      writer.newLine();
      writer.flush();
      
      writersec.newLine();
      writersec.write(s);
      writersec.newLine();
      writersec.flush();
      
      //System.out.println(s);
     // writer.newLine();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    
    for (int i = 0; i< size; i++)
    {  
      Node n = Network.get(i);
      Dissemination dissem = (Dissemination)n.getProtocol(pid);
      
      nummsgreceived = dissem.getReceived(); dissem.resetReceived();
      nummsgfwd = dissem.getForwarded(); dissem.resetForwarded();
      numdupmsg = dissem.getDuplicates(); dissem.resetDuplicates();
      
      Cyclon cyclon = (Cyclon)n.getProtocol(pid1);
      Vicinity vic = (Vicinity)n.getProtocol(pid2);
      Rings rings = (Rings)n.getProtocol(pid3);
      subscriptionsize = TopicsRepository.getTopics(n.getID()).size();
      numcontrolmsg = cyclon.controlMessages+vic.controlMessages+rings.controlMessages; //bandwidth consumption
      cyclon.resetControlMessages(); vic.resetControlMessages(); rings.resetControlMessages();
      //System.out.println(subscriptionsize + " \t" + bandwithConsumption);
      
      String newline = n.getID() + "\t" + nummsgreceived + "\t" + nummsgfwd + "\t" + numdupmsg + "\t" + numcontrolmsg + "\t" + subscriptionsize + "\t 0";//unwanted traffic
      try
      {
        writersec.write(newline);
        writersec.newLine();
        writersec.flush();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      
      
      HashMap<Message,Long> myMsg = dissem.getReceivedMessages();
      Set<Entry<Message,Long>> set = myMsg.entrySet();
      Iterator<Entry<Message,Long>> it = set.iterator();
      
      
      
      
      while(it.hasNext())
      {
        String path="<";
        Entry<Message,Long> entry = it.next();
        
        /*for (Descriptor d : entry.getKey().getPath())
          path += d.getID() + ", ";
        path +=">";*/
        
        String line = n.getID() + " \t" + 
                      entry.getKey().id + " \t" +
                      entry.getKey().topic + " \t" +
                      entry.getKey().hops + " \t" + 
                      entry.getValue();
        //System.out.println(line);
        try
        {
          writer.write(line);
          writer.newLine();
          writer.flush();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
       dissem.resetReceivedMessages();
    }
    
  }
}
