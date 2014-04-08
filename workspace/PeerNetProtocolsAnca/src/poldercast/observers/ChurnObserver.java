/**
 * 
 */
package poldercast.observers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import peernet.core.Descriptor;
import peernet.core.Schedule;
import peernet.core.CommonState;
import peernet.core.Network;
import peernet.util.RandPermutation;
import peernet.config.Configuration;
import peernet.core.Control;
import peernet.util.IncrementalStats;

/**
 * @author anca
 *
 */
public class ChurnObserver implements Control
{
  private static final String PAR_FILE = "file";
  private static final String PAR_TICKS_PER_SEC = "ticks_per_sec";
  
  private String filename;
  /** Number of time units contained in one second */
  private int ticks_per_sec;
  private int step;
  private Integer[][] events;
  
  /**
   * 
   */
  public ChurnObserver(String prefix)
  {
    filename = Configuration.getString(prefix+"."+PAR_FILE);
    step = Configuration.getInt(prefix+"."+Schedule.PAR_STEP);
    ticks_per_sec = Configuration.getInt(prefix+"."+PAR_TICKS_PER_SEC);
    readFile();
  }



  /**
   * 
   */
  private void readFile()
  {
    String line = null;
    ArrayList<long[]> traces = new ArrayList<long[]>();
    
    long max = 0;
    int zn = 0;
    
    IncrementalStats sessions = new IncrementalStats();
    IncrementalStats length = new IncrementalStats();
    
    BufferedReader in = null;
    try
    {
      in = new BufferedReader(new FileReader(filename));
      
      while ((line = in.readLine())!=null)
      {
        StringTokenizer tok = new StringTokenizer(line);
        tok.nextToken();
        int n = Integer.parseInt(tok.nextToken());
        long[] trace = new long[n*2];
        for (int i = 0; i<n; i++)
        {
          long start = (long) (Double.parseDouble(tok.nextToken())*ticks_per_sec);
          long stop = (long) (Double.parseDouble(tok.nextToken())*ticks_per_sec);
          trace[i*2] = start;
          trace[i*2+1] = stop;
          if (stop > max)
            max = stop;
          long diff = stop-start;
          length.add(diff);
        }
        if (n>0)
        {
          sessions.add(n);
          traces.add(trace);
        }
        else
          zn++;
      }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    System.err.println("ZERO "+zn+" SESSIONS "+sessions+" LENGTH "+length);
    
    /*
     * Assign traces to nodes randomly: node i is assigned trace
     * assignedTraces[i]
     */
    int size = Network.size();
    int[] assignedTraces = new int[size];
    RandPermutation rp = new RandPermutation(CommonState.r);
    rp.reset(traces.size());
    for (int i = 0; i<assignedTraces.length; i++)
    {
      if (rp.hasNext()==false)
        rp.reset(traces.size());
      assignedTraces[i] = rp.next();
    }
    
 // From a trace array to an array of cycles
    ArrayList<Integer>[] cycles = new ArrayList[(int) (max/step+1)];
    for (int i = 0; i<cycles.length; i++)
      cycles[i] = new ArrayList<Integer>();
    for (int i = 0; i<size; i++)
    {
      long[] trace = traces.get(assignedTraces[i]);
      for (int j = 0; j<trace.length; j++)
        cycles[(int) (trace[j]/step)].add(i);
    }
    
 // From ArrayList[] to Node[][]
    events = new Integer[cycles.length][];
    for (int i = 0; i<cycles.length; i++)
      if (cycles[i].size()!=0)
      {
        events[i] = cycles[i].toArray(new Integer[cycles[i].size()]);
        int sizeofchurn = events[i].length;
        // if(sizeofchurn > 10){
        // System.out.println(i + "\t" + sizeofchurn);
        // }
      }
    
  }
  
  void removeDeadNodes(Vector<Descriptor> view)
  {
    for (int i = 0; i<view.size(); ++i)
    {
      if ( Network.getByID((int) view.get(i).getID()).isUp() == false)
      {
        view.remove(i--);
      }
    }
  }



  /* (non-Javadoc)
   * @see peernet.core.Control#execute()
   */
  @Override
  public boolean execute()
  {
    // TODO Auto-generated method stub
    return false;
  }
}
