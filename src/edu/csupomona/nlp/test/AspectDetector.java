/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.test;

import edu.csupomona.nlp.aspect.AspectParser;
import edu.csupomona.nlp.ml.NaiveBayes;
import edu.csupomona.nlp.ml.NaiveBayesResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Xing
 */
public class AspectDetector {
    private final String PATH_ASPECT = "./data/aspects/";
    private final String PATH_TRAIN = "./data/training/";
    private final String PATH_NGRAM = "./data/ngrams/";
    private final String PATH_TEST = "./data/testing/";
    
    private final NaiveBayes nb;
    
    public AspectDetector() {
        nb = new NaiveBayes();
    }
    
    public void collectNGram() throws IOException {
        AspectParser ap = new AspectParser();

        // window size 3
        ap.parse(3, 1, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // unigram
        ap.parse(3, 2, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // bigram
        ap.parse(3, 3, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // trigram

        // window size 999
        ap.parse(999, 1, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // unigram
        ap.parse(999, 1, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // bigram
        ap.parse(999, 1, PATH_ASPECT, PATH_TRAIN, PATH_NGRAM);   // trigram
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
                System.out.println(items[0] + "===>" + items[1]);
                NaiveBayesResult nbRet = nb.classify(items[1]);
                String label = "[" + nbRet.getLabel() + "]";
                
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
            
            for (String key : map.keySet()) {
                System.out.println(key + "]: " + map.get(key)[1] + "/"
                                    + map.get(key)[0]);
            }
        }
        
        
    }
    
    public static void main(String[] args) throws IOException {
        AspectDetector ad = new AspectDetector();
        
        // collect n-gram information
//        ad.collectNGram();
        
        // train a naive bayes classifier
        ad.trainNB();
        
        // test on 
        ad.testNB();
    }
}
