/**
 * 
 */
package vitis.protocols;

import java.math.BigInteger;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Protocol;
import peernet.core.ProtocolSettings;

/**
 * @author anca
 *
 */
public class VitisSettings extends ProtocolSettings
{
  
  private static final String PAR_VIEWSIZE = "viewSize";
  private static final String PAR_THRESHOLD = "threshold";
  private static final String PAR_SEND_LINKABLE_ITEMS = "sendLinkableItems";
  private static final String PAR_D = "d";
  
  public final int viewSize;
  public final int threshold;
  public final int d;
  
  boolean sendLinkableItems;
  /**
   * @param prefix
   */
  public VitisSettings(String prefix)
  {
    super(prefix);
    prefix = prefix.replace("."+Protocol.PAR_SETTINGS, "");

    /**
     * Default value is 0, so that the 'items' array is (trivially) initialized
     * to a vector of size 0, instead of raising an exception for protocols for
     * which this parameter is irrelevant (like in VicinityCoverage).
     */
    viewSize = Configuration.getInt(prefix+"."+PAR_VIEWSIZE, 0);
    threshold = Configuration.getInt(prefix+"."+PAR_THRESHOLD, 0);
    d = Configuration.getInt(prefix + "." + PAR_D, 0);
    
    sendLinkableItems = Configuration.getBoolean(prefix+"."+ PAR_SEND_LINKABLE_ITEMS);
    System.out.println("SendLikableItems is " + sendLinkableItems);
    // If 'sendLinkableItems' is set, make sure I am linked to some protocol.
    if (sendLinkableItems && ! hasLinkable())
    {
      sendLinkableItems = false;
      System.err.println(
          "Warning: Setting 'sendLinkableItems' to false, due to lack of linkables");
    }
  }
}
