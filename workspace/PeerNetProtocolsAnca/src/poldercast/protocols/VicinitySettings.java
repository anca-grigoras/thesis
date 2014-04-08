/*
 * Created on Dec 7, 2004 by Spyros Voulgaris
 *
 */
package poldercast.protocols;

import java.util.Comparator;

import peernet.config.Configuration;
import peernet.core.Descriptor;
import peernet.core.Protocol;
import peernet.core.ProtocolSettings;

public class VicinitySettings extends ProtocolSettings
{
 
  private static String PAR_VIEWLEN = "view";
  private static String PAR_GOSSIPLEN = "gossip";
  private static String PAR_SEND_LINKABLE_ITEMS = "sendLinkableItems";
  
  // Protocol specific parameters
  public int viewLen;
  public int gossipLen;

  public Comparator<Descriptor> duplCmp;

   // Protocol specific parameters
  boolean sendLinkableItems;


  public VicinitySettings(String prefix)
  {
    super(prefix);
 // cut off the last component of prefix
    // e.g., protocol.vicinity.settings -> protocol.vicinity
    prefix = prefix.replace("."+Protocol.PAR_SETTINGS, "");
    //prefix = prefix.substring(0, prefix.lastIndexOf("."));
    
    /**
     * Default value is -1, so that the check "if (gossipLen==0)"
     * doesn't block a generalized nextCycle() for protocol instances
     * for which this parameter is irrelevant (like in VicinityCoverage).
     */
    gossipLen = Configuration.getInt(prefix+"."+ PAR_GOSSIPLEN, -1);

    /**
     * Default value is 0, so that the 'items' array is (trivially)
     * initialized to a vector of size 0, instead of raising an
     * exception for protocols for which this parameter is
     * irrelevant (like in VicinityCoverage).
     */
    viewLen = Configuration.getInt(prefix+"."+ PAR_VIEWLEN, 0);

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
