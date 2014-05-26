package edu.csupomona.nlp.aspect;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edu.csupomona.nlp.ml.NaiveBayes;
import edu.csupomona.nlp.ml.NaiveBayesResult;
import edu.csupomona.nlp.utils.MapUtil;
import edu.csupomona.nlp.utils.SemanticSimilarity;
import edu.csupomona.nlp.utils.StanfordTools;
import edu.csupomona.nlp.utils.Stopwords;


public class WordParser {

	private AspectParser aspectParser;
	private String aspectPath;
	
	private Map<String, Double> cameraSim;
	
	public WordParser(String pathOutput){
		aspectParser = new AspectParser();
		aspectPath = pathOutput;
		
		cameraSim = new TreeMap<String, Double>();
		
		StanfordTools.init();
		SemanticSimilarity.init();
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
				if(words.length > 0){
					aspectParser.parseSentece(words);
				}
				
				// Xing: find similar words of camera
//				List<String[]> list = StanfordTools.posTag(adjustedSentence);
				if (cameraSim.size() < 5000) {
					if (cameraSim.size() % 500 == 0) 
						System.out.println(cameraSim.size());
//					System.out.println(adjustedSentence);
					for (String[] word: StanfordTools.posTag(adjustedSentence)) {
						word[0] = StanfordTools.lemmatize(word[0]);	// lemma
						if ((!cameraSim.containsKey(word[0])) 
							&& (word[1].equals("NN") || word[1].equals("NNS"))) {
							cameraSim.put(word[0], SemanticSimilarity.getSim("camera", word[0]));
						}
					}
				}
			}
		}
		reviews.close();
	}

	public void storeData(String outputPath) throws IOException{
		aspectParser.writeBigrams(outputPath);
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
	
	public static void main(String[] args) {
		WordParser parser = new WordParser("aspects/");
		try {
			// Preprocessing training data
			parser.readDataStore("data/");
			parser.storeData("bigrams.csv");
			
			// Train Naive Bayes Classifier
			NaiveBayes nb = new NaiveBayes();
			nb.loadAspectFrequency(parser.getAspectParser());
			
			// Test on testing set and get sentiment score
			FileReader reviewFile = new FileReader("nokia-lumia521-tmobile.txt");
			BufferedReader reviews = new BufferedReader(reviewFile);
			FileWriter writer = new FileWriter("nokia-result.txt");
			BufferedWriter writerBW = new BufferedWriter(writer);
			String review;
//			HashMap<String, int[]> results = new HashMap<String, int[]>();
			while((review = reviews.readLine()) != null){
				//split the reviews into sentences
				BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
				iterator.setText(review);
				int start = iterator.first();
				//loop through each sentence
				for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
					String sentence = review.substring(start,end);
					String adjustedSentence = sentence.replaceAll("( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
					NaiveBayesResult nbRet = nb.classifySentence(adjustedSentence);
					
					// sentiment analysis for non-neutral class
					String label = nbRet.getLabel();
					Integer sentiment = 9;	// init as invalid value
//					if (!label.equals("other")) {
//						sentiment = StanfordTools.sentiment(adjustedSentence);
//						int[] value = null;
//						if (results.containsKey(label)) {
//							value = results.get(label);
//							
//						} else {
//							value = new int[5];
//						}
//						if ((sentiment > 4) || (sentiment < 0)) {
//							System.out.println("Wrong Sentiment: " + sentiment.toString());
//							return;
//						}
//						value[sentiment]++;
//						results.put(label, value);
//					}
					
					// write result for each sentence
					writerBW.write("[" + label + "][" + sentiment.toString() + "][" + nbRet.getProbability() + "]:" + adjustedSentence + "\n");
				}
			}
			reviews.close();
			writerBW.close();
			
			parser.PrintSim();
			
//			for (Map.Entry<String, int[]> entry : results.entrySet()) {
//				System.out.print(entry.getKey() + ": ");
//				for (int idx=0; idx<entry.getValue().length; ++idx) {
//					System.out.print(entry.getValue()[idx] + "\t");
//				}
//				System.out.println();
//			}
			
			

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
