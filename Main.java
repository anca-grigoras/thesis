import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;


/**
 * @author anca
 *
 */
public class Main {

	private static final String PROT = "poldercast";
	private static int cycleInterval;
	private static final int from = 5;
	private static int numOfCycles;
	private static final String cyclePath = "collectedResult.txt";

	private static HashMap<Integer,Vector<Long>> topicRepo = new HashMap<Integer,Vector<Long>>(); //nodeId, list of topics
	private static HashMap<Long,Vector<Integer>> subscribers = new HashMap<Long,Vector<Integer>>(); //topic, list of nodes

	/**
	 * 
	 */
	public Main() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int nrFiles = args.length-3;
		File[] files = new File[nrFiles];
		BufferedWriter outbuf = null;
		numOfCycles = Integer.parseInt(args[0]);
		cycleInterval = Integer.parseInt(args[1]);
		readTopics(args[2]);
		//printSubscribers();
		readResultFiles(files, outbuf, nrFiles, args);

		//create the file for hit ratio
		File hitRatioFile = new File(PROT + ".dat");
		BufferedWriter hitBuf = null;
		try {
			hitBuf = new BufferedWriter(new FileWriter(hitRatioFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		logPublishedEvents(hitBuf);
		//checkLostMessages();
		//processControl();
		

		try {
			hitBuf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		deleteCycleFiles();		
		System.out.println("Done");
	}
	/**
	 * 
	 */
	private static void checkLostMessages() {
		File file = new File(cyclePath);
		BufferedReader bufferReader = null;
		long start, end;
		start = from*cycleInterval;
		end = start + cycleInterval;
		int cycle = 0;
		Vector<Long> msgs = new Vector<Long>();
		try {
			
			String line;
			boolean goOn = true;
			while (goOn)
			{
				cycle++;
				int received = 0, sent = 0;
				bufferReader = new BufferedReader(new FileReader(file));
				while ((line = bufferReader.readLine()) != null) {
					
					String[] tokens = line.split("\t");
					for (int i = 0; i < tokens.length; i++)
					{
						int nodeId =  Integer.parseInt(tokens[i]);
						long msgId = Long.parseLong(tokens[++i]);
						long time = Long.parseLong(tokens[++i]);
						String label = tokens[++i];
						//if (time >= start && time <= end)
							if (label.equals("r")) received++;
							else {
								sent++;
								msgs.add(msgId);
							}
					}					
				}
				
				bufferReader.close();
				bufferReader = new BufferedReader(new FileReader(file));
				while ((line = bufferReader.readLine()) != null) {
					String[] tokens = line.split("\t");
					for (int i = 0; i < tokens.length; i++)
					{
						int nodeId =  Integer.parseInt(tokens[i]);
						long msgId = Long.parseLong(tokens[++i]);
						long time = Long.parseLong(tokens[++i]);
						String label = tokens[++i];
						//if (time >= start && time <= end)
							if (label.equals("r")) 
							{
								if (!msgs.contains(msgId))
								{
									System.out.println("message received but not sent");
									System.out.println("\t"+msgId);
								}
								msgs.remove(msgId);
							}
					}
				}
				for (int i = 0; i< msgs.size(); i++)
					System.out.println(msgs.get(i));
				if (received != sent)
					System.out.println("Cycle " + cycle + " : lost msg " + (sent-received));
				bufferReader.close();
				start += cycleInterval;
				end += cycleInterval;
				//if (cycle == numOfCycles)
					goOn = false;
			}			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

		
	}

	/**
	 * @param i 
	 * @param hitBuf 
	 * 
	 */
	private static void logPublishedEvents(BufferedWriter hitBuf) {
		File file = new File(cyclePath);
		BufferedReader bufferReader = null;
		long start, end;
		start = from*cycleInterval;
		end = start + cycleInterval;
		//end = start + numOfCycles*cycleInterval;
		int cycle = 0;
		
		try {
			
			String line;
			boolean goOn = true;
			while (goOn)
			{
				cycle++;
				int received = 0, sent = 0;
				double completeDisseminations = 0;
				//System.out.println(cycle);
				HashMap<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>> publishedEvents = new HashMap<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>>();
				HashMap<Long,Integer> msgLost = new HashMap<Long, Integer>();
				bufferReader = new BufferedReader(new FileReader(file));
				while ((line = bufferReader.readLine()) != null) {
					
					String[] tokens = line.split("\t");
					for (int i = 0; i < tokens.length; i++)
					{
						int nodeId =  Integer.parseInt(tokens[i]);
						long msgId = Long.parseLong(tokens[++i]);
						long topicId = Long.parseLong(tokens[++i]);
						int hops = Integer.parseInt(tokens[++i]);
						long time = Long.parseLong(tokens[++i]);
						String tag = tokens[++i]; //s or r
						if (tag.equals("s")) i++;
						//i++; //this is the path; we don't care, is just for debug

						if (time >= start && time < end && hops == 0)
						{
							if (tag.equals("s")) System.out.println("error: label should be r");
							if (publishedEvents.containsKey(msgId)) System.out.println("error: duplicate message");
							AbstractMap.SimpleEntry<Integer, Long> pair = new SimpleEntry<Integer,Long>(nodeId, topicId);
							Vector<AbstractMap.SimpleEntry<Integer, Long>> v = new Vector<AbstractMap.SimpleEntry<Integer, Long>>();
							v.add(pair);
							publishedEvents.put(msgId, v);
							msgLost.put(msgId, 0);
							received++;
						}					
					}
				}
				long numOfMessagesPerCycle = received;
				System.out.println(numOfMessagesPerCycle + " published events");
				bufferReader.close();
				bufferReader = new BufferedReader(new FileReader(file));
				while ((line = bufferReader.readLine()) != null) {
					String[] tokens = line.split("\t");
					for (int i = 0; i < tokens.length; i++)
					{
						int nodeId =  Integer.parseInt(tokens[i]);
						long msgId = Long.parseLong(tokens[++i]);
						long topicId = Long.parseLong(tokens[++i]);
						int hops = Integer.parseInt(tokens[++i]);
						long time = Long.parseLong(tokens[++i]);
						String tag = tokens[++i];
						String p = "";
						if (tag.equals("s")) i++; //this is the path; we don't care, is just for debug
						
						if (publishedEvents.containsKey(msgId) && hops != 0)
						{
							if (tag.equals("s"))
							{
								sent++;
							        msgLost.put(msgId, msgLost.get(msgId)+1);
							}
							else{
								received++;
								msgLost.put(msgId, msgLost.get(msgId)-1);
								Vector<AbstractMap.SimpleEntry<Integer, Long>> v = publishedEvents.get(msgId);
								AbstractMap.SimpleEntry<Integer, Long> pair = new SimpleEntry<Integer,Long>(nodeId, topicId);
								v.add(pair);
								publishedEvents.put(msgId, v);		
							}								
						}						
					}
				}
				bufferReader.close();
				
				if(received != sent+numOfMessagesPerCycle)
					System.out.println("lost messages "+ (sent+numOfMessagesPerCycle-received));
				double totalHits = 0;
				Set<Entry<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>>> set = publishedEvents.entrySet();
				Iterator<Entry<Long, Vector<SimpleEntry<Integer, Long>>>> it = set.iterator();
				while (it.hasNext())
				{
					Entry<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>> entry = it.next();
					Vector<AbstractMap.SimpleEntry<Integer, Long>> values = entry.getValue();
					long topic = values.get(0).getValue();
					Vector<Integer> hits = new Vector<Integer>();
					for (AbstractMap.SimpleEntry<Integer, Long> value : values)
					{
						int nodeId = value.getKey();
						if (!hits.contains(nodeId))
							hits.add(nodeId);
					}
					int interests = subscribers.get(topic).size();
					int numOfHits = hits.size();
					double percentage = (double) numOfHits/interests*100;
					if (percentage == 100)
						completeDisseminations++;
					totalHits += percentage;
				}
				if (numOfMessagesPerCycle != 0)
					totalHits = totalHits/numOfMessagesPerCycle;
				double missRatio = 100-totalHits;
				completeDisseminations = completeDisseminations/numOfMessagesPerCycle*100;
				double missDis = 100 - completeDisseminations;
				System.out.println(cycle + " " + missRatio + " " + missDis);
				/*Set<Entry<Long,Integer>> set = msgLost.entrySet();
				Iterator<Entry<Long,Integer>> it = set.iterator();
				while (it.hasNext())
				{
					Entry<Long, Integer> entry = it.next();
					long id = entry.getKey();
					int lost = entry.getValue();
					if (lost > 0)
						System.out.println("msgId= " + id + " " + lost);
				}*/
				hitBuf.write(cycle + " " + missRatio + " " + missDis);
				hitBuf.newLine();
				hitBuf.flush();
				
				start += cycleInterval;
				end += cycleInterval;
				if (cycle == numOfCycles)
					goOn = false;
			}			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bufferReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param entryHit
	 */
	private static void eliminateDuplicates(Vector<Integer> v) {
		for(int i=0;i<v.size();i++)
        {
            for(int j=0;j<v.size();j++)
            {
                    if(i!=j)
                    {
                        if(v.elementAt(i).equals(v.elementAt(j)))
                        {
                        v.removeElementAt(j);
                        }
                    }
            }
        }

		
	}

	/**
	 * 
	 */
	private static void processControl() {
		// TODO Auto-generated method stub

	}



	/**
	 * @param string
	 */
	private static void readTopics(String string) {
		//read the topics file
		File topicsFile = new File(string);
		BufferedReader topicsBuf = null;
		Vector<Long> nodeSubscriptionList;

		try {
			//Construct the BufferedReader object
			topicsBuf = new BufferedReader(new FileReader(topicsFile));

			String line = null;
			int key= 0;
			while ((line = topicsBuf.readLine()) != null) {
				//Process the data, here we just print it out
				nodeSubscriptionList = new Vector<Long>();
				String[] tokens = line.split(",");   
				for (int i = 0; i < tokens.length; i++)
				{
					Long topic =  Long.parseLong(tokens[i]);
					nodeSubscriptionList.add(topic);
					addSubscriber(topic, key);
				}
				topicRepo.put(key, nodeSubscriptionList);
				key++;
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			//Close the BufferedReader
			try {
				if (topicsBuf != null)
					topicsBuf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}


	}

	/**
	 * @param topic
	 * @param key
	 */
	private static void addSubscriber(Long topic, int key) {
		if (subscribers.containsKey(topic))
			subscribers.get(topic).add(key);
		else
		{
			Vector<Integer> v = new Vector<Integer>();
			v.add(key);
			subscribers.put(topic, v);
		}

	}

	/**
	 * prints the subscribers for each topic
	 */
	public static void printSubscribers()
	{
		Set<Entry<Long,Vector<Integer>>> set = subscribers.entrySet();
		Iterator<Entry<Long,Vector<Integer>>> it = set.iterator();
		while(it.hasNext())
		{
			Entry<Long,Vector<Integer>> entry = it.next();
			System.out.print("< " + entry.getKey() + "> ");
			for (int i = 0; i< entry.getValue().size(); i++)
				System.out.print(entry.getValue().get(i) + " ");
			System.out.println();
		}
	}



	/**
	 * @param files
	 * @param outbuf
	 * @param args 
	 * @param nrFiles 
	 */
	private static void readResultFiles(File[] files, BufferedWriter outbuf, int nrFiles, String[] args) {
		
		File outfile = new File(cyclePath);
		try {
			outbuf = new BufferedWriter(new FileWriter(outfile));
			String line = "";
			for (int i = 0; i< nrFiles; i++)
			{
				files[i] = new File(args[i+3]);
				BufferedReader buf = new BufferedReader(new FileReader(files[i]));
				
				while ((line = buf.readLine()) != null) {
					if (line.startsWith("#"))
						continue;					
					outbuf.write(line);
					outbuf.newLine();
					outbuf.flush();
				}
			} 
		}catch (IOException e) {
			e.printStackTrace();
		}
		/*finally {
			//Close the BufferedReader
			try {
				if (buf != null)
					buf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	*/
	}

	/**
	 * 
	 */
	private static void deleteCycleFiles() {
		System.out.println("Deleting files...");
		try{
			File file = new File(cyclePath);
			if(file.delete()){
				System.out.println(file.getName() + " is deleted!");
			}else{
				System.out.println("Delete operation is failed.");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
