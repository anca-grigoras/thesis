/*
 * Created on Jun 6, 2011 by Spyros Voulgaris
 *
 */
package netsize;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Descriptor;
import peernet.core.Linkable;
import peernet.core.Node;
import peernet.core.Protocol;
import peernet.transport.Address;



public class NetSize extends Protocol
{
  private static final String PAR_ALGORITHM = "algorithm";

  public int uploadBytes = 0;
  public int downloadBytes = 0;

  private Algorithm algorithm = null;



  public NetSize(String prefix)
  {
    super(prefix);
    String algorithmName = Configuration.getString(prefix+"."+PAR_ALGORITHM);
    algorithm = (Algorithm) Configuration.getInstance(algorithmName);
  }



  public void nextCycle(Node selfNode, int pid)
  {
    int linkableID = settings.getLinkable(pid);
    Linkable linkable = (Linkable) selfNode.getProtocol(linkableID);

    // pick a random neighbor of my linkable protocol
    if (linkable.degree() == 0)
      return;  // I can't get hold of any neighbor...

    int r = CommonState.r.nextInt(linkable.degree());
    Descriptor destDescriptor = linkable.getNeighbor(r);

    // get my own descriptor
    Descriptor selfDescriptor = createDescriptor();

    // compile event object as an object array: [MESSAGE, SIZE]
    Object message = algorithm.prepareMessage();
    int messageSize = algorithm.getMessageSize();
    Object[] messageWithSize = {message, messageSize};

    // Update upload counter
    uploadBytes += messageSize;

    // Send event object to the random peer
    send(destDescriptor.address, pid, messageWithSize);
  }



  @Override
  public void processEvent(Address src, Node node, int pid, Object eventObject)
  {
    // Extract the MESSAGE and its SIZE from the received event object.
    Object[] messageWithSize = (Object[]) eventObject;
    Object message = messageWithSize[0];
    Integer messageSize = (Integer)messageWithSize[1];

    // Update download counter
    downloadBytes += messageSize;

    // Deliver message to the algorithm
    algorithm.deliverMessage(message);
  }



  public Algorithm getAlgorithm()
  {
    return algorithm;
  }



  public Object clone()
  {
    NetSize ns = (NetSize) super.clone();
    ns.algorithm = (Algorithm)algorithm.clone();
    
    return ns;
  }
}
