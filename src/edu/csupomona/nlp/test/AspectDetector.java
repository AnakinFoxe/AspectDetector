/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.test;

import edu.csupomona.nlp.aspect.AspectParser;
import edu.csupomona.nlp.ml.NaiveBayes;
import edu.csupomona.nlp.ml.NaiveBayesResult;
import edu.csupomona.nlp.util.SentenceDetector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Xing
 */
public class AspectDetector {
    // list contains user defined aspects
    private List<String> aspects;
    
    // list contains user defined words related to each aspect
    private List<List<String>> aspectWords;  
    
    // break iterator (not very accurate?)
    private final SentenceDetector sentDect;
    
    // aspect parser
    private final AspectParser ap;
    
    
    private final String PATH_ASPECT = "./data/aspects/";
    private final String PATH_TRAIN = "./data/training/";
    private final String PATH_NGRAM = "./data/ngrams/";
    private final String PATH_TEST = "./data/testing/";
    
    private final NaiveBayes nb;
    
    public AspectDetector() {
        nb = new NaiveBayes();
        
        sentDect = new SentenceDetector();
        
        ap = new AspectParser();
    }
    
    /**
     * Get aspect words from the aspect file.
     * Each word should possess a single line
     * @param file          Aspect file containing aspect related words
     * @return              List of aspect words
     * @throws IOException 
     */
    private List<String> getAspectWords(File file) throws IOException{
        FileReader fr = new FileReader(file);
        List<String> words;
        try (BufferedReader br = new BufferedReader(fr)) {
            String word;
            words = new ArrayList<>();
            while((word = br.readLine())!=null){
                word = word.trim(); // remove whitespace
                if(!word.isEmpty()){
                    words.add(word);
                }
            }
        }
        
        return words;
    }
    
    /**
     * Load all aspect and related words defined by user
     * @param path          Path to the folder contains aspect files
     * @throws IOException 
     */
    private int[] loadAspects(String path) 
            throws IOException{
        // drop old list and construct new
        this.aspects = new ArrayList<>();
        this.aspectWords = new ArrayList<>();
        
        File[] files = new File(path).listFiles();
        Arrays.sort(files);
        
//        System.out.println("Loading Aspects:");
        for (File file : files) {
            String aspect = file.getName();
//            System.out.println(aspect);
            
            // get user defined words for each aspect
            List<String> words = getAspectWords(file);
            
            // add to specific list
            this.aspectWords.add(words);
            this.aspects.add(aspect);
        }
        
        // add a list for aspect not defined by user
        // NOTE: "others" has to be at the tail of the list
        List<String> others = new ArrayList<>();
        others.add("other");
        this.aspectWords.add(others);
        
        // create aspect sentence
        return new int[aspectWords.size()];
    }
    
    private void parseFile(Integer W, Integer N,
            File file, HashMap<String, List<Integer>> freqMap,
            int[] aspectSentences) 
            throws FileNotFoundException, IOException {
        // read the file
        FileReader fr = new FileReader(file);
        try (BufferedReader br = new BufferedReader(fr)) {
            String text;
            
            // parse each line in the file
            while ((text = br.readLine()) != null) {
                // split the reviews into sentences
                List<String> sentences = sentDect.simple(text);

                // loop through each sentence
                for (String sentence : sentences) 
                    ap.parseAspect(sentence, W, N, 
                            aspectWords, freqMap, aspectSentences);
                
            }
        }
    }
    
    private void writeAspectSent(int[] aspectSentences,
                            String filename, 
                            boolean append) throws IOException {
        FileWriter writer = new FileWriter(filename, append);
        try (BufferedWriter writerBW = new BufferedWriter(writer)) {
            int i;
            for (i = 0; i < aspectSentences.length-1; i++) {
                writerBW.write(this.aspects.get(i) + ":" 
                        + aspectSentences[i] + "\n");
            }
            writerBW.write("others:" + aspectSentences[i] + "\n");
        }
    }
    
    
    private void writeFreqMap(HashMap<String, List<Integer>> freqMap,
                            String filename, 
                            boolean append) throws IOException {
        FileWriter writer = new FileWriter(filename, append);
        try (BufferedWriter writerBW = new BufferedWriter(writer)) {
            String ngram;
            
            Iterator<String> keyIter = freqMap.keySet().iterator();
            while (keyIter.hasNext()) {
                ngram = keyIter.next();
                String freqList = freqMap.get(ngram).toString();
                writerBW.write(ngram + ","
                        + freqList.substring(1, freqList.length()-1) + "\n");
            }
        }
    }
    
    public HashMap<String, List<Integer>> parse(Integer W, Integer N,
            String aspectsPath, 
            String trainSetPath,
            String ngramsPath) 
            throws IOException {
        System.out.print("[W" + W.toString() + "_N" + N.toString() + "]");
        
        // construct new frequency map and aspect sentences
        HashMap<String, List<Integer>> freqMap = new HashMap<>();

        // load aspect related words
        int[] aspectSentences = loadAspects(aspectsPath);
        
        // for each file in training set path
        File[] files = new File(trainSetPath).listFiles();
        for (File file : files) {
            parseFile(W, N, file, freqMap, aspectSentences);
        }
        
        // write aspect sentences count
        String asFile = ngramsPath + "aspectSentences.txt";
        writeAspectSent(aspectSentences, asFile, false);
        
        // write frequency map to file
        String ngramFile = ngramsPath + "ngram_W" + W + "_N" + N + ".txt";
        writeFreqMap(freqMap, ngramFile, false);
        
        nb.train(aspects, freqMap, W, N, aspectSentences);
        
        return freqMap;
    }
    
    public void collectNGram(int operation) throws IOException {
        File[] files = new File(PATH_NGRAM).listFiles();
        for (File file : files)
            file.delete();

        // window size 3
        if ((operation & 1) != 0) 
            parse(3, 1, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // unigram
        if ((operation & 2) != 0)
            parse(3, 2, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // bigram
        if ((operation & 4) != 0)
            parse(3, 3, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // trigram

        // window size 999
        if ((operation & 8) != 0)
            parse(999, 1, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // unigram
        if ((operation & 16) != 0)
            parse(999, 2, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // bigram
        if ((operation & 32) != 0)
            parse(999, 3, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // trigram
    }
    
    public void trainNB() throws IOException {
        // train the classifier
        nb.train(PATH_NGRAM);
    }
    
    public void testNB() throws FileNotFoundException, IOException {
        File[] files = new File(PATH_TEST).listFiles();
        
        for (File file : files) {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            HashMap<String, int[]> map = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] items = line.split("::");
               
                NaiveBayesResult nbRet = nb.classify(items[1]);
                String label = "[" + nbRet.getLabel() + "]";
                
//                System.out.println(items[0] + "===>" + label);
                
                int[] count;
                if (map.containsKey(items[0])) {
                    count = map.get(items[0]);
                    
                } else {
                    count = new int[2];
                }
                count[0]++;
                count[1] += (items[0].equals(label))? 1 : 0;
                
                map.put(items[0], count);
            }
            
            System.out.print(",");
            for (String key : map.keySet()) {
//                System.out.println(key + ": " 
//                        + map.get(key)[1] + "/"
//                        + map.get(key)[0] + " = "
//                        + (double)map.get(key)[1]/map.get(key)[0]);
                System.out.print(map.get(key)[1] + "," 
                        + (double)map.get(key)[1]/map.get(key)[0] + ",");
            }
            System.out.print("\n");
        }
        
        
    }
    
    public static void main(String[] args) throws IOException {
        for (int i=1; i<64; i*=2) {
            AspectDetector ad = new AspectDetector();
            // collect n-gram information
            ad.collectNGram(i);

            // train a naive bayes classifier
//            ad.trainNB();

            // test on 
            ad.testNB();
        }
        
//        AspectDetector ad = new AspectDetector();
//        ad.collectNGram(63);
        
    }
}
