/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.aspect;

import edu.csupomona.nlp.util.NGram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Xing
 */
public class NGramParser {
    
    public NGramParser() {
        
    }
    
    private void updateFreqMap(Integer aspectIdx, Integer nAspect,
            HashMap<String, Integer> map,
            HashMap<String, List<Integer>> frequencyMap) {
        for (String key : map.keySet()) {
            List<Integer> frequency = new ArrayList<>();
            for (int idx = 0; idx < nAspect; idx++) {
                // retrieve the old count
                int count = 0;
                if (frequencyMap.containsKey(key))
                    count = frequencyMap.get(key).get(idx);
                
                // update new count
                if (idx != aspectIdx)
                    frequency.add(count);
                else
                    frequency.add(count + map.get(key));
            }
            frequencyMap.put(key, frequency);
        }
        
    }
    
    public void parseNGram(Integer W, Integer N, 
            List<String> words, List<List<String>> aspectWords,
            HashMap<String, List<Integer>> frequencyMap,
            int[] aspectSentences) {
        int begin = 0;
        int end = 0;    // when sentence contains no aspect words
        boolean hasAspectWord = false;
        
        // search through all the aspects (neglacting "others")
        for (int i = 0; i < aspectWords.size()-1; i++) {
            // search through all aspect words for each aspect
            for (int j = 0; j < aspectWords.get(i).size(); j++) {
                String aspectWord = aspectWords.get(i).get(j);
                if (words.contains(aspectWord)) {
                    // update aspect sentences count
                    hasAspectWord = true;
                    aspectSentences[i]++;
                    
                    // boundary of the window area
                    int pos = words.indexOf(aspectWord);
                    begin = ((pos - W) > 0 ? pos - W : 0);
                    end = ((pos + W) < words.size() ? 
                            pos + W : words.size()-1);
                    
                    // extract n-gram within the window
                    HashMap<String, Integer> map = new HashMap<>();
                    NGram.updateNGram(N, map, words.subList(begin, end));
                    
                    // update the frequency map
                    updateFreqMap(i, aspectWords.size(), 
                            map, frequencyMap);
                }
            }
        }
            
        // extract the before window part of sentence 
        HashMap<String, Integer> map = new HashMap<>();
        NGram.updateNGram(N, map, words.subList(0, begin));
        updateFreqMap(aspectWords.size()-1, aspectWords.size(), 
                map, frequencyMap);
        
        // extract the after window part of sentence
        map = new HashMap<>();
        NGram.updateNGram(N, map, words.subList(end, words.size()));
        updateFreqMap(aspectWords.size()-1, aspectWords.size(), 
                map, frequencyMap);
        
        // no aspect word was found
        if (!hasAspectWord) 
            aspectSentences[aspectSentences.length-1]++;
    }
    
}
