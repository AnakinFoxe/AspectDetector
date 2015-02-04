/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.aspectdetector;

import edu.csupomona.nlp.ml.supervised.NaiveBayes;
import edu.csupomona.nlp.ml.supervised.NaiveBayesResult;
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
        
        sentDect = new SentenceDetector("en");
        
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
    private Long[] loadAspects(String path)
            throws IOException{
        // drop old list and construct new
        this.aspects = new ArrayList<>();
        this.aspectWords = new ArrayList<>();
        
        File[] files = new File(path).listFiles();
        Arrays.sort(files);
        
//        System.out.println("Loading Aspects:");
        for (File file : files) {
            if (file.getName().equals(".DS_Store"))
                continue;

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
        Long[] aspectSentence = new Long[aspectWords.size()];
        for (int idx =0; idx < aspectSentence.length; ++idx)
            aspectSentence[idx] = 0L;
        return aspectSentence;
    }

    /**
     * Load aspects from a list
     * @param aspects       List of aspects
     * @return
     */
    private Long[] loadAspects(List<String> aspects, List<List<String>> aspectWords) {
        // drop old list and copy new
        this.aspects = new ArrayList<>(aspects);
        this.aspectWords = new ArrayList<>(aspectWords);


        // add a list for aspect not defined by user
        // NOTE: "others" has to be at the tail of the list
        List<String> others = new ArrayList<>();
        others.add("other");
        this.aspectWords.add(others);

        // create aspect sentence
        Long[] aspectSentence = new Long[aspectWords.size()];
        for (int idx =0; idx < aspectSentence.length; ++idx)
            aspectSentence[idx] = 0L;
        return aspectSentence;
    }

    private void parseFile(Integer W, Integer N,
            File file, HashMap<String, List<Integer>> freqMap,
            Long[] aspectSentences)
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
    
    private void writeAspectSent(Long[] aspectSentences,
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
    
    private List<File> getFiles(String path) throws IOException {
        List<File> files = new ArrayList<>();
        
        File[] candidates = new File(path).listFiles();
        for (File file : candidates) {
            if (file.isFile())
                files.add(file);
            else
                files.addAll(getFiles(file.getCanonicalPath()));
        }
        
        return files;
    }

    /**
     * Parse files and obtain N-gram information
     * @param W                 Window size
     * @param N                 N of N-gram
     * @param aspectsPath       Path to folder contains aspects
     * @param trainSetPath      Path to folder contains training data
     * @param ngramsPath        Path to output folder
     * @return
     * @throws IOException
     */
    public HashMap<String, List<Integer>> parse(Integer W, Integer N,
            String aspectsPath, 
            String trainSetPath,
            String ngramsPath) 
            throws IOException {
        System.out.print("[W" + W.toString() + "_N" + N.toString() + "]");
        
        // construct new frequency map and aspect sentences
        HashMap<String, List<Integer>> freqMap = new HashMap<>();

        // load aspect related words
        Long[] aspectSentences = loadAspects(aspectsPath);
        
        // for each file in training set path
        List<File> files = getFiles(trainSetPath);
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

    /**
     * Parse files and obtain N-gram information using aspect list and aspect word list
     * @param W                     Window size
     * @param N                     N of N-gram
     * @param aspects               aspect list
     * @param aspectWords           aspect word list
     * @param trainSetPath          Path to folder contains training data
     * @param freqMap               frequency map containing N-gram information
     * @return
     * @throws IOException
     */
    public Long[] parse(Integer W, Integer N,
                        List<String> aspects,
                        List<List<String>> aspectWords,
                        String trainSetPath,
                        HashMap<String, List<Integer>> freqMap)
            throws IOException {
        // load aspect related words
        Long[] aspectSentences = loadAspects(aspects, aspectWords);

        // for each file in training set path
        List<File> files = getFiles(trainSetPath);
        for (File file : files) {
            parseFile(W, N, file, freqMap, aspectSentences);
        }

        return aspectSentences;
    }


    /**
     * Parse files and obtain N-gram information using aspect path for aspect list
     * @param W                     Window size
     * @param N                     N of N-gram
     * @param aspectsPath           Path to folder contains aspects
     * @param trainSetPath          Path to folder contains training data
     * @param freqMap               frequency map containing N-gram information
     * @return
     * @throws IOException
     */
    public Long[] parse(Integer W, Integer N,
                        String aspectsPath,
                        String trainSetPath,
                        HashMap<String, List<Integer>> freqMap)
            throws IOException {
        // load aspect related words
        Long[] aspectSentences = loadAspects(aspectsPath);

        // for each file in training set path
        List<File> files = getFiles(trainSetPath);
        for (File file : files) {
            parseFile(W, N, file, freqMap, aspectSentences);
        }

        return aspectSentences;
    }
    
    public void collectNGram(int operation) throws IOException {
        File[] files = new File(PATH_NGRAM).listFiles();
        if (files != null)
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
    
    public HashMap<String, List<String>> testNB() 
            throws FileNotFoundException, IOException {
        HashMap<String, List<String>> mapAspectSents = new HashMap<>();
        List<File> files = getFiles(PATH_TEST);
        
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
                
                // repare aspect <-> sentences mapping
                List<String> sents;
                if (mapAspectSents.containsKey(label))
                    sents = mapAspectSents.get(label);
                else
                    sents = new ArrayList<>();
                sents.add(items[1]);
                mapAspectSents.put(label, sents);
                
                // for report display
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
            
            // display results
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
        
        return mapAspectSents;
    }
    
    public HashMap<String, List<String>> classifyNB() 
            throws FileNotFoundException, IOException {
        HashMap<String, List<String>> mapAspectSents = new HashMap<>();
        List<File> files = getFiles(PATH_TEST);
        
        for (File file : files) {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                NaiveBayesResult nbRet = nb.classify(line.trim());
                String label = "[" + nbRet.getLabel() + "]";
                
//                System.out.println(items[0] + "===>" + label);
                
                // repare aspect <-> sentences mapping
                List<String> sents;
                if (mapAspectSents.containsKey(label))
                    sents = mapAspectSents.get(label);
                else
                    sents = new ArrayList<>();
                sents.add(line.trim());
                mapAspectSents.put(label, sents);
            }
        }
        
        return mapAspectSents;
    }

    public String detectAspect(String sentence) {
        NaiveBayesResult nbRet = nb.classify(sentence);
        return nbRet.getLabel();
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
