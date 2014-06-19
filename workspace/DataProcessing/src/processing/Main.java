package processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

/** 
 * @author anca
 *
 */
public class Main {

	private static final String PROT = "poldercast";
	private static final int FROM = 5;
	
	private static int cycleInterval;
	private static int numOfCycles;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		cycleInterval = Integer.parseInt(args[0]);
		numOfCycles = Integer.parseInt(args[1]);
		
		readData(args);
		processData();
		cleanData();
		
		System.out.println("Done");
	}
	
	/**
	 * @param args 
	 * 
	 */
	private static void readData(String[] args) 
	{
		ReadData.readTopics(args[2]);

		int nrFiles = args.length-3;
		ReadData.readResultFiles(new File[nrFiles], nrFiles, args);
	}

	/**
	 * 
	 */
	private static void processData() {
		logPublishedEvents();
	}

	/**
	 * 
	 */
	private static void cleanData() {
		ReadData.deleteCycleFiles();		
	}

	/**
	 * 
	 */
	private static void checkLostMessages() {
		File file = new File(ReadData.CYCLE_PATH);
		BufferedReader bufferReader = null;
		long start, end;
		start = FROM*cycleInterval;
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
	private static void logPublishedEvents() {
		File file = new File(ReadData.CYCLE_PATH);
		BufferedReader bufferReader = null;
		File hitRatioFile = new File("results/" + PROT + "/hitRatio.dat");
		BufferedWriter hitBuf = null;
		long start = FROM*cycleInterval;
		long end = start + cycleInterval;
		int cycle = 0;
		String line;
		boolean goOn = true;
		
		try {
			hitBuf = new BufferedWriter(new FileWriter(hitRatioFile));
			while (goOn)
			{
				cycle++;
				int received = 0, sent = 0;
				HashMap<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>> publishedEvents = new HashMap<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>>();
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

						if (time >= start && time <= end && hops == 0) //the message was send within the current cycle
						{
							if (tag.equals("s")) System.out.println("error: label should be r");
							
							if (publishedEvents.containsKey(msgId)) System.out.println("error: duplicate message");
							AbstractMap.SimpleEntry<Integer, Long> pair = new SimpleEntry<Integer,Long>(nodeId, topicId);
							Vector<AbstractMap.SimpleEntry<Integer, Long>> v = new Vector<AbstractMap.SimpleEntry<Integer, Long>>();
							v.add(pair);
							publishedEvents.put(msgId, v);
							received++;
						}					
					}
				}
				long numOfMessagesPerCycle = received;
				
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
						if (tag.equals("s")) i++; //this is the path; we don't care, is just for debug
						
						if (publishedEvents.containsKey(msgId) && hops != 0)
						{
							if (tag.equals("s"))
							{
								sent++;
							}
							else{
								received++;
								Vector<AbstractMap.SimpleEntry<Integer, Long>> v = publishedEvents.get(msgId);
								AbstractMap.SimpleEntry<Integer, Long> pair = new SimpleEntry<Integer,Long>(nodeId, topicId);
								v.add(pair);
								publishedEvents.put(msgId, v);		
							}								
						}						
					}
				}
				bufferReader.close();
				
				if(received != sent + numOfMessagesPerCycle)
					System.out.println("lost messages "+ (sent+numOfMessagesPerCycle-received));
				System.out.println(numOfMessagesPerCycle + " published events per cycle");
				System.out.println(sent + numOfMessagesPerCycle + " total messages sent per cycle");
				
				processHitRatio(publishedEvents, numOfMessagesPerCycle, cycle, hitBuf);
				
				
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
			try {
				hitBuf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param publishedEvents
	 * @param numOfMessagesPerCycle
	 * @param cycle
	 * @param hitBuf 
	 */
	private static void processHitRatio(HashMap<Long, Vector<SimpleEntry<Integer, Long>>> publishedEvents, long numOfMessagesPerCycle, int cycle, BufferedWriter hitBuf)
	{
		double completeDisseminations = 0;
		double totalHits = 0;
		
		
		
		Set<Entry<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>>> set = publishedEvents.entrySet();
		Iterator<Entry<Long, Vector<SimpleEntry<Integer, Long>>>> it = set.iterator();
		while (it.hasNext())
		{
			Entry<Long,Vector<AbstractMap.SimpleEntry<Integer, Long>>> entry = it.next();
			Vector<AbstractMap.SimpleEntry<Integer, Long>> values = entry.getValue();
			
			Vector<Integer> hits = new Vector<Integer>();
			for (AbstractMap.SimpleEntry<Integer, Long> value : values)
			{
				int nodeId = value.getKey();
				if (!hits.contains(nodeId))
					hits.add(nodeId);
			}
			
			long topic = values.get(0).getValue();
			int numOfInterests = ReadData.getSubscribers().get(topic).size();
			int numOfHits = hits.size();
			double percentageOfHits = (double) numOfHits/numOfInterests*100;
			if (percentageOfHits == 100)
			{
				completeDisseminations++;
			}
			totalHits += percentageOfHits;
		}
		if (numOfMessagesPerCycle != 0)
		{
			totalHits = totalHits/numOfMessagesPerCycle;
			completeDisseminations = completeDisseminations/numOfMessagesPerCycle*100;
		}
		
		double missRatio = 100-totalHits;
		double missDis = 100 - completeDisseminations;
		System.out.println(cycle + " " + missRatio + " " + missDis);
		
		try {
			hitBuf.write(cycle + " " + missRatio + " " + missDis);
			hitBuf.newLine();
			hitBuf.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
				
	}
}
