package edu.csupomona.nlp.aspect;

public class BigramAspect {
	
	private String bigram;
	private String aspect;
	
	public BigramAspect(String bigram, String aspect) {
		super();
		this.bigram = bigram;
		this.aspect = aspect;
	}

	public String getBigram() {
		return bigram;
	}

	public void setBigram(String bigram) {
		this.bigram = bigram;
	}

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}
	
	

}
