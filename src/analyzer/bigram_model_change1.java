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
import structures._SparseFeature;


class struct{
	public String word;
	public double prob;
	struct(String w, double p)
	{
		this.word = w;
		this.prob = p;
	}
}

public class bigram_model_change1 {
	
	Map<String, Integer> unigram = new HashMap<String, Integer>();
	Map<Integer, String> unigram_index = new HashMap<Integer,String>();
	Map<String, Double> unigram_prob = new HashMap<String, Double>();
	Map<String, Double> unigram_prob_test = new HashMap<String, Double>();
	Map<String, Double> unigram_matrix = new HashMap<String, Double>();
	Map<Integer, String> bigram = new HashMap<Integer, String>();
	Map<String, Double> bigram_prob = new HashMap<String, Double>();
	Map<String, Double> bigram_count = new HashMap<String, Double>();
	Map<String, Double> sortedMap = new HashMap<String, Double>();
	Map<String, Double> sortedMapUnigram = new HashMap<String, Double>();
	
	Map<Double, String> sortedMapProbWord = new HashMap<Double, String>(); // key-> probabilty sum, value-> string
	Map<Integer, Double> sortedMapIndexProb = new HashMap<Integer, Double>(); // key-> index value -> prob
	
	Map<String, Double> uniInBigramCount = new HashMap<String, Double>();
	Map<String, Double> uniInBigramCountTest = new HashMap<String, Double>();
	
	public Map<String, Double> unigramTest = new HashMap<String, Double>();
	public Map<String, Double> bigramTest = new HashMap<String, Double>();
	Random number = new Random();
	Random r = new Random();
	
	double lambda = 0.9; // linear smoothing
	double sigma = 0.1;
	double sum = 0;
	double unk_prob;
	
	int vocabulary_size = 0;

	public void readunigram(String csvFile)
	{
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\t";
		int i = 0;
		sum = 0.0;
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] word = line.split(cvsSplitBy);
				
				if(word[0]=="" || word[0]==null)
				{
					System.out.println("during read unigram: " + word[0]);
					continue;
				}
				
				if(count(word[0])>=1){
					System.out.println("during read unigram: " + word[0]);
					continue;
				}
				
				
				
				unigram.put(word[0], i); // word and their index in final vocabulary key: word value: index
				unigram_index.put(i,word[0]);
				unigram_matrix.put(word[0], (double)Integer.parseInt(word[3]));
				
				i++;
				sum = sum +  Integer.parseInt(word[3]);
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
		
		vocabulary_size = unigram.size();
		for(i =0; i<unigram_matrix.size(); i++)
		{
			String tmp1 = unigram_index.get(i);
			unigram_prob.put(tmp1,(double) unigram_matrix.get(tmp1) / sum);
	
		}
	 
		
		for(i =0; i<unigram_matrix.size(); i++)
		{
			String tmp1 = unigram_index.get(i);
			unigram_prob_test.put(tmp1,(double) (unigram_matrix.get(tmp1) + sigma) / (sum + sigma*vocabulary_size));
	
		}
		
		unk_prob = sigma / (sum + sigma*vocabulary_size);
		
	  
	}
	

	
	public void readunigramonTest(String csvFile)
	{
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\t";
		int i = unigram.size(); // i start from the train folder index
		
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			    // use comma as separator
				String[] word = line.split(cvsSplitBy);
				
				if(word[0]=="" || word[0]==null)
				{
					System.out.println("during read unigram Test: " + word[0]);
					continue;
				}
				
				
				if(count(word[0])>=1){
					System.out.println("during read unigram Test Folder: " + word[0]);
					continue;
				}
				
				
				if(!unigram.containsKey(word[0]))
				{
					unigram.put(word[0], i); // word and their index in final vocabulary key: word value: index
					unigram_index.put(i,word[0]);
					unigram_matrix.put(word[0], 0.0); // placing count zero here because test folder
					
					i++;
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
		
		
		// this now the smoothed unigram model
		for(i =0; i<unigram_matrix.size(); i++)
		{
			String tmp1 = unigram_index.get(i);
			unigram_prob.put(tmp1,(double) (unigram_matrix.get(tmp1) + sigma)  / (unigram_matrix.size()*sigma + sum));
	
		}
	 
		
	  
	}
	
	
	public void readbigram(String csvFile)
	{
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\t";
		int i=0;
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			    // use tab as separator
				String[] word = line.split(cvsSplitBy);
				String tmp[] = word[0].split("@");
				if(tmp.length<=0)
					continue;
				//System.out.println(tmp[0]);
				
				if(count(word[0])>1){
					System.out.println("during read bigram: " + word[0]);
					continue;
				}
				
				if(!unigram.containsKey(tmp[0])){
					System.out.println("NOT IN UNI during read");
					continue;
				}
				Double count = unigram_matrix.get(tmp[0]);
				bigram.put(i, word[0]);
				bigram_count.put(word[0], (double) Integer.parseInt(word[3])); 
				
				bigram_prob.put(word[0], (double) Integer.parseInt(word[3]) / count); // MLE
				i++;
				
				if(uniInBigramCount.containsKey(tmp[0]))
				{
					double val = uniInBigramCount.get(tmp[0]);
					val = val + 1;
					uniInBigramCount.put(tmp[0],val);
				}
				else
				{
					uniInBigramCount.put(tmp[0],1.0);
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
	}
	
	

	
	public void linear_smoothing_new1(String word_check)
	{
		lambda = 0.9;
		MyPriorityQueue<_RankItem> fVector = new MyPriorityQueue<_RankItem>(10);
		for (int i=0; i<unigram.size(); i++) {
			
			String str = unigram_index.get(i);
			
			String bi = word_check+"@"+str;
		
			double pml, value;
			if(bigram_prob.containsKey(bi)){
				pml = bigram_prob.get(bi);
				value = lambda*pml + (1 - lambda)*unigram_prob.get(str);
			}
			else
			{
				pml = 0;
				value = lambda*pml + (1 - lambda)*unigram_prob.get(str);
			}
			
			fVector.add(new _RankItem(str, value));
			
		}
		
		System.out.println("Word from Linear Smoothing");
		for(_RankItem it:fVector){
			System.out.println(it.m_name);
		
		}
	}
	
	
	public String randomSampleFinal(MyPriorityQueue<_RankItem> fVector)
	{
		
		Map<Double, String> tmpMapProbWord =new HashMap<Double, String>();
		Map<Integer, Double> tmpMapIndexProb = new HashMap<Integer, Double>();
		double sum = 0.0;
		int index = 0;
		
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
	
	
	
	public struct linear_smoothing_sample_new(String word_check, double random_value)
	{
		
		lambda = 0.9;
		for (Entry<String, Double> entry : bigram_prob.entrySet()) {
			
			String str1 = entry.getKey().split("@")[0];
			String str2 = entry.getKey().split("@")[1];
			if(!word_check.equalsIgnoreCase(str1))
				continue;
			
			String bi = entry.getKey();
		
			double pml, value;
			if(bigram_prob.containsKey(bi)){
				pml = bigram_prob.get(bi);
				value = lambda*pml + (1 - lambda)*unigram_prob.get(str2);
			}
			else
			{
				pml = 0;
				value = lambda*pml + (1 - lambda)*unigram_prob.get(str2);
			}
			
			random_value = random_value - value;
			if(random_value<=0){
				struct s = new struct(str2,value);
				return s;
			}
		}
		return null;
    }
	
	
	
	
	public struct linear_smoothing_sample(String word_check, double random_value)
	{
		
		
		for (int i=0; i<unigram.size(); i++) {
			
			String str = unigram_index.get(i);
			
			String bi = word_check+"@"+str;
		
			double pml, value;
			if(bigram_prob.containsKey(bi)){
				pml = bigram_prob.get(bi);
				value = lambda*pml + (1 - lambda)*unigram_prob.get(str);
			}
			else
			{
				pml = 0;
				value = lambda*pml + (1 - lambda)*unigram_prob.get(str);
			}
			
			random_value = random_value - value;
			if(random_value<=0){
				struct s = new struct(str,value);
				return s;
			}
			
		
			
		}
		
		return null;
    }
	
	
	public double linear_smoothing_for_perplexity(String bigram_word)
	{
		
			double pml, value;
			lambda = 0.9;
			String str[]= bigram_word.split("@");
			if(bigram_prob.containsKey(bigram_word)){
				pml = bigram_prob.get(bigram_word);
				value = lambda*pml + (1 - lambda)*unigram_prob_test.get(str[1]);
			}
			else
			{
				pml = 0;
				if(unigram_prob_test.containsKey(str[1])){
					
					value = lambda*pml + (1 - lambda)*unigram_prob_test.get(str[1]);
				}
				else
					value = lambda*pml + (1 - lambda)*unk_prob;
				
				
			}
			
			return value;
			
	}
	
		
	public double discount_smoothing_for_perplexity(String bigram_word)
	{
		
			double max, value;
			String str[]= bigram_word.split("@");
			if(bigram_prob.containsKey(bigram_word)){
				
				max = Math.max(bigram_count.get(bigram_word) - sigma , 0.0);
				double tmp = unigram_matrix.get(str[0]);
				lambda = (sigma*uniInBigramCount.get(str[0]))/tmp;
				value = max/tmp + lambda*unigram_prob_test.get(str[1]);
					
			}
			else
			{
				max = 0.0; // bigram does not contain so it should be zero
			
				if(!uniInBigramCount.containsKey(str[0])) 
					lambda = 1;
				else{
					if(!unigram_prob_test.containsKey(str[0]))
						lambda = (sigma*uniInBigramCount.get(str[0]))/sigma; //since not in bigram that means not in unigram also, so set unigram count = 1  
					else
						lambda = (sigma*uniInBigramCount.get(str[0]))/unigram_matrix.get(str[0]); 
				}
				
				if(unigram_prob_test.containsKey(str[1]))
					value = 0.0 + lambda*unigram_prob_test.get(str[1]);
				else
					value = 0.0 + lambda*unk_prob;
			}
			
		
		return value;
	}
	
	
	public struct discount_smoothing_sample_new(String word_check, double random_value)
	{
		
		
		//MyPriorityQueue<_RankItem> fVector = new MyPriorityQueue<_RankItem>(unigram.size());
	for (Entry<String, Double> entry : bigram_prob.entrySet()) {
			
			String str1 = entry.getKey().split("@")[0];
			String str2 = entry.getKey().split("@")[1];
			if(!word_check.equalsIgnoreCase(str1))
				continue;
			
			String bi = entry.getKey();
			double pml, max, value;
			if(bigram_prob.containsKey(bi)){
				
				max = Math.max(bigram_count.get(bi) - sigma , 0.0);
			    lambda = (sigma*uniInBigramCount.get(word_check))/unigram_matrix.get(word_check);
				value = max/unigram_matrix.get(word_check) + lambda*unigram_prob.get(str2);
				
			}
			else
			{
				max = 0.0;
				if(!uniInBigramCount.containsKey(word_check)){
					System.out.println("NOT IN uniInBigramCount:" + word_check);
				}
				lambda = (sigma*uniInBigramCount.get(word_check))/unigram_matrix.get(word_check);
				value = max/unigram_matrix.get(word_check) + lambda*unigram_prob.get(str2);
			}
			
			random_value = random_value - value;
			if(random_value<=0){
				struct s = new struct(str2,value);
				return s;
			}
				
			//fVector.add(new _RankItem(str, value));
		}
		
		return null;
		//return randomSampleFinal(fVector);
	}
	
	
	
	public struct discount_smoothing_sample(String word_check, double random_value)
	{
		
		for (int i=0; i<unigram.size(); i++) {
			
			String str = unigram_index.get(i);
			
			String bi = word_check+"@"+str;
			
			double pml, max, value;
			if(bigram_prob.containsKey(bi)){
				
				max = Math.max(bigram_count.get(bi) - sigma , 0.0);
			    lambda = (sigma*uniInBigramCount.get(word_check))/unigram_matrix.get(word_check);
				value = max/unigram_matrix.get(word_check) + lambda*unigram_prob.get(str);
				
			}
			else
			{
				max = 0.0;
				if(!uniInBigramCount.containsKey(word_check)){
					System.out.println("NOT IN uniInBigramCount:" + word_check);
				}
				lambda = (sigma*uniInBigramCount.get(word_check))/unigram_matrix.get(word_check);
				value = max/unigram_matrix.get(word_check) + lambda*unigram_prob.get(str);
			}
			
			random_value = random_value - value;
			if(random_value<=0){
				struct s = new struct(str,value);
				return s;
			}
				
			//fVector.add(new _RankItem(str, value));
		}
		
		return null;
		//return randomSampleFinal(fVector);
	}
	
	
	public void discount_smoothing(String word_check)
	{
		MyPriorityQueue<_RankItem> fVector = new MyPriorityQueue<_RankItem>(10);
		for (int i=0; i<unigram.size(); i++) {
			
			String str = unigram_index.get(i);
			
			String bi = word_check+"@"+str;
			
			double pml, max, value;
			if(bigram_prob.containsKey(bi)){
				
				max = Math.max(bigram_count.get(bi) - sigma , 0.0);
				lambda = (sigma*uniInBigramCount.get(word_check))/unigram_matrix.get(word_check);
				value = max/unigram_matrix.get(word_check) + lambda*unigram_prob.get(str);
				
			}
			else
			{
				max = 0.0;
				lambda = (sigma*uniInBigramCount.get(word_check))/unigram_matrix.get(word_check);
				value = max/unigram_matrix.get(word_check) + lambda*unigram_prob.get(str);
			}
			
			
			
			fVector.add(new _RankItem(str, value));
		}
		
		System.out.println("Words from Absolute Smoothing");
		for(_RankItem it:fVector){
			System.out.println(it.m_name);
		
		}
	}
	



	
	public struct random_sample_uniform()
	{
		
		int index = r.nextInt(vocabulary_size);
		String str = unigram_index.get(index);
		struct s = new struct(str, unigram_prob.get(str));
		return s;
	}
	


	public void generateSentenceUniform()
	{
		int sentence_lenght = 15;
		int number_of_sentence = 10;
		
		String sentence = "";
		double liklihood = 1;
		struct tmp=null;
		
		for(int i = 1; i<=number_of_sentence; i++){
			sentence = "";
			String prev = null;
			liklihood = 1;
			tmp=null;
			
			while(tmp==null)
			{
				tmp = random_sample_uniform();
			}
			liklihood = liklihood*tmp.prob;
			prev = tmp.word;
			
			sentence = sentence + " "+ prev ;
			for(int j = 1; j< sentence_lenght; j++){
				tmp = random_sample_uniform();
				String next = tmp.word;
				liklihood = liklihood*tmp.prob;
				//System.out.println("next"+next);
				sentence = sentence +  " "+ next;
				
			}
			
			System.out.println("Sentence "+i+":"+ sentence);
			System.out.println("liklihood "+i+":"+ liklihood);
		}
	}
	
	
	public void generateSentenceBigramLinear()
	{
		int sentence_lenght = 15;
		int number_of_sentence = 10;
		
		String sentence = "";
		double liklihood = 1;
		struct tmp=null;
		
		for(int i = 1; i<=number_of_sentence; i++){
			sentence = "";
			String prev = null;
			liklihood = 1;
			tmp=null;
			while(tmp==null)
			{
				tmp = random_sample_uniform();
			}
			prev = tmp.word;
			liklihood = liklihood*tmp.prob;
			sentence = sentence + " "+ prev ;
			for(int j = 1; j< sentence_lenght; j++){
				double random = Math.random();
				tmp = linear_smoothing_sample_new(prev, random);
				while(tmp==null)
				{
					random = Math.random();
					tmp = linear_smoothing_sample_new(prev, random);
				}
				String next = tmp.word;
				liklihood = liklihood*tmp.prob;
				sentence = sentence +  " "+ next;
				prev = next;
			}
			
			System.out.println("Sentence "+i+":"+ sentence);
			System.out.println("liklihood "+i+":"+ liklihood);
		}
	}
	
	
	public void generateSentenceBigramAbsolute()
	{
		int sentence_lenght = 15;
		int number_of_sentence = 10;
		
		String sentence = "";
		double liklihood = 1;
		struct tmp=null;
		
		for(int i = 1; i<=number_of_sentence; i++){
			sentence = "";
			String prev = null;
			liklihood = 1;
			tmp=null;
			while(tmp==null)
			{
				tmp = random_sample_uniform();
			}
			prev = tmp.word;
			liklihood = liklihood*tmp.prob;
			sentence = sentence + " "+ prev ;
			for(int j = 1; j< sentence_lenght; j++){
				double random = Math.random();
				tmp = discount_smoothing_sample_new(prev, random);
				while(tmp==null)
				{
					random = Math.random();
					tmp = discount_smoothing_sample_new(prev, random);
				}
				String next = tmp.word;
				liklihood = liklihood*tmp.prob;
				sentence = sentence +  " "+ next;
				prev = next;
			}
			
			System.out.println("Sentence "+i+":"+ sentence);
			System.out.println("liklihood "+i+":"+ liklihood);
		}
	}
	
	
	private void save2File(String sFileName, ArrayList<Double> list)
    {
 	try
 	{
 		
 		double mean_sum = 0.0;
 		double var_sum = 0.0;
 		FileWriter writer = new FileWriter(sFileName); 
 	
 		for(int i=0; i<list.size(); i++){
			writer.append(list.get(i)+"\n");
			mean_sum = mean_sum + list.get(i);
	 	  }
  
 		
 	   double mean = mean_sum / list.size();
 	   writer.append("MEAN: "+mean +"\n");
 	
 	  for(int i=0; i<list.size(); i++){
 		  var_sum = var_sum + (list.get(i) - mean)*(list.get(i) - mean);
	  }
 	  double variance = var_sum / list.size();
 	  writer.append("VARIANCE "+variance +"\n"); 
 	  writer.flush();
 	  
 	 
 	   writer.close();
 	    
 	}
 	catch(IOException e)
 	{
 	     e.printStackTrace();
 	} 
    }
	
	public void sortByComparatorUnigram() {
		 
		// Convert Map to List
		ArrayList<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(unigram_prob.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		sortedMapUnigram = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMapUnigram.put(entry.getKey(), entry.getValue());
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
	
	public double calculate_perplexity_uni(ArrayList<String> list, double N)
	{
		double value = 0.0;
		
		for(int i=0; i<list.size(); i++)
		{
			
			if(unigram_prob_test.containsKey(list.get(i)))
				value = value + Math.log(unigram_prob_test.get(list.get(i)));
			else
				value = value + Math.log(unk_prob);
		}
		
		value = value / N;
		value = (-1)* value;
		
		return Math.exp(value);
		
	}
	
	public double calculate_perplexity_bi_linear(ArrayList<String> list, double N, String start)
	{
		double value = 0.0;
		if(start==null || start=="")
			return 0.0;
		
		if(unigram_prob_test.containsKey(start))
			value = value + Math.log(unigram_prob_test.get(start));
		else
			value = value + Math.log(unk_prob);
		for(int i=0; i<list.size(); i++)
		{
			value = value + Math.log(linear_smoothing_for_perplexity(list.get(i)));
		}
		value = value / N;
		value = (-1)* value;
		
		return Math.exp(value);
		
	}
	
	public double calculate_perplexity_bi_abs(ArrayList<String> list, double N,String start)
	{
		double value = 0.0;
		if(start==null || start=="")
			return 0.0;
		if(unigram_prob_test.containsKey(start))
			value = value + Math.log(unigram_prob_test.get(start));
		else
			value = value + Math.log(unk_prob);
		for(int i=0; i<list.size(); i++)
		{
			value = value + Math.log(discount_smoothing_for_perplexity(list.get(i)));
		}
		
		value = value / N;
		value = (-1)* value;
		
		return Math.exp(value);
		
	}
	
	
	public int count(String str)
	{
		int count=0;
		for (int i = 0; i <str.length(); i++)
		{
			char current = str.charAt(i);
			if (current == '@') // Check the char is a letter.
			{
				count++;
			}
		} 
		return count;
	}
	
	
	
	public static void main(String[] args) {
		
		
		bigram_model_change1 com = new bigram_model_change1();
		com.readunigram("./data/stat1/new_uni_stat_all.txt");
		com.readbigram("./data/stat1/new_bi_stat_all.txt");
        //com.linear_smoothing_new1("good");
        //com.discount_smoothing("good");
		
/*		//com.sortByComparatorUnigram();
        //com.sortByComparator(); // Sorting then call random sample
        //com.generateCsvFile2("./data/stat1/probabilty_bigram_ttf_linear_smoothing.txt");
        
        //com.createAuxiliaryMap(); // must be called after sorting */
        com.generateSentenceUniform(); // uniform  
        com.generateSentenceBigramLinear();
        com.generateSentenceBigramAbsolute();
      
        //com.readunigramonTest("./data/stat1/new_uni_stat_5.txt");
        
        DocAnalyzer analyzer = new DocAnalyzer();
		analyzer.LoadDirectory("./data/Yelp_small/test", ".json");
		
		
		ArrayList<Double> uni_pp = new ArrayList<Double>();
		ArrayList<Double> bi_pp_lin = new ArrayList<Double>();
		ArrayList<Double> bi_pp_abs = new ArrayList<Double>();
		
		
		
		for(int i = 0; i<analyzer.m_corpus.getCollection().size(); i++){
			_SparseFeature[] unigram = analyzer.m_corpus.getCollection().get(i).getSparse();
			_SparseFeature[] bigram = analyzer.m_corpus.getCollection().get(i).getBiSparse() ;
			ArrayList<String> list = new ArrayList<String>();
			ArrayList<String> list_bi = new ArrayList<String>();
			
			double size = analyzer.m_corpus.getCollection().get(i).getTotalDocLength();
			
			
			for(int j=0; j<unigram.length; j++)
			{
				
				String word = analyzer.m_featureNames.get(unigram[j].getIndex());
				if(com.count(word)>=1){
					System.out.println("Problem in unigram: " + word);
					continue;
				}
				
				
				list.add(analyzer.m_featureNames.get(unigram[j].getIndex()));
				
			}
			
			
			
			for(int j=0; j<bigram.length; j++){
			
				String word = analyzer.m_bifeatureNames.get(bigram[j].getIndex());
				
				if(com.count(word)>1)
				{
					System.out.println("Problem more:" +word);
					continue;
				}
			
				list_bi.add(analyzer.m_bifeatureNames.get(bigram[j].getIndex()));
				
				
			}
				
			uni_pp.add(com.calculate_perplexity_uni(list,size));
			
			String start =  analyzer.m_corpus.getCollection().get(i).m_start_token;
			
			bi_pp_lin.add(com.calculate_perplexity_bi_linear(list_bi, size, start));
			
			
			bi_pp_abs.add(com.calculate_perplexity_bi_abs(list_bi, size, start));
			
			
			
		}
		com.save2File("./data/stat1/perplexity_unigram.txt", uni_pp);
		com.save2File("./data/stat1/perplexity_bigram_linear.txt", bi_pp_lin);
		com.save2File("./data/stat1/perplexity_bigram_abs.txt", bi_pp_abs);
	}
	

}

