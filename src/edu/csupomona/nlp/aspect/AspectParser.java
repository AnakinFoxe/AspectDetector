/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.aspect;

import edu.csupomona.nlp.util.Stopword;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Xing
 */
public class AspectParser {
    
    // list contains user defined aspects
    private List<String> aspects;
    
    // list contains user defined words related to each aspect
    private List<List<String>> aspectWords;  
    
    // break iterator (not very accurate?)
    private final BreakIterator breakIter;
    
    // n-gram parser
    private final NGramParser ngramParser;
    
    // stopwords removal
    private final Stopword sw;
    
    public AspectParser() {
        this.breakIter = BreakIterator.getSentenceInstance(Locale.US);
        
        this.ngramParser = new NGramParser();
        
        this.sw = new Stopword("E");
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
        List<String> others = new ArrayList<>();
        others.add("other");
        this.aspectWords.add(others);
        
        // create aspect sentence
        return new int[aspectWords.size()];
    }
    
    
    
    /**
     * Remove useless symbols (To Be Replaced In Future)
     * @param sentence
     * @return 
     */
    private String adjustSent(String sentence) {
        return sentence.replaceAll(
                "( +: ?| +\\*+ ?)|[\\[\\] \\(\\)\\.,;!\\?\\+-]", " ");
    }
    
    private void parseFile(File file, HashMap<String, List<Integer>> freqMap,
            int[] aspectSentences) 
            throws FileNotFoundException, IOException {
        // read the file
        FileReader fr = new FileReader(file);
        try (BufferedReader br = new BufferedReader(fr)) {
            String text;
            
            // parse each line in the file
            while ((text = br.readLine()) != null) {
                // split the reviews into sentences
                breakIter.setText(text);
                int start = breakIter.first();
                // loop through each sentence
                for (int end = breakIter.next(); end != BreakIterator.DONE;
                        start = end, end = breakIter.next()) {
                    String sentence = text.substring(start,end);
                    
                    // a little preprocessing
                    String adjustedSentence = adjustSent(sentence);
                    adjustedSentence = adjustedSentence.toLowerCase();
                    
                    // tokenize
                    List<String> words = new ArrayList<>(
                            Arrays.asList(adjustedSentence.split(" +")));
                    
                    // remove stopwords for unigram
                    if (ngramParser.getN() == 1)
                        words = sw.rmStopword(words);
                    
                    // parse n-gram
                    if (words.size() > 0) 
                        ngramParser.parseNGram(words,
                                this.aspectWords,
                                freqMap,
                                aspectSentences);
                }
                
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
        System.out.println("W=" + W.toString() + ",N=" + N.toString());
        
        // construct new frequency map and aspect sentences
        HashMap<String, List<Integer>> freqMap = new HashMap<>();

        // load aspect related words
        int[] aspectSentences = loadAspects(aspectsPath);
        
        // setup parameters
        this.ngramParser.setN(N);
        this.ngramParser.setW(W);
        
        // for each file in training set path
        File[] files = new File(trainSetPath).listFiles();
        for (File file : files) {
            parseFile(file, freqMap, aspectSentences);
        }
        
        // write aspect sentences count
        String asFile = ngramsPath + "aspectSentences.txt";
        writeAspectSent(aspectSentences, asFile, false);
        
        // write frequency map to file
        String ngramFile = ngramsPath + "ngram_W" + W + "_N" + N + ".txt";
        writeFreqMap(freqMap, ngramFile, false);
        
        return freqMap;
    }
}
