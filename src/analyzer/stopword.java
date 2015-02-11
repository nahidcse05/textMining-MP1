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
import java.text.Normalizer;
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

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import utils.Utils;




public class stopword {
	
	Map<String, Double> dictionary = new HashMap<String, Double>();
	Map<String, Double> sortedMap;
	ArrayList<String> smart_system_stop_wordlist;
	ArrayList<String> restaurant_stop_wordlist;
	SnowballStemmer m_stemmer;
	
	stopword()
	{
		m_stemmer = new englishStemmer();
		restaurant_stop_wordlist = new ArrayList<String>();
	}
	
	//Snowball Stemmer.
	protected String SnowballStemming(String token){
		m_stemmer.setCurrent(token);
		if(m_stemmer.stem())
			return m_stemmer.getCurrent();
		else
			return token;
	}
	
	//Normalize.
	protected String Normalize(String token){
		token = Normalizer.normalize(token, Normalizer.Form.NFKC);
		token = token.replaceAll("\\W+", "");
		token = token.toLowerCase();
		
		if (Utils.isNumber(token))
			return "NUM";
		else
			return token;
	}
	
	
	public void constructrefinedCV(String inputFile)
	{
		BufferedReader br = null;
		String line = "";
		String SplitBy = ",";
		String sFileName = "./data/stat1/conrolled_CV.txt";
	    try {
	    	
	    	FileWriter writer = new FileWriter(sFileName);
			br = new BufferedReader(new FileReader(inputFile));
			while ((line = br.readLine()) != null) {
				String[] word = line.split(SplitBy);
			    if(smart_system_stop_wordlist.contains(word[0]))
	    		{
	    			// do nothing
	    		}
	    		else
	    		{
	    			 writer.append(word[0]+","+word[1]+","+word[2]+"\n");
	    		}
			}
			writer.flush();
		 	writer.close();	
	 
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
	
	
		public void readTop100Ngram(String File)
		{
			
			
			BufferedReader br = null;
			String line = "";
			String SplitBy = ",";
		    try {
		 
				br = new BufferedReader(new FileReader(File));
				while ((line = br.readLine()) != null) {
					String[] word = line.split(SplitBy);
				    dictionary.put(word[0], Double.parseDouble(word[1]));
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
		 
			System.out.println("Done-"+dictionary.size());
		  
		}
		
	public void readStopword(String File)
	{
		
		smart_system_stop_wordlist= new ArrayList<String>();
		BufferedReader br = null;
		String line = "";
		
		
		try {
			
			br = new BufferedReader(new FileReader(File));
			while ((line = br.readLine()) != null) {
				smart_system_stop_wordlist.add(SnowballStemming(Normalize(line)));
	
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
	 
		System.out.println("Done"+smart_system_stop_wordlist.size());
	  
	}
	

	
	
	  public void restaurant_specific_stop_word()
	    {
	    	
	    	
	    	for (Map.Entry<String, Double> entry : dictionary.entrySet()) {
	    		if(smart_system_stop_wordlist.contains(entry.getKey()))
	    		{
	    			
	    		}
	    		else
	    		{
	    			restaurant_stop_wordlist.add(entry.getKey());
	    			smart_system_stop_wordlist.add(entry.getKey());
	    		}
	    		
	    	}
	   }
	
	  

	
	private void generateFinalStopwordList(String sFileName)
    {
	 	try
	 	{
	 		FileWriter writer = new FileWriter(sFileName);
	 	    
		 	   for (String str : smart_system_stop_wordlist) {
		 		   writer.append(str+"\n");
		 	    }	    
		    writer.flush();
	 	    writer.close();
	 	    
	 	}
	 	catch(IOException e)
	 	{
	 	     e.printStackTrace();
	 	} 
    }
	
	
	private void generateFinalRestaurantStopwordList(String sFileName)
    {
	 	try
	 	{
	 		FileWriter writer = new FileWriter(sFileName);
	 	    
		 	   for (String str : restaurant_stop_wordlist) {
		 		   writer.append(str+"\n");
		 	    }	    
		    writer.flush();
	 	    writer.close();
	 	    
	 	}
	 	catch(IOException e)
	 	{
	 	     e.printStackTrace();
	 	} 
    }
	
	

	
	public static void main(String[] args) {
		stopword com = new stopword();
		com.readStopword("./data/stat1/smart_word_list_top_stop_word.txt");
		com.readTop100Ngram("./data/stat1/Stopword_top_100_ngram.txt");
		com.restaurant_specific_stop_word();
		com.generateFinalStopwordList("./data/stat1/Final_stop_word_list.txt");
		com.generateFinalRestaurantStopwordList("./data/stat1/Restaurant_stopwords.txt");
		com.constructrefinedCV("./data/stat1/combine_ngram_df.txt");
	}

}
