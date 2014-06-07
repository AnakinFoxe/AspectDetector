package edu.csupomona.nlp.ml;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.csupomona.nlp.aspect.AspectParser;
import edu.csupomona.nlp.utils.Stopwords;


public class NaiveBayes {
	private List<String> bigramList;
	private HashMap<String, List<Integer>> frequencyMap;
	private HashMap<String, List<Integer>> frequencyMap2;
	private HashMap<String, List<Integer>> frequencyMap3;
	private HashMap<String, List<Integer>> frequencyMap4;
	private List<String> aspectList;
	private int[] aspectWordSum;
	private int[] aspectWordSum2;
	private int[] aspectWordSum3;
	private int[] aspectWordSum4;
	private int[] aspectSentence;
	private int sentenceTotal;
	
	public NaiveBayes(){
		
	}
	
	public void loadAspectBigrams(List<String> aspectBigrams){
		bigramList.addAll(aspectBigrams);
	}
	
	public void loadAspectFrequency(AspectParser aspectParser){
		// aspect list
		aspectList = new ArrayList<String>();
		List<List<String>> aLists = aspectParser.getAspectList();
		for(int i = 0; i < aLists.size()-2; i++){	// -2
			List<String> aspectSyn = aLists.get(i);
			String aspect = aspectSyn.get(0);
			aspectList.add(aspect);
		}
		aspectList.add("all");		// label: all
		aspectList.add("other");	// label: other
		
		// sentences belong to each aspect, and total count
		aspectSentence = aspectParser.getAspectSentences();
		for(int count : aspectSentence){
			sentenceTotal+=count;
		}
		
		// bigram count for each aspect
		aspectWordSum = new int[aLists.size()];
		frequencyMap = aspectParser.getFrequencyMap();
		for(String bigram : frequencyMap.keySet()){
			List<Integer> counts = frequencyMap.get(bigram);
			for(int i = 0; i < counts.size(); i++){
				aspectWordSum[i]+=counts.get(i);
			}
		}
		
		// local unigram count for each aspect
		aspectWordSum2 = new int[aLists.size()];
		frequencyMap2 = aspectParser.getFrequencyMap2();
		for(String unigram : frequencyMap2.keySet()){
			List<Integer> counts = frequencyMap2.get(unigram);
			for(int i = 0; i < counts.size(); i++){
				aspectWordSum2[i]+=counts.get(i);
			}
		}
		
		// local bigram count for each aspect
		aspectWordSum3 = new int[aLists.size()];
		frequencyMap3 = aspectParser.getFrequencyMap3();
		for(String bigram : frequencyMap3.keySet()){
			List<Integer> counts = frequencyMap3.get(bigram);
			for(int i = 0; i < counts.size(); i++){
				aspectWordSum3[i]+=counts.get(i);
			}
		}
		
		// local trigram count for each aspect
		aspectWordSum4 = new int[aLists.size()];
		frequencyMap4 = aspectParser.getFrequencyMap4();
		for(String trigram : frequencyMap4.keySet()){
			List<Integer> counts = frequencyMap4.get(trigram);
			for(int i = 0; i < counts.size(); i++){
				aspectWordSum4[i]+=counts.get(i);
			}
		}
	}
	
	/**
	 * find the probability of a bigram given it resides in a certain aspect
	 * @param feature
	 * @param given
	 * @return
	 */
	private double bigramProbability(String feature, String given){
		
		List<Integer> count = frequencyMap.get(feature); // (frequencyMap.get(feature)==null)?null:
		int aspectIndex = aspectList.indexOf(given);
		int featureCount = (count != null)? count.get(aspectIndex) : 0 ;		 
		int bigramSize = frequencyMap.size();
		int givenTotal = aspectWordSum[aspectIndex];
		double laplaceProb = ((double)featureCount + 1.0)/((double)givenTotal + (double)bigramSize);
		return laplaceProb;	
	}
	
	/**
	 * find the probability of a unigram given it resides near a certain aspect
	 * @param feature
	 * @param given
	 * @return
	 */
	private double unigramLocalProb(String feature, String given){
		
		List<Integer> count = frequencyMap2.get(feature); // (frequencyMap.get(feature)==null)?null:
		int aspectIndex = aspectList.indexOf(given);
		int featureCount = (count != null)? count.get(aspectIndex) : 0 ;		 
		int unigramSize = frequencyMap2.size();
		int givenTotal = aspectWordSum2[aspectIndex];
		double laplaceProb = ((double)featureCount + 1.0)/((double)givenTotal + (double)unigramSize);
		return laplaceProb;	
	}
	
	/**
	 * find the probability of a bigram given it resides near a certain aspect
	 * @param feature
	 * @param given
	 * @return
	 */
	private double bigramLocalProb(String feature, String given){
		
		List<Integer> count = frequencyMap3.get(feature); // (frequencyMap.get(feature)==null)?null:
		int aspectIndex = aspectList.indexOf(given);
		int featureCount = (count != null)? count.get(aspectIndex) : 0 ;		 
		int bigramSize = frequencyMap3.size();
		int givenTotal = aspectWordSum3[aspectIndex];
		double laplaceProb = ((double)featureCount + 1.0)/((double)givenTotal + (double)bigramSize);
		return laplaceProb;	
	}
	
	/**
	 * find the probability of a trigram given it resides near a certain aspect
	 * @param feature
	 * @param given
	 * @return
	 */
	private double trigramLocalProb(String feature, String given){
		
		List<Integer> count = frequencyMap4.get(feature); // (frequencyMap.get(feature)==null)?null:
		int aspectIndex = aspectList.indexOf(given);
		int featureCount = (count != null)? count.get(aspectIndex) : 0 ;		 
		int trigramSize = frequencyMap4.size();
		int givenTotal = aspectWordSum4[aspectIndex];
		double laplaceProb = ((double)featureCount + 1.0)/((double)givenTotal + (double)trigramSize);
		return laplaceProb;	
	}
	
	/**
	 * find the probability a sentence belongs to a certain aspect
	 * @param aspect
	 * @param sentence
	 * @return
	 */
	private double sentenceProbability(String aspect, String sentence){
		
		String adjustedSentence = sentence.replaceAll("( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
		String words[] = adjustedSentence.split(" +");
//		words = Stopwords.rmStopword(words);	// should move this operation out of here
		double sentenceProb;
		if(words.length > 0){
			String unigram;
			String bigram;
			String trigram;
			sentenceProb = (double)aspectSentence[aspectList.indexOf(aspect)]/sentenceTotal;
			for(int i=0; i < words.length; i++){
				if (!Stopwords.isStopword(words[i])) {
					unigram = words[i];
					sentenceProb+=Math.log(unigramLocalProb(unigram, aspect));
				}
				
				if (i < words.length-1) {
					bigram = words[i] + words[i+1];
//					sentenceProb+=Math.log(bigramProbability(bigram, aspect));
//					sentenceProb+=Math.log(bigramLocalProb(bigram, aspect));
					
					if (i < words.length-2) {
						trigram = words[i] + words[i+1] + words[i+2];
//						sentenceProb+=Math.log(trigramLocalProb(trigram, aspect));
					}
				}
			}
		}else{
			sentenceProb = 0.0;
		}
		return sentenceProb;
	}
	
private double sentenceProbability2(String aspect, String sentence){
		
		String adjustedSentence = sentence.replaceAll("( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
		String words[] = adjustedSentence.split(" +");
//		words = Stopwords.rmStopword(words);	// should move this operation out of here
		double sentenceProb;
		if(words.length > 0){
			String unigram;
			String bigram;
			String trigram;
			sentenceProb = (double)aspectSentence[aspectList.indexOf(aspect)]/sentenceTotal;
			for(int i=0; i < words.length; i++){
				if (!Stopwords.isStopword(words[i])) {
					unigram = words[i];
//					sentenceProb+=Math.log(unigramLocalProb(unigram, aspect));
				}
				
				if (i < words.length-1) {
					bigram = words[i] + words[i+1];
					sentenceProb+=Math.log(bigramProbability(bigram, aspect));
//					sentenceProb+=Math.log(bigramLocalProb(bigram, aspect));
					
					if (i < words.length-2) {
						trigram = words[i] + words[i+1] + words[i+2];
//						sentenceProb+=Math.log(trigramLocalProb(trigram, aspect));
					}
				}
			}
		}else{
			sentenceProb = 0.0;
		}
		return sentenceProb;
	}
	
	public NaiveBayesResult classifySentence(String sentence){
		double max = Double.NEGATIVE_INFINITY;
		String prediction = aspectList.get(aspectList.size()-1); //assume it is not talking about any aspects
		List<String> neutralCase = aspectList.subList(aspectList.size()-2, aspectList.size());
		for(String aspect : neutralCase){
			double aspectProb = sentenceProbability(aspect, sentence);
			if(aspectProb > max && aspectProb != 0.0){
				max = aspectProb;
				prediction = aspect;
			}
//			System.out.println(aspect + ":" + aspectProb);
		}
		String neutral = neutralCase.get(neutralCase.size()-1);
		if(!prediction.equals(neutral)){
			List<String> aspectCase = aspectList.subList(0, aspectList.size()-2);
			max = Double.NEGATIVE_INFINITY;
			for(String aspect : aspectCase){
				double aspectProb = sentenceProbability2(aspect, sentence);
				if(aspectProb > max && aspectProb != 0.0){
					max = aspectProb;
					prediction = aspect;
				}
//				System.out.println(aspect + ":" + aspectProb);
			}
		}

		
		return new NaiveBayesResult(prediction, max);
	}
	
}
