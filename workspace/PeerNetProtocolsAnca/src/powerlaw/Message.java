/*
 * Created on Mar 25, 2011 by Spyros Voulgaris
 *
 */
package powerlaw;

import peernet.core.Descriptor;

public class Message
{
  public enum Type
  {
    INVITE,
    ACCEPT,
    REJECT
  };

  Descriptor sender;
  Type type;


  /**
   * @param sender
   * @param type
   */
  public Message(Descriptor sender, Type type)
  {
    super();
    this.sender = sender;
    this.type = type;
  }
}
