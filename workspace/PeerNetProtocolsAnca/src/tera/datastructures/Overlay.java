/**
 * 
 */
package tera.datastructures;

import java.util.Hashtable;

/**
 * @author anca
 *
 */
public class Overlay
{
  private static Overlay _instance = null;
  
  private Hashtable<String,Integer> _overlays;
  /**
   * 
   */
  protected Overlay()
  {
    _overlays = new Hashtable<String,Integer>();
  }
  
  public static Overlay instance() {
    if (_instance == null) 
      _instance = new Overlay();
    return _instance;
  }
  
  public void inc(String oid) {
    if (_overlays.containsKey(oid))
      _overlays.put(oid,_overlays.get(oid)+1);
    else
      _overlays.put(oid,1);
  }
  
  public void dec(String oid) {
    if (_overlays.containsKey(oid)) {
      _overlays.put(oid,_overlays.get(oid)-1);
      if (_overlays.get(oid)==0)
        _overlays.remove(oid);
    }
  }
  
  public int get(String oid) {
    if (_overlays.containsKey(oid))
      return _overlays.get(oid);
    else
      return 0;
  }
  
  public String toString() {
    return "Number of overlays: " + _overlays.size() + " - " + _overlays;
  }
}
