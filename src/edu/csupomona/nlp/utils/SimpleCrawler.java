package edu.csupomona.nlp.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class SimpleCrawler {
	
	private static String main_url = "http://www.amazon.com/product-reviews/";
	private static String next_rest1 = "/ref=cm_cr_pr_top_link_next_";
	private static String next_rest2 = "?ie=UTF8&pageNumber=";
	private static String next_rest3 = "&showViewpoints=0&sortBy=bySubmissionDateDescending";
	
	public SimpleCrawler() {
		
	}
	
	private static String getReviewUrl(String product_id) {
		return main_url + product_id;
	}

	private static String getNextUrl(String base_url, Integer page_id) {
		return base_url + next_rest1 + page_id.toString() + next_rest2 + page_id.toString() + next_rest3;
	}
	
	private static List<String> scapeReview(String base_url) {
		List<String> reviews = new ArrayList<String>();
		
		try {
			Integer n_page = 1;
			URL url = new URL(base_url);
			while (true) {
				URLConnection connect = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
				String line;
				Integer n_reviews = 0;
				System.out.println("Parsing Page " + n_page + "...");
				while ((line = in.readLine()) != null) {
					Pattern reviews_pattern = Pattern.compile("<div class=\"reviewText\">([\\w\\W]*?)</div>");
					Matcher reviews_matcher = reviews_pattern.matcher(line);
					
					while (reviews_matcher.find()) {
						String text = reviews_matcher.group(1);
						
						text = Preprocessor.preprocess(text);	// should be put somewhere else
						
						reviews.add(text);
						n_reviews++;
						//System.out.println(text);
					}
				}
				in.close();
				
				if (n_reviews == 0) {
					System.out.println("Done Parsing at Page " + n_page.toString());
					break;
				}
				
				n_page++;
				url = new URL(getNextUrl(base_url, n_page));
			}
				
			
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException");
		} catch (IOException e) {
			System.out.println("IOException");
		} catch (PatternSyntaxException e) {
			System.out.println("PatternSyntaxException");
		}
		
		return reviews;
	}
	
	public static List<String> crawl(String product_id) {		
		
		String base_url = getReviewUrl(product_id);
		List<String> reviews = scapeReview(base_url);
		
		return reviews;
	}
	
//	public static void main(String[] args) {
//		String product_id = "B00GDBN6KQ";	// nexus 5
//		
//		SimpleCrawler sc = new SimpleCrawler();
//		Preprocessor pre = new Preprocessor();
//		
//		String base_url = sc.getReviewUrl(product_id);
//		List<String> reviews = sc.scapeReview(base_url);
//		
//		for (String review : reviews) 
//			System.out.println(pre.preprocess(review));
//	}
}
