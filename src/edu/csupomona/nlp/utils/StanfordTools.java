package edu.csupomona.nlp.utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;


public class StanfordTools {
	
	private static StanfordCoreNLP pipeline;
	

	
	public static void init() {
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	
	/*
	 * 0 = very negative, 1 = negative, 2 = neutral, 3 = positive,
	 * and 4 = very positive
	 */
	public static Integer sentiment(String line) {
		if (line == null || line.length() == 0)
			return 2;
		
	    Annotation ano = pipeline.process(line);
	    
	    for (CoreMap sentence : ano.get(SentencesAnnotation.class)) {
	    	Tree tree = sentence.get(AnnotatedTree.class);
	    	return RNNCoreAnnotations.getPredictedClass(tree);
	    }
	    
	    return 2;
	}
	
	public static String lemmatize(String word)
    {
        String lemma = null;

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(word);

        // run all Annotators on this text
        pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
            	lemma = token.get(LemmaAnnotation.class);
            }
        }

        return lemma;
    }
	
	public static List<String[]> posTag(String line) {
		List<String[]> list = new ArrayList<String[]>();
		
		Annotation document = new Annotation(line);
		pipeline.annotate(document);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                String[] word = new String[2];
                
                word[0] = token.get(TextAnnotation.class);
            	word[1] = token.get(PartOfSpeechAnnotation.class);
            	
            	list.add(word);
            }
        }
		
		return list;
	}
	
	public static void parser(String line) {
		Annotation document = new Annotation(line);
		pipeline.annotate(document);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {			
			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			
			System.out.println(dependencies.toString("plain"));
        }
	}
	
//	public static void main(String args[]) {
//		StanfordTools.init();
//		StanfordTools.parser("The camera is very good, so is the signal.");
//	}

}
