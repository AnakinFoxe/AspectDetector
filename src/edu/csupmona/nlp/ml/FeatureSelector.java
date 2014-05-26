package edu.csupmona.nlp.ml;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.csupomona.nlp.aspect.BigramAspect;
import edu.csupomona.nlp.aspect.WordParser;


public class FeatureSelector implements FeatureVector {

	public List<String> getFeatures(String pathname) {
		// TODO Auto-generated method stub
		WordParser parser = new WordParser("aspects/");
		try {
			parser.readDataStore("data/");
			List<BigramAspect> baList = parser.selectBigrams(50);
			List<String> bigramList = new ArrayList<String>();
			for (BigramAspect ba : baList) {
				bigramList.add(ba.getBigram());
//				System.out.println(ba.getAspect() + " : " + ba.getBigram());
			}
			
			return bigramList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public List<BigramAspect> getBigrams(String sentence) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
