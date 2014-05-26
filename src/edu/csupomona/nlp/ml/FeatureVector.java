package edu.csupomona.nlp.ml;
import java.util.List;

import edu.csupomona.nlp.aspect.BigramAspect;


public interface FeatureVector {
	/**
	 * this outputs the features that we want to use in our naive bayes algorithm
	 * @param pathname
	 * @return
	 */
	public List<String> getFeatures(String pathname);
	
	/**
	 * This will parse a sentence into its bigram form using the accepted features.
	 * @param sentence
	 * @return a list of bigrams that are valid
	 */
	public List<BigramAspect> getBigrams(String sentence);
}
