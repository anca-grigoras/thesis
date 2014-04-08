/*
 * Created on Nov 26, 2004 by Spyros Voulgaris
 */
package vitis.protocols;

import peernet.config.Configuration;
import peernet.core.Protocol;
import peernet.core.ProtocolSettings;

/**
 * @author Spyros Voulgaris
 * 
 */
public class CyclonSettings extends ProtocolSettings
{
  private static String PAR_VIEWLEN = "view";
  private static String PAR_GOSSIPLEN = "gossip";
  
  public int viewLen;
  public int gossipLen;

  public CyclonSettings(String prefix)
  {
    super(prefix);

    // First remove the ".settings" from the prefix
    prefix = prefix.replace("."+Protocol.PAR_SETTINGS, "");

    /**
     * Default value is -1, so that the check "if (gossipLen==0)" doesn't block
     * a generalized nextCycle() for protocol instances for which this parameter
     * is irrelevant (like in VicinityCoverage).
     */
    gossipLen = Configuration.getInt(prefix+"."+ PAR_GOSSIPLEN, -1);
    /**
     * Default value is 0, so that the 'items' array is (trivially) initialized
     * to a vector of size 0, instead of raising an exception for protocols for
     * which this parameter is irrelevant (like in VicinityCoverage).
     */
    viewLen = Configuration.getInt(prefix+"."+ PAR_VIEWLEN, 0);
  }
}
