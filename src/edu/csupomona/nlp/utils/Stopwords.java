package edu.csupomona.nlp.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Stopwords {
	
	private static HashSet<String> stopwords;
	
	public static void init() {
		try {
			FileReader swFile = new FileReader("stopwords.txt");
			BufferedReader swReader = new BufferedReader(swFile);
			String sw;
			
			stopwords = new HashSet<String>();
			while((sw = swReader.readLine()) != null){
				stopwords.add(sw.replaceAll("\\s+", ""));
			}
			swFile.close();
		} catch (IOException e) {
			System.out.println("Can not open file");
		}
		
	}
	
	public static boolean isStopword(String word) {
		return stopwords.contains(word.replaceAll("\\s+", "").toLowerCase());
	}
	
	public static List<String> rmStopword(List<String> sentence) {
		List<String> newSent = new ArrayList<String>();
		
		for (String w : sentence) {
			if (isStopword(w))
				continue;
			newSent.add(w);
		}
		
		return newSent;
	}
	
	public static String[] rmStopword(String[] sentence) {
		List<String> newSent = new ArrayList<String>();
		
		for (String w : sentence) {
			if (isStopword(w))
				continue;
			newSent.add(w);
		}
		
		String[] arrNewSent = newSent.toArray(new String[0]);
		
		return arrNewSent;
	}
}
