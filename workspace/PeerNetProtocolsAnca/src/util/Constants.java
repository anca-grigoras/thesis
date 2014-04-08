package util;

public class Constants
{
  /**
   *  View size of the protocol.
   */
  public static final String PAR_VIEWLEN= "view";

  /**
   * Number of items to gossip in each cycle.
   * 
   * It can trivially be 0, but then it makes sense only if this protocol's
   * view is fed exclusively by its underlying protocol.
   */
  public static final String PAR_GOSSIPLEN = "gossip";
  
  /** 
   * Specifies whether the view of underlying linkables
   * should be considered to send links to other peers.
   */
  public static final String PAR_SEND_LINKABLE_ITEMS = "sendLinkableItems";
  
  public static final String PAR_DATA = "data";
  public static final String PAR_PROT = "protocol";
  public static final String PAR_FILE = "file";
  
  public static final String PAR_SW = "k";
  public static final String PAR_THRESHOLD = "threshold";
  public static final String PAR_MAX_HOPS = "hops";
  
  public static final String PAR_M = "m";
}
