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
public class TManSettings extends ProtocolSettings
{
  private static final String PAR_VIEWSIZE = "viewSize";
  private static final String PAR_SMALLWORLD_LINKS = "small-world-links";
 
  //private static final String PAR_HOPS = "hops";
  private static final String PAR_IDLENGTH = "idLength";
  private static final String PAR_SEND_LINKABLE_ITEMS = "sendLinkableItems";
  
  boolean sendLinkableItems;
  
  public final int viewSize;
  public final int numOfSmallWorldLinks;
  
  //public final int maxHops;
  public final int idLength;
  public final int fingerDistanceSeed;
  public final BigInteger maxN;
  /**
   * @param prefix
   */
  public TManSettings(String prefix)
  {
    super(prefix);
    prefix = prefix.replace("."+Protocol.PAR_SETTINGS, "");

    /**
     * Default value is 0, so that the 'items' array is (trivially) initialized
     * to a vector of size 0, instead of raising an exception for protocols for
     * which this parameter is irrelevant (like in VicinityCoverage).
     */
    viewSize = Configuration.getInt(prefix+"."+PAR_VIEWSIZE, 0);
    numOfSmallWorldLinks = Configuration.getInt(prefix+"."+PAR_SMALLWORLD_LINKS, 0);
    //maxHops = Configuration.getInt(prefix+"."+PAR_HOPS, 0);
    idLength = Configuration.getInt(prefix+"."+PAR_IDLENGTH,0);
    maxN = (new BigInteger(2 + "")).pow(idLength);
    fingerDistanceSeed = CommonState.r.nextInt();
    
    sendLinkableItems = Configuration.getBoolean(prefix+"."+PAR_SEND_LINKABLE_ITEMS);
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
