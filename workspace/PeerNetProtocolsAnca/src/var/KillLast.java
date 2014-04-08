package var;

import peeremu.core.CommonState;
import peeremu.core.Control;
import peeremu.core.Network;





public class KillLast implements Control
{
  public KillLast(String prefix)
  {
  }

  public final boolean execute()
  {
    System.out.println(CommonState.getTime()+"\n----- KILLING LAST NODE -----\n\n");
    ListTopology.instance.execute();
    Network.remove();
    ListTopology.instance.execute();
    return false;
  }
}
