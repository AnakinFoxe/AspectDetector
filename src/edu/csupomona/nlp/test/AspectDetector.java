/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.test;

import edu.csupomona.nlp.aspect.AspectParser;
import edu.csupomona.nlp.ml.NaiveBayes;
import java.io.IOException;

/**
 *
 * @author Xing
 */
public class AspectDetector {
    
    
    public static void main(String[] args) throws IOException {
//        AspectParser ap = new AspectParser();
//        String aspectPath = "./data/aspects/";
//        String trainPath = "./data/training/";
        String ngramPath = "./data/ngrams/";
//        String testPath = "./data/testing/";
//        
//        // window size 3
//        ap.parse(3, 1, aspectPath, trainPath, ngramPath);   // unigram
//        ap.parse(3, 2, aspectPath, trainPath, ngramPath);   // bigram
//        ap.parse(3, 3, aspectPath, trainPath, ngramPath);   // trigram
//        
//        // window size 999
//        ap.parse(999, 1, aspectPath, trainPath, ngramPath);   // unigram
//        ap.parse(999, 1, aspectPath, trainPath, ngramPath);   // bigram
//        ap.parse(999, 1, aspectPath, trainPath, ngramPath);   // trigram
        
        NaiveBayes nb = new NaiveBayes();
        
        nb.train(ngramPath);
        
        
    }
}
