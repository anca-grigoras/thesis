/**
 * 
 */
package poldercast.protocols;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import peernet.config.Configuration;
import peernet.core.Protocol;
import peernet.core.ProtocolSettings;

/**
 * @author anca
 *
 */
public class DisseminationSettings extends ProtocolSettings
{
  private static final String PAR_LOG_PATH = "log";
  private static String PAR_RT = "routing";
  private static String PAR_FANOUT = "fanout";
  int rtPid;
  protected int fanout;
  public File generalLogPath;
  public BufferedWriter log;
  /**
   * @param prefix
   */
  public DisseminationSettings(String prefix)
  {
    super(prefix);
    prefix = prefix.replace("."+Protocol.PAR_SETTINGS, "");
    rtPid = Configuration.getPid(prefix+"."+PAR_RT);
    fanout = Configuration.getInt(prefix+"."+PAR_FANOUT);
    generalLogPath = new File(Configuration.getString(prefix + "." + PAR_LOG_PATH) + Configuration.getString("LOGFILE_PREFIX") + ".txt");    
    
    try 
    {
      String firstLine = "# nodeId\tmsgId\ttopicId\thops\ttime";  
      log = new BufferedWriter(new FileWriter(generalLogPath));
      log.write(firstLine);
      log.newLine();
      log.flush();
      System.out.println("Created file " + generalLogPath.getName());
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }
  }
}
