package edu.csupomona.nlp.utils;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


public class SemanticSimilarity {
	private static ILexicalDatabase db;
	private static RelatednessCalculator rc;
	private static List<POS[]> posPairs;
	
	public static void init() {
		db = new NictWordNet();
		WS4JConfiguration.getInstance().setMFS(true);
		
		// use Jcn 
		rc = new JiangConrath(db);
		posPairs = rc.getPOSPairs();
	}
	
	public static double getSim(String w1, String w2) {
		String word1 = StanfordTools.lemmatize(w1);
		String word2 = StanfordTools.lemmatize(w2);
		
		double maxScore = -1D;
		double score = 0;
		
		List<Concept> synsets1;
		List<Concept> synsets2;
		Relatedness relatedness;

		for(POS[] posPair: posPairs) {
		    synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		    synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		    for(Concept synset1: synsets1) {
		        for (Concept synset2: synsets2) {
		            relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
		            score = relatedness.getScore();
		            if (score > maxScore) { 
		                maxScore = score;
		            }
		        }
		    }
		}

		if (maxScore == -1D) {
		    maxScore = 0.0;
		}

//		System.out.println("sim('" + word1 + "', '" + word2 + "') =  " + maxScore);
		
		return maxScore;
	}

}
