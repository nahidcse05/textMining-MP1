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
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import structures.MyPriorityQueue;
import structures._RankItem;

public class bigram_model {
	
	Map<String, Integer> unigram = new HashMap<String, Integer>();
	Map<String, Double> unigram_prob = new HashMap<String, Double>();
	Map<String, Integer> bigram = new HashMap<String, Integer>();
	Map<String, Double> bigram_prob = new HashMap<String, Double>();
	Map<String, Double> sortedMap = new HashMap<String, Double>();
	
	
	Map<Double, String> sortedMapProbWord = new HashMap<Double, String>(); // key-> probabilty sum, value-> string
	Map<Integer, Double> sortedMapIndexProb = new HashMap<Integer, Double>(); // key-> index value -> prob
	
	
	
	double lambda = 0.9; // linear smoothing
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
	
	public void createAuxiliaryMap()
	{
		 
			int index = 0;
			double sum = 0;
			for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {	
				 
				if(entry.getKey()!=null){
				sum = sum + entry.getValue();
				sortedMapProbWord.put(sum, entry.getKey());
				sortedMapIndexProb.put(index, sum);
				
				index++;
				}
			 }
	}
	
	public String randomSample(String first_word)
	{
		
		int index = 0;
		for (Entry<String, Double> entry : bigram_prob.entrySet()) {
			String str = entry.getKey(); // it is a bigram in the form word1_word2
			String[] word = str.split("_"); // we split the bigram by _, and take the word[0] 
			
			if(word.length<2)
				continue;
			
			if(word[0].equalsIgnoreCase(first_word)){
				index++;
			}
		}
		
		MyPriorityQueue<_RankItem> fVector = new MyPriorityQueue<_RankItem>(index);
		for (Entry<String, Double> entry : bigram_prob.entrySet()) {
			String str = entry.getKey(); // it is a bigram in the form word1_word2
			String[] word = str.split("_"); // we split the bigram by _, and take the word[0] 
			
			if(word.length<2)
				continue;
			
			if(word[0].equalsIgnoreCase(first_word)){
				fVector.add(new _RankItem(word[1], entry.getValue()));
			}
		}
		
		Map<Double, String> tmpMapProbWord =new HashMap<Double, String>();
		Map<Integer, Double> tmpMapIndexProb = new HashMap<Integer, Double>();
		double sum = 0.0;
		index = 0;
		
		for(int i=fVector.size() - 1; i>=0; i-- ){ // Queue is in descedning order and we need increasing order
			
			sum = sum + fVector.get(i).m_value;
			tmpMapProbWord.put(sum, fVector.get(i).m_name);
			tmpMapIndexProb.put(index, sum);
			index++;
		
		}
		
		Random r = new Random();
		index = r.nextInt(fVector.size());
		
		double probabilty = tmpMapIndexProb.get(index);
		String str = tmpMapProbWord.get(probabilty);
		return str;
		
		
	}
	
	public String randomSample()
	{
		
		Random r = new Random();
		int index = r.nextInt(sortedMap.size());
		
		double probabilty = sortedMapIndexProb.get(index);
		String str = sortedMapProbWord.get(probabilty);
		String word[] = str.split("_");
		if(word.length<2 || word[1]==null) return null;
		return word[1];
	}

	public void generateSentence()
	{
		int sentence_lenght = 15;
		int number_of_sentence = 10;
		
		String sentence = "";
		
		for(int i = 1; i< number_of_sentence; i++){
			sentence = "";
			String prev = null;
			while(prev==null)
			{
				prev = randomSample();
			}
			sentence = sentence + prev + " ";
			for(int j = 1; j<=sentence_lenght; j++){
				String next = randomSample(prev);
				sentence = sentence +  next + " ";
				prev = next;
			}
			
			System.out.println("Sentence "+i+":"+ sentence);
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
		ArrayList<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(bigram_prob.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		//sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
	
	}
	
	


	
	
	public static void main(String[] args) {
		
		
		bigram_model com = new bigram_model();
		
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
	
		com.linear_smoothing();
		com.most_likely_word("good");
		
        com.sortByComparator(); // Sorting then call random sample
        com.generateCsvFile2("./data/stat1/probabilty_bigram_ttf_linear_smoothing.txt");
        com.createAuxiliaryMap(); // must be called after sorting 
        com.generateSentence(); // 
        
		//com.generateCsvFile2("./data/stat1/probabilty_bigram_ttf_linear_smoothing.txt");
		
		
		
		//analyzer.LoadJson("D:/nahid/Medforum/data/json/eHealth/Part2/eHealth/Schizophrenia/ka5am-T142.json");
		
		//analyzer.generateFrequency_for_smart_word_list();
		
		
		//DocAnalyzer analyzer_bigram = new DocAnalyzer(2);
		//analyzer_bigram.LoadDirectory("./data/json/eHealth/Panic_Attack/", ".json");
		
	}

	

}

