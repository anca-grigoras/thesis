/**
 * 
 */
package vitis.protocols;

import peernet.config.Configuration;
import peernet.core.Protocol;
import peernet.core.ProtocolSettings;

/**
 * @author anca
 *
 */
public class DisseminationSettings extends ProtocolSettings
{
  private static String PAR_RT = "routing";
  int rtPid;

  /**
   * @param prefix
   */
  public DisseminationSettings(String prefix)
  {
    super(prefix);
    prefix = prefix.replace("."+Protocol.PAR_SETTINGS, "");
    rtPid = Configuration.getPid(prefix+"."+PAR_RT);
  }
}
