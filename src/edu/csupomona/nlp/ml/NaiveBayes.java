package edu.csupomona.nlp.ml;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.csupomona.nlp.aspect.AspectParser;


public class NaiveBayes {
	private List<String> bigramList;
	private HashMap<String, List<Integer>> frequencyMap;
	private List<String> aspectList;
	private int[] aspectWordSum;
	private int[] aspectSentence;
	private int sentenceTotal;
	
	public NaiveBayes(){
		
	}
	
	public void loadAspectBigrams(List<String> aspectBigrams){
		bigramList.addAll(aspectBigrams);
	}
	
	public void loadAspectFrequency(AspectParser aspectParser){
		frequencyMap = aspectParser.getFrequencyMap();
		aspectList = new ArrayList<String>();
		List<List<String>> aLists = aspectParser.getAspectList();
		aspectWordSum = new int[aLists.size()];
		aspectSentence = new int[aLists.size()];
		for(int i = 0; i < aLists.size(); i++){
			List<String> aspectSyn = aLists.get(i);
			String aspect = aspectSyn.get(0);
			aspectList.add(aspect);
		}
		
		Set<String> keySet = frequencyMap.keySet();
		for(String bigram : keySet){
			List<Integer> counts = frequencyMap.get(bigram);
			for(int i = 0; i < counts.size(); i++){
				aspectWordSum[i]+=counts.get(i);
			}
		}
		
		aspectSentence = aspectParser.getAspectSentences();
		for(int count : aspectSentence){
			sentenceTotal+=count;
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
//		System.out.println(feature + "+" + given + ": " + featureCount + "=>" + laplaceProb);
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
		double sentenceProb;
		if(words.length > 0){
			String prevWord = words[0];
			String bigram;
			sentenceProb = aspectSentence[aspectList.indexOf(aspect)]/sentenceTotal;
			for(int i=1; i < words.length; i++){
				bigram = prevWord+words[i];
				sentenceProb+=Math.log(bigramProbability(bigram, aspect));
				prevWord = words[i];
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
				double aspectProb = sentenceProbability(aspect, sentence);
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