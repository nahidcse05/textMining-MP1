/**
 * 
 */
package analyzer;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class combine_bigram {
	
	Map<String, Integer> dictionary = new HashMap<String, Integer>();
	Map<String, Integer> sortedMap;
	
	public void readcsv(String csvFile)
	{
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\t";
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] word = line.split(cvsSplitBy);
	
				
				//System.out.println(word[0]);
				if(dictionary.containsKey(word[0])) {
					Integer val = (Integer) dictionary.get(word[0]);
					dictionary.put(word[0], val + 1);
					}
				else
					{
						if(word[0]!="" || word[0]!=null)
							dictionary.put(word[0], Integer.parseInt(word[1]));
					}
	 
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		//System.out.println("Done"+smart_system_stop_wordlist.size());
	  
	}
	
	
	
	private void generateCsvFile1(String sFileName)
    {
 	try
 	{
 		
 		
 		FileWriter writer = new FileWriter(sFileName); // for Matlab
 		//FileWriter writer1 = new FileWriter(sFileName+"_stopword.csv");// for StopWord Removal
 		FileWriter writer_top_100 = new FileWriter("./data/stat1/Stopword_top_100_ngram.txt"); // for Matlab
 		FileWriter writer_less_50 = new FileWriter("./data/stat1/less_50_df_ngram.txt"); // for Matlab
 		FileWriter writer_top_50 = new FileWriter("./data/stat1/bottom_50_df_ngram.txt"); // for Matlab
 	 	FileWriter writer_bottom_50 = new FileWriter("./data/stat1/bottom_50_df_ngram.txt"); // for Matlab
 	
	 	   int x= 1;
	 	   int last = sortedMap.size() - 1;
	 	   int bottom = last - 49;
	 	   int y = 1;
	 	   for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			
	 		  
	 		   
	 		  double idf_temp = (double)629921/entry.getValue();
			  double idf = 1 + Math.log10(idf_temp);
	 		  
			  if(entry.getValue() >= 50)
				  writer.append(entry.getKey() +","+entry.getValue()+","+idf+"\n");
			  
			 /* if(y>=1 && y<=50)
				  writer_top_50.append(entry.getKey() +","+entry.getValue()+","+idf+"\n");
	 		   
	 		  if(x>=1 && x<=100)
	 			 writer_top_100.append(entry.getKey() +","+entry.getValue()+","+idf+"\n");
	 		  
	 		  if(entry.getValue() < 50)
	 			 writer_less_50.append(entry.getKey() +","+entry.getValue()+","+idf+"\n");
	 		  else
	 			 writer.append(entry.getKey() +","+entry.getValue()+","+idf+"\n");
	 		  
	 		  if(x>=bottom && x<=last)
	 			 writer_bottom_50.append(entry.getKey() +","+entry.getValue()+","+idf+"\n");*/
	 		  
	 		  //writer1.append(entry.getKey()+","+entry.getValue()+"\n");
	 		   //writer_log.append(Math.log(x)+","+Math.log(entry.getValue())+"\n");
	 		   x++;y++;
	 
			}	    
	 	    
 	  
 	    //generate whatever data you want
  
 	   writer.flush();
 	   writer_top_100.flush();
 	  writer_top_50.flush();
 	   writer_less_50.flush();
 	  writer_bottom_50.flush();
 	 
 	   writer.close();
 	   writer_top_100.close();
 	  writer_top_50.close();
 	   writer_less_50.close();
 	  writer_bottom_50.close();
 	    
 	}
 	catch(IOException e)
 	{
 	     e.printStackTrace();
 	} 
    }
	
	public void sortByComparator() {
		 
		// Convert Map to List
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(dictionary.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
                                           Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
	
	}

	
	public static void main(String[] args) {
		
		
		
	
		
		combine_bigram com = new combine_bigram();
		
		
		/*for(int i=1;i<=4;i++)
        {
			String filename = "./data/stat1/bi_stat_"+i+".txt";
            com.readcsv(filename);
        }*/
		/*for(int i=1;i<=4;i++)
        {
       
            String filename = "./data/stat1/uni_stat"+i+".txt";
            com.readcsv(filename);
        }*/
		
		com.readcsv("./data/stat1/bi_stat_all.txt");
		com.readcsv("./data/stat1/uni_stat_all.txt");
		
		com.sortByComparator();
		com.generateCsvFile1("./data/stat1/combine_unigram_df_new.txt");
		
		
		
		//analyzer.LoadJson("D:/nahid/Medforum/data/json/eHealth/Part2/eHealth/Schizophrenia/ka5am-T142.json");
		
		//analyzer.generateFrequency_for_smart_word_list();
		
		
		//DocAnalyzer analyzer_bigram = new DocAnalyzer(2);
		//analyzer_bigram.LoadDirectory("./data/json/eHealth/Panic_Attack/", ".json");
		
	}

	

}
