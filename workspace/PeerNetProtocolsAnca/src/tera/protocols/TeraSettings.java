/**
 * 
 */
package tera.protocols;

import peernet.config.Configuration;
import peernet.core.Protocol;
import peernet.core.ProtocolSettings;

/**
 * @author anca
 *
 */
public class TeraSettings extends ProtocolSettings
{
  private static final String PAR_GOSSIPLEN = "d";
  private static final String PAR_SEARCHNUM = "searchNum";
  private static final String PAR_TTL = "ttl";
  private static final String PAR_APT = "aptSize";
  private static final String PAR_VIEW = "view";
  
  public int gossipLen;
  public int searchNum;
  public int ttl;
  public int aptSize;
  public int viewSize;
  public int nSubUpdate;
  /**
   * @param prefix
   */
  public TeraSettings(String prefix)
  {
    super(prefix);
    prefix = prefix.replace("."+Protocol.PAR_SETTINGS, "");
    /**
     * Default value is -1, so that the check "if (gossipLen==0)" doesn't block
     * a generalized nextCycle() for protocol instances for which this parameter
     * is irrelevant (like in VicinityCoverage).
     */
    viewSize = Configuration.getInt(prefix+"."+PAR_VIEW);
    gossipLen = Configuration.getInt(prefix+"."+ PAR_GOSSIPLEN, -1);
    searchNum = Configuration.getInt(prefix+"."+PAR_SEARCHNUM);
    ttl = Configuration.getInt(prefix+"."+PAR_TTL);
    aptSize = Configuration.getInt(prefix+"."+PAR_APT);
  }
}
