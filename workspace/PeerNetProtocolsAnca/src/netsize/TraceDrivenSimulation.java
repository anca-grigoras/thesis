/*
 * Created on Dec 4, 2012 by Spyros Voulgaris
 *
 */
package netsize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Network;
import peernet.core.Node;
import peernet.core.Protocol;
import peernet.transport.Address;
import peernet.transport.AddressSim;

public class TraceDrivenSimulation extends Protocol
{
  private static final String PAR_ALGORITHM = "algorithm";
  private static final String PAR_TRACE = "trace";

  private static ArrayList<Integer>[][] communication;

  private Algorithm algorithm;

  public TraceDrivenSimulation(String prefix) throws IOException
  {
    super(prefix);
    String traceFile = Configuration.getString(prefix+"."+PAR_TRACE);
    BufferedReader in = new BufferedReader(new FileReader(traceFile));
    String line;
    line = in.readLine();
    StringTokenizer t = new StringTokenizer(line, "\t");
    int nodes = Integer.parseInt(t.nextToken());
    int rounds = Integer.parseInt(t.nextToken());
    communication = new ArrayList[nodes][rounds];

    while ( (line = in.readLine()) != null )
    {
      StringTokenizer tok = new StringTokenizer(line, "\t");
      int time = Integer.parseInt(tok.nextToken());
      int round = Integer.parseInt(tok.nextToken());
      int sender = Integer.parseInt(tok.nextToken());
      int receiver = Integer.parseInt(tok.nextToken());
      if (communication[sender][round] == null)
        communication[sender][round] = new ArrayList<Integer>();
      communication[sender][round].add(receiver);
    }

    String algorithmName = Configuration.getString(prefix+"."+PAR_ALGORITHM);
    algorithm = (Algorithm) Configuration.getInstance(algorithmName);
  }



  @Override
  public void processEvent(Address src, Node node, int pid, Object event)
  {
    // TODO Auto-generated method stub
  }



  @Override
  public void nextCycle(Node node, int protocolID)
  {
    long round = CommonState.getTime();
    ArrayList<Integer> peerIds = communication[(int) node.getID()][(int) round];

    Object payload = algorithm.prepareMessage();

    for (int peerId: peerIds)
    {
      Node peer = Network.getByID(peerId);
      Address addr = new AddressSim(peer);
      send(addr, protocolID, payload);
    }
  }
}
