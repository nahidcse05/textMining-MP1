/**
 * 
 */
package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import structures.MyPriorityQueue;
import structures.Post;
import structures._Corpus;
import structures._Doc;
import structures._RankItem;
import structures._SparseFeature;
import structures._stat;
import utils.Utils;

/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 * NOTE: the code here is only for demonstration purpose, 
 * please revise it accordingly to maximize your implementation's efficiency!
 */
public class DocAnalyzer {
	
	//a list of stopwords
	HashSet<String> m_stopwords;
	protected int m_lengthThreshold;
	//you can store the loaded reviews in this arraylist for further processing
	ArrayList<Post> m_reviews;
	public int review_counter = 0;
	
	//you might need something like this to store the counting statistics for validating Zipf's and computing IDF
	//HashMap<String, Token> m_stats;	
	
	//we have also provided sample implementation of language model in src.structures.LanguageModel
	Hashtable<String, Integer> dictionary = new Hashtable<String, Integer>();
	Hashtable<String, Integer> ByGramSet = new Hashtable<String, Integer>();

	protected Tokenizer m_tokenizer;
	protected SnowballStemmer m_stemmer;
	
	protected boolean m_isCVLoaded; 
	protected boolean m_releaseContent;
	
	protected _Corpus m_corpus;
	protected int m_classNo; //This variable is just used to init stat for every feature. How to generalize it?
	
	protected ArrayList<String> m_featureNames; //ArrayList for features
	protected HashMap<String, Integer> m_featureNameIndex;//key: content of the feature; value: the index of the feature
	protected HashMap<String, _stat> m_featureStat; //Key: feature Name; value: the stat of the feature
	
	protected HashMap<Integer, Double> m_featureIDF; //Key: feature Name; value: the IDF of the feature on trainset
	
	
	protected ArrayList<String> m_bifeatureNames; //ArrayList for features
	protected HashMap<String, Integer> m_bifeatureNameIndex;//key: content of the feature; value: the index of the feature
	protected HashMap<String, _stat> m_bifeatureStat; //Key: feature Name; value: the stat of the feature
	
	
	
	protected int m_Ngram; 
	
	
	
	
	public DocAnalyzer(String providedCV) {
		m_reviews = new ArrayList<Post>();
		m_classNo = 1;
		m_corpus = new _Corpus();
		m_featureNames = new ArrayList<String>();
		m_featureNameIndex = new HashMap<String, Integer>();//key: content of the feature; value: the index of the feature
		m_featureStat = new HashMap<String, _stat>();
		m_featureIDF = new HashMap<Integer, Double>();
		
		
		m_bifeatureNames = new ArrayList<String>();
		m_bifeatureNameIndex = new HashMap<String, Integer>();//key: content of the feature; value: the index of the feature
		m_bifeatureStat = new HashMap<String, _stat>();
		m_isCVLoaded = LoadCVnew(providedCV);
		
		
		m_Ngram = 1;
		try {
			m_tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			m_stemmer = new englishStemmer();
			
			//m_Ngram = Ngram;
			//m_isCVLoaded = LoadCV(providedCV);
			
			
			m_stopwords = new HashSet<String>();
			m_releaseContent = false;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DocAnalyzer() {
		m_reviews = new ArrayList<Post>();
		m_classNo = 1;
		m_corpus = new _Corpus();
		m_featureNames = new ArrayList<String>();
		m_featureNameIndex = new HashMap<String, Integer>();//key: content of the feature; value: the index of the feature
		m_featureStat = new HashMap<String, _stat>();
		
		m_bifeatureNames = new ArrayList<String>();
		m_bifeatureNameIndex = new HashMap<String, Integer>();//key: content of the feature; value: the index of the feature
		m_bifeatureStat = new HashMap<String, _stat>();
		
		
		
		m_Ngram = 1;
		try {
			m_tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			m_stemmer = new englishStemmer();
			
			//m_Ngram = Ngram;
			//m_isCVLoaded = LoadCV(providedCV);
			m_isCVLoaded = false;
			
			m_stopwords = new HashSet<String>();
			m_releaseContent = false;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//sample code for loading a list of stopwords from file
	//you can manually modify the stopword file to include your newly selected words
	public void LoadStopwords(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;

			while ((line = reader.readLine()) != null) {
				//it is very important that you perform the same processing operation to the loaded stopwords
				//otherwise it won't be matched in the text content
				line = SnowballStemmingDemo(NormalizationDemo(line));
				if (!line.isEmpty())
					m_stopwords.add(line);
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n", m_stopwords.size(), filename);
		} catch(IOException e){
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}
	
	
	protected boolean LoadCVnew(String filename) {
		if (filename==null || filename.isEmpty())
			return false;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")){
					if (line.startsWith("#NGram")) {//has to be decoded
						int pos = line.indexOf(':');
						m_Ngram = Integer.valueOf(line.substring(pos+1));
					}
						
				} else {
				    int firstTabindex = line.indexOf(",");
				    int secondTabindex = line.indexOf(",", firstTabindex + 1);
				   
				    String vocab = line.substring(0, firstTabindex);
				    //System.out.println("sec:"+secondTabindex + " " +  thirdTabindex);
				    double idf = Double.parseDouble(line.substring(secondTabindex+1));
				    //System.out.println("idf"+ idf);
				    CreateVocabulary(vocab,idf);
				}
			}
			reader.close();
			
			System.out.format("%d feature words loaded from %s...\n", m_featureNames.size(), filename);
			
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!!", filename);
			return false;
		}
		
		return true; // if loading is successful
	}
	
	
	
	protected boolean LoadCV(String filename) {
		if (filename==null || filename.isEmpty())
			return false;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")){
					if (line.startsWith("#NGram")) {//has to be decoded
						int pos = line.indexOf(':');
						m_Ngram = Integer.valueOf(line.substring(pos+1));
					}
						
				} else {
				    int firstTabindex = line.indexOf("\t");
				    int secondTabindex = line.indexOf("\t", firstTabindex + 1);
				    int thirdTabindex = line.indexOf("\t", secondTabindex + 1);
				    String vocab = line.substring(0, firstTabindex);
				    //System.out.println("sec:"+secondTabindex + " " +  thirdTabindex);
				    double idf = Double.parseDouble(line.substring(secondTabindex,thirdTabindex));
				    //System.out.println("idf"+ idf);
				    CreateVocabulary(vocab,idf);
				}
			}
			reader.close();
			
			System.out.format("%d feature words loaded from %s...\n", m_featureNames.size(), filename);
			
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!!", filename);
			return false;
		}
		
		return true; // if loading is successful
	}
	
	public void analyzeDocumentDemo(JSONObject json) {		
		try {
			JSONArray jarray = json.getJSONArray("Reviews");
			for(int i=0; i<jarray.length(); i++) {
				Post review = new Post(jarray.getJSONObject(i));
				_Doc d = new _Doc(m_corpus.getSize(), review.getContent(), review.getAuthor(), review.getDate());
				//System.out.println(d.getID()+ " "+d.getAuthor()+" "+ d.getDate()+ " "+ d.getSource()+"\n"); 
				//TokenizerDemon(review.getContent());
				String[] tokens = TokenizerNormalizeStemmer(d.getSource());
				if(tokens.length<=0)
					{
						d.m_start_token = null;
						continue;
					}
				d.m_start_token = tokens[0];
				AnalyzeDoc(d);
				review_counter++;
				
				/**
				 * HINT: perform necessary text processing here, e.g., tokenization, stemming and normalization
				 */
				
				//m_reviews.add(review);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	//sample code for loading a json file
	public JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			
			while((line=reader.readLine())!=null) {
				buffer.append(line);
			}
			reader.close();
			
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	// sample code for demonstrating how to recursively load files in a directory 
	public void LoadDirectory(String folder, String suffix) {
		File dir = new File(folder);
		int size = m_reviews.size();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				analyzeDocumentDemo(LoadJson(f.getAbsolutePath()));
				System.out.println(f.getName());
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		size = m_reviews.size() - size;
		System.out.println("Loading " + review_counter + " review documents from " + folder);
	}

	//sample code for demonstrating how to use Snowball stemmer
	public String SnowballStemmingDemo(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to perform text normalization
	public String NormalizationDemo(String token) {
		// remove all non-word characters
		// please change this to removing all English punctuation
		token = token.replaceAll("\\W+", ""); 
		
		// convert to lower case
		token = token.toLowerCase(); 
		
		// add a line to recognize integers and doubles via regular expression
		// and convert the recognized integers and doubles to a special symbol "NUM"
		
		return token;
	}
	
	/*public void TokenizerDemon(String text) {
		//text=stripGarbage(text, 2);
		int inword=1;
		String prevToken="";
		
		for(String str:tokenizer.tokenize(text))
		{
			
			String word = SnowballStemmingDemo(str).toLowerCase().replaceAll("\\W","");
			if(!word.equals(""))
			{
				
				if(inword==1){
					inword=0;
					prevToken=word;
				}
				else{
					String bigram=prevToken+"_"+word;      //ByGramSet
					if(ByGramSet.containsKey(bigram))
					{
						int tempcount= (int)ByGramSet.get(bigram);
						tempcount++;
						ByGramSet.put(bigram, tempcount);
					}
					else{
						ByGramSet.put(bigram, 1);
					}						
					prevToken=word;
				}
				
				if(dictionary.containsKey(word)) {
					Integer val = (Integer) dictionary.get(word);
					dictionary.put(word, val + 1);
					}
				else
					{
						dictionary.put(word, 1);
					}
			}
			
		}
	}
	
	*/

	
	//Tokenizer.
	protected String[] Tokenizer(String source){
		String[] tokens = m_tokenizer.tokenize(source);
		return tokens;
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
	
	//Normalize.

	
	
	protected boolean isLegit(String token) {
		return !token.isEmpty() 
			&& !m_stopwords.contains(token)
			&& token.length()>1
			&& token.length()<20;
	}
	
	protected boolean isBoundary(String token) {
		return token.isEmpty();//is this a good checking condition?
	}
	
	//Given a long string, tokenize it, normalie it and stem it, return back the string array.
	protected String[] TokenizerNormalizeStemmer(String source){
		String[] tokens = Tokenizer(source); //Original tokens.
		//Normalize them and stem them.		
		for(int i = 0; i < tokens.length; i++)
			tokens[i] = SnowballStemming(Normalize(tokens[i]));
		
		LinkedList<String> Ngrams = new LinkedList<String>();
		int tokenLength = tokens.length, N = m_Ngram;		
		for(int i=0; i<tokenLength; i++) {
			String token = tokens[i];
			boolean legit = isLegit(token);
			if (legit)
				Ngrams.add(token);//unigram
			
			//N to 2 grams
			if (!isBoundary(token)) {
				for(int j=i-1; j>=Math.max(0, i-N+1); j--) {	
					if (isBoundary(tokens[j]))
						break;//touch the boundary
					
					token = tokens[j] + "-" + token;
					legit |= isLegit(tokens[j]);
					if (legit)//at least one of them is legitimate
						Ngrams.add(token);
				}
			}
		}
		
		return Ngrams.toArray(new String[Ngrams.size()]);
	}
	
	
	
	//Create Vocabulary from Control Vocabulary File.
	protected void CreateVocabulary(String token, double idf) {
				int index = m_featureNames.size();
				m_featureNameIndex.put(token, index); // set the index of the new feature.
				m_featureNames.add(token); // Add the new feature.
				m_featureStat.put(token, new _stat(m_classNo));
				m_featureIDF.put(index, idf);
	}
		
	
	
	//Add one more token to the current vocabulary.
	protected void expandVocabulary(String token) {
			m_featureNameIndex.put(token, m_featureNames.size()); // set the index of the new feature.
			m_featureNames.add(token); // Add the new feature.
			m_featureStat.put(token, new _stat(m_classNo));
		}
	
	
	//Add one more token to the current vocabulary.
	protected void expandbiVocabulary(String token) {
			m_bifeatureNameIndex.put(token, m_bifeatureNames.size()); // set the index of the new feature.
			m_bifeatureNames.add(token); // Add the new feature.
			m_bifeatureStat.put(token, new _stat(m_classNo));
		}
	
	//Save all the features and feature stat into a file.
	protected void SaveCVStat(String finalLocation, String finalbiLocation) throws FileNotFoundException{
		if (finalLocation==null || finalLocation.isEmpty())
			return;
		
		PrintWriter writer = new PrintWriter(new File(finalLocation));
		for(int i = 0; i < m_featureNames.size(); i++){
			writer.print(m_featureNames.get(i));
			_stat temp = m_featureStat.get(m_featureNames.get(i));
			for(int j = 0; j < temp.getDF().length; j++)
				writer.print("\t" + temp.getDF()[j]);
			for(int j = 0; j < temp.getDF().length; j++){
				double idf_temp = (double)m_corpus.getSize()/temp.getDF()[j];
				double idf = 1 + Math.log10(idf_temp);
				writer.print("\t" + idf);
			}
			for(int j = 0; j < temp.getTTF().length; j++)
				writer.print("\t" + temp.getTTF()[j]);
			writer.println();
		}
		writer.close();
		
		
		PrintWriter biwriter = new PrintWriter(new File(finalbiLocation));
		for(int i = 0; i < m_bifeatureNames.size(); i++){
			biwriter.print(m_bifeatureNames.get(i));
			_stat temp = m_bifeatureStat.get(m_bifeatureNames.get(i));
			for(int j = 0; j < temp.getDF().length; j++)
				biwriter.print("\t" + temp.getDF()[j]);
			for(int j = 0; j < temp.getDF().length; j++){
				double idf_temp = (double)m_corpus.getSize()/temp.getDF()[j];
				double idf = 1 + Math.log10(idf_temp);
				biwriter.print("\t" + idf);
			}
			for(int j = 0; j < temp.getTTF().length; j++)
				biwriter.print("\t" + temp.getTTF()[j]);
			biwriter.println();
		}
		biwriter.close();
		
	}
	
	protected boolean AnalyzeDoc(_Doc doc) {
		String[] tokens = TokenizerNormalizeStemmer(doc.getSource());// Three-step analysis.
		HashMap<Integer, Double> spVct = new HashMap<Integer, Double>(); // Collect the index and counts of features.
		
		HashMap<Integer, Double> bispVct = new HashMap<Integer, Double>(); // Collect the index and counts of features.
		
		int index = 0;
		double value = 0;
		
		int inword=1;
		String prevToken="";
		
		// Construct the sparse vector.
		for (String token : tokens) {
			// CV is not loaded, take all the tokens as features.
			if (!m_isCVLoaded) {
				
				////////////////////////////////for Bigram Start
				
				if(inword==1){
					inword=0;
					prevToken=token;
				}
				else{
					String bigram=prevToken+"@"+token;      //ByGramSet
					
					if (m_bifeatureNameIndex.containsKey(bigram)) {
						index = m_bifeatureNameIndex.get(bigram);
						if (bispVct.containsKey(index)) {
							value = bispVct.get(index) + 1;
							bispVct.put(index, value);
						} else {
							bispVct.put(index, 1.0);
							m_bifeatureStat.get(bigram).addOneDF(doc.getYLabel());
						}
					} else {// indicate we allow the analyzer to dynamically expand the feature vocabulary
						expandbiVocabulary(bigram);// update the m_featureNames.
						index = m_bifeatureNameIndex.get(bigram);
						bispVct.put(index, 1.0);
						m_bifeatureStat.get(bigram).addOneDF(doc.getYLabel());
					}
					m_bifeatureStat.get(bigram).addOneTTF(doc.getYLabel());
				
					
					
					prevToken=token;
				}
				
				
				
				////////////////////////////////for Bigram End
						
				if (m_featureNameIndex.containsKey(token)) {
					index = m_featureNameIndex.get(token);
					if (spVct.containsKey(index)) {
						value = spVct.get(index) + 1;
						spVct.put(index, value);
					} else {
						spVct.put(index, 1.0);
						m_featureStat.get(token).addOneDF(doc.getYLabel());
					}
				} else {// indicate we allow the analyzer to dynamically expand the feature vocabulary
					expandVocabulary(token);// update the m_featureNames.
					index = m_featureNameIndex.get(token);
					spVct.put(index, 1.0);
					m_featureStat.get(token).addOneDF(doc.getYLabel());
				}
				m_featureStat.get(token).addOneTTF(doc.getYLabel());
			} 
			else{ // CV is loaded.
				
				if (m_featureNameIndex.containsKey(token)) {
					
					index = m_featureNameIndex.get(token);
					if (spVct.containsKey(index)) {
						value = spVct.get(index) + 1;
						spVct.put(index, value);
					} else {
						spVct.put(index, 1.0);
						m_featureStat.get(token).addOneDF(doc.getYLabel());
					}
					m_featureStat.get(token).addOneTTF(doc.getYLabel());
				}
		}
			// if the token is not in the vocabulary, nothing to do.
		}// Enf of For of Tokens
	
		
		if (spVct.size()>=m_lengthThreshold) {//temporary code for debugging purpose
			
			if(m_isCVLoaded){
				
				 for(Map.Entry e : spVct.entrySet()) {
					value= (double) e.getValue();
					if(value > 0.0)
					{
						//sub-liner TF scaling
						double TF = 1 + Math.log10(value);
						double idf = m_featureIDF.get(e.getKey());
						
						// put TF*IDF value in doc sparse matrix
						spVct.put((Integer) e.getKey(), TF*idf);
					}
				}
			}

			doc.createSpVct(spVct);
			doc.createBiSpVct(bispVct);
			m_corpus.addDoc(doc);
			
			if (m_releaseContent)
				doc.clearSource();
			return true;
		} else
			return false;
	}
	
	public static void main(String[] args) {		
		DocAnalyzer analyzer = new DocAnalyzer();
		
		//codes for demonstrating tokenization and stemming
		//analyzer.TokenizerDemon("I've practiced for 30 years in pediatrics, and I've never seen anything quite like this.");
		
		//codes for loading json file
		//analyzer.analyzeDocumentDemo(analyzer.LoadJson("./data/Samples/query.json"));
		
		//when we want to execute it in command line
		//analyzer.LoadDirectory("./data/samples", ".json");
		
		analyzer.LoadDirectory("./data/Yelp_small/test", ".json");
		
		//analyzer.LoadDirectory("./data/train", ".json");
		try {
			analyzer.SaveCVStat("./data/stat1/new_uni_stat_5.txt", "./data/stat1/new_bi_stat_5.txt");
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
	/*	String token = "Im/y-na`me?Iam{a}s(to)'n\"is[h]ed!35c";
		token = token.replaceAll("([?`:!'\".,;{}()-/])*", " ");
		token = token.replaceAll("\\W+", "");
		token = token.toLowerCase();
		
		System.out.println(token);*/
	
		
		/*DocAnalyzer analyzer2 = new DocAnalyzer("./data/stat1/conrolled_CV_new.txt");
		analyzer2.LoadDirectory("./data/Yelp_small/query", ".json");
		
		DocAnalyzer analyzer1 = new DocAnalyzer("./data/stat1/conrolled_CV_new.txt");
		analyzer1.LoadDirectory("./data/Yelp_small/test", ".json");
		
		for(int j = 0; j<analyzer2.m_corpus.getSize(); j++)	{
			
			
			MyPriorityQueue<_RankItem> fVector = new MyPriorityQueue<_RankItem>(3);
			
			for(int i = 0; i<analyzer1.m_corpus.getSize(); i++){
				
				double cos_sim = Utils.cosine(analyzer2.m_corpus.getCollection().get(j).getSparse(),analyzer1.m_corpus.getCollection().get(i).getSparse());
				_Doc d = analyzer1.m_corpus.getCollection().get(i);
				//System.out.println( d.getID() +","+ cos_sim);
				fVector.add(new _RankItem(d.getID(), cos_sim));
			}

			System.out.println("Similar docs for "+ analyzer2.m_corpus.getCollection().get(j).getID());
			
			int index = 0;
			for(_RankItem it:fVector){
				
				_Doc d = analyzer1.m_corpus.getCollection().get(it.m_index);
				System.out.println("Doc id:" + d.getID());
				System.out.println("Doc similarity index:" + it.m_value );
				System.out.println("Doc Author:" + d.getAuthor());
				System.out.println("Doc Content:" + analyzer1.m_corpus.getCollection().get(it.m_index).getSource() );
				System.out.println("Doc Date:" + d.getDate());
			
			}	

			System.out.println("----------------------------------------------");
			
		}
		
	*/
		
		
	}
	

}
