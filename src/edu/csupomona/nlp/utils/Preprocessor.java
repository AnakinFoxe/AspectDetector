package edu.csupomona.nlp.utils;


public class Preprocessor {
	
	public Preprocessor() {
		
	}
	
	private static String removeHtmlTag(String line) {
		return line.replaceAll("<.+?>", "");
	}
	
	private static String removeUrl(String line) {
		return line.replaceAll("http[s]?://[\\w\\d\\./]+", "");
	}
	
	private static String removePunctuation(String line) {
		String parsed = line.replaceAll("([0-9]+)\"", "\1 inch");
		parsed = line.replaceAll("\"", "");
		parsed = line.replaceAll("\\*", "");
		parsed = line.replaceAll("\\$([0-9]+)", "\1 dollars");
		parsed = line.replaceAll(" @ ", " at ");
		
		return parsed;
	}
	
	// for now just remove them
	private static String removeHtmlCode(String line) {
		return line.replaceAll("&#[0-9]+", "");
	}
	
	public static String preprocess(String line) {
		String parsed = removeHtmlTag(line);
		parsed = removeUrl(parsed);
		parsed = removeHtmlCode(parsed);
		parsed = removePunctuation(parsed);
		
		return parsed;
	}
	
//	public static void main(String[] args) {
//		String line = "NOTE: this comes from a non-rooter.<br /><br />I previously had a HTC Desire HD that costed about the same as this phone two years ago when I bought it, and I didn't realize how &#34;un&#34;smart my HTC was until I finally switched over to Nexus 5.  All Tech Specs aside, this phone just runs really fast and smoothly, and fairly easy to get your hands on, even though I do have a few complaints to follow.  Since my previous phone was an Android too, my new Nexus 5 just automatically synced and picked up all the apps I downloaded to the previous phone, and I thought that was pretty smart.  Can't really tell what difference Android 4.4 makes as opposed to the really old 2.3.X that I had before, but I suppose it has better compatability with certain apps.<br /><br />Now the complaints, there aren't many.  I do not appreciate being forced to use Google Hangout as the source of my SMS (text message).  Once I enter into a chat, I am having a hard time telling whether I am chatting over text messaging, or a web source.  In my opinion, Hangout is a pretty versatile add-on tool to Chrome, but it has a poor user-interface.  I think the phone version of Hangout is actually a bit easier to use; the one on PC is almost impossible to use.  Great for video conference calls, but very difficult to find or add new contacts.  You basically have to type in the exact e-mail address in order to find the right person.  TMI on how I feel about Hangout, but that is why I don't like it to handle all my text messages.  I am also having a hard time figuring out how to remove apps from the phone w/o having to go through the Play store.<br /><br />Battery life seems moderate, definitely not impressive.  Can't comment on useful life since I only had it for a week now.  One thing I don't like though is it does not allow you to swap the battery which is the most consumed part of a phone.  I don't see how it's going to survive two years (my normal cycle for a phone) without a battery replacement.<br /><br />Overall, I think it's an amazing phone for its price.  Never had a top-of-the-line phone like a Samsung Note 3 or iPhone 5S, but they can't be twice as good like their price tags suggest.  Final note - you are better off buying direct from the Play store, they sell the 16GB models for $349.99, and you pay about $385 after tax and shipping.  (Sorry Amazon, you have great prices for almost everything but not on this phone lol)";
//		
//		Preprocessor pre = new Preprocessor();
//		
//		System.out.println(pre.preprocess(line));
//	}

}
