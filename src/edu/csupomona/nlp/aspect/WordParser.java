package edu.csupomona.nlp.aspect;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edu.csupomona.nlp.ml.NaiveBayes;
import edu.csupomona.nlp.ml.NaiveBayesResult;
import edu.csupomona.nlp.utils.MapUtil;
import edu.csupomona.nlp.utils.SimpleCrawler;
import edu.csupomona.nlp.utils.StanfordTools;
import edu.csupomona.nlp.utils.Stopwords;


public class WordParser {

	private AspectParser aspectParser;
	private String aspectPath;
	
	private Map<String, Double> cameraSim;
	
	private NaiveBayes nb;
	
	public WordParser(String pathOutput){
		aspectParser = new AspectParser();
		aspectPath = pathOutput;
		
		cameraSim = new TreeMap<String, Double>();
		
		nb = new NaiveBayes();
		
		StanfordTools.init();
//		SemanticSimilarity.init();
		Stopwords.init();
	}
	
	public void readDataStore(String dataStorePath) throws IOException{
		File[] files = new File(dataStorePath).listFiles();
		aspectParser.loadAspects(aspectPath);
		for (File file : files) {
			processFile(dataStorePath + file.getName());
		}
	}

	private void processFile(String pathInput) throws IOException{
		FileReader reviewFile = new FileReader(pathInput);
		BufferedReader reviews = new BufferedReader(reviewFile);
		String review;
		while((review = reviews.readLine()) != null){
			//split the reviews into sentences
			BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
			iterator.setText(review);
			int start = iterator.first();
			//loop through each sentence
			for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
				String sentence = review.substring(start,end);
				String adjustedSentence = sentence.replaceAll("( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
				adjustedSentence = adjustedSentence.toLowerCase();	// to lower case
				String words[] = adjustedSentence.split(" +");
				words = Stopwords.rmStopword(words);	// remove stopwords at here
				if(words.length > 0){
					aspectParser.parseSentence(words);
				}
				
				// Xing: find similar words of camera
//				List<String[]> list = StanfordTools.posTag(adjustedSentence);
//				if (cameraSim.size() < 5000) {
//					if (cameraSim.size() % 500 == 0) 
//						System.out.println(cameraSim.size());
//					System.out.println(adjustedSentence);
//					for (String[] word: StanfordTools.posTag(adjustedSentence)) {
//						word[0] = StanfordTools.lemmatize(word[0]);	// lemma
//						if ((!cameraSim.containsKey(word[0])) 
//							&& (word[1].equals("NN") || word[1].equals("NNS"))) {
//							cameraSim.put(word[0], SemanticSimilarity.getSim("camera", word[0]));
//						}
//					}
//				}
			}
		}
		reviews.close();
	}

	public void storeData(String outputPath) throws IOException{
		aspectParser.writeData(outputPath);
	}
	
	public List<BigramAspect> selectBigrams(Integer featNum) {
		return aspectParser.selectBigrams(featNum);
	}
	
	public AspectParser getAspectParser() {
		return aspectParser;
	}
	
	public void PrintSim() {
		cameraSim = MapUtil.sortByValue(cameraSim);
		int cnt = 0;
		for (Map.Entry<String, Double> entry : cameraSim.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
			cnt++;
			if (cnt > 100)
				break;
		}
	}
	
	public void train() {
		try {
			readDataStore("data/");
			nb.loadAspectFrequency(this.getAspectParser());
		} catch (Exception e) {
			System.out.println("Training Exception");
		}
	}
	

	
	
	public HashMap<String, List<Integer>> testProduct(String productId) {
		HashMap<String, List<Integer>> results = new HashMap<String, List<Integer>>();
		
		List<String> reviews = SimpleCrawler.crawl(productId);
		
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
		Integer count = 0;
		for (String review : reviews) {
			iterator.setText(review);
			int start = iterator.first();
			//loop through each sentence
			for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
				String sentence = review.substring(start,end);
				String adjustedSentence = sentence.replaceAll("( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
				
				// Naive Bayes classifier
				NaiveBayesResult nbRet = nb.classifySentence(adjustedSentence);
				
				// Sentiment Analysis
				String label = nbRet.getLabel();
				Integer sentiment = 9;	// init as invalid value
				if (!label.equals("other")) {
					count++;
					sentiment = StanfordTools.sentiment(adjustedSentence);
					Integer[] value = null;
					if (results.containsKey(label)) {
						value = results.get(label).toArray(new Integer[0]);
					} else {
						value = new Integer[3];
						value[0] = 0;
						value[1] = 0;
						value[2] = 0;
					}
					if ((sentiment > 0) && (sentiment < 4))
						value[sentiment-1]++;
					else if (sentiment == 4) {
						sentiment = 2;
						value[sentiment] += 2;
					}
					else if (sentiment == 0) 
						value[sentiment] += 2;
					else if ((sentiment > 4) || (sentiment < 0)) {
						System.out.println("Wrong Sentiment: " + sentiment.toString());
						continue;
					}
					
					results.put(label, new ArrayList<Integer>(Arrays.asList(value)));
				}
			}
		}
		
		System.out.println(count.toString());
		
		return results;
	}
	
	
	public static void main(String[] args) {
		WordParser parser = new WordParser("aspects/");
		parser.train();
		HashMap<String, List<Integer>> results = parser.testProduct("B00BV1NKCW");
		
		for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
			System.out.println(entry.getKey() + entry.getValue());
		}
		
//		WordParser parser = new WordParser("aspects/");
//		try {
//			// Preprocessing training data
//			parser.readDataStore("data/");
////			parser.storeData("bigrams.csv");
//			
//			// Train Naive Bayes Classifier
////			NaiveBayes nb = new NaiveBayes();
//			parser.nb.loadAspectFrequency(parser.getAspectParser());
//			
//			// Test on testing set and get sentiment score
//			FileReader reviewFile = new FileReader("nokia-lumia521-tmobile.txt");
//			BufferedReader reviews = new BufferedReader(reviewFile);
//			FileWriter writer = new FileWriter("nokia-result.txt");
//			BufferedWriter writerBW = new BufferedWriter(writer);
//			String review;
////			HashMap<String, int[]> results = new HashMap<String, int[]>();
//			while((review = reviews.readLine()) != null){
//				HashMap<String, List<Integer>> results = parser.testReview(review);
//				
//				for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
//					System.out.println(review);
//					System.out.println(entry.getKey() + entry.getValue());
//				}
//				
//				//split the reviews into sentences
////				BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
////				iterator.setText(review);
////				int start = iterator.first();
////				//loop through each sentence
////				for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
////					String sentence = review.substring(start,end);
////					String printOutSentence = sentence.replaceAll("\\s+$", "");
////					String adjustedSentence = sentence.replaceAll("( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
////					NaiveBayesResult nbRet = nb.classifySentence(adjustedSentence);
////					
////					// sentiment analysis for non-neutral class
////					String label = nbRet.getLabel();
////					Integer sentiment = 9;	// init as invalid value
//////					if (!label.equals("other")) {
//////						sentiment = StanfordTools.sentiment(adjustedSentence);
//////						int[] value = null;
//////						if (results.containsKey(label)) {
//////							value = results.get(label);
//////							
//////						} else {
//////							value = new int[5];
//////						}
//////						if ((sentiment > 4) || (sentiment < 0)) {
//////							System.out.println("Wrong Sentiment: " + sentiment.toString());
//////							return;
//////						}
//////						value[sentiment]++;
//////						results.put(label, value);
//////					}
////					
////					// write result for each sentence
////					writerBW.write("[" + label + "][" + sentiment.toString() + "][]:" + printOutSentence + "\n");
////					//writerBW.write("[" + label + "][" + sentiment.toString() + "][" + nbRet.getProbability() + "]:" + printOutSentence + "\n");
////				}
//				
//				
//			}
//			reviews.close();
//			writerBW.close();
//			
//			parser.PrintSim();
//			
////			for (Map.Entry<String, int[]> entry : results.entrySet()) {
////				System.out.print(entry.getKey() + ": ");
////				for (int idx=0; idx<entry.getValue().length; ++idx) {
////					System.out.print(entry.getValue()[idx] + "\t");
////				}
////				System.out.println();
////			}
//			
//			
//			System.out.println("Finished Processing.");
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}
}
