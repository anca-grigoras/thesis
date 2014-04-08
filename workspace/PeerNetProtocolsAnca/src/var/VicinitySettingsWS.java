/*
 * Created on Aug 1, 2010 by Spyros Voulgaris
 */
package var;

import gossip.comparator.DescriptorComparator;
import gossip.descriptor.DescriptorAge;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import peeremu.core.CommonState;
import peeremu.core.Descriptor;





public class VicinitySettingsWS extends VicinitySettings
{
  TargetOverlayComparator proxCmp;
  Comparator<Descriptor> gossipCmp;

  public VicinitySettingsWS(String prefix)
  {
    super(prefix);

    gossipCmp = new NewestFirst();
    proxCmp = new TargetOverlayComparator(prefix);
    duplCmp = new OldestFirst();
  }



  @Override
  public Vector<Descriptor> selectProximal(Descriptor ref,
      Vector<Descriptor> pool, Vector<Descriptor> exclude, int howmany)
  {
    Vector<Descriptor> selected = new Vector<Descriptor>(howmany+1);

    // If we can fit the whole pool, no need to sort it.
    if (howmany<pool.size())
    {
      Collections.shuffle(pool, CommonState.r);
      proxCmp.setReference(ref);
      Collections.sort(pool, proxCmp);
    }

    // And now select the first 'howmany' from the sorted list,
    // excluding duplicates and descriptors in the 'exclude' list.
    for (Descriptor d: pool)
    {
      if (ref.equals(d))
        continue;

      assert (!selected.contains(d)): "selectProximal() expects a duplicate-free Descriptor list!!";

      if (exclude!=null && exclude.contains(d))
        continue;

      try {selected.add((Descriptor) d.clone());}
      catch (CloneNotSupportedException e) {e.printStackTrace();}

      if (--howmany==0)
        break;
    }
    return selected;
  }



  @Override
  public Descriptor selectToGossip(Vector<Descriptor> pool, boolean remove)
  {
    if (pool.size() == 0)
      return null;

    Collections.shuffle(pool, CommonState.r);
    Collections.sort(pool, gossipCmp);

    if (remove)
      return pool.remove(pool.size()-1);
    else
      return pool.lastElement();
    
//    int r = CommonState.r.nextInt(pool.size());
//    return pool.remove(r);
  }





  public class TargetOverlayComparator implements DescriptorComparator
  {
    // TargetOverlay overlay = TargetOverlay.instance();
    int refID = -1;

    public TargetOverlayComparator(String prefix)
    {
    }

    @Override
    public void setReference(Descriptor ref)
    {
      refID = (int)ref.getID();
    }

    @Override
    public int compare(Descriptor a, Descriptor b)
    {
      assert refID != -1;
      int distA = TargetOverlay.distance(refID, (int)a.getID());
      int distB = TargetOverlay.distance(refID, (int)b.getID());
      if (distA==distB)
        return 0;
      // Else, return -1 if dist1 is lower, and +1 if dist1 is higher
      return distA-distB;
    }
  }

  public class NewestFirst implements Comparator<Descriptor>
  {
    @Override
    public int compare(Descriptor a, Descriptor b)
    {
      return ((DescriptorAge)a).getAge()-((DescriptorAge)b).getAge();
    }
  }

  
  public class OldestFirst implements Comparator<Descriptor>
  {
    @Override
    public int compare(Descriptor a, Descriptor b)
    {
      return - ( ((DescriptorAge)a).getAge()-((DescriptorAge)b).getAge() );
    }
  }
}
