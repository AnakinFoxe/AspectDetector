package edu.csupomona.nlp.aspect;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.csupomona.nlp.utils.MapUtil;

/**
 * parses sentences to determine if the sentence is talking about a specific aspect. 
 */
public class AspectParser {

	private HashMap<String, List<Integer>> frequencyMap; //bigram to each frequecy
	private HashMap<String, List<Integer>> frequencyMap2;
	private HashMap<String, List<Integer>> frequencyMap3;
	private HashMap<String, List<Integer>> frequencyMap4;
	private List<List<String>> aspectList; //list of aspects we are looking for
	private int[] aspectSentences;
	private List<String> aspectNameList;

	public AspectParser(){
		frequencyMap = new HashMap<String, List<Integer>>();
		frequencyMap2 = new HashMap<String, List<Integer>>();
		frequencyMap3 = new HashMap<String, List<Integer>>();
		frequencyMap4 = new HashMap<String, List<Integer>>();
		aspectList = new ArrayList<List<String>>();
		aspectNameList = new ArrayList<String>();
	}
	
	
	// comment
	
	/**
	 * load the file that contains the aspects we are looking for. The program will also
	 * add a neutral aspect when a sentence does not relate to an aspect.
	 * @param filename
	 * @throws IOException 
	 */
	private List<String> getAspect(String filename) throws IOException{
		FileReader aspectsFile = new FileReader(filename);
		BufferedReader aspectBR = new BufferedReader(aspectsFile);
		String aspect;
		List<String> aspectSynonyms = new ArrayList<String>();
		while((aspect = aspectBR.readLine())!=null){
			aspect = aspect.trim();
			if(!aspect.isEmpty()){
				aspectSynonyms.add(aspect);
			}
		}
		//aspectList.add("neutral");
		aspectBR.close();
		return aspectSynonyms;
	}
	
	public void loadAspects(String path) throws IOException{
		File[] files = new File(path).listFiles();
		Arrays.sort(files);
		System.out.println("loading aspects:");
		for (File file : files) {
			System.out.println(file.getName());
			List<String> aspect = getAspect(path + file.getName());
			aspectList.add(aspect);
			aspectNameList.add(file.getName());
		}
		List<String> neutralList = new ArrayList<String>();
		neutralList.add("other");
		aspectList.add(neutralList);
		aspectSentences = new int[aspectList.size()];
	}
	/**
	 * converts an array of words into a list of words
	 * @param words
	 * @return
	 */
	private List<String> array2List(String[] words){
		List<String> sentence = new ArrayList<String>();
		for(int i = 0; i < words.length; i++){
			sentence.add(words[i]);
		}
		return sentence;
	}
	/**
	 * through each sentence check to see if the aspect is explicitly mentioned then write
	 * to the appropriate aspect file the bigram word
	 * @param words
	 */
	public void parseSentence(String[] words) {
		// TODO Auto-generated method stub
		List<String> sentence = array2List(words);
		List<Integer> aspectFrequency = new ArrayList<Integer>();
		for(int i = 0; i < aspectList.size()-1; i++){
			List<String> aspect = aspectList.get(i);
			for (int j = 0; j < aspect.size(); j++) {
				if(sentence.contains(aspect.get(j))){
					aspectFrequency.add(1);
					aspectSentences[i]+=1;
					break;
				}else if (j == aspect.size()-1) {
					aspectFrequency.add(0);
				}
			}
		}
		
		//check to see if any aspects were found
		if(aspectFrequency.contains(1)){
			aspectFrequency.add(0);
		}else{
			aspectFrequency.add(1);	// add 1 to "other" column
			aspectSentences[aspectList.size()-1]+=1;
		}
		
		String bigram;
		String prevWord = words[0];
		for(int i = 1; i < words.length; i++){
			bigram = (prevWord+words[i]).toLowerCase();
			prevWord = words[i];
			if(frequencyMap.containsKey(bigram)){
				List<Integer> oldList = frequencyMap.get(bigram);
				List<Integer> newFrequency = mergeList(oldList, aspectFrequency);
				frequencyMap.put(bigram, newFrequency);
			}else{
				frequencyMap.put(bigram, aspectFrequency);
			}
		}
		
		parseWindow(words);
	}
	
	public void parseWindow(String[] words) {
		// TODO Auto-generated method stub
		Integer window = 3;
		
		String unigram;
		String bigram;
		String trigram;
		
		
		int[] sentenceMarker = new int[words.length];
		
		List<String> sentence = array2List(words);
		
		for(int i = 0; i < aspectList.size()-1; i++){
			List<String> aspect = aspectList.get(i);
			for (int j = 0; j < aspect.size(); j++) {
				if(sentence.contains(aspect.get(j))){
					int pos = sentence.indexOf(aspect.get(j));
					int begin = ((pos - window) > 0 ? pos - window : 0);
					int end = ((pos + window) < sentence.size() ? pos + window : sentence.size()-1);
					
					// create the frequency list
					List<Integer> aspectFrequency = new ArrayList<Integer>();
					for (int idx = 0; idx < aspectList.size(); ++idx) {
						if (idx != i)
							aspectFrequency.add(0);
						else
							aspectFrequency.add(1);
					}
					
					// update frequency list to the phrase
					for (int idx = begin; idx <= end; ++idx) {
						sentenceMarker[idx]++;
						
						// unigram
						unigram = words[idx];
						if (frequencyMap2.containsKey(unigram)) {
							List<Integer> oldList = frequencyMap2.get(unigram);
							List<Integer> newFrequency = mergeList(oldList, aspectFrequency);
							frequencyMap2.put(unigram, newFrequency);
						} else {
							frequencyMap2.put(unigram, aspectFrequency);
						}
						
						// bigram
						if (end-idx > 0) {
							bigram = words[idx] + words[idx+1];
							if (frequencyMap3.containsKey(bigram)) {
								List<Integer> oldList = frequencyMap3.get(bigram);
								List<Integer> newFrequency = mergeList(oldList, aspectFrequency);
								frequencyMap3.put(bigram, newFrequency);
							} else {
								frequencyMap3.put(bigram, aspectFrequency);
							}
						}
						
						// trigram
						if (end-idx > 1) {
							trigram = words[idx] + words[idx+1] + words[idx+2];
							if (frequencyMap4.containsKey(trigram)) {
								List<Integer> oldList = frequencyMap4.get(trigram);
								List<Integer> newFrequency = mergeList(oldList, aspectFrequency);
								frequencyMap4.put(trigram, newFrequency);
							} else {
								frequencyMap4.put(trigram, aspectFrequency);
							}
						}
						
					}
					
					//aspectSentences[i]+=1;
					//break;
				}
			}
		}
		
		// create the frequency list for other phrases
		List<Integer> aspectFrequency = new ArrayList<Integer>();
		for (int idx = 0; idx < aspectList.size()-1; ++idx) {
			aspectFrequency.add(0);		
		}
		aspectFrequency.add(1);
		
		for(int i = 1; i < words.length; i++){
			if (sentenceMarker[i] == 0) {
				unigram = words[i];
				if(frequencyMap2.containsKey(unigram)){
					List<Integer> oldList = frequencyMap2.get(unigram);
					List<Integer> newFrequency = mergeList(oldList, aspectFrequency);
					frequencyMap2.put(unigram, newFrequency);
				}else{
					frequencyMap2.put(unigram, aspectFrequency);
				}
				
				if ((i < words.length-1) && (sentenceMarker[i+1] == 0)) {
					bigram = words[i] + words[i+1];
					if(frequencyMap3.containsKey(bigram)){
						List<Integer> oldList = frequencyMap3.get(bigram);
						List<Integer> newFrequency = mergeList(oldList, aspectFrequency);
						frequencyMap3.put(bigram, newFrequency);
					}else{
						frequencyMap3.put(bigram, aspectFrequency);
					}
					
					if ((i < words.length-2) && (sentenceMarker[i+2] == 0)) {
						trigram = words[i] + words[i+1] + words[i+2];
						if(frequencyMap4.containsKey(trigram)){
							List<Integer> oldList = frequencyMap4.get(trigram);
							List<Integer> newFrequency = mergeList(oldList, aspectFrequency);
							frequencyMap4.put(trigram, newFrequency);
						}else{
							frequencyMap4.put(trigram, aspectFrequency);
						}
					}
				}
			}
			
		}
		
		
	}

	private List<Integer> mergeList(List<Integer> oldList,
			List<Integer> aspectFrequency) {
		List<Integer> newList = new ArrayList<Integer>();
		for(int i=0; i < oldList.size(); i++){
			newList.add(oldList.get(i)+aspectFrequency.get(i));
		}
		return newList;
	}
	/**
	 * write the list of bigrams and their frequency related to an aspect
	 * @param filename
	 * @throws IOException
	 */
	public void writeBigrams(String filename) throws IOException {
		// TODO Auto-generated method stub
		FileWriter writer = new FileWriter(filename);
		BufferedWriter writerBW = new BufferedWriter(writer);
		String bigram;
		Set<String> keySet = frequencyMap.keySet();
		Iterator<String> keyIter = keySet.iterator();
		while(keyIter.hasNext()){
			bigram = keyIter.next();
			boolean haveAspect = false;
			for(int i=0; i < aspectList.size()-1; i++){
				List<String> aspect = aspectList.get(i);
				for (int j = 0; j < aspect.size(); j++) {
					if(bigram.contains(aspect.get(j))){
						haveAspect = true;
					}
				}
			}
			
			if(!haveAspect){ //only write the bigram if does not contain the aspect
				List<Integer> aspectFrequency = frequencyMap.get(bigram);
				String freqList = aspectFrequency.toString();
				String aspectBigram = bigram+","+freqList.substring(1, freqList.length()-1)+"\n";
				writerBW.write(aspectBigram);
			}
		}
		writerBW.close();
	}
	
	public void loadAspectFrequency(String filename) throws IOException{
		FileReader bigramFileReader = new FileReader(filename);
		BufferedReader bigramBR = new BufferedReader(bigramFileReader);
		String bigramInfo;
		String bigram;
		while((bigramInfo = bigramBR.readLine())!=null){
			String biInfo[] = bigramInfo.split("[,\\]\\[]");
			List<Integer> aspectFrequencies = new ArrayList<Integer>();
			bigram = biInfo[0];
			for(int i=1; i<biInfo.length;i++){
				aspectFrequencies.add(Integer.parseInt(biInfo[i]));
			}
			frequencyMap.put(bigram, aspectFrequencies);
		}
		bigramBR.close();
	}

	
	/**
	 * select a list of bigrams related to aspects
	 * @param filename
	 * @throws IOException
	 */
	public List<BigramAspect> selectBigrams(Integer featNum) {
		// TODO Auto-generated method stub
		String bigram;
		Set<String> keySet = frequencyMap.keySet();
		Iterator<String> keyIter = keySet.iterator();
		List<Integer> bigramTotal_eachAspect = new ArrayList<Integer>();
		List<BigramAspect> selected = new ArrayList<BigramAspect>();
		while(keyIter.hasNext()){
			bigram = keyIter.next();
			boolean haveAspect = false;
			for(int i=0; i < aspectList.size()-1; i++){
				List<String> aspect = aspectList.get(i);
				for (int j = 0; j < aspect.size(); j++) {
					if(bigram.contains(aspect.get(j))){
						haveAspect = true;
					}
				}
				
				// for each aspect, count the total number of frequency of all bigrams
				if (!haveAspect) {
					List<Integer> aspectFrequency = frequencyMap.get(bigram);
					
					if (bigramTotal_eachAspect.size() > i)
						bigramTotal_eachAspect.set(i, bigramTotal_eachAspect.get(i) + aspectFrequency.get(i));
					else
						bigramTotal_eachAspect.add(aspectFrequency.get(i));
				}
			}
			
		}
		
		for(int i=0; i < aspectList.size()-1; i++){
			List<String> aspect = aspectList.get(i);
			Map<String, Double> bigramList = new TreeMap<String, Double>();
			keyIter = keySet.iterator();
			while(keyIter.hasNext()){
				bigram = keyIter.next();
				boolean haveAspect = false;
			
				for (int j = 0; j < aspect.size(); j++) {
					if(bigram.contains(aspect.get(j))){
						haveAspect = true;
					}
				}
			
			
				if(!haveAspect){ //only write the bigram if does not contain the aspect
					List<Integer> aspectFrequency = frequencyMap.get(bigram);
					int bigramFreq_allAspect = 0;
					double validRatio = 0;
					for (int freq : aspectFrequency) {
						bigramFreq_allAspect += freq;
					}
					validRatio = (double)aspectFrequency.get(i) / bigramFreq_allAspect;
					if (validRatio > 0.1) {
						double overallRatio = (double)aspectFrequency.get(i) / bigramTotal_eachAspect.get(0);
						double score = Math.log(validRatio * overallRatio);
						bigramList.put(bigram, score);
					}
				}
			}
			
			bigramList = MapUtil.sortByValue(bigramList);
			Integer cnt = featNum;
			for (Map.Entry<String, Double> entry : bigramList.entrySet()) {
				BigramAspect ba = new BigramAspect(entry.getKey(), aspectNameList.get(i));
				selected.add(ba);
//				System.out.println(aspectNameList.get(i) + " : " + entry.getKey() + " = " + entry.getValue());
				cnt--;
				if (cnt <= 0)
					break;
			}
		}
		
		return selected;
	}



	public HashMap<String, List<Integer>> getFrequencyMap() {
		return frequencyMap;
	}


	public List<List<String>> getAspectList() {
		return aspectList;
	}


	public int[] getAspectSentences() {
		return aspectSentences;
	}


}
