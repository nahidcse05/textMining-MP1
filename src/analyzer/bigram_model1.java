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

import structures.MyPriorityQueue;
import structures._RankItem;

public class bigram_model1 {
	
	Map<String, Integer> unigram = new HashMap<String, Integer>();
	Map<String, Double> unigram_prob = new HashMap<String, Double>();
	Map<String, Integer> bigram = new HashMap<String, Integer>();
	Map<String, Double> bigram_prob = new HashMap<String, Double>();
	Map<String, Integer> sortedMap;
	double lambda = 0.9; // linear smoothing
	double sigma = 0.1;
	double sum = 0;
	
	
	public void readunigram(String csvFile)
	{
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] word = line.split(cvsSplitBy);
				
				unigram.put(word[0], Integer.parseInt(word[1]));
	
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
	
	
	public void readunigramProbabilty(String csvFile)
	{
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] word = line.split(cvsSplitBy);
				
				unigram_prob.put(word[0], Double.parseDouble(word[2]));
	
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
	
	
	
	public void readbigram(String csvFile)
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
				if(bigram.containsKey(word[0])) {
					Integer val = (Integer) bigram.get(word[0]);
					bigram.put(word[0], val + 1);
					
					}
				else
					{
						if(word[0]!="" || word[0]!=null)
							bigram.put(word[0], Integer.parseInt(word[3]));
					}
				sum = sum + 1.0;
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
	
	
	public void linear_smoothing()
	{
		for (Map.Entry<String, Integer> entry : bigram.entrySet()) {
			
			String str = entry.getKey(); // it is a bigram in the form word1_word2
			String[] word = str.split("_"); // we split the bigram by _, and take the word[0] 
			
			if(word.length<1)
				continue;
			
			if(!unigram.containsKey(word[0]))
				continue;
			
			int Cw_i_1 = unigram.get(word[0]); // c(w_i-1)
			int Cw_i = entry.getValue();
			
			double p_ML = (double)Cw_i/ Cw_i_1;
			
			bigram_prob.put(str, lambda*p_ML + (1 - lambda)*unigram_prob.get(word[0]));
			
		}
	}
	
	
	public void discount_smoothing()
	{
		for (Map.Entry<String, Integer> entry : bigram.entrySet()) {
			
			String str = entry.getKey(); // it is a bigram in the form word1_word2
			String[] word = str.split("_"); // we split the bigram by _, and take the word[0] 
			
			if(word.length<1)
				continue;
			
			if(!unigram.containsKey(word[0]))
				continue;
			
			int Cw_i_1 = unigram.get(word[0]); // c(w_i-1)
			int Cw_i = entry.getValue();
			double numerator = Math.max(Cw_i - sigma, 0.0);
			
			double p_ML = (double)numerator/ Cw_i_1;
			
			bigram_prob.put(str, p_ML + lambda*unigram_prob.get(word[0]));
			
		}
	}
	
	public void most_likely_word(String word_check)
	{
		MyPriorityQueue<_RankItem> fVector = new MyPriorityQueue<_RankItem>(10);
		for (Entry<String, Double> entry : bigram_prob.entrySet()) {
			String str = entry.getKey(); // it is a bigram in the form word1_word2
			String[] word = str.split("_"); // we split the bigram by _, and take the word[0] 
			
			if(word.length<2)
				continue;
			
			if(word[0].equalsIgnoreCase(word_check))
			{
				fVector.add(new _RankItem(word[1], entry.getValue()));
			}
			
			
		}
		
		for(_RankItem it:fVector){
			System.out.println(it.m_name);
		
		}
	}
	
	
	private void generateCsvFile1(String sFileName)
    {
 	try
 	{
 		
 		
 		FileWriter writer = new FileWriter(sFileName); // for Matlab
 	
	 	  for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {	   

			  writer.append(entry.getKey() +","+entry.getValue()+"\n");
			 
		  
			  
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
	 		 
			}	    
	 	    
 	  
 	    //generate whatever data you want
  
 	   writer.flush();
 	  
 	 
 	   writer.close();
 	    
 	}
 	catch(IOException e)
 	{
 	     e.printStackTrace();
 	} 
    }
	
	
	private void generateCsvFile2(String sFileName)
    {
 	try
 	{
 		
 		
 		FileWriter writer = new FileWriter(sFileName); // for Matlab
 	
	 	  for (Map.Entry<String, Double> entry : bigram_prob.entrySet()) {	   

			writer.append(entry.getKey() +","+entry.getValue()+"\n");
	 	  }
  
 	   writer.flush();
 	  
 	 
 	   writer.close();
 	    
 	}
 	catch(IOException e)
 	{
 	     e.printStackTrace();
 	} 
    }
	
	
	
	
	public void sortByComparator() {
		 
		// Convert Map to List
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(bigram.entrySet());
 
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
		
		
		bigram_model1 com = new bigram_model1();
		
		/*String line = "Ayesha_Nahid";
		String word[] = line.split("_");
		System.out.println(word[0]);*/
		
		
		com.readunigram("./data/stat1/probabilty_unigram_ttf.txt");
		com.readunigramProbabilty("./data/stat1/probabilty_unigram_ttf.txt");
		
		for(int i=1;i<=4;i++)
        {
			String filename = "./data/stat1/bi_stat_"+i+".txt";
            com.readbigram(filename);
        }
	
		com.discount_smoothing();
		com.most_likely_word("good");
        com.sortByComparator();
		com.generateCsvFile2("./data/stat1/probabilty_bigram_ttf_absolute_smoothing.txt");
		
		
		
		//analyzer.LoadJson("D:/nahid/Medforum/data/json/eHealth/Part2/eHealth/Schizophrenia/ka5am-T142.json");
		
		//analyzer.generateFrequency_for_smart_word_list();
		
		
		//DocAnalyzer analyzer_bigram = new DocAnalyzer(2);
		//analyzer_bigram.LoadDirectory("./data/json/eHealth/Panic_Attack/", ".json");
		
	}

	

}

