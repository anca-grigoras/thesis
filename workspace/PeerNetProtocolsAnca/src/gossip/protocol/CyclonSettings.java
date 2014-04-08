/*
 * Created on Nov 26, 2004 by Spyros Voulgaris
 */
package gossip.protocol;

import peernet.config.Configuration;
import peernet.core.Protocol;
import peernet.core.ProtocolSettings;





/**
 * @author Spyros Voulgaris
 * 
 */
public class CyclonSettings extends ProtocolSettings
{
  /**
   * View size of the protocol.
   */
  private static final String PAR_VIEWLEN = "view";
  /**
   * Number of items to gossip in each cycle.
   * 
   * It can trivially be 0, but then it makes sense only if this protocol's view
   * is fed exclusively by its underlying protocol.
   */
  private static final String PAR_GOSSIPLEN = "gossip";
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
    gossipLen = Configuration.getInt(prefix+"."+PAR_GOSSIPLEN, -1);
    /**
     * Default value is 0, so that the 'items' array is (trivially) initialized
     * to a vector of size 0, instead of raising an exception for protocols for
     * which this parameter is irrelevant (like in VicinityCoverage).
     */
    viewLen = Configuration.getInt(prefix+"."+PAR_VIEWLEN, 0);
    
  }
}
