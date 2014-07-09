/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.ml;

import edu.csupomona.nlp.util.NGram;
import edu.csupomona.nlp.util.Stopword;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Xing
 */
public class NaiveBayes {
    
    private final List<HashMap<String, List<Integer>>> freqMap;
    private final List<List<Integer>> wN;
    
    private List<int[]> aspectWordSum;
    
    private List<String> aspects;
    private HashMap<String, Integer> aspectSentences;
    
    private int aspectSentTotal;
    
    private final Pattern ptnWN = Pattern.compile("ngram_W([0-9]+)_N([0-9]+).*");
    
    private final Stopword sw;
    
    public NaiveBayes() {
        sw = new Stopword("E");
        
        freqMap = new ArrayList<>();
        wN = new ArrayList<>();
        
        aspects = new ArrayList<>();
        aspectWordSum = new ArrayList<>();
//        aspectSentences = new HashMap<>();
    }
    
    private HashMap<String, List<Integer>> readNGram(File file) 
            throws IOException {
        FileReader fr = new FileReader(file);
        
        String line;
        HashMap<String, List<Integer>> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(fr)) {
            while ((line = br.readLine()) != null) {
                String[] items = line.split(",");
                List<Integer> counts = new ArrayList<>();
                int sum = 0;
                
                // add aspects
                int i;
                for (i=1; i<items.length-1; i++) {
                    int count = Integer.valueOf(items[i].trim());
                    counts.add(count);
                    sum += count;
                }
                
                // add others
                counts.add(Integer.valueOf(items[i].trim()));
                
                // add all
                counts.add(sum);
                
                map.put(items[0], counts);
            }
        }
        
        return map;
    }
    
    private List<Integer> readWN(String filename) {
        Matcher matcher = ptnWN.matcher(filename);
        
        List<Integer> wn = new ArrayList<>();
        if (matcher.matches()) {
            wn.add(Integer.valueOf(matcher.group(1)));  // W
            wn.add(Integer.valueOf(matcher.group(2)));  // N
        }
        
        return wn;
    }
    
    private HashMap<String, Integer> readAspectSent(File file) 
            throws IOException{
        FileReader fr = new FileReader(file);
        
        Integer countAll = 0;
        String line;
        HashMap<String, Integer> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(fr)) {
            while ((line = br.readLine()) != null) {
                String[] items = line.split(":");
                
                map.put(items[0], Integer.valueOf(items[1].trim()));
                
                if (!items[0].equals("others"))
                    countAll += Integer.valueOf(items[1].trim());
                
                this.aspects.add(items[0]);
            }
            
            map.put("all", countAll);
            this.aspects.add("all");
        }

        return map;
    }
    
    private int[] calAspectWordSum(HashMap<String, List<Integer>> map) {
        int[] wordSum = new int[aspectSentences.size()];
        
        for (String key : map.keySet()) {
            int sum = 0;
            int i;
            // update aspect words
            for (i=0; i<map.get(key).size()-2; i++) {
                wordSum[i] += map.get(key).get(i);
                sum += map.get(key).get(i);
            }
            
            // update others
            wordSum[i] += map.get(key).get(i);
            i++;
            
            // update all
            wordSum[i] += sum;
        }
       
        return wordSum;
    }
    
    public void train(String ngramPath) throws IOException {
        File[] files = new File(ngramPath).listFiles();
        
        for (File file : files) {
            
            if (file.getName().contains("ngram")) {
                // parse the ngram files
                freqMap.add(readNGram(file));
                
                // parse the name of the files to obtain W and N info
                wN.add(readWN(file.getName()));
            }
            else 
                // parse the aspect sentences count
                aspectSentences = readAspectSent(file);
        }
        
        aspectSentTotal = aspectSentences.get("all")
                + aspectSentences.get("others");
        
        for (int i=0; i<freqMap.size(); ++i) 
            aspectWordSum.add(calAspectWordSum(freqMap.get(i)));
        
    }
    
    private double calNGramProb(String ngram, String aspect, Integer N, 
            HashMap<String, List<Integer>> map,
            int[] wordSum) {
        List<Integer> count = map.get(ngram);
        int idx = this.aspects.indexOf(aspect);
        int aspectCount = (count != null)? count.get(idx) : 0;
        int v = map.size();
        int total = wordSum[idx];
        
        return (double)(aspectCount + 1.0) / (total + v);
    }
    
    private double calProbability(String aspect, String sentence) {
        String adjustedSentence = sentence.replaceAll("( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
        String[] words = adjustedSentence.split(" +");

        double sentenceProb;
        if(words.length > 0){
            sentenceProb = Math.log((double)this.aspectSentences.get(aspect) 
                    / this.aspectSentTotal);    // bugfix: add Math.log
            
            for (int i=0; i<wN.size(); i++) {
//                int W = wN.get(i).get(0);
                int N = wN.get(i).get(1);
                
                // get n-gram from the sentence
                HashMap<String, Integer> map = new HashMap<>();
                NGram ng = new NGram(N);
                if (N == 1) {
                    String[] trimWords = sw.rmStopword(words);
                    ng.updateNGram(map, trimWords);
                } else 
                    ng.updateNGram(map, words);
                
                // add probability for each n-gram
                for (String ngram : map.keySet()) 
                    sentenceProb += 
                            Math.log(calNGramProb(ngram, aspect, N, 
                                    this.freqMap.get(i),
                                    this.aspectWordSum.get(i)))
                            * map.get(ngram);
            }
        }else{
                sentenceProb = 0.0;
        }
        return sentenceProb;
    }
    
    public NaiveBayesResult classify(String sentence) {
        double max = Double.NEGATIVE_INFINITY;
        String prediction = "others"; //assume it is not talking about any aspects
        List<String> iter1 = aspects.subList(aspects.size()-2, aspects.size());
        for(String aspect : iter1){
            double aspectProb = calProbability(aspect, sentence);
            if(aspectProb > max && aspectProb != 0.0){
                max = aspectProb;
                prediction = aspect;
            }
        }
        
        if(prediction.equals("all")){
            List<String> iter2 = aspects.subList(0, aspects.size()-2);
            
            max = Double.NEGATIVE_INFINITY;
            for(String aspect : iter2){
                double aspectProb = calProbability(aspect, sentence);
                if(aspectProb > max && aspectProb != 0.0){
                    max = aspectProb;
                    prediction = aspect;
                }
            }
        }
        
        return new NaiveBayesResult(prediction, max);
    }

    
}
