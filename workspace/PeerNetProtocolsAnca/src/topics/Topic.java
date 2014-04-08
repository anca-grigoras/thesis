package topics;

import java.io.Serializable;

/**
 * 
 * @author anca
 * 
 */

public class Topic implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  long id;
  int age;
 
  public Topic(long id)
  {
    this.id = id;
    age = 0;
  }

  /**
   * @return the id
   */
  public long getId()
  {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(long id)
  {
    this.id = id;
  }

  public int getAge()
  {
    return age;
  }
  
  public void incAge()
  {
    age++;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime*result+(int) (id^(id>>>32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this==obj)
      return true;
    if (obj==null)
      return false;
    if (getClass()!=obj.getClass())
      return false;
    Topic other = (Topic) obj;
    if (id!=other.id)
      return false;
    return true;
  }
 
}
