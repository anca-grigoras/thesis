/**
 * 
 */
package processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author anca
 *
 */
public class ReadData {

	public static final String CYCLE_PATH = "collectedResult.txt";
	
	private static HashMap<Integer,Vector<Long>> topicRepo = new HashMap<Integer,Vector<Long>>(); //nodeId, list of topics
	private static HashMap<Long,Vector<Integer>> subscribers = new HashMap<Long,Vector<Integer>>(); //topic, list of nodes

	/**
	 * @param string
	 */
	public static void readTopics(String string) {
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
	 * @return the subscribers
	 */
	public static HashMap<Long, Vector<Integer>> getSubscribers() {
		return subscribers;
	}
	
	/**
	 * @return the topicRepo
	 */
	public static HashMap<Integer, Vector<Long>> getTopicRepo() {
		return topicRepo;
	}
	
	/**
	 * @param files
	 * @param outbuf
	 * @param args 
	 * @param nrFiles 
	 */
	public static void readResultFiles(File[] files, int nrFiles, String[] args) {
		
		File outfile = new File(CYCLE_PATH);
		BufferedReader buf = null;
		BufferedWriter outbuf = null;
		try {
			outbuf = new BufferedWriter(new FileWriter(outfile));
			String line = "";
			for (int i = 0; i< nrFiles; i++)
			{
				files[i] = new File(args[i+3]);
				buf = new BufferedReader(new FileReader(files[i]));
				
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
		finally {
			//Close the BufferedReader
			try {
				if (buf != null)
					buf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			try {
				if (outbuf != null)
					outbuf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 */
	public static void deleteCycleFiles() {
		System.out.println("Deleting files...");
		try{
			File file = new File(CYCLE_PATH);
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
